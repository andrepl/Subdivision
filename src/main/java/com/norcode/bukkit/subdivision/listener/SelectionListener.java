package com.norcode.bukkit.subdivision.listener;


import com.norcode.bukkit.playerid.PlayerID;
import com.norcode.bukkit.subdivision.MetaKey;
import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.datastore.DatastoreException;
import com.norcode.bukkit.subdivision.region.Region;
import com.norcode.bukkit.subdivision.rtree.Bounds;
import com.norcode.bukkit.subdivision.rtree.Point3D;
import com.norcode.bukkit.subdivision.selection.BumpDirection;
import com.norcode.bukkit.subdivision.selection.CuboidSelection;
import com.norcode.bukkit.subdivision.selection.SelectionVisualization;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SelectionListener implements Listener {
	private static final int MIN_DISTANCE = 16;
	private SubdivisionPlugin plugin;

	public SelectionListener(SubdivisionPlugin plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	private boolean isVisualizationOn(Player player) {
		return player.hasMetadata(MetaKey.VISUALIZATION_ON);
	}

	private void setVisualization(Player player, boolean on) {
		if (on) {
			boolean seenHelp = true;
			long now = System.currentTimeMillis();
			if (!player.hasMetadata(MetaKey.WAND_HELP_SEEN)) {
				seenHelp = false;
			} else if (player.getMetadata(MetaKey.WAND_HELP_SEEN).get(0).asLong() < now - TimeUnit.HOURS.toMillis(1)) {
				seenHelp = false;
			}
			if (!seenHelp) {
				player.setMetadata(MetaKey.WAND_HELP_SEEN, new FixedMetadataValue(plugin, now));
				ConfigurationSection cfg = PlayerID.getPlayerData(plugin.getName(), player);
				cfg.set(MetaKey.WAND_HELP_SEEN, now);
				PlayerID.savePlayerData(plugin.getName(), player, cfg);
				player.sendMessage("This is the " + ChatColor.GOLD + "Selection Wand" + ChatColor.RESET + ".  Left or Right click while sneaking to set one corner of your current selection.  If you already have a selection made, left or right click without sneaking to expand or contract the selection in the direction youre facing.");
			}
			CuboidSelection selection = plugin.getPlayerSelection(player);
			if (!selection.isValid()) {
				Region currentRegion = plugin.getRegion(player);
				if (currentRegion.isOwner(player)) {
					selection = CuboidSelection.forRegion(currentRegion);
					player.setMetadata(MetaKey.SELECTION, new FixedMetadataValue(plugin, selection));
				}
			}
			SelectionVisualization viz = SelectionVisualization.create(plugin, player);
			plugin.getRenderManager().getRenderer(player).draw(viz);
			player.setMetadata(MetaKey.VISUALIZATION_ON, new FixedMetadataValue(plugin, true));
		} else {
			if (player.hasMetadata(MetaKey.VISUALIZATION_ON)) {
				CuboidSelection selection = plugin.getPlayerSelection(player);
				player.removeMetadata(MetaKey.VISUALIZATION_ON, plugin);
				plugin.getRenderManager().getRenderer(player).clear();
			}
		}
	}

	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent event) {
		ConfigurationSection playerData = PlayerID.getPlayerData(plugin.getName(), event.getPlayer());
		if (!event.getPlayer().hasMetadata(MetaKey.WAND_HELP_SEEN)) {
			if (playerData.contains(MetaKey.WAND_HELP_SEEN)) {
				event.getPlayer().setMetadata(MetaKey.WAND_HELP_SEEN, new FixedMetadataValue(plugin, playerData.getLong(MetaKey.WAND_HELP_SEEN)));
			}
		}
	}

	@EventHandler
	public void onPlayerCloseInventory(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player) {
			Player p = (Player) event.getPlayer();
			if (isVisualizationOn(p) && !isWandItem(p.getItemInHand())) {
				setVisualization(p, false);
			} else if (isWandItem(p.getItemInHand()) && !isVisualizationOn(p)) {
				setVisualization(p, true);
			}
		}
	}

	@EventHandler
	public void onPlayerDropWand(PlayerDropItemEvent event) {
		if (isWandItem(event.getItemDrop().getItemStack())) {
			setVisualization(event.getPlayer(), false);
		}
	}


	@EventHandler
	public void onPlayerChangeToFromWand(PlayerItemHeldEvent event) {
		ItemStack goingTo = event.getPlayer().getInventory().getItem(event.getNewSlot());
		if (isWandItem(goingTo)) {
			setVisualization(event.getPlayer(), true);
		} else if (event.getPlayer().hasMetadata(MetaKey.VISUALIZATION_ON)) {
			setVisualization(event.getPlayer(), false);
		}
	}

	@EventHandler(ignoreCancelled=false)
	public void onPlayerClickWand(PlayerInteractEvent event) {
		if (isWandItem(event.getItem())) {
			Player p = event.getPlayer();
			CuboidSelection selection = plugin.getPlayerSelection(p);
			Location loc = (event.getClickedBlock() == null ? getLocationSwinging(p) : event.getClickedBlock().getLocation());
			Location min = selection.getMin();
			Location max = selection.getMax();
			// save old coords to reset if this collides with an illegal region or
			// if the player can't afford it.
			Location sp1 = selection.getP1().clone();
			Location sp2 = selection.getP2().clone();

			boolean cancel = false;
			// 'edge-bumping' can only be done done if both corners are set.
			// otherwise, sneak or not will set corners.
			if (!p.isSneaking() && min != null && max != null) {
				// bump the current selection
				// tricky!
				if (min == null && max == null) {
					p.sendMessage("You haven't made a selection yet.  Sneak and right or left click to set a corner.");
					event.setCancelled(true);
					return;
				}
				Bounds selBounds = new Bounds(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
				Location pl = p.getLocation();
				BlockFace facing;
				BumpDirection bumpDir = BumpDirection.EXPAND;
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK || (event.getAction() == Action.RIGHT_CLICK_AIR && event.isCancelled())) {
					bumpDir = BumpDirection.CONTRACT;
				}
				if (selBounds.contains(new Point3D(pl.getBlockX(), pl.getBlockY(), pl.getBlockZ()))) {
					// from inside	
					facing = getBlockFace(pl.getDirection());
				} else {
					facing = getBlockFace(pl.getDirection()).getOppositeFace();
					bumpDir = bumpDir.getOpposite();
				}
				selection.bump(bumpDir, facing);
			} else {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK || (event.getAction() == Action.RIGHT_CLICK_AIR && event.isCancelled())) {
					event.getPlayer().sendMessage("Set " + ChatColor.GREEN + "Pt.2" + ChatColor.RESET + " to " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
					selection.setP2(loc);
				} else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_AIR && event.isCancelled())) {
					event.getPlayer().sendMessage("Set " + ChatColor.AQUA + "Pt.1" + ChatColor.RESET + " to " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
					selection.setP1(loc);
				} else {
					return;
				}
			}
			List<Region> overlapping = plugin.getRegionManager()
					.getGlobalRegion(event.getPlayer().getWorld().getUID()).search(selection.getBounds());

			if (selection.getRegionId() != null) {
				Region selectedRegion = plugin.getRegionManager().getById(selection.getRegionId());
				overlapping.remove(selectedRegion);
				if (!overlapping.isEmpty()) {
					// might be in an illegal spot
					if (overlapping.size() > 1 || (overlapping.size() == 1 &&
							overlapping.get(0).getBounds().insersects(selection.getBounds()))) {
						// can't cross borders.
						cancel = true;
						event.getPlayer().sendMessage(ChatColor.RED + "Your border overlaps another one.");
					}
					// there is only one, and HOPEFULLY were fully contained in it
					Region parent = overlapping.get(0);
					if (!parent.getBounds().contains(selection.getBounds())) {
						event.getPlayer().sendMessage(ChatColor.RED + "Your border overlaps it's parent.");
						cancel = true;
					}
					if (!parent.isOwner(event.getPlayer())) {
						event.getPlayer().sendMessage(ChatColor.RED + "You do not own this region.");
						cancel = true;
					}
				}
				Bounds expandedBounds = selection.getBounds();
				expandedBounds.setX1(expandedBounds.getX1() - MIN_DISTANCE);
				expandedBounds.setX2(expandedBounds.getX2() + MIN_DISTANCE);
				expandedBounds.setZ1(expandedBounds.getZ1() - MIN_DISTANCE);
				expandedBounds.setZ2(expandedBounds.getZ2() + MIN_DISTANCE);
				expandedBounds.setY1(Math.max(0, expandedBounds.getY1() - MIN_DISTANCE));
				expandedBounds.setY2(Math.min(255, expandedBounds.getY2() + MIN_DISTANCE));
				List<Region> nearby = plugin.getRegionManager().getGlobalRegion(event.getPlayer().getWorld().getUID())
						.search(expandedBounds);
				nearby.remove(selectedRegion);
				nearby.removeAll(overlapping);
				if (!overlapping.isEmpty()) {
					for (Region r: overlapping) {
						if (!r.isOwner(event.getPlayer())) {
							event.getPlayer().sendMessage(ChatColor.RED + "Your border is too close to another one.");
							cancel = true;
						}
					}
				}
			}
			String costStr = "";
			if (selection.getRegionId() != null) {
				Region r = plugin.getRegionManager().getById(selection.getRegionId());
				int selValue = (int) (selection.getArea() * Math.max(1, (selection.getHeight()/8.0)));
				int rValue = (int) (r.getArea() * Math.max(1, (r.getHeight()/8.0)));
				int cost = (selValue - rValue);
				int avail = plugin.getClaimAllowance(event.getPlayer());
				if (cost > avail) {
					event.getPlayer().sendMessage(ChatColor.RED + "You do not have enough claim blocks.");
					cancel = true;
				} else {
					costStr = " Remaining Claim Blocks: " + (avail - cost);
				}

				if (cancel) {
					selection.setP1(sp1);
					selection.setP2(sp2);
				} else {
					event.getPlayer().sendMessage(ChatColor.GOLD + " * " + selection.toColoredString() + costStr);
					r.adjustBounds(selection.getBounds());
					plugin.getRegionManager().add(plugin.getRegionManager().remove(r));
					plugin.setClaimAllowance(event.getPlayer(), avail-cost);
					try {
						plugin.getDatastore().saveRegion(r.getRegionData());
					} catch (DatastoreException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}
			}

			event.setCancelled(true);
			setVisualization(event.getPlayer(), true);
		}
	}

	private BlockFace getBlockFace(Vector direction) {
		if (Math.abs(direction.getY()) > 0.5) {
			if (direction.getY() < 0) {
				return BlockFace.DOWN;
			} else {
				return BlockFace.UP;
			}
		}
		Set<BlockFace> faces = new HashSet<BlockFace>();
		if (Math.abs(direction.getX()) > 0.5) {
			if (direction.getX() < 0) {
				faces.add(BlockFace.WEST);
			} else {
				faces.add(BlockFace.EAST);
			}
		}
		if (Math.abs(direction.getZ()) > 0.5) {
			if (direction.getZ() < 0) {
				faces.add(BlockFace.NORTH);
			} else {
				faces.add(BlockFace.SOUTH);
			}
		}
		if (faces.contains(BlockFace.NORTH)) {
			if (faces.contains(BlockFace.EAST)) {
				return BlockFace.NORTH_EAST;
			} else if (faces.contains(BlockFace.WEST)) {
				return BlockFace.NORTH_WEST;
			}
			return BlockFace.NORTH;
		} else if (faces.contains(BlockFace.SOUTH)) {
			if (faces.contains(BlockFace.EAST)) {
				return BlockFace.SOUTH_EAST;
			} else if (faces.contains(BlockFace.WEST)) {
				return BlockFace.SOUTH_WEST;
			}
			return BlockFace.SOUTH;
		} else if (faces.contains(BlockFace.WEST)) {
			return BlockFace.WEST;
		}
		return BlockFace.EAST;
	}

	private Location getLocationSwinging(Player p) {
		Block b;
		List<Block> los = p.getLineOfSight(null, 12);
		b = los.get(los.size()-1);
		return b.getLocation();
	}

	private boolean isWandItem(ItemStack item) {
		return item != null && item.getType().equals(Material.GOLD_SPADE);
	}

}
