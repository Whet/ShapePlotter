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
				for(int k = 0; k < monomer.getConnectionPointsLocations().size(); k++) {

					// Only matching flavours can bind
					if(lastLevelPolygon.getConnectionPoints().get(j).getFlavour() != monomer.getConnectionPoints().get(k).getFlavour()) {
						continue;
					}
					
					MultiPoly newMonomer = new MultiPoly(monomer.getConnectionPoints(), monomer.getPolygon());
					
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
						nextGeneration.add(new MultiPoly(lastLevelPolygon.getCode(), lastLevelPolygon.getCode() + nextGeneration.size(), lastLevelPolygon.getConnectionPoints(), newMonomer.getConnectionPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
						
						newMonomer.rotate(centreBind, -angle * 2);
						if(!intersects(lastLevelPolygon, newMonomer)) {
							nextGeneration.add(new MultiPoly(lastLevelPolygon.getCode(), lastLevelPolygon.getCode() + nextGeneration.size(), lastLevelPolygon.getConnectionPoints(), newMonomer.getConnectionPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
						}
					}
					else {
						// Try spinning other way
						newMonomer.rotate(centreBind, -angle * 2);
						if(!intersects(lastLevelPolygon, newMonomer)) {
							nextGeneration.add(new MultiPoly(lastLevelPolygon.getCode(), lastLevelPolygon.getCode() + nextGeneration.size(), lastLevelPolygon.getConnectionPoints(), newMonomer.getConnectionPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
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
<<<<<<< HEAD
				shapes.add(new MultiPoly(monomer.getConnectPoints(), new ArrayList<Integer>(), new Polygon(monomer.getPolygon().xpoints, monomer.getPolygon().ypoints, monomer.getPolygon().npoints)));
=======
				shapes.add(new MultiPoly(monomer.getConnectionPoints(), new Polygon(monomer.getPolygon().xpoints, monomer.getPolygon().ypoints, monomer.getPolygon().npoints)));
>>>>>>> origin/shapematching
				continue;
			}

			for(int y = 0; y < hierarchy.get(currentDepth - 1).size(); y++) {
			
				MultiPoly lastLevelPolygon = null;
				try {
					lastLevelPolygon = (MultiPoly) hierarchy.get(currentDepth - 1).get(y).clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				
				List<ConnectionPoint> connectPoints = lastLevelPolygon.getConnectPoints();
				
				/*
				 * For each binding point match the centre location [0][1]
				 * Make the inside of one line up with the outside of the other [2][3]/[4][5]
				 * Check if polygons overlap
				 */
				
				for(int j = 0; j < connectPoints.size(); j++) {
					for(int k = 0; k < monomer.getConnectionPointsLocations().size(); k++) {

<<<<<<< HEAD
						MultiPoly newMonomer = new MultiPoly(monomer.getConnectPoints(), new ArrayList<Integer>(), monomer.getPolygon());
						newMonomer.addConnection(monomer.getConnectPoints().get(k).getIdentifier());
=======
						// Only matching flavours can bind
						if(lastLevelPolygon.getConnectionPoints().get(j).getFlavour() != monomer.getConnectionPoints().get(k).getFlavour())
							continue;
						
						MultiPoly newMonomer = new MultiPoly(monomer.getConnectionPoints(), monomer.getPolygon());
>>>>>>> origin/shapematching
						
						Point centreBind = new Point(connectPoints.get(j).getLocation().x, connectPoints.get(j).getLocation().y); 
					
						int translateX = centreBind.x - connectPoints.get(k).getLocation().x;
						int translateY = centreBind.y - connectPoints.get(k).getLocation().y;
						
						// Move centre to centre
						newMonomer.translate(translateX, translateY);
						
						// find angle difference between inside and outside
						double angle = Maths.getRads(connectPoints.get(j).getInnie().x, connectPoints.get(j).getInnie().y,
													 newMonomer.getConnectPoints().get(k).getOuttie().x, newMonomer.getConnectPoints().get(k).getOuttie().x) * 2;
						
						if(angle == 0 && translateX == 0 && translateY == 0)
							angle = Math.PI;
						
						// TEMP: Round angle to nearest PI/2
						angle = Maths.round(angle, Math.PI / 2);
						
						// Rotate the polygon around the centre
						newMonomer.rotate(centreBind, angle);
						
						// Check if rotatedPoly intersects with poly1
						if(!intersects(lastLevelPolygon, newMonomer)) {
<<<<<<< HEAD
							MultiPoly multiPoly = new MultiPoly(lastLevelPolygon.getConnectPoints(), newMonomer.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons(), lastLevelPolygon.getUsedConnections(), newMonomer.getUsedConnections());
							hierarchy.get(currentDepth).add(multiPoly);
							multiPoly.addConnection(connectPoints.get(j).getIdentifier());
							
							newMonomer.rotate(centreBind, -angle * 2);
							if(!intersects(lastLevelPolygon, newMonomer)) {
								MultiPoly multiPoly2 = new MultiPoly(lastLevelPolygon.getConnectPoints(), newMonomer.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons(), lastLevelPolygon.getUsedConnections(), newMonomer.getUsedConnections());
								hierarchy.get(currentDepth).add(multiPoly2);
								multiPoly2.addConnection(connectPoints.get(j).getIdentifier());
=======
							hierarchy.get(currentDepth).add(new MultiPoly(lastLevelPolygon.getCode(), lastLevelPolygon.getCode() + hierarchy.get(currentDepth).size(), lastLevelPolygon.getConnectionPoints(), newMonomer.getConnectionPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
							
							newMonomer.rotate(centreBind, -angle * 2);
							if(!intersects(lastLevelPolygon, newMonomer)) {
								hierarchy.get(currentDepth).add(new MultiPoly(lastLevelPolygon.getCode(), lastLevelPolygon.getCode() + hierarchy.get(currentDepth).size(), lastLevelPolygon.getConnectionPoints(), newMonomer.getConnectionPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
>>>>>>> origin/shapematching
							}
						}
						else {
							// Try spinning other way
							newMonomer.rotate(centreBind, -angle * 2);
							if(!intersects(lastLevelPolygon, newMonomer)) {
<<<<<<< HEAD
								MultiPoly multiPoly = new MultiPoly(lastLevelPolygon.getConnectPoints(), newMonomer.getConnectPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons(), lastLevelPolygon.getUsedConnections(), newMonomer.getUsedConnections());
								hierarchy.get(currentDepth).add(multiPoly);
								multiPoly.addConnection(connectPoints.get(j).getIdentifier());
=======
								hierarchy.get(currentDepth).add(new MultiPoly(lastLevelPolygon.getCode(), lastLevelPolygon.getCode() + hierarchy.get(currentDepth).size(), lastLevelPolygon.getConnectionPoints(), newMonomer.getConnectionPoints(), newMonomer.getPolygons(), lastLevelPolygon.getPolygons()));
>>>>>>> origin/shapematching
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
				
				System.out.println("Points");
				
				for(int i = 0; i < poly.getUsedConnections().size(); i++) {
					System.out.println(poly.getUsedConnections().get(i));
				}
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
