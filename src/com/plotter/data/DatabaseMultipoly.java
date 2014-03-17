package com.plotter.data;

import java.awt.Point;
import java.awt.Polygon;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plotter.algorithms.LineMergePolygon;
import com.plotter.algorithms.MultiPoly;

public class DatabaseMultipoly implements Serializable {

	private Map<Integer,int[]> markerDisplacements;
	private Map<Integer,Double> markerRotations;
	private double rotation;
	private LineMergePolygon lmp;
	private Polygon mergedPolygon;
	private List<Connection> connections;
	
	public DatabaseMultipoly(int rotationComponent, Polygon mergedPolygon, LineMergePolygon lmp, List<Connection> connections, List<Integer> markerIds, List<Point> markerLocations, List<Double> markerRotations, Point polygonCentre) {
		
		this.lmp = lmp;
		this.mergedPolygon = new Polygon(mergedPolygon.xpoints, mergedPolygon.ypoints, mergedPolygon.npoints);
		this.connections = new ArrayList<>();
		
		for(Connection connection:connections) {
			this.connections.add(connection.clone());
		}
		
		this.rotation = (Math.PI / 2) * rotationComponent;
		this.markerDisplacements = new HashMap<>();
		this.markerRotations = new HashMap<>();
		
		for(int i = 0; i < markerIds.size(); i++) {
			this.markerDisplacements.put(markerIds.get(i), new int[]{polygonCentre.x - markerLocations.get(i).x, polygonCentre.y - markerLocations.get(i).y});
		}
		
		for(int i = 0; i < markerRotations.size(); i++) {
			this.markerRotations.put(markerIds.get(i), markerRotations.get(i));
		}
		
//		int minX = (int)this.mergedPolygon.getBounds2D().getMinX();
//		int minY = (int)this.mergedPolygon.getBounds2D().getMinY();
//		
//		lmp.translate(-minX, -minY);
//		this.mergedPolygon.translate(-minX, -minY);
//		
//		for(Connection connection:this.connections) {
//			connection.translate(-minX, -minY);
//		}
		
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
	
	public void rotateConnections(int deltaX, int deltaY, double rotation) {
		ArrayList<Connection> rotatedConnections = new ArrayList<>();
		
		for (Connection connection : this.connections) {
			
			Point rotatedConnection = MultiPoly.rotatePoint(new Point(connection.getCentre().x, connection.getCentre().y), new Point(deltaX, deltaY), rotation);
			Point rotatedConnection1 = MultiPoly.rotatePoint(new Point(connection.getOutside().x, connection.getOutside().y), new Point(deltaX, deltaY), rotation);
			Point rotatedConnection2 = MultiPoly.rotatePoint(new Point(connection.getInside().x, connection.getInside().y), new Point(deltaX, deltaY), rotation);
			
			rotatedConnections.add(new Connection(connection.getFlavour(),
					rotatedConnection.x, rotatedConnection.y,
					rotatedConnection1.x, rotatedConnection1.y,
					rotatedConnection2.x, rotatedConnection2.y));
		}
	}

	public double getRotation(Integer markerId) {
		return rotation;
	}
	
	public double getRotation() {
		return this.rotation;
	}

	public Polygon getMergedPolygon() {
		return this.mergedPolygon;
	}

	public LineMergePolygon getLineMergePolygon() {
		return this.lmp;
	}

	public List<Connection> getConnectionPoints() {
		return this.connections;
	}
}
