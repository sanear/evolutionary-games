import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Random;

public class Lattice
{
	// Defaults
	protected static final int DEFAULT_SIZE = 100;
	protected static final double[][] DEFAULT_3STRATEGY_PAYOFF_MATRIX = {{1,1,1},{1,1,1},{1,1,1}};
	protected static final double[][] DEFAULT_2STRATEGY_PAYOFF_MATRIX = {{1,1},{1,1}};
	protected static final int DEFAULT_DIMENSIONS = 2;
	protected static final int DEFAULT_NUM_STRATEGIES = 3;
	protected static final int DEFAULT_INTERACTION_RANGE = 1;
	protected static final double[] DEFAULT_2STRATEGY_INIT_PROPORTION_MASKS = {1,1};
	protected static final double[] DEFAULT_3STRATEGY_INIT_PROPORTION_MASKS = {1,1,1};
	protected static final int DEFAULT_UPDATE_METHOD = 0;
	protected static final Color[] STRATEGY_COLORS = {Color.red, Color.green, Color.blue};
	protected static final String[] STRATEGY_COLOR_NAMES = {"Red", "Green", "Blue"};

	// Random number generator for all random numbers
	protected Random rand;
	
	// Active instance variables
	protected int[][] array;
	protected double[][] payoffMatrix;
	protected BufferedImage image;
	
	// Parameter instance variables
	protected int size;
	protected int dimensions;
	protected int numStrategies;
	protected int interactionRange;
	protected double[] initProportionMasks;
	protected int[] strategyCounts;
	protected int updateMethod;
	
	public Lattice()
	{
		size = DEFAULT_SIZE;
		dimensions = DEFAULT_DIMENSIONS;
		numStrategies = DEFAULT_NUM_STRATEGIES;
		interactionRange = DEFAULT_INTERACTION_RANGE;
		updateMethod = DEFAULT_UPDATE_METHOD;
		rand = new Random();
		
		initializeLattice();
		checkMode();
	}
	
	public Lattice(int dimensions, int numStrategies, int interactionRange, double[] initProportionMasks, int updateMethod, double[][] payoffMatrix)
	{
		this.size = DEFAULT_SIZE;
		this.dimensions = dimensions;
		this.numStrategies = numStrategies;
		this.interactionRange = interactionRange;
		this.updateMethod = updateMethod;
		rand = new Random();

		initializeLattice(initProportionMasks, payoffMatrix);
		checkMode();
	}
	
	protected void initializeLattice()
	{
		strategyCounts = new int[numStrategies];
		initProportionMasks = new double[numStrategies];
		if (numStrategies == 2)
		{
			for (int i=0; i<numStrategies; i++)
			{
				initProportionMasks[i] = DEFAULT_2STRATEGY_INIT_PROPORTION_MASKS[i];
			}

		}
		else if (numStrategies == 3)
		{
			for (int i=0; i<numStrategies; i++)
			{
				initProportionMasks[i] = DEFAULT_3STRATEGY_INIT_PROPORTION_MASKS[i];
			}
		}
		initializePayoffMatrix();
		initializeArray();
	}
	
	protected void initializeLattice(double[][] payoffMatrix)
	{
		strategyCounts = new int[numStrategies];
		initProportionMasks = new double[numStrategies];
		if (numStrategies == 2)
		{
			for (int i=0; i<numStrategies; i++)
			{
				initProportionMasks[i] = DEFAULT_2STRATEGY_INIT_PROPORTION_MASKS[i];
			}

		}
		else if (numStrategies == 3)
		{
			for (int i=0; i<numStrategies; i++)
			{
				initProportionMasks[i] = DEFAULT_3STRATEGY_INIT_PROPORTION_MASKS[i];
			}
		}
		initializePayoffMatrix(payoffMatrix);
		initializeArray();
	}
	
	protected void initializeLattice(double[] initProportionMasks, double[][] payoffMatrix)
	{
		strategyCounts = new int[numStrategies];
		this.initProportionMasks = new double[numStrategies];
		if (numStrategies == initProportionMasks.length)
		{
			for (int i=0; i<numStrategies; i++)
			{
				this.initProportionMasks[i] = initProportionMasks[i];
			}
		}
		else
		{
			System.err.println("Length of argument is not the number of strategies... EXITING.");
			System.exit(1);
		}
		initializePayoffMatrix(payoffMatrix);
		initializeArray();
	}
	
