package net.zetaeta.plugins.fairtrade;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class FairTrade extends JavaPlugin {
    
    public static FairTrade plugin;
    public Logger log;
    private FTInventoryListener invListener;
    private FCommandExecutor cExec;
    private static Map<String, Chest> overflowChests = new HashMap<>();
    public FileConfiguration config;
    
    
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
        loadPlayerInfo();
        
        getCommand("trade").setExecutor(cExec);
        getServer().getPluginManager().registerEvents(invListener, this);
        log.info(this + " is now enabled!");
    }
    
    private void loadPlayerInfo() {
        if (!config.contains("players.chests")) {
            return;
        }
        ConfigurationSection chestsSection = config.getConfigurationSection("players.chests");
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
        ConfigurationSection confSec = plugin.config.getConfigurationSection("players.chests");
        confSec.set(player.getName() + ".x", chest.getLocation().getBlockX());
        confSec.set(player.getName() + ".y", chest.getLocation().getBlockY());
        confSec.set(player.getName() + ".z", chest.getLocation().getBlockZ());
        confSec.set(player.getName() + ".world", chest.getWorld().getName());
        plugin.saveConfig();
    }
    
}

