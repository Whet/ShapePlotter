package com.plotter.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.plotter.algorithms.LineMergePolygon;
import com.plotter.algorithms.LineMergePolygon.Edge;
import com.plotter.data.DatabaseMultipoly;

public class PossibleShapesPanel extends JPanel {

	private JPanel imagesPanel;
	private JScrollPane scrollPane;
	
	private List<DatabaseMultipoly> possibleShapes;
	
	public PossibleShapesPanel() {
		
		this.possibleShapes = new ArrayList<>();
		
		this.setLayout(new BorderLayout());
		
		this.imagesPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(0, 1);
		gridLayout.setVgap(20);
		this.imagesPanel.setLayout(gridLayout);
		this.imagesPanel.setBackground(Color.black);
		
		this.scrollPane = new JScrollPane(imagesPanel);
		
		scrollPane.getVerticalScrollBar().setUnitIncrement(32);
		
		this.add(scrollPane, BorderLayout.CENTER);
		
	}
	
	public void setPossibleShapes(List<DatabaseMultipoly> possibleShapes) {
		this.possibleShapes = possibleShapes;
	}
	
	private static class PossibleShape extends JLabel {
		
		private DatabaseMultipoly polygon;
		
		public PossibleShape(DatabaseMultipoly polygon) {
			
			this.polygon = polygon;
			
			BufferedImage imageBuffered = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D graphics = (Graphics2D) imageBuffered.getGraphics();
			
			graphics.setColor(Color.orange);
			
			try {
				LineMergePolygon clone = (LineMergePolygon) polygon.getLineMergePolygon().clone();
				
				// translate to origin
				clone.translate(-(int)polygon.getMergedPolygon().getBounds2D().getWidth(),
								-(int)polygon.getMergedPolygon().getBounds2D().getHeight());
				
				// scale shape to fit on image
				
				double mergedWidth = polygon.getMergedPolygon().getBounds2D().getWidth();
				double mergedHeight = polygon.getMergedPolygon().getBounds2D().getHeight();
				
				double scale = 1.0;
				
				if(mergedHeight > mergedWidth)
					scale = 120 / mergedHeight;
				else
					scale = 120 / mergedWidth;
				
				for(Edge edge:clone.getHairlines()) {
					graphics.drawLine((int)(edge.end1.x * scale),
									  (int)(edge.end1.y * scale),
									  (int)(edge.end2.x * scale),
									  (int)(edge.end2.y * scale));
				}
				
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			
			
			this.setIcon(new ImageIcon(imageBuffered));
			
		}
		
	}
	
}
