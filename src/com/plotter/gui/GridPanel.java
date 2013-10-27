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
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import com.plotter.data.Maths;
import com.plotter.data.ModulePolygon;

public class GridPanel extends JPanel {

	// Grid drawing variables
	private static final int BRIGHTEST_LINE_GAP = 10;
	private static final Color BRIGHTEST_LINE_COLOUR = new Color(0, 60, 60);
	
	private static final int BRIGHT_LINE_GAP = 5;
	private static final Color BRIGHT_LINE_COLOUR = new Color(0, 120, 120);
	private static final Color NORMAL_LINE_COLOUR = new Color(0, 30, 30);;
	private static final Color CROSSHAIR_COLOUR = Color.yellow;
	
	private static final int CROSSHAIR_LENGTH = 10;
	public static final int GRID_SIZE = 40;
	
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
	
	// The shape which is being drawn
	private ModulePolygon modulePolygon;
	
	public GridPanel(ModulePolygon modulePolygon) {
		
		this.modulePolygon = modulePolygon;
		
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
				GridPanel.this.requestFocusInWindow();
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
					Point gridPoint = gridPoint(e.getPoint());
					
					if(shiftDown) {
						connectXDown = gridPoint.x;
						connectYDown = gridPoint.y;
					}
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {

				// Left Mouse
				if(e.getButton() == 1) {
					Point gridPoint = gridPoint(e.getPoint());
					
					if(shiftDown) {
						Point opposite = oppositePoint(connectXDown, connectYDown, gridPoint.x, gridPoint.y);
						Point oppositeGrid = gridPoint(opposite);
						
						modulePolygon.addConnectPoint(connectXDown, connectYDown, gridPoint.x, gridPoint.y, oppositeGrid.x, oppositeGrid.y);
					}
					else
						modulePolygon.addPoint(gridPoint.x, gridPoint.y);
				}
				
				// Right Mouse
				else if(e.getButton() == 3) {
					Point gridPoint = gridPoint(e.getPoint());
					
					if(shiftDown)
						modulePolygon.removeConnectPoint(gridPoint.x, gridPoint.y);
					else
						modulePolygon.removePoint(gridPoint.x, gridPoint.y);
				}

				// Middle Mouse
				else if(e.getButton() == 2) {
					mousePanning = false;
				}
				
			}

			private Point oppositePoint(int x, int y, int x1, int y1) {
				double angle = Maths.getRads(x, y, x1, y1);
				angle += Math.PI;
				double distance = Maths.getDistance(x, y, x1, y1);
				
				// x, y is the start point; x1 y1 is an inside/outside point
				
				return new Point(x += Math.cos(angle) * distance, y += Math.sin(angle) * distance);
			}
			
		});
		
		Timer updTimer = new Timer();

		updTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				newPoint = GridPanel.this.getMousePosition();
				GridPanel.this.repaint();
				
				if(!mousePanning || panAtClick == null || GridPanel.this.getMousePosition() == null)
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
				}
			}
		});
	}
	
	@Override
	public void paint(Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(Color.black);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		drawGrid(g);
		
		g.setColor(Color.cyan);
		
		drawPolygon(g2);
		
//		if(newPoint != null) {
//			g.setColor(Color.white);
//			Point gridPoint = gridPoint(newPoint);
//			g.drawString("[" + gridPoint.x + "," + gridPoint.y + "]", 30, 60);
//		}
	}

	private void drawGrid(Graphics g) {
		int brightLineCounter = -BRIGHTEST_LINE_GAP / 2;
		
		// Stop panning to region where grid doesn't draw correctly
		if(panX > 0)
			panX = 0;
		if(panY > 0)
			panY = 0;
		
		int paintBrush = panX;
		
		// Draw grid
		
		while(paintBrush < this.getWidth()){
			
			if(brightLineCounter % BRIGHTEST_LINE_GAP == 0){
				g.setColor(BRIGHTEST_LINE_COLOUR);
			}
			else if(brightLineCounter % BRIGHT_LINE_GAP == 0){
				g.setColor(BRIGHT_LINE_COLOUR);
			}
			else{
				g.setColor(NORMAL_LINE_COLOUR);
			}
			
			g.drawLine(paintBrush, 0, paintBrush, this.getHeight());
			paintBrush += GRID_SIZE;
			brightLineCounter++;
		}
		
		brightLineCounter = -BRIGHTEST_LINE_GAP / 2;
		
		paintBrush = panY;
		while(paintBrush < this.getHeight()){
			
			if(brightLineCounter % BRIGHTEST_LINE_GAP == 0){
				g.setColor(BRIGHTEST_LINE_COLOUR);
			}
			else if(brightLineCounter % BRIGHT_LINE_GAP == 0){
				g.setColor(BRIGHT_LINE_COLOUR);
			}
			else{
				g.setColor(NORMAL_LINE_COLOUR);
			}
			
			g.drawLine(0, paintBrush, this.getWidth(), paintBrush);
			paintBrush += GRID_SIZE;
			brightLineCounter++;
		}
		
		// Draw crosshair
		if(newPoint == null)
			return;
		
		g.setColor(CROSSHAIR_COLOUR);
		Point mouse = gridPoint(newPoint);
		g.drawLine(mouse.x + panX,
						   mouse.y - CROSSHAIR_LENGTH + panY,
						   mouse.x + panX, 
						   mouse.y + CROSSHAIR_LENGTH + panY);
		g.drawLine(mouse.x - CROSSHAIR_LENGTH + panX,
						   mouse.y + panY,
						   mouse.x + CROSSHAIR_LENGTH + panX, 
						   mouse.y + panY);
	}
	
	private void drawPolygon(Graphics2D g) {

		g.setColor(VERTEX_COLOUR);
		int[][] points = this.modulePolygon.getPoints().toArray(new int[this.modulePolygon.getPoints().size()][2]);
		
		for(int i = 0; i < points.length; i++) {
			g.drawOval(points[i][0] - 3 + panX, points[i][1] - 3 + panY, 6, 6);
		}
		
		g.setColor(POLYGON_COLOUR);
		g.setTransform(new AffineTransform(){
			
			{
				this.translate(panX, panY);
			}
		
		});
		g.drawPolygon(this.modulePolygon.getPolygon());
		g.setTransform(new AffineTransform());
		
		g.setColor(BINDING_POINT_COLOUR);
		points = this.modulePolygon.getConnectPointsInts().toArray(new int[this.modulePolygon.getConnectPoints().size()][2]);
		
		for(int i = 0; i < points.length; i++) {
			g.fillOval(points[i][0] - 3 + panX, points[i][1] - 3 + panY, 6, 6);
			g.fillOval(points[i][2] - 3 + panX, points[i][3] - 3 + panY, 6, 6);
			g.fillOval(points[i][4] - 3 + panX, points[i][5] - 3 + panY, 6, 6);
			g.drawLine(points[i][2] + panX, points[i][3] + panY, points[i][4] + panX, points[i][5] + panY);
		}
		
		
	}
	
	// Return the nearest grid intersection to a point
	private Point gridPoint(Point point){
		int[] moddedMouseLocation = new int[2];
		moddedMouseLocation[0] = (int) ( Math.round( ((point.x) - (panX) ) / (float)(GRID_SIZE) ) * (GRID_SIZE) );
		moddedMouseLocation[1] = (int) ( Math.round( ((point.y) - (panY) ) / (float)(GRID_SIZE) ) * (GRID_SIZE) );
		
		return new Point(moddedMouseLocation[0],moddedMouseLocation[1]);
	}
	
}
