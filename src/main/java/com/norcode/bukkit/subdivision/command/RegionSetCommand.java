package com.norcode.bukkit.subdivision.command;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.flag.Flag;
import com.norcode.bukkit.subdivision.flag.perm.PermissionFlag;
import com.norcode.bukkit.subdivision.flag.perm.RegionPermissionState;
import com.norcode.bukkit.subdivision.region.Region;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RegionSetCommand extends BaseCommand {

	public RegionSetCommand(SubdivisionPlugin plugin) {
		super(plugin,"set", new String[] {}, "subdivision.command.region.set", new String[] {"Region Set Help"});
		registerSubcommand(new SetFlagCommand(plugin));
	}

	private class SetFlagCommand extends BaseCommand {
		public SetFlagCommand(SubdivisionPlugin plugin) {
			super(plugin, "flag", new String[] {}, "subdivision.command.region.set.flag", new String[] {"Set Flag Help"});
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			if (!(commandSender instanceof Player)) {
				throw new CommandError("This command is only available to players.");
			}

			Player player = (Player) commandSender;

			if (args.size() == 0) {
				showHelp(commandSender, label, args);
				return;
			}

			Region region = plugin.getRegion(player);
			if (region == null) {
				throw new CommandError("There is no region defined here.");
			}

			String flagPartial = args.peek().toLowerCase();
			List<Flag> results = new ArrayList<Flag>();
			for (Flag f: Flag.getAllFlags()) {
				if (f.getName().toLowerCase().startsWith(flagPartial)) {
					results.add(f);
				}
			}
			if (results.size() == 0) {
				throw new CommandError("Unknown flag: " + flagPartial);
			} else if (results.size() > 1) {
				throw new CommandError("Ambiguous flag: " + flagPartial + " (matches " + StringUtils.join(results, ", ") + ")");
			}
			Flag flag = results.get(0);
			if (args.size() == 1) {
				showFlagHelp(commandSender, flag);
				return;
			} else {
				try {
					Object value = flag.parseValue(args.get(1));
					region.setFlag(flag, value);
				} catch (IllegalArgumentException ex) {
					throw new CommandError("Invalid value for flag " + flag.getName() + ": " + args.get(1));
				}
			}
		}

		private void showFlagHelp(CommandSender commandSender, Flag flag) {
			commandSender.sendMessage("This is help for the flag `" + flag.getName() + "`");
		}

		@Override
		protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
			List<String> res = new ArrayList<String>();
			for (Flag f: Flag.getAllFlags()) {
				if (f.getName().toLowerCase().startsWith(args.peek().toLowerCase())) {
					res.add(f.getName());
				}
			}

			if (args.size() == 2) {
				if (res.size() == 1) {
					Flag f = Flag.fromKey(res.get(0));
					res.clear();
					if (f instanceof PermissionFlag) {
						for (RegionPermissionState s: RegionPermissionState.values()) {
							if (s.name().toLowerCase().startsWith(args.peekLast().toLowerCase())) {
								res.add(s.name());
							}
						}
					}
				} else {
					return null;
				}
			}
			return res;
		}
	}

}
