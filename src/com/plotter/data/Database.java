package com.plotter.data;

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
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;

import com.plotter.algorithms.TetrisSolution.TetrisPiece;
import com.plotter.data.OutputSVG.LayoutPolygon;

public class Database implements Serializable {

	private static final long serialVersionUID = -8232193117752056206L;
	
	public Map<List<Integer>, DatabaseMultipoly> markersToShape;
	
	
	public Database() {
		this.markersToShape = new HashMap<>();
		
	}
	
	private int getRandomMarkerNumber() {
		int number = 0;
		
		do {
			number = new Random().nextInt(224) + 1;
		}while(number == 23 || number == 110 || number == 127 || number == 131 || number == 134 || number == 137 || number == 151 || number == 217);
		
		return number;
	}
	
	private boolean markerSetExists(List<Integer> markers) {
		
		if(markersToShape.size() == 0)
			return false;
		
LOOP:	for(List<Integer> markerSet:markersToShape.keySet()) {
			
			for(Integer marker:markerSet) {
				if(!markers.contains(marker))
					break LOOP;
			}
			
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
				markers.add(getRandomMarkerNumber());
			}
		}while(markerSetExists(markers));

		markersToShape.put(markers, new DatabaseMultipoly(tP.rotationComponent, tP.mPoly, markers, layoutPolygon.getMarkerCentres(), layoutPolygon.getMarkerRotations(), layoutPolygon.getCentre()));
		
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
