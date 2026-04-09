package com.bpmaxlancer.veinminer;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Core logic for vein mining functionality
 * Handles recursive vein detection and block breaking
 */
public class VeinMinerLogic implements Listener {
    
    private final VeinMiner plugin;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;
    
    // Directions to check for adjacent blocks (6 faces + 20 edges/corners = 26 total)
    private static final int[][] DIRECTIONS = {
        // Faces (6)
        {1, 0, 0}, {-1, 0, 0},
        {0, 1, 0}, {0, -1, 0},
        {0, 0, 1}, {0, 0, -1},
        // Edges (12)
        {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
        {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
        {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1},
        // Corners (8)
        {1, 1, 1}, {1, 1, -1}, {1, -1, 1}, {1, -1, -1},
        {-1, 1, 1}, {-1, 1, -1}, {-1, -1, 1}, {-1, -1, -1}
    };
    
    public VeinMinerLogic(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();
        
        // Check if player is sneaking
        if (!player.isSneaking()) {
            return;
        }
        
        // Check if VeinMiner is enabled for this player
        if (!playerDataManager.isEnabled(player)) {
            return;
        }
        
        // Check if block is enabled for vein mining
        if (!configManager.isBlockEnabled(blockType)) {
            return;
        }
        
        // Check permissions based on block type
        String blockTypeCategory = configManager.getBlockType(blockType);
        if (blockTypeCategory != null) {
            String permission = "veinminer.use." + blockTypeCategory;
            if (!player.hasPermission(permission)) {
                player.sendMessage(configManager.getMsgNoPermission());
                return;
            }
        }
        
        // Check cooldown
        if (playerDataManager.isOnCooldown(player)) {
            int remaining = playerDataManager.getRemainingCooldown(player);
            String message = configManager.getMsgCooldown()
                .replace("{time}", String.valueOf(remaining));
            player.sendMessage(message);
            event.setCancelled(true);
            return;
        }
        
        // Get the item in hand
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        // Find the vein
        Set<Block> vein = findVein(block, blockType, configManager.getMaxBlocks());
        
        // If vein only has the initial block, no need to do anything special
        if (vein.size() <= 1) {
            return;
        }
        
        // Set cooldown
        playerDataManager.setCooldown(player);
        
        // Break all blocks in the vein
        breakVein(player, vein, block.getLocation(), tool);
    }
    
    /**
     * Finds all connected blocks of the same type using recursive algorithm
     * 
     * @param startBlock Starting block
     * @param targetType Material to match
     * @param maxBlocks Maximum blocks to find
     * @return Set of blocks in the vein
     */
    @NotNull
    private Set<Block> findVein(@NotNull Block startBlock, 
                                @NotNull Material targetType, 
                                int maxBlocks) {
        
        Set<Block> vein = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        
        queue.add(startBlock);
        vein.add(startBlock);
        
        while (!queue.isEmpty() && vein.size() < maxBlocks) {
            Block current = queue.poll();
            
            // Check all 26 adjacent blocks
            for (int[] direction : DIRECTIONS) {
                Block adjacent = current.getRelative(direction[0], direction[1], direction[2]);
                
                // Skip if already processed
                if (vein.contains(adjacent)) {
                    continue;
                }
                
                // Check if same material
                if (adjacent.getType() == targetType) {
                    vein.add(adjacent);
                    queue.add(adjacent);
                    
                    // Stop if we've reached the max
                    if (vein.size() >= maxBlocks) {
                        break;
                    }
                }
            }
        }
        
        return vein;
    }
    
    /**
     * Breaks all blocks in the vein with proper durability and drops
     * 
     * @param player Player breaking the vein
     * @param vein Set of blocks to break
     * @param dropLocation Location where all drops should spawn
     * @param tool Tool being used
     */
    private void breakVein(@NotNull Player player, 
                          @NotNull Set<Block> vein, 
                          @NotNull Location dropLocation, 
                          @NotNull ItemStack tool) {
        
        World world = dropLocation.getWorld();
        if (world == null) return;
        
        // Check if tool has Unbreaking enchantment
        int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
        boolean hasUnbreaking = unbreakingLevel > 0;
        
        int blocksBroken = 0;
        
        for (Block block : vein) {
            // Get drops for this block
            Collection<ItemStack> drops = block.getDrops(tool, player);
            
            // Break the block
            block.setType(Material.AIR);
            
            // Drop items at origin location
            for (ItemStack drop : drops) {
                world.dropItemNaturally(dropLocation, drop);
            }
            
            // Apply durability damage
            if (tool.getType() != Material.AIR && !tool.getType().isAir()) {
                ItemMeta meta = tool.getItemMeta();
                if (meta instanceof Damageable) {
                    Damageable damageable = (Damageable) meta;
                    
                    // Calculate if durability should be consumed
                    // Unbreaking has a chance to not consume durability
                    boolean consumeDurability = true;
                    if (hasUnbreaking) {
                        // Unbreaking formula: 100 / (level + 1) % chance to consume
                        Random random = new Random();
                        int chance = 100 / (unbreakingLevel + 1);
                        consumeDurability = random.nextInt(100) < chance;
                    }
                    
                    if (consumeDurability) {
                        int currentDamage = damageable.getDamage();
                        damageable.setDamage(currentDamage + 1);
                        tool.setItemMeta(meta);
                        
                        // Check if tool broke
                        if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
                            tool.setAmount(0);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                            break; // Stop breaking blocks
                        }
                    }
                }
            }
            
            blocksBroken++;
        }
        
        // Play effects
        if (configManager.isParticlesEnabled()) {
            world.spawnParticle(Particle.BLOCK_CRACK, dropLocation, 
                50, 0.5, 0.5, 0.5, 0.1, 
                vein.iterator().next().getBlockData());
        }
        
        if (configManager.isSoundsEnabled()) {
            world.playSound(dropLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
        }
    }
}
