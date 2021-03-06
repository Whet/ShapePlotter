package com.plotter.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.plotter.data.ModulePolygon;
import com.plotter.data.OutputSVG;
import com.plotter.data.OutputTikz;

public class PlotterWindow extends JFrame {

	private String HOME_LOCATION;
	private ModulePolygon modulePolygon;
	private AssemblyHierarchyPanel hierarchyPanel;

	public PlotterWindow() {
		
		// Set default home location to the jar location to open/save files in jar folder by default
		HOME_LOCATION = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		if(HOME_LOCATION.endsWith("ShapeDesigner.jar"))
			HOME_LOCATION = HOME_LOCATION.substring(0, HOME_LOCATION.length() - "ShapeDesigner.jar".length());
		
		this.setDecorations();
		
		modulePolygon = new ModulePolygon();
		
		GridPanel gridPanel = new GridPanel(modulePolygon);
		gridPanel.init();
		
		hierarchyPanel = new AssemblyHierarchyPanel(this, modulePolygon);		
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gridPanel, hierarchyPanel);
		this.add(splitPane);
		
		this.addMenuBar();
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		
		splitPane.setDividerLocation(0.8);
		splitPane.setResizeWeight(0.8);
		
	}
	
	private void addMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		this.add(menuBar, BorderLayout.NORTH);
		
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		JMenuItem saveTikzBtn = new JMenuItem("Save to .tex");
		
		saveTikzBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser choose = new JFileChooser();
				choose.setCurrentDirectory(new File(HOME_LOCATION));
				int showSaveDialog = choose.showSaveDialog(PlotterWindow.this);
				
				if(showSaveDialog == JFileChooser.APPROVE_OPTION) {
					try {
						OutputTikz.outputTikz(choose.getSelectedFile().toString(), hierarchyPanel.getStages());
					} catch (IOException e) {
						JOptionPane.showMessageDialog(PlotterWindow.this, "Error saving file");
					}
				}
			}
		});
		
		fileMenu.add(saveTikzBtn);
		
		JMenuItem saveSvg = new JMenuItem("Save to .svg");
		
		saveSvg.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser choose = new JFileChooser();
				choose.setCurrentDirectory(new File(HOME_LOCATION));
				int showSaveDialog = choose.showSaveDialog(PlotterWindow.this);
				
				if(showSaveDialog == JFileChooser.APPROVE_OPTION) {
					try {
						OutputSVG.outputSVG(choose.getSelectedFile().toString(), hierarchyPanel.getShapes(), 320, 320);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(PlotterWindow.this, "Error saving file");
					}
				}
			}
		});
		
		fileMenu.add(saveSvg);
		
		JMenu hierMenu = new JMenu("Hierarchy");
		menuBar.add(hierMenu);
		
		JMenuItem generateHierarchy = new JMenuItem("Generate Hierarchy");
		generateHierarchy.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String inputDialog = JOptionPane.showInputDialog("What is the max number of units you want to simulate with?");
				
				try {
					int maxDepth = Integer.parseInt(inputDialog);
					hierarchyPanel.createNewHierarchy(maxDepth);
				}
				catch(NumberFormatException nfe) {
					JOptionPane.showMessageDialog(PlotterWindow.this, "Did not enter a number!");
				}
			}
			
		});
		hierMenu.add(generateHierarchy );
		
		JMenu optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);
		
		JMenuItem connectionRules = new JMenuItem("Connection rules");
		connectionRules.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			}
			
		});
		optionsMenu.add(connectionRules);
	}

	private void setDecorations() {
		
		this.setTitle("Decomposition Plotter");
		this.setSize(800,600);
		
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				PlotterWindow window = new PlotterWindow();
			}
		});
	}
	
}
