package net.zetaeta.plugins.fairtrade;

import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

public class FTInventoryListener implements Listener {
	
	@SuppressWarnings("static-method")
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
	}
	
	public void inventoryChanged(InventoryClickEvent event) {
		if (!Trade.playersInTrades.contains((Player) event.getWhoClicked())) {
			return;
		}
		
		CraftInventory cInv = (CraftInventory) event.getInventory();
		if (!(cInv instanceof CraftInventoryDoubleChest)) {
			return;
		}
		CraftInventoryDoubleChest cdInv = (CraftInventoryDoubleChest) cInv;
		
		if (event.)
	}
	
}
