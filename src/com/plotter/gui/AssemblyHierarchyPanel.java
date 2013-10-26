package com.plotter.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.plotter.algorithms.MultiPoly;
import com.plotter.algorithms.SelfAssemblyHierarchy;
import com.plotter.data.ModulePolygon;

public class AssemblyHierarchyPanel extends JPanel {

	private JFrame window;
	private ModulePolygon modulePolygon;
	private JPanel imagesPanel;
	private JScrollPane scrollPane;
	
	private List<DecompositionStage> decompStages;
	private Map<Integer, List<MultiPoly>> hierarchy;
	
	public AssemblyHierarchyPanel(PlotterWindow window, ModulePolygon modulePolygon) {
		
		this.decompStages = new ArrayList<>();
		
		this.window = window;
		this.modulePolygon = modulePolygon;
		this.setLayout(new BorderLayout());
		
		this.imagesPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(0, 1);
		gridLayout.setVgap(20);
		this.imagesPanel.setLayout(gridLayout);
		this.imagesPanel.setBackground(Color.black);
		
		this.scrollPane = new JScrollPane(imagesPanel);
		
		this.add(scrollPane);
		this.showStages();
	}

	public void addStageImage(MultiPoly shape, int stageNo) {

		MultiPoly shapeCopy = null;
		try {
			shapeCopy = (MultiPoly) shape.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		int imageDimensions = 80;
		int drawDimensions = imageDimensions - 5;
		
		BufferedImage rawImage = new BufferedImage(imageDimensions, imageDimensions, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D graphics = (Graphics2D) rawImage.getGraphics();
		
		graphics.setColor(Color.green);

		double scale = 1;
		
		double polyWidth = shapeCopy.getMergedPolygon().getBounds2D().getWidth();
		double polyHeight = shapeCopy.getMergedPolygon().getBounds2D().getHeight();
		
		if(Math.abs(drawDimensions - polyWidth) > Math.abs(drawDimensions - polyHeight)) {
			// Scale based off width
			scale = drawDimensions / polyWidth;
		}
		else {
			// Scale based off height
			scale = drawDimensions/ polyHeight;
		}
		
		AffineTransform scaleTransform = new AffineTransform();
		
		scaleTransform.setToScale(scale, scale);
		
		graphics.setTransform(scaleTransform);
		
		int deltaX = (int) shapeCopy.getMergedPolygon().getBounds2D().getMinX();
		int deltaY = (int) shapeCopy.getMergedPolygon().getBounds2D().getMinY();
		
		for(Polygon polygon:shapeCopy.getPolygons()) {
			polygon.translate(-deltaX, -deltaY);
			graphics.drawPolygon(polygon);
		}
		
		ImageIcon image = new ImageIcon(rawImage);
		
		JLabel menuImage = new JLabel(image);
		
		for(DecompositionStage stage:this.decompStages) {
			if(stage.getStageNo() == stageNo) {
				stage.addImage(menuImage);
				return;
			}
		}
		
		// Didn't find stage so make new one
		DecompositionStage decompositionStage = new DecompositionStage(stageNo);
		this.decompStages.add(decompositionStage);
		decompositionStage.addImage(menuImage);
		
	}
	
	public void showStages() {
		this.imagesPanel.removeAll();
		
		Collections.sort(this.decompStages);
		
		for(DecompositionStage stage:this.decompStages) {
			JTextField jTextField = new JTextField("Stage: " + stage.getStageNo());
			jTextField.setEditable(false);
			this.imagesPanel.add(jTextField);
			
			for(JLabel image:stage.getStageImages()) {
				this.imagesPanel.add(image);
			}
		}
	}
	
	private static class DecompositionStage implements Comparable<DecompositionStage> {
		
		private int stageNo;
		private List<JLabel> stageImages;
		
		public DecompositionStage(int number) {
			this.stageNo = number;
			this.stageImages = new ArrayList<>();
		}
		
		public void addImage(JLabel menuImage) {
			this.stageImages.add(menuImage);
		}

		public int getStageNo() {
			return stageNo;
		}

		public List<JLabel> getStageImages() {
			return stageImages;
		}

		@Override
		public int compareTo(DecompositionStage o) {
			
			if(o.getStageNo() > this.stageNo)
				return -1;
			
			return 1;
		}
		
	}

	public void createNewHierarchy(int maxDepth) {
		hierarchy = SelfAssemblyHierarchy.getHierarchy(maxDepth, modulePolygon);
		
		this.decompStages.clear();
		this.imagesPanel.removeAll();

		for(Entry<Integer, List<MultiPoly>> entry:hierarchy.entrySet()) {
			for(MultiPoly shape:entry.getValue()) {
				this.addStageImage(shape, entry.getKey());
			}
		}
		
		showStages();
		this.repaint();
	}
	

}
