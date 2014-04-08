package com.plotter.algorithms;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

import com.plotter.data.Connection;
import com.plotter.data.Maths;
import com.plotter.gui.AssemblyHierarchyPanel.DecompositionImage;
import com.plotter.gui.GridPanel;
import com.plotter.gui.SVGOptionsMenu.ReferenceInt;

public class TetrisSolution {

	// Heuristics
	private static final int H_HIGHEST_Y = -10;
	private static final int H_MAX_SLOPE = -1;
	private static final int H_FULLNESS = -1;
	private static final int H_FULL_HEIGHT = -10;
	private static final int H_TOTAL_SLOPE = -2;
	private static final int H_HOLES = 0;
	
	// Genetic Algorithm
	private static final int LOOK_AHEAD = 1;
	
	private static final int GENERATIONS = 10;
	
	private static final int MAX_CROSSOVERS = 5;
	private static final int MAX_GENEPOOL = 30;
	private static final int MAX_BREEDERS = 30;
	
	private static final int INITIAL_POPULATION = 30;
	
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
		
		Map<ReferenceInt, List<TetrisPiece>> mutate = new HashMap<>();
		
		for(TetrisPiece piece:polygons) {
			if(!mutate.containsKey(piece.pop))
				mutate.put(piece.pop, new ArrayList<TetrisPiece>());
			
			mutate.get(piece.pop).add(piece);
		}
		
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
			
			// Chance to mutate
			if(new Random().nextInt(10) < 2) {
				List<TetrisPiece> mutationChoices = mutate.get(randP.pop);
				randP = mutationChoices.get(new Random().nextInt(mutationChoices.size())).copy();
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
				
//				System.out.println(bestStage.incomingBlocks.size());
				
			}
			
			if(bestStage.placingBlock != null) {
				System.out.println("Failed to place all blocks");
				attempts++;
			}
			else {
				System.out.println("Starter " + attempts + " fitness: " + bestStage.getGrid().getGridScore());
				geneticQueue.add(new GeneticStub(bestStage.getGrid().getGridScore(), incomingBlocksMem));
			}
			
			if(attempts > 3) {
				System.out.println("Failed to place all blocks");
				bestStage.getGrid().drawGrid();
				bestStage.lookAhead();
				break;
			}
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
					
