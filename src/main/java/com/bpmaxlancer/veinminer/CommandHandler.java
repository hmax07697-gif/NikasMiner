package com.bpmaxlancer.veinminer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles VeinMiner commands
 * Commands: /vm toggle, /vm reload
 */
public class CommandHandler implements CommandExecutor, TabCompleter {
    
    private final VeinMiner plugin;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;
    
    public CommandHandler(@NotNull VeinMiner plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, 
                           @NotNull Command command, 
                           @NotNull String label, 
                           @NotNull String[] args) {
        
        // No arguments - show usage
        if (args.length == 0) {
            sender.sendMessage(configManager.getMsgInvalidCommand());
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "toggle":
                return handleToggle(sender);
                
            case "reload":
                return handleReload(sender);
                
            default:
                sender.sendMessage(configManager.getMsgInvalidCommand());
                return true;
        }
    }
    
    /**
     * Handles the toggle subcommand
     * 
     * @param sender Command sender
     * @return true if handled
     */
    private boolean handleToggle(@NotNull CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check permission
        if (!player.hasPermission("veinminer.toggle")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        // Toggle the state
        boolean newState = playerDataManager.toggle(player);
        
        // Send appropriate message
        if (newState) {
            player.sendMessage(configManager.getMsgEnabled());
        } else {
            player.sendMessage(configManager.getMsgDisabled());
        }
        
        return true;
    }
    
    /**
     * Handles the reload subcommand
     * 
     * @param sender Command sender
     * @return true if handled
     */
    private boolean handleReload(@NotNull CommandSender sender) {
        // Check permission
        if (!sender.hasPermission("veinminer.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        // Reload configuration
        try {
            configManager.loadConfig();
            sender.sendMessage(configManager.getMsgReload());
        } catch (Exception e) {
            sender.sendMessage("§cError reloading configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, 
                                     @NotNull Command command, 
                                     @NotNull String alias, 
                                     @NotNull String[] args) {
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - suggest subcommands
            List<String> subCommands = Arrays.asList("toggle", "reload");
            
            String input = args[0].toLowerCase();
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    // Check permissions before suggesting
                    if (subCommand.equals("toggle") && sender.hasPermission("veinminer.toggle")) {
                        completions.add(subCommand);
                    } else if (subCommand.equals("reload") && sender.hasPermission("veinminer.reload")) {
                        completions.add(subCommand);
                    }
                }
            }
        }
        
        return completions;
    }
}
