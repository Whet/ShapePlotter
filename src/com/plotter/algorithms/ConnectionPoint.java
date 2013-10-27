package com.plotter.algorithms;

import java.awt.Point;

public class ConnectionPoint {

	private Point location, innie, outtie;
	private int identifier;
	private int flavour;
	
	public ConnectionPoint(int x, int y, int x1, int y1, int x2, int y2, int identifier, int flavour) {
		this.location = new Point(x, y);
		this.innie = new Point(x1, y1);
		this.outtie = new Point(x2, y2);
		this.identifier = identifier;
		this.flavour = flavour;
	}

	public Point getLocation() {
		return location;
	}

	public Point getInnie() {
		return innie;
	}

	public Point getOuttie() {
		return outtie;
	}

	public int getIdentifier() {
		return identifier;
	}

	public int getFlavour() {
		return flavour;
	}

	public void translate(int deltaX, int deltaY) {
		this.location.translate(deltaX, deltaY);
		this.innie.translate(deltaX, deltaY);
		this.outtie.translate(deltaX, deltaY);
	}

	public void rotate(Point centreOfRotation, double angle) {
		this.location = rotatePoint(location, centreOfRotation, angle);
		this.innie = rotatePoint(innie, centreOfRotation, angle);
		this.outtie = rotatePoint(outtie, centreOfRotation, angle);
	}

	// http://stackoverflow.com/questions/10533403/how-to-rotate-a-polygon-around-a-point-with-java
	private Point rotatePoint(Point pt, Point center, double angle) {
	    double cosAngle = Math.cos(angle);
	    double sinAngle = Math.sin(angle);
	    double dx = (pt.x-center.x);
	    double dy = (pt.y-center.y);
	
	    pt.x = center.x + (int) (dx*cosAngle-dy*sinAngle);
	    pt.y = center.y + (int) (dx*sinAngle+dy*cosAngle);
	    return pt;
	}
	
}
