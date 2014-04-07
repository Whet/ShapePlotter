package com.plotter.xmlcorrection;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.plotter.algorithms.LineMergePolygon;
import com.plotter.algorithms.MultiPoly;
import com.plotter.algorithms.LineMergePolygon.Edge;
import com.plotter.data.DatabaseMultipoly;
import com.plotter.gui.PropertiesPanel;

public class MarkerGroup {

	private Set<MarkerData> markers;
	private DatabaseMultipoly shape;
	private LineMergePolygon originShape;
	private List<Edge> transformedEdges;
	
	private Point centre;
	private double rotation;
	
	public MarkerGroup() {
		this.markers = new HashSet<>();
		this.transformedEdges = new ArrayList<>();
	}
	
	public void addMarker(MarkerData marker) {
		this.markers.add(marker);
	}
	
	public void removeMarker(MarkerData marker) {
		this.markers.remove(marker);
	}
	
	public Point getCentre() {
		
		if(this.markers.size() == 0)
			return null;
		
		Point avgPoint = new Point();
		
		for(MarkerData marker:markers) {
			
			int[] offset;
			if(this.shape != null)
				offset = this.shape.getDisplacement(marker.getMarkerNumber());
			else
				offset = new int[]{0,0};
			
			// Adjust displacement for rotation of marker
			double[] rotDisplacement = new double[2];
			
			double cT = Math.cos(this.rotation);
			double sT = Math.sin(this.rotation);
			
			rotDisplacement[0] = offset[0] * cT - offset[1] * sT;
			rotDisplacement[1] = offset[0] * sT + offset[1] * cT;
			
			avgPoint.x += marker.getLocation().x + rotDisplacement[0];
			avgPoint.y += marker.getLocation().y + rotDisplacement[1];
		}
		
		avgPoint.x /= markers.size();
		avgPoint.y /= markers.size();
		
		return avgPoint;
	}
	
	private double getRotation() {
		double averageRotation = 0;
		
		for(MarkerData marker:markers) {
			averageRotation += marker.getRotation() - Math.toDegrees(this.shape.getRotation());
		}
		
		return Math.toRadians(averageRotation / markers.size());
	}
	
	public void translate(int x, int y) {
		for(MarkerData marker:markers) {
			marker.translate(x, y);
		}
		for(Edge edge:this.transformedEdges) {
			edge.translate(x, y);
		}
		
		this.centre = getCentre();
		updateShape();
	}
	
	public DatabaseMultipoly getShape() {
		return shape;
	}

	public void setShape(DatabaseMultipoly shape) {
		this.shape = shape;
		
		try {
			this.originShape = (LineMergePolygon) shape.getLineMergePolygon().clone();
			int minX = (int) shape.getMergedPolygon().getBounds2D().getMinX();
			int minY = (int) shape.getMergedPolygon().getBounds2D().getMinY();
			this.originShape.translate(-minX, -minY);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		update();
	}
	
	public void update() {
		if(this.markers.size() == 0)
			return;
		
		this.rotation = getRotation();
		this.centre = getCentre();
		
		updateShape();
	}

	private void updateShape() {
		
		this.transformedEdges.clear();
		
		List<Edge> edges = new ArrayList<>();
		
		// Rotate
		
		// Centre of origin shape used for rotation
		final Point centre = new Point((int)(shape.getMergedPolygon().getBounds2D().getWidth() / 2), (int)(shape.getMergedPolygon().getBounds2D().getHeight() / 2));
		
		for(Edge edge:this.originShape.getHairlines()) {
			edges.add(new Edge(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y));
		}
		
		for(Edge edge:edges) {
			Point end1 = new Point(MultiPoly.rotatePoint(edge.end1, centre, this.rotation));
//			System.out.println(edge.end1);
			Point end2 = new Point(MultiPoly.rotatePoint(edge.end2, centre, this.rotation));
			edge.end1 = end1;
			edge.end2 = end2;
		}
//		System.out.println();
		
		// Scale
		final int scale = 1;
		
		for(Edge edge:edges) {
			edge.end1.x *= scale;
			edge.end1.y *= scale;
			edge.end2.x *= scale;
			edge.end2.y *= scale;
		}
		
		// Translate to centre
		int translationX, translationY;
		
		translationX = this.centre.x - centre.x;
		translationY = this.centre.y - centre.y;
		
		for(Edge edge:edges) {
			edge.end1.translate(translationX, translationY);
			edge.end2.translate(translationX, translationY);
			transformedEdges.add(edge);
		}
		
	}

	public Set<MarkerData> getMarkers() {
		return markers;
	}

	public void setLocation(Point realLocation) {
		this.translate(realLocation.x - this.getCentre().x, realLocation.y - this.getCentre().y);
	}

	public boolean toggleMarkerMembership(MarkerData selectedMarker) {
		if(!this.markers.contains(selectedMarker)) {
			this.markers.add(selectedMarker);
			return true;
		}
		else {
			this.markers.remove(selectedMarker);
			return false;
		}
	}

	public boolean contains(MarkerData selectedMarker) {
		return this.markers.contains(selectedMarker);
	}
	
	public boolean isEmpty() {
		return this.markers.isEmpty();
	}

	public List<Edge> getScaledShape() {
		return transformedEdges;
	}

	public boolean hasNoShape() {
		return this.shape == null;
	}

}
