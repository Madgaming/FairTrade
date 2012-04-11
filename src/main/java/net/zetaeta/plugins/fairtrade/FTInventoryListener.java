package net.zetaeta.plugins.fairtrade;

import static net.zetaeta.plugins.fairtrade.Trade.ActionStatus.INVITEE_VIEWING_INVITOR;
import static net.zetaeta.plugins.fairtrade.Trade.ActionStatus.INVITEE_VIEWING_OWN;
import static net.zetaeta.plugins.fairtrade.Trade.ActionStatus.INVITOR_VIEWING_INVITEE;
import static net.zetaeta.plugins.fairtrade.Trade.ActionStatus.INVITOR_VIEWING_OWN;
import net.zetaeta.plugins.fairtrade.Trade.Stage;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FTInventoryListener implements Listener {
    
/*    @SuppressWarnings("static-method")
    @EventHandler
    public void inventoryOpen(InventoryOpenEvent event) {

        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        if (Trade.playersInTrades.contains(player)) {
            Trade trade = Trade.getTrade(player);
            EntityPlayer ePlayer = ((CraftPlayer) player).getHandle();
            ePlayer.openContainer(trade.getChest(player));
        }
    }*/
    
    @SuppressWarnings("static-method")
    @EventHandler
    public void inventoryChanged(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof CraftPlayer)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Trade trade = Trade.getTrade(player);
        if (trade == null) {
            return;
        }
        
        if (trade.getInvitor().equals(player)) {
            if (trade.hasActionStatus(INVITOR_VIEWING_INVITEE)) {
                event.setCancelled(true);
            }
        }
        else if (trade.getInvitee().equals(player)) {
            if (trade.hasActionStatus(INVITEE_VIEWING_INVITOR)) {
                event.setCancelled(true);
            }
        }
        if (trade.getStage() == Stage.COMPLETE) {
            if (trade.getInvitor().equals(player)) {
                if (trade.hasActionStatus(INVITOR_VIEWING_INVITEE | INVITOR_VIEWING_OWN)) {
                    event.setCancelled(true);
                }
            }
            if (trade.getInvitee().equals(player)) {
                if (trade.hasActionStatus(INVITEE_VIEWING_INVITOR | INVITEE_VIEWING_OWN)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    
    @SuppressWarnings("static-method")
    @EventHandler
    public void inventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof CraftPlayer)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        Trade trade;
        if ((trade = Trade.getTrade(player)) == null) {
            return;
        }
        if (trade.getInvitor().equals(player)) {                        // if player is invitor
            if (trade.hasActionStatus(INVITOR_VIEWING_INVITEE)) {
                trade.removeActionStatus(INVITOR_VIEWING_INVITEE);
            }
            if (trade.hasActionStatus(INVITOR_VIEWING_OWN)) {
                trade.removeActionStatus(INVITOR_VIEWING_OWN);
            }
        }
        else if (trade.getInvitee().equals(player)) {
            if (trade.hasActionStatus(INVITEE_VIEWING_INVITOR)) {
                trade.removeActionStatus(INVITEE_VIEWING_INVITOR);
            }
            if (trade.hasActionStatus(INVITEE_VIEWING_OWN)) {
                trade.removeActionStatus(INVITEE_VIEWING_OWN);
            }
        }
    }
    
    @SuppressWarnings("static-method")
    @EventHandler
    public void playerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Trade trade;
        if ((trade = Trade.getTrade(player)) == null) {
            return;
        }
        trade.cancel();
        trade.delete();
    }
}
