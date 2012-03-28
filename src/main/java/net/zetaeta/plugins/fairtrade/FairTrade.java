package net.zetaeta.plugins.fairtrade;

import net.zetaeta.libraries.InterMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FairTrade extends JavaPlugin {
	
	public InterMap<Player, Player> pendingTrades = new InterMap<Player, Player>();
	
	public static FairTrade plugin;
	
    public void onDisable() {
        // TODO: Place any custom disable code here.
    }

    public void onEnable() {
    	
    }
}

