package com.norcode.bukkit.subdivision.selection;

import com.norcode.bukkit.subdivision.SubdivisionPlugin;
import com.norcode.bukkit.subdivision.rtree.Bounds;
import com.norcode.bukkit.subdivision.rtree.Point3D;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class CuboidWireframe {

	protected int[] xPoints = new int[0];
	protected int[] yPoints = new int[0];
	protected int[] zPoints = new int[0];
	protected Bounds bounds;

	public CuboidWireframe(Bounds bounds) {
		this.bounds = bounds;
		int width = (int) bounds.getWidth() + 1;
		int height = (int) bounds.getHeight() + 1;
		int depth = (int) bounds.getDepth() + 1;
		if (width > 3) {
			xPoints = toPointArray(drawLine(width-2), (int) bounds.getMin().getX()+1);
		}
		if (height > 3) {
			yPoints = toPointArray(drawLine(height-2), (int) bounds.getMin().getY()+1);
		}
		if (depth > 3) {
			zPoints = toPointArray(drawLine(depth-2), (int) bounds.getMin().getZ()+1);
		}
	}

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

	protected int[] toPointArray(String s, int x) {
		char[] chars = s.toCharArray();
		int[] points = new int[StringUtils.countMatches(s, "-")];
		int ctr = 0;
		for (int i=0;i<chars.length;i++) {
			if (chars[i] == '-') {
				points[ctr++] = x+i;
			}
		}
		return points;
	}

	private ArrayList<Integer> getK(int n) {
		ArrayList<Integer> points = new ArrayList<Integer>();
		if (n <= 2) {
			return points;
		}
		if (n < 4) {
			points.add(2);
			return points;
		}
		int max = (n%2==0) ? n/2 : (n+1)/2;
		double num = -1;
		for (int gap=1;gap<max;gap++) {
			num = (n+gap) / (double) (1+gap);
			if (num % 1 == 0 && num > 2) {
				points.add((int) num);
			}
		}
		return points;
	}

	protected String drawLine(int length) {
		if (length == 35) {
			int i=1;
		}
		boolean even = length % 2 == 0;
		if (length <= 4) {
			return "-" + StringUtils.repeat(" ", (length - 2)) + "-";
		} else if (even) {
			int halfSize = length/2;
			boolean halfEven = halfSize % 2 == 0;
			String p1 = drawLine(halfEven ? halfSize + 1 : halfSize);
			String p2 = drawLine(halfEven ? halfSize + 1 : halfSize);
			if (halfEven) {
				p1 = p1.substring(0, p1.length()-1);
				p2 = StringUtils.reverse(p2).substring(1);
			}
			return p1+p2;
		}

		ArrayList<Integer> points = getK(length);
		int pts = -1;
		if (points.size() >= 3) {
			pts = points.get(2);
		} else if (points.size() == 2 && length > 20) {
			// split again;
			int halfSize = length/2;
			boolean halfEven = halfSize % 2 == 0;
			String p1 = drawLine(halfEven ? halfSize + 1 : halfSize);
			String p2 = drawLine(halfEven ? halfSize + 1 : halfSize);
			if (halfEven) {
				p1 = p1.substring(0, p1.length()-1);
				p2 = StringUtils.reverse(p2).substring(1);
			}
			return p1+" "+p2;
		} else {
			pts = points.get(0);
		}

		String gap = StringUtils.repeat(" ", (length-pts)/(pts-1));
		return StringUtils.repeat("-" + gap, pts).trim();
	}

	public static CuboidWireframe fromSelection(CuboidSelection selection) {
		if (selection instanceof VerticalCuboidSelection) {
			return new VerticalCuboidWireframe(selection.getBounds());
		} else {
			return new CuboidWireframe(selection.getBounds());
		}
	}
}
