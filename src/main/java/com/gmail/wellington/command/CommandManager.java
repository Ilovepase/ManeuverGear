package com.gmail.wellington.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.wellington.plugin.ManeuverGearMain;

public class CommandManager implements CommandExecutor {
	ManeuverGearMain plugin;
	public CommandManager(ManeuverGearMain plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) { 
		if (sender instanceof Player) {
			sender.sendMessage(cmd.getName());
			sender.sendMessage(args[0]);
			if (cmd.getName().equalsIgnoreCase("mgear")) {
				if (args.length == 1) {
					if (args[0].equals("get")) {
						Player player = (Player)sender;
						plugin.GiveGear(player.getUniqueId());
						return true;
					}
					else if (args[0].equals("reload")) {
						//kuso code
						plugin.reloadConfig();
						plugin.ReloadConfig();
						return true;
					}
				}
			}
		}
		return false;
	}
}
