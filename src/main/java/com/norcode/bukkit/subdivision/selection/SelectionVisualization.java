package com.norcode.bukkit.subdivision.selection;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.rtree.Point3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

public class SelectionVisualization {

	private CuboidWireframe wireframe;
	private MaterialData blockType;
	private SubdivisionPlugin plugin;
	private Player player;
	private World world;
	private CuboidSelection selection;
	private Point3D selP1;
	private Point3D selP2;
	private int drawStep = 0;

	private SelectionVisualization(SubdivisionPlugin plugin, Player player, World world, CuboidSelection selection) {
		this.plugin = plugin;
		this.player = player;
		this.world = world;
		this.wireframe = CuboidWireframe.fromSelection(selection);
		Location p1 = selection.getP1();
		selP1 = p1 == null ? null : new Point3D(p1.getBlockX(), p1.getBlockY(), p1.getBlockZ());
		Location p2 = selection.getP2();
		selP2 = p2 == null ? null : new Point3D(p2.getBlockX(), p2.getBlockY(), p2.getBlockZ());
	}

	public boolean draw() {

		Point3D blk = wireframe.step(drawStep++);
		if (blk == null) {
			drawStep --;
			return false;
		}

		if (drawStep <= 8) {
			Material mat = Material.GLOWSTONE;
			if (blk.equals(selP1)) {
				mat = Material.DIAMOND_BLOCK;
			} else if (blk.equals(selP2)) {
				mat = Material.EMERALD_BLOCK;
			}
			this.player.sendBlockChange(new Location(world, (int) blk.getX(), (int) blk.getY(), (int) blk.getZ()), mat, (byte) 0);
		} else {
			this.player.sendBlockChange(new Location(world, (int) blk.getX(), (int) blk.getY(), (int) blk.getZ()), Material.STAINED_GLASS, (byte) 0);
		}
		return true;
	}

	public boolean undraw() {
		Point3D blk = wireframe.step(--drawStep);
		if (blk == null) {
			drawStep ++;
			return false;
		}
		Location l = new Location(world, (int) blk.getX(), (int) blk.getY(), (int) blk.getZ());
		Block b = world.getBlockAt(l);
		this.player.sendBlockChange(l, b.getType(), b.getData());
		return true;
	}

	public static SelectionVisualization create(SubdivisionPlugin plugin, Player player) {
		try {
			return new SelectionVisualization(plugin, player, player.getWorld(), plugin.getPlayerSelection(player));
		} catch (NullPointerException ex) {
			return null;
		}
	}
}
