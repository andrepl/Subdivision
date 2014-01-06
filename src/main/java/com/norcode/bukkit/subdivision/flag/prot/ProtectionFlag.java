package com.norcode.bukkit.subdivision.flag.prot;

import com.norcode.bukkit.subdivision.flag.Flag;
import com.norcode.bukkit.subdivision.region.Region;

public abstract class ProtectionFlag extends Flag<RegionProtectionState> {

	protected ProtectionFlag(String name) {
		super(name);
	}

	protected ProtectionFlag(String name, String description) {
		super(name, description);
	}

	@Override
	public RegionProtectionState parseValue(String input) throws IllegalArgumentException {
		return RegionProtectionState.valueOf(input);
	}

	@Override
	public String serializeValue(Object value) {
		RegionProtectionState state = ((RegionProtectionState) value);
		return state == null?null:state.name();
	}

	@Override
	public RegionProtectionState getValue(Object value) {
		return (RegionProtectionState) value;
	}

	@Override
	public RegionProtectionState get(Region r) {
		RegionProtectionState state =(RegionProtectionState) r.getFlag(this);
		if (state.equals(RegionProtectionState.INHERIT)) {
			return (RegionProtectionState) r.getParent().getFlag(this);
		}
		return state;
	}
}
