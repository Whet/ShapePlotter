package com.plotter.data;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.plotter.algorithms.LibkokiUtils.MarkerInfo;
import com.plotter.algorithms.ShapeData;
import com.plotter.algorithms.ShapeData.Connection;

public class OutputXML {

	public static void outputXML(String fileLocation,
								 List<MarkerInfo> markers, Set<MarkerInfo> allocatedMarkers, List<ShapeData> shapeData) throws IOException {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<ShapeData>\n");
		
		sb.append(TABS(1) + "<LocatedShapes>\n");
		for(int i = 0; i < shapeData.size(); i++) {
			sb.append(new ShapeXML(shapeData.get(i)).toXML(2));
		}
		sb.append(TABS(1) + "</LocatedShapes>\n");
		
		sb.append(TABS(1) + "<AssignedMarkers>\n");
		for(MarkerInfo info:allocatedMarkers) {
			sb.append(new MarkerXML(info).toXML(2));
		}
		sb.append(TABS(1) + "</AssignedMarkers>\n");
		
		sb.append(TABS(1) + "<UnassignedMarkers>\n");
		for(int i = 0; i < markers.size(); i++) {
			if(!allocatedMarkers.contains(markers.get(i)))
				sb.append(new MarkerXML(markers.get(i)).toXML(2));
		}
		sb.append(TABS(1) + "</UnassignedMarkers>\n");
		
		sb.append("</ShapeData>");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileLocation)));
		writer.write(sb.toString());
		writer.close();
		
	}
	
	private static class ShapeXML {
		
		public ShapeData data;
		
		public ShapeXML(ShapeData data) {
			this.data = data;
		}
		
		/*
		 * <Shape>
		 *  <ShapeId>1</ShapeId>
		 *  <ComponentId>1</ComponentId>
		 * 	<Verticies>
		 * 		<Vertex>
		 * 			<X>100.0</X> 
		 * 			<Y>100.0</Y> 
		 * 		</Vertex>
		 * 	</Verticies>
		 * 	<Connections>
		 * 		<Connection>
		 * 			<Flavour>1</Flavour>
		 * 			<X>100.0</X> 
		 * 			<Y>100.0</Y> 
		 * 			<Rotation>180.0</Rotation>
		 * 		</Connection>
		 * 	</Connections>
		 * 	<Markers>
		 * 		<Marker>
		 * 			<Id>1</Id>
		 * 			<X>100.0</X>
		 * 			<Y>100.0</Y>
		 * 			<Rotation>180.0</Rotation>
		 * 		</Marker>
		 * 	</Markers>
		 * </Shape>
		 */
		
		public StringBuffer toXML(int tabs) {
			
			StringBuffer sb = new StringBuffer();
			
			sb.append(TABS(tabs) + "<Shape>\n");
			
			sb.append(TABS(tabs) + "<Id>" + data.shapeId + "</Id>\n");
			
			sb.append(TABS(tabs) + "<ComponentId>" + data. + "</ComponentId>\n");
			
			sb.append(TABS(tabs + 1) + "<Verticies>\n");
				for(Point vertex:data.shapeVerticies) {
					sb.append(new VertexXML(vertex).toXML(tabs + 1));
				}
			sb.append(TABS(tabs + 1) + "</Verticies>\n");
			
			sb.append(TABS(tabs + 1) + "<Connections>\n");
			for(Connection marker:data.connections) {
				sb.append(new ConnectionXML(marker).toXML(tabs + 1));
			}
			sb.append(TABS(tabs + 1) + "</Connections>\n");
			
			sb.append(TABS(tabs + 1) + "<Markers>\n");
			for(MarkerInfo marker:data.markers) {
				sb.append(new MarkerXML(marker).toXML(tabs + 1));
			}
			sb.append(TABS(tabs + 1) + "</Markers>\n");
			
			sb.append(TABS(tabs) + "</Shape>\n");
			
			return sb;
		}
	}
	
	private static class VertexXML {
		
		private Point point;
		
		public VertexXML(Point point) {
			this.point = point;
		}
		
		public StringBuffer toXML(int tabs) {
			
			StringBuffer sb = new StringBuffer();
			
			sb.append(TABS(tabs) + "<Vertex>\n");
			
			sb.append(TABS(tabs + 1) + "<X>" + point.x + "</X>\n");
			sb.append(TABS(tabs + 1) + "<Y>" + point.y + "</Y>\n");
			
			sb.append(TABS(tabs) + "</Vertex>\n");
			
			return sb;
		}
		
	}
	
	private static class ConnectionXML {
		
		private Connection connection;
		
		public ConnectionXML(Connection connection) {
			this.connection = connection;
		}
		
		public StringBuffer toXML(int tabs) {
			
			StringBuffer sb = new StringBuffer();
			
			sb.append(TABS(tabs) + "<Connection>\n");
			
			sb.append(TABS(tabs + 1) + "<X>" + connection.centre.x + "</X>\n");
			sb.append(TABS(tabs + 1) + "<Y>" + connection.centre.y + "</Y>\n");
			sb.append(TABS(tabs + 1) + "<Rotation>" + connection.angle + "</Rotation>\n");
			sb.append(TABS(tabs + 1) + "<Flavour>" + connection.flavour + "</Flavour>\n");
			
			sb.append(TABS(tabs) + "</Connection>\n");
			
			return sb;
		}
		
	}
	
	private static class MarkerXML {
		
		private MarkerInfo marker;
		
		public MarkerXML(MarkerInfo marker) {
			this.marker = marker;
		}

		public StringBuffer toXML(int tabs) {
			
			StringBuffer sb = new StringBuffer();
			
			sb.append(TABS(tabs) + "<Marker>\n");
			
			sb.append(TABS(tabs + 1) + "<Id>" + marker.id + "</Id>\n");
			sb.append(TABS(tabs + 1) + "<Rotation>" + marker.rotation + "</Rotation>\n");
			sb.append(TABS(tabs + 1) + "<X>" + marker.centrePixels[0] + "</X>\n");
			sb.append(TABS(tabs + 1) + "<Y>" + marker.centrePixels[1] + "</Y>\n");
			
			sb.append(TABS(tabs) + "</Marker>\n");
			
			return sb;
			
		}
		
	}
	
	private static String TABS(int tabCount) {
		String tabString = "";
		for(int i = 0; i < tabCount; i++) {
			tabString = tabString + "\t";
		}
		return tabString;
	}
}
