package com.plotter.xmlcorrection;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.plotter.algorithms.LineMergePolygon;
import com.plotter.algorithms.LineMergePolygon.Edge;
import com.plotter.algorithms.MultiPoly;
import com.plotter.data.Connection;
import com.plotter.data.Database;
import com.plotter.data.DatabaseMultipoly;
import com.plotter.gui.PropertiesPanel;

public class MarkerGroup {

	private Database database;
	private Set<MarkerData> markers;
	private DatabaseMultipoly shape;
	private LineMergePolygon originShape;
	private List<Edge> transformedEdges;
	
	private Point centre;
	private double rotation;
	private List<com.plotter.data.Connection> transformedConnections;
	
	public MarkerGroup(Database database) {
		this.markers = new HashSet<>();
		this.transformedEdges = new ArrayList<>();
		this.transformedConnections = new ArrayList<>();
		this.database = database;
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
			
			// Group has marker that is not in shape so offset will be null!
			if(offset == null)
				offset = new int[]{0,0};
			
			// Adjust displacement for rotation of marker
			double[] rotDisplacement = new double[2];
			
			double cT = Math.cos(this.rotation);
			double sT = Math.sin(this.rotation);
			
			rotDisplacement[0] = offset[0] * cT - offset[1] * sT;
			rotDisplacement[1] = offset[0] * sT + offset[1] * cT;
			
			avgPoint.x += marker.getLocation().x + rotDisplacement[0] * PropertiesPanel.SCALE;
			avgPoint.y += marker.getLocation().y + rotDisplacement[1] * PropertiesPanel.SCALE;
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
		this.transformedConnections.clear();
		
		List<Edge> edges = new ArrayList<>();
		List<com.plotter.data.Connection> connections = new ArrayList<>();
		List<com.plotter.data.Connection> connectionsBuffer = new ArrayList<>();
		
		// Rotate
		
		// Centre of origin shape used for rotation
		final Point centre = new Point((int)(shape.getMergedPolygon().getBounds2D().getWidth() / 2), (int)(shape.getMergedPolygon().getBounds2D().getHeight() / 2));
		
		for(Edge edge:this.originShape.getHairlines()) {
			edges.add(new Edge(edge.end1.x, edge.end1.y, edge.end2.x, edge.end2.y));
		}
		
		for(com.plotter.data.Connection connection:this.getShape().getConnectionPoints()) {
			connections.add(new com.plotter.data.Connection(connection.getFlavour(), connection.getCentre().x, connection.getCentre().y, connection.getInside().x, connection.getInside().y, connection.getOutside().x, connection.getOutside().y));
		}
		
		for(Edge edge:edges) {
			Point end1 = new Point(MultiPoly.rotatePoint(edge.end1, centre, this.rotation));
			Point end2 = new Point(MultiPoly.rotatePoint(edge.end2, centre, this.rotation));
			edge.end1 = end1;
			edge.end2 = end2;
		}
		
		for(com.plotter.data.Connection connection:connections) {
			Point centre1 = new Point(MultiPoly.rotatePoint(connection.getCentre(), centre, this.rotation));
			Point inside = new Point(MultiPoly.rotatePoint(connection.getInside(), centre, this.rotation));
			Point outside = new Point(MultiPoly.rotatePoint(connection.getOutside(), centre, this.rotation));
			
			connectionsBuffer.add(new Connection(connection.getFlavour(), centre1.x, centre1.y, inside.x, inside.y, outside.x, outside.y));
		}
		
		connections.clear();
		connections.addAll(connectionsBuffer);
		connectionsBuffer.clear();
		
		// Scale
		final double scale = PropertiesPanel.SCALE;
		
		for(Edge edge:edges) {
			edge.end1.x *= scale;
			edge.end1.y *= scale;
			edge.end2.x *= scale;
			edge.end2.y *= scale;
		}
		
		for(com.plotter.data.Connection connection:connections) {
			Point centre1 = new Point(connection.getCentre().x *= scale, connection.getCentre().y *= scale);
			Point inside = new Point(connection.getInside().x *= scale, connection.getInside().y *= scale);
			Point outside = new Point(connection.getOutside().x *= scale, connection.getOutside().y *= scale);
			
			connectionsBuffer.add(new Connection(connection.getFlavour(), centre1.x, centre1.y, inside.x, inside.y, outside.x, outside.y));
		}
		
		connections.clear();
		connections.addAll(connectionsBuffer);
		connectionsBuffer.clear();
		
		// Translate to centre
		int translationX, translationY;
		
		translationX = (int)(this.centre.x - centre.x * scale);
		translationY = (int)(this.centre.y - centre.y * scale);
		
		for(Edge edge:edges) {
			edge.end1.translate(translationX, translationY);
			edge.end2.translate(translationX, translationY);
			transformedEdges.add(edge);
		}
		
		for(com.plotter.data.Connection connection:connections) {
			connection.translate(translationX, translationY);
			this.transformedConnections.add(connection);
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

	public List<DatabaseMultipoly> getPossibleShapes() {

		List<DatabaseMultipoly> possibleShapes = new ArrayList<>();
		
		NEXT_SHAPE:for(Entry<List<Integer>, DatabaseMultipoly> entry:database.markersToShape.entrySet()) {
			
			for(Integer markerId:entry.getKey()) {
				boolean markerFound = false;
				
				for(MarkerData marker:this.markers) {
					if(marker.getMarkerNumber() == markerId) {
						markerFound = true;
						break;
					}
				}
				
				if(!markerFound)
					continue NEXT_SHAPE;
			}
			
			possibleShapes.add(entry.getValue());
			
		}
		
		return possibleShapes;
	}

	public List<Edge> getTransformedEdges() {
		return this.transformedEdges;
	}
	
	public List<com.plotter.data.Connection> getTransformedConnections() {
		return this.transformedConnections;
	}

}
