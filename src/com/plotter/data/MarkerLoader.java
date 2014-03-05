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
	public static final int MARKER_WIDTH = 197; // 57 for 15 mil   38 for 10 mil
	public static final int MARKER_HEIGHT = 197; // 57 for 15 mil  38 for 10 mil
	
	private static final int WIDTH_GAP = 1;
	private static final int HEIGHT_GAP = 1;
	
	public static List<BufferedImage> getMarkers(List<Integer> markerIds, List<Double> rotations) throws IOException {
		
		List<BufferedImage> markers = new ArrayList<>();
		
		BufferedImage spriteSheet = ImageIO.read(new File(MARKER_SHEET));
		
		for(int i = 0; i < markerIds.size(); i++) {
			
			int x = 0;
			int y = 0;
			
			Integer markerId = markerIds.get(i);
			
			// Special case 138 doesn't get read
			if(markerId == 138)
				markerId = 137;
			
			for(int j = 0; j < markerId; j++) {
				
				if(j == 23 || j == 110 || j == 127 || j == 131 || j == 134 || j == 137 || j == 151 || j == 217) {
					continue;
				}
				
				if(j < markerId - 1) {
					y += MARKER_WIDTH;
					
					if(y + MARKER_WIDTH > spriteSheet.getHeight()) {
						x += MARKER_HEIGHT;
						y = 0;
					}
				}
			}
			
			BufferedImage subimage = spriteSheet.getSubimage(x, y, MARKER_WIDTH, MARKER_WIDTH);
			
			BufferedImage rotatedImage = new BufferedImage(subimage.getWidth(), subimage.getHeight(), subimage.getType());
			
			AffineTransform rotation = new AffineTransform();
			rotation.rotate(rotations.get(i), subimage.getWidth() / 2, subimage.getHeight() / 2); 
			
			Graphics2D graphics = (Graphics2D) rotatedImage.getGraphics();
			graphics.drawImage(subimage, rotation, null);
			
			markers.add(rotatedImage);
			
		}

		return markers;
		
	}
	
	public static void main(String[] args) throws IOException {
		BufferedImage spriteSheet = ImageIO.read(new File(MARKER_SHEET));
		Graphics graphics = spriteSheet.getGraphics();
		
		int[] markerIds = new int[]{1,2,3,4,5,16,123,214};
		
		for(int i = 0; i < markerIds.length; i++) {
			
			int x = 0;
			int y = 0;
			
			for(int j = 0; j < markerIds[i]; j++) {
				
				if(j == 23 || j == 110 || j == 127 || j == 131 || j == 134 || j == 137 || j == 151 || j == 217) {
					continue;
				}
				
				if(j < markerIds[i] - 1) {
					y += MARKER_WIDTH;
					
					if(y + MARKER_WIDTH > spriteSheet.getHeight()) {
						x += MARKER_HEIGHT;
						y = 0;
					}
				}
			}
			
//			BufferedImage subimage = spriteSheet.getSubimage(x, y, MARKER_WIDTH, MARKER_WIDTH);
			graphics.setColor(Color.red);
			graphics.drawRect(x, y, MARKER_HEIGHT, MARKER_HEIGHT);
			
		}
		
		ImageIO.write(spriteSheet, "png", new File(MARKER_SHEET));
	}
	
	// Draw out a database
//	public static void main(String[] args) throws IOException, CloneNotSupportedException {
//		JFileChooser databaseChoose = new JFileChooser();
//		int showOpenDialog = databaseChoose.showOpenDialog(null);
//		
//		if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
//			Database database = Database.loadDatabase(databaseChoose.getSelectedFile());
//			
//			Collection<Entry<List<Integer>, DatabaseMultipoly>> entries = database.markersToShape.entrySet();
//			
//			BufferedImage spriteSheet = new BufferedImage(5000, 4000, BufferedImage.TYPE_INT_ARGB);
//			Graphics2D graphics = (Graphics2D) spriteSheet.getGraphics();
//			graphics.setColor(Color.black);
//			graphics.fillRect(0, 0, 5000, 4000);
//			
//			int x = 20;
//			int y = 20;
//			
//			for(Entry<List<Integer>, DatabaseMultipoly> entry:entries) {
//				
//				DatabaseMultipoly mP = entry.getValue();
//				
//				if(y + mP.getMergedPolygon().getBounds2D().getHeight() >= spriteSheet.getHeight()) {
//					y = 20; x += mP.getMergedPolygon().getBounds2D().getWidth() * 2;
//				}
//				
//				graphics.setColor(Color.white);
//				graphics.drawString(""+entry.getKey(), x, y );
//				graphics.fillOval(x - 5, y - 5, 10, 10);
//				
//				MultiPoly polygonCopy = (MultiPoly) mP.clone();
//				List<Edge> hairlines = polygonCopy .getMergedLines().getHairlines();
//				for(Edge edge:hairlines) {
//					graphics.setColor(Color.orange);
//					graphics.setStroke(new BasicStroke(3));
//					graphics.drawLine((int)(x + edge.end1.x),
//									  (int)(y + edge.end1.y),
//									  (int)(x + edge.end2.x),
//									  (int)(y + edge.end2.y));
//					graphics.setColor(Color.black);
//					graphics.setStroke(new BasicStroke(1));
//					graphics.drawLine((int)(x + edge.end1.x),
//									  (int)(y + edge.end1.y),
//									  (int)(x + edge.end2.x),
//									  (int)(y + edge.end2.y));
//				}
//				
//				y += mP.getMergedPolygon().getBounds2D().getHeight() + 100;
//				
//			}
//			
//			ImageIO.write(spriteSheet, "png", new File("Database"));
//		}
//	}
	
}
