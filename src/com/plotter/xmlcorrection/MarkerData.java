package com.plotter.xmlcorrection;

import java.awt.Point;

public class MarkerData {
	
	private Point location;
	private double rotation;
	private int markerNumber;
	
	public MarkerData(Point location, double rotation, int markerNumber) {
		this.location = location;
		this.rotation = rotation;
		this.markerNumber = markerNumber;
	}

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	public Point getLocation() {
		return location;
	}

	public void translate(int x, int y) {
		this.location.translate(x, y);
	}

	public void setLocation(Point realLocation) {
		this.location = realLocation;
	}

	public int getMarkerNumber() {
		return markerNumber;
	}
	
}
