package com.plotter.algorithms;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.plotter.algorithms.LineMergePolygon.Edge;
import com.plotter.data.Connection;
import com.plotter.data.Database;
import com.plotter.data.DatabaseMultipoly;

public class LibkokiUtils {
	
	public static void showShapes(File selectedFile, Graphics2D graphics, Database database) {
		
		double scale = 1;
		
		List<MarkerInfo> markers = parseXML(selectedFile);
		
		for(MarkerInfo marker:markers) {
			DatabaseMultipoly multiPoly = database.markerToShape.get(marker.id);
			MultiPoly polygonCopy = null;
			try {
				polygonCopy = (MultiPoly) multiPoly.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			
			Point polygonCentre = new Point((int) multiPoly.getMergedPolygon().getBounds2D().getCenterX(), (int) multiPoly.getMergedPolygon().getBounds2D().getCenterY());
			polygonCopy.rotate(polygonCentre, Math.toRadians(marker.rotation));
			
			graphics.setColor(Color.red);
			graphics.setComposite(makeComposite(0.3f));
			
			// Draw connections
			for(Connection connection:polygonCopy.getConnectionPoints()) {
				int x = (int)(marker.centrePixels[0] + (connection.getCentre().x - polygonCentre.x + multiPoly.getDisplacement()[0]) * scale);
				int y = (int)(marker.centrePixels[1] + (connection.getCentre().y - polygonCentre.y + multiPoly.getDisplacement()[1]) * scale);
				
				graphics.fillOval(x, y, 10, 10);
			}
			
			// Draw shapes
			List<Edge> hairlines = polygonCopy.getMergedLines().getHairlines();
			graphics.setColor(Color.green);
			graphics.setComposite(makeComposite(0.6f));
			for(Edge edge:hairlines) {
				graphics.drawLine((int)(marker.centrePixels[0] + (edge.end1.x - polygonCentre.x + multiPoly.getDisplacement()[0]) * scale),
								  (int)(marker.centrePixels[1] + (edge.end1.y - polygonCentre.y + multiPoly.getDisplacement()[1]) * scale),
								  (int)(marker.centrePixels[0] + (edge.end2.x - polygonCentre.x + multiPoly.getDisplacement()[0]) * scale),
								  (int)(marker.centrePixels[1] + (edge.end2.y - polygonCentre.y + multiPoly.getDisplacement()[1]) * scale));
			}
			
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
