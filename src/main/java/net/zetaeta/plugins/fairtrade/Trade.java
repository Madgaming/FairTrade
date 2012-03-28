package net.zetaeta.plugins.fairtrade;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.TileEntityChest;
import net.zetaeta.libraries.InterMap;

import org.bukkit.entity.Player;

public class Trade {
	public static InterMap<Player, Player> playersInTrades = new InterMap<Player, Player>();
	public static Map<Player, Trade> trades = new HashMap<Player, Trade>();
	
	private Player player1;
	private Player player2;
	private TileEntityChest chest1;
	private TileEntityChest chest2;
	
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
		TileEntityChest c1;
		TileEntityChest c2;
		if (player.equals(player1)) {
			c1 = chest2;
			c2 = chest1;
		}
		else if (player.equals(player2)) {
			c1 = chest1;
			c2 = chest2;
		}
		else {
			return null;
		}
		return new InventoryLargeChest("Trade with " + Trade.getOtherPlayer(player).getDisplayName(), c1, c2);
	}
}
