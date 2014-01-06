package com.norcode.bukkit.subdivision.selection;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Renderer implements Runnable {

	public static enum State {
		DRAWING, CLEARING, DRAWN, CLEARED;
	}

	private final SubdivisionPlugin plugin;
	private State state = State.CLEARED;
	private Player player;
	private SelectionVisualization working;
	private SelectionVisualization next;

	public Renderer(SubdivisionPlugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean hasState() {
		return state == State.DRAWING || state == State.CLEARING || state == State.DRAWN;
	}

	public void clear() {
		if (state == State.DRAWING || state == State.DRAWN) {
			state = State.CLEARING;
		}
	}

	public void draw(SelectionVisualization viz) {
		if (viz == null) {
			clear();
			return;
		}
		switch (state) {
			case CLEARED:
				working = viz;
				state = State.DRAWING;
				break;
			case CLEARING:
				next = viz;
				break;
			case DRAWING:
			case DRAWN:
				next = viz;
				state = State.CLEARING;
				break;
		}

	}

	public void run() {
		if (working != null) {
			if (state == State.DRAWING) {
				if (!working.draw()) {
					state = State.DRAWN;
				}
			} else if (state == State.CLEARING) {
				if (!working.undraw()) {
					state = State.CLEARED;
					if (next != null) {
						state = State.DRAWING;
						working = next;
						next = null;
					}
				}
			}
		}
	}
}
