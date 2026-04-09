package com.bpmaxlancer.veinminer;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player-specific data including toggle states and cooldowns
 * Uses PersistentDataContainer for persistent storage
 */
public class PlayerDataManager {
    
    private final VeinMiner plugin;
    private final NamespacedKey enabledKey;
    private final Map<UUID, Long> cooldowns;
    
    public PlayerDataManager(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
        this.enabledKey = new NamespacedKey(plugin, "veinminer_enabled");
        this.cooldowns = new HashMap<>();
    }
    
    /**
     * Checks if VeinMiner is enabled for a player
     * Default is true if not set
     * 
     * @param player Player to check
     * @return true if enabled
     */
    public boolean isEnabled(@NotNull Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        
        // Default to true if not set
        if (!pdc.has(enabledKey, PersistentDataType.BYTE)) {
            return true;
        }
        
        return pdc.get(enabledKey, PersistentDataType.BYTE) == 1;
    }
    
    /**
     * Sets VeinMiner enabled state for a player
     * 
     * @param player Player to set
     * @param enabled Enabled state
     */
    public void setEnabled(@NotNull Player player, boolean enabled) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(enabledKey, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
    }
    
    /**
     * Toggles VeinMiner for a player
     * 
     * @param player Player to toggle
     * @return New enabled state
     */
    public boolean toggle(@NotNull Player player) {
        boolean newState = !isEnabled(player);
        setEnabled(player, newState);
        return newState;
    }
    
    /**
     * Checks if a player is on cooldown
     * 
     * @param player Player to check
     * @return true if on cooldown
     */
    public boolean isOnCooldown(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }
        
        long lastUse = cooldowns.get(uuid);
        long currentTime = System.currentTimeMillis();
        long cooldownMs = plugin.getConfigManager().getCooldown() * 1000L;
        
        return (currentTime - lastUse) < cooldownMs;
    }
    
    /**
     * Gets the remaining cooldown time in seconds
     * 
     * @param player Player to check
     * @return Remaining seconds, or 0 if not on cooldown
     */
    public int getRemainingCooldown(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }
        
        long lastUse = cooldowns.get(uuid);
        long currentTime = System.currentTimeMillis();
        long cooldownMs = plugin.getConfigManager().getCooldown() * 1000L;
        long elapsed = currentTime - lastUse;
        
        if (elapsed >= cooldownMs) {
            return 0;
        }
        
        return (int) Math.ceil((cooldownMs - elapsed) / 1000.0);
    }
    
    /**
     * Sets the cooldown for a player
     * 
     * @param player Player to set cooldown for
     */
    public void setCooldown(@NotNull Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    /**
     * Clears the cooldown for a player
     * 
     * @param player Player to clear cooldown for
     */
    public void clearCooldown(@NotNull Player player) {
        cooldowns.remove(player.getUniqueId());
    }
    
    /**
     * Saves all player data
     * Called on plugin disable
     */
    public void saveAll() {
        // PDC data is automatically saved by Bukkit
        // Clear in-memory cooldowns
        cooldowns.clear();
    }
}
