package com.norcode.bukkit.subdivision.selection;


import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.rtree.Bounds;
import com.norcode.bukkit.subdivision.rtree.Point3D;

import java.util.ArrayList;

public class VerticalCuboidWireframe extends CuboidWireframe {
	public VerticalCuboidWireframe(Bounds bounds) {
		super(bounds);


		ArrayList<Integer> pts = new ArrayList<Integer>();
		int[] mid = toPointArray(drawLine((bounds.getY2()-bounds.getY1())-3), bounds.getY1());
		for (int i: mid) { pts.add(i); }
		for (int i=0;i<=15;i+=5) {
			int y = (bounds.getY1() - 2) - i;
			if (y > 0) {
				pts.add(y);
			}
			y = (bounds.getY2() + 2) + i;
			if (y < 255) {
				pts.add(y);
			}
		}
		yPoints = new int[pts.size()];
		for (int i=0;i<pts.size();i++) {
			yPoints[i] = pts.get(i);
		}
	}

	@Override
	public Point3D step(int i) {
		switch (i) {
			/**
			 ['x1,y1,z1',
			 'x2,y2,z2',
			 'x1,y1,z2',
			 'x2,y2,z1',
			 'x1,y2,z1',
			 'x2,y1,z2',
			 'x1,y2,z2',
			 'x2,y1,z1']
			 */
			case 0:
//				SubdivisionPlugin.debug("Rendering Corner 1");
				return new Point3D(bounds.getX1(), bounds.getY1(), bounds.getZ1());
			case 1:
//				SubdivisionPlugin.debug("Rendering Corner 2");
				return new Point3D(bounds.getX2(), bounds.getY2(), bounds.getZ2());
			case 2:
//				SubdivisionPlugin.debug("Rendering Corner 3");
				return new Point3D(bounds.getX1(), bounds.getY1(), bounds.getZ2());
			case 3:
//				SubdivisionPlugin.debug("Rendering Corner 4");
				return new Point3D(bounds.getX2(), bounds.getY2(), bounds.getZ1());
			case 4:
//				SubdivisionPlugin.debug("Rendering Corner 5");
				return new Point3D(bounds.getX1(), bounds.getY2(), bounds.getZ1());
			case 5:
//				SubdivisionPlugin.debug("Rendering Corner 6");
				return new Point3D(bounds.getX2(), bounds.getY1(), bounds.getZ2());
			case 6:
//				SubdivisionPlugin.debug("Rendering Corner 7");
				return new Point3D(bounds.getX1(), bounds.getY2(), bounds.getZ2());
			case 7:
//				SubdivisionPlugin.debug("Rendering Corner 8");
				return new Point3D(bounds.getX2(), bounds.getY1(), bounds.getZ1());
			default:
				int lineStep = i-8;
				int idx = lineStep / 12;
				switch (lineStep % 3) {
					case 0:
						if (idx < xPoints.length) {
							switch (lineStep % 4) {
								case 0:	return new Point3D(xPoints[idx], bounds.getY1(), bounds.getZ1());
								case 1:	return new Point3D(xPoints[idx], bounds.getY1(), bounds.getZ2());
								case 2: return new Point3D(xPoints[idx], bounds.getY2(), bounds.getZ1());
								case 3: return new Point3D(xPoints[idx], bounds.getY2(), bounds.getZ2());
							}
						} else if (idx < yPoints.length) {
							switch (lineStep % 4) {
								case 0: return new Point3D(bounds.getX1(), yPoints[idx], bounds.getZ1());
								case 1: return new Point3D(bounds.getX1(), yPoints[idx], bounds.getZ2());
								case 2: return new Point3D(bounds.getX2(), yPoints[idx], bounds.getZ1());
								case 3: return new Point3D(bounds.getX2(), yPoints[idx], bounds.getZ2());
							}
						} else if (idx < zPoints.length) {
							switch (lineStep % 4) {
								case 0: return new Point3D(bounds.getX1(), bounds.getY1(), zPoints[idx]);
								case 1: return new Point3D(bounds.getX1(), bounds.getY2(), zPoints[idx]);
								case 2: return new Point3D(bounds.getX2(), bounds.getY1(), zPoints[idx]);
								case 3: return new Point3D(bounds.getX2(), bounds.getY2(), zPoints[idx]);
							}
						} else {
							return null;
						}
					case 1:
						if (idx < yPoints.length) {
							switch (lineStep % 4) {
								case 0: return new Point3D(bounds.getX1(), yPoints[idx], bounds.getZ1());
								case 1: return new Point3D(bounds.getX1(), yPoints[idx], bounds.getZ2());
								case 2: return new Point3D(bounds.getX2(), yPoints[idx], bounds.getZ1());
								case 3: return new Point3D(bounds.getX2(), yPoints[idx], bounds.getZ2());
							}
						} else if (idx < zPoints.length) {
							switch (lineStep % 4) {
								case 0: return new Point3D(bounds.getX1(), bounds.getY1(), zPoints[idx]);
								case 1: return new Point3D(bounds.getX1(), bounds.getY2(), zPoints[idx]);
								case 2: return new Point3D(bounds.getX2(), bounds.getY1(), zPoints[idx]);
								case 3: return new Point3D(bounds.getX2(), bounds.getY2(), zPoints[idx]);
							}
						} else if (idx < xPoints.length) {
							switch (lineStep % 4) {
								case 0:	return new Point3D(xPoints[idx], bounds.getY1(), bounds.getZ1());
								case 1:	return new Point3D(xPoints[idx], bounds.getY1(), bounds.getZ2());
								case 2: return new Point3D(xPoints[idx], bounds.getY2(), bounds.getZ1());
								case 3: return new Point3D(xPoints[idx], bounds.getY2(), bounds.getZ2());
							}
						} else {
							return null;
						}
					case 2:
						if (idx < zPoints.length) {
							switch (lineStep % 4) {
								case 0: return new Point3D(bounds.getX1(), bounds.getY1(), zPoints[idx]);
								case 1: return new Point3D(bounds.getX1(), bounds.getY2(), zPoints[idx]);
								case 2: return new Point3D(bounds.getX2(), bounds.getY1(), zPoints[idx]);
								case 3: return new Point3D(bounds.getX2(), bounds.getY2(), zPoints[idx]);
							}
						} else if (idx < xPoints.length) {
							switch (lineStep % 4) {
								case 0:	return new Point3D(xPoints[idx], bounds.getY1(), bounds.getZ1());
								case 1:	return new Point3D(xPoints[idx], bounds.getY1(), bounds.getZ2());
								case 2: return new Point3D(xPoints[idx], bounds.getY2(), bounds.getZ1());
								case 3: return new Point3D(xPoints[idx], bounds.getY2(), bounds.getZ2());
							}
						} else if (idx < yPoints.length) {
							switch (lineStep % 4) {
								case 0: return new Point3D(bounds.getX1(), yPoints[idx], bounds.getZ1());
								case 1: return new Point3D(bounds.getX1(), yPoints[idx], bounds.getZ2());
								case 2: return new Point3D(bounds.getX2(), yPoints[idx], bounds.getZ1());
								case 3: return new Point3D(bounds.getX2(), yPoints[idx], bounds.getZ2());
							}
						} else {
							return null;
						}
					default:
						// shouldn't get here.
						return null;
				}
		}
	}
}
