package com.gmail.wellington.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.gmail.wellington.plugin.ManeuverGearMain;

public class ManeuverGearListener implements Listener {
	ManeuverGearMain plugin;
	public ManeuverGearListener(ManeuverGearMain plugin) {
		this.plugin = plugin;
	}
	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if (e.getAction()==Action.PHYSICAL) return;
		if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction()==Action.RIGHT_CLICK_BLOCK) {
			plugin.RightClick(e.getPlayer().getUniqueId());
		}
	}
	@EventHandler
	public void onHit(ProjectileHitEvent e) {
		if (e.getHitBlock() != null) plugin.BlockHit(e.getEntity().getUniqueId(), e.getHitBlockFace());
		else if (e.getHitEntity() != null) plugin.EntityHit(e.getEntity().getUniqueId(), e.getHitEntity());
	}
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		plugin.DropkeyPressed(e);
	}
	@EventHandler
	public void onFallDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player)
			if (e.getCause().equals(DamageCause.FALL))
				plugin.FallDamage(e);
	}
	@EventHandler
	public void onToggleFlightMode(PlayerToggleFlightEvent e) {
		plugin.onToggleFlightMode(e);
	}
	@EventHandler
	public void onLogout(PlayerQuitEvent e) {
		plugin.Logout(e.getPlayer().getUniqueId());
	}
	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent e) {
		plugin.Logout(e.getPlayer().getUniqueId());
	}
	@EventHandler
	public void onItemHeld(PlayerItemHeldEvent e) {
		plugin.ItemHeld(e.getPlayer().getUniqueId());
	}
}
