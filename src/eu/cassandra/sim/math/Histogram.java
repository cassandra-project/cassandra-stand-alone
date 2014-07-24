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

public class Histogram implements ProbabilityDistribution{

	/** The number of bins. */
	protected int numberOfBins;
	
	/** The starting point of the bins for the precomputed values. */
	protected double precomputeFrom;
	
	/** The ending point of the bins for the precomputed values. */
	protected double precomputeTo;
	
	/** The histogram values. */
	protected double[] histogram;
	
	/** A boolean variable that shows if the values of the distribution histogram has been precomputed or not. */
	protected boolean precomputed;

	/**
	 * Instantiates a new histogram of the given size.
	 *
	 * @param size the size
	 */
	public Histogram(int size){

		precomputeFrom = 0;
		precomputeTo = size;
		numberOfBins = size;
		histogram = new double[size];
		precomputed = false;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getType()
	 */
	@Override
	public String getType()
	{
		return "Histogram";
	}

	/**
	 * Instantiates a new histogram, based on the provided set of values.
	 *
	 * @param values the set of values
	 */
	public Histogram(double[] values){

		precomputeFrom = 0;
		precomputeTo = values.length;
		numberOfBins = values.length;
		histogram = values;
		precomputed = true;

	}

	/**
	 * Instantiates a new histogram, by copying another one.
	 *
	 * @param source the source histogram
	 */
	public Histogram(Histogram source) {
		precomputeFrom = source.precomputeFrom;
		precomputeTo = source.precomputeTo;
		numberOfBins = source.numberOfBins;
		histogram = source.histogram.clone();
		precomputed = source.precomputed;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getDescription()
	 */
	@Override
	public String getDescription() {
		String description = "Histogram Frequency Probability Density function";
		return description;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getNumberOfParameters()
	 */
	@Override
	public int getNumberOfParameters() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getParameter(int)
	 */
	@Override
	public double getParameter(int index) {
		return numberOfBins;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#setParameter(int, double)
	 */
	@Override
	public void setParameter(int index, double value) {	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#precompute(double, double, int)
	 */
	@Override
	public void precompute(double startValue, double endValue, int nBins) {	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getProbability(double)
	 */
	@Override
	public double getProbability(double x) {
		return histogram[(int)x];
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getPrecomputedProbability(double)
	 */
	@Override
	public double getPrecomputedProbability(double x) {
		return histogram[(int)x];
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
	public double[] getHistogram() {

		return histogram;
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

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#status()
	 */
	@Override
	public void status() {
		System.out.print("Histogram");
		System.out.print(" Number Of Bins: " + getParameter(0));
		if (precomputed) {
			System.out.print(" Starting Point: " + precomputeFrom);
			System.out.println(" Ending Point: " + precomputeTo);
		}

		for (int i = 0; i < histogram.length;i++){
			System.out.println("Index: " + i + " Value: " + histogram[i]);
		}
		System.out.println();
	}
}
