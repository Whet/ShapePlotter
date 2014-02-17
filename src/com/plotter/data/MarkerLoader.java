package com.plotter.data;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class MarkerLoader {

	private static final String MARKER_SHEET = "spritesheets/15mill.png";
	public static final int MARKER_WIDTH = 57;
	public static final int MARKER_HEIGHT = 57;
	
	private static final int WIDTH_GAP = 1;
	private static final int HEIGHT_GAP = 1;
	
	private static final int COLUMNS = 12;
	private static final int ROWS = 18;
	
	public static List<BufferedImage> getMarkers(int numberOfMarkers, List<Integer> rotations) throws IOException {
		
		List<BufferedImage> markers = new ArrayList<>();
		
		BufferedImage spriteSheet = ImageIO.read(new File(MARKER_SHEET));
		
		int x = WIDTH_GAP;
		int y = HEIGHT_GAP;
		
		for(int i = 0; i < numberOfMarkers; i++) {
			BufferedImage subimage = spriteSheet.getSubimage(x, y, MARKER_WIDTH, MARKER_WIDTH);
			
			BufferedImage rotatedImage = new BufferedImage(subimage.getWidth(), subimage.getHeight(), subimage.getType());
			
			AffineTransform rotation = new AffineTransform();
			rotation.rotate(Math.PI / 2 * rotations.get(i), subimage.getWidth() / 2, subimage.getHeight() / 2); 
			
			Graphics2D graphics = (Graphics2D) rotatedImage.getGraphics();
			graphics.drawImage(subimage, rotation, null);
			
			markers.add(rotatedImage);
			
			y += MARKER_WIDTH + WIDTH_GAP * 2;
			
			if(y + MARKER_WIDTH > spriteSheet.getHeight()) {
				x += MARKER_HEIGHT + HEIGHT_GAP * 2;
				y = WIDTH_GAP;
			}
		}

		return markers;
		
	}
	
}
