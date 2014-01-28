package com.plotter.algorithms;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.plotter.data.Connection;
import com.plotter.data.Database;

public class LibkokiUtils {
	
	public static void showShapes(File selectedFile, Graphics2D graphics, Database database) {
		
		List<MarkerInfo> markers = parseXML(selectedFile);
		
		for(MarkerInfo marker:markers) {
			MultiPoly multiPoly = database.markerToShape.get(marker.id);
			
			graphics.setColor(Color.green);
			graphics.setComposite(makeComposite(1.0f));
			Polygon mergedPolygon = new Polygon(multiPoly.getMergedPolygon().xpoints, multiPoly.getMergedPolygon().ypoints, multiPoly.getMergedPolygon().npoints);
			mergedPolygon.translate((int)(marker.centrePixels[0] - mergedPolygon.getBounds2D().getWidth() / 2),
									(int)(marker.centrePixels[1] - mergedPolygon.getBounds2D().getHeight() / 2));
			graphics.drawPolygon(mergedPolygon);
			
			graphics.setColor(Color.red);
			graphics.setComposite(makeComposite(0.3f));
			
			for(Connection connection:multiPoly.getConnectionPoints()) {
				graphics.fillOval(connection.getCentre().x - 5, connection.getCentre().y - 5, 10, 10);
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
		}

	}

	private static List<MarkerInfo> parseXML(File selectedFile) {
		
		final List<MarkerInfo> returnList = new ArrayList<>();
		
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				boolean centrePixels = false;
				boolean x = false;
				boolean y = false;
				
				private int markerId = 0;
				double xVal = 0;
				double yVal = 0;

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
					
				}

				public void endElement(String uri, String localName, String qName) throws SAXException {

//					System.out.println("End Element :" + qName);
					
					if (qName.equalsIgnoreCase("centrePixels")) {
						centrePixels = false;
						
						returnList.add(new MarkerInfo(markerId, xVal, yVal));
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
		
		public MarkerInfo(int id, double x, double y) {
			this.id = id;
			this.centrePixels = new double[]{x, y};
		}
	}
	
	private static AlphaComposite makeComposite(float alpha){
		int type = AlphaComposite.SRC_OVER;
		return(AlphaComposite.getInstance(type, alpha));
	}

}
