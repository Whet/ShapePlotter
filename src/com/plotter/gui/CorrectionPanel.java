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
	private int currentFlavour;
	private final BufferedImage image;
	
	public CorrectionPanel(BufferedImage image) {
		
		this.image = image;
		
		this.panX = 0;
		this.panY = 0;
		
		this.currentFlavour = 0;
		
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

				// Left Mouse
				if(e.getButton() == 1) {
				}
				
				// Right Mouse
				else if(e.getButton() == 3) {
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
					// 0
					case 48:
						currentFlavour = 0;
					break;
					// 1
					case 49:
						currentFlavour = 1;
					break;
					// 2
					case 50:
						currentFlavour = 2;
					break;
					// 3
					case 51:
						currentFlavour = 3;
					break;
					// 4
					case 52:
						currentFlavour = 4;
					break;
					// 5
					case 53:
						currentFlavour = 5;
					break;
					// 6
					case 54:
						currentFlavour = 6;
					break;
					// 7
					case 55:
						currentFlavour = 7;
					break;
					// 8
					case 56:
						currentFlavour = 8;
					break;
					// 9
					case 57:
						currentFlavour = 9;
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
		
		
		
	}
	
}
