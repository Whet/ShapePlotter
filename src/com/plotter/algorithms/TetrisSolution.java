package com.plotter.algorithms;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
	private static final int H_MAX_SLOPE = -1;
	private static final int H_FULLNESS = -1;
	private static final int H_FULL_HEIGHT = -5;
	private static final int H_TOTAL_SLOPE = -2;
	private static final int H_HOLES = 0;
	
	// Genetic Algorithm
	private static final int LOOK_AHEAD = 1;
	
	private static final int GENERATIONS = 50;
	
	private static final int MAX_CROSSOVERS = 10;
	private static final int MAX_GENEPOOL = 200;
	private static final int MAX_BREEDERS = 10;
	
	private static final int INITIAL_POPULATION = 10;
	
	// Stores best lines on a grid
	// No real measurements, must be scaled on svg output
	public static TetrisSolution getSolution(int width, int height, Map<DecompositionImage, ReferenceInt> decompImages) {

		// Convert all possible shapes to unit polygons
		List<TetrisPiece> shrunkPolygons = new ArrayList<TetrisPiece>();
		
		for(Entry<DecompositionImage, ReferenceInt> mPoly:decompImages.entrySet()) {
			if(mPoly.getKey().isUsed()) {
				shrunkPolygons.add(new TetrisPiece(0, mPoly.getKey().getPolygon(), mPoly.getValue()));
				shrunkPolygons.add(new TetrisPiece(1, mPoly.getKey().getPolygon(), mPoly.getValue()));
				shrunkPolygons.add(new TetrisPiece(2, mPoly.getKey().getPolygon(), mPoly.getValue()));
				shrunkPolygons.add(new TetrisPiece(3, mPoly.getKey().getPolygon(), mPoly.getValue()));
			}
		}
		
		long startTime = System.currentTimeMillis();
		
		System.out.println("Mission accepted");

		// Genetic Algorithm
		TetrisStage bestStage = geneticTetris(shrunkPolygons, width, height);
		
		long endTime = System.currentTimeMillis();
		long diff = (endTime - startTime) / 1000;
		
		System.out.println("Mission Completed took " + diff + " seconds");
		
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
	
	private static Queue<TetrisPiece> getCrossover(Queue<TetrisPiece> parent1, Queue<TetrisPiece> parent2, List<TetrisPiece> polygons) {
		
		Queue<TetrisPiece> pieces = new ArrayBlockingQueue<>(parent1.size());
		
		List<TetrisPiece> parent1Copy = new ArrayList<>();
		parent1Copy.addAll(parent1);
		
		List<TetrisPiece> parent2Copy = new ArrayList<>();
		parent2Copy.addAll(parent2);
		
		
		for(TetrisPiece piece:parent1) {
			piece.pop.reset();
		}
		for(TetrisPiece piece:parent2) {
			piece.pop.reset();
		}
		
		TetrisPiece randP = null;
		
		while(pieces.size() < parent1.size()) {
			if(new Random().nextBoolean()) {
				do {
					randP = parent1Copy.get(new Random().nextInt(parent1Copy.size()));
				}while(randP.pop.currentPopulation == randP.pop.targetPopulation);
			}
			else {
				do {
					randP = parent2Copy.get(new Random().nextInt(parent2Copy.size()));
				}while(randP.pop.currentPopulation == randP.pop.targetPopulation);
			}
			
			pieces.add(randP);
			randP.pop.increment();
		}
		
		return pieces;
	}
	
	private static TetrisStage geneticTetris(List<TetrisPiece> polygons, int width, int height) {
		
		int blockPopulation = 0;
		
		Queue<GeneticStub> geneticQueue = new PriorityQueue<>(10, new GeneticComp());
		
		// Create starting population
		
		System.out.println("Creating Population!");
		
		int attempts = 0;
		
		while(geneticQueue.size() < INITIAL_POPULATION) {
			attempts++;
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
//				System.out.println("Failed to place all blocks");
			}
			else {
				System.out.println("Starter " + attempts + " fitness: " + bestStage.grid.getGridScore());
				geneticQueue.add(new GeneticStub(bestStage.grid.getGridScore(), incomingBlocksMem));
			}
			
			if(attempts > INITIAL_POPULATION * 2) {
				System.out.println("Failed to place all blocks");
				bestStage.grid.drawGrid();
				bestStage.lookAhead();
				break;
			}
		}
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileOutputStream(new File("C:/Users/Daedalus/Desktop/geneticData.txt"), true));
			
			List<GeneticStub> stubs = new ArrayList<>();
			
			while(geneticQueue.size() > 0) {
				GeneticStub stub = geneticQueue.poll();
				stubs.add(stub);
				
				writer.println(0 + "," + stub.fitness);
			}
			
			geneticQueue.addAll(stubs);
			
			writer.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// For X iterations take the top M children and alter them
		for(int generation = 0; generation < GENERATIONS; generation++) {
			
			List<GeneticStub> parents = new ArrayList<>();
			
			for(int i = 0; i < MAX_GENEPOOL; i++) {
				
				if(geneticQueue.size() == 0)
					break;
				
				parents.add(geneticQueue.poll());
			}
			// Put stubs back on queue
			for(int i = 0; i < parents.size(); i++) {
				geneticQueue.add(parents.get(i));
			}
			
			if(parents.size() > 1)
			for(int parentNo = 0; parentNo < MAX_BREEDERS; parentNo++) {
				for(int childNo = 0; childNo < MAX_CROSSOVERS; childNo++) {
				
					Queue<TetrisPiece> parentQueue = parents.get(parentNo).startQueue;
					
					Queue<TetrisPiece> incomingBlocks = new ArrayBlockingQueue<>(parentQueue.size());
					Queue<TetrisPiece> incomingBlocksMem = new ArrayBlockingQueue<>(parentQueue.size());
					
					Queue<TetrisPiece> mateQueue = null;
					
					do {
						mateQueue = parents.get(new Random().nextInt(parents.size())).startQueue;
					}while(mateQueue == parentQueue);
					
					incomingBlocks = getCrossover(parentQueue, mateQueue, polygons);
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
//						System.out.println("Failed to place all blocks");
					}
					else {
						System.out.println("Generation " + generation + " child of " +  parents.get(parentNo).fitness + " fitness " + bestStage.grid.getGridScore());
						geneticQueue.add(new GeneticStub(bestStage.grid.getGridScore(), incomingBlocksMem));
					}
				}
			}
			
			try {
				writer = new PrintWriter(new FileOutputStream(new File("C:/Users/Daedalus/Desktop/geneticData.txt"), true));
				
				List<GeneticStub> stubs = new ArrayList<>();
				
				while(geneticQueue.size() > 0) {
					GeneticStub stub = geneticQueue.poll();
					stubs.add(stub);
					writer.println((generation + 1) + "," + stub.fitness);
				}
				
				geneticQueue.addAll(stubs);
				
				writer.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
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
	public TetrisGrid finalGrid;
	
	public TetrisSolution(TetrisStage bestStage) {
		this.solutionPieces = new ArrayList<>();
		
		TetrisStage tetrisPiece = bestStage;
		
		finalGrid = tetrisPiece.grid;
		
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
			this.grid = new TetrisGrid(width, height, this.placingBlock.getSize());
			this.score = 0;
			
//			System.out.println("INC BLOCKS: " + incomingBlocks.size());
		}
		
		public TetrisStage(TetrisStage parentStage, TetrisPiece newBlock) {
			
			if(parentStage.incomingBlocks.size() > 0) {
				this.incomingBlocks = new ArrayBlockingQueue<>(parentStage.incomingBlocks.size());
				this.incomingBlocks.addAll(parentStage.incomingBlocks);
				this.placingBlock = incomingBlocks.poll();
				this.grid = new TetrisGrid(parentStage.grid, this.placingBlock.getSize());
			}
			else {
				this.incomingBlocks = new ArrayBlockingQueue<>(1);
				this.placingBlock = null;
				this.grid = new TetrisGrid(parentStage.grid, 0);
			}
				
			this.blockChoices = new PriorityQueue<TetrisStage>(10, new StageComp());
			
			this.parent = parentStage;
			this.block = newBlock;
			
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
			
			if(lookAhead > 0)
				this.computeAllChoices(lookAhead);
			
			if(this.blockChoices.peek() == null) {
				return this;
			}
			else {
				return this.blockChoices.peek();
			}
			
		}
		
		private void computeAllChoices(int lookAhead) {
			// Take the placing block
			// Try all possible translations
			// AddChoice(position)
//			System.out.println("Possibilities: " + this.grid.getAnchors().size());
			for(int[] possibleLocation:this.grid.getAnchors()) {
				
				TetrisPiece testPiece = this.placingBlock.copy();
				testPiece.translate(possibleLocation[0], possibleLocation[1]);
				
				// Try all positions of polygon
				
				for(int i = 0; i < testPiece.getWidth(); i++) {
					for(int j = 0; j < testPiece.getHeight(); j++) {
						
						TetrisPiece translatedPiece = testPiece.copy();
						translatedPiece.translate(i, j);
						
						if(this.grid.isPieceValid(translatedPiece)) {
							this.addChoice(translatedPiece, lookAhead);
						}
					}
				}
			}
			
		}
		
		private void addChoice(TetrisPiece positionedBlock, int lookAhead) {
			TetrisStage tetrisStage = new TetrisStage(this, positionedBlock);
			this.blockChoices.add(tetrisStage.lookAhead(lookAhead - 1));
//			System.out.println("Choices: " + this.blockChoices.size());
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
		public Polygon mergedPolygon;
		public double[] markerPolygonLocation;
		public ReferenceInt pop;
		private int width, height;
		public int rotationComponent;
		
		public TetrisPiece(int rotationComponent, MultiPoly mPoly, ReferenceInt integer) {
			
			this.rotationComponent = rotationComponent;
			this.pop = integer;
			
			Area area = new Area();
			mergedPolygon = new Polygon();
			
			mPoly = mPoly.getRotatedMultipoly(rotationComponent);
			
			for(Polygon poly:mPoly.getPolygons()) {
				Polygon nPoly = new Polygon();
				
				for(int i = 0; i < poly.npoints; i++) {
					nPoly.addPoint(Maths.round(poly.xpoints[i] - mPoly.getMergedPolygon().getBounds2D().getMinX(), GridPanel.GRID_SIZE) / (GridPanel.GRID_SIZE * 2),
								   Maths.round(poly.ypoints[i] - mPoly.getMergedPolygon().getBounds2D().getMinY(), GridPanel.GRID_SIZE) / (GridPanel.GRID_SIZE * 2));
					
				}
				
				area.add(new Area(nPoly));
				
			}
			
			this.mergedPolygon = new Polygon();
			
			PathIterator path = area.getPathIterator(null);
	        while (!path.isDone()) {
	            toPolygon(path);
	            path.next();
	        }
			
			this.markerPolygonLocation = new double[]{area.getBounds2D().getCenterX(), area.getBounds2D().getCenterY()};
			
			this.height = (int) area.getBounds2D().getHeight();
			this.width = (int) area.getBounds2D().getWidth();
			
			if(height == 0 || width == 0)
				System.err.println("WRONG SIZE");
		}
		
		public int getSize() {
			return Math.max(width, height);
		}

		public int getHeight() {
			return height;
		}

		public int getWidth() {
			return width;
		}

		private TetrisPiece(){
		}

		public TetrisPiece copy() {
			TetrisPiece t = new TetrisPiece();
			
			t.mergedPolygon = new Polygon(this.mergedPolygon.xpoints, this.mergedPolygon.ypoints, this.mergedPolygon.npoints);
			t.pop = this.pop;
			t.markerPolygonLocation = new double[]{this.markerPolygonLocation[0], this.markerPolygonLocation[1]};
			t.width = this.width;
			t.height = this.height;
			t.rotationComponent = this.rotationComponent;
			
			return t;
		}
		
		public void translate(int deltaX, int deltaY) {
			this.mergedPolygon.translate(deltaX, deltaY);
			this.markerPolygonLocation[0] += deltaX;
			this.markerPolygonLocation[1] += deltaY;
		}
		
		private void toPolygon(PathIterator p_path) {
			double[] point = new double[2];
			if(p_path.currentSegment(point) != PathIterator.SEG_CLOSE)
				this.mergedPolygon.addPoint((int) point[0], (int) point[1]);
		}
		
	}
	
	public static class TetrisGrid {
		
		/*
		 * 0 - No Block
		 * -1 - Block
		 * 1 - Block can be placed
		 * 
		 */
		private int[][] blocks;
		private int height;
		
		public TetrisGrid(int width, int height, int blockSize) {
			this.blocks = new int[width][height];
			
			// Make the first row 1's
			for(int i = 0; i < height; i++) {
				blocks[0][i] = 1;
			}
			this.height = 0;
			
		}
		
		public TetrisGrid(TetrisGrid parentGrid, int blockSize) {
			// Check how many rows are completed and removed them
			this.height = parentGrid.height;
			boolean rowFull = true;
			int extraHeight = 0;
			do {
				rowFull = true;
				for(int i = 0; i < parentGrid.blocks.length; i++) {
					if(parentGrid.blocks[i][this.height - parentGrid.height] == 0 || parentGrid.blocks[i][this.height - parentGrid.height] == 1) {
						rowFull = false;
						break;
					}
				}
				
				if(rowFull)
					height++;
				
			}while(rowFull);
			
			// Check if placing the block would go out of bounds on height
//			if(this.height + parentGrid.blocks[0].length < M_HEIGHT) {
//				
//				List<int[]> anchors = parentGrid.getAnchors();
//				
//				int hMax = 0;
//				
//				for(int[] loc:anchors) {
//					if(loc[1] > hMax)
//						hMax = loc[1];
//				}
//				
//				if(hMax + blockSize >= parentGrid.blocks[0].length)
//					extraHeight = blockSize;
//				
//				if(extraHeight + parentGrid.blocks[0].length > M_HEIGHT)
//					extraHeight = M_HEIGHT - parentGrid.blocks[0].length;
//				
//			}
			
			this.blocks = new int[parentGrid.blocks.length][parentGrid.blocks[0].length - (this.height - parentGrid.height) + extraHeight];
			
			for(int i = 0; i < parentGrid.blocks.length; i++) {
				for(int j = 0; j < parentGrid.blocks[i].length - (this.height - parentGrid.height); j++) {
					this.blocks[i][j] = parentGrid.blocks[i][j + (this.height - parentGrid.height)];
				}
			}
		}
		
		public int getGridScore() {
			
			int width = 0;
			int height = 0;
			
			for(int i = 0; i < blocks.length; i++) {
				for(int j = 0; j < blocks[i].length; j++) {
					if(blocks[i][j] == -1) {
						width = i;
						
						if(j > height)
							height = j;
					}
				}
			}
			
			return (this.height + height) * width;
		}

		public boolean isPieceValid(TetrisPiece piece) {
			
			// Block must be sat ontop of another block to be valid
			boolean anchorFound = false;
			
			Polygon polygon = piece.mergedPolygon;
				
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
						if(i + width > this.blocks.length ||
						   i < 0 ||
						   j + height > this.blocks[0].length + this.height ||
						   j - this.height < 0) {
							return false;
						}
						
						// Overlaps with a non-empty or non-anchor block
						if(blocks[i][j - this.height] != 0 && blocks[i][j - this.height] != 1) {
							return false;
						}
						// Overlaps with anchor
						else if(blocks[i][j - this.height] == 1) {
							anchorFound = true;
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
			
			Polygon polygon = piece.mergedPolygon;
				
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
						blocks[i][j - this.height] = -1;
						
						// Set the block one down to being a possible building point: 1
						if(i < blocks.length - 1 && j - this.height >= 0 && blocks[i + 1][j - this.height] == 0) {
							blocks[i + 1][j - this.height] = 1;
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
					if(this.blocks[i][j] == 1) {
						locations.add(new int[]{i, j + this.height});
					}
					
				}
			}
			
			return locations;
		}

		public List<int[]> getHoles() {
			// Get all 1's & 0's 
			List<int[]> locations = new ArrayList<>();
			
			for(int i = 0; i < this.blocks.length; i++) {
				for(int j = 0; j < this.blocks[i].length; j++) {
					if(this.blocks[i][j] == 0 || this.blocks[i][j] == 1) {
						locations.add(new int[]{i, j + this.height});
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
