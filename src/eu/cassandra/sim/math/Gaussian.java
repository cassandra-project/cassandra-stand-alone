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
 * @author Christos Diou <diou at iti dot gr>
 */
public class Gaussian implements ProbabilityDistribution
{
	protected double mean;
	protected double sigma;

	// For precomputation
	protected boolean precomputed;
	protected int numberOfBins;
	protected double precomputeFrom;
	protected double precomputeTo;
	protected double[] histogram;

	// return phi(x) = standard Gaussian pdf
	private static double phi (double x)
	{
		return Math.exp(-(x * x) / 2) / Math.sqrt(2 * Math.PI);
	}

	// return phi(x, mu, s) = Gaussian pdf with mean mu and stddev s
	private static double phi (double x, double mu, double s)
	{
		return phi((x - mu) / s) / s;
	}

	// return Phi(z) = standard Gaussian cdf using Taylor approximation
	private static double bigPhi (double z)
	{
		if (z < -8.0) {
			return 0.0;
		}
		if (z > 8.0) {
			return 1.0;
		}

		double sum = 0.0;
		double term = z;
		for (int i = 3; Math.abs(term) > 1e-5; i += 2) {
			sum += term;
			term *= (z * z) / i;
		}
		return 0.5 + sum * phi(z);
	}

	// return Phi(z, mu, s) = Gaussian cdf with mean mu and stddev s
	protected static double bigPhi (double z, double mu, double s)
	{
		return bigPhi((z - mu) / s);
	}


	/**
	 * @param mu
	 *          Mean value of the Gaussian distribution.
	 * @param s
	 *          Standard deviation of the Gaussian distribution.
	 */
	public Gaussian (double mu, double s, boolean precomputed)
	{
		mean = mu;
		sigma = s;
		this.precomputed = precomputed;
		if (precomputed)
			precompute(0, Constants.MINUTES_PER_DAY-1, Constants.MINUTES_PER_DAY);
	}

	public Gaussian (Gaussian source)
	{
		mean = source.mean;
		sigma = source.sigma;
		precomputed = source.precomputed;
		numberOfBins = source.numberOfBins;
		precomputeFrom = source.precomputeFrom;
		precomputeTo = source.precomputeTo;
		histogram = source.histogram.clone();
	}

	@Override
	public String getDescription()
	{
		String description = "Gaussian probability density function";
		return description;
	}

	@Override
	public String getType()
	{
		return "Gaussian";
	}

	@Override
	public int getNumberOfParameters ()
	{
		return 2;
	}

	@Override
	public double getParameter (int index)
	{
		switch (index) {
		case 0:
			return mean;
		case 1:
			return sigma;
		default:
			return 0.0;
		}

	}

	@Override
	public void setParameter (int index, double value)
	{
		switch (index) {
		case 0:
			mean = value;
			break;
		case 1:
			sigma = value;
			break;
		default:
			return;
		}
	}

	@Override
	public void precompute (double startValue, double endValue, int nBins)
	{
		if ((startValue >= endValue) || (nBins == 0)) {
			// TODO Throw an exception or whatever.
			return;
		}
		precomputeFrom = startValue;
		precomputeTo = endValue;
		numberOfBins = nBins;

		double div = (endValue - startValue) / nBins;
		histogram = new double[nBins];

		double residual = bigPhi(startValue, mean, sigma) + 1 -
				bigPhi(endValue, mean, sigma);
		double res_right = 1 - bigPhi(0, mean, sigma);
		residual /= res_right;
		for (int i = 0; i < nBins; i++) {
			//      double x = startValue + i * div - small_number;
			double x = startValue + i * div;
			histogram[i] = bigPhi(x + div / 2.0, mean, sigma) -
					bigPhi(x - div / 2.0, mean, sigma);
			histogram[i] += (histogram[i] * residual);
		}
		precomputed = true;
	}

	@Override
	public double getProbability (double x)
	{
		return phi(x, mean, sigma);
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
		//    System.out.println(dice);
		double sum = 0;
		for (int i = 0; i < numberOfBins; i++) {
			sum += histogram[i];
			// if(dice < sum) return (int)(precomputeFrom + i * div);
			if (dice < sum)
				return i;
		}
		return -1;
	}

	@Override
	public void status ()
	{
		System.out.print("Normal Distribution with");
		System.out.print(" Mean: " + getParameter(0));
		System.out.println(" Sigma: " + getParameter(1));
		System.out.println("Precomputed: " + precomputed);
		if (precomputed) {
			System.out.print("Number of Bins: " + numberOfBins);
			System.out.print(" Starting Point: " + precomputeFrom);
			System.out.println(" Ending Point: " + precomputeTo);
		}
		System.out.println();

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
	public double getProbabilityGreater (int x)
	{
		double prob = 0;

		int start = x;

		for (int i = start+1; i < histogram.length; i++)
			prob += histogram[i];

		return prob;
	}

}
