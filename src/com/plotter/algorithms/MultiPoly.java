package com.plotter.algorithms;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.plotter.data.Connection;
import com.plotter.data.Maths;
import com.plotter.gui.GridPanel;

public class MultiPoly {

<<<<<<< HEAD
	private List<ConnectionPoint> connectedPoints;
	private List<Polygon> polygons;
	private Polygon mergedPolygon;
	private List<Integer> usedConnections;
	
	public MultiPoly(List<ConnectionPoint> connectedPoints, List<Integer> usedConnections, Polygon... polygons) {
		
		this.usedConnections = usedConnections;
=======
	private List<Connection> connectedPoints;
	private List<Polygon> polygons;
	private Polygon convexMergedPolygon;
	private LineMergePolygon lineMergedPolygon;
	private String parent;
	private String code;
	
	public MultiPoly(List<Connection> connectedPoints, Polygon... polygons) {
		
		this.parent = "";
		this.code = "0";
>>>>>>> origin/shapematching
		
		this.polygons = new ArrayList<Polygon>();
		
		for(int i = 0; i < polygons.length; i++) {
			this.polygons.add(new Polygon(polygons[i].xpoints, polygons[i].ypoints, polygons[i].npoints));
		}
		
		this.connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < connectedPoints.size(); i++) {
<<<<<<< HEAD
			this.connectedPoints.add(new ConnectionPoint(connectedPoints.get(i).getLocation().x, connectedPoints.get(i).getLocation().y, connectedPoints.get(i).getInnie().x, connectedPoints.get(i).getInnie().y, connectedPoints.get(i).getOuttie().x, connectedPoints.get(i).getOuttie().y, 0, 0));
=======
			Connection connection = connectedPoints.get(i);
			this.connectedPoints.add(new Connection(connection.getFlavour(), connection.getCentre().x, connection.getCentre().y, connection.getOutside().x, connection.getOutside().y, connection.getInside().x, connection.getInside().y));
>>>>>>> origin/shapematching
		}
		
		this.lineMergedPolygon = new LineMergePolygon();
		ArrayList<Point> points = new ArrayList<>();
		
		for(Polygon polygon:this.polygons) {
			for(int i = 0; i < polygon.npoints; i++) {
				points.add(new Point(polygon.xpoints[i], polygon.ypoints[i]));
			}
			this.lineMergedPolygon.addPolygon(polygon);
		}
		
		ArrayList<Point> mergedPoints = FastConvexHull.execute(points);
		
		this.convexMergedPolygon = new Polygon();
		
		for(Point point: mergedPoints) {
			this.convexMergedPolygon.addPoint(point.x, point.y);
		}
	}
	
<<<<<<< HEAD
	public MultiPoly(List<ConnectionPoint> connectedPoints, List<ConnectionPoint> connectedPoints1, List<Polygon> polygons1, List<Polygon> polygons2, List<Integer> usedConnections1, List<Integer> usedConnections2) {
=======
	public MultiPoly(String parent, String code, List<Connection> connectedPoints, List<Connection> connectedPoints1, List<Polygon> polygons1, List<Polygon> polygons2) {
		
		this.parent = parent;
		this.code = code;
>>>>>>> origin/shapematching
		
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
<<<<<<< HEAD
			Point end1 = new Point(connectedPoints.get(i).getInnie().x, connectedPoints.get(i).getInnie().y);
			Point end2 = new Point(connectedPoints.get(i).getOuttie().x, connectedPoints.get(i).getOuttie().y);
=======
			Point end1 = new Point(connectedPoints.get(i).getOutside().x, connectedPoints.get(i).getOutside().y);
			Point end2 = new Point(connectedPoints.get(i).getInside().x, connectedPoints.get(i).getInside().y);
>>>>>>> origin/shapematching
			
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
<<<<<<< HEAD
				this.connectedPoints.add(new ConnectionPoint(connectedPoints.get(i).getLocation().x, connectedPoints.get(i).getLocation().y, end1.x, end1.y, end2.x, end2.y, connectedPoints.get(i).getIdentifier(), connectedPoints.get(i).getFlavour()));
		}
		for(int i = 0; i < connectedPoints1.size(); i++) {
			// if both out and in ends are inside of a polygon then don't add the connect point as it is in use
			Point end1 = new Point(connectedPoints1.get(i).getInnie().x, connectedPoints1.get(i).getInnie().y);
			Point end2 = new Point(connectedPoints1.get(i).getOuttie().x, connectedPoints1.get(i).getOuttie().y);
=======
				this.connectedPoints.add(new Connection(connectedPoints.get(i).getFlavour(), connectedPoints.get(i).getCentre().x, connectedPoints.get(i).getCentre().y, end1.x, end1.y, end2.x, end2.y));
		}
		for(int i = 0; i < connectedPoints1.size(); i++) {
			// if both out and in ends are inside of a polygon then don't add the connect point as it is in use
			Point end1 = new Point(connectedPoints1.get(i).getOutside().x, connectedPoints1.get(i).getOutside().y);
			Point end2 = new Point(connectedPoints1.get(i).getInside().x, connectedPoints1.get(i).getInside().y);
>>>>>>> origin/shapematching
			
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
<<<<<<< HEAD
				this.connectedPoints.add(new ConnectionPoint(connectedPoints1.get(i).getLocation().x, connectedPoints1.get(i).getLocation().y, end1.x, end1.y, end2.x, end2.y, connectedPoints1.get(i).getIdentifier(), connectedPoints1.get(i).getFlavour()));
		}
		
		
		this.usedConnections = new ArrayList<>();
		
		for(Integer connection:usedConnections1) {
			this.usedConnections.add(connection);
		}
		for(Integer connection:usedConnections2) {
			this.usedConnections.add(connection);
=======
				this.connectedPoints.add(new Connection(connectedPoints1.get(i).getFlavour(), connectedPoints1.get(i).getCentre().x, connectedPoints1.get(i).getCentre().y, end1.x, end1.y, end2.x, end2.y));
>>>>>>> origin/shapematching
		}
		
		this.lineMergedPolygon = new LineMergePolygon();
		ArrayList<Point> points = new ArrayList<>();
		
		for(Polygon polygon:this.polygons) {
			for(int i = 0; i < polygon.npoints; i++) {
				points.add(new Point(polygon.xpoints[i], polygon.ypoints[i]));
			}
			this.lineMergedPolygon.addPolygon(polygon);
		}
		
		ArrayList<Point> mergedPoints = FastConvexHull.execute(points);
		
		this.convexMergedPolygon = new Polygon();
		
		for(Point point: mergedPoints) {
			this.convexMergedPolygon.addPoint(point.x, point.y);
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		
		Polygon[] polygons = new Polygon[this.polygons.size()];
		
		for(int i = 0; i < this.polygons.size(); i++) {
			polygons[i] = new Polygon(this.polygons.get(i).xpoints, this.polygons.get(i).ypoints, this.polygons.get(i).npoints);
		}
		
<<<<<<< HEAD
		ArrayList<ConnectionPoint> connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < this.connectedPoints.size(); i++) {
			connectedPoints.add(new ConnectionPoint(this.connectedPoints.get(i).getLocation().x, this.connectedPoints.get(i).getLocation().y, this.connectedPoints.get(i).getInnie().x, this.connectedPoints.get(i).getInnie().y, this.connectedPoints.get(i).getOuttie().x, this.connectedPoints.get(i).getOuttie().y, 0, 0));
		}
		
		ArrayList<Integer> usedConnections = new ArrayList<>();
		
		for(Integer intV:this.usedConnections) {
			usedConnections.add(new Integer(intV));
		}
		
		return new MultiPoly(connectedPoints, usedConnections, polygons);
=======
		ArrayList<Connection> connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < this.connectedPoints.size(); i++) {
			Connection connection = this.connectedPoints.get(i);
			connectedPoints.add(new Connection(connection.getFlavour(), connection.getCentre().x, connection.getCentre().y, connection.getOutside().x, connection.getOutside().y, connection.getInside().x, connection.getInside().y));
		}
		
