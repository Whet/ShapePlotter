package com.plotter.data;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plotter.algorithms.MultiPoly;

public class DatabaseMultipoly extends MultiPoly {

	private Map<Integer,int[]> markerDisplacements;
	private Map<Integer,Double> markerRotations;
	private double rotation;
	
	public DatabaseMultipoly(int rotationComponent, MultiPoly multipoly, List<Integer> markerIds, List<Point> markerLocations, List<Double> markerRotations, Point polygonCentre) {
		super(multipoly.getConnectionPoints(), multipoly.getPolygons().toArray(new Polygon[multipoly.getPolygons().size()]));
		
		this.rotation = (Math.PI / 2) * rotationComponent;
		this.markerDisplacements = new HashMap<>();
		this.markerRotations = new HashMap<>();
		
		for(int i = 0; i < markerIds.size(); i++) {
			this.markerDisplacements.put(markerIds.get(i), new int[]{polygonCentre.x - markerLocations.get(i).x, polygonCentre.y - markerLocations.get(i).y});
		}
		
		for(int i = 0; i < markerRotations.size(); i++) {
			this.markerRotations.put(markerIds.get(i), markerRotations.get(i));
		}
		
		moveToOrigin();
		
	}

	private void moveToOrigin() {
		// Find top left corner
		Area area = new Area();
		for(Polygon polygon:this.polygons) {
			area.add(new Area(polygon));
		}
		double minX = area.getBounds2D().getMinX();
		double minY = area.getBounds2D().getMinY();
		
		this.translate(-(int)minX, -(int)minY);
	}

	public int[] getDisplacement(Integer markerId) {
		return markerDisplacements.get(markerId);
	}
	
	public int[] getDisplacement() {
		int[] averageLocation = new int[2];
		
		for(int[] displacement:markerDisplacements.values()) {
			averageLocation[0] += displacement[0];
			averageLocation[1] += displacement[1];
		}
		
		averageLocation[0] /= this.markerDisplacements.size();
		averageLocation[1] /= this.markerDisplacements.size();
		
		return averageLocation;
	}

	public double getRotation(Integer markerId) {
		return rotation;
	}
	
	public double getRotation() {
		return this.rotation;
	}
}
