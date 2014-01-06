package com.norcode.bukkit.subdivision.command;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.datastore.DatastoreException;
import com.norcode.bukkit.subdivision.datastore.RegionData;
import com.norcode.bukkit.subdivision.rtree.Bounds;
import com.norcode.bukkit.subdivision.selection.CuboidSelection;
import com.norcode.bukkit.subdivision.region.GlobalRegion;
import com.norcode.bukkit.subdivision.region.Region;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;


public class RegionSaveCommand extends BaseCommand {

	public RegionSaveCommand(SubdivisionPlugin plugin) {
		super(plugin,"save", new String[] {"redefine"}, "subdivision.command.region.save", new String[] {"Region Save Help"});
	}

	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		if (!(commandSender instanceof Player))
			throw new CommandError("This command is only available to players.");

		Player player = (Player) commandSender;
		CuboidSelection selection = plugin.getPlayerSelection(player);
		if (!selection.isValid()) {
			throw new CommandError("You do not have a valid selection.");
		}
		if (selection.getRegionId() == null) {
			throw new CommandError("You do not have a region selected.");
		}
		Location min = selection.getMin();
		Location max = selection.getMax();
		Bounds bounds = new Bounds(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
		RegionData data = new RegionData(selection.getWorld(), bounds);
		GlobalRegion gr = plugin.getRegionManager().getGlobalRegion(selection.getWorld().getUID());
		List<Region> overlapping = gr.search(data.getBounds());
		Region selectedRegion = plugin.getRegionManager().getById(selection.getRegionId());
		plugin.debug("Overlapping: " + overlapping.size() + ", contains selected?: " + overlapping.contains(selectedRegion));
		plugin.debug("Removed: " + overlapping.remove(selectedRegion));
		plugin.debug("Overlapping: " + overlapping.size());
		if (!overlapping.isEmpty()) {

			// might be in an illegal spot
			if (overlapping.size() > 1 || (overlapping.size() == 1 &&
					overlapping.get(0).getBounds().insersects(data.getBounds()))) {
				// can't cross borders.
				throw new CommandError("Your selection crosses an existing border");
			}
			// there is only one, and HOPEFULLY were fully contained in it
			Region parent = overlapping.get(0);
			if (!parent.getBounds().contains(data.getBounds())) {
				throw new CommandError("ERROR! Tell metalhedd that intersects is broken!");
			}
			if (!parent.isOwner(player)) {
				throw new CommandError("Your selection is within a region you do not own.");
			}
		}
		Region region = plugin.getRegionManager().getById(selection.getRegionId());
		region.adjustBounds(selection.getBounds());
		plugin.getRegionManager().add(plugin.getRegionManager().remove(region));
		try {
			plugin.getDatastore().saveRegion(region.getRegionData());
		} catch (DatastoreException e) {
			e.printStackTrace();
		}
	}
}
