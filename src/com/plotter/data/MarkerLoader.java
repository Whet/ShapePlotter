package com.plotter.data;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class MarkerLoader {

	private static final String MARKER_SHEET = "spritesheets/15mill.png";
	private static final int MARKER_WIDTH = 57;
	private static final int MARKER_HEIGHT = 57;
	
	private static final int WIDTH_GAP = 1;
	private static final int HEIGHT_GAP = 1;
	
	private static final int COLUMNS = 12;
	private static final int ROWS = 18;
	
	public static List<BufferedImage> getMarkers(int numberOfMarkers) throws IOException {
		
		List<BufferedImage> markers = new ArrayList<>();
		
		BufferedImage spriteSheet = ImageIO.read(new File(MARKER_SHEET));
		
		// Debug
//		Graphics graphics = spriteSheet.getGraphics();
//		graphics.setColor(Color.red);
		
		int x = WIDTH_GAP;
		int y = HEIGHT_GAP;
		
		for(int i = 0; i < numberOfMarkers; i++) {
			markers.add(spriteSheet.getSubimage(x, y, MARKER_WIDTH, MARKER_WIDTH));
			
			// Debug
//			graphics.drawRect(x, y, MARKER_WIDTH, MARKER_WIDTH);
			
			x += MARKER_WIDTH + WIDTH_GAP * 2;
			
			if(x + MARKER_WIDTH > spriteSheet.getWidth()) {
				y += MARKER_HEIGHT + HEIGHT_GAP * 2;
				x = WIDTH_GAP;
			}
		}
		
		// Debug
//		ImageIO.write(spriteSheet, "png", new File("spritesheets/test.png"));
		
		return markers;
		
	}
	
	public static void main(String[] args) {
		try {
			getMarkers(216);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
