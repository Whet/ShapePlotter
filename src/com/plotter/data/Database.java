package com.plotter.data;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.plotter.algorithms.TetrisSolution.TetrisPiece;
import com.plotter.data.OutputSVG.LayoutPolygon;

public class Database implements Serializable {

	private static final long serialVersionUID = -8232193117752056206L;
	
	public Map<List<Integer>, DatabaseMultipoly> markersToShape;
	
	public Database() {
		this.markersToShape = new HashMap<>();
		
	}
	
//	public static void main(String[] args) {
//		
//		Database db = new Database();
//		
//		for(int j = 0; j < 5; j++) {
//			List<Integer> markers = new ArrayList<>();
//			final int requiredMarkers = j + 1;
//			
//			do {
//				markers.clear();
//				for(int i = 0; i < requiredMarkers; i++) {
//					
//					int marker = 0;
//					
//					// Avoid repeated markers in sets
//					do {
//						marker = db.getRandomMarkerNumber();
//					}while(markers.contains(marker));
//					
//					
//					markers.add(marker);
//				}
//				
//				System.out.println(markers);
//			}while(db.markerSetExists(markers));
//			System.out.println("Doesn't exist");
//			db.markersToShape.put(markers, null);
//		}
//		
//	}
	
	private int getRandomMarkerNumber() {
		int number = 0;
		
		do {
			number = new Random().nextInt(223) + 1;
		}while(number == 23 || number == 110 || number == 127 || number == 131 || number == 134 || number == 137 || number == 151 || number == 217);
		
		return number;
	}
	
	private boolean markerSetExists(List<Integer> markers) {
		
		if(markersToShape.size() == 0)
			return false;
		
		for(List<Integer> markerSet:markersToShape.keySet()) {
			if(markerSet.containsAll(markers))
				return true;
		}
	
		// Check other way round as well!
		
		for(List<Integer> markerSet:markersToShape.keySet()) {
			if(markers.containsAll(markerSet))
				return true;
		}
		
		return false;
	}
	
	public List<Integer> addPiece(TetrisPiece tP, LayoutPolygon layoutPolygon) {

		List<Integer> markers = new ArrayList<>();
		final int requiredMarkers = layoutPolygon.getMarkerCentres().size();
		
		do {
			markers.clear();
			for(int i = 0; i < requiredMarkers; i++) {
				
				int marker = 0;
				
				// Avoid repeated markers in sets
				do {
					marker = getRandomMarkerNumber();
				}while(markers.contains(marker));
				
				
				markers.add(marker);
			}
			
		}while(markerSetExists(markers));
		
		System.out.println("Markers " + markers);
		
		markersToShape.put(markers,
						   new DatabaseMultipoly(tP.rotationComponent,
								   				 layoutPolygon.getMergedPolygon(),
								   				 layoutPolygon.getLineMergePolygon(),
								   				 layoutPolygon.getConnections(),
								   				 markers,
								   				 layoutPolygon.markerLocations,
								   				 tP.markerRotations,
								   				 new Point((int)layoutPolygon.getMergedPolygon().getBounds2D().getCenterX(), (int)layoutPolygon.getMergedPolygon().getBounds2D().getCenterY()),
								   				 tP.getId(),
								   				 this.markersToShape.size()));
		
		return markers;
	}
	
	public Set<Entry<List<Integer>, DatabaseMultipoly>> getPossibleShapes(Integer markerId) {
		Set<Entry<List<Integer>, DatabaseMultipoly>> possibleShapes = new HashSet<>();
		
		for(Entry<List<Integer>, DatabaseMultipoly> entry:this.markersToShape.entrySet()) {
			for(Integer marker:entry.getKey()) {
				if(marker.equals(markerId))
					possibleShapes.add(entry);
			}
		}
		
		return possibleShapes;
	}
	
	public void saveDatabase(File file) {
		ObjectOutputStream stream = null;
		
		try {
			stream = new ObjectOutputStream(new FileOutputStream(file));   
		
			stream.writeObject(this);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				stream.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	public static Database loadDatabase(File file) {
		ObjectInputStream stream = null;
		
		try {
			stream = new ObjectInputStream(new FileInputStream(file));   
		
			Object readObject = stream.readObject();
			
			return (Database) readObject;
		} 
		catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally{
			try {
				stream.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;	
	}

}
