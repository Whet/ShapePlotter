package com.plotter.data;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.plotter.algorithms.MultiPoly;

public class OutputTikz {

	private static int MAX_SHAPE_SIZE;
	
	public static void outputTikz(String fileLocation, List<List<MultiPoly>> stages) throws IOException {
		
		MAX_SHAPE_SIZE = 0;
		
		for(int i = 0; i < stages.get(stages.size() - 1).size(); i++) {
			
			Rectangle2D bounds2d = stages.get(stages.size() - 1).get(i).getMergedPolygon().getBounds2D();
			
			if(bounds2d.getWidth() > MAX_SHAPE_SIZE)
				MAX_SHAPE_SIZE = (int) bounds2d.getWidth();
			if(bounds2d.getHeight() > MAX_SHAPE_SIZE)
				MAX_SHAPE_SIZE = (int) bounds2d.getWidth();
		}
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(createPreamble());
		
		for(int i = 0; i < stages.size(); i++) {
			String varName = "Stage" + i;
			String referenceName = "Stage" + (i-1) + "0";
			
			if(i == 0) {
				sb.append(createStartNode(stages.get(0).get(0).getPolygons().get(0), stages.get(0).get(0).getMergedPolygon(), varName + "0"));
				continue;
			}
			
			List<MultiPoly> stage = stages.get(i);
			for(int j = 0; j < stage.size(); j++) {
				
				String thisVar = varName + j;
				String lastVar = varName + (j - 1);
				
				if(j == 0)
					sb.append(createNode(stage.get(j).getPolygons(), stage.get(j).getMergedPolygon(), thisVar, referenceName, true));
				else
					sb.append(createNode(stage.get(j).getPolygons(), stage.get(j).getMergedPolygon(), thisVar, lastVar, false));
				
			}
		}
		
		sb.append(createPostamble());
		PrintWriter out = new PrintWriter(new FileWriter(fileLocation));
		
		out.print(sb.toString());
		
		out.close();
	}

	private static String createPreamble() {
		return "\\documentclass{article}\n" +
			   "\\usepackage{tikz}\n" +
			   "\\usetikzlibrary{positioning,calc}\n\n" +
			   "\\begin{document}\n" +
			   "\\begin{tikzpicture}[remember picture,  inner/.style={draw=black!}, outer/.style={draw=black!20,thick,inner sep=10pt} ]\n\n";
	}

	private static String createPostamble() {
		return "\\end{tikzpicture}\n\n\\end{document}";
	}
	
	private static String polygonToLines(Polygon polygon, int translateX, int translateY, int polygonCount) {
		
		Polygon polygonCopy = new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
		
		polygonCopy.translate(-translateX, -translateY);
		
		StringBuffer sb = new StringBuffer();
		
		String firstLocation = ":p";
		
		sb.append("\\draw[inner] ");
		
		for(int i = 0; i < polygonCopy.npoints; i++) {
			
			if(i == 0)
				firstLocation = "(" + shrinkNumber(polygonCopy.xpoints[i], polygonCount) + "," + shrinkNumber(polygonCopy.ypoints[i], polygonCount) + ")";
			
			sb.append("(" + shrinkNumber(polygonCopy.xpoints[i], polygonCount) + "," + shrinkNumber(polygonCopy.ypoints[i], polygonCount) + ") --" );
		}
		
		sb.append(firstLocation);
		sb.append(";\n");
		
		return sb.toString();
		
	}
	
	private static String createStartNode(Polygon polygon, Polygon mergedPolygon, String nodeName) {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("\\node[outer] (" + nodeName + ") {\n\\begin{tikzpicture}\n");
		sb.append(polygonToLines(polygon, (int)mergedPolygon.getBounds2D().getMinX(), (int)mergedPolygon.getBounds2D().getMinY(), 1));
		sb.append("\n\\end{tikzpicture}\n};");		
		return sb.toString();
		
	}
	
	private static String createNode(List<Polygon> polygons, Polygon mergedPolygon, String nodeName, String referenceNode, boolean isFirstChild) {
		
		StringBuffer sb = new StringBuffer();
		
		if(isFirstChild)
			sb.append("\\node[outer,below=of " + referenceNode + "] (" + nodeName + ") {\n\\begin{tikzpicture}\n");
		else
			sb.append("\\node[outer,right=of " + referenceNode + "] (" + nodeName + ") {\n\\begin{tikzpicture}\n");
		
		for(Polygon polygon:polygons) {
			sb.append(polygonToLines(polygon, (int)mergedPolygon.getBounds2D().getMinX(), (int)mergedPolygon.getBounds2D().getMinY(), polygons.size()));
		}
		
		sb.append("\n\\end{tikzpicture}\n};\n");	
		
		return sb.toString();
		
	}

	private static double shrinkNumber(int i, int polygonCount) {
		
		return (i * 2) / (double)(MAX_SHAPE_SIZE * polygonCount);
	}
	
}
