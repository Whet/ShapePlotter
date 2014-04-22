package com.plotter.algorithms;

import java.awt.Point;
import java.util.Set;

public class ShapeData {

	public final Set<LibkokiUtils.MarkerInfo> markers;
	public final Set<Connection> connections;
	public final Set<Point> shapeVerticies;
	public final int shapeId;
	public final int componentId;
	
	public ShapeData(int shapeId, int componentId, Set<Point> shapeDataVerticies, Set<Connection> connections, Set<LibkokiUtils.MarkerInfo> markers) {
		this.connections = connections;
		this.shapeVerticies = shapeDataVerticies;
		this.markers = markers;
		this.shapeId = shapeId;
		this.componentId = componentId;
	}
	
	public static class Connection {
		public final int flavour;
		public final Point centre;
		public final double angle;
		
		public Connection(int flavour, Point centre, double angle) {
			this.flavour = flavour;
			this.centre = centre;
			this.angle = angle;
		}
	}
	
}
