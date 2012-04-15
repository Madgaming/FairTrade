package net.zetaeta.plugins.fairtrade;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.IInventory;
import net.minecraft.server.ItemStack;
import net.minecraft.server.TileEntityChest;
import net.zetaeta.libraries.InterMap;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.iCo6.system.Account;

public class Trade {
    public static InterMap<Player, Player> playersInTrades = new InterMap<Player, Player>();
    public static Map<Player, Trade> trades = new HashMap<Player, Trade>();
    public static Set<Player> playersWithChestOpen = new HashSet<Player>();
    
    private TileEntityChest invitorChest;
    private TileEntityChest inviteeChest;
    private double invitorMoney;
    private double inviteeMoney;
    private Player invitor;
    private Player invitee;
    private Stage currentStage;
    private int actionStatus;
    private boolean isCancelled;
    
    public enum Stage {
        PENDING, IN_PROCESS, COMPLETE
    }
    public static class ActionStatus {
        public static final int INVITOR_VIEWING_INVITEE = 0x01;      // 0b00000001;
        public static final int INVITEE_VIEWING_INVITOR = 0x02;      // 0b00000010;
        public static final int INVITOR_VIEWING_OWN     = 0x04;      // 0b00000100;
        public static final int INVITEE_VIEWING_OWN     = 0x08;      // 0b00001000;
        public static final int INVITOR_HAS_CONFIRMED   = 0x10;      // 0b00010000;
        public static final int INVITEE_HAS_CONFIRMED   = 0x20;      // 0b00100000;
        public static final int INVITOR_HAS_RESTARTED   = 0x40;      // 0b01000000;
        public static final int INVITEE_HAS_RESTARTED   = 0x80;      // 0b10000000;
    }
    
    public Trade(Player invitor, Player invitee) {
        this.invitor = invitor;
        this.invitee = invitee;
        trades.put(invitor, this);
        trades.put(invitee, this);
        invitorChest = new TileEntityChest();
        inviteeChest = new TileEntityChest();
        currentStage = Stage.PENDING;
    }
    
    
    public static Trade getTrade(Player player) {
        if (trades.containsKey(player)) {
            return trades.get(player);
        }
        return null;
    }
    
    
    public Player getOtherPlayer(Player player) throws InvalidPlayerException {
        if (player.equals(invitor)) {
            return invitee;
        }
        else if (player.equals(invitee)) {
            return invitor;
        }
        throw new InvalidPlayerException("This trade does not contain " + player.getName());
    }

    public void begin() {
        currentStage = Stage.IN_PROCESS;
    }

    
    public IInventory getChest(Player player) {
        Trade trade = trades.get(player);
        if (trade == null) {
            return null;
        }
        if (player.equals(invitor)) {
            return invitorChest;
        }
        else if (player.equals(invitee)) {
            return inviteeChest;
        } 
        else {
            return null;
        }
    }
    
    @SuppressWarnings("boxing")
    public boolean addMoney(Player player, double amount) throws InvalidPlayerException {
        if (!FairTrade.useIConomy) {
            return false;
        }
        if (player.equals(invitor)) {
            Account acct = new Account(player.getName());
            if (amount < 0) {
                if (acct.getHoldings().getBalance() < -amount) {
                    return false;
                }
            }
            if (amount > acct.getHoldings().getBalance()) {
                return false;
            }
            invitorMoney += amount;
            acct.getHoldings().add(-amount);
            return true;
        }
        else if (player.equals(invitee)) {
            Account acct = new Account(player.getName());
            if (amount < 0) {
                if (acct.getHoldings().getBalance() < -amount) {
                    return false;
                }
            }
            if (amount > acct.getHoldings().getBalance()) {
                return false;
            }
            inviteeMoney += amount;
            acct.getHoldings().add(-amount);
            return true;
        }
        throw new InvalidPlayerException();
    }
    
    
    public Player getInvitor() {
        return invitor;
    }
    
    
    public Player getInvitee() {
        return invitee;
    }
    
    
    public Stage getStage() {
        return currentStage;
    }
    
    
    public void setStage(Stage stage) {
        this.currentStage = stage;
    }


    public void delete() {
        trades.remove(invitor);
        trades.remove(invitee);
        invitorChest = null;
        inviteeChest = null;
        invitor = null;
        invitor = null;
        invitee = null;
        invitorMoney = 0;
        inviteeMoney = 0;
        currentStage = null;
    }
    
    
    public void addActionStatus(int stage) {
        actionStatus |= stage;
    }
    
    
    public int getActionStatus() {
        return actionStatus;
    }
    
    
    public boolean removeActionStatus(int stage) {
        if ((actionStatus & stage) == 0) {
            return false;
        }
        actionStatus &= ~stage;
        return true;
    }
    
