package com.plotter.algorithms;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.plotter.algorithms.LineMergePolygon.Edge;
import com.plotter.data.Connection;
import com.plotter.data.Database;
import com.plotter.data.DatabaseMultipoly;
import com.plotter.data.Maths;
import com.plotter.data.OutputXML;

public class LibkokiUtils {
	
	private static final int ATTEMPTS = 10;
	private static final double SCALE = 1;
	
	public static void getShapes(File selectedFile, Graphics2D graphics, Database database,
            List<MarkerInfo> markers, Set<DatabaseMultipoly> allocatedShapes,
            Set<MarkerInfo> allocatedMarkers, List<ShapeData> shapeData) {
		getShapes(selectedFile, graphics, database, markers, allocatedShapes, allocatedMarkers, shapeData, new HashMap<ShapeData, DatabaseMultipoly>());
	}
	
	public static void getShapes(File selectedFile, Graphics2D graphics, Database database,
			                     List<MarkerInfo> markers, Set<DatabaseMultipoly> allocatedShapes,
			                     Set<MarkerInfo> allocatedMarkers, List<ShapeData> shapeData,
			                     Map<ShapeData, DatabaseMultipoly> shapeDataMapping) {
		
		markers.addAll(parseXML(selectedFile));
		
		MarkerInfo[] markerInfo = markers.toArray(new MarkerInfo[markers.size()]);
		
		for(int i = 0; i < ATTEMPTS; i++) {
			Iterator<MarkerInfo> markerIt = markers.iterator();
			
			while(markerIt.hasNext()) {
				MarkerInfo marker = markerIt.next();
				
				if(allocatedMarkers.contains(marker)) {
					markerIt.remove();
					continue;
				}
				
				boolean markerProcessed = processMarker(marker, graphics, database, markerInfo, allocatedShapes, allocatedMarkers, shapeData, shapeDataMapping);
				
				if(markerProcessed)
					markerIt.remove();
			}
		}
		
	}
	
