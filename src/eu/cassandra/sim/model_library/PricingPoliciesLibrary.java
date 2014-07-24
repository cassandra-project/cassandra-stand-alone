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
package eu.cassandra.sim.model_library;

import eu.cassandra.sim.PricingPolicy;

/**
 * Class with various ready-to-use Pricing Policies.
 * 
 *  @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 * 
 */
public class PricingPoliciesLibrary {
	
	public static PricingPolicy getDefaultPricingPolicy()
	{
		return new PricingPolicy();
	}
	
	public static PricingPolicy getAllInclusivePricingPolicy()
	{
		String pricingType = "AllInclusivePricing"; 			
		int billingCycle = 120;  					// all cases
		double fixedCharge = 15;				// all cases
		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);			
		builderPP.allInclusivePricing(100, 50, 100);
		PricingPolicy pricPolicy = builderPP.build();
		return pricPolicy;
	}
	
	public static PricingPolicy getScalarEnergyPricingPolicy()
	{	 
		String pricingType = "ScalarEnergyPricing"; 			
		int billingCycle = 120;  					// all cases
		double fixedCharge = 15;				// all cases
		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
		double[] prices = {0.10, 0.07, 0.07, 0.06};		
		double[] levels = {0, 400, 400, 500};				
		builderPP.scalarEnergyPricing(prices, levels);
		PricingPolicy pricPolicy = builderPP.build();
		return pricPolicy;
	}
	
	public static PricingPolicy getEnergyPowerPricingPolicy()
	{
		String pricingType = "EnergyPowerPricing"; 			
		int billingCycle = 30;  					// all cases
		double fixedCharge = 2;				// all cases
		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
		double contractedCapacity = 10;
		double energyPricing = 0.08;
		double powerPricing = 2.5;
		builderPP.energyPowerPricing(contractedCapacity, energyPricing, powerPricing);
		PricingPolicy pricPolicy = builderPP.build();
		return pricPolicy;
	}
	
	public static PricingPolicy getTOUPricingPolicy()
	{
	    String pricingType = "TOUPricing"; 			
	    int billingCycle = 90;  					// all cases
	    double fixedCharge = 10;				// all cases
	    PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
	    double[] prices = {0.5, 0.01, 0.10};		
	    String[] froms = {"14:00", "00:00", "19:45"};
	    String[] tos = {"19:45", "14:00", "23:45"};
	    builderPP.touPricing(froms, tos, prices);
	    PricingPolicy pricPolicy = builderPP.build();		
		return pricPolicy;
	}
	
	public static PricingPolicy getScalarEnergyPricingTimeZonesPolicy()
	{		    
	    String pricingType = "ScalarEnergyPricingTimeZones"; 			
	    int billingCycle = 120;  					// all cases
	    double fixedCharge = 10;				// all cases
	    PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
	    double[] prices = {0.10, 0.08, 0.07, 0.06};		
	    double[] levels = {0, 400, 400, 500};	
	    double offpeakPrice = 0.05;
	    	String[] froms = new String[0];
	    String[] tos  = new String[0];
	    builderPP.scalarEnergyPricingTimeZones(offpeakPrice, prices, levels, froms, tos);
	    PricingPolicy pricPolicy = builderPP.build();
		return pricPolicy;
	}
}
