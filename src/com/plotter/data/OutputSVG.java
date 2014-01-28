package com.plotter.data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
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

	private static final int POLY_SCALE = 100;
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
		
		LayoutPolygon.rotations = new HashMap<>();
		
		List<LayoutPolygon> layout = new ArrayList<>();
		
		for(TetrisSolution.TetrisPiece tP:solution.getSolutionPieces()) {
			layout.add(new LayoutPolygon(tP));
		}
		
		System.out.println("Blocks placed " + layout.size());
		
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
		out.close();
		
		paintToPageFancy(svgGenerator, pageWidth * POLY_SCALE, pageHeight * POLY_SCALE, markerPlots, solution, layout);

		out = new OutputStreamWriter(new FileOutputStream(new File(fileLocation.substring(0, fileLocation.length() - 4) + "Fancy.svg")), "UTF-8");
		svgGenerator.stream(out, useCSS);
		out.close();
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
			
			poly.fillColour = colours.get(poly.identity);
		}
		
		for(LayoutPolygon poly:shapes) {
			
			for(Line hairline: poly.getHairlines()) {
				lines.add(new Line(hairline.end1.x, hairline.end1.y, hairline.end2.x, hairline.end2.y, colours.get(poly.identity)));
			}
			
		}
		
		return lines;
	}
	
	private static void paintToPage(SVGGraphics2D page, int pageWidth, int pageHeight, List<Line> hairLines, List<Polygon> markerPlots) throws IOException {
		
		List<BufferedImage> markers = MarkerLoader.getMarkers(markerPlots.size());
		
		// Setup graphics
		page.setSVGCanvasSize(new Dimension(pageWidth, pageHeight));
		
		// Draw lines
		page.setStroke(new BasicStroke(HAIRLINE));
		for(Line hairline:hairLines) {
			page.setColor(hairline.colour);
			page.drawLine(hairline.end1.x, hairline.end1.y, hairline.end2.x, hairline.end2.y);
		}
		
		int markerId = 0;
		
		// Draw markers
		for(Polygon marker:markerPlots) {
			Rectangle2D bounds = marker.getBounds2D();
			AffineTransform transformation = new AffineTransform();
			transformation.translate(bounds.getCenterX() - MarkerLoader.MARKER_WIDTH / 2, bounds.getCenterY() -  MarkerLoader.MARKER_WIDTH / 2);
			page.drawImage(markers.get(markerId), transformation, null);
			markerId++;
		}
	}
	
	private static void paintToPageFancy(SVGGraphics2D page, int pageWidth, int pageHeight, List<Polygon> markerPlots, TetrisSolution solution, List<LayoutPolygon> layout) throws IOException {
		
		List<BufferedImage> markers = MarkerLoader.getMarkers(markerPlots.size());
		
		// Setup graphics
		page.setSVGCanvasSize(new Dimension(pageWidth, pageHeight));
		
		page.setStroke(new BasicStroke(HAIRLINE));
		
		for(LayoutPolygon poly:layout) {
			page.setColor(poly.fillColour);
			page.fill(poly.fullPoly);
			page.setColor(Color.black);
			page.draw(poly.fullPoly);
		}
		
		int markerId = 0;
		
		// Draw markers
		for(Polygon marker:markerPlots) {
			Rectangle2D bounds = marker.getBounds2D();
			AffineTransform transformation = new AffineTransform();
			transformation.translate(bounds.getCenterX() -  MarkerLoader.MARKER_WIDTH / 2, bounds.getCenterY() -  MarkerLoader.MARKER_WIDTH / 2);
			page.drawImage(markers.get(markerId), transformation, null);
			markerId++;
		}
		
		TetrisGrid finalGrid = solution.finalGrid;
		
		page.setColor(HOLE_COLOUR);
		
		for(int[] hole:finalGrid.getHoles()) {
			page.fillRect(hole[0] * POLY_SCALE, hole[1] * POLY_SCALE, POLY_SCALE, POLY_SCALE);
		}
	}
	
	public static class LayoutPolygon {

		protected static Map<ReferenceInt, int[]> rotations;
		
		public Color fillColour;
		private Polygon marker;
		private LineMergePolygon lmp;
		public ReferenceInt identity;
		private Point centre;
		private Polygon fullPoly;
		
		public LayoutPolygon(TetrisPiece tP) {
			
			this.identity = tP.pop;
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
			
			centre = new Point((int)area.getBounds2D().getCenterX(), (int)area.getBounds2D().getCenterY());
			
			marker = new Polygon();
			
			double centreX = tP.markerPolygonLocation[0] * POLY_SCALE;
			double centreY = tP.markerPolygonLocation[1] * POLY_SCALE;
			
			int halfMarkerSize = MarkerLoader.MARKER_WIDTH / 2;
			
			// Put block in centre if possible
			if(area.contains((int)centreX - halfMarkerSize, (int)centreY - halfMarkerSize) && 
			   area.contains((int)centreX + halfMarkerSize, (int)centreY - halfMarkerSize) &&
			   area.contains((int)centreX + halfMarkerSize, (int)centreY + halfMarkerSize) &&
			   area.contains((int)centreX - halfMarkerSize, (int)centreY + halfMarkerSize)) {
				
				marker.addPoint((int)centreX - halfMarkerSize, (int)centreY - halfMarkerSize);
				marker.addPoint((int)centreX + halfMarkerSize, (int)centreY - halfMarkerSize);
				marker.addPoint((int)centreX + halfMarkerSize, (int)centreY + halfMarkerSize);
				marker.addPoint((int)centreX - halfMarkerSize, (int)centreY + halfMarkerSize);
				
			}
			// Keep moving marker towards corner until it fits
			else {
				
				boolean blockPlaced = true;
				
				int[][] combos = new int[4][2];
				
				if(tP.rotationComponent == 0) {
					combos[0][0] = 1;
					combos[0][1] = 1;
					combos[1][0] = 1;
					combos[1][1] = -1;
					combos[2][0] = -1;
					combos[2][1] = -1;
					combos[3][0] = -1;
					combos[3][1] = 1;
				}
				else if(tP.rotationComponent == 1){
					combos[1][0] = 1;
					combos[1][1] = 1;
					combos[2][0] = 1;
					combos[2][1] = -1;
					combos[3][0] = -1;
					combos[3][1] = -1;
					combos[0][0] = -1;
					combos[0][1] = 1;
				}
				else if(tP.rotationComponent == 2){
					combos[2][0] = 1;
					combos[2][1] = 1;
					combos[3][0] = 1;
					combos[3][1] = -1;
					combos[0][0] = -1;
					combos[0][1] = -1;
					combos[1][0] = -1;
					combos[1][1] = 1;		
				}
				else if(tP.rotationComponent == 3){
					combos[3][0] = 1;
					combos[3][1] = 1;
					combos[0][0] = 1;
					combos[0][1] = -1;
					combos[1][0] = -1;
					combos[1][1] = -1;
					combos[2][0] = -1;
					combos[2][1] = 1;
				}
				
				double startX = centreX;
				double startY = centreY;
				
				if(rotations.containsKey(tP.pop)) {
					
					centreX = startX;
					centreY = startY;
					centreX += rotations.get(tP.pop)[1] * combos[rotations.get(tP.pop)[0]][0];
					centreY += rotations.get(tP.pop)[1] * combos[rotations.get(tP.pop)[0]][1];
					
					marker.addPoint((int)Math.ceil(centreX - halfMarkerSize), (int)Math.ceil(centreY - halfMarkerSize));
					marker.addPoint((int)Math.ceil(centreX + halfMarkerSize), (int)Math.ceil(centreY - halfMarkerSize));
					marker.addPoint((int)Math.ceil(centreX + halfMarkerSize), (int)Math.ceil(centreY + halfMarkerSize));
					marker.addPoint((int)Math.ceil(centreX - halfMarkerSize), (int)Math.ceil(centreY + halfMarkerSize));
					return;
				}
				
				for(int i = 0; i < 4; i++) {
					
					blockPlaced = true;
					centreX = startX;
					centreY = startY;
					
					int offset = 1;
					
					do {
						
						centreX += 1 * combos[i][0];
						centreY += 1 * combos[i][1];
						
						if(!area.getBounds2D().contains(centreX,centreY)) {
							blockPlaced = false;
							break;
						}
						
						offset++;
						
					}
					while(!area.contains((int)centreX - halfMarkerSize, (int)centreY - halfMarkerSize) || 
						  !area.contains((int)centreX + halfMarkerSize, (int)centreY - halfMarkerSize) ||
						  !area.contains((int)centreX + halfMarkerSize, (int)centreY + halfMarkerSize) ||
						  !area.contains((int)centreX - halfMarkerSize, (int)centreY + halfMarkerSize));
				
					if(blockPlaced) {
						marker.addPoint((int)Math.ceil(centreX - halfMarkerSize), (int)Math.ceil(centreY - halfMarkerSize));
						marker.addPoint((int)Math.ceil(centreX + halfMarkerSize), (int)Math.ceil(centreY - halfMarkerSize));
						marker.addPoint((int)Math.ceil(centreX + halfMarkerSize), (int)Math.ceil(centreY + halfMarkerSize));
						marker.addPoint((int)Math.ceil(centreX - halfMarkerSize), (int)Math.ceil(centreY + halfMarkerSize));
						
						rotations.put(tP.pop, new int[]{i,offset});
						break;
					}
				}
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

		public Polygon getMarker() {
			return marker;
		}
		
		private void toPolygon(PathIterator p_path) {
			double[] point = new double[2];
			if(p_path.currentSegment(point) != PathIterator.SEG_CLOSE)
				this.fullPoly.addPoint((int) point[0], (int) point[1]);
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
