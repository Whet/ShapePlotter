package com.plotter.xmlcorrection;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.plotter.algorithms.LibkokiUtils.MarkerInfo;
import com.plotter.algorithms.LineMergePolygon.Edge;
import com.plotter.algorithms.ShapeData;
import com.plotter.algorithms.ShapeData.Connection;
import com.plotter.data.Database;
import com.plotter.data.DatabaseMultipoly;
import com.plotter.data.Maths;
import com.plotter.gui.PropertiesPanel;

public class XMLCorrectionData {

	private List<MarkerData> markers;
	private List<MarkerGroup> markerGroups;
	private Database database;
	private PropertiesPanel properties;
	
	private int selectionMode;
	private Object selectedObject;
	
	public XMLCorrectionData(List<ShapeData> shapeData, Map<ShapeData, DatabaseMultipoly> shapeDataMapping, Database database) {
		this.markers = new ArrayList<>();
		this.markerGroups = new ArrayList<>();
		this.database = database;
		
		findMarkers(shapeData, shapeDataMapping);
		
		selectionMode = 0;
	}
	
	public void loadVariables(PropertiesPanel properties) {
		this.properties = properties;
	}

	private void findMarkers(List<ShapeData> shapeData, Map<ShapeData, DatabaseMultipoly> shapeDataMapping) {
		for(ShapeData sData:shapeData) {
			MarkerGroup group = new MarkerGroup(database);
			
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
	
	public void mD(Point locationOnScreen, Point realLocation, boolean shiftDown) {
		switch(selectionMode) {
			case 0:
				selectedObject = null;
				
				for(MarkerData marker:markers) {
					if(marker.getLocation().distance(realLocation) < 40) {
						selectedObject = marker;
						properties.setInfo(selectedObject);
						break;
					}
				}
			break;
			case 1:
				selectedObject = null;
				
				for(MarkerGroup markerGroup:markerGroups) {
					if(markerGroup.getCentre().distance(realLocation) < 40) {
						selectedObject = markerGroup;
						properties.setInfo(selectedObject);
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
		
		MarkerGroup group = new MarkerGroup(database);
		group.addMarker(selectedMarker);
		markerGroups.add(group);
		
		clearEmptyGroups();
		updateShapes();
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
		updateShapes();
	}

	public void updateShapes() {
		for(MarkerGroup group:this.markerGroups) {
			group.update();
		}
	}

	public boolean isSelected(Object object) {
		return selectedObject == object;
	}

	public int getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode(int selectionMode) {
		selectedObject = null;
		properties.setInfo(null);
		this.selectionMode = selectionMode;
	}

	public Set<MarkerInfo> getMarkerData() {
		
		Set<MarkerInfo> markers = new HashSet<>();
		
		for(MarkerData marker:this.markers) {
			MarkerInfo mData = new MarkerInfo(marker.getMarkerNumber(), marker.getLocation().x, marker.getLocation().y, marker.getRotation());
			markers.add(mData);
		}
		
		return markers;
	}

	public List<ShapeData> getShapeData() {

		List<ShapeData> shapeData = new ArrayList<>();
		
		for(MarkerGroup group:this.markerGroups) {
			Set<Point> shapeDataVerticies = new HashSet<>();
			Set<Connection> connections = new HashSet<>();
			Set<MarkerInfo> markerInfo =  new HashSet<>();
			
			for(Edge edge:group.getShape().getLineMergePolygon().getHairlines()) {
				shapeDataVerticies.add(new Point(edge.end1.x, edge.end1.y));
				shapeDataVerticies.add(new Point(edge.end2.x, edge.end2.y));
			}
			
			for(com.plotter.data.Connection connection:group.getShape().getConnectionPoints()) {
				double angleOutside = Maths.getDegrees(connection.getCentre().x, connection.getCentre().y, connection.getOutside().x, connection.getOutside().y);
				connections.add(new Connection(connection.getFlavour(), connection.getCentre(), angleOutside));
			}
			
			for(MarkerData marker:group.getMarkers()) {
				markerInfo.add(new MarkerInfo(marker.getMarkerNumber(), marker.getLocation().x, marker.getLocation().y, marker.getRotation()));
			}
			
			
			ShapeData sData = new ShapeData(group.getShape().getShapeId(), shapeDataVerticies, connections, markerInfo);
			shapeData.add(sData);
		}
		
		return shapeData;
	}
	
}
