package eu.cassandra.sim;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import eu.cassandra.sim.entities.appliances.Appliance;
import eu.cassandra.sim.entities.appliances.ConsumptionModel;
import eu.cassandra.sim.entities.appliances.Tripplet;
import eu.cassandra.sim.entities.installations.Installation;
import eu.cassandra.sim.entities.people.Activity;
import eu.cassandra.sim.entities.people.Person;
import eu.cassandra.sim.math.Gaussian;
import eu.cassandra.sim.math.Histogram;
import eu.cassandra.sim.math.ProbabilityDistribution;
import eu.cassandra.sim.standalone.ConsumptionModelsLibrary;
import eu.cassandra.sim.standalone.DistributionsLibrary;
import eu.cassandra.sim.utilities.Constants;

public class StandAloneSimulation {
	
//	static Logger logger = Logger.getLogger(Simulation.class);
	
	public static void main(String[] args)
	{	
//		System.out.println("Simulation setup started");
		
		String aresources_path = "/Users/fanitzima";
		int seed = 171181;
		Simulation sim = new  Simulation(aresources_path, "2Persons"+System.currentTimeMillis(), seed);
//		sim.setInstallations(new Vector<Installation>());
		
		
  	    String scenarioName = "Scenario1";
  		String responseType = "None"; 		// "None", "Optimal", "Normal", "Discrete", "Daily"
  	    String locationInfo ="Katerini";
  	    int numOfDays = 3; 						// duration
  	    int startDateDay = 24;
	    int startDateMonth = 3;
	    int startDateYear = 2014;
	    int mcruns = 5;
//		sim.setMcruns(mcruns);
  		double co2 = 2; 
//  		sim.setCo2(co2);
  	   
	    SimulationParams simParams = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, startDateDay,  startDateMonth, startDateYear);
//  	    sim.setSimulationWorld(simParams);
  		
  		
  		String pricingType = "ScalarEnergyPricing"; 			// TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
  		int billingCycle = 120;  					// all cases
  		double fixedCharge = 15;				// all cases
  		double offpeakPrice = -5;				// ScalarEnergyPricingTimeZones
  		int contractedCapacity = -5;			// EnergyPowerPricing
  		double energyPrice = -5;				// EnergyPowerPricing & MaximumPowerPricing
  		double powerPrice = -5;				// EnergyPowerPricing & MaximumPowerPricing
  		double maximumPower = -5;		// MaximumPowerPricing
		int fixedCost = 	-5;						// AllInclusivePricing
		double additionalCost = -5;			// AllInclusivePricing
		double contractedEnergy = -5;		// AllInclusivePricing
		