	public static void showShapes(File selectedFile, Graphics2D graphics, Database database) {
		
		List<MarkerInfo> markers = parseXML(selectedFile);
		Set<DatabaseMultipoly> allocatedShapes = new HashSet<>();
		Set<MarkerInfo> allocatedMarkers = new HashSet<>();
		List<ShapeData> shapeData = new ArrayList<>();
		
		MarkerInfo[] markerInfo = markers.toArray(new MarkerInfo[markers.size()]);
		
		System.out.println("DATABASE NUMBERS");
		System.out.println(database.markersToShape.keySet());
		
		for(int i = 0; i < ATTEMPTS; i++) {
			Iterator<MarkerInfo> markerIt = markers.iterator();
			
			while(markerIt.hasNext()) {
				MarkerInfo marker = markerIt.next();
				
				if(allocatedMarkers.contains(marker)) {
					markerIt.remove();
					continue;
				}
				
				boolean markerProcessed = processMarker(marker, graphics, database, markerInfo, allocatedShapes, allocatedMarkers, shapeData, new HashMap<ShapeData, DatabaseMultipoly>());
				
				if(markerProcessed)
					markerIt.remove();
			}
		}
		
		// Output the located shapes data to XML
		try {
			OutputXML.outputXML(selectedFile.getAbsolutePath().substring(0, selectedFile.getAbsolutePath().length() - 4) + "Data.xml", markers, allocatedMarkers, shapeData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean processMarker(MarkerInfo marker, Graphics2D graphics, Database database,
										 MarkerInfo[] markerInfo, Set<DatabaseMultipoly> allocatedShapes, Set<MarkerInfo> allocatedMarkers,
										 List<ShapeData> shapeData, Map<ShapeData, DatabaseMultipoly> shapeDataMapping) {
		
		// Get possible shapes for this marker
		Set<Entry<List<Integer>, DatabaseMultipoly>> possibleShapes = database.getPossibleShapes(marker.id);
		
		// Search in the area of each possible shape to see if the required markers are found
		Iterator<Entry<List<Integer>, DatabaseMultipoly>> possibleShapesIt = possibleShapes.iterator();
			
		Map<DatabaseMultipoly, Set<MarkerInfo>> locatedMarkers = new HashMap<DatabaseMultipoly, Set<MarkerInfo>>();
		
		System.out.println("-------");
		System.out.println(possibleShapes.size() + " possible shapes for marker " + marker.id);
		
		while(possibleShapesIt.hasNext()) {
			
			Entry<List<Integer>, DatabaseMultipoly> entry = possibleShapesIt.next();
			
			if(allocatedShapes.contains(entry.getValue())) {
				possibleShapesIt.remove();
				continue;
			}
			
			System.out.println("Need markers {" + entry.getKey() + "}");
			
			// Approximation of search radius
			double radius = 0;
			double width = entry.getValue().getMergedPolygon().getBounds2D().getWidth() * SCALE;
			double height = entry.getValue().getMergedPolygon().getBounds2D().getHeight() * SCALE;
			
			radius = width + height;
			
			Set<MarkerInfo> polygonMarkersLocated = new HashSet<>();
			
			for(int i = 0; i < entry.getKey().size(); i++) {
				
				Integer markerNeighbour = entry.getKey().get(i);
				
				System.out.println("Looking for marker " + markerNeighbour);
				
				// See if marker is in radius, if not then exclude that possible shape
				for(int j = 0; j < markerInfo.length; j++) {

					// If the marker being investigated is found or another marker in the area is found that is part of the shape in entry add it
					if((markerInfo[j].equals(marker) &&
					    markerNeighbour.equals(markerInfo[j].id)) ||
					   ((markerNeighbour.equals(markerInfo[j].id) &&
					   Maths.getDistance(marker.centrePixels, markerInfo[j].centrePixels) <= radius))) {
						
						System.out.println("Found marker " + markerInfo[j].id);
						polygonMarkersLocated.add(markerInfo[j]);
					}
				}
			}
			
			System.out.println("Found " + polygonMarkersLocated.size() + " need " + entry.getKey().size());
			
			// If too many markers found then choose the marker which is closest
			if(polygonMarkersLocated.size() > entry.getKey().size()) {
				
				 Iterator<MarkerInfo> iterator = polygonMarkersLocated.iterator();
				 
				 while(iterator.hasNext()) {
					 
					 MarkerInfo invMarker = iterator.next();
					 
					 // Check against other markers to find duplicate id
					 for(MarkerInfo info:polygonMarkersLocated) {
						 if(invMarker.id == info.id && invMarker != info && Maths.getDistance(info.centrePixels, marker.centrePixels) < Maths.getDistance(invMarker.centrePixels, marker.centrePixels)) {
							 iterator.remove();
							 break;
						 }
					 }
				 }
			}
			
			// No match
			if(!(possibleShapes.size() == 1 && polygonMarkersLocated.size() > 0) && polygonMarkersLocated.size() < entry.getKey().size()) {
				possibleShapesIt.remove();
				return false;
			}
			// Found enough markers
			else {
				// Add all found markers for this shape to the located ones
				locatedMarkers.put(entry.getValue(), polygonMarkersLocated);
			}
		}
		
		DatabaseMultipoly multiPoly;
		
		// Hopefully only one possible DatabaseMultipoly is left so that is the matching shape
		if(possibleShapes.size() == 1) {
			multiPoly = possibleShapes.iterator().next().getValue();
			
			// Debug method, check shape is picked
//			DatabaseMultipoly selectShape = selectShape(possibleShapes, locatedMarkers);
			
			// Don't repeat shapes
			if(allocatedShapes.contains(multiPoly)) {
				return true;
			}
			
			allocatedShapes.add(multiPoly);
		}
		else {
			System.out.println("Couldn't make a choice!!!");
			
			multiPoly = selectShape(possibleShapes, locatedMarkers);
			
			if(multiPoly == null)
				return false;
			
			System.out.println("RECOVERED!!!");
			
			// Don't repeat shapes
			if(allocatedShapes.contains(multiPoly)) {
				return true;
			}
			
			allocatedShapes.add(multiPoly);
			
		}
		
		// Have made a choice by now, so mark all markers as allocated
		allocatedMarkers.addAll(locatedMarkers.get(multiPoly));
		
		LineMergePolygon polygonCopy = null;
		try {
			polygonCopy = (LineMergePolygon) multiPoly.getLineMergePolygon().clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		Point polygonCentre = new Point((int) multiPoly.getMergedPolygon().getBounds2D().getCenterX(), (int) multiPoly.getMergedPolygon().getBounds2D().getCenterY());
		double meanRotation = Math.toRadians(getMeanRotation(multiPoly, locatedMarkers.get(multiPoly)));
		if(Math.abs(meanRotation) > 0.00001)
			polygonCopy.rotate(polygonCentre, meanRotation);
		
		graphics.setColor(Color.red);
		graphics.setComposite(makeComposite(0.3f));
		
		int[] markerDisplacement = multiPoly.getDisplacement(marker.id);
		markerDisplacement[0] *= SCALE;
		markerDisplacement[1] *= SCALE;
		
		double[] rotDisplacement = new double[2];
		double theta = meanRotation;
		
		double cT = Math.cos(theta);
		double sT = Math.sin(theta);
		
		rotDisplacement[0] = markerDisplacement[0] * cT - markerDisplacement[1] * sT;
		rotDisplacement[1] = markerDisplacement[0] * sT + markerDisplacement[1] * cT;
		
		// Shape data Info=
		Set<ShapeData.Connection> shapeDataConnections = new HashSet<>();
		
		ArrayList<Point> verticies = new ArrayList<>();
		
		// Shape data Info
		Set<Point> shapeDataVerticies = new HashSet<>();
		
		// Draw shapes
		List<Edge> hairlines = polygonCopy.getHairlines();
		for(Edge edge:hairlines) {
			graphics.setColor(Color.orange);
			graphics.setComposite(makeComposite(0.9f));
			graphics.setStroke(new BasicStroke(3));
			graphics.drawLine((int)(marker.centrePixels[0] + rotDisplacement[0] + ((edge.end1.x - polygonCentre.x) * SCALE)),
							  (int)(marker.centrePixels[1] + rotDisplacement[1] + ((edge.end1.y - polygonCentre.y) * SCALE)),
							  (int)(marker.centrePixels[0] + rotDisplacement[0] + ((edge.end2.x - polygonCentre.x) * SCALE)),
							  (int)(marker.centrePixels[1] + rotDisplacement[1] + ((edge.end2.y - polygonCentre.y) * SCALE)));
			graphics.setColor(Color.black);
			graphics.setComposite(makeComposite(1f));
			graphics.setStroke(new BasicStroke(1));
			graphics.drawLine((int)(marker.centrePixels[0] + rotDisplacement[0] + ((edge.end1.x - polygonCentre.x) * SCALE)),
							  (int)(marker.centrePixels[1] + rotDisplacement[1] + ((edge.end1.y - polygonCentre.y) * SCALE)),
							  (int)(marker.centrePixels[0] + rotDisplacement[0] + ((edge.end2.x - polygonCentre.x) * SCALE)),
							  (int)(marker.centrePixels[1] + rotDisplacement[1] + ((edge.end2.y - polygonCentre.y) * SCALE)));
			
			Point point = new Point((int)(marker.centrePixels[0] + rotDisplacement[0] + ((edge.end1.x - polygonCentre.x) * SCALE)), 
								    (int)(marker.centrePixels[1] + rotDisplacement[1] + ((edge.end1.y - polygonCentre.y) * SCALE)));
			
			Point point2 = new Point((int)(marker.centrePixels[0] + rotDisplacement[0] + ((edge.end2.x - polygonCentre.x) * SCALE)), 
									 (int)(marker.centrePixels[1] + rotDisplacement[1] + ((edge.end2.y - polygonCentre.y) * SCALE)));
			
			shapeDataVerticies.add(point);
			shapeDataVerticies.add(point2);
			
			verticies.add(point);
			verticies.add(point2);
		}
		
		// Create hitbox of polygon to check that connections are the correct way round
//		Polygon rotMergedPolygon = new Polygon();
//		ArrayList<Point> convexPoints = FastConvexHull.execute(verticies );
//		for(Point point:convexPoints) {
//			rotMergedPolygon.addPoint(point.x, point.y);
//		}
//		Area hitbox = new Area(rotMergedPolygon);
		
		graphics.setColor(Color.red);
		
		// Draw connections
		for(Connection connection:multiPoly.getConnectionPoints()) {
			
			Point centre = MultiPoly.rotatePoint(connection.getCentre(), polygonCentre, meanRotation);
			Point inside = MultiPoly.rotatePoint(connection.getInside(), polygonCentre, meanRotation);
			Point outside = MultiPoly.rotatePoint(connection.getOutside(), polygonCentre, meanRotation);
			
			Connection rotatedConnection = new Connection(connection.getFlavour(),centre.x, centre.y, inside.x, inside.y, outside.x, outside.y);
			
			int x = (int)(marker.centrePixels[0] + rotDisplacement[0] + ((rotatedConnection.getCentre().x - polygonCentre.x) * SCALE));
			int y = (int)(marker.centrePixels[1] + rotDisplacement[1] + ((rotatedConnection.getCentre().y - polygonCentre.y) * SCALE));
			
			graphics.fillOval(x - 5, y - 5, 10, 10);
			
			int x1 = (int)(marker.centrePixels[0] + rotDisplacement[0] + ((rotatedConnection.getOutside().x - polygonCentre.x) * SCALE));
			int y1 = (int)(marker.centrePixels[1] + rotDisplacement[1] + ((rotatedConnection.getOutside().y - polygonCentre.y) * SCALE));
			
//			if(hitbox.contains(x1, y1)) {
//				x1 = (int)(marker.centrePixels[0] + rotDisplacement[0] + ((rotatedConnection.getInside().x - polygonCentre.x) * SCALE));
//				y1 = (int)(marker.centrePixels[1] + rotDisplacement[1] + ((rotatedConnection.getInside().y - polygonCentre.y) * SCALE));
//			}
			
			// Hitbox contains both ends then discard the connection
//			if(hitbox.contains(x1, y1)) {
				graphics.fillOval(x1 - 5, y1 - 5, 10, 10);
				graphics.drawLine(x1, y1, x, y);
				
				double angleOutside = Maths.getDegrees(x, y, x1, y1);
				com.plotter.algorithms.ShapeData.Connection imageConnection = new com.plotter.algorithms.ShapeData.Connection(connection.getFlavour(), new Point(x, y), angleOutside);
				
				
				shapeDataConnections.add(imageConnection );
//			}
		}
		
		ShapeData shapeData2 = new ShapeData(multiPoly.getShapeId(), shapeDataVerticies, shapeDataConnections, locatedMarkers.get(multiPoly));
		shapeData.add(shapeData2);
		shapeDataMapping.put(shapeData2, multiPoly);
		
		//DEBUG
		// Write the rotation info
		graphics.setColor(Color.white);
		graphics.setComposite(makeComposite(1f));
		graphics.drawString("MROT: " + (Math.round(Math.toDegrees(meanRotation) * 100) / 100.0), (int)marker.centrePixels[0], (int)marker.centrePixels[1]);
		graphics.drawString(""+marker.id, (int)marker.centrePixels[0], (int)marker.centrePixels[1] + 20);
		
		graphics.setColor(Color.red);
		graphics.drawLine((int)marker.centrePixels[0], (int)marker.centrePixels[1],
						  (int)(marker.centrePixels[0] + rotDisplacement[0]),
						  (int)(marker.centrePixels[1] + rotDisplacement[1]));
		
		return true;
	}

	private static DatabaseMultipoly selectShape(Set<Entry<List<Integer>, DatabaseMultipoly>> possibleShapes, Map<DatabaseMultipoly, Set<MarkerInfo>> locatedMarkers) {
		
		DatabaseMultipoly matchingShape = null;
		
NEXT_MARKER:for(Entry<List<Integer>, DatabaseMultipoly> possibleShape:possibleShapes) {
			
			Area polyArea = new Area();
			
			DatabaseMultipoly multipoly = possibleShape.getValue();

			double theta = getMeanRotation(multipoly, locatedMarkers.get(multipoly));

			polyArea.add(new Area(multipoly.getMergedPolygon()));
			
			double centerX = polyArea.getBounds2D().getCenterX();
			double centerY = polyArea.getBounds2D().getCenterY();
			
			double width = polyArea.getBounds2D().getWidth();
			double height = polyArea.getBounds2D().getHeight();
			
			// Scale shape
			AffineTransform scaleTransform = new AffineTransform();
			scaleTransform.setToScale(SCALE, SCALE);
			polyArea.transform(scaleTransform);
			
			centerX = polyArea.getBounds2D().getCenterX();
			centerY = polyArea.getBounds2D().getCenterY();
			
			width = polyArea.getBounds2D().getWidth();
			height = polyArea.getBounds2D().getHeight();
			
			// Move area to origin
			AffineTransform translation = new AffineTransform();
			double minX = polyArea.getBounds2D().getMinX();
			double minY = polyArea.getBounds2D().getMinY();
			translation.setToTranslation(-minX, -minY);
			polyArea.transform(translation);
			
			centerX = polyArea.getBounds2D().getCenterX();
			centerY = polyArea.getBounds2D().getCenterY();
			
			// Rotate shape
			AffineTransform rotation = new AffineTransform();
			rotation.setToRotation(theta);
			polyArea.transform(rotation);
			
			centerX = polyArea.getBounds2D().getCenterX();
			centerY = polyArea.getBounds2D().getCenterY();
			
			// Move area to origin
			translation = new AffineTransform();
			translation.setToTranslation(-minX, -minY);
			polyArea.transform(translation);
			
			centerX = polyArea.getBounds2D().getCenterX();
			centerY = polyArea.getBounds2D().getCenterY();
			
			
			// Move area to fit over a marker
			MarkerInfo markerInfo = locatedMarkers.get(multipoly).iterator().next();
			int[] markerDisplacement = multipoly.getDisplacement(markerInfo.id);
			markerDisplacement[0] *= SCALE;
			markerDisplacement[1] *= SCALE;
			
			double[] rotDisplacement = new double[2];
			
			double cT = Math.cos(theta);
			double sT = Math.sin(theta);
			
			rotDisplacement[0] = markerDisplacement[0] * cT - markerDisplacement[1] * sT;
			rotDisplacement[1] = markerDisplacement[0] * sT + markerDisplacement[1] * cT;
			
			centerX = polyArea.getBounds2D().getCenterX();
			centerY = polyArea.getBounds2D().getCenterY();
			
			translation = new AffineTransform();
			
			// Move scaled centre to marker then minus marker displacement
			translation.setToTranslation((markerInfo.centrePixels[0] - centerX) - rotDisplacement[0],
										 (markerInfo.centrePixels[1] - centerY) - rotDisplacement[1]);
			polyArea.transform(translation);
			
			centerX = polyArea.getBounds2D().getCenterX();
			centerY = polyArea.getBounds2D().getCenterY();
			
			// Check all markers in the set are contained in the area
			Set<MarkerInfo> markers = locatedMarkers.get(multipoly);
			for(MarkerInfo marker:markers) {
				if(!polyArea.contains(marker.centrePixels[0], marker.centrePixels[1]))
					continue NEXT_MARKER;
			}
			
			// If a match has already been found, cannot conclude what shape it is
			if(matchingShape != null)
				return null;
			
			// Else make this shape the match
			matchingShape = multipoly;
		}
		
		return matchingShape;
	}

	private static double getMeanRotation(DatabaseMultipoly multiPoly, Set<MarkerInfo> locatedMarkers) {
		
		Map<Integer, Double> markerRotationOffsets = new HashMap<>();
		Map<Integer, Double> rotations = new HashMap<>();
		for(MarkerInfo info:locatedMarkers) {
			markerRotationOffsets.put(info.id, multiPoly.getRotation(info.id));
			rotations.put(info.id, info.rotation);
		}
		
		if(rotations.size() < 3) {
			double mean = 0.0;
			
			for(Entry<Integer, Double> entry:rotations.entrySet()) {
				mean += entry.getValue() - Math.toDegrees(markerRotationOffsets.get(entry.getKey()));
			}
			return (mean / rotations.size());
		}
		else {
			
			List<Double> rotationsList = new ArrayList<>();
			rotationsList.addAll(rotations.values());
			
			// Only add data that isn't outlier
			// 1.5 * IQR range
			double interQuartileRange = Quartiles.interQuartileRange(rotationsList);
			// [0] = q1, [1] = median, [2] = q3
			double[] quartiles = Quartiles.quartiles(rotationsList);
			
			//TODO prevent looping around angles issue
			
			double lowestAccepted = quartiles[0] - 1.5 * interQuartileRange;
			double highestAccepted = quartiles[2] + 1.5 * interQuartileRange;
			
			int meanC = 0;
			double mean = 0.0;

			for(Entry<Integer, Double> entry:rotations.entrySet()) {
				
				Double rotation = entry.getValue();
				
				if(rotation >= lowestAccepted && rotation <= highestAccepted) {
					mean += rotation - Math.toDegrees(markerRotationOffsets.get(entry.getKey()));
					meanC++;
				}
			}
			
			return (mean / meanC);
		}
		
	}
	
	public static void showMarkers(File selectedFile, Graphics2D graphics) {
		List<MarkerInfo> markers = parseXML(selectedFile);
		
		for(MarkerInfo marker:markers) {
			graphics.setColor(Color.green);
			graphics.setComposite(makeComposite(0.4f));
			graphics.fillRect((int)marker.centrePixels[0] - 20, (int)marker.centrePixels[1] - 20, 40, 40);
			
			graphics.setColor(Color.black);
			graphics.setComposite(makeComposite(1f));
			graphics.drawString(Integer.toString(marker.id), (int)marker.centrePixels[0], (int)marker.centrePixels[1]);
			
			graphics.setColor(Color.red);
			graphics.drawLine((int)marker.centrePixels[0], (int)marker.centrePixels[1],
							  (int) (marker.centrePixels[0]+ (Math.cos(-Math.toRadians(marker.rotation)) * 40)), (int) (marker.centrePixels[1] + (Math.sin(-Math.toRadians(marker.rotation)) * 40)));
			
			//DEBUG
			// Write the rotation info
			graphics.setColor(Color.white);
			graphics.setComposite(makeComposite(1f));
			graphics.drawString("R:" + (int)marker.rotation, (int)marker.centrePixels[0] - 20, (int)marker.centrePixels[1]);
			graphics.drawString(""+marker.id, (int)marker.centrePixels[0], (int)marker.centrePixels[1] + 20);
		}

	}

	private static List<MarkerInfo> parseXML(File selectedFile) {
		
		final List<MarkerInfo> returnList = new ArrayList<>();
		
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				boolean rotation = false;
				boolean centrePixels = false;
				boolean x = false;
				boolean y = false;
				boolean z = false;
				
				private int markerId = 0;
				double xVal = 0;
				double yVal = 0;
				double zRot = 0;

				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

//					System.out.println("Start Element :" + qName);

					if (qName.equalsIgnoreCase("marker")) {
						markerId = Integer.parseInt(attributes.getValue(0));
					}

					if (qName.equalsIgnoreCase("centrePixels")) {
						centrePixels = true;
					}
					
					if (qName.equalsIgnoreCase("x")) {
						x = true;
					}
					
					if (qName.equalsIgnoreCase("y")) {
						y = true;
					}
					
					if (qName.equalsIgnoreCase("z")) {
						z = true;
					}
					
					if (qName.equalsIgnoreCase("rotation")) {
						rotation = true;
					}
					
				}

				public void endElement(String uri, String localName, String qName) throws SAXException {

//					System.out.println("End Element :" + qName);
					
					if (qName.equalsIgnoreCase("centrePixels")) {
						centrePixels = false;
					}
					
					if(qName.equalsIgnoreCase("rotation")) {
						rotation = false;
						returnList.add(new MarkerInfo(markerId, xVal, yVal, zRot));
					}
					
					if (qName.equalsIgnoreCase("x")) {
						x = false;
					}
					
					if (qName.equalsIgnoreCase("y")) {
						y = false;
					}
					
					if (qName.equalsIgnoreCase("z")) {
						z = false;
					}

				}

				public void characters(char ch[], int start, int length) throws SAXException {

					if (centrePixels && x) {
						try {
							xVal = Double.parseDouble(new String(ch, start, length).trim());
							x = false;
						}
						catch(NumberFormatException e){};
					}

					if (centrePixels && y) {
						try {
							yVal = Double.parseDouble(new String(ch, start, length).trim());
							y = false;
						}
						catch(NumberFormatException e){};
					}
					
					if (rotation && z) {
						try {
							zRot = Double.parseDouble(new String(ch, start, length).trim());
							z = false;
						}
						catch(NumberFormatException e){};
					}

				}

			};

			saxParser.parse(selectedFile, handler);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnList;
	}
	
	public static class MarkerInfo {
		public final int id;
		public final double[] centrePixels;
		public final double rotation;
		
		public MarkerInfo(int id, double x, double y, double zRot) {
			this.id = id;
			this.centrePixels = new double[]{x, y};
			this.rotation = zRot;
		}
	}
	
	private static AlphaComposite makeComposite(float alpha){
		int type = AlphaComposite.SRC_OVER;
		return(AlphaComposite.getInstance(type, alpha));
	}

}
