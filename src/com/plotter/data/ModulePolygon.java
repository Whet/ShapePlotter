package com.plotter.data;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.plotter.algorithms.ConnectionPoint;

public class ModulePolygon {

	private List<int[]> points;
	private List<int[]> connectPoints;
	private List<ConnectionPoint> connections;
	private Polygon polygon;
	
	public ModulePolygon() {
		this.points = new ArrayList<>();
		this.connectPoints = new ArrayList<>();
		this.connections = new ArrayList<>();
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
	
	public void addConnectPoint(int x, int y, int x1, int y1, int x2, int y2, int identifier, int flavour) {
		this.connectPoints.add(new int[]{x, y, x1, y1, x2, y2});
		this.connections.add(new ConnectionPoint(x, y, x1, y1, x2, y2, identifier, flavour));
	}
	
	public void removeConnectPoint(int x, int y) {
		Iterator<int[]> iterator = this.connectPoints.iterator();
		
		while(iterator.hasNext()) {
			int[] next = iterator.next();
			
			if(next[0] == x && next[1] == y) {
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

	public List<int[]> getConnectPointsInts() {
		return connectPoints;
	}
	
	public List<ConnectionPoint> getConnectPoints() {
		
		List<ConnectionPoint> connections = new ArrayList<>();
		
		for(ConnectionPoint connection:this.connections) {
			connections.add(new ConnectionPoint(connection.getLocation().x, connection.getLocation().y, connection.getOuttie().x, connection.getOuttie().y, connection.getInnie().x, connection.getInnie().y, connection.getIdentifier(), connection.getFlavour()));
		}
		
		return connections;
	}
	
}
