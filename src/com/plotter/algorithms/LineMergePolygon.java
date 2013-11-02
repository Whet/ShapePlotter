package com.plotter.algorithms;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.plotter.data.Maths;

public class LineMergePolygon {

	private List<Edge> borderEdges;
	private List<Edge> insideEdges;
	
	public LineMergePolygon() {
		this.borderEdges = new ArrayList<>();
		this.insideEdges = new ArrayList<>();
	}
	
	public void addPolygon(Polygon polygon) {
		
		List<Edge> polygonEdges = polygonToEdges(polygon);
		
		Iterator<Edge> newIt = polygonEdges.iterator();
		
		while(newIt.hasNext()) {
			Edge newPolyEdge = newIt.next();
			
			// If there is already an inside edge for this edge nothing more can be done
			// Discard the new edge
			for(Edge insideEdge:insideEdges) {
				if(newPolyEdge.equals(insideEdge)) {
					continue;
				}
			}
			
			
			// If there is a border edge that equals a new edge then try to merge them
			Iterator<Edge> oldIt = borderEdges.iterator();
			boolean newEdge = true;
			
			while(oldIt.hasNext()) {
				
				Edge oldBorderEdge = oldIt.next();
				
				// If edges are equal then discard the old border edge and new edge and make a new inside edge
				if(newPolyEdge.equals(oldBorderEdge)) {
					newIt.remove();
					oldIt.remove();
					insideEdges.add(newPolyEdge);
					newEdge = false;
					break;
				}
				// Check if edges overlap and create new edges
				else if(newPolyEdge.overlaps(oldBorderEdge)) {
					
					newIt.remove();
					oldIt.remove();
					
					Edge[] newLines = newPolyEdge.getOverlap(oldBorderEdge);
					
					if(newLines[0].getDistance() > 0)
						this.borderEdges.add(newLines[0]);
					if(newLines[1].getDistance() > 0)
						this.insideEdges.add(newLines[1]);
					if(newLines[2].getDistance() > 0)
						this.borderEdges.add(newLines[2]);
					
					newEdge = false;
					break;
				}
				
			}
			
			// Must be a new border edge
			if(newEdge) {
				this.borderEdges.add(newPolyEdge);
			}
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
		
		// 0 diagonal, 1 horizontal, 2 vertical
		private int lineType;
		
		public Edge(int x, int y, int x1, int y1) {
			this.end1 = new Point(x,y);
			this.end2 = new Point(x1,y1);
			
			this.lineType = 0;
			
			if(this.end1.y == this.end2.y && this.end1.x != this.end2.x)
				this.lineType = 1;
			else if(this.end1.x == this.end2.x && this.end1.y != this.end2.y)
				this.lineType = 2;
		}
		
		public boolean overlaps(Edge edge) {
			
			// Check horizontal
			if(this.lineType == 1 && edge.lineType == 1 && this.end1.y == edge.end1.y) {
				
				// Order points horizontally
				List<Point> orderedPoints = new ArrayList<>();
				orderedPoints.add(edge.end1);
				orderedPoints.add(edge.end2);
				orderedPoints.add(this.end1);
				orderedPoints.add(this.end2);
				
				Collections.sort(orderedPoints, new Comparator<Point>() {
					
					@Override
					public int compare(Point o1, Point o2) {
						if(o1.x < o2.x)
							return -1;
						else if(o1.x > o2.x)
							return 1;
						return 0;
					}
					
				});
				
				// if first two points or neither belong to this then no overlap
				if(((orderedPoints.get(0) == this.end1 || (orderedPoints.get(0) == this.end2)) &&
				   (orderedPoints.get(1) == this.end1 || (orderedPoints.get(1) == this.end2))) ||
				   
				   ((orderedPoints.get(2) == this.end1 || (orderedPoints.get(2) == this.end2)) &&
				   (orderedPoints.get(3) == this.end1 || (orderedPoints.get(3) == this.end2))))
					return false;
				
				return true;
			}
			// Check vertical
			else if(this.lineType == 2 && edge.lineType == 2 && this.end1.x == edge.end1.x) {
				
				// Order points vertically
				List<Point> orderedPoints = new ArrayList<>();
				orderedPoints.add(edge.end1);
				orderedPoints.add(edge.end2);
				orderedPoints.add(this.end1);
				orderedPoints.add(this.end2);
				
				Collections.sort(orderedPoints, new Comparator<Point>() {
					
					@Override
					public int compare(Point o1, Point o2) {
						if(o1.y < o2.y)
							return -1;
						else if(o1.y > o2.y)
							return 1;
						return 0;
					}
					
				});
				
				// if first two points or neither belong to this then no overlap
				if(((orderedPoints.get(0) == this.end1 || (orderedPoints.get(0) == this.end2)) &&
				   (orderedPoints.get(1) == this.end1 || (orderedPoints.get(1) == this.end2))) ||
				   
				   ((orderedPoints.get(2) == this.end1 || (orderedPoints.get(2) == this.end2)) &&
				   (orderedPoints.get(3) == this.end1 || (orderedPoints.get(3) == this.end2))))
					return false;
				
				return true;
			}
			
			return false;
		}
		
		private Edge[] getOverlap(Edge edge) {
			
			// horizontal
			if(this.lineType == 1) {
				
				List<Point> orderedPoints = new ArrayList<>();
				orderedPoints.add(edge.end1);
				orderedPoints.add(edge.end2);
				orderedPoints.add(this.end1);
				orderedPoints.add(this.end2);
				
				Collections.sort(orderedPoints, new Comparator<Point>() {
					
					@Override
					public int compare(Point o1, Point o2) {
						if(o1.x < o2.x)
							return -1;
						else if(o1.x > o2.x)
							return 1;
						return 0;
					}
					
				});
				
				Edge[] edges = new Edge[]{
						new Edge(orderedPoints.get(0).x, orderedPoints.get(0).y, orderedPoints.get(1).x, orderedPoints.get(1).y),
						new Edge(orderedPoints.get(1).x, orderedPoints.get(1).y, orderedPoints.get(2).x, orderedPoints.get(2).y),
						new Edge(orderedPoints.get(2).x, orderedPoints.get(2).y, orderedPoints.get(3).x, orderedPoints.get(3).y),
				};
				
				return edges;
				
			}
			// vertical
			else if(this.lineType == 2) {
				
				List<Point> orderedPoints = new ArrayList<>();
				orderedPoints.add(edge.end1);
				orderedPoints.add(edge.end2);
				orderedPoints.add(this.end1);
				orderedPoints.add(this.end2);
				
				Collections.sort(orderedPoints, new Comparator<Point>() {
					
					@Override
					public int compare(Point o1, Point o2) {
						if(o1.y < o2.y)
							return -1;
						else if(o1.y > o2.y)
							return 1;
						return 0;
					}
					
				});
				
				Edge[] edges = new Edge[]{
						new Edge(orderedPoints.get(0).x, orderedPoints.get(0).y, orderedPoints.get(1).x, orderedPoints.get(1).y),
						new Edge(orderedPoints.get(1).x, orderedPoints.get(1).y, orderedPoints.get(2).x, orderedPoints.get(2).y),
						new Edge(orderedPoints.get(2).x, orderedPoints.get(2).y, orderedPoints.get(3).x, orderedPoints.get(3).y),
				};
				
				return edges;
				
			}
			return null;
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
		
		public int getDistance() {
			return (int) Maths.getDistance(end1.x, end1.y, end2.x, end2.y);
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
