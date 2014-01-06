package com.norcode.bukkit.subdivision.flag;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.flag.perm.BuildingFlag;
import com.norcode.bukkit.subdivision.flag.perm.ButtonsFlag;
import com.norcode.bukkit.subdivision.flag.perm.ContainersFlag;
import com.norcode.bukkit.subdivision.flag.perm.FarmingFlag;
import com.norcode.bukkit.subdivision.flag.perm.PVPFlag;
import com.norcode.bukkit.subdivision.flag.prot.ExplosionFlag;
import com.norcode.bukkit.subdivision.flag.prot.FireFlag;
import com.norcode.bukkit.subdivision.flag.prot.MobSpawningFlag;
import com.norcode.bukkit.subdivision.flag.prot.PistonFlag;
import com.norcode.bukkit.subdivision.region.Region;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;

import java.util.Collection;
import java.util.LinkedHashMap;

public abstract class Flag<T> {

	private static LinkedHashMap<String, Flag> registry = new LinkedHashMap<String, Flag>();
	protected static Gson gson = new Gson();

	protected String name;
	protected String description;

	public abstract T parseValue(String input) throws IllegalArgumentException;
	public abstract String serializeValue(Object value);
	public abstract T getValue(Object value);

	protected Flag(String name) {
		this.name = name;
	}

	protected Flag(String name, String description) {
		this(name);
		this.description = description;
	}

	public static boolean register(Flag flag) {
		if (registry.containsKey(flag.getName())) {
			return false;
		}
		registry.put(flag.getName().toLowerCase(), flag);
		return true;
	}

	public String getName() {
		return name;
	}

	public static Flag fromKey(String flagKey) {
		return registry.get(flagKey.toLowerCase());
	}


	public static void setupFlags(SubdivisionPlugin subdivisionPlugin) {
		// register stock flags
		register(PVPFlag.flag);
		register(ContainersFlag.flag);
		register(BuildingFlag.flag);
		register(ButtonsFlag.flag);
		register(FarmingFlag.flag);
		register(PistonFlag.flag);
		register(MobSpawningFlag.flag);
		register(FireFlag.flag);
		register(ExplosionFlag.flag);
		// enable all registered flags.

		for (Flag f: registry.values()) {
			f.onEnable(subdivisionPlugin);
		}
	}

	protected void onEnable(SubdivisionPlugin plugin) {
	}

	protected void onDisable() {}

	public static Collection<Flag> getAllFlags() {
		return registry.values();
	}
	public abstract Object get(Region r);
}
