package com.norcode.bukkit.subdivision.selection;

import java.lang.reflect.InvocationTargetException;

public enum SelectionMode {

	VERTICAL(VerticalCuboidSelection.class),
	CUBOID(CuboidSelection.class);

	private Class<? extends CuboidSelection> selectionClass;

	SelectionMode(Class<? extends CuboidSelection> selectionClass) {
		this.selectionClass = selectionClass;
	}

	public CuboidSelection createNewSelection() {
		try {
			return this.selectionClass.getConstructor().newInstance();
		} catch (NoSuchMethodException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}
}
