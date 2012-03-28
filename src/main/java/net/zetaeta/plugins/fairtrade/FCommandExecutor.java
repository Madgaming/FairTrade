package net.zetaeta.plugins.fairtrade;

import static org.bukkit.Bukkit.getPlayer;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.zetaeta.plugins.libraries.ZPUtil;
import net.zetaeta.plugins.libraries.commands.CommandHandler;
import net.zetaeta.plugins.libraries.commands.Executor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FCommandExecutor implements Executor {
	
	@CommandHandler("trade")
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		switch (args[0]) {
		case "start" :
		case "init" :
		case "begin" :
			return beginTrade(sender, ZPUtil.removeFirstIndex(args));
		case "add" :
		case "additem" :
			return addItem(sender, ZPUtil.removeFirstIndex(args));
		default :
			return false;
		}
	}
	

	private static boolean beginTrade(CommandSender sender, String[] args) {
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
		
		Player invitee = getPlayer(args[0]);
		
		if (FairTrade.plugin.pendingTrades.containsKey(invitee) || FairTrade.plugin.pendingTrades.containsValue(invitee)) {
			sender.sendMessage(new String[] {
				"§aThis player already has a pending trade!",
				"§aAsk them to complete that trade or cancel in order to continue with this trade."
			});
			invitee.sendMessage(new String[] {
					"§aSomeone else has invited you to trade!",
					"If you wish to participate, finish or cancel your current trade!"
			});
			return true;
		}
		
		invitee.sendMessage(new String[] {
				"§aYou have been invited to a trade by §2" + sender.getName() + "§a.",
				"§aUse /trade accept to begin the trade"
		});
		FairTrade.plugin.pendingTrades.put((Player) sender, invitee);
		return true;
	}
	
	
	private boolean addItem(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cThis command can only be run by a player!");
			return true;
		}
		int count = -1;
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
		}
	}
}