					if(bestStage.placingBlock != null) {
						System.out.println("Failed to place all blocks");
					}
					else {
						System.out.println("Generation " + generation + " child of " +  parents.get(parentNo).fitness + " fitness " + bestStage.getGrid().getGridScore());
						geneticQueue.add(new GeneticStub(bestStage.getGrid().getGridScore(), incomingBlocksMem));
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
	public TetrisGrid finalGrid;
	
	public TetrisSolution(TetrisStage bestStage) {
		this.solutionPieces = new ArrayList<>();
		
		TetrisStage tetrisPiece = bestStage;
		
		finalGrid = tetrisPiece.getGrid();
		
		while(tetrisPiece.parent != null) {
			// DEBUG
//			System.out.println("STAGE");
//			tetrisPiece.grid.drawGrid();
			solutionPieces.add(tetrisPiece.getBlock());
//			tetrisPiece.getBlock().pop.increment();
			tetrisPiece = tetrisPiece.parent;
		}
		
		System.out.println("Solution blocks " + solutionPieces.size());
		
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
			this.grid = null;
			if(parentStage.incomingBlocks.size() > 0) {
				this.incomingBlocks = new ArrayBlockingQueue<>(parentStage.incomingBlocks.size());
				this.incomingBlocks.addAll(parentStage.incomingBlocks);
				this.placingBlock = incomingBlocks.poll();
//				this.grid = new TetrisGrid(parentStage.getGrid(), this.placingBlock.getSize());
			}
			else {
				this.incomingBlocks = new ArrayBlockingQueue<>(1);
				this.placingBlock = null;
//				this.grid = new TetrisGrid(parentStage.getGrid(), 0);
			}
				
			this.blockChoices = new PriorityQueue<TetrisStage>(10, new StageComp());
			
			this.parent = parentStage;
			this.block = newBlock;
			
			this.computeScore();
			
//			System.out.println("INC BLOCKS: " + incomingBlocks.size());
		}

		private TetrisPiece getBlock() {
			return this.block;
		}
		
		public TetrisStage lookAhead() {
			
			if(this.grid == null) {
				this.grid = this.getGrid();
				
				if(this.parent != null && this.parent.parent != null && this.parent.grid != null)
					this.parent.grid = null;
			}
			
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
			
			for(int[] possibleLocation:this.getGrid().getAnchors()) {
				
				TetrisPiece testPiece = this.placingBlock.copy();
				testPiece.translate(possibleLocation[0], possibleLocation[1]);
				
				// Try all positions of polygon
				
				for(int i = 0; i < testPiece.getWidth(); i++) {
					for(int j = 0; j < testPiece.getHeight(); j++) {
						
						TetrisPiece translatedPiece = testPiece.copy();
						translatedPiece.translate(i, j);
						
						if(this.getGrid().isPieceValid(translatedPiece)) {
							this.addChoice(translatedPiece, lookAhead);
						}
					}
				}
			}
			
		}
		
		private void addChoice(TetrisPiece positionedBlock, int lookAhead) {
			TetrisStage tetrisStage = new TetrisStage(this, positionedBlock);
			this.blockChoices.add(tetrisStage.lookAhead(lookAhead - 1));
		}
		
		private void computeScore() {
			this.score = 0;
			
			int highestY = 0;
			int fullBlocks = 0;
			int fullBlocksXHeight = 0;
			int largestSlope = 0;
			int totalSlopes = 0;
			int holes = 0;
			
			TetrisGrid localGrid = this.getGrid();
			
			for(int i = 0; i < localGrid.blocks.length; i++) {
				for(int j = 0; j < localGrid.blocks[i].length; j++) {
					
					int gridCode = localGrid.blocks[i][j];
					
					// Detect highest point on grid
					if(j > highestY && gridCode == -1)
						highestY = j;
					
					// Measure fullness
					if(gridCode == -1) {
						fullBlocks++;
						
						fullBlocksXHeight += j;
					}
					
					// Detect holes (one block where either side is covered)
					if((i - 1 < 0 || localGrid.blocks[i - 1][j] == -1) && (i + 1 >= localGrid.blocks.length || localGrid.blocks[i + 1][j] == -1)) {
						holes++;
					}
					else if((j - 1 < 0 || localGrid.blocks[i][j - 1] == -1) && (j + 1 >= localGrid.blocks[0].length || localGrid.blocks[i][j + 1] == -1)) {
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
		
		public TetrisGrid getGrid() {
			
			if(this.grid != null)
				return new TetrisGrid(this.grid, 0);
			
			TetrisGrid grid = this.parent.getGrid();
			grid.addPiece(this.block);
			
			return grid;
		}
		
	}
	
	public static class TetrisPiece {
		
		// Copies polygons and make them unit polygons
		public MultiPoly mPoly;
		public Polygon mergedPolygon;
		public List<Point> markerPolygonLocations;
		public List<Double> markerRotations;
		public List<double[]> connections;
		public ReferenceInt pop;
		private int width, height;
		public int rotationComponent;
		
		public TetrisPiece(int rotationComponent, MultiPoly mPoly, ReferenceInt integer) {
			
			this.markerPolygonLocations = new ArrayList<>();
			this.markerRotations = new ArrayList<>();
			this.rotationComponent = rotationComponent;
			this.pop = integer;
			
			Area largeArea = new Area();
			Area area = new Area();
			mergedPolygon = new Polygon();
			
			try {
				this.mPoly = (MultiPoly) mPoly.clone();
				this.mPoly.rotate(new Point((int)(this.mPoly.getMergedPolygon().getBounds2D().getMinX()), (int)(this.mPoly.getMergedPolygon().getBounds2D().getMinY())), (Math.PI / 2) * rotationComponent);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			
			for(Polygon poly:this.mPoly.getPolygons()) {
				Polygon nPoly = new Polygon();
				
				for(int i = 0; i < poly.npoints; i++) {
					nPoly.addPoint(Maths.round(poly.xpoints[i] - this.mPoly.getMergedPolygon().getBounds2D().getMinX(), GridPanel.GRID_SIZE) / (GridPanel.GRID_SIZE * 2),
								   Maths.round(poly.ypoints[i] - this.mPoly.getMergedPolygon().getBounds2D().getMinY(), GridPanel.GRID_SIZE) / (GridPanel.GRID_SIZE * 2));
				}
				
				this.markerPolygonLocations.add(new Point((int)nPoly.getBounds2D().getCenterX(), (int)nPoly.getBounds2D().getCenterY()));
				this.markerRotations.add((Math.PI / 2) * rotationComponent);
				
				area.add(new Area(nPoly));
				largeArea.add(new Area(poly));
			}
			
			this.mergedPolygon = new Polygon();
			
			PathIterator path = area.getPathIterator(null);
	        while (!path.isDone()) {
	            toPolygon(path);
	            path.next();
	        }
			
			this.height = (int) area.getBounds2D().getHeight();
			this.width = (int) area.getBounds2D().getWidth();
			
			if(height == 0 || width == 0)
				System.err.println("WRONG SIZE");
			
			this.connections = new ArrayList<>();
			
			double minX = largeArea.getBounds2D().getMinX();
			double minY = largeArea.getBounds2D().getMinY();
			
			double maxX = largeArea.getBounds2D().getMaxX();
			double maxY = largeArea.getBounds2D().getMaxY();
			
			double width = maxX - minX;
			double height = maxY - minY;
			
			// Store connections as a fraction of the shape width/height
			for(Connection connection:this.mPoly.connectedPoints) {
				double[] ds = new double[7];
				this.connections.add(ds);
				
				ds[0] = (connection.getCentre().x - minX) / width;
				ds[1] = (connection.getCentre().y - minY) / height;
				ds[2] = (connection.getInside().x - minX) / width;
				ds[3] = (connection.getInside().y - minY) / height;
				ds[4] = (connection.getOutside().x - minX) / width;
				ds[5] = (connection.getOutside().y - minY) / height;
				ds[6] = connection.getFlavour();
			}
			
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
			t.markerPolygonLocations = new ArrayList<>();
			
			for(Point markerLoc:this.markerPolygonLocations) {
				t.markerPolygonLocations.add(new Point(markerLoc.x, markerLoc.y));
			}
			
			
			t.markerRotations = new ArrayList<>();
			for(Double rot:this.markerRotations) {
				t.markerRotations.add(new Double(rot));
			}
			
			t.connections = new ArrayList<>();
			for(double[] connection:this.connections) {
				t.connections.add(new double[]{connection[0], connection[1], connection[2], connection[3], connection[4], connection[5], connection[6]});
			}
			
			t.width = this.width;
			t.height = this.height;
			t.rotationComponent = this.rotationComponent;
			t.mPoly = this.mPoly;
			
			return t;
		}
		
		public void translate(int deltaX, int deltaY) {
			this.mergedPolygon.translate(deltaX, deltaY);
			for(Point markerLoc:this.markerPolygonLocations) {
				markerLoc.translate(deltaX, deltaY);
			}
		}
		
		private void toPolygon(PathIterator p_path) {
			double[] point = new double[2];
			if(p_path.currentSegment(point) != PathIterator.SEG_CLOSE)
				this.mergedPolygon.addPoint((int) point[0], (int) point[1]);
		}

		public int getId() {
			return this.pop.getId();
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
//			boolean rowFull = true;
			int extraHeight = 0;
//			do {
//				rowFull = true;
//				for(int i = 0; i < parentGrid.blocks.length; i++) {
//					if(parentGrid.blocks[i][this.height - parentGrid.height] == 0 || parentGrid.blocks[i][this.height - parentGrid.height] == 1) {
//						rowFull = false;
//						break;
//					}
//				}
//				
//				if(rowFull)
//					height++;
//				
//			}while(rowFull);
			
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
			
			int width = 1;
			int height = 1;
			
			for(int i = 0; i < blocks.length; i++) {
				for(int j = 0; j < blocks[i].length; j++) {
					if(blocks[i][j] == -1) {
						
						if(i + 1 > width)
							width = i + 1;
						
						if(j + 1 > height)
							height = j + 1;
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
