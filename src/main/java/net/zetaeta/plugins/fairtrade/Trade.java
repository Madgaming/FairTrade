package net.zetaeta.plugins.fairtrade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.TileEntityChest;
import net.zetaeta.libraries.InterMap;

import org.bukkit.entity.Player;

public class Trade {
	public static InterMap<Player, Player> playersInTrades = new InterMap<Player, Player>();
	public static Map<Player, Trade> trades = new HashMap<Player, Trade>();
	public static Set<Player> playersWithChestOpen = new HashSet<Player>();
	
	private Player player1;
	private Player player2;
//	private TileEntityChest chest1a;
//	private TileEntityChest chest2a;
//	private TileEntityChest chest1b;
//	private TileEntityChest chest2b;
	private InventoryLargeChest chest1;
	private InventoryLargeChest chest2;
	
	
	public static Trade getTrade(Player player) {
		if (trades.containsKey(player)) {
			return trades.get(player);
		}
		return null;
	}
	
	
	public static Player getOtherPlayer(Player player) {
		if (playersInTrades.contains(player)) {
			return playersInTrades.get(player);
		}
		return null;
	}


	public InventoryLargeChest getChest(Player player) {
		Trade trade = trades.get(player);
		if (trade == null) {
			return null;
		}
		if (player.equals(player1)) {
			return chest1;
		}
		else if (player.equals(player2)) {
			return chest2;
		} else {
			return null;
		}
	}
}
