package com.plotter.data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.plotter.algorithms.LineMergePolygon;
import com.plotter.algorithms.LineMergePolygon.Edge;
import com.plotter.algorithms.MultiPoly;
import com.plotter.algorithms.TetrisSolution;
import com.plotter.algorithms.TetrisSolution.TetrisPiece;
import com.plotter.gui.GridPanel;

public class OutputSVG {

	private static final int POLY_SIZE = 10;
	private static final float HAIRLINE = 0.00001f;
	private static final float DOTTED = 1;

	public static void outputSVG(String fileLocation, List<MultiPoly> shapes, int pageWidth, int pageHeight) throws IOException {

		// Get a DOMImplementation.
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);

		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		int widthCubes = 10;
		int heightCubes = 10;
		
		TetrisSolution solution = TetrisSolution.getSolution(widthCubes, heightCubes, shapes);
		
		List<LayoutPolygon> layout = new ArrayList<>();
		
		for(TetrisSolution.TetrisPiece tP:solution.getSolutionPieces()) {
			layout.add(new LayoutPolygon(tP));
		}
		
		List<Line> dottedLines = computeDottedLines(layout);
		List<Line> hairLines = computeHairLines(layout);
		
		// Ask the test to render into the SVG Graphics2D implementation.
		paintToPage(svgGenerator, pageWidth, pageHeight, hairLines, dottedLines);

		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes
		Writer out = new OutputStreamWriter(new FileOutputStream(new File(fileLocation)), "UTF-8");
		svgGenerator.stream(out, useCSS);
	}

	private static List<Line> computeDottedLines(List<LayoutPolygon> shapes) {
		
		List<Line> lines = new ArrayList<Line>();
		
		for(LayoutPolygon poly:shapes) {
			
			for(Line dottedline: poly.getDottedlines()) {
				lines.add(new Line(dottedline.end1.x, dottedline.end1.y, dottedline.end2.x, dottedline.end2.y));
			}
			
		}
		
		return lines;
	}
	
	private static List<Line> computeHairLines(List<LayoutPolygon> shapes) {
		
		List<Line> lines = new ArrayList<Line>();
		
		for(LayoutPolygon poly:shapes) {
			
			for(Line hairline: poly.getHairlines()) {
				lines.add(new Line(hairline.end1.x, hairline.end1.y, hairline.end2.x, hairline.end2.y));
			}
			
		}
		
		return lines;
	}

	private static void paintToPage(SVGGraphics2D page, int pageWidth, int pageHeight, List<Line> hairLines, List<Line> dottedLines) {
		
		// Setup graphics
		page.setColor(Color.black);
		page.setSVGCanvasSize(new Dimension(pageWidth, pageHeight));
		
		page.setStroke(new BasicStroke(HAIRLINE));
		for(Line hairline:hairLines) {
			page.drawLine(hairline.end1.x, hairline.end1.y, hairline.end2.x, hairline.end2.y);
		}
		
		page.setStroke(new BasicStroke(DOTTED));
		for(Line dotted:dottedLines) {
			page.drawLine(dotted.end1.x, dotted.end1.y, dotted.end2.x, dotted.end2.y);
		}
		
	}
	
	public static class LayoutPolygon {

		private LineMergePolygon lmp;
		
		public LayoutPolygon(TetrisPiece tP) {
			lmp = new LineMergePolygon();
			
			for(Polygon polygon:tP.polygons) {
				
				Polygon scaledPoly = new Polygon();
				
				for(int i = 0; i < polygon.npoints; i++) {
					scaledPoly.addPoint(polygon.xpoints[i] * POLY_SIZE, polygon.ypoints[i] * POLY_SIZE);
				}
				
				lmp.addPolygon(polygon);	
			}
		}

		public List<Line> getHairlines() {
			
			List<Line> lines = new ArrayList<>();
			
			for(Edge edge: lmp.getHairlines()) {
				lines.add(new Line(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y));
			}
			
			return lines;
		}

		public List<Line> getDottedlines() {

			List<Line> lines = new ArrayList<>();
			
			for(Edge edge: lmp.getHairlines()) {
				lines.add(new Line(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y));
			}
			
			return lines;
		}
		
		
	}
	
	private static class Line {
		
		private Point end1, end2;
		
		public Line(int x, int y, int x1, int y1) {
			this.end1 = new Point(x, y);
			this.end2 = new Point(x1, y1);
		}
		
	}

}
