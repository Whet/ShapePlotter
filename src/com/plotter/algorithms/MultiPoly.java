package com.plotter.algorithms;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import com.plotter.data.Connection;
import com.plotter.data.Maths;
import com.plotter.gui.GridPanel;

public class MultiPoly {

	private List<Connection> connectedPoints;
	private List<Polygon> polygons;
	private Polygon mergedPolygon;
	private LineMergePolygon lineMergedPolygon;
	private String parent;
	private String code;
	
	public MultiPoly(List<Connection> connectedPoints, Polygon... polygons) {
		
		this.parent = "";
		this.code = "0";
		
		this.polygons = new ArrayList<Polygon>();
		
		for(int i = 0; i < polygons.length; i++) {
			this.polygons.add(new Polygon(polygons[i].xpoints, polygons[i].ypoints, polygons[i].npoints));
		}
		
		this.connectedPoints = new ArrayList<>();
		
		for(int i = 0; i < connectedPoints.size(); i++) {
			Connection connection = connectedPoints.get(i);
			this.connectedPoints.add(new Connection(connection.getFlavour(), connection.getCentre().x, connection.getCentre().y, connection.getOutside().x, connection.getOutside().y, connection.getInside().x, connection.getInside().y));
		}
		
		this.lineMergedPolygon = new LineMergePolygon();
		ArrayList<Point> points = new ArrayList<>();
		Area mergedArea = new Area();
		
		for(Polygon polygon:this.polygons) {
			for(int i = 0; i < polygon.npoints; i++) {
				points.add(new Point(polygon.xpoints[i], polygon.ypoints[i]));
			}
			this.lineMergedPolygon.addPolygon(polygon);
			mergedArea.add(new Area(polygon));
		}
		
		createMergedPolygon(mergedArea.getPathIterator(new AffineTransform()));
		
	}
	
	public MultiPoly(String parent, String code, List<Connection> connectedPoints, List<Connection> connectedPoints1, List<Polygon> polygons1, List<Polygon> polygons2) {
		
		this.parent = parent;
		this.code = code;
		
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
			
			this.connectedPoints.add(new Connection(connectedPoints.get(i).getFlavour(), connectedPoints.get(i).getCentre().x, connectedPoints.get(i).getCentre().y, end1.x, end1.y, end2.x, end2.y));
		}
		for(int i = 0; i < connectedPoints1.size(); i++) {
			// if both out and in ends are inside of a polygon then don't add the connect point as it is in use
			Point end1 = new Point(connectedPoints1.get(i).getOutside().x, connectedPoints1.get(i).getOutside().y);
			Point end2 = new Point(connectedPoints1.get(i).getInside().x, connectedPoints1.get(i).getInside().y);
			
				this.connectedPoints.add(new Connection(connectedPoints1.get(i).getFlavour(), connectedPoints1.get(i).getCentre().x, connectedPoints1.get(i).getCentre().y, end1.x, end1.y, end2.x, end2.y));
		}
		
		this.lineMergedPolygon = new LineMergePolygon();
		ArrayList<Point> points = new ArrayList<>();
		Area mergedArea = new Area();
		
		for(Polygon polygon:this.polygons) {
			for(int i = 0; i < polygon.npoints; i++) {
				points.add(new Point(polygon.xpoints[i], polygon.ypoints[i]));
			}
			this.lineMergedPolygon.addPolygon(polygon);
			mergedArea.add(new Area(polygon));
		}
		
