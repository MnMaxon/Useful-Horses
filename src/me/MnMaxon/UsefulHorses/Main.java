package me.MnMaxon.UsefulHorses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
	public static String dataFolder;
	public static Main plugin;
	public static boolean running = false;
	static Map<Horse, Integer> horses = new HashMap<Horse, Integer>();

	@Override
	public void onEnable() {
		plugin = this;
		dataFolder = this.getDataFolder().getAbsolutePath();
		setupConfig();
		Config.Load(dataFolder + "/Data");
		getServer().getPluginManager().registerEvents(new MainListener(), this);
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getInventory().contains(Material.SADDLE))
				for (ItemStack is : p.getInventory().getContents())
					if (is != null)
						is.setItemMeta(refresh(is));

	}

	@Override
	public void onDisable() {
		Map<Horse, Integer> remover = horses;
		for (Entry<Horse, Integer> entry : remover.entrySet()) {
			saveHorse(entry.getKey(), entry.getValue());
			YamlConfiguration data = Config.Load(dataFolder + "/Data");
			data.set(entry.getValue() + ".Refresh", "FULL");
			Config.Save(data, dataFolder + "/Data");
		}
		horses = new HashMap<Horse, Integer>();
	}

	public YamlConfiguration setupConfig() {
		cfgSetter("Awesomeness", true);
		return Config.Load(dataFolder + "/Config.yml");
	}

	public void cfgSetter(String path, Object value) {
		YamlConfiguration cfg = Config.Load(dataFolder + "/Config.yml");
		if (cfg.get(path) == null) {
			cfg.set(path, value);
			Config.Save(cfg, dataFolder + "/Config.yml");
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player p = (Player) sender;
		if (args.length == 1 && args[0].equalsIgnoreCase("get")) {
			ItemStack is = new ItemStack(Material.SADDLE);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(ChatColor.RED + "[EMPTY] CATCHER");
			ArrayList<String> lore = new ArrayList<String>();
			lore.add("ID: " + generateID());
			im.setLore(lore);
			is.setItemMeta(im);
			p.getInventory().addItem(is);
		} else
			displayHelp(p);
		return false;
	}

	private void displayHelp(Player p) {
		p.sendMessage(ChatColor.GOLD + "===== " + ChatColor.DARK_PURPLE + "Useful Horses Help" + ChatColor.GOLD
				+ " =====");
		p.sendMessage(ChatColor.DARK_AQUA + "/UH" + ChatColor.DARK_PURPLE + " - Displays Help");
		p.sendMessage(ChatColor.DARK_AQUA + "/UH Get" + ChatColor.DARK_PURPLE + " - Gives a capture device");
	}

	public static boolean checkOpen(Location loc) {
		if (checkHelper(loc, 0, 1, 0) && checkHelper(loc, 1, 1, 0) && checkHelper(loc, 1, 1, 1)
				&& checkHelper(loc, 1, 1, -1) && checkHelper(loc, -1, 1, 0) && checkHelper(loc, -1, 1, -1)
				&& checkHelper(loc, -1, 1, 1) && checkHelper(loc, 0, 1, 1) && checkHelper(loc, 0, 1, -1)
				&& checkHelper(loc, 0, 2, 0) && checkHelper(loc, 1, 2, 0) && checkHelper(loc, 1, 2, 1)
				&& checkHelper(loc, 1, 2, -1) && checkHelper(loc, -1, 2, 0) && checkHelper(loc, -1, 2, -1)
				&& checkHelper(loc, -1, 2, 1) && checkHelper(loc, 0, 2, 1) && checkHelper(loc, 0, 2, -1))
			return true;
		return false;
	}

	private static boolean checkHelper(Location loc, int x, int y, int z) {
		if (loc.getBlock().getRelative(x, y, z).getType().equals(Material.AIR))
			return true;
		return false;
	}

	public static void saveHorse(Horse horse, int ID) {
		YamlConfiguration data = Config.Load(dataFolder + "/Data");
		data.set(ID + ".Catcher", true);
		data.set(ID + ".Breedable", horse.canBreed());
		data.set(ID + ".Age", horse.getAge());
		data.set(ID + ".AgeLock", horse.getAgeLock());
		data.set(ID + ".Color", horse.getColor().name());
		data.set(ID + ".Name", horse.getCustomName());
		data.set(ID + ".Domestication", horse.getDomestication());
		data.set(ID + ".Style", horse.getStyle().name());
		data.set(ID + ".Adult", horse.isAdult());
		data.set(ID + ".CarryingChest", horse.isCarryingChest());
		data.set(ID + ".Tame", horse.isTamed());
		data.set(ID + ".VisibleName", horse.isCustomNameVisible());
		if (horse.getInventory() != null) {
			int i = 0;
			for (ItemStack is : horse.getInventory().getContents()) {
				if (horse.getInventory().getItem(i) != null
						&& !horse.getInventory().getItem(i).getType().equals(Material.AIR)
						&& horse.getInventory().getItem(i) != null) {
					data.set(ID + ".Inventory." + i + ".Material", is.getType().name());
					data.set(ID + ".Inventory." + i + ".Amount", is.getAmount());
					data.set(ID + ".Inventory." + i + ".Durability", (int) is.getDurability());
					if (is.hasItemMeta()) {
						if (is.getItemMeta().hasDisplayName())
							data.set(ID + ".Inventory." + i + ".DisplayName", is.getItemMeta().getDisplayName());
						if (is.getItemMeta().hasLore())
							for (int x = 0; x < 10; x++)
								if (is.getItemMeta().getLore().get(x) != null)
									data.set(ID + ".Inventory." + i + ".Lore." + x, is.getItemMeta().getLore().get(x));
						int counter = 1;
						if (is.getItemMeta().hasEnchants())
							for (Entry<Enchantment, Integer> entry : is.getItemMeta().getEnchants().entrySet()) {
								data.set(ID + ".Inventory." + i + ".Enchantments." + counter + ".Enchantment", entry
										.getKey().getName());
								data.set(ID + ".Inventory." + i + ".Enchantments." + counter + ".Level",
										entry.getValue());
								counter++;
							}
					}
				}
				i++;
			}
		}
		Config.Save(data, dataFolder + "/Data");
		horse.remove();
	}

	public static Horse loadHorse(Location loc, int ID) {
		YamlConfiguration data = Config.Load(dataFolder + "/Data");
		if (data.get(ID + "") == null)
			return null;
		Horse horse = (Horse) loc.getWorld().spawnEntity(loc, EntityType.HORSE);
		horse.setBreed(data.getBoolean(ID + ".Breedable", horse.canBreed()));
		horse.setAge(data.getInt(ID + ".Age"));
		horse.setAgeLock(data.getBoolean(ID + ".AgeLock"));
		horse.setColor(Horse.Color.valueOf(data.getString(ID + ".Color")));
		horse.setCustomName(data.getString(ID + ".Name"));
		horse.setDomestication(data.getInt(ID + ".Domestication"));
		horse.setStyle(Horse.Style.valueOf(data.getString(ID + ".Style")));
		horse.setCustomNameVisible(data.getBoolean(ID + ".VisibleName"));
		if (data.getBoolean(ID + ".Adult"))
			horse.setAdult();
		else
			horse.setBaby();
		horse.setCarryingChest(data.getBoolean(ID + ".CarryingChest"));
		horse.setTamed(data.getBoolean(ID + ".Tame"));
		for (int i = 0; i < 200; i++) {
			if (data.get(ID + ".Inventory." + i) != null) {
				ItemStack is = new ItemStack(Material.matchMaterial(data
						.getString(ID + ".Inventory." + i + ".Material")));
				is.setAmount(data.getInt(ID + ".Inventory." + i + ".Amount"));
				is.setDurability((short) data.getInt(ID + ".Inventory." + i + ".Durability"));
				ItemMeta im = is.getItemMeta();
				if (data.get(ID + ".Inventory." + i + ".DisplayName") != null)
					im.setDisplayName(data.getString(ID + ".Inventory." + i + ".DisplayName"));
				if (data.getString(ID + ".Inventory." + i + ".Lore") != null) {
					ArrayList<String> lore = new ArrayList<String>();
					for (int x = 0; x < 10; x++)
						if (data.get(ID + ".Inventory." + i + ".Lore." + x) != null)
							lore.set(x, data.getString(ID + ".Inventory." + i + ".Lore." + x));
					im.setLore(lore);
				}
				for (int x = 0; x < 20; x++)
					if (data.get(ID + ".Inventory." + i + ".Enchantments." + x) != null) {
						im.addEnchant(
								Enchantment.getByName(data.getString(ID + ".Inventory." + i + ".Enchantments." + x
										+ ".Enchantment")),
								data.getInt(ID + ".Inventory." + i + ".Enchantments." + x + ".Level"), false);
					}
				is.setItemMeta(im);
				horse.getInventory().setItem(i, is);
			}
		}
		return horse;
	}

	public static int generateID() {
		YamlConfiguration data = Config.Load(dataFolder + "/Data");
		int i = 1;
		while (data.get(i + "") != null)
			i++;
		return i;
	}

	public static int getID(ItemStack is) {
		if (is != null && is.getType().equals(Material.SADDLE) && is.hasItemMeta() && is.getItemMeta().hasDisplayName()
				&& is.getItemMeta().getDisplayName().contains("CATCHER") && is.getItemMeta().hasLore()) {
			if (is.getItemMeta().getLore().get(0) != null && !is.getItemMeta().getLore().get(0).equals("")
					&& is.getItemMeta().getLore().get(0).contains("ID: "))
				return Integer.parseInt(ChatColor.stripColor(is.getItemMeta().getLore().get(0).replace("ID: ", "")));
		}
		return 0;
	}

	public static Horse getHorse(int ID) {
		for (Entry<Horse, Integer> entry : horses.entrySet())
			if (entry.getValue() == ID)
				return entry.getKey();
		return null;
	}

	public static ItemMeta refresh(ItemStack is) {
		ItemMeta im = null;
		if (is != null && is.hasItemMeta()) {
			im = is.getItemMeta();
		} else {
			return im;
		}
		int ID = getID(is);
		if (ID == 0)
			return im;
		YamlConfiguration data = Config.Load(dataFolder + "/Data");
		if (data.get(ID + ".Refresh") != null) {
			im.setDisplayName(ChatColor.RED + "[" + data.getString(ID + ".Refresh").toUpperCase() + "] CATCHER");
			data.set(ID + ".Refresh", null);
			Config.Save(data, dataFolder + "/Data");
		}
		return im;
	}
}