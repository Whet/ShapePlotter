package com.plotter.algorithms;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plotter.data.Maths;
import com.plotter.data.ModulePolygon;

public class SelfAssemblyHierarchy {

	public static List<MultiPoly> getNextGeneration(ModulePolygon monomer, List<MultiPoly> currentGeneration) {
		
		List<MultiPoly> nextGeneration = new ArrayList<>();
		
		for(int y = 0; y < currentGeneration.size(); y++) {
			
			MultiPoly lastLevelPolygon = null;
			try {
				lastLevelPolygon = (MultiPoly) currentGeneration.get(y).clone();
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
				for(int k = 0; k < monomer.getConnectPoints().size(); k++) {

					MultiPoly newMonomer = new MultiPoly(monomer.getConnectPoints(), monomer.getPolygon());
					
					Point centreBind = new Point(connectPoints.get(j)[0], connectPoints.get(j)[1]); 
				
					int translateX = centreBind.x - connectPoints.get(k)[0];
					int translateY = centreBind.y - connectPoints.get(k)[1];
					
					// Move centre to centre
					newMonomer.translate(translateX, translateY);
					
					// find angle difference between inside and outside
					double angle = Maths.getRads(connectPoints.get(j)[4], connectPoints.get(j)[5],
												 newMonomer.getConnectPoints().get(k)[2], newMonomer.getConnectPoints().get(k)[3]) * 2;
					
					if(angle == 0 && translateX == 0 && translateY == 0)
						angle = Math.PI;
					
					// TEMP: Round angle to nearest PI/2
					angle = Maths.round(angle, Math.PI / 2);
					
					// Rotate the polygon around the centre
					newMonomer.rotate(centreBind, angle);
					
					// Check if rotatedPoly intersects with poly1
					if(!intersects(lastLevelPolygon, newMonomer)) {
						nextGeneration.add(new MultiPoly(lastLevelPolygon, lastLevelPolygon.getConnectPoints(), newMonomer.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
						
						newMonomer.rotate(centreBind, -angle * 2);
						if(!intersects(lastLevelPolygon, newMonomer)) {
							nextGeneration.add(new MultiPoly(lastLevelPolygon, lastLevelPolygon.getConnectPoints(), newMonomer.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
						}
					}
					else {
						// Try spinning other way
						newMonomer.rotate(centreBind, -angle * 2);
						if(!intersects(lastLevelPolygon, newMonomer)) {
							nextGeneration.add(new MultiPoly(lastLevelPolygon, lastLevelPolygon.getConnectPoints(), newMonomer.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
						}
					}
				}
			}
		}
		
		// Remove duplicates
		List<MultiPoly> undupedList = new ArrayList<>();
		
LOOP:	for(MultiPoly poly:nextGeneration) {
			for(MultiPoly poly1:undupedList) {
				if(poly1.equals(poly)) {
					continue LOOP;
				}
			}
			
			undupedList.add(poly);
		}
		
		return undupedList;
		
	}
	
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
					for(int k = 0; k < monomer.getConnectPoints().size(); k++) {

						MultiPoly newMonomer = new MultiPoly(monomer.getConnectPoints(), monomer.getPolygon());
						
						Point centreBind = new Point(connectPoints.get(j)[0], connectPoints.get(j)[1]); 
					
						int translateX = centreBind.x - connectPoints.get(k)[0];
						int translateY = centreBind.y - connectPoints.get(k)[1];
						
						// Move centre to centre
						newMonomer.translate(translateX, translateY);
						
						// find angle difference between inside and outside
						double angle = Maths.getRads(connectPoints.get(j)[4], connectPoints.get(j)[5],
													 newMonomer.getConnectPoints().get(k)[2], newMonomer.getConnectPoints().get(k)[3]) * 2;
						
						if(angle == 0 && translateX == 0 && translateY == 0)
							angle = Math.PI;
						
						// TEMP: Round angle to nearest PI/2
						angle = Maths.round(angle, Math.PI / 2);
						
						// Rotate the polygon around the centre
						newMonomer.rotate(centreBind, angle);
						
						// Check if rotatedPoly intersects with poly1
						if(!intersects(lastLevelPolygon, newMonomer)) {
							hierarchy.get(currentDepth).add(new MultiPoly(lastLevelPolygon, lastLevelPolygon.getConnectPoints(), newMonomer.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
							
							newMonomer.rotate(centreBind, -angle * 2);
							if(!intersects(lastLevelPolygon, newMonomer)) {
								hierarchy.get(currentDepth).add(new MultiPoly(lastLevelPolygon, lastLevelPolygon.getConnectPoints(), newMonomer.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
							}
						}
						else {
							// Try spinning other way
							newMonomer.rotate(centreBind, -angle * 2);
							if(!intersects(lastLevelPolygon, newMonomer)) {
								hierarchy.get(currentDepth).add(new MultiPoly(lastLevelPolygon, lastLevelPolygon.getConnectPoints(), newMonomer.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
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
					if(poly1.equals(poly)) {
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
			
			Area area1 = new Area(shape1);
			
			for(Polygon shape2:mPoly2.getPolygons()) {
				
				Area area2 = new Area(shape2);
				
				area2.intersect(area1);
				
				if(area2.getBounds2D().getWidth() > 0 && area2.getBounds2D().getHeight() > 0)
					return true;
				
			}
		}
		
        return false;
	}

}
