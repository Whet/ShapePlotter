package com.plotter.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.plotter.data.OutputSVG;
import com.plotter.gui.AssemblyHierarchyPanel.DecompositionImage;

public class SVGOptionsMenu extends JFrame {

	private JPanel imagesPanel;
	private JScrollPane scrollPane;
	private JTextField loadingText;
	
	private JTextField widthText, heightText;
	
	private File saveFile;
	private Map<DecompositionImage, ReferenceInt> decompImages;
	private Map<ReferenceInt, JTextField> populationCountFields;
	
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
				
//				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
//					
//					@Override
//					protected void process(List<Void> chunks) {
//	                	loadingText.setText("Saving !!!");
//	                	SVGOptionsMenu.this.repaint();
//					}
//					
//					@Override
//					protected Void doInBackground() throws Exception {
//						publish();
//						output();
//						publish();
//						return null;
//					}
//					
//					@Override
//					protected void done() {
//						loadingText.setText("");
//						showStages();
//						super.done();
//					}
//					
//				};
//				
//				worker.execute();
				
				output();
			}
		});
		
		this.setSize(300,400);
		
		this.add(saveButton, BorderLayout.SOUTH);
		this.add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,2));
		
		this.widthText = new JTextField();
		this.heightText = new JTextField();
		
		panel.add(this.widthText);
		panel.add(this.heightText);
		
		this.add(panel, BorderLayout.NORTH);
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		showStages();
	}
	
	public void output() {
		
		for(Entry<ReferenceInt, JTextField> entry:this.populationCountFields.entrySet()) {
			try {
				entry.getKey().targetPopulation = Integer.parseInt(entry.getValue().getText());
			}
			catch(NumberFormatException e) {
				entry.getKey().targetPopulation = 0;
			}
			entry.getKey().reset();
		}
		
		try {
			OutputSVG.outputSVG(saveFile.toString(), decompImages, Integer.parseInt(this.widthText.getText()), Integer.parseInt(this.heightText.getText()));
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(SVGOptionsMenu.this, "Error saving file");
		}
	}
	
	public void showStages() {
		this.imagesPanel.removeAll();
		
		this.populationCountFields = new HashMap<>();
		
		for(Entry<DecompositionImage, ReferenceInt> image:this.decompImages.entrySet()) {
			JTextField jTextField = new JTextField(image.getValue().targetPopulation);
			jTextField.setEditable(true);
			this.imagesPanel.add(jTextField);
			this.imagesPanel.add(image.getKey());
			
			this.populationCountFields.put(image.getValue(), jTextField);
			jTextField.setText("5");
		}
	}
	
	public static class ReferenceInt {
		
		public int currentPopulation;
		public int targetPopulation;
		
		public ReferenceInt() {
			this.currentPopulation = 0;
		}
		
		public void reset() {
			this.currentPopulation = 0;
		}
		
		public void increment() {
			this.currentPopulation++;
		}
	}

}
