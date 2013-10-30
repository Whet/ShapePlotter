package com.plotter.algorithms;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import com.plotter.data.Connection;
import com.plotter.data.Maths;
import com.plotter.gui.GridPanel;

public class MultiPoly {

	private List<Connection> connectedPoints;
	private List<Polygon> polygons;
	private Polygon mergedPolygon;
	private MultiPoly parent;
	
	public MultiPoly(List<Connection> connectedPoints, Polygon... polygons) {
		
		this.parent = null;
		this.polygons = new ArrayList<Polygon>();
		
		for(int i = 0; i < polygons.length; i++) {
			this.polygons.add(new Polygon(polygons[i].xpoints, polygons[i].ypoints, polygons[i].npoints));
		}
		
		this.connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < connectedPoints.size(); i++) {
			Connection connection = connectedPoints.get(i);
			this.connectedPoints.add(new Connection(connection.getFlavour(), connection.getCentre().x, connection.getCentre().y, connection.getOutside().x, connection.getOutside().y, connection.getInside().x, connection.getInside().y));
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
	
	public MultiPoly(MultiPoly parent, List<Connection> connectedPoints, List<Connection> connectedPoints1, List<Polygon> polygons1, List<Polygon> polygons2) {
		
		this.parent = parent;
		this.polygons = new ArrayList<Polygon>();
		
		for(int i = 0; i < polygons1.size(); i++) {
			this.polygons.add(new Polygon(polygons1.get(i).xpoints, polygons1.get(i).ypoints, polygons1.get(i).npoints));
		}
		for(int i = 0; i < polygons2.size(); i++) {
			this.polygons.add(new Polygon(polygons2.get(i).xpoints, polygons2.get(i).ypoints, polygons2.get(i).npoints));
		}
		
		this.connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < connectedPoints.size(); i++) {
			// if both out and in ends are inside of a polygon then don't add the connect point as it is in use
			Point end1 = new Point(connectedPoints.get(i).getOutside().x, connectedPoints.get(i).getOutside().y);
			Point end2 = new Point(connectedPoints.get(i).getInside().x, connectedPoints.get(i).getInside().y);
			
//			boolean e1 = false;
//			boolean e2 = false;
//			
//			for(Polygon polygon:this.polygons) {
//				if(polygon.contains(end1)) {
//					e1 = true;
//				}
//				if(polygon.contains(end2)) {
//					e2 = true;
//				}
//				if(e1 && e2)
//					break;
//			}
//			
//			if(!(e1 && e2))
				this.connectedPoints.add(new Connection(connectedPoints.get(i).getFlavour(), connectedPoints.get(i).getCentre().x, connectedPoints.get(i).getCentre().y, end1.x, end1.y, end2.x, end2.y));
		}
		for(int i = 0; i < connectedPoints1.size(); i++) {
			// if both out and in ends are inside of a polygon then don't add the connect point as it is in use
			Point end1 = new Point(connectedPoints1.get(i).getOutside().x, connectedPoints1.get(i).getOutside().y);
			Point end2 = new Point(connectedPoints1.get(i).getInside().x, connectedPoints1.get(i).getInside().y);
			
//			boolean e1 = false;
//			boolean e2 = false;
//			
//			for(Polygon polygon:this.polygons) {
//				if(polygon.contains(end1)) {
//					e1 = true;
//				}
//				if(polygon.contains(end2)) {
//					e2 = true;
//				}
//				if(e1 && e2)
//					break;
//			}
//			
//			if(!(e1 && e2))
				this.connectedPoints.add(new Connection(connectedPoints1.get(i).getFlavour(), connectedPoints1.get(i).getCentre().x, connectedPoints1.get(i).getCentre().y, end1.x, end1.y, end2.x, end2.y));
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
		
		ArrayList<Connection> connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < this.connectedPoints.size(); i++) {
			Connection connection = this.connectedPoints.get(i);
			connectedPoints.add(new Connection(connection.getFlavour(), connection.getCentre().x, connection.getCentre().y, connection.getOutside().x, connection.getOutside().y, connection.getInside().x, connection.getInside().y));
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
		
		List<int[]> connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < this.connectedPoints.size(); i++) {
			Connection connection = this.connectedPoints.get(i);
			connectedPoints.add(new int[]{connection.getCentre().x, connection.getCentre().y, connection.getOutside().x, connection.getOutside().y, connection.getInside().x, connection.getInside().y});
		}
		
		return connectedPoints;
	}

