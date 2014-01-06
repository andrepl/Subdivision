package com.norcode.bukkit.subdivision.flag.prot;

import com.norcode.bukkit.subdivision.flag.BooleanFlag;
import com.norcode.bukkit.subdivision.flag.Flag;

public class MobSpawningFlag extends ProtectionFlag {

	public static MobSpawningFlag flag = new MobSpawningFlag();

	private MobSpawningFlag() {
		super("mobspawning", "Mob spawning");
	}

}