package com.plotter.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plotter.algorithms.MultiPoly;
import com.plotter.algorithms.TetrisSolution.TetrisPiece;

public class Database implements Serializable {

	private static final long serialVersionUID = -8232193117752056206L;
	
	public Map<Integer, MultiPoly> markerToShape;
	
	public Database(List<TetrisPiece> pieces) {
		this.markerToShape = new HashMap<>();
		
		int id = 1;
		
		for (TetrisPiece tetrisPiece : pieces) {
			Integer markerId = new Integer(id);
			markerToShape.put(markerId, tetrisPiece.mPoly);
			id++;
		}
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
