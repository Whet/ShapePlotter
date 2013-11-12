package com.plotter.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.plotter.data.OutputSVG;
import com.plotter.gui.AssemblyHierarchyPanel.DecompositionImage;

public class SVGOptionsMenu extends JFrame {

	private JPanel imagesPanel;
	private JScrollPane scrollPane;
	private JTextField loadingText;
	
	private File saveFile;
	private Map<DecompositionImage, ReferenceInt> decompImages;
	
	public SVGOptionsMenu(File selectedFile, List<DecompositionImage> decompImages) {
		this.saveFile = selectedFile;

		this.decompImages = new HashMap<>();
		
		for(int i = 0; i < decompImages.size(); i++) {
			this.decompImages.put(decompImages.get(i), new ReferenceInt());
		}
		
		this.setTitle(selectedFile.toString());
		
		this.setLayout(new BorderLayout());
		
		this.imagesPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(0, 1);
		gridLayout.setVgap(20);
		this.imagesPanel.setLayout(gridLayout);
		this.imagesPanel.setBackground(Color.black);
		
		this.scrollPane = new JScrollPane(imagesPanel);
		
		scrollPane.getVerticalScrollBar().setUnitIncrement(32);
		
		
		this.loadingText = new JTextField();
		this.loadingText.setEditable(false);
		this.add(this.loadingText, BorderLayout.NORTH);
		
		final JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					
					@Override
					protected void process(List<Void> chunks) {
	                	loadingText.setText("Saving !!!]");
	                	SVGOptionsMenu.this.repaint();
					}
					
					@Override
					protected Void doInBackground() throws Exception {
						publish();
						output();
						publish();
						return null;
					}
					
					@Override
					protected void done() {
						loadingText.setText("");
						showStages();
						super.done();
					}
					
				};
				
				worker.execute();
			}
		});
		
		this.setSize(300,400);
		
		this.add(saveButton, BorderLayout.SOUTH);
		this.add(scrollPane, BorderLayout.CENTER);
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		showStages();
	}
	
	public void output() {
		try {
			OutputSVG.outputSVG(saveFile.toString(), decompImages, 25, 25);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(SVGOptionsMenu.this, "Error saving file");
		}
	}
	
	public void showStages() {
		this.imagesPanel.removeAll();
		
		for(Entry<DecompositionImage, ReferenceInt> image:this.decompImages.entrySet()) {
			JTextField jTextField = new JTextField("Population: " + image.getValue().value);
			jTextField.setEditable(false);
			this.imagesPanel.add(jTextField);
			
			this.imagesPanel.add(image.getKey());
		}
	}
	
	public static class ReferenceInt {
		public int value;
		
		public ReferenceInt() {
			this.value = 0;
		}
		
		public void reset() {
			this.value = 0;
		}
		
		public void increment() {
			this.value++;
		}
	}

}
