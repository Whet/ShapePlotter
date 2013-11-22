package com.plotter.algorithms;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import com.plotter.data.Maths;
import com.plotter.gui.AssemblyHierarchyPanel.DecompositionImage;
import com.plotter.gui.GridPanel;
import com.plotter.gui.SVGOptionsMenu.ReferenceInt;

public class TetrisSolution {

	// Heuristics
	private static final int H_HIGHEST_Y = -10;
	private static final int H_MAX_SLOPE = 1;
	private static final int H_FULLNESS = 1;
	private static final int H_FULL_HEIGHT = 1;
	private static final int H_TOTAL_SLOPE = 1;
	private static final int H_HOLES = -1;
	
	// Genetic Algorithm
	private static final int LOOK_AHEAD = 1;
	
	private static final int GENERATIONS = 10;
	
	private static final int MAX_PARENTS = 2;
	private static final int MAX_CHILDREN = 2;
	
	
	// Stores best lines on a grid
	// No real measurements, must be scaled on svg output
	public static TetrisSolution getSolution(int width, int height, Map<DecompositionImage, ReferenceInt> decompImages) {

		// Convert all possible shapes to unit polygons
		List<TetrisPiece> shrunkPolygons = new ArrayList<TetrisPiece>();
		
		for(Entry<DecompositionImage, ReferenceInt> mPoly:decompImages.entrySet()) {
			if(mPoly.getKey().isUsed()) {
				shrunkPolygons.add(new TetrisPiece(mPoly.getKey().getPolygon(), mPoly.getValue()));
				
				MultiPoly poly1 = mPoly.getKey().getPolygon().getRotatedMultipoly(1);
				MultiPoly poly2 = mPoly.getKey().getPolygon().getRotatedMultipoly(2);
				MultiPoly poly3 = mPoly.getKey().getPolygon().getRotatedMultipoly(3);
				
				shrunkPolygons.add(new TetrisPiece(poly1, mPoly.getValue()));
				shrunkPolygons.add(new TetrisPiece(poly2, mPoly.getValue()));
				shrunkPolygons.add(new TetrisPiece(poly3, mPoly.getValue()));
			}
		}

		// Genetic Algorithm
		TetrisStage bestStage = geneticTetris(shrunkPolygons, width, height, MAX_CHILDREN, GENERATIONS);
		
		return new TetrisSolution(bestStage);
	}
	
	private static Queue<TetrisPiece> getPieces(List<TetrisPiece> polygons, int totalPop) {
		
		Queue<TetrisPiece> pieces = new ArrayBlockingQueue<>(totalPop);
		
		Set<TetrisPiece> possiblePieces = new HashSet<>();
		possiblePieces.addAll(polygons);
		
		while(possiblePieces.size() > 0) {
			
			int rand = new Random().nextInt(possiblePieces.size());
			int index = 0;
			
			Iterator<TetrisPiece> iterator = possiblePieces.iterator();
			
			while(iterator.hasNext()) {
				TetrisPiece next = iterator.next();
				
				if(index == rand) {
					pieces.add(next);
					next.pop.increment();
					break;
				}
				
				index++;
			}
			
			
			// Removed pieces with no remaining population
			iterator = possiblePieces.iterator();
			
			while(iterator.hasNext()) {
				TetrisPiece next = iterator.next();
				if(next.pop.currentPopulation == next.pop.targetPopulation)
					iterator.remove();
			}
		}
			
		return pieces;
	}
	
	private static Queue<TetrisPiece> getPieces(Queue<TetrisPiece> parentQueue, int totalPop) {
		
		Queue<TetrisPiece> pieces = new ArrayBlockingQueue<>(totalPop);
		
		Queue<TetrisPiece> possiblePieces = new ArrayBlockingQueue<>(totalPop);
		possiblePieces.addAll(parentQueue);
		
		TetrisPiece shufflePiece = null;
		
		while(possiblePieces.size() > 0) {
			
			// Chance to keep queue as it is
			if(new Random().nextInt(10) > 8) {
				pieces.add(possiblePieces.poll());
			}
			// Removed a piece and remember it, if there is a piece add it in
			else if(shufflePiece == null) {
				shufflePiece = possiblePieces.poll();
			}
			else {
				pieces.add(shufflePiece);
				shufflePiece = null;
			}
		}
		
		if(shufflePiece != null)
			pieces.add(shufflePiece);
			
		return pieces;
	}
	
