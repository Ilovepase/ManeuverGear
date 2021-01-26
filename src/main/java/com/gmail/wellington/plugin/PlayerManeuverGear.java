package com.gmail.wellington.plugin;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.gmail.wellington.util.AnchorModule;
import com.gmail.wellington.util.ManeuverModule;

public class PlayerManeuverGear {
	int click_count = 0;
	int sting = 0;
	int effect_type = 0;
	double speed = 5;
	double range = 30;
	double falldamage = 5; 
	boolean isLaunch = false;
	boolean isJet = false;
	Player player;
	ManeuverGearMain plugin;
	AnchorModule rightAnchor;
	AnchorModule leftAnchor;
	ManeuverModule maneuverModule;
	public PlayerManeuverGear(ManeuverGearMain plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
		maneuverModule = new ManeuverModule(plugin, this, player);
	}
	public void ConfigReload() {
		plugin.reloadConfig();
		FileConfiguration config = this.plugin.getConfig();
		this.speed = config.getDouble("arrow_speed",5);
		this.range = config.getDouble("arrow_range",30);
		this.falldamage = config.getDouble("fall_damage",5);
		this.speed = Math.min(speed, 10);
		this.speed = Math.max(speed, 0.1);
		this.range = Math.min(range, 255);
		this.range = Math.max(range, 1);
		this.falldamage = Math.max(falldamage, 0);
		this.effect_type = config.getInt("effect_type",0);
		maneuverModule.reloadConfig();
	}
	public boolean getSting() {
		if (leftAnchor!=null && rightAnchor!=null) {
			if (rightAnchor.isSting && leftAnchor.isSting) return true;
			else return false;
		}
		else if (leftAnchor!=null) {
			if (leftAnchor.isSting) return true;
			else return false;
		}
		else if (rightAnchor!=null) {
			if (rightAnchor.isSting) return true;
			else return false;
		}
		else {
			return false;
		}
	}
	public void FallDamage () {
		player.damage(Math.abs(maneuverModule.getSpeedY()*maneuverModule.getSpeedY()*falldamage));
	}
	public void Reel () {
		if (click_count<3) return;
		if (rightAnchor == null && leftAnchor == null) return;
		if (getSting()) {
			if (player.isSneaking())
				maneuverModule.set(rightAnchor, leftAnchor, -1);
			else
				maneuverModule.set(rightAnchor, leftAnchor, 1);
		}
	}
	public void Click(int num) {
		if (!isLaunch)
			if (num==1)
				Launch();
			else if (num==2)
				SearchLaunch();
		click_count ++;
	}
	public void doubleJump() {
		if (getSting())
			maneuverModule.doubleJump();
	}
	public void Partwith() {
		if (rightAnchor != null)
			rightAnchor.Delete();
		if (leftAnchor != null)
			leftAnchor.Delete();
		if (player.getGameMode()==GameMode.CREATIVE) player.setAllowFlight(true);
		else player.setAllowFlight(false);
		isLaunch = false;
		sting = 0;
		click_count = 0;
		maneuverModule.stop();
	}
	private boolean Launch() {
		Location loc = player.getEyeLocation();
		Vector front = player.getLocation().getDirection().normalize();
		isLaunch = true;
		rightAnchor = new AnchorModule(plugin, this, player, loc, front, speed, effect_type);
		maneuverModule.set(rightAnchor, leftAnchor, 0);
		return true;
	}
	private boolean SearchLaunch() {
		Location loc = player.getEyeLocation();
		Vector front = player.getLocation().getDirection().normalize();
		Vector right = new Vector(-front.getZ(),0,front.getX()).normalize();
		Vector up = front.getCrossProduct(right).multiply(-1.0d).normalize();
		for (int i = 1; i < 45; i++) {
			Vector r = front.clone().rotateAroundAxis(up, Math.toRadians(i));
			Vector l = front.clone().rotateAroundAxis(up, Math.toRadians(-i));
			boolean b_r = isRayHit(loc,r);
			boolean b_l = isRayHit(loc,l);
			if (b_r&&b_l) {
				isLaunch = true;
				rightAnchor = new AnchorModule(plugin, this, player, loc, r, speed, effect_type);
				leftAnchor = new AnchorModule(plugin, this, player, loc, l, speed, effect_type);
				maneuverModule.set(rightAnchor, leftAnchor, 0);
				return true;
			}
		}
		return false;
	}
	private boolean isRayHit(Location loc, Vector v) {
		RayTraceResult hit = player.getWorld().rayTraceBlocks(loc, v, 30);
		if (hit != null) return true;
		else return false;
	}
}
