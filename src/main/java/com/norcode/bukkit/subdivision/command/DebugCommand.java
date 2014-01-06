package com.norcode.bukkit.subdivision.command;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class DebugCommand extends BaseCommand {

	public DebugCommand(SubdivisionPlugin plugin) {
		super(plugin, "debug", new String[] {"dbg"}, "subdivision.debug", new String[] {""});
		plugin.getServer().getPluginCommand("debug").setExecutor(this);
	}
}
