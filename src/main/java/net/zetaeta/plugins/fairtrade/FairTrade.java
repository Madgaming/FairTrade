package net.zetaeta.plugins.fairtrade;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class FairTrade extends JavaPlugin {

    public static FairTrade plugin;
    public Logger log;
    public FileConfiguration config;
    public FileConfiguration players;
    public File playersFile;
    private FTInventoryListener invListener;
    private FCommandExecutor cExec;
    
    public static boolean useIConomy;
    public static double maxPlayerDistance;
    private static Map<String, Chest> overflowChests = new HashMap<>();
    
    
    public void onDisable() {
        Trade.cancelAll();
        log.info(this + " is now disabled!");
    }
    
    public void onEnable() {
        plugin = this;
        log = getLogger();
        config = getConfig();
        
        invListener = new FTInventoryListener();
        cExec = new FCommandExecutor();
        
        playersFile = new File(getDataFolder(), "players.yml");
        
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        players = YamlConfiguration.loadConfiguration(playersFile);
        
        
        if (config.contains("iconomy_enabled")) {
            useIConomy = config.getBoolean("iconomy_enabled");
        }
        else {
            config.set("iconomy_enabled", false);
            useIConomy = false;
        }
        
        final String playerDistanceConfig = "max_player_distance";
        
        if (config.contains(playerDistanceConfig)) {
            maxPlayerDistance = config.getDouble(playerDistanceConfig);
        }
        else {
            config.set(playerDistanceConfig, -1.0);
        }
        
        loadPlayerInfo();
        
        getCommand("trade").setExecutor(cExec);
        getServer().getPluginManager().registerEvents(invListener, this);
        saveConfig();
        log.info(this + " is now enabled!");
    }
    
    private void loadPlayerInfo() {
        
        if (config.contains("players.chests")) {
            ConfigurationSection chestsSection = players.getConfigurationSection("players.chests");
            Set<String> confPlayers = chestsSection.getKeys(false);
            for (String s : confPlayers) {
                int x = chestsSection.getInt(s + ".x");
                int y = chestsSection.getInt(s + ".y");
                int z = chestsSection.getInt(s + ".z");
                String worldname = chestsSection.getString(s + ".world");
                World world = getServer().getWorld(worldname);
                if (world == null) {
                    log.warning("Player " + s + " has overflow chest in an invalid world!");
                    continue;
                }
                Block chestBlock = world.getBlockAt(x, y, z);
                if (!(chestBlock.getState() instanceof Chest)) {
                    log.warning("Player " + s + " had a chest saved that wasn't a chest");
                    continue;
                }
                overflowChests.put(s, (Chest) chestBlock.getState());
                chestsSection.set(s, null);
            }
        }
        
        if (!players.contains("players.chests")) {
            return;
        }
        
        
        
        ConfigurationSection chestsSection = players.getConfigurationSection("players.chests");
        Set<String> players = chestsSection.getKeys(false);
        for (String s : players) {
            int x = chestsSection.getInt(s + ".x");
            int y = chestsSection.getInt(s + ".y");
            int z = chestsSection.getInt(s + ".z");
            String worldname = chestsSection.getString(s + ".world");
            World world = getServer().getWorld(worldname);
            if (world == null) {
                log.warning("Player " + s + " has overflow chest in an invalid world!");
                continue;
            }
            Block chestBlock = world.getBlockAt(x, y, z);
            if (!(chestBlock.getState() instanceof Chest)) {
                log.warning("Player " + s + " had a chest saved that wasn't a chest");
                continue;
            }
            overflowChests.put(s, (Chest) chestBlock.getState());
        }
        
        
    }


    public static boolean playerHasOverflowChest(Player player) {
        return getPlayerOverflowChest(player) != null;
    }


    public static Inventory getPlayerOverflowChest(Player player) {
        
        Chest chest = overflowChests.get(player.getName());
        if (chest == null) {
            return null;
        }
        return chest.getBlockInventory();
    }

    @SuppressWarnings("boxing")
    public static void addChest(Player player, Chest chest) {
        player.sendMessage("addChest!");
        overflowChests.put(player.getName(), chest);
        player.sendMessage("OverflowAdded!");
        if (!plugin.config.contains("players.chests")) {
            plugin.config.createSection("players.chests");
        }
        ConfigurationSection confSec = plugin.players.getConfigurationSection("players.chests");
        confSec.set(player.getName() + ".x", chest.getLocation().getBlockX());
        confSec.set(player.getName() + ".y", chest.getLocation().getBlockY());
        confSec.set(player.getName() + ".z", chest.getLocation().getBlockZ());
        confSec.set(player.getName() + ".world", chest.getWorld().getName());
        try {
            plugin.players.save(plugin.playersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.saveConfig();
    }
    
}

