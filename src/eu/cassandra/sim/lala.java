package eu.cassandra.sim;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import eu.cassandra.sim.entities.appliances.Appliance;
import eu.cassandra.sim.entities.appliances.ConsumptionModel;
import eu.cassandra.sim.entities.installations.Installation;
import eu.cassandra.sim.entities.people.Activity;
import eu.cassandra.sim.entities.people.Person;
import eu.cassandra.sim.math.Gaussian;
import eu.cassandra.sim.math.Histogram;
import eu.cassandra.sim.math.ProbabilityDistribution;
import eu.cassandra.sim.math.Uniform;
import eu.cassandra.sim.utilities.Constants;

public class lala {
	
//	static Logger logger = Logger.getLogger(Simulation.class);
	
	public static void main(String[] args)
	{
		String aresources_path = "/Users/fanitzima";
		int seed = 171181;
		Simulation sim = new  Simulation(aresources_path, seed);
		sim.setInstallations(new Vector<Installation>());
		
		System.out.println("Simulation setup started");
  		
  		String responseType = "Optimal"; 		// "None", "Optimal", "Normal", "Discrete", "Daily"
  	    String scenarioName = "lalaName";
  	    String locationInfo ="lalaLocation";
  	    int numOfDays = 3; 						// duration

  	    int startDateDay = 24;
	    int startDateMonth = 3;
	    int startDateYear = 2014;
  	   
	    SimulationParams simParams = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, startDateDay,  startDateMonth, startDateYear);
  	    sim.setSimulationWorld(simParams);
  		
  		
  		String pricingType = "None"; 			// TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
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
  		String setup = "static"; 							// static, dynamic
  		if(setup.equalsIgnoreCase("static")) 
  		{
  			String[] instNames = {"inst1", "inst2", "inst3"};		// installation names
  			String[] instIDs = {"1", "2", "3"};							// installation ids
  			String[] descriptions = {"lala1", "lala2", "lala3"};	// installation descriptions
  			String[] types = {"lala1", "lala2", "lala3"};				// installation types
  			
  			
			String[][] instNamesA= {		{"app1", "app2", "app3", "app4", "app5"}, 	// appliance names
													{"app1", "app2", "app3", "app4", "app5"}, 
													{"app1", "app2", "app3", "app4", "app5"} };		
	  		String[][] instIDsA = {			{"1", "2", "3", "4", "5"}, 								// appliance ids
	  												{"1", "2", "3", "4", "5"}, 															
	  					 							{"1", "2", "3", "4", "5"} };										
  			String[][] descriptionsA = {	{"lala1", "lala2", "lala3", "lala4", "lala5"},		// appliance descriptions
								  					{"lala1", "lala2", "lala3", "lala4", "lala5"},
								  					{"lala1", "lala2", "lala3", "lala4", "lala5"} };
  			String[][] typesA = {			{"lala1", "lala2", "lala3", "lala4", "lala5"}, 		// appliance types
								  					{"lala1", "lala2", "lala3", "lala4", "lala5"},
								  					{"lala1", "lala2", "lala3", "lala4", "lala5"} };
  			double[][] standbyVals = {	{1, 2, 3, 4, 5}, 
								  					{1, 2, 3, 4, 5}, 
								  					{1, 2, 3, 4, 5} };
  			boolean[][] isbase = { 		{true, false, false, false, true},
								  					{true, false, false, false, true},
  													{true, false, false, false, true} };
  			
  			String[][] personNames = {	{"Fani", "Nikos"}, 										// person names
  													{"Fani", "Nikos"}, 
  													{"Fani", "Nikos"} };	
  			String[][] personIDs = {		{"id1", "id2"}, 												// person ids
													{"id1", "id2"}, 
													{"id1", "id2"} };	
  			String[][] personDescs = {	{"Fani", "Nikos"}, 										// person descriptions
													{"Fani", "Nikos"}, 
													{"Fani", "Nikos"} };	
  			String[][] personTypes = {	{"girl", "boy"}, 											// person types
								  					{"girl", "boy"}, 
								  					{"girl", "boy"}};	
  			double[][] personAw= {		{0.5, 0.9}, 
								  					{0.5, 0.9},
  													{0.5, 0.9} };
  			double[][] personSens= {	{0.9, 0.3}, 
								  					{0.9, 0.3},
													{0.9, 0.3} };
  			
  			int numOfInstallations = instNames.length;
  			PriorityBlockingQueue<Event> queue = new PriorityBlockingQueue<Event>(2 * numOfInstallations);
  			for (int i = 0; i < numOfInstallations; i++) {
  				String id = instIDs[i];
  				String name = instNames[i];
  				String description = descriptions[i];
  				String type = types[i];
  				Installation inst = new Installation.Builder(id, name, description, type).build();
//  				// Thermal module if exists
//  				DBObject thermalDoc = (DBObject)instDoc.get("thermal");
//  				if(thermalDoc != null && pricing.getType().equalsIgnoreCase("TOUPricing")) {
//  					ThermalModule tm = new ThermalModule(thermalDoc, pricing.getTOUArray());
//  					inst.setThermalModule(tm);
//  				}
  				int appcount = 	instIDsA[i].length;																
  				// Create the appliances
  				HashMap<String,Appliance> existing = new HashMap<String,Appliance>();
  				for (int j = 0; j < appcount; j++) {
  					String appid = instIDsA[i][j];
  					String appname = instNamesA[i][j];
  					String appdescription = descriptionsA[i][j];
  					String apptype = typesA[i][j];
  					double standby = standbyVals[i][j];
  					boolean base = isbase[i][j];
  					ConsumptionModel pconsmod = new ConsumptionModel();
  					ConsumptionModel qconsmod = new ConsumptionModel();
//					try {
//						pconsmod = new ConsumptionModel("TODO", "p");
//						qconsmod  = new ConsumptionModel("TODO", "q");
//					} catch (BadParameterException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
  					
					Appliance app = new Appliance.Builder(appid, appname, appdescription, apptype, inst, pconsmod, qconsmod, standby, base).build(sim.getOrng());
  					existing.put(appid, app);
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
	  				
	  				String[] activityNames = {"Cooking", "Cleaning"};
	  				String[] activityIDs = {"act1", "act2"};
	  				String[] activityTypes = {"type1", "type2"};
	  				int[] actModCounts = {1, 1};
	  				
	  				int actcount = activityIDs.length;
	  				for (int l = 0; l < actcount; l++) 
	  				{
	  					String activityName = activityNames[l];
	  					String activityType = activityTypes[l];
	  					String actid = activityIDs[l];
	  					Activity act = new Activity.Builder(actid, activityName, "", activityType, sim.getSimulationWorld()).build();
	  					ProbabilityDistribution startDist;
	  					ProbabilityDistribution durDist;
	  					ProbabilityDistribution timesDist;
	  					int actmodcount = actModCounts[l];
	  					for (int k = 1; k <= actmodcount; k++) {
	  						String actmodName = "activity model";
	  						String actmodType = "actmodel type";
	  						String actmodDayType = "weekdays";  //any day | weekdays or weekends | abbreviations of specific weekdays, i.e. [Mon, Tue, Sat] | specific days formated as 1/12, 31/10 
	  						boolean shiftable = true;
	  						boolean exclusive = true;
	  						String[] containsAppliances = {"1", "3"};
	  						
//	  						// "Normal Distribution"
//	  						double mean = 10;
//	  						double std = 1.5;
//	  						durDist = new Gaussian(mean, std); 				
//	  						durDist.precompute(0, 1439, 1440);
	  						
	  						double[] v1 = {5, 0, 10, 2, 3};
	  						durDist = new Histogram(v1);

	  						// "Uniform Distribution"
	  						double from = 100; 
	  			   			double to = 150; 
	  						startDist = new Uniform(Math.max(from-1,0), Math.min(to-1, 1439), true);  	
	  								  // = new Uniform(from, to, false); when constructing a distribution for duration or repetitions
	  						
//	  						// "Gaussian Mixture Models"
//	  			   			double[] w = {0, 0.1, 0.2};
//	  			         	double[] means = {0, 0.1, 0.2};
//	  			         	double[] stds = {0, 0.1, 0.2};
//	  			         	timesDist = new GaussianMixtureModels(w.length, w, means, stds);
//	  			         	timesDist.precompute(0, 1439, 1440);
	  						
	  			         	// "Histogram"
	  						double[] v = {5, 0, 10, 2, 3};
	  						timesDist = new Histogram(v);
	  			         	
	  						act.addDuration(actmodDayType, durDist);
	  						act.addStartTime(actmodDayType, startDist);
	  						act.addTimes(actmodDayType, timesDist);
	  						act.addShiftable(actmodDayType, shiftable);
	  						act.addConfig(actmodDayType, exclusive);
	  						// add appliances
	  						for(int m = 0; m < containsAppliances.length; m++) {
	  							String containAppId = containsAppliances[m];
	  							Appliance app  = existing.get(containAppId);
	  							act.addAppliance(actmodDayType,app,1.0/containsAppliances.length);
	  						}
	  					}
	  					person.addActivity(act);
	  				}
  				}
  				sim.getInstallations().add(inst);
  			}
  		    sim.setQueue(queue);
  		    
  		} else if(setup.equalsIgnoreCase("dynamic")) {
  			System.err.println("Dynamic setup for scenarios not yet supported");
//  			dynamicSetup(jsonScenario, jump);
  		} else {
  			System.err.println("Problem with setup property!!!");
  		}
  		System.out.println("Simulation setup finished");
		
  		sim.runStandAlone();
	}
	
	
	
	

}
