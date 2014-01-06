package com.norcode.bukkit.subdivision.selection;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.region.Region;
import com.norcode.bukkit.subdivision.rtree.Bounded;
import com.norcode.bukkit.subdivision.rtree.Bounds;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CuboidSelection implements Bounded {

	Location p1;
	Location p2;
	protected UUID regionId = null;

	@Override
	public Bounds getBounds() {
		if (!isValid()) {
			if (p1 == null && p2 == null) {
				return null;
			}
			if (p1 != null) {
				return new Bounds(
						p1.getBlockX(), p1.getBlockY(), p1.getBlockZ(),
						p1.getBlockX(), p1.getBlockY(), p1.getBlockZ()
				);
			} else if (p2 != null) {
				return new Bounds(
						p2.getBlockX(), p2.getBlockY(), p2.getBlockZ(),
						p2.getBlockX(), p2.getBlockY(), p2.getBlockZ()
				);
			}
		}
		Location min = getMin();
		Location max = getMax();
		return new Bounds(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
	}

	public static CuboidSelection forRegion(Region r) {

		CuboidSelection sel = new CuboidSelection();

		sel.setP1(new Location(Bukkit.getServer().getWorld(r.getWorldId()),
				r.getMinX(), r.getMinY(), r.getMinZ()));
		sel.setP2(new Location(Bukkit.getServer().getWorld(r.getWorldId()),
				r.getMaxX(), r.getMaxY(), r.getMaxZ()));
		sel.regionId = r.getId();
		return sel;

	}

	public CuboidSelection(Location p1, Location p2) {
		this(p1);
		this.p2 = p2;
	}

	public CuboidSelection(Location p1) {
		this();
		this.p1 = p1;
	}

	public CuboidSelection() {}

	public Location getP1() {
		return p1;
	}

	public void setP1(Location p1) {
		this.p1 = p1;
	}

	public Location getP2() {
		return p2;
	}

	public void setP2(Location p2) {
		this.p2 = p2;
	}

	public Location getMin() {
		if (!isValid()) {
			return null;
		}
		return new Location(p1.getWorld(),
				Math.min(p1.getBlockX(), p2.getBlockX()),
				Math.min(p1.getBlockY(), p2.getBlockY()),
				Math.min(p1.getBlockZ(), p2.getBlockZ()));
	}

	public Location getMax() {
		if (!isValid()) return null;
		return new Location(p1.getWorld(),
				Math.max(p1.getBlockX(), p2.getBlockX()),
				Math.max(p1.getBlockY(), p2.getBlockY()),
				Math.max(p1.getBlockZ(), p2.getBlockZ()));
	}

	public int getVolume() {
		if (!isValid()) return -1;
		return getArea() * (getHeight());
	}

	public World getWorld() {
		if (p1 != null) {
			return p1.getWorld();
		} else if (p2 != null) {
			return p2.getWorld();
		}
		return null;
	}

	public int getArea() {
		if (!isValid()) return -1;
		return getWidth() * getLength();
	}

	public int getLength() {
		if (!isValid()) return -1;
		return 1 + (getMax().getBlockZ() - getMin().getBlockZ());
	}

	public int getWidth() {
		if (!isValid()) return -1;
		return 1 + (getMax().getBlockX() - getMin().getBlockX());
	}

	public int getHeight() {
		if (!isValid()) return -1;
		return 1 + (getMax().getBlockY() - getMin().getBlockY());
	}

	public boolean isValid() {
		return (p1 != null && p2 != null && p1.getWorld().getUID().equals(p2.getWorld().getUID()));
	}

	public static CuboidSelection fromWorldEdit(SubdivisionPlugin plugin, Player player) {
		WorldEditPlugin we = plugin.getWorldEdit();
		if (we != null) {
			Selection sel = we.getSelection(player);
			if (sel instanceof com.sk89q.worldedit.bukkit.selections.CuboidSelection) {
				CuboidSelection cs = new CuboidSelection();
				return new CuboidSelection(
					sel.getMinimumPoint(),
					sel.getMaximumPoint()
				);
			}
		}
		return null;
	}

	public void bump(BumpDirection bumpDir, BlockFace facing) {
		int amt = 1;
		if (bumpDir == BumpDirection.CONTRACT) {
			amt = -1;
		}
		if (facing.getModX() < 0) {
			if (getP1().getBlockX() <= getP2().getBlockX()) {
				setP1(getP1().subtract(amt,0,0));
			} else if (getP2().getBlockX() <= getP1().getBlockX()) {
				setP2(getP2().subtract(amt,0,0));
			}
		} else if (facing.getModX() > 0) {
			if (getP1().getBlockX() >= getP2().getBlockX()) {
				setP1(getP1().add(amt,0,0));
			} else if (getP2().getBlockX() >= getP1().getBlockX()) {
				setP2(getP2().add(amt,0,0));
			}
		}
		if (facing.getModZ() < 0) {
			if (getP1().getBlockZ() <= getP2().getBlockZ()) {
				setP1(getP1().subtract(0,0,amt));
			} else if (getP2().getBlockZ() <= getP1().getBlockZ()) {
				setP2(getP2().subtract(0,0,amt));
			}
		} else if (facing.getModZ() > 0) {
			if (getP1().getBlockZ() >= getP2().getBlockZ()) {
				setP1(getP1().add(0,0,amt));
			} else if (getP2().getBlockZ() >= getP1().getBlockZ()) {
				setP2(getP2().add(0,0,amt));
			}
		}
		if (facing.getModY() < 0) {
			if (getP1().getBlockY() <= getP2().getBlockY()) {
				setP1(getP1().subtract(0,amt,0));
			} else if (getP2().getBlockY() <= getP1().getBlockY()) {
				setP2(getP2().subtract(0,amt,0));
			}
		} else if (facing.getModY() > 0) {
			if (getP1().getBlockY() >= getP2().getBlockY()) {
				setP1(getP1().add(0,amt,0));
			} else if (getP2().getBlockY() >= getP1().getBlockY()) {
				setP2(getP2().add(0,amt,0));
			}
		}

	}

	public UUID getRegionId() {
		return regionId;
	}

	public void setRegionId(UUID regionId) {
		this.regionId = regionId;
	}

	public String toColoredString() {
		Location min = getMin();
		Location max = getMax();
		return (ChatColor.RESET + " area: " +
				ChatColor.DARK_AQUA + "" + getWidth() + ChatColor.RESET + "x" +
				ChatColor.DARK_AQUA + "" + getLength() + ChatColor.RESET + " height: " +
				ChatColor.DARK_AQUA + "" + getHeight() + ChatColor.RESET);
	}
}
