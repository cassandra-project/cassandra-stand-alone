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
		
	public static void main(String[] args)
	{	
		String aresources_path = "/Users/fanitzima";
		int seed = 171181;
		Simulation sim = new  Simulation(aresources_path, "2Persons"+System.currentTimeMillis(), seed);
		
		
  	    String scenarioName = "Scenario1";
  		String responseType = "None"; 		// "None", "Optimal", "Normal", "Discrete", "Daily"
  	    String locationInfo ="Katerini";
  	    int numOfDays = 3; 						// duration
  	    int startDateDay = 24;
	    int startDateMonth = 3;
	    int startDateYear = 2014;
	    SimulationParams simParams = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, startDateDay,  startDateMonth, startDateYear);
	    
	    
  		String pricingType = "ScalarEnergyPricing"; 			// TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
  		int billingCycle = 120;  					// all cases
  		double fixedCharge = 15;				// all cases
  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
		double[] prices = {0.06, 0.07, 0.07, 0.10};		
		double[] levels = {500, 400, 400, 0};				
		builderPP.scalarEnergyPricing(prices, levels);
		PricingPolicy pricPolicy = builderPP.build();
		
		String pricingTypeB = "None"; 		// TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
		PricingPolicy pricPolicyB = new PricingPolicy();
		
		
	    int mcruns = 5;
  		double co2 = 2; 
  		String setup = "static"; 							// static, dynamic

  		
  		Vector<Installation> installations = new Vector<Installation>();
  		
  		
  		//Create the installation
		String instName = "Milioudis Base";			// installation names
		String instID= "inst1";								// installation ids
		String instDescription = "Milioudis Base";	// installation descriptions
		String instType = "lala1";							// installation types
		Installation inst = new Installation.Builder(instID, instName, instDescription, instDescription).build();
		installations.add(inst);
		
		// Create the appliances
		HashMap<String,Appliance> existing = new HashMap<String,Appliance>();
		
		String applName ="Cleaning Washing Machine";
		String appliID = "appl1";
		String applDescription = "Description of Cleaning Washing Machine";
		String applType = "Washing";
		double applStandByCons = 0;
		boolean applIsBase = false;
		ConsumptionModel consModelsP = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("p");
		ConsumptionModel consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("q");
		Appliance app1 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(sim.getOrng());
		existing.put(appliID, app1);
		inst.addAppliance(app1);
		
		applName ="Lighting Lighting 0";
		appliID = "appl2";
		applDescription = "Description of Lighting Lighting 0";
		applType = "Lighting";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForLighting("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForLighting("q");
		Appliance app2 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(sim.getOrng());
		existing.put(appliID, app2);
		inst.addAppliance(app2);
		
		applName ="Cleaning Vacuum Cleaner 0";
		appliID = "appl3";
		applDescription = "Description of Cleaning Vacuum Cleaner 0";
		applType = "Cleaning";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner1("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner1("q");
		Appliance app3 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(sim.getOrng());
		existing.put(appliID, app3);
		inst.addAppliance(app3);
		
		applName ="Cleaning Water Heater";
		appliID = "appl4";
		applDescription = "Description of Cleaning Water Heater";
		applType = "Cleaning";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForWaterHeater("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForWaterHeater("q");
		Appliance app4 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(sim.getOrng());
		existing.put(appliID, app4);
		inst.addAppliance(app4);

		applName ="Cleaning Vacuum Cleaner 1";
		appliID = "appl5";
		applDescription = "Description of Cleaning Vacuum Cleaner 1";
		applType = "Cleaning";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner2("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner2("q");
		Appliance app5 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(sim.getOrng());
		existing.put(appliID, app5);
		inst.addAppliance(app5);
		
		
		// Create the people
		String personName = "Nikos";
		String personID = "person1";
		String personDesc ="Person";	
		String personType ="Boy";	
		double awareness= 0.8;
		double sensitivity=0.3;
		Person person = new Person.Builder(personID, personName, personDesc, personType, inst, awareness, sensitivity).build();
		inst.addPerson(person);
		
		// Create the activities
		String activityName = "Cleaning"; 
		String activityID = "act1";
		String activityDesc = "Person Cleaning Activity"; 
		String activityType = "lala"; 
		Activity act1 = new Activity.Builder(activityID, activityName, activityDesc, activityType, simParams).build();
		
		String actmodDayType = "any";  //any | weekdays | weekends | working | nonworking | abbreviations of specific weekdays, i.e. [Mon, Tue, Sat] | specific days formated as 1/12, 31/10 
		
		ProbabilityDistribution durDist = new Gaussian(1, 1); 			// Normal Distribution: mean = 1, std = 1
		durDist.precompute(0, 1439, 1440);
		act1.addDuration(actmodDayType, durDist);
		
		ProbabilityDistribution startDist = new Histogram(DistributionsLibrary.getStartTimeHistForCleaning());
		act1.addStartTime(actmodDayType, startDist);
		
		double[] v2 = {0.22222,0.33333,0.44444};
		ProbabilityDistribution timesDist = new Histogram(v2);
		act1.addTimes(actmodDayType, timesDist);
		
		boolean shiftable = false;
		act1.addShiftable(actmodDayType, shiftable);
		boolean exclusive = true;
		act1.addConfig(actmodDayType, exclusive);
		
		String[] containsAppliances = {"appl1", "appl3", "appl4", "appl5"};
		// add appliances
		for(int m = 0; m < containsAppliances.length; m++) {
			String containAppId = containsAppliances[m];
			Appliance app  = existing.get(containAppId);
			act1.addAppliance(actmodDayType,app,1.0/containsAppliances.length);
		}
		
		person.addActivity(act1);
		
		
		activityName = "Lighting";
		activityID = "act2";
		activityDesc = "Person Lighting Activity";
		Activity.Builder actBuilder = new Activity.Builder(activityID, activityName, activityDesc, "", simParams);
		
		actmodDayType = "any";  //any | weekdays | weekends | working | nonworking | abbreviations of specific weekdays, i.e. [Mon, Tue, Sat] | specific days formated as 1/12, 31/10 
		
		durDist = new Gaussian(1, 1); 				
		durDist.precompute(0, 1439, 1440);
		actBuilder.duration(actmodDayType, durDist);
		
		startDist = new Histogram(DistributionsLibrary.getStartTimeHistForLighting());
		actBuilder.startTime(actmodDayType, startDist);
		
		double[] v4 = {0.25,0.375,0.25,0,0,0,0,0.125};
		timesDist = new Histogram(v4);
		actBuilder.times(actmodDayType, timesDist);
		
		shiftable = false;
		actBuilder.shiftable(actmodDayType, shiftable);
		
		Activity act2 = actBuilder.build();
		exclusive = true;
		act2.addConfig(actmodDayType, exclusive);
		
		String[] containsAppliances2 = {"appl2"};
		// add appliances
		for(int m = 0; m < containsAppliances2.length; m++) {
			String containAppId = containsAppliances2[m];
			Appliance app  = existing.get(containAppId);
			act2.addAppliance(actmodDayType,app,1.0/containsAppliances2.length);
		}
		
		person.addActivity(act2);
			
		
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
