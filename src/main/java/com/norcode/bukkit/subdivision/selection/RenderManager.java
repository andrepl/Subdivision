package com.norcode.bukkit.subdivision.selection;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class RenderManager implements Runnable {
	int MAX_OPS = 20;
	private LinkedList<Renderer> renderers = new LinkedList<Renderer>();
	private SubdivisionPlugin plugin;

	public RenderManager(SubdivisionPlugin plugin) {
		this.plugin = plugin;
	}

	public Renderer getRenderer(Player player) {
		for (Renderer r: renderers) {
			if (r.getPlayer().equals(player)) {
				return r;
			}
		}
		Renderer r = new Renderer(plugin, player);
		renderers.add(r);
		return r;
	}

	@Override
	public void run() {
		int i = 0;
		Renderer r;
		while (!renderers.isEmpty() && i < MAX_OPS) {
			r = renderers.pop();
			r.run();
			i ++;
			if (r.hasState()) {
				renderers.add(r);
			}
		}
	}
}
