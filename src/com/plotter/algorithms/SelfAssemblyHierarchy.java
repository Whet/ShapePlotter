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

	public static Map<Integer, List<MultiPoly>> getHierarchy(int maxDepth, ModulePolygon monomer) {
		
		Map<Integer, List<MultiPoly>> hierarchy = new HashMap<Integer, List<MultiPoly>>();
		
		for(int currentDepth = 0; currentDepth < maxDepth; currentDepth++) {
			ArrayList<MultiPoly> shapes = new ArrayList<MultiPoly>();
			hierarchy.put(currentDepth, shapes);
			
			if(currentDepth == 0) {
				shapes.add(new MultiPoly(monomer.getConnectPoints(), new Polygon(monomer.getPolygon().xpoints, monomer.getPolygon().ypoints, monomer.getPolygon().npoints)));
				continue;
			}

			for(int y = 0; y < hierarchy.get(currentDepth - 1).size(); y++) {
			
				MultiPoly lastLevelPolygon = null;
				try {
					lastLevelPolygon = (MultiPoly) hierarchy.get(currentDepth - 1).get(y).clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				
				List<int[]> connectPoints = lastLevelPolygon.getConnectPoints();
				
				/*
				 * For each binding point match the centre location [0][1]
				 * Make the inside of one line up with the outside of the other [2][3]/[4][5]
				 * Check if polygons overlap
				 */
				
				for(int j = 0; j < connectPoints.size(); j++) {
					for(int k = 0; k < connectPoints.size(); k++) {

						MultiPoly newMonomer = new MultiPoly(monomer.getConnectPoints(), monomer.getPolygon());
						
						Point centreBind = new Point(connectPoints.get(j)[0], connectPoints.get(j)[1]); 
					
						int translateX = centreBind.x - connectPoints.get(k)[0];
						int translateY = centreBind.y - connectPoints.get(k)[1];
						System.out.println("X: " + translateX + " Y: " + translateY);
						
						// Move centre to centre
						newMonomer.translate(translateX, translateY);
						
						// find angle difference between inside and outside
						double angle = Maths.getRads(connectPoints.get(j)[4], connectPoints.get(j)[5], newMonomer.getConnectPoints().get(k)[2], newMonomer.getConnectPoints().get(k)[3]) * 2;
						
						if(angle == 0 && translateX == 0 && translateY == 0)
							angle = Math.PI;
						
						System.out.println("ANGLE: " + Math.toDegrees(angle));
						
						// Rotate the polygon around the centre
						newMonomer.rotate(centreBind, angle);
						
						// Check if rotatedPoly intersects with poly1
						if(!intersects(lastLevelPolygon, newMonomer)) {
							System.out.println("ACCEPTED");
							hierarchy.get(currentDepth).add(new MultiPoly(lastLevelPolygon.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
						}
						else {
							// Try spinning other way
							newMonomer.rotate(centreBind, -angle * 2);
							if(!intersects(lastLevelPolygon, newMonomer)) {
								System.out.println("ACCEPTED");
								hierarchy.get(currentDepth).add(new MultiPoly(lastLevelPolygon.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
							}
							else {
								System.out.println("REJECTED");
							}
						}
					}
				}
			}
			
			// Remove duplicates
			List<MultiPoly> list = hierarchy.get(currentDepth);
			List<MultiPoly> undupedList = new ArrayList<>();
			
	LOOP:	for(MultiPoly poly:list) {
				for(MultiPoly poly1:undupedList) {
					if(poly.equals(poly1)) {
						continue LOOP;
					}
				}
				
				undupedList.add(poly);
			}
			
			hierarchy.remove(currentDepth);
			hierarchy.put(currentDepth, undupedList);
			
		}
		
		return hierarchy;
		
	}
	
	// http://wikicode.wikidot.com/check-for-polygon-polygon-intersection
	private static boolean intersects(MultiPoly mPoly1, MultiPoly mPoly2) {
		
		for(Polygon shape1:mPoly1.getPolygons()) {
			for(Polygon shape2:mPoly2.getPolygons()) {
			
				if(shape1.contains(shape2.getBounds2D().getCenterX(), shape2.getBounds2D().getCenterY()) || shape2.contains(shape1.getBounds2D().getCenterX(), shape1.getBounds2D().getCenterY()))
					return true;
				
//				Point p;
//		    LOOP:for(int i = 0; i < shape1.npoints;i++) {
//		            p = new Point(shape1.xpoints[i],shape1.ypoints[i]);
//		            if(shape2.contains(p)) {
//		            	
//		            	for(int j = 0; j < shape2.npoints;j++) {
//		            		if(p.x == shape2.xpoints[j] && p.y == shape2.ypoints[j])
//		            			continue LOOP;
//		            	}
//		            	
//		                return true;
//		            }
//		        }
//		    LOOP:for(int i = 0; i < shape2.npoints;i++) {
//		            p = new Point(shape2.xpoints[i],shape2.ypoints[i]);
//		            if(shape1.contains(p)) {
//		            	
//		            	for(int j = 0; j < shape1.npoints;j++) {
//		            		if(p.x == shape1.xpoints[j] && p.y == shape1.ypoints[j])
//		            			continue LOOP;
//		            	}
//		            	
//		                return true;
//		            }
//		        }
			}
		}
		
        return false;
	}
	
	
}
