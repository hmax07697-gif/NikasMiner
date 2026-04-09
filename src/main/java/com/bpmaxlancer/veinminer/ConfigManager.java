package com.bpmaxlancer.veinminer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages plugin configuration and settings
 */
public class ConfigManager {
    
    private final VeinMiner plugin;
    private FileConfiguration config;
    
    // Settings
    private int maxBlocks;
    private int cooldown;
    private boolean particlesEnabled;
    private boolean soundsEnabled;
    
    // Enabled blocks categorized by type
    private Set<Material> oreBlocks;
    private Set<Material> logBlocks;
    private Set<Material> cropBlocks;
    
    // Messages
    private String msgEnabled;
    private String msgDisabled;
    private String msgCooldown;
    private String msgNoPermission;
    private String msgReload;
    private String msgInvalidCommand;
    
    public ConfigManager(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Loads or reloads the configuration
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Load settings
        maxBlocks = config.getInt("settings.max-blocks", 50);
        cooldown = config.getInt("settings.cooldown", 2);
        particlesEnabled = config.getBoolean("settings.particles", true);
        soundsEnabled = config.getBoolean("settings.sounds", true);
        
        // Load enabled blocks
        oreBlocks = loadMaterialSet("enabled-blocks.ores");
        logBlocks = loadMaterialSet("enabled-blocks.logs");
        cropBlocks = loadMaterialSet("enabled-blocks.crops");
        
        // Load messages
        msgEnabled = colorize(config.getString("messages.enabled", "&a✓ VeinMiner enabled!"));
        msgDisabled = colorize(config.getString("messages.disabled", "&c✗ VeinMiner disabled!"));
        msgCooldown = colorize(config.getString("messages.cooldown", "&eYou must wait {time} seconds before using VeinMiner again."));
        msgNoPermission = colorize(config.getString("messages.no-permission", "&cYou don't have permission to use VeinMiner on this block type."));
        msgReload = colorize(config.getString("messages.reload", "&aVeinMiner configuration reloaded successfully!"));
        msgInvalidCommand = colorize(config.getString("messages.invalid-command", "&cUsage: /vm [toggle|reload]"));
        
        plugin.getLogger().info("Configuration loaded successfully");
    }
    
    /**
     * Loads a set of materials from a config path
     * 
     * @param path Configuration path
     * @return Set of materials
     */
    @NotNull
    private Set<Material> loadMaterialSet(@NotNull String path) {
        List<String> materialNames = config.getStringList(path);
        Set<Material> materials = new HashSet<>();
        
        for (String name : materialNames) {
            try {
                Material material = Material.valueOf(name.toUpperCase());
                materials.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in config: " + name);
            }
        }
        
        return materials;
    }
    
    /**
     * Colorizes a message string with Minecraft color codes
     * 
     * @param message Message to colorize
     * @return Colorized message
     */
    @NotNull
    private String colorize(@NotNull String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Checks if a material is enabled for vein mining
     * 
     * @param material Material to check
     * @return true if enabled
     */
    public boolean isBlockEnabled(@NotNull Material material) {
        return oreBlocks.contains(material) || 
               logBlocks.contains(material) || 
               cropBlocks.contains(material);
    }
    
    /**
     * Gets the block type category (ore, log, crop)
     * 
     * @param material Material to check
     * @return Block type or null if not enabled
     */
    public String getBlockType(@NotNull Material material) {
        if (oreBlocks.contains(material)) return "ores";
        if (logBlocks.contains(material)) return "logs";
        if (cropBlocks.contains(material)) return "crops";
        return null;
    }
    
    /**
     * Gets the total number of enabled blocks
     * 
     * @return Total count
     */
    public int getTotalEnabledBlocks() {
        return oreBlocks.size() + logBlocks.size() + cropBlocks.size();
    }
    
    // Getters for settings
    
    public int getMaxBlocks() {
        return maxBlocks;
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    public boolean isParticlesEnabled() {
        return particlesEnabled;
    }
    
    public boolean isSoundsEnabled() {
        return soundsEnabled;
    }
    
    // Getters for enabled blocks
    
    @NotNull
    public Set<Material> getOreBlocks() {
        return Collections.unmodifiableSet(oreBlocks);
    }
    
    @NotNull
    public Set<Material> getLogBlocks() {
        return Collections.unmodifiableSet(logBlocks);
    }
    
    @NotNull
    public Set<Material> getCropBlocks() {
        return Collections.unmodifiableSet(cropBlocks);
    }
    
    // Getters for messages
    
    @NotNull
    public String getMsgEnabled() {
        return msgEnabled;
    }
    
    @NotNull
    public String getMsgDisabled() {
        return msgDisabled;
    }
    
    @NotNull
    public String getMsgCooldown() {
        return msgCooldown;
    }
    
    @NotNull
    public String getMsgNoPermission() {
        return msgNoPermission;
    }
    
    @NotNull
    public String getMsgReload() {
        return msgReload;
    }
    
    @NotNull
    public String getMsgInvalidCommand() {
        return msgInvalidCommand;
    }
}