		MultiPoly multiPoly = new MultiPoly(connectedPoints, polygons);
		
		multiPoly.code = code;
		multiPoly.parent = parent;
		
		return multiPoly;
>>>>>>> origin/shapematching
	}

	public List<Polygon> getPolygons() {
		return this.polygons;
	}

	public Polygon getMergedPolygon() {
		return convexMergedPolygon;
	}
	
	public LineMergePolygon getMergedLines() {
		return this.lineMergedPolygon;
	}

<<<<<<< HEAD
	public List<ConnectionPoint> getConnectPoints() {
		return this.connectedPoints;
=======
	public List<int[]> getConnectPoints() {
		
		List<int[]> connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < this.connectedPoints.size(); i++) {
			Connection connection = this.connectedPoints.get(i);
			connectedPoints.add(new int[]{connection.getCentre().x, connection.getCentre().y, connection.getOutside().x, connection.getOutside().y, connection.getInside().x, connection.getInside().y});
		}
		
		return connectedPoints;
>>>>>>> origin/shapematching
	}

	public void translate(int deltaX, int deltaY) {
		for(int i = 0; i < this.polygons.size(); i++) {
			this.polygons.get(i).translate(deltaX, deltaY);
		}
		this.convexMergedPolygon.translate(deltaX, deltaY);
		this.lineMergedPolygon.translate(deltaX, deltaY);
		
<<<<<<< HEAD
		for(ConnectionPoint connection:this.connectedPoints) {
			connection.translate(deltaX, deltaY);
=======
		ArrayList<Connection> translatedConnections = new ArrayList<>();
		
		for(Connection connection:this.connectedPoints) {
			Connection tC = connection.clone();
			tC.translate(deltaX, deltaY);
			translatedConnections.add(tC);
>>>>>>> origin/shapematching
		}
	}

	public void rotate(Point centreOfRotation, double angle) {
		ArrayList<Polygon> rotatedPolygons = new ArrayList<>();
		for(Polygon polygon: this.polygons) {
			rotatedPolygons.add(rotatePoly(polygon,angle,centreOfRotation));
		}
		
		this.polygons = rotatedPolygons;
		
		Polygon rotatedMergedPolygon = new Polygon();
		
		for(int i = 0; i < this.convexMergedPolygon.npoints; i++) {
			Point rotatedPoint = rotatePoint(new Point(this.convexMergedPolygon.xpoints[i], this.convexMergedPolygon.ypoints[i]), centreOfRotation, angle);
			
			rotatedMergedPolygon.addPoint(Maths.round(rotatedPoint.x, GridPanel.GRID_SIZE), Maths.round(rotatedPoint.y, GridPanel.GRID_SIZE));
		}
		
		this.convexMergedPolygon = rotatedMergedPolygon;
		
<<<<<<< HEAD
		for(ConnectionPoint connection:this.connectedPoints) {
			connection.rotate(centreOfRotation, angle);
		}
		
=======
		ArrayList<Connection> rotatedConnections = new ArrayList<>();
		
		for(Connection connection:this.connectedPoints) {
			Point rotatedConnection = rotatePoint(new Point(connection.getCentre().x, connection.getCentre().y), centreOfRotation, angle);
			Point rotatedConnection1 = rotatePoint(new Point(connection.getOutside().x, connection.getOutside().y), centreOfRotation, angle);
			Point rotatedConnection2 = rotatePoint(new Point(connection.getInside().x, connection.getInside().y), centreOfRotation, angle);
			rotatedConnections.add(new Connection(connection.getFlavour(), rotatedConnection.x, rotatedConnection.y, rotatedConnection1.x, rotatedConnection1.y, rotatedConnection2.x, rotatedConnection2.y));
		}
		
		this.connectedPoints = rotatedConnections;
		
		try {
			this.lineMergedPolygon = (LineMergePolygon) this.lineMergedPolygon.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		this.lineMergedPolygon.rotate(centreOfRotation, angle);
>>>>>>> origin/shapematching
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
			
			if(connectionsMatch((MultiPoly) obj))
				return true;
			
			return false;
		}
		
		return super.equals(obj);
	}

	private boolean connectionsMatch(MultiPoly otherPoly) {
		
<<<<<<< HEAD
		List<Integer> copiedConnections = new ArrayList<>();
		
		for(Integer connection:this.usedConnections) {
			copiedConnections.add(connection);
		}
		
LOOP:	for(Integer connection:otherPoly.usedConnections) {
			Iterator<Integer> it = copiedConnections.iterator();
			
			while(it.hasNext()) {
				Integer next = it.next();
				
				if(connection.equals(next)) {
					it.remove();
					continue LOOP;
				}
			}
			
			return false;
		}
		
		copiedConnections = new ArrayList<>();
		
		for(Integer connection:otherPoly.usedConnections) {
			copiedConnections.add(connection);
		}
		
LOOP:	for(Integer connection:this.usedConnections) {
			Iterator<Integer> it = copiedConnections.iterator();
			
			while(it.hasNext()) {
				Integer next = it.next();
				
				if(connection.equals(next)) {
					it.remove();
					continue LOOP;
				}
			}
			
			return false;
		}
=======
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
>>>>>>> origin/shapematching
		
		return true;
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

	public String getParent() {
		return parent;
	}
	
	public String getCode() {
		return code;
	}

	public List<Connection> getConnectionPoints() {
		return this.connectedPoints;
	}
	
	public void addConnection(int connection) {
		this.usedConnections.add(connection);
	}

	public List<Integer> getUsedConnections() {
		return this.usedConnections;
	}
	
}
