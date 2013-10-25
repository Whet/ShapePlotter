package com.plotter.algorithms;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plotter.data.Maths;
import com.plotter.data.ModulePolygon;

public class SelfAssemblyHierarchy {

	public static Map<Integer, List<Polygon>> getHierarchy(int maxDepth, ModulePolygon monomer) {
		
		Map<Integer, List<Polygon>> hierarchy = new HashMap<Integer, List<Polygon>>();
		
		
		List<int[]> connectPoints = monomer.getConnectPoints();
		
		for(int currentDepth = 0; currentDepth < maxDepth; currentDepth++) {
			ArrayList<Polygon> shapes = new ArrayList<>();
			hierarchy.put(currentDepth, shapes);
			
			// Clone polygons so they can be translated without affecting original
			Polygon poly1 = new Polygon(monomer.getPolygon().xpoints, monomer.getPolygon().ypoints, monomer.getPolygon().npoints);
			Polygon poly2 = new Polygon(monomer.getPolygon().xpoints, monomer.getPolygon().ypoints, monomer.getPolygon().npoints);
			
			if(currentDepth == 0) {
				shapes.add(poly1);
				continue;
			}
			

			/*
			 * For each binding point match the center location [0][1]
			 * Make the inside of one line up with the outside of the other [2][3]/[4][5]
			 * Check if polygons overlap
			 */
			
			for(int j = 0; j < connectPoints.size(); j++) {
				for(int k = 0; k < connectPoints.size(); k++) {
//					if(j != k) {
						
						int translateX = connectPoints.get(j)[0] - connectPoints.get(k)[0];
						int translateY = connectPoints.get(j)[1] - connectPoints.get(k)[1];
						
						// Update the connect points after translation so we know where they are
						int[] point = connectPoints.get(k);
						int[] translatedPoint = new int[]{point[0] + translateX, point[1] + translateY, point[2] + translateX, point[3] + translateY,point[4] + translateX, point[5] + translateY};
						
						// Move centre to centre
						poly2.translate(translateX, translateY);
						
						// find angle difference between inside and outside
						double angle = Maths.getDegrees(translatedPoint[2], translatedPoint[3], connectPoints.get(j)[4], connectPoints.get(j)[5]);
						
						Polygon rotatedPoly = new Polygon();
						
						// Rotate the polygon around the centre
						for(int m = 0; m < poly2.npoints; m++) {
							Point rotatePoint = rotatePoint(new Point(poly2.xpoints[m], poly2.ypoints[m]), new Point(connectPoints.get(j)[0], connectPoints.get(j)[1]), angle);
							rotatedPoly.addPoint(rotatePoint.x, rotatePoint.y);
						}
						
						// Check if rotatedPoly intersects with poly1
//						if(!intersects(rotatedPoly, poly1)) {
							
							hierarchy.get(currentDepth).add(combinedPolygon(rotatedPoly,poly1));
							
//						}
						
//					}
				}
			}
		}
		
		return hierarchy;
		
	}
	
	private static Polygon combinedPolygon(Polygon rotatedPoly, Polygon poly1) {
		
		Polygon combinedPolygon = new Polygon();
		
		ArrayList<Point> points = new ArrayList<>();
		
		for(int i = 0; i < rotatedPoly.npoints; i++) {
			points.add(new Point(rotatedPoly.xpoints[i], rotatedPoly.ypoints[i]));
		}
		for(int i = 0; i < poly1.npoints; i++) {
			points.add(new Point(poly1.xpoints[i], poly1.ypoints[i]));
		}
		
		ArrayList<Point> mergedPoints = FastConvexHull.execute(points);
		
		for(Point point:mergedPoints) {
			combinedPolygon.addPoint(point.x, point.y);
		}
		
		return combinedPolygon;
	}

	// http://stackoverflow.com/questions/10533403/how-to-rotate-a-polygon-around-a-point-with-java
	private static Point rotatePoint(Point pt, Point center, double angleDeg) {
	    double angleRad = (angleDeg/180)*Math.PI;
	    double cosAngle = Math.cos(angleRad );
	    double sinAngle = Math.sin(angleRad );
	    double dx = (pt.x-center.x);
	    double dy = (pt.y-center.y);

	    pt.x = center.x + (int) (dx*cosAngle-dy*sinAngle);
	    pt.y = center.y + (int) (dx*sinAngle+dy*cosAngle);
	    return pt;
	}
	
	// http://wikicode.wikidot.com/check-for-polygon-polygon-intersection
	private static boolean intersects(Polygon shape1, Polygon shape2) {
		Point p;
        for(int i = 0; i < shape1.npoints;i++) {
            p = new Point(shape1.xpoints[i],shape1.ypoints[i]);
            if(shape2.contains(p))
                return true;
        }
        for(int i = 0; i < shape2.npoints;i++) {
            p = new Point(shape2.xpoints[i],shape2.ypoints[i]);
            if(shape1.contains(p))
                return true;
        }
        return false;
	}
	
	
}
