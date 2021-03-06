package com.norcode.bukkit.subdivision.flag.perm;

import com.norcode.bukkit.subdivision.flag.Flag;
import com.norcode.bukkit.subdivision.region.Region;

public class PermissionFlag extends Flag<RegionPermissionState> {

	protected PermissionFlag(String name, String desc) {
		super(name, desc);
	}

	@Override
	public RegionPermissionState get(Region r) {
		RegionPermissionState ps = getValue(r);
		if (ps == RegionPermissionState.INHERIT) {
			return getValue(r.getParent().getFlag(this));
		}
		return ps;
	}

	@Override
	public RegionPermissionState parseValue(String input) throws IllegalArgumentException {
		if (input == null) {
			input = "INHERIT";
		}
		return RegionPermissionState.valueOf(input.toUpperCase());
	}

	@Override
	public String serializeValue(Object value) {
		if (value == null) {
			return "INHERIT";
		}
		return ((RegionPermissionState) value).name();
	}

	@Override
	public RegionPermissionState getValue(Object value) {
		if (value == null) {
			return RegionPermissionState.INHERIT;
		}
		return (RegionPermissionState) value;
	}

}
