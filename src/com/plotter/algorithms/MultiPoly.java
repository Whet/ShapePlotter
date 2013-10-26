package com.plotter.algorithms;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import com.plotter.gui.GridPanel;

public class MultiPoly {

	private List<int[]> connectedPoints;
	private List<Polygon> polygons;
	private Polygon mergedPolygon;
	
	public MultiPoly(List<int[]> connectedPoints, Polygon... polygons) {
		
		this.polygons = new ArrayList<Polygon>();
		
		for(int i = 0; i < polygons.length; i++) {
			this.polygons.add(new Polygon(polygons[i].xpoints, polygons[i].ypoints, polygons[i].npoints));
		}
		
		this.connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < connectedPoints.size(); i++) {
			this.connectedPoints.add(new int[]{connectedPoints.get(i)[0], connectedPoints.get(i)[1], connectedPoints.get(i)[2], connectedPoints.get(i)[3], connectedPoints.get(i)[4], connectedPoints.get(i)[5]});
		}
		
		ArrayList<Point> points = new ArrayList<>();
		
		for(Polygon polygon:this.polygons) {
			for(int i = 0; i < polygon.npoints; i++) {
				points.add(new Point(polygon.xpoints[i], polygon.ypoints[i]));
			}
		}
		
		ArrayList<Point> mergedPoints = FastConvexHull.execute(points);
		
		this.mergedPolygon = new Polygon();
		
		for(Point point: mergedPoints) {
			this.mergedPolygon.addPoint(point.x, point.y);
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		
		Polygon[] polygons = new Polygon[this.polygons.size()];
		
		for(int i = 0; i < this.polygons.size(); i++) {
			polygons[i] = new Polygon(this.polygons.get(i).xpoints, this.polygons.get(i).ypoints, this.polygons.get(i).npoints);
		}
		
		ArrayList<int[]> connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < this.connectedPoints.size(); i++) {
			connectedPoints.add(new int[]{this.connectedPoints.get(i)[0], this.connectedPoints.get(i)[1], this.connectedPoints.get(i)[2], this.connectedPoints.get(i)[3], this.connectedPoints.get(i)[4], this.connectedPoints.get(i)[5]});
		}
		
		return new MultiPoly(connectedPoints, polygons);
	}

	public List<Polygon> getPolygons() {
		return this.polygons;
	}

	public Polygon getMergedPolygon() {
		return mergedPolygon;
	}

	public List<int[]> getConnectPoints() {
		return this.connectedPoints;
	}

	public void translate(int deltaX, int deltaY) {
		for(int i = 0; i < this.polygons.size(); i++) {
			this.polygons.get(i).translate(deltaX, deltaY);
		}
		this.mergedPolygon.translate(deltaX, deltaY);
		
		ArrayList<int[]> translatedConnections = new ArrayList<>();
		
		for(int[] connection:this.connectedPoints) {
			
			int[] translatedConnection = new int[connection.length];
			
			for(int i = 0; i < connection.length; i+=2) {
				translatedConnection[i] = connection[i] + deltaX;
				translatedConnection[i+1] = connection[i + 1] + deltaY;
			}
			
			translatedConnections.add(translatedConnection);
		}
		
		this.connectedPoints = translatedConnections;
	}

	public void rotate(Point centreOfRotation, double angle) {
		ArrayList<Polygon> rotatedPolygons = new ArrayList<>();
		for(Polygon polygon: this.polygons) {
			Polygon rotatedPoly = new Polygon();
			for(int i = 0; i < polygon.npoints; i++) {
				Point rotatedPoint = rotatePoint(new Point(polygon.xpoints[i], polygon.ypoints[i]), centreOfRotation, angle);
				
				rotatedPoly.addPoint(round(rotatedPoint.x, GridPanel.GRID_SIZE), round(rotatedPoint.y, GridPanel.GRID_SIZE));
			}
			rotatedPolygons.add(rotatedPoly);
		}
		
		this.polygons = rotatedPolygons;
		
		Polygon rotatedMergedPolygon = new Polygon();
		
		for(int i = 0; i < this.mergedPolygon.npoints; i++) {
			Point rotatedPoint = rotatePoint(new Point(this.mergedPolygon.xpoints[i], this.mergedPolygon.ypoints[i]), centreOfRotation, angle);
			
			rotatedMergedPolygon.addPoint(round(rotatedPoint.x, GridPanel.GRID_SIZE), round(rotatedPoint.y, GridPanel.GRID_SIZE));
		}
		
		this.mergedPolygon = rotatedMergedPolygon;
		
		//TODO connection points
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
	
	// http://stackoverflow.com/questions/9303604/rounding-up-a-number-to-nearest-multiple-of-5
	private int round(double number, int roundFigure) {
	    return (int) (Math.round(number/roundFigure) * roundFigure);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof MultiPoly) {
			// Check all polygons have equal points
			
			if(this.getPolygons().size() != ((MultiPoly) obj).getPolygons().size())
				return false;
			
			int matchingPolygons = 0;
			
			for(Polygon polygon:this.polygons) {
				for(Polygon polygon1:((MultiPoly) obj).getPolygons()) {
					if(polyMatch(polygon, polygon1)) {
						matchingPolygons++;
						break;
					}
				}
			}
			
			if(matchingPolygons == this.getPolygons().size()) {
				return true;
			}
			else {
				return false;
			}
		}
		
		return super.equals(obj);
	}

	private boolean polyMatch(Polygon polygon, Polygon polygon1) {
		
		return polygon.getBounds2D().getWidth() == polygon1.getBounds2D().getWidth() && polygon.getBounds2D().getHeight() == polygon1.getBounds2D().getHeight();
		
	}
	
}
