package com.plotter.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import com.plotter.xmlcorrection.MarkerData;
import com.plotter.xmlcorrection.MarkerGroup;
import com.plotter.xmlcorrection.XMLCorrectionData;

public class CorrectionPanel extends JPanel {

	// Shape drawing variables
	private static final Color POLYGON_COLOUR = new Color(244,123,34);
	private static final Color VERTEX_COLOUR = new Color(255,154,81);
	private static final Color BINDING_POINT_COLOUR = new Color(243,40,42);
	
	// Mouse variables
	private int panX, panY;
	private boolean mousePanning;
	private Point oldPoint;
	private Point newPoint;
	private int[] panAtClick;
	
	private boolean shiftDown;
	private final BufferedImage image;
	
	private XMLCorrectionData data;
	
	public CorrectionPanel(XMLCorrectionData data, BufferedImage image) {
		
		this.data = data;
		this.image = image;
		
		this.panX = 0;
		this.panY = 0;
		
		this.setPreferredSize(new Dimension(800, 600));
		
	}
	
	public void init() {
		
		this.addMouseControl();
		this.addKeyboardControl();
		this.setVisible(true);
		
	}

	private void addMouseControl() {
		this.addMouseListener(new MouseAdapter() {
			
			private int connectXDown, connectYDown;
			
			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				CorrectionPanel.this.requestFocusInWindow();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {

				// Middle Mouse
				if(e.getButton() == 2) {
					if(!mousePanning) {
						mousePanning = true;
						oldPoint = e.getPoint();
						panAtClick = new int[]{panX, panY};
					}
				}
				
				// Left Mouse
				else if(e.getButton() == 1) {
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {

				Point transPoint = new Point(e.getPoint());
				transPoint.translate(-panX, -panY);
				
				// Left Mouse
				if(e.getButton() == 1) {
					data.mD(e.getPoint(), transPoint, shiftDown);
				}
				
				// Right Mouse
				else if(e.getButton() == 3) {
					
					data.rMD(e.getPoint(), transPoint, shiftDown);
				}

				// Middle Mouse
				else if(e.getButton() == 2) {
					mousePanning = false;
				}
				
			}

		});
		
		Timer updTimer = new Timer();

		updTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				newPoint = CorrectionPanel.this.getMousePosition();
				CorrectionPanel.this.repaint();
				
				if(!mousePanning || panAtClick == null || CorrectionPanel.this.getMousePosition() == null)
					return;
				
				panX = panAtClick[0];
				panY = panAtClick[1];
				
				panX += newPoint.x - oldPoint.x;
				panY += newPoint.y - oldPoint.y;
			}
		}, 0, 20);
	}
	
	private void addKeyboardControl() {
		this.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				switch(e.getKeyCode()) {
					// SHIFT
					case 16:
						shiftDown = true;
					break;
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()) {
					// SHIFT
					case 16:
						shiftDown = false;
					break;
					// 0
					case 48:
						data.setSelectionMode(0);
					break;
					// 1
					case 49:
						data.setSelectionMode(1);
					break;
				}
			}
		});
	}
	
	@Override
	public void paint(Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(Color.black);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		AffineTransform panTransform = new AffineTransform();
		panTransform.translate(panX, panY);
		
		g.setColor(Color.cyan);
		
		g2.fillRect(panX, panY, image.getWidth(), image.getHeight());
		
		g2.drawImage(image, panTransform, null);
		
		drawData(g2);
		
	}
	
	private void drawData(Graphics2D g) {
		
		AffineTransform panTransform = new AffineTransform();
		panTransform.translate(panX, panY);
		
		g.setTransform(panTransform);
		
		for(MarkerData marker:data.getMarkers()) {
			if(data.isSelected(marker))
				g.setColor(Color.orange);
			else
				g.setColor(Color.green.darker());
			
			g.fillRect(marker.getLocation().x - 20, marker.getLocation().y - 20, 40, 40);
			
			g.setColor(Color.white);
			g.drawString("ID " + marker.getMarkerNumber(), marker.getLocation().x - 20, marker.getLocation().y);
		}
		
		// Showing groups of markers
		if(data.getSelectionMode() == 1) {
			// Draw shape and centre of markers
			for(MarkerGroup group:data.getMarkerGroups()) {
				
				if(data.isSelected(group))
					g.setColor(Color.orange);
				else
					g.setColor(Color.red.darker());
				
				for(MarkerData marker:group.getMarkers()) {
					g.drawLine(marker.getLocation().x, marker.getLocation().y,
							   group.getCentre().x, group.getCentre().y);
				}
				
				g.drawOval(group.getCentre().x - 10, group.getCentre().y - 10, 20, 20);
			}
		}
	}
	
}
