package com.plotter.algorithms;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.plotter.data.Maths;
import com.plotter.gui.GridPanel;

public class TetrisSolution {

	private static final int LOOK_AHEAD = 3;
	
	// Stores best lines on a grid
	// No real measurements, must be scaled on svg output
	public static TetrisSolution getSolution(int width, int height, List<MultiPoly> shapes) {
		System.out.println();
		// Convert all possible shapes to unit polygons
		List<TetrisPiece> shrunkPolygons = new ArrayList<TetrisPiece>();
		
		for(MultiPoly mPoly:shapes) {
			shrunkPolygons.add(new TetrisPiece(mPoly));
		}

		// Shuffle list to have random sequence
		Collections.shuffle(shrunkPolygons);
		
		Queue<TetrisPiece> incomingBlocks = new ArrayBlockingQueue<>(5);
		incomingBlocks.addAll(shrunkPolygons);

		TetrisStage firstStage = new TetrisStage(incomingBlocks, width, height);
		
		TetrisStage bestStage = firstStage;
		
		while(true) {
			
			TetrisStage potentialBest = bestStage.lookAhead();
			
			if(potentialBest == bestStage)
				break;
			else
				bestStage = potentialBest;
			
		}
		
		return new TetrisSolution(bestStage);
	}
	
	private List<TetrisPiece> solutionPieces;
	
	public TetrisSolution(TetrisStage bestStage) {
		this.solutionPieces = new ArrayList<>();
		
		TetrisStage tetrisPiece = bestStage.parent;
		
		while(tetrisPiece.parent != null) {
			solutionPieces.add(tetrisPiece.bestPosition());
			
			tetrisPiece = tetrisPiece.parent;
		}
	}
	
	public List<TetrisPiece> getSolutionPieces() {
		return solutionPieces;
	}

	private static class TetrisStage {
		
		public double score;
		
		private Queue<TetrisPiece> incomingBlocks;
		private Queue<TetrisStage> blockChoices;
		
		private TetrisPiece placingBlock;
		
		private TetrisStage parent;
		private TetrisPiece parentBlock;
		private TetrisGrid grid;
		
		public TetrisStage(Queue<TetrisPiece> incomingBlocks, int width, int height) {
			
			this.incomingBlocks = incomingBlocks;
			
			// Remove next block to be placed and put it at the end
			this.placingBlock = incomingBlocks.poll();
			this.incomingBlocks.add(placingBlock);
			
			this.blockChoices = new PriorityQueue<TetrisStage>(10, new StageComp());
			
			this.parent = null;
			this.parentBlock = null;
			this.grid = new TetrisGrid(width, height);
			this.score = 0;
		}
		
		public TetrisStage(TetrisStage parentStage, TetrisPiece parentBlock) {
			
			this.incomingBlocks = parentStage.incomingBlocks;

			// Remove next block to be placed and put it at the end
			this.placingBlock = incomingBlocks.poll();
			this.incomingBlocks.add(placingBlock);
			
			this.blockChoices = new PriorityQueue<TetrisStage>(10, new StageComp());
			
			this.parent = parentStage;
			this.parentBlock = parentBlock;
			this.grid = new TetrisGrid(parentStage.grid);
			this.grid.addPiece(parentBlock);
			this.computeScore();
		}

		private TetrisPiece bestPosition() {
			return this.blockChoices.peek().parentBlock;
		}
		
		public TetrisStage lookAhead() {
			return lookAhead(TetrisSolution.LOOK_AHEAD);
		}
		
		public TetrisStage lookAhead(int lookAhead) {
			this.computeAllChoices();
			
			Queue<TetrisStage> leaves = new PriorityQueue<>(5, new StageComp());
			
			if(this.blockChoices.peek() == null) {
				return this;
			}
			else if(lookAhead > 0) {
				for(TetrisStage stage:this.blockChoices) {
					leaves.add(stage.lookAhead(lookAhead - 1));
				}
			}
			else {
				return this.blockChoices.peek();
			}
			
			return leaves.peek();
		}
		
		private void computeAllChoices() {
			// Take the placing block
			// Try all possible translations
			// AddChoice(position)
			
			for(int[] possibleLocation:this.grid.getTopLeftCorners()) {
				
				TetrisPiece testPiece = this.placingBlock.copy();
				testPiece.translate(possibleLocation[0], possibleLocation[1]);
				
				if(this.grid.isPieceValid(testPiece)) {
					this.addChoice(testPiece);
				}
			}
			
		}
		
		private void addChoice(TetrisPiece positionedBlock) {
			this.blockChoices.add(new TetrisStage(this, positionedBlock));
		}
		
		private void computeScore() {
			this.score = 0;
			
			int highestY = 0;
			int fullBlocks = 0;
			int fullBlocksXHeight = 0;
			int largestSlope = 0;
			int totalSlopes = 0;
			
			for(int i = 0; i < this.grid.blocks.length; i++) {
				for(int j = 0; j < this.grid.blocks[i].length; j++) {
					
					int gridCode = this.grid.blocks[i][j];
					
					// Detect highest point on grid
					if(j > highestY && gridCode == -1)
						highestY = j;
					
					// Measure fullness
					if(gridCode == -1) {
						fullBlocks++;
						
						fullBlocksXHeight += j;
					}
					
				}
			}
			
			this.score += highestY;
			this.score += fullBlocks;
			this.score += fullBlocksXHeight;
			this.score += largestSlope;
			this.score += totalSlopes;
		}
		
	}
	
	public static class TetrisPiece {
		
