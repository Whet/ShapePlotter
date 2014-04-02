package com.plotter.xmlcorrection;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.plotter.algorithms.LibkokiUtils.MarkerInfo;
import com.plotter.algorithms.ShapeData;
import com.plotter.data.Database;
import com.plotter.data.DatabaseMultipoly;

public class XMLCorrectionData {

	private List<MarkerData> markers;
	private List<MarkerGroup> markerGroups;
	private Database database;
	
	public XMLCorrectionData(List<ShapeData> shapeData, Map<ShapeData, DatabaseMultipoly> shapeDataMapping, Database database) {
		this.markers = new ArrayList<>();
		this.markerGroups = new ArrayList<>();
		this.database = database;
		
		findMarkers(shapeData, shapeDataMapping);
		
		selectionMode = 0;
	}

	private void findMarkers(List<ShapeData> shapeData, Map<ShapeData, DatabaseMultipoly> shapeDataMapping) {
		for(ShapeData sData:shapeData) {
			MarkerGroup group = new MarkerGroup();
			
			for(MarkerInfo marker:sData.markers) {
				MarkerData markerData = new MarkerData(new Point((int)marker.centrePixels[0], (int)marker.centrePixels[1]), marker.rotation, marker.id);
				markers.add(markerData);
				group.addMarker(markerData);
			}
			
			group.setShape(shapeDataMapping.get(sData));
			markerGroups.add(group);
		}
	}

	public List<MarkerData> getMarkers() {
		return markers;
	}

	public List<MarkerGroup> getMarkerGroups() {
		return markerGroups;
	}
	
	private int selectionMode;
	private Object selectedObject;
	
	public void mD(Point locationOnScreen, Point realLocation, boolean shiftDown) {
		switch(selectionMode) {
			case 0:
				selectedObject = null;
				
				for(MarkerData marker:markers) {
					if(marker.getLocation().distance(realLocation) < 40) {
						selectedObject = marker;
						break;
					}
				}
			break;
			case 1:
				selectedObject = null;
				
				for(MarkerGroup markerGroup:markerGroups) {
					if(markerGroup.getCentre().distance(realLocation) < 40) {
						selectedObject = markerGroup;
						break;
					}
				}
			break;
		}
	}

	public void rMD(Point locationOnScreen, Point realLocation, boolean shiftDown) {
		switch(selectionMode) {
			case 0:
				if(selectedObject != null) {
					((MarkerData)selectedObject).setLocation(realLocation);
				}
			break;
			case 1:
				if(selectedObject != null) {
					if(shiftDown) {
						// Add/Remove marker from group
						
						MarkerData selectedMarker = null;
						
						for(MarkerData marker:markers) {
							if(marker.getLocation().distance(realLocation) < 40) {
								selectedMarker = marker;
								break;
							}
						}
						
						if(selectedMarker != null) {
							boolean addedToGroup = ((MarkerGroup)selectedObject).toggleMarkerMembership(selectedMarker);
							
							if(addedToGroup)
								removeFromGroups(selectedMarker, (MarkerGroup)selectedObject);
							else
								createGroup(selectedMarker);
						}
					}
					else {
						// Move whole group
						((MarkerGroup)selectedObject).setLocation(realLocation);
					}
				}
			break;
		}
	}
	
	private void createGroup(MarkerData selectedMarker) {
		// Create a singleton group for the marker if it is not in a group
		for(MarkerGroup mGroup:markerGroups) {
			if(mGroup.contains(selectedMarker))
				return;
		}
		
		MarkerGroup group = new MarkerGroup();
		group.addMarker(selectedMarker);
		markerGroups.add(group);
		
		clearEmptyGroups();
	}

	private void clearEmptyGroups() {
		Iterator<MarkerGroup> iterator = markerGroups.iterator();
		
		while(iterator.hasNext()) {
			MarkerGroup next = iterator.next();
			
			if(next.isEmpty())
				iterator.remove();
		}
	}

	private void removeFromGroups(MarkerData selectedMarker, MarkerGroup group) {
		// Remove marker before all other groups
		
		for(MarkerGroup mGroup:markerGroups) {
			if(mGroup != group)
				mGroup.removeMarker(selectedMarker);
		}
		
		clearEmptyGroups();
	}

	public boolean isSelected(Object object) {
		return selectedObject == object;
	}

	public int getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode(int selectionMode) {
		selectedObject = null;
		this.selectionMode = selectionMode;
	}
	
}
