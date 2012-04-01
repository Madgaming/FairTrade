package net.zetaeta.plugins.fairtrade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.TileEntityChest;
import net.zetaeta.libraries.InterMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.util.NumberConversions;

import com.iCo6.system.Account;

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
	private double plr1Money;
	private double plr2Money;
	private Player invitor;
	private Status status;
	private int currentStage;
	
	public enum Status {
		PENDING, IN_PROCESS, COMPLETE;
		public class CurrentStage {
			public static final int INVITOR_VIEWING_INVITEE = 0b1;
			public static final int INVITEE_VIEWING_INVITOR = 0b10;
		}
	}
	
	public Trade(Player invitor, Player invitee) {
		player1 = invitor;
		player2 = invitee;
		this.invitor = invitor;
		trades.put(invitor, this);
		trades.put(invitee, this);
		status = Status.PENDING;
	}
	
	
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

	public void begin() {
		status = Status.IN_PROCESS;
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
	
	@SuppressWarnings("boxing")
	public boolean addMoney(Player player, double amount) throws InvalidPlayerException {
		if (player.equals(player1)) {
			Account acct = new Account(player.getName());
			if (amount < 0) {
				if (acct.getHoldings().getBalance() < -amount) {
					return false;
				}
			}
			plr1Money += amount;
			acct.getHoldings().add(amount);
			return true;
		}
		else if (player.equals(player2)) {
			Account acct = new Account(player.getName());
			if (amount < 0) {
				if (acct.getHoldings().getBalance() < -amount) {
					return false;
				}
			}
			plr2Money += amount;
			acct.getHoldings().add(amount);
			return true;
		}
		throw new InvalidPlayerException();
	}
	
	
	public Player getInvitor() {
		return invitor;
	}
	
	
	public Player getInvitee() {
		return player1.equals(invitor) ?  player2 : player1;
	}
	
	
	public Status getStatus() {
		return status;
	}
	
	
	public void setStatus(Status status) {
		this.status = status;
	}


	public void delete() {
		trades.remove(player1);
		trades.remove(player2);
		chest1 = null;
		chest2 = null;
		invitor = null;
		player1 = null;
		player2 = null;
		plr1Money = 0;
		plr2Money = 0;
		status = null;
	}
	
	
	public void addStage(int stage) {
		currentStage &= stage;
	}
	
	
	public int getStage() {
		return currentStage;
	}
	
	
	public boolean removeStage(int stage) {
		if ((currentStage & stage) == 0) {
			return false;
		}
		currentStage &= ~stage;
		return true;
	}
}
