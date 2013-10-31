package com.plotter.algorithms;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LineMergePolygon {

	private List<Edge> borderEdges;
	private List<Edge> insideEdges;
	
	public LineMergePolygon() {
		this.borderEdges = new ArrayList<>();
		this.insideEdges = new ArrayList<>();
	}
	
	public void addPolygon(Polygon polygon) {
		
		List<Edge> polygonEdges = polygonToEdges(polygon);
		
		Iterator<Edge> iterator = polygonEdges.iterator();
		
		while(iterator.hasNext()) {
			Edge next = iterator.next();
			
			boolean newEdge = true;
			
			for(Edge insideEdge:insideEdges) {
				if(next.equals(insideEdge)) {
					newEdge = false;
					break;
				}
			}
			
			Iterator<Edge> iterator2 = borderEdges.iterator();
			
			while(iterator2.hasNext() && newEdge) {
				
				Edge next2 = iterator2.next();
				
				if(next.equals(next2)) {
					iterator.remove();
					iterator2.remove();
					insideEdges.add(next);
					newEdge = false;
				}
				
			}
			
			if(newEdge)
				this.borderEdges.add(next);
		}
		
	}
	
	private List<Edge> polygonToEdges(Polygon polygon) {
		
		List<Edge> edges = new ArrayList<>();
		
		for(int i = 0; i < polygon.npoints - 1; i++) {
			edges.add(new Edge(polygon.xpoints[i], polygon.ypoints[i], polygon.xpoints[i + 1], polygon.ypoints[i + 1]));
		}
		
		edges.add(new Edge(polygon.xpoints[polygon.npoints - 1], polygon.ypoints[polygon.npoints - 1], polygon.xpoints[0], polygon.ypoints[0]));
		
		return edges;
	}

	private static class Edge {
		
		private Point end1, end2;
		
		public Edge(int x, int y, int x1, int y1) {
			this.end1 = new Point(x,y);
			this.end2 = new Point(x1,y1);
		}
		
		@Override
		public boolean equals(Object obj) {
			
			if(obj instanceof Edge) {
				
				Edge edge = (Edge) obj;
				
				if((edge.end1.equals(this.end1) && edge.end2.equals(this.end2)) || (edge.end2.equals(this.end1) && edge.end1.equals(this.end2)))
					return true;
				
			}
			
			return false;
			
		}

		public void translate(int deltaX, int deltaY) {
			end1.translate(deltaX, deltaY);
			end2.translate(deltaX, deltaY);
		}

		public void rotate(Point centreOfRotation, double angle) {
			end1 = rotatePoint(end1, centreOfRotation, angle);
			end2 = rotatePoint(end2, centreOfRotation, angle);
		}
		
		private Point rotatePoint(Point pt, Point center, double angle) {
		    double cosAngle = Math.cos(angle);
		    double sinAngle = Math.sin(angle);
		    double dx = (pt.x-center.x);
		    double dy = (pt.y-center.y);
		
		    pt.x = center.x + (int) (dx*cosAngle-dy*sinAngle);
		    pt.y = center.y + (int) (dx*sinAngle+dy*cosAngle);
		    return pt;
		}
		
	}
	
	public void draw(Graphics2D g, Color edgeColour, Color insideColour) {
		g.setColor(edgeColour);
		
		for(Edge edge:this.borderEdges) {
			g.drawLine(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y);
		}
		
		float dash1[] = {10.0f};
	    BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
	    
	    g.setStroke(dashed);
	    g.setColor(insideColour);
	    
	    for(Edge edge:this.insideEdges) {
			g.drawLine(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y);
		}
	    
	    g.setStroke(new BasicStroke());
	}

	public void translate(int deltaX, int deltaY) {
		for(Edge edge:this.insideEdges) {
			edge.translate(deltaX, deltaY);
		}
		for(Edge edge:this.borderEdges) {
			edge.translate(deltaX, deltaY);
		}
	}

	public void rotate(Point centreOfRotation, double angle) {
		for(Edge edge:this.insideEdges) {
			edge.rotate(centreOfRotation, angle);
		}
		for(Edge edge:this.borderEdges) {
			edge.rotate(centreOfRotation, angle);
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		LineMergePolygon lineMergePolygon = new LineMergePolygon();
		
		for(Edge edge:this.borderEdges) {
			lineMergePolygon.borderEdges.add(new Edge(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y));
		}
		
		for(Edge edge:this.insideEdges) {
			lineMergePolygon.insideEdges.add(new Edge(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y));
		}
		
		return lineMergePolygon;
		
	}
}
