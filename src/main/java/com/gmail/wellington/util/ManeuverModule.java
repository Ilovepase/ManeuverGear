package com.gmail.wellington.util;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.gmail.wellington.plugin.ManeuverGearMain;
import com.gmail.wellington.plugin.PlayerManeuverGear;

public class ManeuverModule extends BukkitRunnable {
	boolean jumped = false;
	boolean isStop = false;
	boolean autoPartwith = true;
	int reel = 0;
	double vertical_speed = 0d;
	double speed = 0d;
	double maxspeed = 0d;
	double rDis;
	double lDis;
	Player player;
	Vector velocity;
	Vector rVector;
	Vector lVector;
	AnchorModule rightAnchor;
	AnchorModule leftAnchor;
	ManeuverGearMain plugin;
	PlayerManeuverGear pmg;
	public ManeuverModule(ManeuverGearMain plugin, PlayerManeuverGear pmg, Player player) {
		this.plugin = plugin;
		this.player = player;
		velocity = new Vector(0,0,0);
		this.pmg = pmg;
		reloadConfig();
		this.runTaskTimer(plugin, 1, 1);
	}
	public void set(AnchorModule rightAnchor, AnchorModule leftAnchor, int reel) {
		this.reel = reel;
		velocity = player.getVelocity();
		this.rightAnchor = rightAnchor;
		this.leftAnchor = leftAnchor;
		isStop = false;
	}
	public void reloadConfig () {
		plugin.reloadConfig();
		FileConfiguration config = this.plugin.getConfig();
		maxspeed = config.getDouble("player_maxspeed",1);
	}
	public void stop() {
		isStop = true;
	}
	public void doubleJump() {
		if (!isStop && pmg.getSting())
			jumped = true;
	}
	public double getSpeedY() {
		return vertical_speed;
	}
	private Vector Calc( Vector axis, Vector force) {
		Vector a = axis.clone();
		Vector f = force.clone();
		Vector b = a.getCrossProduct(f);
		Vector c = a.getCrossProduct(b);
		c.normalize().multiply(c.dot(f)/c.length());
		if (c.length() > 100d) {
			pmg.Partwith();
			return new Vector(0,0,0);
		}
		return c;
	}
	private Vector Combined() {
		if (rightAnchor == null || leftAnchor == null) {
			if (rightAnchor == null) {if(leftAnchor.getLoc()!=null)return leftAnchor.getLoc().toVector().subtract(player.getLocation().toVector());}
			else if (leftAnchor == null) {if(rightAnchor.getLoc()!=null)return rightAnchor.getLoc().toVector().subtract(player.getLocation().toVector());}
			else return null;
		}
		Vector r = player.getLocation().toVector();
		if (rightAnchor.getLoc() != null) {
			rDis = player.getLocation().distance(rightAnchor.getLoc());
			r = rightAnchor.getLoc().toVector();
		}
		Vector l = player.getLocation().toVector();
		if (leftAnchor.getLoc() != null) {
			lDis = player.getLocation().distance(leftAnchor.getLoc());
			l = leftAnchor.getLoc().toVector();
		}
		r.subtract(player.getLocation().toVector());
		l.subtract(player.getLocation().toVector());
		r.normalize();
		l.normalize();
		Vector v = new Vector(0,0,0);
		v.add(r);
		v.add(l);
		v.normalize();
		if (v.angle(r) > 1d || v.angle(l) > 1d) {
			pmg.Partwith();
		}
		return v;
	}
	private void ReelEntity(Entity entity) {
		Vector dir = player.getLocation().toVector().subtract(entity.getLocation().toVector());
		try{entity.setVelocity(dir.normalize());}
		catch (java.lang.IllegalArgumentException e) { }
	}
	Particle.DustOptions dustOption = new Particle.DustOptions(Color.WHITE, 5);
	@Override
	public void run() {
		if (!isStop && pmg.getSting()) {
			player.setAllowFlight(true);
			if (player.isSneaking()) {
				player.setVelocity(player.getVelocity().add(player.getLocation().getDirection().multiply(0.1)));
			}
			if (Combined().dot(player.getVelocity()) < 0) {
				velocity = player.getVelocity();
				velocity = Calc(Combined(), velocity);
				try{player.setVelocity(velocity);}
				catch (java.lang.IllegalArgumentException e) { }
			}
			if (reel==1) {
				if (rightAnchor != null && rightAnchor.isPullable && rightAnchor.entity != null) ReelEntity(rightAnchor.entity);
				if (leftAnchor != null && leftAnchor.isPullable && leftAnchor.entity != null) ReelEntity(leftAnchor.entity);
				speed = Math.min(speed+0.1, 1d);
				velocity = Combined().add(player.getVelocity()).normalize();
				velocity.multiply(speed*speed);
				player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(), 10, 0.549019608F, 0.549019608F, 0.549019608F, 0F, dustOption);
				if (rDis < 0.5 || lDis < 0.5) velocity = new Vector(0,0,0);
				else if (rDis < 1 || lDis < 1) velocity.multiply(0.5);
				try{player.setVelocity(velocity);}
				catch (java.lang.IllegalArgumentException e) { }
			}
			else if (reel==-1) {
				speed = Math.min(speed+0.1, 1d);
				velocity = new Vector(0,-0.5,0);
				velocity.multiply(speed*speed);
				try{player.setVelocity(velocity);}
				catch (java.lang.IllegalArgumentException e) { }
			}
			if (jumped) {
				player.setVelocity(new Vector(0,0.5,0));
				jumped = false;
				pmg.Partwith();
			}
			reel = 0;
		} else {if (player.getGameMode()==GameMode.CREATIVE) player.setAllowFlight(false);}
		vertical_speed = player.getVelocity().getY();
	}
}