	protected void initializeArray()
	{
		// We allocate memory for the array depending on the dimension
		if (dimensions == 1)
		{
			array = new int[size][1];
		}
		else
		{
			array = new int[size][size];
		}
		
		double[] sumToI = new double[numStrategies];
		sumToI[0] = getInitProportionOf(0);
		for (int i=1; i<numStrategies; i++)
		{
			sumToI[i] = sumToI[i-1] + getInitProportionOf(i);
		}
		
		// In the 1D case, we use only the first column.
		if (dimensions == 1)
		{
			for (int i=0; i<array.length; i++)
			{
				double key = rand.nextDouble();
				if (key > 0 && key <= sumToI[0])
				{
					array[i][0] = 0;
					strategyCounts[0]++;
				}
				else
				{
					for(int j=1; j<sumToI.length; j++)
					{
						if (key > sumToI[j-1] && key <= sumToI[j])
						{
							array[i][0] = j;
							strategyCounts[j]++;
						}
					}
				}
			}
		}
		// In 2D, we use (size) columns and (size) rows.
		else if (dimensions == 2)
		{
			for (int i=0; i<array.length; i++)
			{
				for (int j=0; j<array[0].length; j++)
				{
					double key = rand.nextDouble();
					if (key > 0 && key <= sumToI[0])
					{
						array[i][j] = 0;
						strategyCounts[0]++;
					}
					else
					{
						for (int k=1; k<sumToI.length; k++)
						{
							if (key > sumToI[k-1] && key <= sumToI[k])
							{
								array[i][j] = k;
								strategyCounts[k]++;
							}
						}
					}
				}
			}
		}
				
		if (dimensions == 1)
		{
			if (numStrategies == 2)
			{
				image = new BufferedImage(size, 1, BufferedImage.TYPE_BYTE_BINARY);
				WritableRaster raster = image.getRaster();
				raster.setPixels(0, 0, size, 1, makeRasterArray());
			}
			else if (numStrategies == 3)
			{
				image = new BufferedImage(size, 1, BufferedImage.TYPE_INT_RGB);
				WritableRaster raster = image.getRaster();
				raster.setPixels(0, 0, size, 1, makeRasterArray());
			}
		}
		
		else if (dimensions == 2)
		{
			if (numStrategies == 2)
			{
				image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_BINARY);
				WritableRaster raster = image.getRaster();
				raster.setPixels(0, 0, size, size, makeRasterArray());
			}
			else if (numStrategies == 3)
			{
				image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
				WritableRaster raster = image.getRaster();
				raster.setPixels(0, 0, size, size, makeRasterArray());
			}
		}
	}
	
	public void initializePayoffMatrix()
	{
		payoffMatrix = new double[numStrategies][numStrategies];
		if (numStrategies == 2)
		{
			for (int i=0; i<numStrategies; i++)
			{
				for (int j=0; j<numStrategies; j++)
				{
					payoffMatrix[i][j] = DEFAULT_2STRATEGY_PAYOFF_MATRIX[i][j];
				}
			}
		}
		else if (numStrategies == 3)
		{
			for (int i=0; i<numStrategies; i++)
			{
				for (int j=0; j<numStrategies; j++)
				{
					payoffMatrix[i][j] = DEFAULT_3STRATEGY_PAYOFF_MATRIX[i][j];
				}
			}
		}
	}
	
	public void initializePayoffMatrix(double[][] payoffMatrix)
	{
		this.payoffMatrix = new double[numStrategies][numStrategies];
		if (numStrategies == payoffMatrix.length && numStrategies == payoffMatrix[0].length)
		{
			for (int i=0; i<numStrategies; i++)
			{
				for (int j=0; j<numStrategies; j++)
				{
					this.payoffMatrix[i][j] = payoffMatrix[i][j];
				}
			}
		}
		else
		{
			System.err.println("Argument length is invalid... EXITING");
			System.exit(1);
		}
	}
	
	public void chooseAndUpdateNode()
	{
		if (dimensions == 1)
		{
			updateNode1D(chooseNodeAtRandom1D());
		}
		else if (dimensions == 2)
		{
			updateNode2D(chooseNodeAtRandom2D());
		}
	}
	
	// 1D version updates the node based on which of the 8 update methods is used. Descriptions of each method
	// are too long to include in this documentation.
	public void updateNode1D(int node)
	{
		if (dimensions != 1)
		{
			System.err.println("This version of updateNode() is valid only for 1 dimension. Cancelling...");
			return;
		}
		
		switch(updateMethod)
		{
			case 0:
				updateMethodZero1D(node);
				break;
			case 1:
				updateMethodOne1D(node);
				break;
			case 2:
				updateMethodTwo1D(node);
				break;
			case 3:
				updateMethodThree1D(node);
				break;
			case 4:
				updateMethodFour1D(node);
				break;
			case 5:
				updateMethodFive1D(node);
				break;
			case 6:
				updateMethodSix1D(node);
				break;
			case 7:
				updateMethodSeven1D(node);
				break;
		}
		
	}
	
	// 2D version updates the node based on which of the 8 update methods is used. Descriptions of each method
	// are too long to include in this documentation.
	public void updateNode2D(Point node)
	{
		if (dimensions != 2)
		{
			System.err.println("This version of updateNode() is valid only for 2 dimensions. Cancelling...");
			return;
		}
		
		switch(updateMethod)
		{
			case 0:
				updateMethodZero2D(node);
				break;
			case 1:
				updateMethodOne2D(node);
				break;
			case 2:
				updateMethodTwo2D(node);
				break;
			case 3:
				updateMethodThree2D(node);
				break;
			case 4:
				updateMethodFour2D(node);
				break;
			case 5:
				updateMethodFive2D(node);
				break;
			case 6:
				updateMethodSix2D(node);
				break;
			case 7:
				updateMethodSeven2D(node);
				break;
		}
	}
	
	// The "Payoff affecting birth and death" update method
	public void updateMethodZero1D(int node)
	{
		
	}
	
	public void updateMethodZero2D(Point node)
	{
		double payoff = getPayoff2D(node);
		// Negative payoff => death rate. If the node dies, we replace it with a copy of a random neighbor.
		if (payoff < 0)
		{
			if (rand.nextDouble() <= (-payoff / getMaxRate()))
			{
				Point neighbor = chooseNeighborAtRandom2D(node);
				setStrategyAt2D(node, array[neighbor.x][neighbor.y]);
			}
		}
		// Positive payoff => birth rate. If the node gives birth, we replace a random neighbor with a copy of (node).
		else if (payoff > 0)
		{
			if (rand.nextDouble() <= (payoff / getMaxRate()))
			{
				Point neighbor = chooseNeighborAtRandom2D(node);
				setStrategyAt2D(neighbor, array[node.x][node.y]);
			}
		}
		// TODO: What about zero??
		// Zero payoff => 1/2 chance of dying, 1/2 chance of giving birth
//		else if (payoff == 0)
//		{
//			// Dying
//			if (rand.nextDouble() <= 0.5)
//			{
//				Point neighbor = chooseNeighborAtRandom2D(node);
//				setStrategyAt2D(node, array[neighbor.x][neighbor.y]);
//			}
//			// Giving birth
//			else
//			{
//				Point neighbor = chooseNeighborAtRandom2D(node);
//				setStrategyAt2D(neighbor, array[node.x][node.y]);
//			}
//		}
	}
	
	// The "Birth-death updating (linear)" update method
	public void updateMethodOne1D(int node)
	{
		
	}
	
	public void updateMethodOne2D(Point node)
	{
		double payoff = getPayoff2D(node);

		// Sites only give birth, and we can't have negative payoff, so we just copy the second half of update method 0.
		if (rand.nextDouble() <= (payoff / getMaxRate()))
		{
			Point neighbor = chooseNeighborAtRandom2D(node);
			setStrategyAt2D(neighbor, array[node.x][node.y]);
		}
	}
	
	// The "Death-birth process" update method. Due to Nowak. Technically, we calculate fitness = (1-w) + w*payoff,
	// where w is the selection parameter. However, we will deal only with cases in which w = 1, so fitness = payoff.
	public void updateMethodTwo1D(int node)
	{
		
	}
	
	public void updateMethodTwo2D(Point node)
	{
		// We keep track of the payoffs of each strategy.
		double[] strategyPayoffs = new double[numStrategies];
		double totalPayoff = 0;
		// Fill the payoffs array by checking the payoff of each neighbor and adding it to the appropriate cell.
		// Own payoff NOT included
		for (int i=0; i<4; i++)
		{
			Point thisNeighbor = getNeighborOf2D(node, i);
			double thisPayoff = getPayoff2D(thisNeighbor);
			strategyPayoffs[array[thisNeighbor.x][thisNeighbor.y]] += thisPayoff;
			totalPayoff += thisPayoff;
		}
		
		// Now we calculate the probability that the newly-dead node is replaced by each of the (numStrategies) strategies.
		double[] strategyProbabilities = new double[numStrategies];
		for (int i=0; i<numStrategies; i++)
		{
			strategyProbabilities[i] = strategyPayoffs[i] / totalPayoff;
			assert strategyProbabilities[i] <= 1;
			assert strategyProbabilities[i] >= 0;
		}
		
		// Finally, we use a random double to determine which strategy we use.
		
		double key = rand.nextDouble();
		double[] sumToI = new double[numStrategies];
		
		// We divide [0,1] into (numStrategies) intervals, with the ith interval of length
		// sum(strategyProbabilities[0...i]) - sum(strategyProbabilities[0...i-1])
		sumToI[0] = strategyProbabilities[0];
		for(int i=1; i<numStrategies; i++)
		{
			sumToI[i] = sumToI[i-1] + strategyProbabilities[i];
		}
		
		// If the key is in the ith interval, strategy i replaces (node).
		if (key > 0 && key <= sumToI[0])
			setStrategyAt2D(node, 0);
		else
			for(int i=1; i<numStrategies; i++)
			{
				if (key > sumToI[i-1] && key <= sumToI[i])
					setStrategyAt2D(node, i);
			}
	}
	
	// The "Imitation process" update method. This process is identical to case 2, but the payoff pool
	// includes the payoff of (node).
	public void updateMethodThree1D(int node)
	{
		
	}
	
	public void updateMethodThree2D(Point node)
	{
		// We keep track of the payoffs of each strategy.
		double[] strategyPayoffs = new double[numStrategies];
		double totalPayoff = 0;
		// Fill the payoffs array by checking the payoff of each neighbor and adding it to the appropriate cell.
		// Own payoff NOT included
		for (int i=0; i<4; i++)
		{
			Point thisNeighbor = getNeighborOf2D(node, i);
			double thisPayoff = getPayoff2D(thisNeighbor);
			strategyPayoffs[array[thisNeighbor.x][thisNeighbor.y]] += thisPayoff;
			totalPayoff += thisPayoff;
		}
		
		// Add the payoff of (node) to the pool.
		double ownPayoff = getPayoff2D(node);
		strategyPayoffs[array[node.x][node.y]] += ownPayoff;
		totalPayoff += ownPayoff;
		
		// Now we calculate the probability that the newly-dead node is replaced by each of the (numStrategies) strategies.
		double[] strategyProbabilities = new double[numStrategies];
		for (int i=0; i<numStrategies; i++)
		{
			strategyProbabilities[i] = strategyPayoffs[i] / totalPayoff;
			assert strategyProbabilities[i] <= 1 && strategyProbabilities[i] >= 0;
		}
		
		// Finally, we use a random double to determine which strategy we use.
		
		double key = rand.nextDouble();
		double[] sumToI = new double[numStrategies];
		
		// We divide [0,1] into (numStrategies) intervals, with the ith interval of length
		// sum(strategyProbabilities[0...i]) - sum(strategyProbabilities[0...i-1])
		sumToI[0] = strategyProbabilities[0];
		for(int i=1; i<numStrategies; i++)
		{
			sumToI[i] = sumToI[i-1] + strategyProbabilities[i];
		}
		
		// If the key is in the ith interval, strategy i replaces (node).
		if (key > 0 && key <= sumToI[0])
			setStrategyAt2D(node, 0);
		else
		{
			for(int i=1; i<numStrategies; i++)
			{
				if (key > sumToI[i-1] && key <= sumToI[i]) 
					setStrategyAt2D(node, i);
			}
		}
	}
	
	public void updateMethodFour1D(int node)
	{
		
	}
	
	// The "Birth-death of the least fit" update method.
	public void updateMethodFour2D(Point node)
	{
		// We allocate an array to store the payoff of each neighbor, using the usual 
		// clockwise-from-north indexing.
		double[] neighborPayoffs = new double[4];		
		for (int i=0; i<neighborPayoffs.length; i++)
		{
			neighborPayoffs[i] = getPayoff2D(getNeighborOf2D(node, i));
		}
		
		// We find the minimum payoff
		double minPayoff = Double.POSITIVE_INFINITY;
		for (int i=0; i<neighborPayoffs.length; i++)
		{
			if (neighborPayoffs[i] < minPayoff)
			{
				minPayoff = neighborPayoffs[i];
			}
		}
		
		// We choose random directions until we get a node that has minimum payoff.
		int directionToReplace = -1;
		int i = 0;
		while (true)
		{
			int key = rand.nextInt(neighborPayoffs.length);
			if (neighborPayoffs[key] == minPayoff)
			{
				// Replace the strategy at the chosen neighbor with a copy of the strategy
				// at (node)
				directionToReplace = key;
				Point pointToReplace = getNeighborOf2D(node, directionToReplace);
				setStrategyAt2D(pointToReplace, array[node.x][node.y]);
				return;
			}
			i++;
//			if (i > 50)
//				System.out.println("Somewhat infinite loop at Lattice.java line 584.");
		}
	}
	
	// The "Death-birth of the fittest" model
	public void updateMethodFive1D(int node)
	{
		
	}
	
	public void updateMethodFive2D(Point node)
	{
		// We allocate an array to store the payoff of each neighbor, using the usual 
		// clockwise-from-north indexing.
		double[] neighborPayoffs = new double[4];
		int[] neighborStrategies = new int[4];
		for (int i=0; i<neighborPayoffs.length; i++)
		{
			Point thisNeighbor = getNeighborOf2D(node, i);
			neighborPayoffs[i] = getPayoff2D(thisNeighbor);
			neighborStrategies[i] = getStrategyAt2D(thisNeighbor);
		}
		
		// We find the maximum payoff
		double maxPayoff = Double.NEGATIVE_INFINITY;
		for (int i=0; i<neighborPayoffs.length; i++)
		{
			if (neighborPayoffs[i] > maxPayoff)
			{
				maxPayoff = neighborPayoffs[i];
			}
		}
		
		// We choose random nodes until we get a node that has minimum payoff.
		while (true)
		{
			int key = rand.nextInt(neighborPayoffs.length);
			if (neighborPayoffs[key] == maxPayoff)
			{
				// Replace (node) with the chosen strategy.
				setStrategyAt2D(node, neighborStrategies[key]);
				return;
			}
		}
	}
	
	// The "Imitation of the fittest" model. It's the same as above, but instead of dying,
	// the node may choose to "imitate itself" (i.e., not change).
	public void updateMethodSix1D(int node)
	{
		
	}
	
	public void updateMethodSix2D(Point node)
	{		
		// We allocate an array to store the payoff of each neighbor, using the usual 
		// clockwise-from-north indexing.
		double[] neighborPayoffs = new double[4];
		int[] neighborStrategies = new int[4];
		for (int i=0; i<neighborPayoffs.length; i++)
		{
			Point thisNeighbor = getNeighborOf2D(node, i);
			neighborPayoffs[i] = getPayoff2D(thisNeighbor);
			neighborStrategies[i] = getStrategyAt2D(thisNeighbor);
		}
		// We also find the payoff of (node) 
		double nodePayoff = getPayoff2D(node);
		
		// We find the maximum payoff, first checking each of the neighbor payoffs,
		// then moving to (node)'s payoff.
		double maxPayoff = Double.NEGATIVE_INFINITY;
		for (int i=0; i<neighborPayoffs.length; i++)
		{
			if (neighborPayoffs[i] > maxPayoff)
			{
				maxPayoff = neighborPayoffs[i];
			}
		}
		if (nodePayoff > maxPayoff)
			maxPayoff = nodePayoff;
		
		// Finally, we produce a random integer between 0 and (numStrategies) + 1.
		// If the random integer coincides with a neighbor who has maximum payoff,
		// we change the strategy at node to match the strategy at that neighbor; if
		// it coincides with the node itself, we do nothing and return.
		while (true)
		{
			int key = rand.nextInt(neighborPayoffs.length + 1);
			if (key < neighborPayoffs.length)
			{
				if (neighborPayoffs[key] == maxPayoff)
				{
					// Replace (node) with the chosen strategy.
					setStrategyAt2D(node, neighborStrategies[key]);
					return;
				}	
			}
			else if (nodePayoff == maxPayoff)
				return;
		}
	}
	
	// "Best Response Dynamics". Choose the best option among all possible payoffs.
	public void updateMethodSeven1D(int node)
	{

	}
	
	public void updateMethodSeven2D(Point node)
	{
		// We first figure out what the best payoff is.
		double bestPayoff = Double.NEGATIVE_INFINITY;
		for (int i=0; i<numStrategies; i++)
		{
			double thisPayoff = getPayoffGivenStrategy2D(node, i);
			if (thisPayoff > bestPayoff)
			{
				bestPayoff = thisPayoff;
			}
		}
		// We then figure out how many strategies have the best payoff, and then
		// choose one of those strategies uniformly at random.
		int tiedStrategyCount = 0;
		ArrayList<Integer> bestStrategies = new ArrayList<Integer>();
		for (int i=0; i<numStrategies; i++)
		{
			if (getPayoffGivenStrategy2D(node, i) == bestPayoff)
			{
				bestStrategies.add(i);
				tiedStrategyCount++;
			}
		}
		setStrategyAt2D(node, bestStrategies.get(rand.nextInt(tiedStrategyCount)));
	}
	
	// Computes the maximum rate birth or death (necessary for update methods 0 and 1).
	public double getMaxRate()
	{
		double maxAbsolutePayoffCoefficient = Double.NEGATIVE_INFINITY;
		for(int i=0; i<numStrategies; i++)
		{
			for(int j=0; j<numStrategies; j++)
			{
				double thisAbsPayoffCoefficient = Math.abs(payoffMatrix[i][j]);
				if (thisAbsPayoffCoefficient > maxAbsolutePayoffCoefficient)
				{
					maxAbsolutePayoffCoefficient = thisAbsPayoffCoefficient;
				}
			}
		}
		assert maxAbsolutePayoffCoefficient > 0;

		int numberOfNeighbors = -1;
		
		if (dimensions == 1)
		{
			numberOfNeighbors = 2*interactionRange;
		}
		else if (dimensions == 2)
		{
			numberOfNeighbors = 4;								// I'm just hardcoding this 4... it shouldn't come up very often.
		}
		
		assert numberOfNeighbors > 0;
		return numberOfNeighbors * maxAbsolutePayoffCoefficient;
	}
	
	// Computes what the payoff of (node) would be if it had (strategy)
	public double getPayoffGivenStrategy1D(int node, int strategy)
	{
		double payoff = 0;
		if (dimensions != 1)
		{
			System.err.println("This version of getPayoff() does not work in 2D. RETURNING -1.");
			return -1;
		}
		
		// Get the payoff contribution from the interactionRange nodes west and east of node.
		for (int i=-interactionRange; i<=interactionRange; i++)
		{
			if (i != 0)
				payoff += payoffMatrix[strategy][array[getNeighborOf1D(node, 0, i)][0]];
		}
		return payoff;
	}
	
	public double getPayoffGivenStrategy2D(Point node, int strategy)
	{
		double payoff = 0;
		
		if (dimensions != 2)
		{
			System.err.println("This verison of getPayoff() does not work in 1D. RETURNING -1.");
			return -1;
		}

		// Get the payoff contribution from the four nearest neighbors of (node).
		for (int i=0; i<4; i++)			
		{
			Point neighbor = getNeighborOf2D(node, i);
			payoff += payoffMatrix[strategy][array[neighbor.x][neighbor.y]];
		}

		return payoff;
	}
	
	// 1D version returns the payoff of (node) as the sum of the contributions of its 2*(interactionRange) neighbors.
	public double getPayoff1D(int node)
	{
		double payoff = 0;
		if (dimensions != 1)
		{
			System.err.println("This version of getPayoff() does not work in 2D. RETURNING -1.");
			return -1;
		}
		
		// Get the payoff contribution from the interactionRange nodes west and east of node.
		for (int i=-interactionRange; i<=interactionRange; i++)
		{
			if (i != 0)
				payoff += payoffMatrix[array[node][0]][array[getNeighborOf1D(node, 0, i)][0]];
		}
		
		return payoff;
	}
	
	// 2D version returns the payoff of (node) as the sum of the contributions of its four nearest neighbors.
	public double getPayoff2D(Point node)
	{
		double payoff = 0;
		
		if (dimensions != 2)
		{
			System.err.println("This verison of getPayoff() does not work in 1D. RETURNING -1.");
			return -1;
		}

		// Get the payoff contribution from the four nearest neighbors of (node).
		for (int i=0; i<4; i++)			
		{
			Point neighbor = getNeighborOf2D(node, i);
			payoff += payoffMatrix[array[node.x][node.y]][array[neighbor.x][neighbor.y]];
		}
		return payoff;
	}
	
	// 1D version makes a random int and chooses a corresponding neighbor of (node).
	public int chooseNeighborAtRandom1D(int node)
	{
		
		int direction = rand.nextInt(2);						// There are 2 possible directions in 1D
		int distance = rand.nextInt(interactionRange) + 1;		// The distance can only range from 1 to interactionRange
		return getNeighborOf1D(node, direction, distance);
	}
	
	public Point chooseNeighborAtRandom2D(Point node)
	{
		
		int direction = rand.nextInt(4);						// There are 4 possible directions in 2D
		return getNeighborOf2D(node, direction);
	}
	
	// The 1D version returns the node distance away from (node) in direction (direction).
	// (direction) = 0 => west, direction = 1 => east.
	public int getNeighborOf1D(int node, int direction, int distance)
	{
		if (dimensions != 1)
		{
			System.err.println("This version of getNieghborOf() does not work in 2D. RETURNING -1");
			return -1;
		}
		switch(direction)
		{
			case 0:
				return (node - distance + size) % size;
			case 1:
				return (node + distance + size) % size;
		}
		System.err.println("The mode must be invalid. RETURNING -1.");
		return -1;
	}
	
	// The 2D version returns the nearest neighbor of (node) in direction (direction).
	// (direction) = 0 => north, and we go clockwise from there.
	public Point getNeighborOf2D(Point node, int direction)
	{
		if (dimensions != 2)
		{
			System.err.println("This verison of getNieghborOf() does not work in 1D. RETURNING NULL.");
			return null;
		}
		Point result = null;
		switch(direction)
		{
			case 0:
				result = (new Point(node.x, (node.y - 1 + size) % size));
				break;
			case 1:
				result = (new Point((node.x + 1 + size) % size, node.y));
				break;
			case 2:
				result = (new Point(node.x, (node.y + 1 + size) % size));
				break;
			case 3:
				result = (new Point((node.x - 1 + size) % size, node.y));
				break;
			default:
				break;
		}
		
//		System.out.println(direction + " " + -(node.x-result.x) + "," + -(node.y-result.y));
		return result;
	}
	
	// 1D version returns a randomly selected int between 0 and size-1.
	protected int chooseNodeAtRandom1D()
	{
		if (dimensions != 1)
		{
			System.err.println("This version of chooseNodeAtRandom is invalid for 2 dimensions. RETURNING -1.");
			return -1;
		}
		
		return rand.nextInt(size);
	}
	
	// 2D version returns a Point with randomly selected x- and y-coordinates between 0 and size-1.
	protected Point chooseNodeAtRandom2D()
	{
		if (dimensions != 2)
		{
			System.err.println("This version of chooseNodeAtRandom is invalid for 1 dimension. RETURNING NULL.");
			return null;
		}
		
		return new Point(rand.nextInt(size), rand.nextInt(size));
	}
	
	// 1D version paints a single row of cells based on the stored BufferedImage at the location (yLocation).
	// (size) must be passed to the method, and will be a variable of the drawing environment (the driver JApplet).
	public void draw1D(Graphics g, int yLocation, int size)
	{
		g.drawImage(image, 0, yLocation, size, 1, Color.white, null);
	}

	// 2D version paints the whole grid
	public void draw2D(Graphics g, int size)
	{
		g.drawImage(image, 0, 0, size, size, Color.white, null);
	}
	
	// Returns false and prints an error-type-dependent message if there is an error; otherwise, returns true.
	public boolean checkMode()
	{
		switch (getModeErrors())
		{
			case 0:
				return true;
			case -8:
				System.err.println("Cannot have negative payoff matrix coefficients with update method 1, 2, or 3.");
				return false;
			case -6:
				System.err.println("Payoff matrix improperly initialized.");
				return false;
			case -5:
				System.err.println("Initial proportions matrix improperly initialized.");
				return false;
			case -4:
				System.err.println("Interaction range > 0 in 2 dimensions.");
				return false;
			case -3:
				System.err.println("Array is 1 dimensional in 2 dimensions.");
				return false;
			case -2:
				System.err.println("Array is 2 dimensional in 1 dimension.");
				return false;
			case -1:
				System.err.println("Array is incorrect size.");
				return false;
			case 1:
				System.err.println("Array is null.");
				return false;
			case 2:
				System.err.println("Payoff matrix is null or not square.");
				return false;
			case 3:
				System.err.println("Dimensions must be 1 or 2.");
				return false;
			case 4:
				System.err.println("Number of strategies must be 2 or 3.");
				return false;
			case 5:
				System.err.println("Update method must be between 0 and 7.");
				return false;
		}
		return true;
	}

	// Goes through the possible inconsistencies in the current mode and returns an integer depending
	// on which error is finds.
	protected int getModeErrors()
	{
		// array requirements
		if (array == null)
			return 1;
				
		// payoffMatrix requirements
		if (payoffMatrix == null)
			return 2;
		if (!(payoffMatrix.length == payoffMatrix[0].length))
			return 2;

		// dimensions requirements
		if (dimensions != 1 && dimensions != 2)
			return 3;
		
		// numStrategies requirements
		if (numStrategies != 2 && numStrategies != 3)
			return 4;
		
		// updateMethod requirements
		if (updateMethod < 0 || updateMethod > 7)
			return 5;
		
		// Interdependencies
		if (array.length != size)
			return -1;
		if (dimensions == 1 && array[0].length > 1)
			return -2;
		if (dimensions == 2 && array[0].length <= 0)
			return -3;
		if (dimensions == 2 && interactionRange > 1)
			return -4;
		if (initProportionMasks.length != numStrategies)
			return -5;
		if (payoffMatrix.length != numStrategies)
			return -6;
		if (updateMethod == 1 || updateMethod == 2 || updateMethod == 3)
		{
			for (int i=0; i<payoffMatrix.length; i++)
			{
				for (int j=0; j<payoffMatrix[0].length; j++)
				{
					if (payoffMatrix[i][j] < 0)
						return -8;
				}
			}
		}
		return 0;
	}

	// This method creates a double[] that can be used to define a raster.
	// This method is unnecessary for two strategies, as we can simply use
	// the instance variable (array) to initialize/change a raster.
	public double[] makeRasterArray()
	{
		if (!checkMode())
		{
			System.err.println("Mode is invalid. Cannot make raster array. RETURNING NULL.");
			return null;
		}
		
		double result[] = null;
		
		// IN 1 DIMENSION
		if (dimensions == 1)
		{
			if (numStrategies == 2)
			{
				result = new double[size];
				for (int i=0; i<result.length; i++)
				{
					result[i] = array[i][0];
				}
			}
			// R, G, and B values are necessary, so (result) must be length 3*(size).
			else if (numStrategies > 2)
			{
				result = new double[3*size];
				// Outer for-loop goes through each node (adds 3 for R, G, B).
				for (int i=0; i<result.length; i += 3)
				{
					// Inner for-loop goes over the three colors for each node (3 for R, G, B).
					for (int j=0; j<3; j++)
					{
						result[i] = STRATEGY_COLORS[array[i/3][0]].getRed();
						result[i+1] = STRATEGY_COLORS[array[i/3][0]].getGreen();
						result[i+2] = STRATEGY_COLORS[array[i/3][0]].getBlue();
					}
				}
			}
		}
		
		// IN 2 DIMENSIONS 
		else if (dimensions == 2)
		{
			if (numStrategies == 2)
			{
				result = new double[size*size];
				for (int i=0; i<array.length; i++)
				{
					for (int j=0; j<array[0].length; j++)
					{
						result[size*i + j] = array[i][j];
					}
				}
			}
			else if (numStrategies > 2)
			{
				// R, G, and B values are necessary, so (result) must be length 3*(size)*(size).
				result = new double[3*size*size];
				
				// Outer for-loop goes through each node (adds 3 for R, G, B).
				for (int i=0; i<result.length; i += 3)
				{
					// Inner for-loop goes over the three colors for each node (3 for R, G, B).
					for (int j=0; j<3; j++)
					{
						result[i] = STRATEGY_COLORS[array[(i/3)/size][(i/3)%size]].getRed();
						result[i+1] = STRATEGY_COLORS[array[(i/3)/size][(i/3)%size]].getGreen();
						result[i+2] = STRATEGY_COLORS[array[(i/3)/size][(i/3)%size]].getBlue();
					}
				}
			}
		}
		return result;
	}

	public int getStrategyAt1D(int node)
	{
		if (dimensions != 1)
		{
			System.err.println("This verion of getStrategyAt() is only valid in 1D. RETURNING -1.");
			return -1;
		}
		return array[node][0];
	}
	
	public int getStrategyAt2D(Point node)
	{
		if (dimensions != 2)
		{
			System.err.println("This verion of getStrategyAt() is only valid in 2D. RETURNING -1.");
			return -1;
		}
		return array[node.x][node.y];
	}
	
	public void setStrategyAt1D(int node, int strategy)
	{
		if (strategy < 0 || strategy > numStrategies-1)
		{
			System.err.println("Attempt to set node to invalid strategy. EXITING.");
			System.exit(1);
		}
		if (dimensions != 1)
		{
			System.err.println("This verion of setStrategyAt() is only valid in 1D. EXITING.");
			System.exit(1);
		}
		
		// We decrement the strategy count for the strategy at the node being replaced, then
		// increment the strategy count for the strategy that's doing the replacing.
		strategyCounts[array[node][0]]--;
		array[node][0] = strategy;
		strategyCounts[strategy]++;
		WritableRaster raster = image.getRaster();
		if (numStrategies == 2)
		{
			double[] changedPoint = {strategy};
			raster.setPixel(node, 0, changedPoint);
		}
		if (numStrategies == 3)
		{
			double[] changedPoint = {STRATEGY_COLORS[strategy].getRed(),
									 STRATEGY_COLORS[strategy].getGreen(),
									 STRATEGY_COLORS[strategy].getBlue()};
			raster.setPixel(node, 0, changedPoint);
		}
	}
	
	public void setStrategyAt2D(Point node, int strategy)
	{
		if (strategy < 0 || strategy > numStrategies-1)
		{
			System.err.println("Attempt to set node to invalid strategy. EXITING.");
		}
		if (dimensions != 2)
		{
			System.err.println("This verion of setStrategyAt() is only valid in 2D. EXITING.");
			System.exit(1);
		}
		
		// We decrement the strategy count for the strategy at the node being replaced, then
		// increment the strategy count for the strategy that's doing the replacing.
		strategyCounts[array[node.x][node.y]]--;
		array[node.x][node.y] = strategy;
		strategyCounts[strategy]++;
		WritableRaster raster = image.getRaster();
		if (numStrategies == 2)
		{
			double[] changedPoint = {strategy};
			raster.setPixel(node.x, node.y, changedPoint);
		}
		else if (numStrategies == 3)
		{
			double[] changedPoint = {STRATEGY_COLORS[strategy].getRed(), 
									 STRATEGY_COLORS[strategy].getGreen(),
									 STRATEGY_COLORS[strategy].getBlue()};
			raster.setPixel(node.x, node.y, changedPoint);
		}
	}
	
	public double getPayoffCoefficient(int i, int j)
	{
		return payoffMatrix[i][j];
	}
	
	public void setPayoffCoefficient(double payoff, int i, int j)
	{
		payoffMatrix[i][j] = payoff;
	}
	
	public double[][] getPayoffMatrix()
	{
		return payoffMatrix;
	}

	public void setPayoffMatrix(double[][] payoffMatrix)
	{
		this.payoffMatrix = payoffMatrix;
	}

	public BufferedImage getImage()
	{
		return image;
	}

	public void setImage(BufferedImage image)
	{
		this.image = image;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public int getDimensions()
	{
		return dimensions;
	}

	public void setDimensions(int dimensions)
	{
		this.dimensions = dimensions;
		initializeLattice();
	}

	public int getNumStrategies()
	{
		return numStrategies;
	}

	public void setNumStrategies(int numStrategies)
	{
		if (this.numStrategies != numStrategies)
		{
			// Changing number of strategies from 2 to 3
			if (this.numStrategies == 2)
			{
				double[][] dummyPayoffMatrix = new double[numStrategies][numStrategies];
				double[] dummyInitProportionMasks = new double[numStrategies];
				for (int i=0; i<numStrategies; i++)
				{
					if (i<this.numStrategies)
						dummyInitProportionMasks[i] = this.initProportionMasks[i];
					else
						dummyInitProportionMasks[i] = DEFAULT_3STRATEGY_INIT_PROPORTION_MASKS[i];
					for (int j=0; j<numStrategies; j++)
					{
						if (i<this.numStrategies && j<this.numStrategies)
						{
							dummyPayoffMatrix[i][j] = this.payoffMatrix[i][j];
						}
						else
						{
							dummyPayoffMatrix[i][j] = DEFAULT_3STRATEGY_PAYOFF_MATRIX[i][j];
						}
					}
				}
				this.numStrategies = numStrategies;
				initializeLattice(dummyInitProportionMasks, dummyPayoffMatrix);
			}
			// Changing number of strategies from 3 to 2
			else if (this.numStrategies == 3)
			{
				double[][] dummyPayoffMatrix = new double[numStrategies][numStrategies];
				double[] dummyInitProportionMasks = new double[numStrategies];
				for (int i=0; i<numStrategies; i++)
				{
					dummyInitProportionMasks[i] = this.initProportionMasks[i];
					for (int j=0; j<numStrategies; j++)
					{
						dummyPayoffMatrix[i][j] = this.payoffMatrix[i][j];
					}
				}
				this.numStrategies = numStrategies;
				initializeLattice(dummyInitProportionMasks, dummyPayoffMatrix);
			}
		}
	}
	
	public int getUpdateMethod()
	{
		return updateMethod;
	}

	public void setUpdateMethod(int updateMethod)
	{
		this.updateMethod = updateMethod;
	}

	public int getInteractionRange()
	{
		return interactionRange;
	}
	
	public void setInteractionRange(int interactionRange)
	{
		this.interactionRange = interactionRange;
	}
	
	public double getProportionOf(int strategy)
	{
		if (dimensions == 1)
			return (double) strategyCounts[strategy] / (double) size;
		else if (dimensions == 2)
			return (double) strategyCounts[strategy] / (double) (size*size);

		System.err.println("Somehow, you lost track of dimensions, and it's neither 1 nor 2... RETURNING -1.");
		return -1;

	}
	
	public double getInitProportionOf(int strategy)
	{
		double sum = 0;
		for (int i=0; i<initProportionMasks.length; i++)
		{
			sum += initProportionMasks[i];
		}
		return (double) initProportionMasks[strategy] / (double) sum;
	}
	
	public double getInitProportionMaskOf(int strategy)
	{
		return initProportionMasks[strategy];
	}
	
	public void setInitProportionMaskOf(int strategy, double proportion)
	{
		initProportionMasks[strategy] = proportion;
	}
	
	public int getStrategyCountOf(int strategy)
	{
		return strategyCounts[strategy];
	}
	
	public int[] getStrategyCounts()
	{
		return strategyCounts;
	}

	public void setStrategyCounts(int[] strategyCounts)
	{
		this.strategyCounts = strategyCounts;
	}

	public void setInitProportionMasks(double[] initProportionMasks)
	{
		this.initProportionMasks = initProportionMasks;
	}
	
	public static String getStrategyColorName(int strategy)
	{
		return STRATEGY_COLOR_NAMES[strategy];
	}
}
