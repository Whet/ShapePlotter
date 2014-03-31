package com.plotter.xmlcorrection;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import com.plotter.data.DatabaseMultipoly;

public class MarkerGroup {

	private Set<MarkerData> markers;
	private DatabaseMultipoly shape;
	
	public MarkerGroup() {
		this.markers = new HashSet<>();
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
	}
	
	public void assignShape(DatabaseMultipoly shape) {
		this.shape = shape;
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
	
}