    public double getMoney(Player player) {
        if (!FairTrade.useIConomy) {
            return 0;
        }
        if (player.equals(invitor)) {
            return invitorMoney;
        }
        else if (player.equals(invitee)) {
            return inviteeMoney;
        }
        try {
            throw new InvalidPlayerException("Player " + player.getName() + " was not in this trade!");
        } catch (InvalidPlayerException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    
    public boolean hasActionStatus(int status) {
        return (actionStatus & status) != 0;
    }


    public void confirm() {
        ItemStack[] invitorItems = invitorChest.getContents();
        CraftItemStack[] invitorCItems = new CraftItemStack[invitorItems.length];
        int l = invitorItems.length;
        for (int i = 0; i < l; i++) {
            invitorCItems[i] = new CraftItemStack(invitorItems[i]);
        }
        
        Map<Integer, org.bukkit.inventory.ItemStack> inviteeLeftovers = invitee.getInventory().addItem(invitorCItems);
        
        Collection<org.bukkit.inventory.ItemStack> inviteeLeftoverItems = inviteeLeftovers.values();
        
        if (inviteeLeftoverItems.size() > 0) {
            if (FairTrade.playerHasOverflowChest(invitee)) {
                Inventory inv = FairTrade.getPlayerOverflowChest(invitee);
                Map<Integer, org.bukkit.inventory.ItemStack> leftoverLeftovers = inv.addItem(inviteeLeftoverItems.toArray(new org.bukkit.inventory.ItemStack[0]));
                
                Collection<org.bukkit.inventory.ItemStack> leftoverLeftoversItems = leftoverLeftovers.values();
                if (leftoverLeftoversItems.size() > 0) {
                    invitee.sendMessage(new String[] {    "§aYou have too many items to fit in your inventory and overflow chest!",
                                                        "§aYour leftover items will be dropped! D:"
                    });
                    for (org.bukkit.inventory.ItemStack is : leftoverLeftoversItems) {
                        invitee.getWorld().dropItemNaturally(invitee.getLocation(), is);
                    }
                }
                
            }
            else {
                invitee.sendMessage(new String[] {    "§aYou have too many items to fit in your inventory!", 
                                                    "§aAlso, silly person, you have not set an overflow chest!",
                });
                for (org.bukkit.inventory.ItemStack is : inviteeLeftoverItems) {
                    invitee.getWorld().dropItemNaturally(invitee.getLocation(), is);
                }
            }
        }
        
        invitorItems = null;
        invitorCItems = null;
        inviteeLeftoverItems = null;
        inviteeLeftovers = null;
        
        ItemStack[] inviteeItems = inviteeChest.getContents();
        CraftItemStack[] inviteeCItems = new CraftItemStack[inviteeItems.length];
        l = inviteeItems.length;
        for (int i = 0; i < l; i++) {
            inviteeCItems[i] = new CraftItemStack(inviteeItems[i]);
        }
        
        Map<Integer, org.bukkit.inventory.ItemStack> invitorLeftovers = invitor.getInventory().addItem(inviteeCItems);
        
        Collection<org.bukkit.inventory.ItemStack> invitorLeftoverItems = invitorLeftovers.values();
        
        if (invitorLeftoverItems.size() > 0) {
            if (FairTrade.playerHasOverflowChest(invitor)) {
                Inventory inv = FairTrade.getPlayerOverflowChest(invitor);
                Map<Integer, org.bukkit.inventory.ItemStack> leftoverLeftovers = inv.addItem(invitorLeftoverItems.toArray(new org.bukkit.inventory.ItemStack[0]));
                
                Collection<org.bukkit.inventory.ItemStack> leftoverLeftoversItems = leftoverLeftovers.values();
                if (leftoverLeftoversItems.size() > 0) {
                    invitor.sendMessage(new String[] {    "§aYou have too many items to fit in your inventory and overflow chest!",
                                                        "§aYour leftover items will be dropped! D:"
                    });
                    for (org.bukkit.inventory.ItemStack is : leftoverLeftoversItems) {
                        invitor.getWorld().dropItemNaturally(invitor.getLocation(), is);
                    }
                }
                
            }
            else {
                invitor.sendMessage(new String[] {    "§aYou have too many items to fit in your inventory!", 
                                                    "§aAlso, silly person, you have not set an overflow chest!",
                });
                for (org.bukkit.inventory.ItemStack is : invitorLeftoverItems) {
                    invitor.getWorld().dropItemNaturally(invitor.getLocation(), is);
                }
            }
        }
        
        
        (new Account(invitor.getName())).getHoldings().add(inviteeMoney);
        (new Account(invitee.getName())).getHoldings().add(invitorMoney);
        if (hasActionStatus(ActionStatus.INVITOR_VIEWING_INVITEE | ActionStatus.INVITOR_VIEWING_OWN)) {
            invitor.closeInventory();
        }
        if (hasActionStatus(ActionStatus.INVITEE_VIEWING_INVITOR | ActionStatus.INVITEE_VIEWING_OWN)) {
            invitee.closeInventory();
        }
        delete();
    }


    public void cancel() {
        if (isCancelled) {
            return;
        }
        ItemStack[] invitorItems = invitorChest.getContents();
        for (ItemStack itemStack : invitorItems) {
            invitor.getInventory().addItem(new CraftItemStack(itemStack));
        }
        
        
        ItemStack[] inviteeItems = inviteeChest.getContents();
        for (ItemStack itemStack : inviteeItems) {
            invitee.getInventory().addItem(new CraftItemStack(itemStack));
        }
        if (FairTrade.useIConomy) {
            (new Account(invitor.getName())).getHoldings().add(invitorMoney);
            (new Account(invitee.getName())).getHoldings().add(inviteeMoney);
        }
        
        if (hasActionStatus(ActionStatus.INVITOR_VIEWING_INVITEE | ActionStatus.INVITOR_VIEWING_OWN)) {
            invitor.closeInventory();
        }
        if (hasActionStatus(ActionStatus.INVITEE_VIEWING_INVITOR | ActionStatus.INVITEE_VIEWING_OWN)) {
            invitee.closeInventory();
        }
        isCancelled = true;
    }


    public static void cancelAll() {
        Collection<Trade> allTrades = trades.values();
        for (Trade tr : allTrades) {
            if (tr == null) {
                continue;
            }
            tr.cancel();
            tr.delete();
        }
    }
}