		String[] froms = {"00:00", "08:00", "16:00"};	//TOUPricing
		String[] tos = {"08:00", "16:00", "00:00"};		//TOUPricing
		double[] prices = {0.06, 0.07, 0.07, 0.10};		//TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones
		double[] levels = {500, 400, 400, 0};				//ScalarEnergyPricing, ScalarEnergyPricingTimeZones
  		
		
		PricingPolicy pricPolicy = new PricingPolicy();
		if (!pricingType.equals("None"))
		{
			try {
				pricPolicy = PricingPolicy.constructPricingPolicy(pricingType, billingCycle, fixedCharge, offpeakPrice, contractedCapacity, 
						energyPrice, powerPrice, maximumPower, fixedCost, additionalCost, contractedEnergy, froms, tos, prices,  levels);	
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
//		sim.setPricing(pricPolicy);
		
		
		String pricingTypeB = "None"; 		// TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
  		int billingCycleB = 15;  					// all cases
  		double fixedChargeB = 20.5;			// all cases
  		double offpeakPriceB = 20.3;			// ScalarEnergyPricingTimeZones
  		int contractedCapacityB = 150;		// EnergyPowerPricing
  		double energyPriceB = 100;			// EnergyPowerPricing & MaximumPowerPricing
  		double powerPriceB = 110;			// EnergyPowerPricing & MaximumPowerPricing
  		double maximumPowerB = 110;	// MaximumPowerPricing
		int fixedCostB = 10;						// AllInclusivePricing
		double additionalCostB = 5;			// AllInclusivePricing
		double contractedEnergyB = 110;	// AllInclusivePricing
		
		String[] fromsB = {"00:00", "08:00", "16:00"};	//TOUPricing
		String[] tosB = {"08:00", "16:00", "00:00"};		//TOUPricing
		double[] pricesB = {10, 20, 15};						//TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones
		double[] levelsB = {10, 20, 15};						//ScalarEnergyPricing, ScalarEnergyPricingTimeZones
  		
		PricingPolicy pricPolicyB = new PricingPolicy();
		if (!pricingTypeB.equals("None"))
		{
			try {
				pricPolicyB = PricingPolicy.constructPricingPolicy(pricingTypeB, billingCycleB, fixedChargeB, offpeakPriceB, contractedCapacityB, 
						energyPriceB, powerPriceB, maximumPowerB, fixedCostB, additionalCostB, contractedEnergyB, fromsB, tosB, pricesB,  levelsB);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
//		sim.setBaseline_pricing(pricPolicyB);
		 	
//		sim.setEndTick(Constants.MIN_IN_DAY * numOfDays);
		
  		// Check type of setup
  		String setup = "static"; 							// static, dynamic

		String[] instNames = {"Milioudis Base"};			// installation names
		String[] instIDs = {"inst1"};								// installation ids
		String[] instDescriptions = {"Milioudis Base"};	// installation descriptions
		String[] instTypes = {"lala1"};							// installation types
		
		String[][] applNames= {				{"Cleaning Washing Machine", "Lighting Lighting 0", "Cleaning Vacuum Cleaner 0", 
													 	 "Cleaning Water Heater", "Cleaning Vacuum Cleaner 1"} };		
  		String[][] appliIDs = {					{"appl1", "appl2", "appl3", "appl4", "appl5"} };										
		String[][] applDescriptions = {	{"lala1", "lala2", "lala3", "lala4", "lala5"} };
		String[][] applTypes = {				{"Washing", "Lighting", "Vacuum Cleaner", "Water Heater", "Vacuum Cleaner"} };
		double[][] applStandByCons = {	{0, 0, 0, 0, 0} };
		boolean[][] applIsBase = { 			{false, false, false, false, false} };
		ConsumptionModel[][] consModelsP = { {	ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("p"), 
																		ConsumptionModelsLibrary.getConsumptionModelForLighting("p"), 
																		ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner1("p"), 
																		ConsumptionModelsLibrary.getConsumptionModelForWaterHeater("p"), 
																		ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner2("p")} };
		
		ConsumptionModel[][] consModelsQ = { {	ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("q"), 
																		ConsumptionModelsLibrary.getConsumptionModelForLighting("q"), 
																		ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner1("q"), 
																		ConsumptionModelsLibrary.getConsumptionModelForWaterHeater("q"), 
																		ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner2("q")} };
		
		String[][] personNames = { {"Nikos"} };	
		String[][] personIDs = {		{"person1"} };	
		String[][] personDescs = {	{"Person"} };	
		String[][] personTypes = {	{"Boy"} };	
		double[][] personAw= {		{0.8} };
		double[][] personSens= {	{0.3} };
		
		Vector<Installation> installations = new Vector<Installation>();
		
		int numOfInstallations = instNames.length;
		for (int i = 0; i < numOfInstallations; i++) {
			String id = instIDs[i];
			String name = instNames[i];
			String description = instDescriptions[i];
			String type = instTypes[i];
			Installation inst = new Installation.Builder(id, name, description, type).build();
//  				// Thermal module if exists
//  				DBObject thermalDoc = (DBObject)instDoc.get("thermal");
//  				if(thermalDoc != null && pricing.getType().equalsIgnoreCase("TOUPricing")) {
//  					ThermalModule tm = new ThermalModule(thermalDoc, pricing.getTOUArray());
//  					inst.setThermalModule(tm);
//  				}
			int appcount = 	appliIDs[i].length;																
			// Create the appliances
			HashMap<String,Appliance> existing = new HashMap<String,Appliance>();
			for (int j = 0; j < appcount; j++) {
				Appliance app = new Appliance.Builder(appliIDs[i][j],  applNames[i][j], applDescriptions[i][j], applTypes[i][j], 
						inst, consModelsP[i][j], consModelsQ[i][j], applStandByCons[i][j], applIsBase[i][j]).build(sim.getOrng());
				existing.put(appliIDs[i][j], app);
				inst.addAppliance(app);
			}
			int personCount = personIDs[i].length;
			for (int j = 0; j < personCount; j++) {
  				String personid =personIDs[i][j];
  				String personName = personNames[i][j];
  				String personDescription = personDescs[i][j];
  				String personType = personTypes[i][j];
  				double awareness = personAw[i][j];
  				double sensitivity = personSens[i][j];
  				Person person = new Person.Builder(personid, personName, personDescription, personType, inst, awareness, sensitivity).build();
  				inst.addPerson(person);
  				
  				String[] activityNames = {"Cleaning", "Lighting"};
  				String[] activityIDs = {"act1", "act2"};
  				String[] activityDescs = {"Person Cleaning Activity", "Person Lighting Activity"};
  				
  				int[] actModCounts = {1, 1};
  				String[] actModName = {"Person Cleaning Activity Model", "Person Lighting Activity Model"};
  				String[] actmodDayType = {"any", "any"};  //any | weekdays | weekends | working | nonworking | abbreviations of specific weekdays, i.e. [Mon, Tue, Sat] | specific days formated as 1/12, 31/10 
				boolean[] shiftable = {false, false};
				boolean[] exclusive = {true, true};
				String[] containsAppliances1 = {"appl1", "appl3", "appl4", "appl5"};
				String[] containsAppliances2 = {"appl2"};
  				Vector<String[]> containsAppliances = new Vector<String[]>();
  				containsAppliances.add(containsAppliances1);
  				containsAppliances.add(containsAppliances2);
  				
				// "Normal Distribution"
				double mean = 1;
				double std = 1;
				ProbabilityDistribution durDist1 = new Gaussian(mean, std); 				
				durDist1.precompute(0, 1439, 1440);
				ProbabilityDistribution durDist2 = new Gaussian(mean, std); 				
				durDist2.precompute(0, 1439, 1440);
				Vector<ProbabilityDistribution> durDist = new Vector<ProbabilityDistribution>();
				durDist.add(durDist1);
				durDist.add(durDist2);
				
				ProbabilityDistribution startDist1 = new Histogram(DistributionsLibrary.getStartTimeHistForLighting());
				ProbabilityDistribution startDist2 = new Histogram(DistributionsLibrary.getStartTimeHistForCleaning());
				Vector<ProbabilityDistribution> startDist = new Vector<ProbabilityDistribution>();
				startDist.add(startDist2);
				startDist.add(startDist1);
				
				double[] v2 = {0.22222,0.33333,0.44444};
				ProbabilityDistribution timesDist1 = new Histogram(v2);
				double[] v4 = {0.25,0.375,0.25,0,0,0,0,0.125};
				ProbabilityDistribution timesDist2 = new Histogram(v4);
				Vector<ProbabilityDistribution> timesDist = new Vector<ProbabilityDistribution>();
				timesDist.add(timesDist2);
				timesDist.add(timesDist1);
				
  				int actcount = activityIDs.length;
  				for (int l = 0; l < actcount; l++) 
  				{
  					String activityName = activityNames[l];
  					String activityDesc = activityDescs[l];
  					String actid = activityIDs[l];
  					Activity act = new Activity.Builder(actid, activityName, activityDesc, "", simParams).build();
  					int actmodcount = actModCounts[l];
  					for (int k = 1; k <= actmodcount; k++) {
  						act.addDuration(actmodDayType[l], durDist.get(l));
  						act.addStartTime(actmodDayType[l], startDist.get(l));
  						act.addTimes(actmodDayType[l], timesDist.get(l));
  						act.addShiftable(actmodDayType[l], shiftable[l]);
  						act.addConfig(actmodDayType[l], exclusive[l]);
  						// add appliances
  						for(int m = 0; m < containsAppliances.get(l).length; m++) {
  							String containAppId = containsAppliances.get(l)[m];
  							Appliance app  = existing.get(containAppId);
  							act.addAppliance(actmodDayType[l],app,1.0/containsAppliances.get(l).length);
  						}
  					}
  					person.addActivity(act);
  				}
			}
			installations.add(inst);
		}
	  
  		try {
			sim.setupStandalone(false, simParams, pricPolicy, pricPolicyB, numOfDays, mcruns, co2, setup, installations);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//  		System.out.println("Simulation setup finished");
//		sim.run();
  		sim.runStandAlone();
	}
	

}
