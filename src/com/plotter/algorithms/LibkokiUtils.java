package com.plotter.algorithms;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
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

public class LibkokiUtils {
	
	public static void showShapes(File selectedFile, Graphics2D graphics, Database database) {
		
		double scale = 0.5;
		
		List<MarkerInfo> markers = parseXML(selectedFile);
		Set<DatabaseMultipoly> allocatedShapes = new HashSet<>();
		
		MarkerInfo[] markerInfo = markers.toArray(new MarkerInfo[markers.size()]);
		
		System.out.println("DATABASE NUMBERS");
		System.out.println(database.markersToShape.keySet());
		
		NEXTMARKER:for(MarkerInfo marker:markers) {
			
			// Get possible shapes for this marker
			Set<Entry<List<Integer>, DatabaseMultipoly>> possibleShapes = database.getPossibleShapes(marker.id);
			
			// Search in the area of each possible shape to see if the required markers are found
			Iterator<Entry<List<Integer>, DatabaseMultipoly>> iterator = possibleShapes.iterator();
				
			Map<DatabaseMultipoly, Set<MarkerInfo>> locatedMarkers = new HashMap<DatabaseMultipoly, Set<MarkerInfo>>();
			
			System.out.println("-------");
			System.out.println(possibleShapes.size() + " possible shapes for marker " + marker.id);
			
			while(iterator.hasNext()) {
				
				Entry<List<Integer>, DatabaseMultipoly> entry = iterator.next();
				
				System.out.println("Need markers {" + entry.getKey() + "}");
				
				// Approximation of search radius
				double radius = 0;
				double width = entry.getValue().getMergedPolygon().getBounds2D().getWidth() * scale;
				double height = entry.getValue().getMergedPolygon().getBounds2D().getHeight() * scale;
				
//				if(width > height)
//					radius = width;
//				else
//					radius = height;
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
				// No mat
				if(polygonMarkersLocated.size() < entry.getKey().size()) {
					iterator.remove();
					continue NEXTMARKER;
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
				
				// Don't repeat shapes
				if(allocatedShapes.contains(multiPoly)) {
					continue;
				}
				
				allocatedShapes.add(multiPoly);
			}
			else {
				//TODO
				System.out.println("Couldn't make a choice!!!");
				continue;
			}
			
			
			MultiPoly polygonCopy = null;
			try {
				polygonCopy = (MultiPoly) multiPoly.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			
			Point polygonCentre = new Point((int) multiPoly.getMergedPolygon().getBounds2D().getCenterX(), (int) multiPoly.getMergedPolygon().getBounds2D().getCenterY());
			double meanRotation = Math.toRadians(getMeanRotation(multiPoly, locatedMarkers.get(multiPoly)));
			if(Math.abs(meanRotation) > 0.00001)
				polygonCopy.rotateNoRounding(polygonCentre, meanRotation);
			
			graphics.setColor(Color.red);
			graphics.setComposite(makeComposite(0.3f));
			
			// Draw connections
			for(Connection connection:polygonCopy.getConnectionPoints()) {
				int x = (int)(marker.centrePixels[0] + (connection.getCentre().x - polygonCentre.x + multiPoly.getDisplacement(marker.id)[0]) * scale);
				int y = (int)(marker.centrePixels[1] + (connection.getCentre().y - polygonCentre.y + multiPoly.getDisplacement(marker.id)[1]) * scale);
				
				graphics.fillOval(x - 5, y - 5, 10, 10);
			}
			
			// Draw shapes
			List<Edge> hairlines = polygonCopy.getMergedLines().getHairlines();
			for(Edge edge:hairlines) {
				graphics.setColor(Color.orange);
				graphics.setComposite(makeComposite(0.9f));
				graphics.setStroke(new BasicStroke(3));
				graphics.drawLine((int)(marker.centrePixels[0] + (edge.end1.x - polygonCentre.x + multiPoly.getDisplacement(marker.id)[0]) * scale),
								  (int)(marker.centrePixels[1] + (edge.end1.y - polygonCentre.y + multiPoly.getDisplacement(marker.id)[1]) * scale),
								  (int)(marker.centrePixels[0] + (edge.end2.x - polygonCentre.x + multiPoly.getDisplacement(marker.id)[0]) * scale),
								  (int)(marker.centrePixels[1] + (edge.end2.y - polygonCentre.y + multiPoly.getDisplacement(marker.id)[1]) * scale));
				graphics.setColor(Color.black);
				graphics.setComposite(makeComposite(1f));
				graphics.setStroke(new BasicStroke(1));
				graphics.drawLine((int)(marker.centrePixels[0] + (edge.end1.x - polygonCentre.x + multiPoly.getDisplacement(marker.id)[0]) * scale),
								  (int)(marker.centrePixels[1] + (edge.end1.y - polygonCentre.y + multiPoly.getDisplacement(marker.id)[1]) * scale),
								  (int)(marker.centrePixels[0] + (edge.end2.x - polygonCentre.x + multiPoly.getDisplacement(marker.id)[0]) * scale),
								  (int)(marker.centrePixels[1] + (edge.end2.y - polygonCentre.y + multiPoly.getDisplacement(marker.id)[1]) * scale));
			}
			
			//DEBUG
			// Write the rotation info
			graphics.setColor(Color.white);
			graphics.setComposite(makeComposite(1f));
			graphics.drawString("MROT: " + (Math.round(Math.toDegrees(meanRotation) * 100) / 100.0), (int)marker.centrePixels[0], (int)marker.centrePixels[1]);
			graphics.drawString(""+marker.id, (int)marker.centrePixels[0], (int)marker.centrePixels[1] + 20);
			
		}
		
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
	
	private static class MarkerInfo {
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
