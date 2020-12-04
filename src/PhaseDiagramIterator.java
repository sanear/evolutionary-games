import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class PhaseDiagramIterator
{
	// DEFAULTS
	private final double DEFAULT_WIDTH = 10; 		// Width in units of each axis.
	private final int DEFAULT_RESOLUTION = 3;		// Number of iteration points on each axis. MUST BE ODD.
	private final double DEFAULT_A12 = 2;
	private final double DEFAULT_A21 = 3;
	private final int DEFAULT_STOP_TIME = 100;
	private final int DEFAULT_UPDATE_METHOD = 0;
	
	// @param variables
	private double a12, a21, width;
	private int resolution, stopTime, updateMethod;
	private double[][] payoffMatrix;
	private Vector<Double> xRange, yRange;
	private Lattice lattice;
	private double[][] proportions;
	
	
	public PhaseDiagramIterator()
	{
		a12 = DEFAULT_A12;
		a21 = DEFAULT_A21;
		width = DEFAULT_WIDTH;
		resolution = DEFAULT_RESOLUTION;
		stopTime = DEFAULT_STOP_TIME;
		updateMethod = DEFAULT_UPDATE_METHOD; 
		xRange = new Vector<Double>();
		yRange = new Vector<Double>();
		proportions = new double[resolution][resolution];
		
		// We set the initial x- and y-points, and then iterate through the array,
		// setting the other appropriate points.
		for (int i=0; i<resolution; i++)
		{
			xRange.add((double) (a12-width/2) + (double) (i*(width/(resolution-1))));
			yRange.add((double) (a21-width/2) + (double) (i*(width/(resolution-1))));
		}
		payoffMatrix = new double[2][2];
		payoffMatrix[0][0] = xRange.get(0);
		payoffMatrix[0][1] = a12;
		payoffMatrix[1][0] = a21;
		payoffMatrix[1][1] = yRange.get(0);
	}
	
	public PhaseDiagramIterator(int a12, int a21, int resolution, int stopTime, int updateMethod)
	{
		assert updateMethod < 8 && updateMethod >= 0;
		
		this.a12 = a12;
		this.a21 = a21;
		width = DEFAULT_WIDTH;
		this.resolution = resolution; 
		this.stopTime = stopTime;
		this.updateMethod = updateMethod;
		xRange = new Vector<Double>(resolution);
		yRange = new Vector<Double>(resolution);
		proportions = new double[resolution][resolution];
		
		// We set the initial x- and y-points, and then iterate through the array,
		// setting the other appropriate points.
		for (int i=0; i<resolution; i++)
		{
			xRange.add((double) (a12-width/2) + (double) (i*(width/(resolution-1))));
			yRange.add((double) (a21-width/2) + (double) (i*(width/(resolution-1))));
		}
		payoffMatrix = new double[2][2];
		payoffMatrix[0][0] = xRange.get(0);
		payoffMatrix[0][1] = a12;
		payoffMatrix[1][0] = a21;
		payoffMatrix[1][1] = yRange.get(0);
	}
	
	// Method iterates through the established payoff matrices and records the resulting
	// proportion black at each step.
	public void iterate()
	{
		long baseTime = System.currentTimeMillis();
		long minTime = Integer.MAX_VALUE;
		long maxTime = Integer.MIN_VALUE;
		long avgTime = 0;
		int count = 0;
		for (int i=0; i<resolution; i++)
		{
			for (int j=0; j<resolution; j++)
			{
				payoffMatrix[0][0] = xRange.get(i);
				payoffMatrix[1][1] = yRange.get(j);
				lattice = new Lattice(2, 2, 1, new double[] {1.0,1.0}, updateMethod, payoffMatrix);
				int time = 0;
				boolean stop = false;
				for (int k=0; time <= stopTime && !stop; k++)
				{
					time = (int) ((double) k / (double) (lattice.getMaxRate()*lattice.getSize()*lattice.getSize()));
					if (lattice.getProportionOf(0) == 1.0 || lattice.getProportionOf(1) == 1.0)
						stop = true;
					lattice.chooseAndUpdateNode();
					if (k % 10000000 == 0)
						System.out.println(k);
				}
				proportions[i][j] = lattice.getProportionOf(0);
				
				// Time stuff
				long theTime = System.currentTimeMillis() - baseTime;
				avgTime = (count * avgTime + theTime) / ++count;
				if (theTime < minTime)
					minTime = theTime;
				if (theTime > maxTime)
					maxTime = theTime;
				
				System.out.println(count + ": " + "Delta: " + theTime + "; Min: " + minTime + "; Max: " + maxTime + "; Average: " + avgTime + ".");
				baseTime = System.currentTimeMillis();
			}
		}
	}

	public double getWidth()
	{
		return width;
	}

	public void setWidth(double width)
	{
		this.width = width;
	}

	public int getResolution()
	{
		return resolution;
	}

	public void setResolution(int resolution)
	{
		this.resolution = resolution;
	}

	public int getStopTime()
	{
		return stopTime;
	}

	public void setStopTime(int stopTime)
	{
		this.stopTime = stopTime;
	}

	public Vector<Double> getxRange()
	{
		return xRange;
	}

	public void setxRange(Vector<Double> xRange)
	{
		this.xRange = xRange;
	}

	public Vector<Double> getyRange()
	{
		return yRange;
	}

	public void setyRange(Vector<Double> yRange)
	{
		this.yRange = yRange;
	}

	public double[][] getProportions()
	{
		return proportions;
	}

	public void setProportions(double[][] proportions)
	{
		this.proportions = proportions;
	}
}
