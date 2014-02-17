package com.plotter.data;

import java.awt.Point;
import java.awt.Polygon;

import com.plotter.algorithms.MultiPoly;

public class DatabaseMultipoly extends MultiPoly {

	private int[] displacement;
	
	public DatabaseMultipoly(MultiPoly multipoly, Point marker, Point centre) {
		super(multipoly.getConnectionPoints(), multipoly.getPolygons().toArray(new Polygon[multipoly.getPolygons().size()]));
		
		this.displacement = new int[]{centre.x - marker.x, centre.y - marker.y};
	}

	public int[] getDisplacement() {
		return displacement;
	}
}
