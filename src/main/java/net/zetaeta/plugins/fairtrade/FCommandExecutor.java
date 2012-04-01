package net.zetaeta.plugins.fairtrade;

import static org.bukkit.Bukkit.getPlayer;
import static net.zetaeta.plugins.fairtrade.FairTrade.plugin;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IInventory;
import net.zetaeta.plugins.fairtrade.Trade.Status;
import net.zetaeta.plugins.libraries.ZPUtil;
import net.zetaeta.plugins.libraries.commands.CommandHandler;
import net.zetaeta.plugins.libraries.commands.Executor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class FCommandExecutor implements Executor {
	
	@CommandHandler("trade")
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		switch (args[0]) {
		case "start" :
		case "init" :
		case "begin" :
			return beginTradeCmd(sender, ZPUtil.removeFirstIndex(args));
		case "add" :
		case "additem" :
			return addItem(sender);
		case "addmoney" :
			return addMoneyCmd(sender, ZPUtil.removeFirstIndex(args));
		case "view" :
		case "viewother" :
			return viewOtherCmd(sender, ZPUtil.removeFirstIndex(args));
//		case "accept" :
//			return acceptTrade(sender);
		default :
			return false;
		}
	}
	
	
	private boolean addMoneyCmd(CommandSender sender, String[] args) {
		if (!ZPUtil.checkPermission(sender, "addmoney")) {
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cThis command can only be run by a player!");
			return true;
		}
		if (args.length != 1) {
			return false;
		}
		Player player = (Player) sender;
		if (Trade.getTrade(player) == null) {
			sender.sendMessage("§cYou are not trading!");
			return true;
		}
		
		
		double amount;
		
		try {
			amount = Double.parseDouble(args[0]);
		}
		catch(NumberFormatException e) {
			sender.sendMessage("§cInvalid number format!");
			sender.sendMessage("§cPlease user /trade addmoney <positive/negative number>");
			return true;
		}
		
		Trade trade = Trade.getTrade(player);
		
		boolean moneyAdded;
		try {
			moneyAdded = trade.addMoney(player, amount);
		} catch (InvalidPlayerException e) {
			player.sendMessage("Sorry, an error occurred! D:");
			e.printStackTrace();
			return true;
		}
		
		player.sendMessage("§aYou added " + amount + " to your offering in the trade!");
		trade.getInvitee().sendMessage("§a" + player.getDisplayName() + " added " + amount + " to his offerings in the trade!");
		return false;
	}


	public static boolean beginTradeCmd(CommandSender sender, String[] args) {
		if (!ZPUtil.checkPermission(sender, "fairtrade.begin")) {
			return true;
		}
		if (args.length > 1) {
			sender.sendMessage("§cUsage: /trade begin <player>");
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cThis command can only be run by a player!");
		}
		if (getPlayer(args[0]) == null) {
			sender.sendMessage("§cNot an online player!");
		}
		
		
		Player invitor = (Player) sender;
		Player invitee = getPlayer(args[0]);
		Trade trade;
		boolean newTrade = false;
		if (Trade.getTrade(invitor) == null) {
			trade = new Trade(invitor, invitee);
		}
		else {
			trade = Trade.getTrade(invitor);
			newTrade = true;
		}
		
		// If the player already has a pending invite
		if (trade.getStatus() == Status.PENDING) {
			if (trade.getInvitee().equals(invitee)) {
				trade.setStatus(Status.IN_PROCESS);
			}
/*			if (plugin.pendingTrades.getByValue(invitor).equals(invitee)) {
				trade = new Trade(invitor, invitee);
				return true;
			}*/
			 trade.delete();
		}
		
		// If the player already has invited someone
		if (!newTrade) {
			if (trade.getInvitee().equals(invitee)) {
				invitee.sendMessage("§a§o" + invitor.getDisplayName() + "§r§a still wants to trade with you!");
				invitee.sendMessage("§2 - §l/trade begin §ar§a" + invitor.getName() + " to begin trade!");
				invitee.sendMessage("§2 - §l/trade refuse §r§ato deny the trade");
				invitor.sendMessage("§aTrade request resent to " + invitee.getDisplayName());
			}
			else {
				plugin.pendingTrades.removeKey(invitor);
				plugin.pendingTrades.putKey(invitor, invitee);
			}
		}
		
		if (FairTrade.plugin.pendingTrades.containsKey(invitee) || FairTrade.plugin.pendingTrades.containsValue(invitee)) {
			if (!plugin.pendingTrades.getFull(invitee).equals(invitor)) {
				sender.sendMessage(new String[] {
					"§aThis player already has a pending trade!",
					"§aAsk them to complete that trade or cancel in order to continue with this trade."
				});
				invitee.sendMessage(new String[] {
						"§a§o" + invitor.getDisplayName() + "§r§ahas invited you to trade!",
						"§aIf you wish to participate, finish or cancel your current trade!"
				});
				return true;
			}
		}
		
		invitee.sendMessage(new String[] {
				"§aYou have been invited to a trade by §2§o" + sender.getName() + "§r§a.",
				"§aUse /trade accept to begin the trade"
		});
		FairTrade.plugin.pendingTrades.put((Player) sender, invitee);
		return true;
	}


	public static boolean acceptTrade(CommandSender sender) {
		//  TODO: Replace this method with actual acceptTrade()
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cThis command can only be run by a player!");
			return true;
		}
		
		Player player = (Player) sender;
		
		if (!(Trade.playersInTrades.contains(player))) {
			sender.sendMessage("§cYou do not have a ");
		}
		return false;
	}
	
	
	public static boolean viewOtherCmd(CommandSender sender, String[] args) {
		if (!(sender instanceof CraftPlayer)) {
			sender.sendMessage("§cThis command can only be run by a player!");
			return true;
		}
		Player player = (Player) sender;
		Trade trade = Trade.getTrade(player);
		if (trade == null) {
			sender.sendMessage("§aYou are not in a trade!");
			return true;
		}
		if (trade.getInvitor().equals(player)) {
			IInventory inv = trade.getChest(trade.getInvitee());
			((CraftPlayer) player).getHandle().openContainer(inv);
			trade.addStage(Trade.Status.CurrentStage.INVITOR_VIEWING_INVITEE);
			return true;
		}
		else if (trade.getInvitee().equals(player)) {
			IInventory inv = trade.getChest(trade.getInvitor());
			((CraftPlayer) player).getHandle().openContainer(inv);
			trade.addStage(Trade.Status.CurrentStage.INVITEE_VIEWING_INVITOR);
			return true;
		}
		sender.sendMessage("§cAn error occurred!");
		try {
			throw new InvalidPlayerException("Expected " + CraftPlayer.class.getName() + " for player " + player.getName() + ", got " + player.getClass().getName() + "!");
		} catch (InvalidPlayerException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	
	public static boolean addItem(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cThis command can only be run by a player!");
			return true;
		}
		
		if (!ZPUtil.checkPermission(sender, "fairtrade.additem")) {
			return true;
		}
		
		Player player = (Player) sender;
		if (!(Trade.playersInTrades.contains(player))) {
			sender.sendMessage("§cYou are not in a trade!");
			return true;
		}
		
		Trade trade = Trade.getTrade(player);
		
		if (trade == null) {
			sender.sendMessage("§cAn error has occurred! D:");
			return true;
		}
		
		if (trade.getChest(player) == null) {
			sender.sendMessage("§cAn error has occurred! D:");
			return true;
		}
		
		IInventory inv = trade.getChest(player);
		
		EntityPlayer ePlayer = ((CraftPlayer) player).getHandle();
		ePlayer.openContainer(inv);
		return true;
		
/*		int count = -1;
		if (args.length == 1) {
			try {
				count = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				
			}
		} else if (args.length > 1) {
			return false;
		} else {
			count = -1;
		}
		Player adder = (Player) sender;
		ItemStack adderItems = adder.getItemInHand().clone();
		if (count == -1) {
			count = adderItems.getAmount();
		}
		else if (adderItems.getAmount() < count) {
			sender.sendMessage("§cYou do not have enough items in your hand!");
			return true;
		}
		else if (adderItems.getAmount() > count) {
			adderItems.setAmount(count);
		}
		
		if (adderItems.getEnchantments().size() == 0) {
			Map<Enchantment, Integer> enchantmentMap = adderItems.getEnchantments();
			StringBuilder enchantsAsString = new StringBuilder(enchantmentMap.size() * 10);
			Set<Enchantment> enchantments = enchantmentMap.keySet();
			Enchantment[] enchantsArray = new Enchantment[enchantments.size()];
			enchantments.toArray(enchantsArray);
			int l = enchantsArray.length - 1;
			
			for (int i=0; i<l; i++) {
				enchantsAsString.append(enchantsArray[i].getName()).append(", ");
			}
			
			enchantsAsString.append(enchantsArray[l]).append(". ");
			StringBuilder adderMessage = new StringBuilder(enchantsAsString.length() + 40 + 4 + 21);
			adderMessage.append("§aYou are adding ").append(count).append(" of ").append(adderItems.getType().toString()).append(" with enchantments ").append(enchantsAsString);
			sender.sendMessage(adderMessage.toString());
			
			StringBuilder otherMessage = new StringBuilder(enchantsAsString.length() + 36 + 4 + 21 + 16);
			otherMessage.append("§a").append(sender.getName()).append(" is adding ").append(count).append(" of ").append(adderItems.getType().toString()).append(" with enchantments ").append(enchantsAsString);
			Trade thisTrade = Trade.getTrade((Player) sender);
			Trade.getOtherPlayer(adder).sendMessage(otherMessage.toString());
			adder.getInventory()
		}*/
	}
}
