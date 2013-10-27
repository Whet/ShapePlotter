package com.plotter.algorithms;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import com.plotter.data.Maths;
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
	
	public MultiPoly(List<int[]> connectedPoints, List<int[]> connectedPoints1, List<Polygon> polygons1, List<Polygon> polygons2) {
		
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
			Point end1 = new Point(connectedPoints.get(i)[2], connectedPoints.get(i)[3]);
			Point end2 = new Point(connectedPoints.get(i)[4], connectedPoints.get(i)[5]);
			
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
				this.connectedPoints.add(new int[]{connectedPoints.get(i)[0], connectedPoints.get(i)[1], end1.x, end1.y, end2.x, end2.y});
		}
		for(int i = 0; i < connectedPoints1.size(); i++) {
			// if both out and in ends are inside of a polygon then don't add the connect point as it is in use
			Point end1 = new Point(connectedPoints1.get(i)[2], connectedPoints1.get(i)[3]);
			Point end2 = new Point(connectedPoints1.get(i)[4], connectedPoints1.get(i)[5]);
			
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
				this.connectedPoints.add(new int[]{connectedPoints1.get(i)[0], connectedPoints1.get(i)[1], end1.x, end1.y, end2.x, end2.y});
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
			rotatedPolygons.add(rotatePoly(polygon,angle,centreOfRotation));
		}
		
		this.polygons = rotatedPolygons;
		
		Polygon rotatedMergedPolygon = new Polygon();
		
		for(int i = 0; i < this.mergedPolygon.npoints; i++) {
			Point rotatedPoint = rotatePoint(new Point(this.mergedPolygon.xpoints[i], this.mergedPolygon.ypoints[i]), centreOfRotation, angle);
			
			rotatedMergedPolygon.addPoint(Maths.round(rotatedPoint.x, GridPanel.GRID_SIZE), Maths.round(rotatedPoint.y, GridPanel.GRID_SIZE));
		}
		
		this.mergedPolygon = rotatedMergedPolygon;
		
		ArrayList<int[]> rotatedConnections = new ArrayList<>();
		
		for(int[] connection:this.connectedPoints) {
			Point rotatedConnection = rotatePoint(new Point(connection[0], connection[1]), centreOfRotation, angle);
			Point rotatedConnection1 = rotatePoint(new Point(connection[2], connection[3]), centreOfRotation, angle);
			Point rotatedConnection2 = rotatePoint(new Point(connection[4], connection[5]), centreOfRotation, angle);
			rotatedConnections.add(new int[]{rotatedConnection.x, rotatedConnection.y, rotatedConnection1.x, rotatedConnection1.y, rotatedConnection2.x, rotatedConnection2.y});
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
		
		Point origin = new Point(0,0);
		
		for(int theta = 0; theta < 4; theta++) {
		
			Area polygon1 = new Area();
			Area polygon2 = new Area();
			
			int deltaX = (int) mergedPoly1.getBounds2D().getMinX();
			int deltaY = (int) mergedPoly1.getBounds2D().getMinY();
			
			for(int i = 0; i < polygons1.size(); i++) {
				Polygon nPoly = new Polygon(polygons1.get(i).xpoints, polygons1.get(i).ypoints, polygons1.get(i).npoints);
				nPoly.translate(-deltaX, -deltaY);
				nPoly = rotatePoly(nPoly,(Math.PI / 2) * theta,origin);
				Area area = new Area(nPoly);
				polygon1.add(area);
			}
			
			deltaX = (int) mergedPoly2.getBounds2D().getMinX();
			deltaY = (int) mergedPoly2.getBounds2D().getMinY();
			
			for(int i = 0; i < polygons2.size(); i++) {
				Polygon nPoly = new Polygon(polygons2.get(i).xpoints, polygons2.get(i).ypoints, polygons2.get(i).npoints);
				nPoly.translate(-deltaX, -deltaY);
				nPoly = rotatePoly(nPoly,(Math.PI / 2) * theta,origin);
				Area area = new Area(nPoly);
				polygon2.add(area);
			}
			
			if(polygon1.equals(polygon2))
				return true;
		
		}
			
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
	
}
