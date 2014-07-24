/*   
   Copyright 2011-2013 The Cassandra Consortium (cassandra-fp7.eu)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package eu.cassandra.sim.math;

import eu.cassandra.sim.utilities.Constants;

/**
 * A uniform probability distribution.
 * 
 * @author Antonios Chrysopoulos
 */
public class Uniform implements ProbabilityDistribution {
	/**
	 * The name of the distribution.
	 */
	private String name = "";

	/**
	 * The type of the distribution.
	 */
	private String type = "";

	/**
	 * A boolean variable that shows if the values of the distribution
	 * histogram has been precomputed or not.
	 */
	protected boolean precomputed;

	/**
	 * A variable presenting the number of bins that are created for the histogram
	 * containing the values of the distribution.
	 */
	protected int numberOfBins;

	/**
	 * The starting point of the bins for the precomputed values.
	 */
	protected double precomputeFrom;

	/**
	 * The ending point of the bins for the precomputed values.
	 */
	protected double precomputeTo;

	/**
	 * An array containing the probabilities of each bin precomputed for the distribution.
	 */
	protected double[] histogram;

	/**
	 * This is an array that contains the probabilities that the distribution has
	 * value over a threshold.
	 */
	private double[] greaterProbability;

	
	/**
	 * Instantiates a new uniform distribution.
	 *
	 * @param start          Starting value of the Uniform distribution.
	 * @param end          Ending value of the Uniform distribution.
	 * @param startTime          variable that shows if this is a start time distribution or not.
	 */
	public Uniform (double start, double end, boolean startTime)
	{
		name = "Generic";
		type = "Uniform Distribution";
		precomputeFrom = start;
		precomputeTo = end;

		if (startTime)
		{
			start = Math.max(end-1,0);
			end = Math.min(end-1, Constants.MIN_IN_DAY-1);
			precompute((int) start, (int) end, Constants.MIN_IN_DAY);
		}
		else
			precompute((int) start, (int) end, (int) (end + 1));

		estimateGreaterProbability();
	}

	/**
	 * Instantiates a new uniform distribution, by copying another one of the same type.
	 *
	 * @param source the source uniform distribution
	 */
	public Uniform (Uniform source)
	{
		name = "Generic";
		type = "Uniform Distribution";
		precomputeFrom = source.precomputeFrom;
		precomputeTo = source.precomputeTo;
		precomputed = source.precomputed;
		greaterProbability = source.greaterProbability.clone();
		numberOfBins = source.numberOfBins;
		histogram = source.histogram.clone();
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getType()
	 */
	@Override
	public String getType ()
	{
		return "Uniform";
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getDescription()
	 */
	@Override
	public String getDescription ()
	{
		String description = "Uniform probability density function";
		return description;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getNumberOfParameters()
	 */
	@Override
	public int getNumberOfParameters ()
	{
		return 2;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getParameter(int)
	 */
	@Override
	public double getParameter (int index)
	{
		switch (index) {
		case 0:
			return precomputeFrom;
		case 1:
			return precomputeTo;
		default:
			return 0.0;
		}

	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#setParameter(int, double)
	 */
	@Override
	public void setParameter (int index, double value)
	{
		switch (index) {
		case 0:
			precomputeFrom = value;
			break;
		case 1:
			precomputeTo = value;
			break;
		default:
			return;
		}
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getProbability(double)
	 */
	@Override
	public double getProbability (double x)
	{
		if (x > precomputeTo || x < precomputeFrom) {
			return 0.0;
		}
		else {
			if (precomputeTo == precomputeFrom && x == precomputeTo)
				return 1.0;
			else
				return 1.0 / (precomputeTo - precomputeFrom + 1);
		}
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getPrecomputedProbability(double)
	 */
	@Override
	public double getPrecomputedProbability (double x)
	{
		if (!precomputed) {
			return -1;
		}
		else if (x > precomputeTo || x < precomputeFrom) {
			return -1;
		}
		return histogram[(int) (x - precomputeFrom)];
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getPrecomputedBin(double)
	 */
	@Override
	public int getPrecomputedBin (double rn)
	{
		if (!precomputed) {
			return -1;
		}
		// double div = (precomputeTo - precomputeFrom) / (double) numberOfBins;
		double dice = rn;
		double sum = 0;
		for (int i = 0; i < numberOfBins; i++) {
			sum += histogram[i];
			// if(dice < sum) return (int)(precomputeFrom + i * div);
			if (dice < sum)
				return i;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getHistogram()
	 */
	@Override
	public double[] getHistogram ()
	{
		if (precomputed == false) {
			System.out.println("Not computed yet!");
			return null;
		}

		return histogram;
	}

	/**
	 * Gets an array that contains the probabilities that the distribution has value over a threshold.
	 *
	 * @return the array that contains the probabilities that the distribution has value over a threshold
	 */
	public double[] getGreaterProbability ()
	{
		return greaterProbability;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#status()
	 */
	@Override
	public void status ()
	{
		System.out.print("Uniform Distribution with ");
		System.out.println("Precomputed: " + precomputed);
		if (precomputed) {
			System.out.print("Number of Beans: " + numberOfBins);
			System.out.print(" Starting Point: " + precomputeFrom);
			System.out.println(" Ending Point: " + precomputeTo);
		}
		System.out.println();

	}

	/**
	 * Gets the name of the distribution.
	 *
	 * @return the name of the distribution
	 */
	public String getName ()
	{
		return name;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getProbabilityGreater(int)
	 */
	@Override
	public double getProbabilityGreater (int x)
	{
		double prob = 0;

		int start = x;

		for (int i = start+1; i < histogram.length; i++)
			prob += histogram[i];

		return prob;
	}

	/**
	 * Estimate greater probability.
	 */
	private void estimateGreaterProbability ()
	{
		greaterProbability = new double[histogram.length];

		for (int i = 0; i < histogram.length; i++)
			greaterProbability[i] = getProbabilityGreater(i);

	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#precompute(double, double, int)
	 */
	@Override
	public void precompute (double startValue, double endValue, int nBins) {
		precompute((int)startValue, (int) endValue, nBins);
	}


	/**
	 * Precompute.
	 *
	 * @param startValue the start value
	 * @param endValue the end value
	 * @param nBins the n bins
	 */
	public void precompute (int startValue, int endValue, int nBins)
	{
		numberOfBins = nBins;
		histogram = new double[nBins];

		if (startValue > endValue) {
			System.out.println("Starting point greater than ending point");
			return;
		}
		else {

			for (int i = startValue; i <= endValue; i++) {

				histogram[i] = 1.0 / (precomputeTo - precomputeFrom + 1);

			}
		}
		precomputed = true;
	}
}