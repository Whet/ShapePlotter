package com.plotter.algorithms;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import com.plotter.data.Maths;
import com.plotter.gui.GridPanel;

public class TetrisSolution {

	// Heuristics
	private static final int H_HIGHEST_Y = 1;
	private static final int H_MAX_SLOPE = 0;
	private static final int H_FULLNESS = 1;
	private static final int H_FULL_HEIGHT = 1;
	private static final int H_TOTAL_SLOPE = 0;
	private static final int H_HOLES = 0;
	
	// Genetic Algorithm
	private static final int LOOK_AHEAD = 1;
	
	private static final int GENERATIONS = 10;
	
	private static final int MAX_PARENTS = 3;
	private static final int MAX_CHILDREN = 5;
	private static final int MAX_MUTATIONS = GENERATIONS + 2;
	
	
	// Stores best lines on a grid
	// No real measurements, must be scaled on svg output
	public static TetrisSolution getSolution(int width, int height, List<MultiPoly> shapes) {

		// Convert all possible shapes to unit polygons
		List<TetrisPiece> shrunkPolygons = new ArrayList<TetrisPiece>();
		
		for(MultiPoly mPoly:shapes) {
			shrunkPolygons.add(new TetrisPiece(mPoly));
		}

		// Genetic Algorithm
		TetrisStage bestStage = geneticTetris(shrunkPolygons, width, height, MAX_CHILDREN, GENERATIONS);
		
		return new TetrisSolution(bestStage);
	}
	
	private static TetrisStage geneticTetris(List<TetrisPiece> polygons, int width, int height, int maxChildren, int iterations) {
		
		Queue<GeneticStub> geneticQueue = new PriorityQueue<>(maxChildren, new GeneticComp());
		
		// Create starting population
		
		System.out.println("Creating Population!");
		
		for(int i = 0; i < maxChildren; i++) {
		
			// Shuffle list to have random sequence start
			Collections.shuffle(polygons);
			
			Queue<TetrisPiece> incomingBlocks = new ArrayBlockingQueue<>(polygons.size());
			Queue<TetrisPiece> incomingBlocksMem = new ArrayBlockingQueue<>(polygons.size());
			incomingBlocks.addAll(polygons);
			incomingBlocksMem.addAll(polygons);
			
			TetrisStage firstStage = new TetrisStage(incomingBlocks, width, height);
			
			TetrisStage bestStage = firstStage;
			
			while(true) {
				
				TetrisStage potentialBest = bestStage.lookAhead();
				
				// Break when no further stages found (Can't find somewhere to put next block)
				if(potentialBest == bestStage)
					break;
				else
					bestStage = potentialBest;
				
			}
			
			System.out.println("Starter " + i + " fitness: " + bestStage.score);
			geneticQueue.add(new GeneticStub(bestStage.score, incomingBlocksMem));
		
		}
		
		// For X iterations take the top M children and alter them
		for(int generation = 0; generation < iterations; generation++) {
			
			GeneticStub[] parents = new GeneticStub[MAX_PARENTS];
			
			for(int i = 0; i < parents.length; i++) {
				parents[i] = geneticQueue.poll();
			}
			// Put stubs back on queue
			for(int i = 0; i < parents.length; i++) {
				geneticQueue.add(parents[i]);
			}
			
			for(int parentNo = 0; parentNo < MAX_PARENTS; parentNo++) {
				for(int childNo = 0; childNo < MAX_CHILDREN; childNo++) {
				
					Queue<TetrisPiece> parentQueue = parents[parentNo].startQueue;
					
					int mutationSize = new Random().nextInt(MAX_MUTATIONS - generation) + 1;
					
					TetrisPiece[] beforeMutations = new TetrisPiece[mutationSize];
					TetrisPiece[] afterMutations = new TetrisPiece[mutationSize];
					
					Queue<TetrisPiece> incomingBlocks = new ArrayBlockingQueue<>(parentQueue.size() + (mutationSize * 2));
					Queue<TetrisPiece> incomingBlocksMem = new ArrayBlockingQueue<>(parentQueue.size() + (mutationSize * 2));
					
					TetrisPiece mutantAddition = polygons.get(new Random().nextInt(polygons.size()));
					// Add random mutation (Add a shape)
					for(int i = 0; i < mutationSize; i++) {
						beforeMutations[i] = mutantAddition;
					}
					mutantAddition = polygons.get(new Random().nextInt(polygons.size()));
					for(int i = 0; i < mutationSize; i++) {
						afterMutations[i] = mutantAddition;
					}
					
					for(TetrisPiece mutant:beforeMutations) {
						incomingBlocks.add(mutant);
						incomingBlocksMem.add(mutant);
					}
					
					incomingBlocks.addAll(parentQueue);
					incomingBlocksMem.addAll(parentQueue);
					
					for(TetrisPiece mutant:afterMutations) {
						incomingBlocks.add(mutant);
						incomingBlocksMem.add(mutant);
					}
					
					TetrisStage firstStage = new TetrisStage(incomingBlocks, width, height);
					
					TetrisStage bestStage = firstStage;
					
					while(true) {
						
						TetrisStage potentialBest = bestStage.lookAhead();
						
						// Break when no further stages found (Can't find somewhere to put next block)
						if(potentialBest == bestStage)
							break;
						else
							bestStage = potentialBest;
						
					}
					
					System.out.println("Generation " + generation + " child of " +  parents[parentNo].fitness + " fitness " + bestStage.score);
					geneticQueue.add(new GeneticStub(bestStage.score, incomingBlocksMem));
				
				}
			}
		}
		
		
		System.out.println("Winner: " + geneticQueue.peek().fitness);
		GeneticStub bestChild = geneticQueue.poll();
		
		// Choose best first stage and compute the tree
		TetrisStage firstStage = new TetrisStage(bestChild.startQueue, width, height);
		
		TetrisStage bestStage = firstStage;
		
		while(true) {
			
			TetrisStage potentialBest = bestStage.lookAhead();
			
			// Break when no further stages found (Can't find somewhere to put next block)
			if(potentialBest == bestStage)
				break;
			else
				bestStage = potentialBest;
			
		}
		
		return bestStage;
	}
	
