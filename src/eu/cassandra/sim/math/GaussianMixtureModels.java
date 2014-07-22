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
 * @author Antonios Chrysopoulos
 */
public class GaussianMixtureModels implements ProbabilityDistribution
{
	protected double[] pi;
	protected Gaussian[] gaussians;

	// For precomputation
	protected boolean precomputed;
	protected int numberOfBins;
	protected double precomputeFrom;
	protected double precomputeTo;
	protected double[] histogram;

	@Override
	public String getType()
	{
		return "GMM";
	}


	/**
	 * Constructor 
	 * @param mu
	 *          Mean value of the Gaussian distribution.
	 * @param s
	 *          Standard deviation of the Gaussian distribution.
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

	@Override
	public String getDescription ()
	{
		String description = "Gaussian Mixture Models probability density function";
		return description;
	}

	@Override
	public int getNumberOfParameters ()
	{
		return 3 * pi.length;
	}

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

	@Override
	public double getProbability (double x)
	{
		double sum = 0;
		for (int j = 0; j < pi.length; j++) {
			sum += pi[j] * gaussians[j].getProbability(x);
		}
		return sum;
	}

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

	@Override
	public double[] getHistogram ()
	{
		if (precomputed == false){
			System.out.println("Not computed yet!");
			return null;		
		}

		return histogram;
	}

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

	@Override
	public double getProbabilityGreater (int x)
	{
		double prob = 0;

		int start = x;

		for (int i = start+1; i < histogram.length; i++)
			prob += histogram[i];

		return prob;
	}

	@Override
	public double getParameter (int index)
	{
		return 0;
	}

	@Override
	public void setParameter (int index, double value)
	{
	}

}
