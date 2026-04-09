package com.bpmaxlancer.veinminer;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * VeinMiner - Main plugin class
 * Allows players to mine entire ore veins, tree logs, and crop patches
 * 
 * @author bpmaxlancer
 * @version 1.0.0
 */
public class VeinMiner extends JavaPlugin {
    
    private static VeinMiner instance;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private VeinMinerLogic veinMinerLogic;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        configManager = new ConfigManager(this);
        playerDataManager = new PlayerDataManager(this);
        veinMinerLogic = new VeinMinerLogic(this);
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(veinMinerLogic, this);
        
        // Register commands
        CommandHandler commandHandler = new CommandHandler(this);
        getCommand("veinminer").setExecutor(commandHandler);
        getCommand("veinminer").setTabCompleter(commandHandler);
        
        // Log successful startup
        getLogger().info("VeinMiner v" + getDescription().getVersion() + " has been enabled!");
        getLogger().info("Loaded " + configManager.getTotalEnabledBlocks() + " enabled block types");
    }
    
    @Override
    public void onDisable() {
        // Save any pending data
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        
        getLogger().info("VeinMiner has been disabled!");
        instance = null;
    }
    
    /**
     * Gets the plugin instance
     * 
     * @return VeinMiner instance
     */
    @NotNull
    public static VeinMiner getInstance() {
        return instance;
    }
    
    /**
     * Gets the configuration manager
     * 
     * @return ConfigManager instance
     */
    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Gets the player data manager
     * 
     * @return PlayerDataManager instance
     */
    @NotNull
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    /**
     * Gets the vein mining logic handler
     * 
     * @return VeinMinerLogic instance
     */
    @NotNull
    public VeinMinerLogic getVeinMinerLogic() {
        return veinMinerLogic;
    }
}
