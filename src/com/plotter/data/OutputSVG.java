package com.plotter.data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.plotter.algorithms.MultiPoly;

public class OutputSVG {

	public static void outputSVG(String fileLocation, List<List<MultiPoly>> stages, int pageWidth, int pageHeight) throws IOException {

		// Get a DOMImplementation.
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);

		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		// Ask the test to render into the SVG Graphics2D implementation.
		paintToPage(svgGenerator, pageWidth, pageHeight);

		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes
		Writer out = new OutputStreamWriter(new FileOutputStream(new File(fileLocation)), "UTF-8");
		svgGenerator.stream(out, useCSS);
	}

	private static void paintToPage(SVGGraphics2D page, int pageWidth, int pageHeight) {
		
		// Setup graphics
		page.setColor(Color.black);
		page.setSVGCanvasSize(new Dimension(pageWidth, pageHeight));
		// Draw hairline
		page.setStroke(new BasicStroke(0.001f));
		
		
		page.drawRect(0, 0, pageWidth, pageHeight);
	}

}