	private static class GeneticStub {
		
		public final double fitness;
		public final Queue<TetrisPiece> startQueue;
		
		public GeneticStub(double score, Queue<TetrisPiece> startQueue) {
			this.fitness = score;
			this.startQueue = startQueue;
		}
		
	}
	
	private List<TetrisPiece> solutionPieces;
	
	public TetrisSolution(TetrisStage bestStage) {
		this.solutionPieces = new ArrayList<>();
		
		TetrisStage tetrisPiece = bestStage;
		
		while(tetrisPiece.parent != null) {
			// DEBUG
//			System.out.println("STAGE");
//			tetrisPiece.grid.drawGrid();
			solutionPieces.add(tetrisPiece.getBlock());
			
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
		private TetrisPiece block;
		private TetrisGrid grid;
		
		public TetrisStage(Queue<TetrisPiece> incomingBlocks, int width, int height) {
			
			this.incomingBlocks = incomingBlocks;
			
			// Remove next block to be placed and put it at the end
			this.placingBlock = incomingBlocks.poll();
			this.incomingBlocks.add(placingBlock);
			
			this.blockChoices = new PriorityQueue<TetrisStage>(10, new StageComp());
			
			this.parent = null;
			this.block = null;
			this.grid = new TetrisGrid(width, height);
			this.score = 0;
		}
		
		public TetrisStage(TetrisStage parentStage, TetrisPiece newBlock) {
			
			this.incomingBlocks = parentStage.incomingBlocks;

			// Remove next block to be placed and put it at the end
			this.placingBlock = incomingBlocks.poll();
			this.incomingBlocks.add(placingBlock);
			
			this.blockChoices = new PriorityQueue<TetrisStage>(10, new StageComp());
			
			this.parent = parentStage;
			this.block = newBlock;
			this.grid = new TetrisGrid(parentStage.grid);
			this.grid.addPiece(newBlock);
			this.computeScore();
		}

		private TetrisPiece getBlock() {
			return this.block;
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
			
			for(int[] possibleLocation:this.grid.getAnchors()) {
				
				TetrisPiece testPiece = this.placingBlock.copy();
				testPiece.translate(possibleLocation[0], possibleLocation[1]);
				
				// Try all positions of polygon
				
				for(int i = 0; i < testPiece.getWidth(); i++) {
					for(int j = 0; j < testPiece.getHeight(); j++) {
						
						TetrisPiece translatedPiece = testPiece.copy();
						translatedPiece.translate(i, j);
						
						if(this.grid.isPieceValid(translatedPiece)) {
							this.addChoice(translatedPiece);
						}
					}
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
			int holes = 0;
			
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
					
					// Detect holes (one block where either side is covered)
					if((i - 1 < 0 || this.grid.blocks[i - 1][j] == -1) && (i + 1 >= this.grid.blocks.length || this.grid.blocks[i + 1][j] == -1)) {
						holes++;
					}
					else if((j - 1 < 0 || this.grid.blocks[i][j - 1] == -1) && (j + 1 >= this.grid.blocks[0].length || this.grid.blocks[i][j + 1] == -1)) {
						holes++;
					}
					
				}
			}
			
			this.score += highestY * H_HIGHEST_Y;
			this.score += fullBlocks * H_FULLNESS;
			this.score += fullBlocksXHeight * H_FULL_HEIGHT;
			this.score += largestSlope * H_MAX_SLOPE;
			this.score += totalSlopes * H_TOTAL_SLOPE;
			this.score += holes * H_HOLES;
			
			this.grid.tallestY = highestY + 1;
		}
		
	}
	
	public static class TetrisPiece {
		
		// Copies polygons and make them unit polygons
		public List<Polygon> polygons;
		private Area area;
		
		public TetrisPiece(MultiPoly mPoly) {
			
			this.area = new Area();
			
			polygons = new ArrayList<>();
			
			for(Polygon poly:mPoly.getPolygons()) {
				Polygon nPoly = new Polygon();
				
				for(int i = 0; i < poly.npoints; i++) {
					nPoly.addPoint(Maths.round(poly.xpoints[i] - mPoly.getMergedPolygon().getBounds2D().getMinX(), GridPanel.GRID_SIZE) / GridPanel.GRID_SIZE,
								   Maths.round(poly.ypoints[i] - mPoly.getMergedPolygon().getBounds2D().getMinY(), GridPanel.GRID_SIZE) / GridPanel.GRID_SIZE);
					
				}
				
				polygons.add(nPoly);
				area.add(new Area(nPoly));
			}
		}
		
		public int getHeight() {
			return (int) area.getBounds2D().getHeight();
		}

		public int getWidth() {
			return (int) area.getBounds2D().getWidth();
		}

		private TetrisPiece(){
			this.polygons = new ArrayList<>();
		}

		public TetrisPiece copy() {
			TetrisPiece t = new TetrisPiece();
			
			for(Polygon polygon:this.polygons) {
				t.polygons.add(new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints));
			}
			
			t.area = (Area) this.area.clone();
			
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
		 */
		private int[][] blocks;
		
		private int tallestY;
		
		public TetrisGrid(int width, int height) {
			this.blocks = new int[width][height];
			this.tallestY = 0;
			
			// Make the first row 1's
			for(int i = 0; i < height; i++) {
				blocks[0][i] = 1;
			}
			
			// DEBUG
//			drawGrid();
		}
		
		public TetrisGrid(TetrisGrid parentGrid) {
			this.blocks = new int[parentGrid.blocks.length][parentGrid.blocks[0].length];
			
			for(int i = 0; i < parentGrid.blocks.length; i++) {
				for(int j = 0; j < parentGrid.blocks[i].length; j++) {
					this.blocks[i][j] = parentGrid.blocks[i][j];
				}
			}
			
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
				for(int i = widthIndex; i < widthIndex + width; i++) {
					for(int j = heightIndex; j < heightIndex + height; j++) {
						if(polygon.contains(i + 0.5, j + 0.5)) {
							
							// If point out of bounds, reject
							if(i + width > this.blocks.length || j + height > this.blocks[0].length) {
								return false;
							}
							
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
			
			// DEBUG
//			System.out.println("BEFORE");
//			drawGrid();
			
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
							if(i < blocks.length - 1 && blocks[i + 1][j] == 0) {
								blocks[i + 1][j] = 1;
							}
							
							// Set new tallest point
							if(j > tallestY)
								this.tallestY = j;
						}
					}
				}
			}
			
			// DEBUG
//			System.out.println("AFTER");
//			drawGrid();
		}
		
		private void drawGrid() {
			for(int i = 0; i < this.blocks[0].length; i++) {
				for(int j = 0; j < this.blocks.length; j++) {
					System.out.print(this.blocks[j][i] + 1);
				}
				System.out.println();
			}
			
			for(int i = 0; i < this.blocks.length / 2 + 1; i++) {
				System.out.print("->");
			}
			System.out.println();
		}

		public List<int[]> getAnchors() {
			// Get all 1's 
			List<int[]> locations = new ArrayList<>();
			
			for(int i = 0; i < this.blocks.length; i++) {
				for(int j = 0; j < this.blocks[i].length; j++) {
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
			
			// Lower score is better
			if(t1.score < t2.score)
				return 1;
			else if(t2.score < t1.score)
				return -1;
			
			return 0;
		}
		
	}
	
	private static class GeneticComp implements Comparator<GeneticStub> {

		@Override
		public int compare(GeneticStub t1, GeneticStub t2) {
			
			// Lower score is better
			if(t1.fitness < t2.fitness)
				return 1;
			else if(t2.fitness < t1.fitness)
				return -1;
			
			return 0;
		}
		
	}
	
}
