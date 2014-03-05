package com.plotter.data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.plotter.algorithms.LineMergePolygon;
import com.plotter.algorithms.LineMergePolygon.Edge;
import com.plotter.algorithms.TetrisSolution;
import com.plotter.algorithms.TetrisSolution.TetrisGrid;
import com.plotter.algorithms.TetrisSolution.TetrisPiece;
import com.plotter.gui.AssemblyHierarchyPanel.DecompositionImage;
import com.plotter.gui.SVGOptionsMenu.ReferenceInt;

public class OutputSVG {
	
	// 60 for 10mil
	private static final int POLY_SCALE = 300;
	private static final float HAIRLINE = 0.3f;
	private static final float DOTTED = 0.5f;
	private static final Color[] COLOURS = {Color.red, Color.green, Color.blue, Color.orange, Color.yellow.darker(), Color.pink, Color.cyan.darker(), Color.magenta, Color.magenta.darker(), Color.yellow, Color.red.darker(), Color.green.darker(), Color.blue.darker()};
	private static final Color HOLE_COLOUR = Color.black;

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
		
		Database database = new Database();
		
		for(TetrisSolution.TetrisPiece tP:solution.getSolutionPieces()) {
			LayoutPolygon layoutPolygon = new LayoutPolygon(tP);
			layout.add(layoutPolygon);
			List<Integer> pieceMarkers = database.addPiece(tP, layoutPolygon);
			layoutPolygon.setMarkerIds(pieceMarkers);
		}
		
		System.out.println("Blocks placed " + layout.size());
		
		
		database.saveDatabase(new File(fileLocation.substring(0, fileLocation.length() - 4) + "Database"));
		
		List<Line> hairLines = computeHairLines(layout);
		System.out.println();
		// Ask the test to render into the SVG Graphics2D implementation.
		paintToPage(svgGenerator, pageWidth * POLY_SCALE, pageHeight * POLY_SCALE, hairLines, layout);

		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes
		Writer out = new OutputStreamWriter(new FileOutputStream(new File(fileLocation)), "UTF-8");
		svgGenerator.stream(out, useCSS);
		out.close();
		
		paintToPageFancy(svgGenerator, pageWidth * POLY_SCALE, pageHeight * POLY_SCALE, solution, layout);