		createMergedPolygon(mergedArea.getPathIterator(new AffineTransform()));
	}
	
	private void createMergedPolygon(PathIterator p_path) {
		this.mergedPolygon = new Polygon();
		double[] point = new double[2];
		if(p_path.currentSegment(point) != PathIterator.SEG_CLOSE)
			this.mergedPolygon.addPoint((int) point[0], (int) point[1]);
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
		
		MultiPoly multiPoly = new MultiPoly(connectedPoints, polygons);
		
		multiPoly.code = code;
		multiPoly.parent = parent;
		
		return multiPoly;
	}

	public List<Polygon> getPolygons() {
		return this.polygons;
	}

	public Polygon getMergedPolygon() {
		return mergedPolygon;
	}
	
	public LineMergePolygon getMergedLines() {
		return this.lineMergedPolygon;
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
		this.lineMergedPolygon.translate(deltaX, deltaY);
		
		ArrayList<Connection> translatedConnections = new ArrayList<>();
		
		for(Connection connection:this.connectedPoints) {
			Connection tC = connection.clone();
			tC.translate(deltaX, deltaY);
			translatedConnections.add(tC);
		}
		
		this.connectedPoints = translatedConnections;
	}
	
	public MultiPoly getRotatedMultipoly(int theta) {
		
		int deltaX = 9999999;
		int deltaY = 9999999;
		
		List<Polygon> rotatedPolys = new ArrayList<>();
		
		for(int i = 0; i < this.polygons.size(); i++) {
			for(int j = 0; j < this.polygons.get(i).npoints; j++) {
				if(this.polygons.get(i).xpoints[j] < deltaX)
					deltaX = this.polygons.get(i).xpoints[j];
				if(this.polygons.get(i).ypoints[j] < deltaY)
					deltaY = this.polygons.get(i).ypoints[j];
			}
		}
		
		for(int i = 0; i < this.polygons.size(); i++) {
			Polygon nPoly = new Polygon(this.polygons.get(i).xpoints, this.polygons.get(i).ypoints, this.polygons.get(i).npoints);
			nPoly = rotatePoly(nPoly, theta * (Math.PI / 2), new Point(deltaX, deltaY));
			rotatedPolys.add(nPoly);
		}
		
		deltaX = 9999999;
		deltaY = 9999999;
		
		for(int i = 0; i < rotatedPolys.size(); i++) {
			for(int j = 0; j < rotatedPolys.get(i).npoints; j++) {
				if(rotatedPolys.get(i).xpoints[j] < deltaX)
					deltaX = rotatedPolys.get(i).xpoints[j];
				if(rotatedPolys.get(i).ypoints[j] < deltaY)
					deltaY = rotatedPolys.get(i).ypoints[j];
			}
		}
		
		for(int i = 0; i < rotatedPolys.size(); i++) {
			rotatedPolys.get(i).translate(-deltaX, -deltaY);
		}
		
		MultiPoly mPoly = new MultiPoly(new ArrayList<Connection>(), rotatedPolys.toArray(new Polygon[rotatedPolys.size()]));
		
		mPoly.polygons = rotatedPolys;
		
		return mPoly;
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
		
		try {
			this.lineMergedPolygon = (LineMergePolygon) this.lineMergedPolygon.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		this.lineMergedPolygon.rotate(centreOfRotation, angle);
	}
	
	// http://stackoverflow.com/questions/10533403/how-to-rotate-a-polygon-around-a-point-with-java
	public static Point rotatePoint(Point pt, Point center, double angle) {
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
			
			if(polyMatch(this.getPolygons(), ((MultiPoly) obj).getPolygons()))
				return true;
			
			return false;
		}
		
		return super.equals(obj);
	}

	private boolean polyMatch(List<Polygon> polygons1, List<Polygon> polygons2) {
		
		Area polygon1 = new Area();
		Area polygon2 = null;
		
		List<Polygon> poly1jg = new ArrayList<>();
		
		int deltaX = 9999999;
		int deltaY = 9999999;
		
		for(int i = 0; i < polygons1.size(); i++) {
			for(int j = 0; j < polygons1.get(i).npoints; j++) {
				if(polygons1.get(i).xpoints[j] < deltaX)
					deltaX = polygons1.get(i).xpoints[j];
				if(polygons1.get(i).ypoints[j] < deltaY)
					deltaY = polygons1.get(i).ypoints[j];
			}
		}
		
		for(int i = 0; i < polygons1.size(); i++) {
			Polygon nPoly = new Polygon(polygons1.get(i).xpoints, polygons1.get(i).ypoints, polygons1.get(i).npoints);
			nPoly.translate(-deltaX, -deltaY);
			Area area = new Area(nPoly);
			polygon1.add(area);
			poly1jg.add(nPoly);
		}
		
		for(int theta = 0; theta < 4; theta++) {
		
			polygon2 = new Area();
			
//			Polygon rotatedMerge = rotatePoly(mergedPoly2, theta * (Math.PI / 2), new Point((int)mergedPoly2.getBounds2D().getCenterX(), (int)mergedPoly2.getBounds2D().getCenterY()));
//			Polygon rotatedMerge = mergedPoly2;
			
			
			deltaX = 9999999;
			deltaY = 9999999;
			
			List<Polygon> rotatedPolys = new ArrayList<>();
			
			for(int i = 0; i < polygons2.size(); i++) {
				for(int j = 0; j < polygons2.get(i).npoints; j++) {
					if(polygons2.get(i).xpoints[j] < deltaX)
						deltaX = polygons2.get(i).xpoints[j];
					if(polygons2.get(i).ypoints[j] < deltaY)
						deltaY = polygons2.get(i).ypoints[j];
				}
			}
			
			for(int i = 0; i < polygons2.size(); i++) {
				Polygon nPoly = new Polygon(polygons2.get(i).xpoints, polygons2.get(i).ypoints, polygons2.get(i).npoints);
				nPoly = rotatePoly(nPoly, theta * (Math.PI / 2), new Point(deltaX, deltaY));
				rotatedPolys.add(nPoly);
			}
			
			deltaX = 9999999;
			deltaY = 9999999;
			
			for(int i = 0; i < rotatedPolys.size(); i++) {
				for(int j = 0; j < rotatedPolys.get(i).npoints; j++) {
					if(rotatedPolys.get(i).xpoints[j] < deltaX)
						deltaX = rotatedPolys.get(i).xpoints[j];
					if(rotatedPolys.get(i).ypoints[j] < deltaY)
						deltaY = rotatedPolys.get(i).ypoints[j];
				}
			}
			
			for(int i = 0; i < rotatedPolys.size(); i++) {
				rotatedPolys.get(i).translate(-deltaX, -deltaY);
				Area area = new Area(rotatedPolys.get(i));
				polygon2.add(area);
			}
			
			polygon2.exclusiveOr(polygon1);
			
			if(polygon2.getBounds2D().getWidth() < 100 && polygon2.getBounds2D().getHeight() < 100)
				return true;
			
		
//			System.out.println();
//			System.out.println("POLY1");
//			for(int i = 0; i < poly1jg.size(); i++) {
//				System.out.print("X: ");
//				
//				for(int j = 0; j < poly1jg.get(i).xpoints.length; j++) {
//					System.out.print(poly1jg.get(i).xpoints[j] + ", ");
//				}
//				
//				System.out.println();
//				System.out.print("Y: ");
//				
//				for(int j = 0; j < poly1jg.get(i).ypoints.length; j++) {
//					System.out.print(poly1jg.get(i).ypoints[j] + ", ");
//				}
//				System.out.println();
//			}
//			
//			System.out.println();
//			System.out.println("POLY2");
//			for(int i = 0; i < rotatedPolys.size(); i++) {
//				System.out.print("X: ");
//				
//				for(int j = 0; j < rotatedPolys.get(i).xpoints.length; j++) {
//					System.out.print(rotatedPolys.get(i).xpoints[j] + ", ");
//				}
//				
//				System.out.println();
//				System.out.print("Y: ");
//				
//				for(int j = 0; j < rotatedPolys.get(i).ypoints.length; j++) {
//					System.out.print(rotatedPolys.get(i).ypoints[j] + ", ");
//				}
//				System.out.println();
//			}
			
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

	public String getParent() {
		return parent;
	}
	
	public String getCode() {
		return code;
	}

	public List<Connection> getConnectionPoints() {
		return this.connectedPoints;
	}
	
}
