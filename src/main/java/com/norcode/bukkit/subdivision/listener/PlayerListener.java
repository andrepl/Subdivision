package com.norcode.bukkit.subdivision.listener;

import com.norcode.bukkit.subdivision.MetaKey;
import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.datastore.DatastoreException;
import com.norcode.bukkit.subdivision.datastore.RegionData;
import com.norcode.bukkit.subdivision.region.Region;
import com.norcode.bukkit.subdivision.rtree.Bounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.LazyMetadataValue;

import java.util.List;
import java.util.concurrent.Callable;

public class PlayerListener implements Listener {

	SubdivisionPlugin plugin;


	public PlayerListener(SubdivisionPlugin plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final String playerName = event.getPlayer().getName();
		event.getPlayer().setMetadata(MetaKey.ACTIVE_REGION, new LazyMetadataValue(plugin, new Callable<Object>() {
			@Override
			public Region call() throws Exception {
				Player player = plugin.getServer().getPlayerExact(playerName);
				return plugin.getRegionManager().getRegion(player.getLocation());
			}
		}));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		Location from = event.getFrom();
		if (from.getBlockX() != to.getBlockX()
				|| from.getBlockZ() != to.getBlockZ()
				|| from.getBlockY() != to.getBlockY()
				|| !from.getWorld().getUID().equals(to.getWorld().getUID())) {
			event.getPlayer().getMetadata(MetaKey.ACTIVE_REGION).get(0).invalidate();
		}
	}

	@EventHandler(ignoreCancelled = true, priority= EventPriority.MONITOR)
	public void onPlayerPlaceChest(BlockPlaceEvent event) {
		// Create the players 'first claim' when they place a chest in an unclaimed area.
		if (event.getBlockPlaced().getType() == Material.CHEST) {
			if (!plugin.hasFirstClaim(event.getPlayer())) {
				Location bl = event.getBlock().getLocation();
				Bounds chestBounds = new Bounds(bl.getBlockX()-7, bl.getBlockY()-3, bl.getBlockZ()-7, bl.getBlockX()+7, bl.getBlockY()+11, bl.getBlockZ() + 7);
				List<Region> overlaps = plugin.getRegionManager().getGlobalRegion(event.getPlayer().getWorld().getUID()).search(chestBounds);
				if (overlaps.isEmpty()) {
					RegionData data = new RegionData(event.getPlayer().getWorld(), chestBounds);
					Region region = new Region(plugin, data);
					region.addOwner(event.getPlayer());
					plugin.getRegionManager().add(region);
					try {
						plugin.getDatastore().saveRegion(region.getRegionData());
					} catch (DatastoreException e) {
						event.getPlayer().sendMessage("Something went wrong!");
						return;
					}
					event.getPlayer().sendMessage("The land around this chest has been protected for you!");
					plugin.setFirstClaim(event.getPlayer(), region.getId());
				} else {
					// Notify the player that they WOULD HAVE had a protected region, but there was no room for it.
					event.getPlayer().sendMessage("You have just placed a chest near someone elses protected property.  if you place your chest further away, you can create your own claim.");
				}
			}
		}
	}
}