		out = new OutputStreamWriter(new FileOutputStream(new File(fileLocation.substring(0, fileLocation.length() - 4) + "Fancy.svg")), "UTF-8");
		svgGenerator.stream(out, useCSS);
		out.close();
	}
	
	private static List<Line> computeHairLines(List<LayoutPolygon> shapes) {
		
		Map<ReferenceInt, Color> colours = new HashMap<>();
		
		List<Line> lines = new ArrayList<Line>();
		
		for(LayoutPolygon poly:shapes) {
			if(!colours.containsKey(poly.identity)) {
				colours.put(poly.identity, COLOURS[colours.size()]);
			}
			
			poly.fillColour = colours.get(poly.identity);
		}
		
		for(LayoutPolygon poly:shapes) {
			
			for(Line hairline: poly.getHairlines()) {
				lines.add(new Line(hairline.end1.x, hairline.end1.y, hairline.end2.x, hairline.end2.y, colours.get(poly.identity)));
			}
			
		}
		
		return lines;
	}
	
	private static void paintToPage(SVGGraphics2D page, int pageWidth, int pageHeight, List<Line> hairLines, List<LayoutPolygon> layout) throws IOException {
		
		List<Entry<Point, Integer>> markerPoints = new ArrayList<>();
		List<Integer> markerNumbers = new ArrayList<>();
		List<Double> rotations = new ArrayList<>();
		
		for(int i = 0; i < layout.size(); i++) {
			LayoutPolygon layoutPolygon = layout.get(i);
			markerPoints.addAll(layoutPolygon.markerNumbers.entrySet());
			rotations.addAll(layoutPolygon.markerRotations);
		}
		
		for(Entry<Point, Integer> marker:markerPoints) {
			markerNumbers.add(marker.getValue());
		}
		
		List<BufferedImage> markers = MarkerLoader.getMarkers(markerNumbers, rotations);
		
		// Setup graphics
		page.setSVGCanvasSize(new Dimension(pageWidth, pageHeight));
		
		// Draw lines
		page.setStroke(new BasicStroke(HAIRLINE));
		for(Line hairline:hairLines) {
			page.setColor(hairline.colour);
			page.drawLine(hairline.end1.x, hairline.end1.y, hairline.end2.x, hairline.end2.y);
		}
		
		// Draw markers
		for(int i = 0; i < markerPoints.size(); i++) {
			Entry<Point, Integer> marker = markerPoints.get(i);
			AffineTransform transformation = new AffineTransform();
			transformation.translate(marker.getKey().x -  MarkerLoader.MARKER_WIDTH / 2, marker.getKey().y -  MarkerLoader.MARKER_WIDTH / 2);
			page.drawImage(markers.get(i), transformation, null);
		}
	}
	
	private static void paintToPageFancy(SVGGraphics2D page, int pageWidth, int pageHeight, TetrisSolution solution, List<LayoutPolygon> layout) throws IOException {
		
		List<Entry<Point, Integer>> markerPoints = new ArrayList<>();
		List<Integer> markerNumbers = new ArrayList<>();
		List<Double> rotations = new ArrayList<>();
		
		for(int i = 0; i < layout.size(); i++) {
			LayoutPolygon layoutPolygon = layout.get(i);
			markerPoints.addAll(layoutPolygon.markerNumbers.entrySet());
			rotations.addAll(layoutPolygon.markerRotations);
		}
		
		for(Entry<Point, Integer> marker:markerPoints) {
			markerNumbers.add(marker.getValue());
		}
		
		List<BufferedImage> markers = MarkerLoader.getMarkers(markerNumbers, rotations);
		
		// Setup graphics
		page.setSVGCanvasSize(new Dimension(pageWidth, pageHeight));
		
		// Draw lines
		page.setStroke(new BasicStroke(HAIRLINE));
		
		for(LayoutPolygon poly:layout) {
			page.setColor(poly.fillColour);
			page.fill(poly.fullPoly);
			page.setColor(Color.black);
			page.draw(poly.fullPoly);
		}
		
		// Draw markers
		for(int i = 0; i < markerPoints.size(); i++) {
			Entry<Point, Integer> marker = markerPoints.get(i);
			AffineTransform transformation = new AffineTransform();
			transformation.translate(marker.getKey().x -  MarkerLoader.MARKER_WIDTH / 2, marker.getKey().y -  MarkerLoader.MARKER_WIDTH / 2);
			page.drawImage(markers.get(i), transformation, null);
//			page.fillOval(marker.getKey().x - 3, marker.getKey().y - 3, 6, 6);
		}
		
		TetrisGrid finalGrid = solution.finalGrid;
		
		page.setColor(HOLE_COLOUR);
		
		for(int[] hole:finalGrid.getHoles()) {
			page.fillRect(hole[0] * POLY_SCALE, hole[1] * POLY_SCALE, POLY_SCALE, POLY_SCALE);
		}
	}
	
	public static class LayoutPolygon {

		public Color fillColour;
		private LineMergePolygon lmp;
		public ReferenceInt identity;
		private Point centre;
		private Polygon fullPoly;
		public final int rotationComponent;
		public final List<Point> markerLocations;
		public final Map<Point, Integer> markerNumbers;
		public List<Double> markerRotations;
		
		public LayoutPolygon(TetrisPiece tP) {
			
			this.identity = tP.pop;
			this.rotationComponent = tP.rotationComponent;
			this.markerRotations = tP.markerRotations;
			lmp = new LineMergePolygon();
			
			Area area = new Area();
			Polygon scaledPoly = new Polygon();
			Polygon polygon = tP.mergedPolygon;
			
			for(int i = 0; i < polygon.npoints; i++) {
				scaledPoly.addPoint(polygon.xpoints[i] * POLY_SCALE, polygon.ypoints[i] * POLY_SCALE);
			}
			
			lmp.addPolygon(scaledPoly, POLY_SCALE);
			area.add(new Area(scaledPoly));
			
			this.fullPoly = scaledPoly;
			
			this.centre = new Point((int)area.getBounds2D().getCenterX(), (int)area.getBounds2D().getCenterY());
			
			this.markerLocations = new ArrayList<>();
			
			for(int i = 0; i < tP.markerPolygonLocations.size(); i++) {
				this.markerLocations.add(new Point(tP.markerPolygonLocations.get(i).x * POLY_SCALE + POLY_SCALE / 2,
												   tP.markerPolygonLocations.get(i).y * POLY_SCALE + POLY_SCALE / 2));
			}
			
			this.markerNumbers = new HashMap<>();
			
		}
		
		public void setMarkerIds(List<Integer> pieceMarkers) {
			this.markerNumbers.clear();
			for(int i = 0; i < this.markerLocations.size(); i++) {
				Point markerCentre = this.markerLocations.get(i);
				this.markerNumbers.put(markerCentre, pieceMarkers.get(i));
			}
			
		}

		public Point getCentre() {
			return centre;
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
			
			for(Edge edge: lmp.getDottedlines()) {
				lines.add(new Line(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y));
			}
			
			return lines;
		}

		private void toPolygon(PathIterator p_path) {
			double[] point = new double[2];
			if(p_path.currentSegment(point) != PathIterator.SEG_CLOSE)
				this.fullPoly.addPoint((int) point[0], (int) point[1]);
		}

		public List<Point> getMarkerCentres() {
			return this.markerLocations;
		}

		public List<Double> getMarkerRotations() {
			return this.markerRotations;
		}
		
	}
	
	private static class Line {
		
		private Color colour;
		private Point end1, end2;
		
		public Line(int x, int y, int x1, int y1) {
			this.end1 = new Point(x, y);
			this.end2 = new Point(x1, y1);
		}
		
		public Line(int x, int y, int x1, int y1, Color colour) {
			this.end1 = new Point(x, y);
			this.end2 = new Point(x1, y1);
			this.colour = colour;
		}
		
	}

}
