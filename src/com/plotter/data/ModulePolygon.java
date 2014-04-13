package com.plotter.data;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModulePolygon {

	private List<int[]> points;
	private List<Connection> connectPoints;
	private List<Point> markerPoints;
	private Polygon polygon;
	
	public ModulePolygon() {
		this.points = new ArrayList<>();
		this.connectPoints = new ArrayList<>();
		this.markerPoints = new ArrayList<>();
		this.polygon = new Polygon();
	}
	
	public void addPoint(int x, int y) {
		this.points.add(new int[]{x, y});
		this.makePolygon();
	}
	
	public void removePoint(int x, int y) {
		
		Iterator<int[]> iterator = this.points.iterator();
		
		while(iterator.hasNext()) {
			int[] next = iterator.next();
			
			if(next[0] == x && next[1] == y) {
				iterator.remove();
				break;
			}
		}
		
		this.makePolygon();
	}
	
	public void addConnectPoint(int flavour, int x, int y, int inX, int inY, int outX, int outY) {
		this.connectPoints.add(new Connection(flavour, x, y, inX, inY, outX, outY));
	}
	
	public void removeConnectPoint(int x, int y) {
		Iterator<Connection> iterator = this.connectPoints.iterator();
		
		while(iterator.hasNext()) {
			Connection next = iterator.next();
			
			if(next.getCentre().x == x && next.getCentre().y == y) {
				iterator.remove();
				break;
			}
		}
	}
	
	public void addMarkerPoint(int x, int y) {
		this.markerPoints.add(new Point(x,y));
	}
	
	public void removeMarkerPoint(int x, int y) {
		
		Iterator<Point> iterator = this.markerPoints.iterator();
		
		while(iterator.hasNext()) {
			Point next = iterator.next();
			
			if(next.x == x && next.y == y) {
				iterator.remove();
				break;
			}
		}
	}
	
	private void makePolygon() {
		this.polygon.reset();
		
		this.polygon = new Polygon();
		
		for(int[] point:this.points) {
			polygon.addPoint(point[0], point[1]);
		}
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public List<int[]> getPoints() {
		return points;
	}

	public List<int[]> getConnectionPointsLocations() {
		List<int[]> connectionPoints = new ArrayList<>();
		
		for(Connection connection:this.connectPoints) {
			connectionPoints.add(new int[]{connection.getCentre().x, connection.getCentre().y, connection.getOutside().x, connection.getOutside().y, connection.getInside().x, connection.getInside().y});
		}
		
		return connectionPoints;
	}
	
	public List<Connection> getConnectionPoints() {
		return this.connectPoints;
	}

	public List<Point> getMarkerLocations() {
		return this.markerPoints;
	}
	
}
