package com.norcode.bukkit.subdivision.selection;

import com.norcode.bukkit.subdivision.rtree.Bounds;
import org.bukkit.Location;

public class VerticalCuboidSelection extends CuboidSelection {

	private int maxHeight = 127;
	public VerticalCuboidSelection(Location p1, Location p2) {
		super(p1, p2);
		maxHeight = p1.getWorld().getMaxHeight();
	}

	public VerticalCuboidSelection(Location p1) {
		super(p1);
	}

	public VerticalCuboidSelection() {
		super();
	}

	@Override
	public Location getMin() {
		Location loc = super.getMin();
		loc.setY(0);
		return loc;
	}

	@Override
	public Location getMax() {
		Location loc = super.getMax();
		loc.setY(maxHeight);
		return loc;
	}


	@Override
	public int getVolume() {
		return super.getArea() * maxHeight-1;
	}

	@Override
	public int getHeight() {
		return maxHeight;
	}

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
		Location min = super.getMin();
		Location max = super.getMax();
		return new Bounds(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
	}
}
