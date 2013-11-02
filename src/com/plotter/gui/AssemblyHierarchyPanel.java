package com.plotter.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.plotter.algorithms.LineMergePolygon;
import com.plotter.algorithms.MultiPoly;
import com.plotter.algorithms.SelfAssemblyHierarchy;
import com.plotter.data.Connection;
import com.plotter.data.ModulePolygon;

public class AssemblyHierarchyPanel extends JPanel {

	private JFrame window;
	private ModulePolygon modulePolygon;
	private JPanel imagesPanel;
	private JScrollPane scrollPane;
	private JButton addGeneration;
	
	private List<DecompositionStage> decompStages;
	private int generation;
	
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
		
		scrollPane.getVerticalScrollBar().setUnitIncrement(32);
		
		addGeneration = new GenerationButton(this);
		
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(addGeneration, BorderLayout.SOUTH);
		
		this.generation = 0;
		
		this.showStages();
		
		
	}

	public void addStageImage(MultiPoly shape, int stageNo) {

		MultiPoly shapeCopy = null;
		try {
			shapeCopy = (MultiPoly) shape.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		int imageDimensions = 120;
		int drawDimensions = imageDimensions - 5;
		
		BufferedImage onImageBuffered = new BufferedImage(imageDimensions, imageDimensions, BufferedImage.TYPE_INT_ARGB);
		BufferedImage offImageBuffered = new BufferedImage(imageDimensions, imageDimensions, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D gOn = (Graphics2D) onImageBuffered.getGraphics();
		Graphics2D gOff = (Graphics2D) offImageBuffered.getGraphics();
		
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
		
		// draw polygons
		AffineTransform scaleTransform = new AffineTransform();
		
		scaleTransform.setToScale(scale, scale);
		
		gOn.setTransform(scaleTransform);
		gOff.setTransform(scaleTransform);
		
		int deltaX = (int) shapeCopy.getMergedPolygon().getBounds2D().getMinX();
		int deltaY = (int) shapeCopy.getMergedPolygon().getBounds2D().getMinY();
		
		LineMergePolygon merged = null;
		try {
			merged = (LineMergePolygon) shape.getMergedLines().clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		merged.translate(-deltaX, -deltaY);
		merged.draw(gOn, Color.green, Color.yellow);
		merged.draw(gOff, Color.cyan, Color.blue);
		
//		for(Polygon polygon:shapeCopy.getPolygons()) {
//			polygon.translate(-deltaX, -deltaY);
//			gOn.drawPolygon(polygon);
//			gOff.drawPolygon(polygon);
//		}
//		shapeCopy.translate(-deltaX, -deltaY);
//		graphics.drawPolygon(shapeCopy.getMergedPolygon());
//		shapeCopy.translate(deltaX, deltaY);
		
//		graphics.setColor(Color.yellow);
		
		// draw connection points
		for(int i = 0; i < shape.getConnectionPoints().size(); i++) {
			Connection connection = shape.getConnectionPoints().get(i);
			gOn.fillOval(connection.getCentre().x - 5 - deltaX, connection.getCentre().y - 5 - deltaY, 10, 10);
			gOn.drawString("F"+connection.getFlavour(), connection.getCentre().x - 5 - deltaX, connection.getCentre().y - 5 - deltaY);
		}
		
		ImageIcon onImage = new ImageIcon(onImageBuffered);
		ImageIcon offImage = new ImageIcon(offImageBuffered);
		
		DecompositionImage menuImage = new DecompositionImage(onImage, offImage, shape);
		
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
			JTextField jTextField = new JTextField("Number of Units: " + (stage.getStageNo() + 1));
			jTextField.setEditable(false);
			this.imagesPanel.add(jTextField);
			
			for(JLabel image:stage.getStageImages()) {
				this.imagesPanel.add(image);
			}
		}
	}
	
	private static class DecompositionStage implements Comparable<DecompositionStage> {
		
		private int stageNo;
		private List<DecompositionImage> stageImages;
		
		public DecompositionStage(int number) {
			this.stageNo = number;
			this.stageImages = new ArrayList<>();
		}
		
		public void addImage(DecompositionImage menuImage) {
			this.stageImages.add(menuImage);
		}

		public int getStageNo() {
			return stageNo;
		}

		public List<DecompositionImage> getStageImages() {
			return stageImages;
		}

		@Override
		public int compareTo(DecompositionStage o) {
			
			if(o.getStageNo() > this.stageNo)
				return -1;
			
			return 1;
		}

		public List<MultiPoly> getStages() {
			List<MultiPoly> polys = new ArrayList<>();
			
			for(DecompositionImage image:stageImages) {
				if(image.isUsed)
					polys.add(image.polygon);
			}
			
			return polys;
		}
		
	}
	
	private static class DecompositionImage extends JLabel {

		private MultiPoly polygon;
		private boolean isUsed;
		
		public DecompositionImage(final ImageIcon onImage, final ImageIcon offImage, MultiPoly polygon) {
			super(onImage);
			
			this.polygon = polygon;
			this.isUsed = true;
			this.addMouseListener(new MouseAdapter() {
				
				@Override
				public void mousePressed(MouseEvent e) {
					isUsed = !isUsed;
					
					if(isUsed)
						setIcon(onImage);
					else
						setIcon(offImage);
					
					repaint();
				}
				
			});
		}

	}
	
	private static class GenerationButton extends JButton implements MouseListener {
		
		private final AssemblyHierarchyPanel window;

		public GenerationButton(AssemblyHierarchyPanel window) {
			super("Add Generation");
			this.window = window;
			this.addMouseListener(this);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			window.addGeneration();
		}
		
	}

	public void createNewHierarchy(int maxDepth) {
		Map<Integer, List<MultiPoly>> hierarchyPolygons = SelfAssemblyHierarchy.getHierarchy(maxDepth, modulePolygon);
		
		this.decompStages.clear();
		this.imagesPanel.removeAll();

		for(Entry<Integer, List<MultiPoly>> entry:hierarchyPolygons.entrySet()) {
			for(MultiPoly shape:entry.getValue()) {
				this.addStageImage(shape, entry.getKey());
			}
			
			if(entry.getKey() > this.generation)
				this.generation = entry.getKey();
		}
		
		showStages();
		this.repaint();
	}
	
	public void addGeneration() {
		
		List<MultiPoly> currentGeneration = new ArrayList<>();
		
		if(this.decompStages.size() == 0) {
			this.addStageImage(new MultiPoly(modulePolygon.getConnectionPoints(), modulePolygon.getPolygon()), this.generation);
			showStages();
			return;
		}
	
		for(DecompositionStage stage:this.decompStages) {
			if(stage.getStageNo() == this.generation) {
				
				for(DecompositionImage image:stage.getStageImages()) {
					if(image.isUsed)
						currentGeneration.add(image.polygon);
				}
				
				List<MultiPoly> nextGeneration = new ArrayList<>();
				
				if(currentGeneration.size() == 0)
					return;
				
				nextGeneration = SelfAssemblyHierarchy.getNextGeneration(modulePolygon, currentGeneration);
					
				// Add here so addStageImage makes new generation
				this.generation++;
				
				for(MultiPoly poly:nextGeneration) {
					this.addStageImage(poly, this.generation);
				}
				
				break;
			}
		}
		
		showStages();
		window.repaint();
	}

	public List<List<MultiPoly>> getStages() {
		
		List<List<MultiPoly>> stages = new ArrayList<List<MultiPoly>>();
		
		for(DecompositionStage stage:this.decompStages) {
			stages.add(stage.getStages());
		}
		
		return stages;
	}
	

}
