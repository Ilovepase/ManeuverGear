package com.gmail.wellington.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.wellington.command.CommandManager;
import com.gmail.wellington.listener.ManeuverGearListener;
import com.gmail.wellington.util.AnchorModule;

public class ManeuverGearMain extends JavaPlugin {
	
	CommandManager commandManager;
	ManeuverGearListener listener;
	NamespacedKey nameKey = new NamespacedKey(this, "maneuver-gear-plugin");
	HashMap<UUID, PlayerManeuverGear> players = new HashMap<>();
	HashMap<UUID, Long> players_click = new HashMap<>();
	HashMap<UUID, AnchorModule> arrows = new HashMap<>();
	List<UUID> players_click_list = new ArrayList<>();
	@Override
	public void onEnable() {
		getLogger().info("ManeuverGearPlugin is active");
		PluginManager pm = getServer().getPluginManager();
		listener = new ManeuverGearListener(this);
		getCommand("mgear").setExecutor(new CommandManager(this));
		pm.registerEvents(listener, this);
		new BukkitRunnable() {
			@Override
			public void run() {
				for (UUID uuid : players_click.keySet()) {
					if (hasGear(uuid)<=0) {
						if (players.containsKey(uuid))
							players.get(uuid).Partwith();
					}
					if (System.currentTimeMillis()-players_click.get(uuid)<205) {
						if (!players_click_list.contains(uuid))
							players.get(uuid).Reel();
					}
				}
			}
		}.runTaskTimer(this, 1L, 1L);
	}
	public void RegisterArrow(UUID uuid, AnchorModule anchor) {
		arrows.put(uuid, anchor);
	}
	public void FallDamage(EntityDamageEvent e) {
		UUID uuid = e.getEntity().getUniqueId();
		if (hasGear(uuid) > 0) {
			if (!players.containsKey(uuid)) {
				players.put(uuid, new PlayerManeuverGear(this, Bukkit.getPlayer(uuid)));
			}
			players.get(uuid).FallDamage();
			e.setCancelled(true);
		}
	}
	public void Logout(UUID uuid) {
		if (players.containsKey(uuid)) {
			players.get(uuid).Partwith();
			players.remove(uuid);
		}
	}
	public void onToggleFlightMode(PlayerToggleFlightEvent e) {
		UUID uuid = e.getPlayer().getUniqueId();
		if (hasGear(uuid) > 0) {
			if (!players.containsKey(uuid)) {
				players.put(uuid, new PlayerManeuverGear(this, Bukkit.getPlayer(uuid)));
			}
			if (players.get(uuid).getSting()) {
				players.get(uuid).doubleJump();
				if (Bukkit.getPlayer(uuid).getGameMode()!=GameMode.CREATIVE)
					e.setCancelled(true);
			}
		}
	}
	public void RightClick(UUID uuid) {
		if (hasGear(uuid) > 0) {
			if (!players.containsKey(uuid)) {
				players.put(uuid, new PlayerManeuverGear(this, Bukkit.getPlayer(uuid)));
			}
			if (!players_click.containsKey(uuid)) {
				//new click
				players_click_list.add(uuid);
			}
			else {
				//old click
				players_click_list.remove(uuid);
			}
			if (hasGear(uuid)==1)
				players.get(uuid).Click(1);
			else if (hasGear(uuid)==2)
				players.get(uuid).Click(2);
			players_click.put(uuid, System.currentTimeMillis());
		}
	}
	public void LeftClick(UUID uuid) {
		if (hasGear(uuid)>0) {
			
		}
	}
	public void ItemHeld() {
		
	}
	public void DropkeyPressed(PlayerDropItemEvent e) {
		UUID uuid = e.getPlayer().getUniqueId();
		if (hasGear(uuid)>=0) {
			if (!players.containsKey(uuid)) {
				players.put(uuid, new PlayerManeuverGear(this, Bukkit.getPlayer(uuid)));
			}
			players.get(uuid).Partwith();
			e.setCancelled(true);
		}
	}
	public void EntityHit(UUID uuid, Entity hitEntity) {
		if (arrows.containsKey(uuid)) {
			arrows.get(uuid).setEntity(hitEntity);
		}
	}
	public void BlockHit(UUID uuid, BlockFace blockFace) {
		if (arrows.containsKey(uuid)) {
			arrows.get(uuid).setBlockFace(blockFace);
		}
	}
	public void ReloadConfig() {
		for (UUID uuid : players.keySet()) {
			players.get(uuid).ConfigReload();
		}
	}
	public void GiveGear(UUID uuid) {
		ItemStack item = new ItemStack(Material.CHAINMAIL_LEGGINGS,1);
		ItemStack gripStackL = new ItemStack(Material.IRON_SWORD,1);
		ItemStack gripStackR = new ItemStack(Material.IRON_SWORD,1);
		ItemMeta meta = item.getItemMeta();
		ItemMeta metaL = gripStackL.getItemMeta();
		ItemMeta metaR = gripStackR.getItemMeta();
		meta.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, "ManeuverGear");
		metaR.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, "Grip");
		metaL.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, "Grip");
		item.setItemMeta(meta);
		gripStackL.setItemMeta(metaL);
		gripStackR.setItemMeta(metaR);
		Bukkit.getPlayer(uuid).getInventory().addItem(item);
		Bukkit.getPlayer(uuid).getInventory().addItem(gripStackL);
		Bukkit.getPlayer(uuid).getInventory().addItem(gripStackR);
	}
	public int hasGear(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player == null) {
			Logout(uuid);
			return -1;
		}
		ItemStack leggings = player.getInventory().getLeggings();
		ItemStack mainHand = player.getInventory().getItemInMainHand();
		ItemStack offHand = player.getInventory().getItemInOffHand();
		if (!getPersistentData(leggings).equals("ManeuverGear")) return -1;
		else if (getPersistentData(mainHand).equals("Grip") && getPersistentData(offHand).equals("Grip")) return 2;
		else if (getPersistentData(mainHand).equals("Grip") || getPersistentData(offHand).equals("Grip")) return 1;
		else return 0;
	}
	private String getPersistentData(ItemStack item) {
		if (item != null) {
			if (item.getType() != Material.AIR) {
				PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
				if (data.has(nameKey, PersistentDataType.STRING)) {
					return data.get(nameKey, PersistentDataType.STRING);
				}
			}
		}
		return "";
	}
	
}
