package com.plotter.data;

import java.awt.Point;
import java.io.Serializable;

public class Connection implements Serializable{

	private int flavour;
	private Point centre, inside, outside;
	
	public Connection(int flavour, int x, int y, int inX, int inY, int outX, int outY) {
		this.flavour = flavour;
		this.centre = new Point(x,y);
		this.inside = new Point(inX,inY);
		this.outside = new Point(outX,outY);
	}

	public Point getCentre() {
		return centre;
	}

	public Point getInside() {
		return inside;
	}

	public Point getOutside() {
		return outside;
	}
	
	public void translate(int deltaX, int deltaY) {
		this.centre.translate(deltaX, deltaY);
		this.inside.translate(deltaX, deltaY);
		this.outside.translate(deltaX, deltaY);
	}

	public Connection clone() {
		return new Connection(flavour, centre.x, centre.y, inside.x, inside.y, outside.x, outside.y);
	}

	public int getFlavour() {
		return this.flavour;
	}
	
}
