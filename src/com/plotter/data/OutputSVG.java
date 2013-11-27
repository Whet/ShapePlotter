package com.plotter.data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.plotter.algorithms.LineMergePolygon;
import com.plotter.algorithms.LineMergePolygon.Edge;
import com.plotter.algorithms.TetrisSolution;
import com.plotter.algorithms.TetrisSolution.TetrisPiece;
import com.plotter.gui.AssemblyHierarchyPanel.DecompositionImage;
import com.plotter.gui.SVGOptionsMenu.ReferenceInt;

public class OutputSVG {

	private static final int POLY_SCALE = 20;
	private static final float HAIRLINE = 0.3f;
	private static final float DOTTED = 0.5f;
	private static final Color[] COLOURS = {Color.red, Color.green, Color.blue, Color.orange, Color.yellow.darker(), Color.pink};

	public static void outputSVG(String fileLocation, Map<DecompositionImage, ReferenceInt> decompImages, int pageWidth, int pageHeight) throws IOException {

		// Get a DOMImplementation.
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);

		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		int widthCubes = pageWidth;
		int heightCubes = pageHeight;
		
		TetrisSolution solution = TetrisSolution.getSolution(widthCubes, heightCubes, decompImages);
		
		List<LayoutPolygon> layout = new ArrayList<>();
		
		for(TetrisSolution.TetrisPiece tP:solution.getSolutionPieces()) {
			layout.add(new LayoutPolygon(tP));
		}
		
		List<Polygon> markerPlots = computeMarkerPlots(layout);
		List<Line> hairLines = computeHairLines(layout);
		System.out.println();
		// Ask the test to render into the SVG Graphics2D implementation.
		paintToPage(svgGenerator, pageWidth * POLY_SCALE, pageHeight * POLY_SCALE, hairLines, markerPlots);

		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes
		Writer out = new OutputStreamWriter(new FileOutputStream(new File(fileLocation)), "UTF-8");
		svgGenerator.stream(out, useCSS);
	}

	private static List<Polygon> computeMarkerPlots(List<LayoutPolygon> shapes) {
		
		List<Polygon> markerPlots = new ArrayList<Polygon>();
		
		for(LayoutPolygon poly:shapes) {
			markerPlots.add(poly.marker);
		}
		
		return markerPlots;
	}
	
	private static List<Line> computeHairLines(List<LayoutPolygon> shapes) {
		
		
		Map<ReferenceInt, Color> colours = new HashMap<>();
		
		List<Line> lines = new ArrayList<Line>();
		
		for(LayoutPolygon poly:shapes) {
			if(!colours.containsKey(poly.identity)) {
				colours.put(poly.identity, COLOURS[colours.size()]);
			}
		}
		
		for(LayoutPolygon poly:shapes) {
			
			for(Line hairline: poly.getHairlines()) {
				lines.add(new Line(hairline.end1.x, hairline.end1.y, hairline.end2.x, hairline.end2.y, colours.get(poly.identity)));
			}
			
		}
		
		return lines;
	}
	
	private static void paintToPage(SVGGraphics2D page, int pageWidth, int pageHeight, List<Line> hairLines, List<Polygon> markerPlots) {
		
		// Setup graphics
		page.setSVGCanvasSize(new Dimension(pageWidth, pageHeight));
		
		page.setStroke(new BasicStroke(HAIRLINE));
		for(Line hairline:hairLines) {
			page.setColor(hairline.colour);
			page.drawLine(hairline.end1.x, hairline.end1.y, hairline.end2.x, hairline.end2.y);
		}
		
		for(Polygon marker:markerPlots) {
			Rectangle2D bounds = marker.getBounds2D();
			
			page.setColor(Color.black);
			page.fillRect((int)bounds.getMinX(), (int)bounds.getMinY(), (int)bounds.getWidth(), (int)bounds.getHeight());
			
			// white fill inside
			page.setColor(Color.white);
			page.fillRect((int)bounds.getMinX() + 2, (int)bounds.getMinY() + 2, (int)bounds.getWidth() - 4, (int)bounds.getHeight() - 4);
		}
	}
	
	public static class LayoutPolygon {

		private Polygon marker;
		private LineMergePolygon lmp;
		public ReferenceInt identity;
		
		public LayoutPolygon(TetrisPiece tP) {
			
			this.identity = tP.pop;
			lmp = new LineMergePolygon();
			
			Area area = new Area();
			
			Polygon firstPolygon = null;
			
			for(Polygon polygon:tP.polygons) {
				
				Polygon scaledPoly = new Polygon();
				
				for(int i = 0; i < polygon.npoints; i++) {
					scaledPoly.addPoint(polygon.xpoints[i] * POLY_SCALE, polygon.ypoints[i] * POLY_SCALE);
				}
				
				lmp.addPolygon(scaledPoly, POLY_SCALE);
				area.add(new Area(scaledPoly));
				
				if(firstPolygon == null)
					firstPolygon = scaledPoly;
			}
			
			marker = new Polygon();
			
			double minX = tP.markerPolygonLocation[0] * POLY_SCALE;
			double minY = tP.markerPolygonLocation[1] * POLY_SCALE;
			
			int halfMarkerSize = POLY_SCALE / 4;
			
			// Put block in centre
			if(area.contains((int)minX - halfMarkerSize, (int)minY - halfMarkerSize) && area.contains((int)minX + halfMarkerSize, (int)minY - halfMarkerSize) && area.contains((int)minX + halfMarkerSize, (int)minY + halfMarkerSize) && area.contains((int)minX - halfMarkerSize, (int)minY + halfMarkerSize)) {
				marker.addPoint((int)minX - halfMarkerSize, (int)minY - halfMarkerSize);
				marker.addPoint((int)minX + halfMarkerSize, (int)minY - halfMarkerSize);
				marker.addPoint((int)minX + halfMarkerSize, (int)minY + halfMarkerSize);
				marker.addPoint((int)minX - halfMarkerSize, (int)minY + halfMarkerSize);
			}
			// Put block in centre of first polygon
			else {
				
				minX = firstPolygon.getBounds2D().getCenterX();
				minY = firstPolygon.getBounds2D().getCenterY();
				
				marker.addPoint((int)minX - halfMarkerSize, (int)minY - halfMarkerSize);
				marker.addPoint((int)minX + halfMarkerSize, (int)minY - halfMarkerSize);
				marker.addPoint((int)minX + halfMarkerSize, (int)minY + halfMarkerSize);
				marker.addPoint((int)minX - halfMarkerSize, (int)minY + halfMarkerSize);
			}
			
			
		}

		public List<Line> getHairlines() {
			
			List<Line> lines = new ArrayList<>();
			
			for(Edge edge: lmp.getHairlines()) {
				lines.add(new Line(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y, Color.white));
			}
			
			return lines;
		}

		public List<Line> getDottedlines() {

			List<Line> lines = new ArrayList<>();
			
			for(Edge edge: lmp.getDottedlines()) {
				lines.add(new Line(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y, Color.white));
			}
			
			return lines;
		}

		public Polygon getMarker() {
			return marker;
		}
		
	}
	
	private static class Line {
		
		private Color colour;
		private Point end1, end2;
		
		public Line(int x, int y, int x1, int y1, Color colour) {
			this.end1 = new Point(x, y);
			this.end2 = new Point(x1, y1);
			this.colour = colour;
		}
		
	}

}
