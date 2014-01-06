package com.norcode.bukkit.subdivision.command;

import com.norcode.bukkit.subdivision.MetaKey;
import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.selection.CuboidSelection;
import com.norcode.bukkit.subdivision.selection.SelectionMode;
import com.norcode.bukkit.subdivision.selection.SelectionVisualization;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SelectCommand extends BaseCommand {

	public SelectCommand(SubdivisionPlugin plugin) {
		super(plugin, "select", new String[]{"wand"}, "subdivision.command.select",
				new String[]{"used to create and manipulate selections which can be saved as claims, cities, and plots."});
		registerSubcommand(new ModeCommand(plugin));
		registerSubcommand(new ClearCommand(plugin));
		plugin.getServer().getPluginCommand("select").setExecutor(this);
	}

	public static class ClearCommand extends SelectionSubcommand {
		public ClearCommand(SubdivisionPlugin plugin) {
			super(plugin, "clear", new String[] {"reset"}, null,
					new String[] {"selection clear help"});
		}

		@Override
		public void onExecute(Player player, CuboidSelection selection, LinkedList<String> args) throws CommandError {
			SelectionMode mode;
			if (!player.hasMetadata(MetaKey.SELECTION_MODE)) {
				player.setMetadata(MetaKey.SELECTION_MODE, new FixedMetadataValue(plugin, SelectionMode.VERTICAL));
			}
			mode = (SelectionMode) player.getMetadata(MetaKey.SELECTION_MODE).get(0).value();
			CuboidSelection newsel = mode.createNewSelection();
			player.setMetadata(MetaKey.SELECTION, new FixedMetadataValue(plugin, newsel));
			player.sendMessage("Your selection has been cleared.");
		}
	}

	public static class ModeCommand extends SelectionSubcommand {

		public ModeCommand(SubdivisionPlugin plugin) {
			super(plugin, "mode", new String[] {}, null, new String[] {"Mode help"});
		}

		@Override
		public void onExecute(Player player, CuboidSelection selection, LinkedList<String> args) throws CommandError {
			SelectionMode mode;
			if (!player.hasMetadata(MetaKey.SELECTION_MODE)) {
				player.setMetadata(MetaKey.SELECTION_MODE, new FixedMetadataValue(plugin, SelectionMode.VERTICAL));
			}
			mode = (SelectionMode) player.getMetadata(MetaKey.SELECTION_MODE).get(0).value();
			if (args.size() == 0) {
				player.sendMessage("Current Selection Mode: " + ChatColor.GOLD + mode.name());
				return;
			}
			mode = SelectionMode.valueOf(args.peek().toUpperCase());
			if (mode == null) {
				throw new CommandError("Invalid Selection Mode: " + args.peek() + "(expecting VERTICAL or CUBOID)");
			}
			player.setMetadata(MetaKey.SELECTION_MODE, new FixedMetadataValue(plugin, mode));

			CuboidSelection sel = plugin.getPlayerSelection(player);
			CuboidSelection newsel = mode.createNewSelection();
			newsel.setP1(sel.getP1());
			newsel.setP2(sel.getP2());
			player.setMetadata(MetaKey.SELECTION, new FixedMetadataValue(plugin, newsel));
			player.sendMessage("Selection Mode is now: " + ChatColor.GOLD + mode.name());
		}

		@Override
		protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
			List<String> results = new ArrayList<String>();
			for (SelectionMode mode: SelectionMode.values()) {
				if (mode.name().toLowerCase().startsWith(args.peek().toLowerCase())) {
					results.add(mode.name());
				}
			}
			return results;
		}
	}

	public static abstract class SelectionSubcommand extends BaseCommand {
		public SelectionSubcommand(SubdivisionPlugin plugin, String name, String[] aliases, String requiredPermission, String[] help) {
			super(plugin, name, aliases, requiredPermission, help);
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			if (!(commandSender instanceof Player)) {
				throw new CommandError("This command is only available in-game.");
			}
			Player player = (Player) commandSender;
			CuboidSelection selection = plugin.getPlayerSelection(player);

			this.onExecute(player, selection, args);

			// re-render the visualization, cuz why not.
			// but re-fetch the selection since it might have changed.
			selection = plugin.getPlayerSelection(player);
			SelectionVisualization viz = SelectionVisualization.create(plugin, player);
			plugin.getRenderManager().getRenderer(player).draw(viz);
		}

		public abstract void onExecute(Player player, CuboidSelection selection, LinkedList<String> args) throws CommandError;

	}
}
