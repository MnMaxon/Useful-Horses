package me.MnMaxon.UsefulHorses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainListener implements Listener {
	@EventHandler
	public void clickBlock(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && Main.getID(e.getItem()) != 0) {
			if (e.getItem().getItemMeta().getDisplayName().contains("[FULL]")) {
				e.getItem().setItemMeta(Main.refresh(e.getItem()));
				if (Main.checkOpen(e.getClickedBlock().getLocation())) {
					int ID = Main.getID(e.getItem());
					Main.horses.put(Main.loadHorse(e.getClickedBlock().getLocation().add(0, 1, 0), ID), ID);
					ItemMeta im = e.getPlayer().getItemInHand().getItemMeta();
					im.setDisplayName(im.getDisplayName().replace("[FULL]", "[AWAY]"));
					e.getPlayer().getItemInHand().setItemMeta(im);
				} else
					e.getPlayer().sendMessage(
							ChatColor.DARK_PURPLE + "[Useful Horses]" + ChatColor.DARK_RED
									+ " This area is not open enough to do this!");
			} else if (e.getItem().getItemMeta().getDisplayName().contains("[AWAY]")) {
				int ID = Main.getID(e.getItem());
				ItemMeta im = e.getPlayer().getItemInHand().getItemMeta();
				im.setDisplayName(im.getDisplayName().replace("[AWAY]", "[FULL]"));
				Main.saveHorse(Main.getHorse(ID), ID);
				e.getPlayer().getItemInHand().setItemMeta(im);
				Main.getHorse(ID).remove();
				Main.horses.remove(Main.getHorse(ID));
			} else if (e.getItem().getItemMeta().getDisplayName().contains("[EMPTY]"))
				e.getPlayer()
						.sendMessage(
								ChatColor.DARK_PURPLE + "[Useful Horses]" + ChatColor.DARK_RED
										+ " You have not caught a horse");
		}
	}

	@EventHandler
	public void clickAir(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_AIR) && Main.getID(e.getItem()) != 0) {
			e.getItem().setItemMeta(Main.refresh(e.getItem()));
			if (e.getItem().getItemMeta().getDisplayName().contains("[FULL]")) {
				e.getPlayer().sendMessage(
						ChatColor.DARK_PURPLE + "[Useful Horses]" + ChatColor.DARK_RED
								+ " Look at the ground to do this");
			} else if (e.getItem().getItemMeta().getDisplayName().contains("[AWAY]")) {
				int ID = Main.getID(e.getItem());
				ItemMeta im = e.getPlayer().getItemInHand().getItemMeta();
				im.setDisplayName(im.getDisplayName().replace("[AWAY]", "[FULL]"));
				Main.saveHorse(Main.getHorse(ID), ID);
				e.getPlayer().getItemInHand().setItemMeta(im);
				Main.getHorse(ID).remove();
				Main.horses.remove(Main.getHorse(ID));
			} else if (e.getItem().getItemMeta().getDisplayName().contains("[EMPTY]"))
				e.getPlayer()
						.sendMessage(
								ChatColor.DARK_PURPLE + "[Useful Horses]" + ChatColor.DARK_RED
										+ " You have not caught a horse");
		}
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		if (Main.horses.containsKey(e.getEntity())) {
			int num = Main.horses.get(e.getEntity());
			YamlConfiguration data = Config.Load(Main.dataFolder + "/Data");
			data.set(num + "", null);
			data.set(num + ".Catcher", true);
			data.set(num + ".Refresh", "EMPTY");
			Config.Save(data, Main.dataFolder + "/Data");
			Main.horses.remove(e.getEntity());
		}
	}

	@EventHandler
	public void playerRenameItem(InventoryClickEvent e) {
		if (e.getView().getType() == InventoryType.ANVIL)
			if (e.getRawSlot() == 2)
				if (e.getView().getItem(0).getType() != Material.AIR
						&& e.getView().getItem(2).getType() != Material.AIR)
					if (e.getView().getItem(0).getItemMeta().getDisplayName() != e.getView().getItem(2).getItemMeta()
							.getDisplayName()
							&& e.getView().getItem(2).getItemMeta().getDisplayName().contains("CATCHER")) {
						e.setCancelled(true);
					}
	}

	@EventHandler
	public void clickHorse(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() != null && e.getRightClicked().getType().equals(EntityType.HORSE)
				&& !Main.horses.containsKey(e.getRightClicked())) {
			Player p = e.getPlayer();
			if (!p.getItemInHand().getType().equals(Material.AIR) && p.getItemInHand().hasItemMeta()
					&& p.getItemInHand().getItemMeta().hasDisplayName()
					&& p.getItemInHand().getItemMeta().getDisplayName().contains("CATCHER")) {
				e.getPlayer().getItemInHand().setItemMeta(Main.refresh(e.getPlayer().getItemInHand()));
				if (p.getItemInHand().getItemMeta().getDisplayName().contains("[EMPTY]")) {
					e.setCancelled(true);
					Main.saveHorse((Horse) e.getRightClicked(), Main.getID(p.getItemInHand()));
					ItemMeta im = e.getPlayer().getItemInHand().getItemMeta();
					im.setDisplayName(im.getDisplayName().replace("[EMPTY]", "[FULL]"));
					e.getPlayer().getItemInHand().setItemMeta(im);
				}
			}
		}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		ItemStack is = e.getCurrentItem();
		if (is != null) {
			is.setItemMeta(Main.refresh(is));
			if (Main.getID(is) != 0 && Main.horses.containsKey(Main.getHorse(Main.getID(is)))) {
				Main.getHorse(Main.getID(is)).remove();
				Main.horses.remove(Main.getHorse(Main.getID(is)));
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(im.getDisplayName().replace("[AWAY]", "[FULL]"));
				is.setItemMeta(im);
				e.setCurrentItem(is);
				YamlConfiguration data = Config.Load(Main.dataFolder + "/Data");
				data.set(Main.getID(is) + ".Refresh", "FULL");
				Config.Save(data, Main.dataFolder + "/Data");
			}
		}
	}

	@EventHandler
	public void hotbarEvent(PlayerItemHeldEvent e) {
		ItemStack is = e.getPlayer().getInventory().getItem(e.getNewSlot());
		if (is != null)
			is.setItemMeta(Main.refresh(is));
	}
}