	public void translate(int deltaX, int deltaY) {
		for(int i = 0; i < this.polygons.size(); i++) {
			this.polygons.get(i).translate(deltaX, deltaY);
		}
		this.mergedPolygon.translate(deltaX, deltaY);
		
		ArrayList<Connection> translatedConnections = new ArrayList<>();
		
		for(Connection connection:this.connectedPoints) {
			Connection tC = connection.clone();
			tC.translate(deltaX, deltaY);
			translatedConnections.add(tC);
		}
		
		this.connectedPoints = translatedConnections;
	}

	public void rotate(Point centreOfRotation, double angle) {
		ArrayList<Polygon> rotatedPolygons = new ArrayList<>();
		for(Polygon polygon: this.polygons) {
			rotatedPolygons.add(rotatePoly(polygon,angle,centreOfRotation));
		}
		
		this.polygons = rotatedPolygons;
		
		Polygon rotatedMergedPolygon = new Polygon();
		
		for(int i = 0; i < this.mergedPolygon.npoints; i++) {
			Point rotatedPoint = rotatePoint(new Point(this.mergedPolygon.xpoints[i], this.mergedPolygon.ypoints[i]), centreOfRotation, angle);
			
			rotatedMergedPolygon.addPoint(Maths.round(rotatedPoint.x, GridPanel.GRID_SIZE), Maths.round(rotatedPoint.y, GridPanel.GRID_SIZE));
		}
		
		this.mergedPolygon = rotatedMergedPolygon;
		
		ArrayList<Connection> rotatedConnections = new ArrayList<>();
		
		for(Connection connection:this.connectedPoints) {
			Point rotatedConnection = rotatePoint(new Point(connection.getCentre().x, connection.getCentre().y), centreOfRotation, angle);
			Point rotatedConnection1 = rotatePoint(new Point(connection.getOutside().x, connection.getOutside().y), centreOfRotation, angle);
			Point rotatedConnection2 = rotatePoint(new Point(connection.getInside().x, connection.getInside().y), centreOfRotation, angle);
			rotatedConnections.add(new Connection(connection.getFlavour(), rotatedConnection.x, rotatedConnection.y, rotatedConnection1.x, rotatedConnection1.y, rotatedConnection2.x, rotatedConnection2.y));
		}
		
		this.connectedPoints = rotatedConnections;
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
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof MultiPoly) {
			
			if(polyMatch(this.getPolygons(), this.getMergedPolygon(), ((MultiPoly) obj).getPolygons(), ((MultiPoly) obj).getMergedPolygon()))
				return true;
			
			return false;
		}
		
		return super.equals(obj);
	}

	private boolean polyMatch(List<Polygon> polygons1, Polygon mergedPoly1, List<Polygon> polygons2, Polygon mergedPoly2) {
		
		Area polygon1 = new Area();
		Area polygon2 = new Area();
		
		int deltaX = (int) mergedPoly1.getBounds2D().getMinX();
		int deltaY = (int) mergedPoly1.getBounds2D().getMinY();
		
		for(int i = 0; i < polygons1.size(); i++) {
			Polygon nPoly = new Polygon(polygons1.get(i).xpoints, polygons1.get(i).ypoints, polygons1.get(i).npoints);
			nPoly.translate(-deltaX, -deltaY);
			Area area = new Area(nPoly);
			polygon1.add(area);
		}
		
		deltaX = (int) mergedPoly2.getBounds2D().getMinX();
		deltaY = (int) mergedPoly2.getBounds2D().getMinY();
		
		for(int i = 0; i < polygons2.size(); i++) {
			Polygon nPoly = new Polygon(polygons2.get(i).xpoints, polygons2.get(i).ypoints, polygons2.get(i).npoints);
			nPoly.translate(-deltaX, -deltaY);
			Area area = new Area(nPoly);
			polygon2.add(area);
		}
		
		if(polygon1.equals(polygon2))
			return true;
		
			
		return false;
		
	}

	private Polygon rotatePoly(Polygon polygon, double angle, Point centreOfRotation) {
		if(angle == 0)
			return polygon;
		
		Polygon rotatedPoly = new Polygon();
		for(int i = 0; i < polygon.npoints; i++) {
			Point rotatedPoint = rotatePoint(new Point(polygon.xpoints[i], polygon.ypoints[i]), centreOfRotation, angle);
			
			rotatedPoly.addPoint(Maths.round(rotatedPoint.x, GridPanel.GRID_SIZE), Maths.round(rotatedPoint.y, GridPanel.GRID_SIZE));
		}
		return rotatedPoly;
	}

	public MultiPoly getParent() {
		return parent;
	}

	public List<Connection> getConnectionPoints() {
		return this.connectedPoints;
	}

}
