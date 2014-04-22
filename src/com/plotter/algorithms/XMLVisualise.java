package com.plotter.algorithms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.plotter.algorithms.ShapeData.Connection;

public class XMLVisualise {

	public static void showXMLShapes(File xmlFile) throws IOException {

		List<ShapeData> shapes = parseXML(xmlFile);
		
		Point minPoint = new Point(0,0);
		Point maxPoint = new Point(0,0);
		
		findDimensions(minPoint, maxPoint, shapes);
		
		int width = maxPoint.x - minPoint.x;
		int height = maxPoint.y - minPoint.y;
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		
		graphics.setColor(Color.black);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		
		for(ShapeData shape:shapes) {
			
			graphics.setColor(Color.red);
			for(Connection connection:shape.connections) {
				graphics.fillOval(connection.centre.x - 5, 
								  connection.centre.y - 5, 10, 10);
				
				graphics.drawLine(connection.centre.x, connection.centre.y,
								  (int)(connection.centre.x + (Math.cos(Math.toRadians(connection.angle)) * 40)),
								  (int)(connection.centre.y + (Math.sin(Math.toRadians(connection.angle)) * 40)));
			}
			
			graphics.setColor(Color.orange);
			for(Point vertex:shape.shapeVerticies) {
				graphics.fillOval(vertex.x - 8, 
								  vertex.y - 8, 16, 16);
			}
			
		}
		
		JFileChooser chooser = new JFileChooser();
		
		int showOpenDialog = chooser.showSaveDialog(null);
		
		if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
			ImageIO.write(image, "png", chooser.getSelectedFile());
		}

	}

	private static List<ShapeData> parseXML(File file) {
		try {

			final List<ShapeData> shapes = new ArrayList<>();
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				Set<Point> shapeVerticies = null;
				Set<Connection> connections = null;
				
				int flavour;
				double rotation, x, y;
				
				boolean settingX, settingY, settingR, settingF;
				
				{
					settingX = false;
					settingY = false;
					settingR = false;
					settingF = false;
				}
				
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

					System.out.println("Start Element :" + qName);

					if (qName.equalsIgnoreCase("Shape")) {
						shapeVerticies = new HashSet<>();
						connections = new HashSet<>();
					}
					else if (qName.equalsIgnoreCase("X")) {
						settingX = true;
					}
					else if (qName.equalsIgnoreCase("Y")) {
						settingY = true;
					}
					else if (qName.equalsIgnoreCase("Rotation")) {
						settingR = true;
					}
					else if (qName.equalsIgnoreCase("Flavour")) {
						settingF = true;
					}

				}

				public void endElement(String uri, String localName, String qName) throws SAXException {

					System.out.println("End Element :" + qName);
					
					if (qName.equalsIgnoreCase("Shape")) {
						ShapeData shape = new ShapeData(0, 0, shapeVerticies, connections, null);
						shapes.add(shape);
					}
					else if (qName.equalsIgnoreCase("X")) {
						settingX = false;
					}
					else if (qName.equalsIgnoreCase("Y")) {
						settingY = false;
					}
					else if (qName.equalsIgnoreCase("Rotation")) {
						settingR = false;
					}
					else if (qName.equalsIgnoreCase("Flavour")) {
						settingF = false;
					}
					else if (qName.equalsIgnoreCase("Connection")) {
						connections.add(new Connection(flavour, new Point((int)x, (int)y),  rotation));
					}
					else if (qName.equalsIgnoreCase("Vertex")) {
						shapeVerticies.add(new Point((int)x, (int)y));
					}

				}

				public void characters(char ch[], int start, int length) throws SAXException {

					if(settingX) {
						x = Double.parseDouble(new String(ch, start, length));
						settingX = false;
					}
					else if(settingY) {
						y = Double.parseDouble(new String(ch, start, length));
						settingY = false;
					}
					else if(settingR) {
						rotation = Double.parseDouble(new String(ch, start, length));	
						settingR = false;
					}
					else if(settingF) {
						flavour = Integer.parseInt(new String(ch, start, length));
						settingF = false;
					}

				}

			};

			saxParser.parse(file, handler);

			return shapes;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private static void findDimensions(Point minPoint, Point maxPoint, List<ShapeData> shapes) {
		for(ShapeData shape:shapes) {
			for(Point vertex:shape.shapeVerticies) {
				if(vertex.x < minPoint.x)
					minPoint.x = vertex.x;
				else if(vertex.x > maxPoint.x)
					maxPoint.x = vertex.x;
				
				if(vertex.y < minPoint.y)
					minPoint.y = vertex.y;
				else if(vertex.y > maxPoint.y)
					maxPoint.y = vertex.y;
			}
		}
	}

}