		// Copies polygons and make them unit polygons
		public List<Polygon> polygons;
		
		public TetrisPiece(MultiPoly mPoly) {
			
			polygons = new ArrayList<>();
			
			for(Polygon poly:mPoly.getPolygons()) {
				Polygon nPoly = new Polygon();
				
				for(int i = 0; i < poly.npoints; i++) {
					nPoly.addPoint(Maths.round(poly.xpoints[i] - mPoly.getMergedPolygon().getBounds2D().getMinX(), GridPanel.GRID_SIZE) / GridPanel.GRID_SIZE,
								   Maths.round(poly.ypoints[i] - mPoly.getMergedPolygon().getBounds2D().getMinY(), GridPanel.GRID_SIZE) / GridPanel.GRID_SIZE);
				}
				
				polygons.add(nPoly);
			}
		}
		
		private TetrisPiece(){
			this.polygons = new ArrayList<>();
		}

		public TetrisPiece copy() {
			TetrisPiece t = new TetrisPiece();
			
			for(Polygon polygon:this.polygons) {
				t.polygons.add(new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints));
			}
			
			return t;
		}
		
		public void translate(int deltaX, int deltaY) {
			for(int i = 0; i < this.polygons.size(); i++) {
				this.polygons.get(i).translate(deltaX, deltaY);
			}
		}
		
	}
	
	private static class TetrisGrid {
		
		/*
		 * 0 - No Block
		 * -1 - Block
		 * 1 - Block can be placed
		 * 
		 * 0,0 is the top left block
		 */
		private int[][] blocks;
		
		private int tallestY;
		
		public TetrisGrid(int width, int height) {
			this.blocks = new int[width][height];
			this.tallestY = 0;
			
			// Make the first row 1's
			for(int i = 0; i < width; i++) {
				blocks[i][0] = 1;
			}
		}
		
		public TetrisGrid(TetrisGrid parentGrid) {
			this.blocks = new int[parentGrid.blocks.length][parentGrid.blocks[0].length];
			this.tallestY = parentGrid.tallestY;
		}

		public boolean isPieceValid(TetrisPiece piece) {
			
			// Block must be sat ontop of another block to be valid
			boolean anchorFound = false;
			
			for(Polygon polygon:piece.polygons) {
				
				// Top left corner
				int widthIndex = (int) polygon.getBounds2D().getMinX();
				int heightIndex = (int) polygon.getBounds2D().getMinY();
				int width = (int) polygon.getBounds2D().getWidth();
				int height = (int) polygon.getBounds2D().getHeight();
				
				// Check all grid points in the bounds to see if the polygon contains the centre
				// - 1's since we don't want to go over the boundary when we add + 0.5
				for(int i = widthIndex; i < widthIndex + width - 1; i++) {
					for(int j = heightIndex; j < heightIndex + height - 1; j++) {
						if(i + width <= this.blocks.length && j + height <= this.blocks[0].length && polygon.contains(i + 0.5, j + 0.5)) {
							
							// Overlaps with a non-empty or non-anchor block
							if(blocks[i][j] != 0 && blocks[i][j] != 1) {
								return false;
							}
							// Overlaps with anchor
							else if(blocks[i][j] == 1) {
								anchorFound = true;
							}
						}
					}
				}
			}
			
			return anchorFound;
		}
		
		public void addPiece(TetrisPiece piece) {
			
			for(Polygon polygon:piece.polygons) {
				
				// Top left corner
				int widthIndex = (int) polygon.getBounds2D().getMinX();
				int heightIndex = (int) polygon.getBounds2D().getMinY();
				int width = (int) polygon.getBounds2D().getWidth();
				int height = (int) polygon.getBounds2D().getHeight();
				
				// Check all grid points in the bounds to see if the polygon contains the centre
				for(int i = widthIndex; i < widthIndex + width; i++) {
					for(int j = heightIndex; j < heightIndex + height; j++) {
						if(polygon.contains(i + 0.5, j + 0.5)) {
							// Set the selected block to being used: -1
							blocks[i][j] = -1;
							
							// Set the block one down to being a possible building point: 1
							// This may get overwritten but it is inconsequential
							if(j < blocks[i].length - 1) {
								blocks[i][j + 1] = 1;
							}
							
							// Set new tallest point
							if(j > tallestY)
								this.tallestY = j;
						}
					}
				}
			}
			
			// DEBUG
			drawGrid();
		}
		
		private void drawGrid() {
			for(int i = 0; i < this.blocks.length; i++) {
				for(int j = 0; j < this.blocks[i].length; j++) {
					System.out.print(this.blocks[i][j] + 1);
				}
				System.out.println();
			}
			
			for(int i = 0; i < this.blocks.length; i++) {
				System.out.print("-");
			}
			System.out.println();
		}

		public List<int[]> getTopLeftCorners() {
			// Get all 1's 
			List<int[]> locations = new ArrayList<>();
			
			for(int i = 0; i < this.blocks.length; i++) {
				for(int j = 0; j < tallestY + 1; j++) {
					if(j < this.blocks[i].length && this.blocks[i][j] == 1) {
						locations.add(new int[]{i, j});
					}
					
				}
			}
			
			return locations;
		}
	}
	
	private static class StageComp implements Comparator<TetrisStage> {

		@Override
		public int compare(TetrisStage t1, TetrisStage t2) {
			
			if(t1.score < t2.score)
				return 1;
			else if(t2.score < t1.score)
				return -1;
			
			return 0;
		}
		
	}
	
}
