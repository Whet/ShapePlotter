package com.plotter.gui;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.plotter.algorithms.LibkokiUtils;
import com.plotter.data.Database;
import com.plotter.data.ModulePolygon;
import com.plotter.data.OutputTikz;

public class PlotterWindow extends JFrame {

	public static String HOME_LOCATION;
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
//					try {
//						OutputSVG.outputSVG(choose.getSelectedFile().toString(), hierarchyPanel.getShapes(), 25, 25);
//					} catch (IOException e) {
//						JOptionPane.showMessageDialog(PlotterWindow.this, "Error saving file");
//					}
					SVGOptionsMenu svgo = new SVGOptionsMenu(choose.getSelectedFile(), hierarchyPanel.getDecompImages());
					svgo.setVisible(true);
					
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
		
		JMenu libkokiMenu = new JMenu("Libkoki");
		menuBar.add(libkokiMenu);
		
		JMenuItem drawMarkersOption = new JMenuItem("Draw Markers");
		drawMarkersOption.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser choose = new JFileChooser();
				FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
				choose.setFileFilter(imageFilter);
				
				choose.setCurrentDirectory(new File(HOME_LOCATION));
				int showOpenDialog = choose.showOpenDialog(PlotterWindow.this);
				
				if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
					
					try {
						BufferedImage image = ImageIO.read(new FileInputStream(choose.getSelectedFile()));
						Graphics2D graphics = (Graphics2D) image.getGraphics();
						
						JFileChooser xmlChoose = new JFileChooser();
						FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
						xmlChoose.setFileFilter(xmlfilter);
						
						xmlChoose.setCurrentDirectory(new File(HOME_LOCATION));
						showOpenDialog = xmlChoose.showOpenDialog(PlotterWindow.this);
						
						if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
							LibkokiUtils.showMarkers(xmlChoose.getSelectedFile(), graphics);
						}
						
						ImageIO.write(image, "png", choose.getSelectedFile());
						
					} catch (FileNotFoundException e1) {
						JOptionPane.showMessageDialog(PlotterWindow.this, "Error loading image file");
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(PlotterWindow.this, "Error loading image file");
					}
				}
				
			}
			
		});
		libkokiMenu.add(drawMarkersOption);
		
		JMenuItem drawShapesOption = new JMenuItem("Draw Polygons");
		drawShapesOption.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser choose = new JFileChooser();
				FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
				choose.setFileFilter(imageFilter);
				
				choose.setCurrentDirectory(new File(HOME_LOCATION));
				int showOpenDialog = choose.showOpenDialog(PlotterWindow.this);
				
				if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
					
					try {
						BufferedImage image = ImageIO.read(new FileInputStream(choose.getSelectedFile()));
						Graphics2D graphics = (Graphics2D) image.getGraphics();
						
						JFileChooser xmlChoose = new JFileChooser();
						FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
						xmlChoose.setFileFilter(xmlfilter);
						
						xmlChoose.setCurrentDirectory(new File(HOME_LOCATION));
						showOpenDialog = xmlChoose.showOpenDialog(PlotterWindow.this);
						
						if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
							
							JFileChooser databaseChoose = new JFileChooser();
							databaseChoose.setCurrentDirectory(new File(HOME_LOCATION));
							showOpenDialog = databaseChoose.showOpenDialog(PlotterWindow.this);
							
							if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
								Database database = Database.loadDatabase(databaseChoose.getSelectedFile());
								LibkokiUtils.showShapes(xmlChoose.getSelectedFile(), graphics, database);
							}
						}
						
						ImageIO.write(image, "png", choose.getSelectedFile());
						
					} catch (FileNotFoundException e1) {
						JOptionPane.showMessageDialog(PlotterWindow.this, "Error loading image file");
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(PlotterWindow.this, "Error loading image file");
					}
				}
				
			}
			
		});
		libkokiMenu.add(drawShapesOption);
		
		JMenuItem correctShapesOption = new JMenuItem("Polygon Correction");
		correctShapesOption.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser choose = new JFileChooser();
				FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
				choose.setFileFilter(imageFilter);
				
				choose.setCurrentDirectory(new File(HOME_LOCATION));
				int showOpenDialog = choose.showOpenDialog(PlotterWindow.this);
				
				if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
					
					try {
						BufferedImage image = ImageIO.read(new FileInputStream(choose.getSelectedFile()));
						
						JFileChooser xmlChoose = new JFileChooser();
						FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
						xmlChoose.setFileFilter(xmlfilter);
						
						xmlChoose.setCurrentDirectory(new File(HOME_LOCATION));
						showOpenDialog = xmlChoose.showOpenDialog(PlotterWindow.this);
						
						if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
							
							JFileChooser databaseChoose = new JFileChooser();
							databaseChoose.setCurrentDirectory(new File(HOME_LOCATION));
							showOpenDialog = databaseChoose.showOpenDialog(PlotterWindow.this);
							
							if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
								Database database = Database.loadDatabase(databaseChoose.getSelectedFile());
								XMLCorrection window = new XMLCorrection(xmlChoose.getSelectedFile(), image, database);
								window.setVisible(true);
							}
						}
						
					} catch (FileNotFoundException e1) {
						JOptionPane.showMessageDialog(PlotterWindow.this, "Error loading image file");
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(PlotterWindow.this, "Error loading image file");
					}
				}
				
			}
			
		});
		libkokiMenu.add(correctShapesOption);
	}

	private void setDecorations() {
		
		this.setTitle("Shape Plotter");
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
