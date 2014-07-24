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
 * A Mixture of Gaussian Distributions.
 *
 * @author Antonios Chrysopoulos
 */
public class GaussianMixtureModels implements ProbabilityDistribution
{
	/** The weights of the Gaussian distributions. */
	protected double[] pi;
	/** The Gaussian distributions. */
	protected Gaussian[] gaussians;

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

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getType()
	 */
	@Override
	public String getType()
	{
		return "GMM";
	}

	/**
	 * Instantiates a new GaussianMixtureModels distribution.
	 *
	 * @param n the number of Gaussian distributions
	 * @param pi the weights of the Gaussian distributions
	 * @param mu  the mean values of the Gaussian distributions
	 * @param s  the standard deviation sof the Gaussian distributions
	 * @param precomputed whether the values of the distribution histogram will be precomputed or not
	 */
	public GaussianMixtureModels (int n, double[] pi, double[] mu, double[] s, boolean precomputed)
	{
		gaussians = new Gaussian[n];
		this.pi = new double[n];
		this.precomputed = precomputed;
		for (int i = 0; i < n; i++) {
			this.pi[i] = pi[i];
			gaussians[i] = new Gaussian(mu[i], s[i], false);
		}
		if (precomputed)
			precompute(0, Constants.MINUTES_PER_DAY-1, Constants.MINUTES_PER_DAY);   
	}

	/**
	 * Instantiates a new GaussianMixtureModels distribution, by copying another one of the same type.
	 *
	 * @param source the source GaussianMixtureModels distribution
	 */
	public GaussianMixtureModels (GaussianMixtureModels source)
	{
		int n = source.gaussians.length;
		gaussians = new Gaussian[n];
		pi = new double[n];
		for (int i = 0; i < n; i++) {
			pi[i] = source.pi[i];
			gaussians[i] = new Gaussian(source.gaussians[i]);
		}
		precomputed = source.precomputed;
		precomputeFrom = source.precomputeFrom;
		precomputeTo = source.precomputeTo;
		numberOfBins = source.numberOfBins;
		histogram =source.histogram.clone();
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getDescription()
	 */
	@Override
	public String getDescription ()
	{
		String description = "Gaussian Mixture Models probability density function";
		return description;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#precompute(double, double, int)
	 */
	@Override
	public void precompute (double startValue, double endValue, int nBins)
	{
		if (startValue >= endValue) {
			// TODO Throw an exception or whatever.
			return;
		}
		precomputeFrom = startValue;
		precomputeTo = endValue;
		numberOfBins = nBins;
		histogram = new double[nBins];

		for (int i = 0; i < gaussians.length; i++) {
			gaussians[i].precompute(startValue, endValue, nBins);
		}

		for (int i = 0; i < nBins; i++) {
			for (int j = 0; j < gaussians.length; j++) {
				histogram[i] += pi[j] * gaussians[j].getHistogram()[i];
			} 
		}

		precomputed = true;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getProbability(double)
	 */
	@Override
	public double getProbability (double x)
	{
		double sum = 0;
		for (int j = 0; j < pi.length; j++) {
			sum += pi[j] * gaussians[j].getProbability(x);
		}
		return sum;
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
		double div = (precomputeTo - precomputeFrom) / numberOfBins;
		int bin = (int) Math.floor((x - precomputeFrom) / div);
		if (bin == numberOfBins) {
			bin--;
		}
		return histogram[bin];
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
			if (dice < sum) {
				return i;
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getHistogram()
	 */
	@Override
	public double[] getHistogram ()
	{
		if (precomputed == false){
			System.out.println("Not computed yet!");
			return null;		
		}

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
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getParameter(int)
	 */
	@Override
	public double getParameter (int index)
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#setParameter(int, double)
	 */
	@Override
	public void setParameter (int index, double value)
	{
	}

	
	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#getNumberOfParameters()
	 */
	@Override
	public int getNumberOfParameters() {
		return 3 * pi.length;
	}
	

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.math.ProbabilityDistribution#status()
	 */
	@Override
	public void status ()
	{

		System.out.print("Gaussian Mixture with");
		System.out.println(" Number of Mixtures:" + pi.length);
		for (int i = 0; i < pi.length; i++) {
			System.out.print("Mixture " + i);
			System.out.print(" Mean: " + gaussians[i].getParameter(0));
			System.out.print(" Sigma: " + gaussians[i].getParameter(1));
			System.out.print(" Weight: " + pi[i]);
			System.out.println();
		}
		System.out.println("Precomputed: " + precomputed);
		if (precomputed) {
			System.out.print("Number of Beans: " + numberOfBins);
			System.out.print(" Starting Point: " + precomputeFrom);
			System.out.println(" Ending Point: " + precomputeTo);
		}
		System.out.println();
	}



}
