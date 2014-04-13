package com.plotter.data;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class MarkerLoader {

	private static final String MARKER_SHEET = "spritesheets/198Square.png";
	public static final int MARKER_DIMENSION = 197; // 57 for 15 mil   38 for 10 mil
	
	private static final int WIDTH_GAP = 1;
	private static final int HEIGHT_GAP = 1;
	
	public static List<BufferedImage> getMarkers(List<Integer> markerIds, List<Double> rotations) throws IOException {
		
		List<BufferedImage> markers = new ArrayList<>();
		
		BufferedImage spriteSheet = ImageIO.read(new File(MARKER_SHEET));
		
		for(int i = 0; i < markerIds.size(); i++) {
			
			Integer markerId = markerIds.get(i);
			
			int x = 0;
			int y = 0;
			
			for(int j = 0; j < markerId; j++) {
			
				if(j == 23 || j == 110 || j == 127 ||
				   j == 131 || j == 134 || j == 137 ||
				   j == 151 || j == 217) {
					continue;
				}
				
				if(j < markerId - 1) {
					y += MARKER_DIMENSION;
					
					if(y + MARKER_DIMENSION > spriteSheet.getHeight()) {
						x += MARKER_DIMENSION;
						y = 0;
					}
				}
			}
			
			if(markerId == 24 || markerId == 111 || markerId == 128 ||
			   markerId == 132 || markerId == 135 || markerId == 138 ||
			   markerId == 152 || markerId == 218) {
				y -= MARKER_DIMENSION;
			}
			
			BufferedImage subimage = spriteSheet.getSubimage(x, y, MARKER_DIMENSION, MARKER_DIMENSION);
			
			BufferedImage rotatedImage = new BufferedImage(subimage.getWidth(), subimage.getHeight(), subimage.getType());
			
			AffineTransform rotation = new AffineTransform();
			rotation.rotate(rotations.get(i), subimage.getWidth() / 2, subimage.getHeight() / 2); 
			
			Graphics2D graphics = (Graphics2D) rotatedImage.getGraphics();
			graphics.drawImage(subimage, rotation, null);
			
			markers.add(rotatedImage);
			
		}

		return markers;
		
	}
	
//	public static void main(String[] args) throws IOException {
//		BufferedImage spriteSheet = ImageIO.read(new File(MARKER_SHEET));
//		Graphics graphics = spriteSheet.getGraphics();
//		
//		Integer markerId = 1;
//		
//		for(int i = 0; i < 216; i++) {
//			
//			int x = 0;
//			int y = 0;
//			
//			for(int j = 0; j < markerId; j++) {
//			
//				if(j == 23 || j == 110 || j == 127 ||
//				   j == 131 || j == 134 || j == 137 ||
//				   j == 151 || j == 217) {
//					continue;
//				}
//				
//				if(j < markerId - 1) {
//					y += MARKER_DIMENSION;
//					
//					if(y + MARKER_DIMENSION > spriteSheet.getHeight()) {
//						x += MARKER_DIMENSION;
//						y = 0;
//					}
//				}
//			}
//			
//			if(markerId == 24 || markerId == 111 || markerId == 128 ||
//			   markerId == 132 || markerId == 135 || markerId == 138 ||
//			   markerId == 152 || markerId == 218) {
//				y -= MARKER_DIMENSION;
//			}
//			
////			BufferedImage subimage = spriteSheet.getSubimage(x, y, MARKER_WIDTH, MARKER_WIDTH);
//			graphics.setColor(Color.red);
//			graphics.drawRect(x, y, MARKER_DIMENSION, MARKER_DIMENSION);
//			graphics.drawString("M" + markerId, x, y + MARKER_DIMENSION);
//			markerId++;
//			
//			// Debug code
//			while(markerId == 23 || markerId == 110 || markerId == 127 ||
//				  markerId == 131 || markerId == 134 || markerId == 137 ||
//				  markerId == 151 || markerId == 217) {
//				markerId++;
//			}
//			
//		}
//		
//		ImageIO.write(spriteSheet, "png", new File(MARKER_SHEET));
//	}
	
	
}