package com.norcode.bukkit.subdivision.command;

import com.norcode.bukkit.playerid.PlayerID;
import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.flag.Flag;
import com.norcode.bukkit.subdivision.region.GlobalRegion;
import com.norcode.bukkit.subdivision.region.Region;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

public class RegionCommand extends BaseCommand {

	public RegionCommand(SubdivisionPlugin plugin) {
		super(plugin, "region", new String[] {}, null, new String[] { "Region Help" });
		registerSubcommand(new RegionInfoCommand(plugin));
		registerSubcommand(new RegionCreateCommand(plugin));
		registerSubcommand(new RegionSetCommand(plugin));
		registerSubcommand(new RegionSaveCommand(plugin));
		plugin.getServer().getPluginCommand("region").setExecutor(this);
	}


	private class RegionInfoCommand extends BaseCommand {
		public RegionInfoCommand(SubdivisionPlugin plugin) {
			super(plugin, "info", new String[] {}, null, new String[] {"info help"});
		}

		@Override
		protected void onExecute(CommandSender sender, String label, LinkedList<String> args) throws CommandError {
			Region region = plugin.getRegion((Player) sender);
			sender.sendMessage("Region::" + region.getId().toString().substring(0,8) + ((region instanceof GlobalRegion) ? " (Global Region)" : ""));
			if (!(region instanceof GlobalRegion)) {
				sender.sendMessage("Owners: " + formatPlayerList(region.getOwnerIds()));
				sender.sendMessage("Members: " + formatPlayerList(region.getMemberIds()));
				sender.sendMessage("Bounds: " + region.getBounds().toString());
			}
			String flagStr = "";
			for (Flag f: Flag.getAllFlags()) {
				String val = f.serializeValue(region.getFlag(f));
				flagStr += f.getName() + "=" + val + ", ";
			}
			if (flagStr.endsWith(", ")) {
				flagStr = flagStr.substring(0,flagStr.length()-2);
			}
			sender.sendMessage("Flags: " + flagStr);
		}

	}

	private String formatPlayerList(Set<UUID> ownerIds) {
		Iterator<UUID> it = ownerIds.iterator();
		String s = "";
		while (it.hasNext()) {
			s += PlayerID.getPlayerName(it.next());
			if (it.hasNext()) {
				s += ", ";
			}
		}
		return s;
	}
}
