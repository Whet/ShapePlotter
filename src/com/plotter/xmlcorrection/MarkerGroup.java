package com.plotter.xmlcorrection;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.plotter.algorithms.LineMergePolygon.Edge;
import com.plotter.data.DatabaseMultipoly;

public class MarkerGroup {

	private Set<MarkerData> markers;
	private DatabaseMultipoly shape;
	private List<Edge> scaledShape;
	
	public MarkerGroup() {
		this.markers = new HashSet<>();
		this.scaledShape = new ArrayList<>();
	}
	
	public void addMarker(MarkerData marker) {
		this.markers.add(marker);
	}
	
	public void removeMarker(MarkerData marker) {
		this.markers.remove(marker);
	}
	
	public Point getCentre() {
		
		Point avgPoint = new Point();
		
		for(MarkerData marker:markers) {
			avgPoint.x += marker.getLocation().x;
			avgPoint.y += marker.getLocation().y;
		}
		
		avgPoint.x /= markers.size();
		avgPoint.y /= markers.size();
		
		return avgPoint;
	}
	
	public void translate(int x, int y) {
		for(MarkerData marker:markers) {
			marker.translate(x, y);
		}
		for(Edge edge:this.scaledShape) {
			edge.translate(x, y);
		}
	}
	
	public DatabaseMultipoly getShape() {
		return shape;
	}

	public void setShape(DatabaseMultipoly shape) {
		this.shape = shape;
		
		createScaledShape(shape, 1);
	}

	private void createScaledShape(DatabaseMultipoly shape, int scale) {
		List<Edge> hairlines = shape.getLineMergePolygon().getHairlines();
		this.scaledShape.clear();
		for(Edge edge:hairlines) {
			scaledShape.add(new Edge(edge.end1.x * scale, edge.end1.y * scale, edge.end2.x * scale, edge.end2.y * scale));
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
		return scaledShape;
	}
	
}
