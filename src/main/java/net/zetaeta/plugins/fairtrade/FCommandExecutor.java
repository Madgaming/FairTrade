package net.zetaeta.plugins.fairtrade;

import static net.zetaeta.plugins.fairtrade.Trade.ActionStatus.*;
import static org.bukkit.Bukkit.getPlayer;

import java.util.HashSet;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IInventory;
import net.zetaeta.libraries.ZPUtil;
import net.zetaeta.plugins.fairtrade.Trade.Stage;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("boxing")
public class FCommandExecutor implements CommandExecutor {
    
    private static final HashSet<Byte> transparentBlocks;
    
    static {
        transparentBlocks = new HashSet<Byte>();
        transparentBlocks.add((byte) 0);
        transparentBlocks.add((byte) 8);
        transparentBlocks.add((byte) 9);
        transparentBlocks.add((byte) 10);
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (args.length < 1)
            return false;
        String arg = args[0].toLowerCase();
        if (arg.equals("start") || arg.equals("init") || arg.equals("begin")) {
            return beginTradeCmd(sender, ZPUtil.removeFirstIndex(args));
        }
        if (arg.equals("add") || arg.equals("view") || arg.equals("check") || arg.equals("additem")) {
            return addItem(sender);
        }
        if (arg.equals("addmoney")) {
            return addMoneyCmd(sender, ZPUtil.removeFirstIndex(args));
        }
        if (arg.equals("viewother") || arg.equals("checkother")) {
            return viewOtherCmd(sender, ZPUtil.removeFirstIndex(args));
        }
        if (arg.equals("money")) {
            return moneyCmd(sender, ZPUtil.removeFirstIndex(args));
        }
        if (arg.equals("finish") || arg.equals("end")) {
            return finishCmd(sender, ZPUtil.removeFirstIndex(args));
        }
        if (arg.equals("confirm") || arg.equals("accept")) {
            return confirmTrade(sender, ZPUtil.removeFirstIndex(args));
        }
        if (arg.equals("cancel")) {
            return cancelTradeCmd(sender, ZPUtil.removeFirstIndex(args));
        }
        if (arg.equals("continue") || arg.equals("restart")) {
            return restartTradeCmd(sender, ZPUtil.removeFirstIndex(args));
        }
        if (arg.equals("addchest") || arg.equals("setchest")) {
            return addChestCmd(sender, ZPUtil.removeFirstIndex(args));
        }
        if (arg.equals("chest") || arg.equals("chestinfo")) {
            return chestInfoCmd(sender);
        }
        return false;
    }
    
    private static boolean addChestCmd(CommandSender sender, String[] args) {
        try {
            if (!checkValid(sender, "fairtrade.addchest", args, 0)) {
                return true;
            }
        } catch (InvalidArgumentCountException e) {
            
            return false;
        }
        Player player = (Player) sender;
        
        Block block = player.getTargetBlock(transparentBlocks, 20);
        if (block == null) {
            sender.sendMessage("§cYou have not a chest in range!");
            return true;
        }
        if (!(block.getState() instanceof Chest)) {
            sender.sendMessage("§cYou have not a chest in range!");
            return true;
        }
        FairTrade.addChest(player, (Chest) block.getState());
        sender.sendMessage("§aTrade overflow chest set!");
        return true;
    }

    private static class InvalidArgumentCountException extends Exception {
        private static final long serialVersionUID = 1877056235566428252L;
    }
    
