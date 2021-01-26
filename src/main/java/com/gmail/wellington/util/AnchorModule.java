package com.gmail.wellington.util;

import java.util.Set;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.util.Vector;

import com.gmail.wellington.plugin.ManeuverGearMain;
import com.gmail.wellington.plugin.PlayerManeuverGear;

public class AnchorModule extends BukkitRunnable {
	public boolean isSting = false;
	public boolean isPullable = false;
	public int effect_type = 0;
	Arrow arrow;
	Player player;
	Entity entity;
	Vector offset = new Vector(0,1,0);
	BlockFace blockFace;
	ManeuverGearMain plugin;
	PlayerManeuverGear pmg;
	Set<Entity> impullableEntity = new HashSet<>();
	public AnchorModule(ManeuverGearMain plugin, PlayerManeuverGear pmg, Player player, Location loc, Vector vec, double speed, int effect_type) {
		this.plugin = plugin;
		this.player = player;
		this.pmg = pmg;
		this.effect_type = effect_type;
		Launch(player, loc, vec, speed);
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Location getLoc() {
		if (!arrow.isDead())
			return arrow.getLocation();
		else if (entity != null && !entity.isDead()) {
			if (isPullable)	return entity.getLocation();
			else return null;
		}
		else
			return null;
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
		arrow.setVelocity(new Vector(0,0,0));
		if (entity instanceof IronGolem || entity instanceof Boss) {
			isPullable = false;//toriaezu
		}
		else {
			isPullable = true;
		}
		isSting = true;
	}
	public void setBlockFace(BlockFace blockFace) {
		this.blockFace = blockFace;
		isSting = true;
	}
	
	public void Delete() {
		arrow.remove();
		this.cancel();
	}
	
	private void Launch(Player player, Location loc, Vector dir, double speed) {
		arrow = player.getWorld().spawn(loc, Arrow.class);
		arrow.teleport(loc);
		arrow.setVelocity(dir.multiply(speed));
		arrow.setShooter(player);
		arrow.setPickupStatus(PickupStatus.DISALLOWED);
		arrow.setDamage(1d);
		plugin.RegisterArrow(arrow.getUniqueId(), this);
		this.runTaskTimer(plugin, 0, 1);
		player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 3, 1);
	}

	public void run() {
		if (arrow.getLocation().distance(player.getLocation())>40) {
			arrow.remove();
			isSting = true;
		}
		if (arrow.isDead()) {
			if (entity == null) pmg.Partwith();
			else if (entity.isDead()) pmg.Partwith();
		}
		if (!arrow.isDead())
			DrawLine(player.getLocation().add(offset), arrow.getLocation());
		else if (entity != null)
			DrawLine(player.getLocation().add(offset), entity.getLocation().add(offset));
	}
	
	private boolean DrawLine(Location origin, Location dest) {
        Vector dir = dest.toVector().subtract(origin.toVector());
        Vector dir30 = dir.multiply(1d/30d);
        Vector vector = origin.toVector();
        for (int i = 0; i < 30; i ++) {
        	vector.add(dir30);
        	switch (effect_type) {
        		case 0: origin.getWorld().spawnParticle(Particle.CRIT, vector.toLocation(origin.getWorld()), 0, 0.549019608F, 0.549019608F, 0.549019608F, 0F);break;
        		case 1: origin.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, vector.toLocation(origin.getWorld()), 0, 0.549019608F, 0.549019608F, 0.549019608F, 0F);break;
        		case 2: origin.getWorld().spawnParticle(Particle.END_ROD, vector.toLocation(origin.getWorld()), 0, 0.549019608F, 0.549019608F, 0.549019608F, 0F);break;
        		case 3: origin.getWorld().spawnParticle(Particle.END_ROD, vector.toLocation(origin.getWorld()), 0, 0.549019608F, 0.549019608F, 0.549019608F, 0F);break;
        		case 4: origin.getWorld().spawnParticle(Particle.SPELL_INSTANT, vector.toLocation(origin.getWorld()), 0, 0.549019608F, 0.549019608F, 0.549019608F, 0F);break;
        	}
        }
        return true;
    }
}
