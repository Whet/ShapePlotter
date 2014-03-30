package com.plotter.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.plotter.algorithms.LibkokiUtils;
import com.plotter.algorithms.ShapeData;
import com.plotter.algorithms.LibkokiUtils.MarkerInfo;
import com.plotter.data.Database;
import com.plotter.data.DatabaseMultipoly;

public class XMLCorrection extends JFrame {

	private final File xmlFileLocation;
	private final Database database;
	private final CorrectionPanel panel;
	
	public XMLCorrection(final File selectedFile, final BufferedImage image, final Database database) {
		this.xmlFileLocation = new File(selectedFile.getAbsolutePath().substring(0, selectedFile.getAbsolutePath().length() - 4) + "Data.xml");
		this.database = database;
		
		List<MarkerInfo> markers = new ArrayList<>();
		Set<DatabaseMultipoly> allocatedShapes = new HashSet<>();
		Set<MarkerInfo> allocatedMarkers = new HashSet<>();
		List<ShapeData> shapeData = new ArrayList<>();
		
		BufferedImage gImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		LibkokiUtils.getShapes(selectedFile, (Graphics2D)gImage.getGraphics(), database, markers, allocatedShapes, allocatedMarkers, shapeData);
		
		this.setTitle("Correction Screen");
		this.panel = new CorrectionPanel(image);
		this.add(panel);
		this.panel.init();
		
		Timer paintTimer = new Timer();
		paintTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				XMLCorrection.this.repaint();
			}
		}, 0, 16);
		
		this.setSize(800, 600);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		this.panel.repaint();
	}
	
}
