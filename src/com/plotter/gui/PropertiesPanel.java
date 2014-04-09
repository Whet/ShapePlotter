package com.plotter.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.plotter.algorithms.LibkokiUtils;
import com.plotter.data.OutputXML;
import com.plotter.xmlcorrection.MarkerData;
import com.plotter.xmlcorrection.MarkerGroup;
import com.plotter.xmlcorrection.XMLCorrectionData;

public class PropertiesPanel extends JPanel {

	private PossibleShapesPanel possibleShapes;
	private TextBoxesPanel textPanel;
	private boolean updated;
	
	public PropertiesPanel(XMLCorrectionData data) {
		this.setLayout(new BorderLayout());
		JTextField title = new JTextField("Properties:");
		title.setEditable(false);
		this.add(title, BorderLayout.NORTH);
		
		possibleShapes = new PossibleShapesPanel();
		textPanel = new TextBoxesPanel(this, possibleShapes, data);
		
		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout());
		
		this.add(sub, BorderLayout.CENTER);
		
		sub.add(textPanel, BorderLayout.NORTH);
		
		sub.add(possibleShapes, BorderLayout.CENTER);
		
		this.updated = false;
	}
	
	public boolean update() {
		textPanel.updateInformation();
		
		if(updated) {
			updated = false;
			return true;
		}
		
		return false;
	}
	
	public void setInfo(Object information) {
		this.textPanel.setInfo(information);
	}
	
	private static class TextBoxesPanel extends JPanel {
		
		private JTextField topBox, bottomBox;
		private JLabel topLabel, bottomLabel;
		private JButton outputXML;
		private Object information;
		private PossibleShapesPanel possibleShapes;
		
		public TextBoxesPanel(final PropertiesPanel panel, final PossibleShapesPanel possibleShapesPanel, final XMLCorrectionData data) {
			
			this.setLayout(new GridLayout(3,2));
			
			this.topBox = new JTextField();
			this.bottomBox = new JTextField();
			
			this.topLabel = new JLabel();
			this.bottomLabel = new JLabel();
			
			this.topLabel.setLabelFor(topBox);
			this.bottomLabel.setLabelFor(bottomBox);
			
			this.add(topLabel);
			this.add(topBox);
			
			this.add(bottomLabel);
			this.add(bottomBox);
			
			this.add(Box.createHorizontalGlue());
			
			outputXML = new JButton("Output XML");
			outputXML.addMouseListener(new MouseAdapter() {
				
				@Override
				public void mousePressed(MouseEvent event) {
					
					JFileChooser choose = new JFileChooser();
					choose.setCurrentDirectory(new File(PlotterWindow.HOME_LOCATION));
					int showSaveDialog = choose.showSaveDialog(TextBoxesPanel.this);
					
					if(showSaveDialog == JFileChooser.APPROVE_OPTION) {
						
						File selectedFile = choose.getSelectedFile();
						
						// Output the located shapes data to XML
						try {
							OutputXML.outputXML(selectedFile.getAbsolutePath(), new ArrayList<LibkokiUtils.MarkerInfo>(), data.getMarkerData(), data.getShapeData());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			this.add(outputXML);
			
			this.possibleShapes = possibleShapesPanel;
			
			this.topBox.addKeyListener(new KeyAdapter() {
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						panel.updated = true;
					}
				}
				
			});
			
			this.bottomBox.addKeyListener(new KeyAdapter() {
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						panel.updated = true;
					}
				}
				
			});
			
		}
		
		public void updateInformation() {
			if(information instanceof MarkerData) {
				
				MarkerData marker = (MarkerData) information;
				
				try {
					marker.setMarkerNumber(Integer.parseInt(topBox.getText()));
					marker.setRotation(Double.parseDouble(bottomBox.getText()));
				}
				catch(NumberFormatException e){}
			}
			
		}

		public void updateText() {
			
			if(information instanceof MarkerData) {
				
				MarkerData marker = (MarkerData) information;
				
				this.topLabel.setText("Marker Number:");
				this.topBox.setEditable(true);
				this.topBox.setText(""+marker.getMarkerNumber());
				
				this.bottomLabel.setText("Rotation:");
				this.bottomBox.setEditable(true);
				this.bottomBox.setText(""+marker.getRotation());
			}
			else if(information instanceof MarkerGroup) {
				
				MarkerGroup group = (MarkerGroup) information;
				
				this.topLabel.setText("Markers:");
				this.topBox.setEditable(false);
				
				Set<Integer> markerIds = new HashSet<>();
				
				for(MarkerData marker:group.getMarkers()) {
					markerIds.add(marker.getMarkerNumber());
				}
				
				this.topBox.setText(markerIds.toString());
				
				this.bottomLabel.setText("-");
				this.bottomBox.setEditable(false);
				this.bottomBox.setText("-");
				
				this.possibleShapes.setPossibleShapes(group);
			}
			else {
				this.topLabel.setText("");
				this.topBox.setEditable(false);
				this.topBox.setText("");
				
				this.bottomLabel.setText("");
				this.bottomBox.setEditable(false);
				this.bottomBox.setText("");
				
				this.information = null;
			}
		}

		public void setInfo(Object information) {
			this.information = information;
			this.updateText();
		}
		
	}
	
}