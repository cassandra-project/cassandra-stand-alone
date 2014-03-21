package eu.cassandra.sim;

import java.text.ParseException;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.cassandra.sim.utilities.Constants;

public class lala {
	
//	static Logger logger = Logger.getLogger(Simulation.class);
	
	public static void main(String[] args)
	{
		String aresources_path = "/Users/fanitzima";
		int seed = 171181;
		Simulation sim = new  Simulation(aresources_path, seed);
		
		// sim.setup(); ----------------------------------- [ START ]
		
//  		logger.info("Simulation setup started");
  		
  		String responseType = "Optimal"; // "None", "Optimal", "Normal", "Discrete", "Daily"
  	    String name = "lalaName";
  	    String locationInfo ="lalaLocation";
  	    int numOfDays = 30; // duration

  	    int startDateDay = 17;
	    int startDateMonth = 11;
	    int startDateYear = 2013;
  	   
	    SimulationParams simParams = new SimulationParams(responseType, name, locationInfo, numOfDays, startDateDay,  startDateMonth, startDateYear);
  	    sim.setSimulationWorld(simParams);
  		
  		
  		String pricingType = "None"; // TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
  		int billingCycle = 15;  					// all cases
  		double fixedCharge = 20.5;			// all cases
  		double offpeakPrice = 20.3;			// ScalarEnergyPricingTimeZones
  		int contractedCapacity = 150;		// EnergyPowerPricing
  		double energyPrice = 100;			// EnergyPowerPricing & MaximumPowerPricing
  		double powerPrice = 110;				// EnergyPowerPricing & MaximumPowerPricing
  		double maximumPower = 110;		// MaximumPowerPricing
		int fixedCost = 	10;						// AllInclusivePricing
		double additionalCost = 5;			// AllInclusivePricing
		double contractedEnergy = 110;	// AllInclusivePricing
		
		String[] froms = {"00:00", "08:00", "16:00"};	//TOUPricing
		String[] tos = {"08:00", "16:00", "00:00"};		//TOUPricing
		double[] prices = {10, 20, 15};						//TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones
		double[] levels = {10, 20, 15};						//ScalarEnergyPricing, ScalarEnergyPricingTimeZones
  		
		if (pricingType.equals("None"))
		{
			PricingPolicy pricPolicy = new PricingPolicy();
			sim.setPricing(pricPolicy);
		}
		else
		{
			try {
				PricingPolicy pricPolicy = PricingPolicy.constructPricingPolicy(pricingType, billingCycle, fixedCharge, offpeakPrice, contractedCapacity, 
						energyPrice, powerPrice, maximumPower, fixedCost, additionalCost, contractedEnergy, froms, tos, prices,  levels);
				sim.setPricing(pricPolicy);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		
		String pricingTypeB = "None"; // TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
  		int billingCycleB = 15;  					// all cases
  		double fixedChargeB = 20.5;			// all cases
  		double offpeakPriceB = 20.3;			// ScalarEnergyPricingTimeZones
  		int contractedCapacityB = 150;		// EnergyPowerPricing
  		double energyPriceB = 100;			// EnergyPowerPricing & MaximumPowerPricing
  		double powerPriceB = 110;				// EnergyPowerPricing & MaximumPowerPricing
  		double maximumPowerB = 110;		// MaximumPowerPricing
		int fixedCostB = 	10;						// AllInclusivePricing
		double additionalCostB = 5;			// AllInclusivePricing
		double contractedEnergyB = 110;	// AllInclusivePricing
		
		String[] fromsB = {"00:00", "08:00", "16:00"};	//TOUPricing
		String[] tosB = {"08:00", "16:00", "00:00"};		//TOUPricing
		double[] pricesB = {10, 20, 15};						//TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones
		double[] levelsB = {10, 20, 15};						//ScalarEnergyPricing, ScalarEnergyPricingTimeZones
  		
		if (pricingTypeB.equals("None"))
		{
			PricingPolicy pricPolicy = new PricingPolicy();
			sim.setBaseline_pricing(pricPolicy);
		}
		else
		{
			try {
				PricingPolicy pricPolicy = PricingPolicy.constructPricingPolicy(pricingTypeB, billingCycleB, fixedChargeB, offpeakPriceB, contractedCapacityB, 
						energyPriceB, powerPriceB, maximumPowerB, fixedCostB, additionalCostB, contractedEnergyB, fromsB, tosB, pricesB,  levelsB);
				sim.setBaseline_pricing(pricPolicy);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		 	
		sim.setEndTick(Constants.MIN_IN_DAY * numOfDays);
		int mcruns = 10;
		sim.setMcruns(mcruns);
  		double co2 = 15; 
  		sim.setCo2(co2);
  		
  		// Check type of setup
  		String setup = "static"; // static, dynamic
  		if(setup.equalsIgnoreCase("static")) {
//  			staticSetup(jsonScenario);
  		} else if(setup.equalsIgnoreCase("dynamic")) {
//  			dynamicSetup(jsonScenario, jump);
  		} else {
  			System.err.println("Problem with setup property!!!");
  		}
//  		logger.info("Simulation setup finished: " + dbname);
		
	}
	
	
	
	

}