	private static TetrisStage geneticTetris(List<TetrisPiece> polygons, int width, int height, int maxChildren, int iterations) {
		
		int blockPopulation = 0;
		
		Queue<GeneticStub> geneticQueue = new PriorityQueue<>(maxChildren, new GeneticComp());
		
		// Create starting population
		
		System.out.println("Creating Population!");
		
		for(int i = 0; i < maxChildren; i++) {

			// Get number of blocks needed
			Set<ReferenceInt> uniqueCounters = new HashSet<>();
			
			for(TetrisPiece piece:polygons) {
				uniqueCounters.add(piece.pop);
			}
			
			for(ReferenceInt number:uniqueCounters) {
				number.reset();
				blockPopulation += number.targetPopulation;
			}
			
			Queue<TetrisPiece> incomingBlocks = new ArrayBlockingQueue<>(blockPopulation);
			Queue<TetrisPiece> incomingBlocksMem = new ArrayBlockingQueue<>(blockPopulation);
			incomingBlocks = getPieces(polygons, blockPopulation);
			incomingBlocksMem.addAll(incomingBlocks);
			
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
			
			if(bestStage.incomingBlocks.size() > 0) {
				System.out.println("Failed to place all blocks");
			}
			else {
				System.out.println("Starter " + i + " fitness: " + bestStage.grid.getGridScore());
				geneticQueue.add(new GeneticStub(bestStage.grid.getGridScore(), incomingBlocksMem));
			}
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
					
					Queue<TetrisPiece> incomingBlocks = new ArrayBlockingQueue<>(parentQueue.size());
					Queue<TetrisPiece> incomingBlocksMem = new ArrayBlockingQueue<>(parentQueue.size());
					
					incomingBlocks = getPieces(parentQueue, blockPopulation);
					incomingBlocksMem.addAll(incomingBlocks);
					
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
					
					if(bestStage.incomingBlocks.size() > 0) {
						System.out.println("Failed to place all blocks");
					}
					else {
						System.out.println("Generation " + generation + " child of " +  parents[parentNo].fitness + " fitness " + bestStage.grid.getGridScore());
						geneticQueue.add(new GeneticStub(bestStage.grid.getGridScore(), incomingBlocksMem));
					}
				
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
//			tetrisPiece.getBlock().pop.increment();
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
			
			this.incomingBlocks = new ArrayBlockingQueue<>(incomingBlocks.size());
			this.placingBlock = incomingBlocks.poll();
			this.incomingBlocks.addAll(incomingBlocks);
			
			this.blockChoices = new PriorityQueue<TetrisStage>(10, new StageComp());
			
			this.parent = null;
			this.block = null;
			this.grid = new TetrisGrid(width, height);
			this.score = 0;
			
//			System.out.println("INC BLOCKS: " + incomingBlocks.size());
		}
		
		public TetrisStage(TetrisStage parentStage, TetrisPiece newBlock) {
			
			if(parentStage.incomingBlocks.size() > 0) {
				this.incomingBlocks = new ArrayBlockingQueue<>(parentStage.incomingBlocks.size());
				this.incomingBlocks.addAll(parentStage.incomingBlocks);
				this.placingBlock = incomingBlocks.poll();
			}
			else {
				this.incomingBlocks = new ArrayBlockingQueue<>(1);
				this.placingBlock = null;
			}
				
			this.blockChoices = new PriorityQueue<TetrisStage>(10, new StageComp());
			
			this.parent = parentStage;
			this.block = newBlock;
			this.grid = new TetrisGrid(parentStage.grid);
			this.grid.addPiece(newBlock);
			this.computeScore();
			
//			System.out.println("INC BLOCKS: " + incomingBlocks.size());
		}

		private TetrisPiece getBlock() {
			return this.block;
		}
		
		public TetrisStage lookAhead() {
			return lookAhead(TetrisSolution.LOOK_AHEAD);
		}
		
		public TetrisStage lookAhead(int lookAhead) {
			
			if(this.placingBlock == null)
				return this;
			
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
		}
		
	}
	
	public static class TetrisPiece {
		
		// Copies polygons and make them unit polygons
		public List<Polygon> polygons;
		private Area area;
		private ReferenceInt pop;
		
		public TetrisPiece(MultiPoly mPoly, ReferenceInt integer) {
			
			this.pop = integer;
			
			this.area = new Area();
			
			polygons = new ArrayList<>();
			
			for(Polygon poly:mPoly.getPolygons()) {
				Polygon nPoly = new Polygon();
				
				for(int i = 0; i < poly.npoints; i++) {
					nPoly.addPoint(Maths.round(poly.xpoints[i] - mPoly.getMergedPolygon().getBounds2D().getMinX(), GridPanel.GRID_SIZE) / (GridPanel.GRID_SIZE * 2),
								   Maths.round(poly.ypoints[i] - mPoly.getMergedPolygon().getBounds2D().getMinY(), GridPanel.GRID_SIZE) / (GridPanel.GRID_SIZE * 2));
					
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
			t.pop = this.pop;
			
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
		
		public TetrisGrid(int width, int height) {
			this.blocks = new int[width][height];
			
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
		}
		
		public int getGridScore() {
			
			int height = 0;
			int width = 0;
			
			for(int i = 0; i < blocks.length; i++) {
				for(int j = 0; j < blocks[i].length; j++) {
					if(blocks[i][j] == -1) {
						height = i;
						
						if(j > width)
							width = j;
					}
				}
			}
			
			return height * width;
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
			if(t1.fitness > t2.fitness)
				return 1;
			else if(t2.fitness > t1.fitness)
				return -1;
			
			return 0;
		}
		
	}
	
}
