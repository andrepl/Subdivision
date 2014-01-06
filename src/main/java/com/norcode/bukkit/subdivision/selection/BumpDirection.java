package com.norcode.bukkit.subdivision.selection;

public enum BumpDirection {
	EXPAND, CONTRACT;

	public BumpDirection getOpposite() {
		if (this.equals(EXPAND)) {
			return CONTRACT;
		}
		return EXPAND;
	}
}