    public static boolean checkValid(CommandSender sender, String permission, String[] args, int numArgs) throws InvalidArgumentCountException {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be run by a player!");
            return false;
        }
        if (!ZPUtil.checkPermission(sender, permission)) {
            return false;
        }
        if (args.length != numArgs) {
            throw new InvalidArgumentCountException();
        }
        return true;
    }


    private static boolean moneyCmd(CommandSender sender, String[] args) {
        if (!FairTrade.useIConomy) {
            sender.sendMessage("§ciConomy is not enabled!");
            return true;
        }
        try {
            if (!checkValid(sender, "fairtrade.money", args, 0))
                return true;
        } catch (InvalidArgumentCountException e1) {
            return false;
        }
        Player player = (Player) sender;
        Trade trade;
        if ((trade = Trade.getTrade(player)) == null) {
            sender.sendMessage("§aYou are not in a trade!");
            return true;
        }
        Player otherPlayer;
        try {
            otherPlayer = trade.getOtherPlayer(player);
        } catch (InvalidPlayerException e) {
            e.printStackTrace();
            return false;
        }
        sender.sendMessage(new String[] {"§aYou have put §2" + trade.getMoney(player) + " §ainto the trade!", "§a§o" + otherPlayer.getDisplayName() + " §ahas put §2" + trade.getMoney(otherPlayer) + " §ainto the trade!"});
        return true;
    }


    private static boolean addMoneyCmd(CommandSender sender, String[] args) {
        if (!FairTrade.useIConomy) {
            sender.sendMessage("§ciConomy is not enabled!");
            return true;
        }
        try {
            if (!checkValid(sender, "fairtrade.addmoney", args, 1))
                return true;
        } catch (InvalidArgumentCountException e1) {
            return false;
        }
        Player player = (Player) sender;
        Trade trade;
        if ((trade = Trade.getTrade(player)) == null) {
            sender.sendMessage("§cYou are not trading!");
            return true;
        }
        
        if (trade.getStage() != Stage.IN_PROCESS) {
            sender.sendMessage("§cYou are not allowed to add money at this point!");
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
        
        boolean moneyAdded;
        try {
            moneyAdded = trade.addMoney(player, amount);
        } catch (InvalidPlayerException e) {
            player.sendMessage("§cSorry, an error occurred! D:");
            e.printStackTrace();
            return true;
        }
        if (moneyAdded) {
            player.sendMessage("§aYou added " + amount + " to your offering in the trade!");
            try {
                trade.getOtherPlayer(player).sendMessage("§a" + player.getDisplayName() + " added " + amount + " to his offerings in the trade!");
            } catch (InvalidPlayerException e) {
                sender.sendMessage("§cAn error occurred! D:");
                e.printStackTrace();
            }
            return true;
        } else {
            player.sendMessage("§cYou do not have " + amount + " in your iConomy account!");
            return true;
        }
    }


    @SuppressWarnings("null")
    public static boolean beginTradeCmd(CommandSender sender, String[] args) {
        try {
            if (!checkValid(sender, "fairtrade.begin", args, 1))
                return true;
        } catch (InvalidArgumentCountException e1) {
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be run by a player!");
            return true;
        }
        if (getPlayer(args[0]) == null) {
            sender.sendMessage("§cNot an online player!");
            return true;
        }
        
        
        Player invitor = (Player) sender;
        Player invitee = getPlayer(args[0]);
        if (invitor.equals(invitee)) {
            invitor.sendMessage("§cYou cannot trade with yourself!");
            return true;
        }
        
        if (FairTrade.maxPlayerDistance > 0) {
            if (invitor.getLocation().distance(invitee.getLocation()) > FairTrade.maxPlayerDistance) {
                invitor.sendMessage("§cYou are too far away from that player!");
                return true;
            }
        }
        
        Trade trade;
        boolean newTrade = false;
        if (Trade.getTrade(invitor) == null) {
//            trade = new Trade(invitor, invitee);
            trade = null;
            newTrade = true;
        }
        else {
            trade = Trade.getTrade(invitor);
            newTrade = false;
        }
        
        
        // If the player already has invited someone
        if (!newTrade) {
            if (trade.getStage() == Stage.PENDING) { // if the trade is a pending trade.
                if (trade.getInvitor().equals(invitor)) { // if the original invitor of the trade is the current invitor
                    if (trade.getInvitee().equals(invitee)) { // if the player already invited this person
                        invitee.sendMessage("§a§o" + invitor.getDisplayName() + "§r§a still wants to trade with you!");
                        invitee.sendMessage("§a - §o/trade begin §a" + invitor.getName() + " to begin trade!");
                        invitee.sendMessage("§a - §o/trade cancel §ato deny the trade");
                        invitor.sendMessage("§aTrade request resent to " + invitee.getDisplayName());
                        return true;
                    }
                    else { // if the person invited someone else
                        trade.getInvitee().sendMessage("§aYour pending trade with " + invitor.getDisplayName() + " has been cancelled.");
                        invitee.sendMessage(new String[] {    "§a" + invitor.getDisplayName() + " has invited you to trade!",
                                                            "§a - §o/trade begin " + invitor.getDisplayName() + " §ato begin the trade",
                                                            "§a - §o/trade cancel §ato deny the trade"});
                        trade.delete();
                        trade = new Trade(invitor, invitee);
                        return true;
                    }
                }
                else if (trade.getInvitee().equals(invitor)) { // if the original invitor invited this person
                    if (trade.getInvitor().equals(invitee)) { // if the original invitor is the person being invited here
                        invitor.sendMessage("§aBeginning trade with " + invitee.getDisplayName());
                        invitee.sendMessage("§aBeginning trade with " + invitor.getDisplayName());
                        trade.setStage(Stage.IN_PROCESS);
                        return true;
                    }
                    else { // if someone else invited this invitor
                        invitor.sendMessage(new String[] {    "§aYou are already in a trade with " + trade.getInvitor().getDisplayName() + "!", 
                                                            "§aUse §o/trade cancel §ato exit!"
                                                        });
                        return true;
                    }
                }
            }
        }
        
        Trade otherTrade = Trade.getTrade(invitee);
        if (otherTrade != null) { // if the invitee is already in a trade
            if (!(otherTrade.getInvitor().equals(invitor) || otherTrade.getInvitee().equals(invitor))) {
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
                "§aYou have been invited to a trade by §a§o" + sender.getName() + "§r§a.",
                "§a - §o/trade begin " + invitor.getDisplayName() + " §ato begin the trade",
                "§a - §o/trade cancel §ato deny the trade"
        });
        invitor.sendMessage("§aTrade request resent to " + invitee.getDisplayName());
        trade = new Trade(invitor, invitee);
        return true;
    }
    
    public static boolean viewOtherCmd(CommandSender sender, String[] args) {
        try {
            if (!checkValid(sender, "fairtrade.viewother", args, 0))
                return true;
        } catch (InvalidArgumentCountException e1) {
            return false;
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
            trade.addActionStatus(INVITOR_VIEWING_INVITEE);
            return true;
        }
        else if (trade.getInvitee().equals(player)) {
            IInventory inv = trade.getChest(trade.getInvitor());
            ((CraftPlayer) player).getHandle().openContainer(inv);
            trade.addActionStatus(INVITEE_VIEWING_INVITOR);
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
    
    
    private static boolean finishCmd(CommandSender sender, String[] args) {
        try {
            if (!checkValid(sender, "fairtrade.finish", args, 0))
                return true;
        } catch (InvalidArgumentCountException e1) {
            return false;
        }
        Player player = (Player) sender;
        Trade trade;
        if ((trade = Trade.getTrade(player)) == null) {
            sender.sendMessage("§cYou are not in a trade!");
            return true;
        }
        trade.setStage(Stage.COMPLETE);
        trade.getInvitor().sendMessage(new String[] {    "§aThe trade has been finished!", 
                                                        "§aCheck the trade chests and money with §o/trade viewother §aand", 
                                                        "§a§o/trade money§a to ensure you have what you want!", 
                                                        "§aThen confirm the trade with §o/trade confirm§a and wait for the other player to do the same, and you will recieve your items."
                                                        });
        trade.getInvitee().sendMessage(new String[] {    "§aThe trade has been finished!", 
                                                        "§aCheck the trade chests and money with §o/trade viewother §aand", 
                                                        "§a§o/trade money§a to ensure you have what you want!", 
                                                        "§aThen confirm the trade with §o/trade confirm§a and wait for the other player to do the same, and you will recieve your items."
                                                        });
        return true;
    }


    private static boolean cancelTradeCmd(CommandSender sender, String[] args) {
        try {
            if (!checkValid(sender, "fairtrade.cancel", args, 0))
                return true;
        } catch (InvalidArgumentCountException e1) {
            return false;
        }
        if (args.length > 1) {
            return false;
        }
        if (!ZPUtil.checkPermission(sender, "fairtrade.cancel")) {
            return true;
        }
        Player player = (Player) sender;
        Trade trade;
        if ((trade = Trade.getTrade(player)) == null) {
            sender.sendMessage("§cYou are not in a trade!");
            return true;
        }
        sender.sendMessage("§aCancelling trade...");
        try {
            trade.getOtherPlayer(player).sendMessage("§a" + player.getDisplayName() + " has cancelled the trade!");
        } catch (InvalidPlayerException e) {
            e.printStackTrace();
        }
        trade.cancel();
        trade.delete();
        return true;
    }
    
    
    private static boolean restartTradeCmd(CommandSender sender, String[] args) {
        try {
            if (!checkValid(sender, "fairtrade.continue", args, 0))
                return true;
        } catch (InvalidArgumentCountException e1) {
            return false;
        }
        Player player = (Player) sender;
        
        Trade trade;
        if ((trade = Trade.getTrade(player)) == null) {
            sender.sendMessage("§cYou are not in a trade!");
            return true;
        }
        if (trade.getInvitor().equals(player)) {
            if (trade.hasActionStatus(INVITEE_HAS_RESTARTED)) {
                trade.setStage(Stage.IN_PROCESS);
                trade.removeActionStatus(INVITEE_HAS_RESTARTED);
                sender.sendMessage("§aGoing back to trading!");
                trade.getInvitee().sendMessage("§aGoing back to trading!");
                return true;
            }
            else if (trade.hasActionStatus(INVITOR_HAS_RESTARTED)) {
                sender.sendMessage("§aYou have already requested to continue the trade!");
                trade.getInvitee().sendMessage("§a" + player.getDisplayName() + " has re-requested that you continue trading!");
                return true;
            }
            trade.addActionStatus(INVITOR_HAS_RESTARTED);
            sender.sendMessage("§aYou have requested to continue trading!");
            trade.getInvitee().sendMessage(new String[] {    "§a" + player.getDisplayName() + " has requested that you continue trading!", 
                                                             "§aUse §o/trade continue §ato go back to trading!"});
            return true;
        }
        if (trade.getInvitee().equals(player)) {
            if (trade.hasActionStatus(INVITOR_HAS_RESTARTED)) {
                trade.setStage(Stage.IN_PROCESS);
                trade.removeActionStatus(INVITOR_HAS_RESTARTED);
                sender.sendMessage("§aGoing back to trading!");
                trade.getInvitor().sendMessage("§aGoing back to trading!");
                return true;
            }
            else if (trade.hasActionStatus(INVITEE_HAS_RESTARTED)) {
                sender.sendMessage("§aYou have already requested to continue the trade!");
                trade.getInvitor().sendMessage("§a" + player.getDisplayName() + " has re-requested that you continue trading!");
                return true;
            }
            trade.addActionStatus(INVITEE_HAS_RESTARTED);
            sender.sendMessage("§aYou have requested to continue trading!");
            trade.getInvitor().sendMessage(new String[] {    "§a" + player.getDisplayName() + " has requested that you continue trading!", 
                                                             "§aUse §o/trade continue §ato go back to trading!"});
            return true;
        }
        sender.sendMessage("§cSorry, an error occurred. Try again?");
        return true;
    }


    private static boolean confirmTrade(CommandSender sender, String[] args) {
        try {
            if (!checkValid(sender, "fairtrade.confirm", args, 0))
                return true;
        } catch (InvalidArgumentCountException e1) {
            return false;
        }
        Player player = (Player) sender;
        Trade trade;
        if ((trade = Trade.getTrade(player)) == null) {
            sender.sendMessage("§cYou are not in a trade!");
            return true;
        }
        if (trade.getStage() != Stage.COMPLETE) {
            sender.sendMessage("§cThe trade is not ready to be confirmed!");
            return true;
        }
        
        if (trade.getInvitor().equals(player)) {
            if (FairTrade.maxPlayerDistance > 0) {
                if (trade.getInvitee().getLocation().distance(player.getLocation()) > FairTrade.maxPlayerDistance) {
                    player.sendMessage("§cYou are too far away from that player!");
                    return true;
                }
            }
            if (trade.hasActionStatus(INVITEE_HAS_CONFIRMED)) {
                player.sendMessage("§aConfirming trade!");
                trade.getInvitee().sendMessage("§aConfirming trade!");
                trade.confirm();
                return true;
            }
            if (trade.hasActionStatus(INVITOR_HAS_CONFIRMED)) {
                sender.sendMessage("§aYou have already confirmed!");
                trade.getInvitee().sendMessage("§a" + player.getDisplayName() + " has re-requested that you confirm the trade!");
                return true;
            }
            sender.sendMessage("§aYou have confirmed the trade! Waiting for other player...");
            trade.getInvitee().sendMessage(new String[] {    "§a" + player.getDisplayName() + " has confirmed the trade!", 
                                                            "§aUse §o/trade confirm §ato confirm it!"});
            trade.addActionStatus(INVITOR_HAS_CONFIRMED);
            return true;
        }
        if (trade.getInvitee().equals(player)) {
            if (FairTrade.maxPlayerDistance > 0) {
                if (trade.getInvitor().getLocation().distance(player.getLocation()) > FairTrade.maxPlayerDistance) {
                    player.sendMessage("§cYou are too far away from that player!");
                    return true;
                }
            }
            if (trade.hasActionStatus(INVITOR_HAS_CONFIRMED)) {
                player.sendMessage("§aConfirming trade!");
                trade.getInvitor().sendMessage("§aConfirming trade!");
                trade.confirm();
                return true;
            }
            if (trade.hasActionStatus(INVITEE_HAS_CONFIRMED)) {
                sender.sendMessage("§aYou have already confirmed!");
                trade.getInvitee().sendMessage("§a" + player.getDisplayName() + " has re-requested that you confirm the trade!");
                return true;
            }
            sender.sendMessage("§aYou have confirmed the trade! Waiting for other player...");
            trade.getInvitor().sendMessage(new String[] {    "§a" + player.getDisplayName() + " has confirmed the trade!", 
                                                            "§aUse §o/trade confirm §ato confirm it!"});
            trade.addActionStatus(INVITEE_HAS_CONFIRMED);
            return true;
        }
        return false;
    }
    
    
    
    
    
    public static boolean addItem(CommandSender sender) {
        String[] args = new String[0];
        try {
            if (!checkValid(sender, "fairtrade.view", args, 0))
                return true;
        } catch (InvalidArgumentCountException e1) {
            return false;
        }
        Player player = (Player) sender;
        Trade trade;
        if ((trade = Trade.getTrade(player)) == null) {
            sender.sendMessage("§cYou are not in a trade!");
            return true;
        }
        
        if (trade.getStage() == Stage.PENDING) {
            sender.sendMessage("§cThe other player has not begun the trade!");
            return true;
        }
        
        if (trade.getChest(player) == null) {
            sender.sendMessage("§cAn error has occurred! D:");
            return true;
        }
        
        IInventory inv = trade.getChest(player);
        
        EntityPlayer ePlayer = ((CraftPlayer) player).getHandle();
        ePlayer.openContainer(inv);
        trade.addActionStatus(trade.getInvitor().equals(player) ? INVITOR_VIEWING_OWN : INVITEE_VIEWING_OWN);
        return true;
    }

    private boolean chestInfoCmd(CommandSender sender) {
        try {
            if (!checkValid(sender, "fairtrade.chestinfo", new String[0], 0)) {
                return true;
            }
        } catch (InvalidArgumentCountException e) {
            return false;
        }
        
        Player player = (Player) sender;
        if (FairTrade.playerHasOverflowChest(player)) {
            Chest chest = FairTrade.getPlayerOverflowChestBlock(player);
            player.sendMessage(new StringBuilder().append("§aYour overflow chest is at §2X: ").append(chest.getX()).append(", Y: ").append(chest.getY()).append(", Z: ").append(chest.getZ()).toString());
        }
        else {
            player.sendMessage("§cYou do not have an overflow chest!");
        }
        return true;
    }

}
