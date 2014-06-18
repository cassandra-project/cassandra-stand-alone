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
package eu.cassandra.sim;

import java.util.HashMap;
import java.util.Vector;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

import eu.cassandra.sim.entities.appliances.Appliance;
import eu.cassandra.sim.entities.appliances.ConsumptionModel;
import eu.cassandra.sim.entities.installations.Installation;
import eu.cassandra.sim.entities.people.Activity;
import eu.cassandra.sim.entities.people.Person;
import eu.cassandra.sim.math.Gaussian;
import eu.cassandra.sim.math.GaussianMixtureModels;
import eu.cassandra.sim.math.Histogram;
import eu.cassandra.sim.math.ProbabilityDistribution;
import eu.cassandra.sim.math.Uniform;
import eu.cassandra.sim.utilities.ConsumptionModelsLibrary;
import eu.cassandra.sim.utilities.DistributionsLibrary;

/**
 * 
 * 
 * @author Fani A. Tzima (fani [at] iti [dot] gr)
 * 
 */
public class StandAloneSimulation extends Simulation{
	
	public StandAloneSimulation(String aresources_path, String adbname, int seed) {
		super(aresources_path, adbname, seed);
	}
	
//	@Override
	public Vector<Installation> setupScenario()
	{
  	    String scenarioName = "Scenario1";
  		String responseType = "None"; 		// "None", "Optimal", "Normal", "Discrete", "Daily"
  	    String locationInfo ="Katerini";
  	    int numOfDays = 3; 						// duration
  	    int startDateDay = 5;
	    int startDateMonth = 4;
	    int startDateYear = 2014;
	    SimulationParams simParams = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, startDateDay,  startDateMonth, startDateYear);
	    
	    // TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
	    
//  		String pricingType = "ScalarEnergyPricing"; 			
//  		int billingCycle = 120;  					// all cases
//  		double fixedCharge = 15;				// all cases
//  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
//		double[] prices = {0.10, 0.07, 0.07, 0.06};		
//		double[] levels = {0, 400, 400, 500};				
//		builderPP.scalarEnergyPricing(prices, levels);
//		PricingPolicy pricPolicy = builderPP.build();
		
 		String pricingType = "AllInclusivePricing"; 			
  		int billingCycle = 120;  					// all cases
  		double fixedCharge = 15;				// all cases
  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);			
		builderPP.allInclusivePricing(100, 50, 100);
		PricingPolicy pricPolicy = builderPP.build();
		
//		String pricingType = "EnergyPowerPricing"; 			
//  		int billingCycle = 30;  					// all cases
//  		double fixedCharge = 2;				// all cases
//  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
//		double contractedCapacity = 10;
//		double energyPricing = 0.08;
//		double powerPricing = 2.5;
//		builderPP.energyPowerPricing(contractedCapacity, energyPricing, powerPricing);
//		PricingPolicy pricPolicy = builderPP.build();
	    
//		String pricingType = "MaximumPowerPricing"; 			
//  		int billingCycle = 30;  					// all cases
//  		double fixedCharge = 0;				// all cases
//  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
//		double energyPricing = 0.08;
//		double powerPricing = 2.5;
//		builderPP.maximumPowerPricing(energyPricing, powerPricing, 0.0);
//		PricingPolicy pricPolicy = builderPP.build();
	    
//	    String pricingType = "TOUPricing"; 			
//	    int billingCycle = 90;  					// all cases
//	    double fixedCharge = 10;				// all cases
//	    PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
//	    double[] prices = {0.5, 0.01, 0.10};		
//	    String[] froms = {"14:00", "00:00", "19:45"};
//	    String[] tos = {"19:45", "14:00", "23:45"};
//	    builderPP.touPricing(froms, tos, prices);
//	    PricingPolicy pricPolicy = builderPP.build();
	    
//	    String pricingType = "ScalarEnergyPricingTimeZones"; 			
//	    int billingCycle = 120;  					// all cases
//	    double fixedCharge = 10;				// all cases
//	    PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
//	    double[] prices = {0.10, 0.08, 0.07, 0.06};		
//	    double[] levels = {0, 400, 400, 500};	
//	    double offpeakPrice = 0.05;
//	    	String[] froms = new String[0];
//	    String[] tos  = new String[0];
//	    builderPP.scalarEnergyPricingTimeZones(offpeakPrice, prices, levels, froms, tos);
//	    PricingPolicy pricPolicy = builderPP.build();
		
		String pricingTypeB = "None"; 		
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
		Installation inst = new Installation.Builder(instID, instName, instDescription, instDescription, null, null, null).build();
		
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
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
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
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
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
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
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
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
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
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
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
		
		double[] v4 = {0.25,0.375,0.25,0,0,0,0,0.125};
		ProbabilityDistribution timesDist = new Histogram(v4);
		act1.addTimes(actmodDayType, timesDist);
		
		boolean shiftable = false;
		act1.addShiftable(actmodDayType, shiftable);
		boolean exclusive = true;
		act1.addConfig(actmodDayType, exclusive);
		
		String[] containsAppliances = {"appl1", "appl3", "appl4", "appl5"};
//		String[] containsAppliances = {"appl1"};
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
		
		ProbabilityDistribution durDist2 = new Gaussian(1, 1); 				
		durDist2.precompute(0, 1439, 1440);
		actBuilder.duration(actmodDayType, durDist2);
		
		ProbabilityDistribution startDist2 = new Histogram(DistributionsLibrary.getStartTimeHistForLighting());
		actBuilder.startTime(actmodDayType, startDist2);
		
		double[] v2a = {0.22222,0.33333,0.44444};
		ProbabilityDistribution timesDist2 = new Histogram(v2a);
		actBuilder.times(actmodDayType, timesDist2);
		
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
		
		inst.addPerson(person);	
		installations.add(inst);
		
		
		//Create the installation
		instName = "Fani's house";			// installation names
		instID= "inst2";								// installation ids
		instDescription = "Fani's house";	// installation descriptions
		instType = "lala1";							// installation types
		Installation inst2 = new Installation.Builder(instID, instName, instDescription, instDescription, null, null, null).build();
		
		// Create the appliances
		existing = new HashMap<String,Appliance>();
		
		applName ="Cleaning Washing Machine";
		appliID = "appl21";
		applDescription = "Description of Cleaning Washing Machine";
		applType = "Washing";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("q");
		Appliance app21 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst2, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
		existing.put(appliID, app21);
		inst2.addAppliance(app21);
		
		// Create the people
		personName = "Fani";
		personID = "person2";
		personDesc ="Person";	
		personType ="Girl";	
		awareness= 0.9;
		sensitivity=0.7;
		Person person2 = new Person.Builder(personID, personName, personDesc, personType, inst2, awareness, sensitivity).build();
		
		// Create the activities
		activityName = "Cleaning"; 
		activityID = "act21";
		activityDesc = "Person Cleaning Activity"; 
		activityType = "lala"; 
		Activity act21 = new Activity.Builder(activityID, activityName, activityDesc, activityType, simParams).build();
		
		String[] containsAppliances21 = {"appl21"};
		
		actmodDayType = "weekends";  //any | weekdays | weekends | working | nonworking | abbreviations of specific weekdays, i.e. [Mon, Tue, Sat] | specific days formated as 1/12, 31/10 	
		double[] w = {0.7, 0.3};
     	double[] means = {480, 1200};
     	double[] stds = {40, 60};
		ProbabilityDistribution durDist3 = new GaussianMixtureModels(w.length, w, means, stds);
		durDist3.precompute(0, 1439, 1440);
		act21.addDuration(actmodDayType, durDist3);
		ProbabilityDistribution startDist3 = new Histogram(DistributionsLibrary.getStartTimeHistForCleaning());
		act21.addStartTime(actmodDayType, startDist3);
		double[] v42 = {0.25,0.375,0.25,0,0,0,0,0.125};
		ProbabilityDistribution timesDist3 = new Histogram(v42);
		act21.addTimes(actmodDayType, timesDist3);
		act21.addShiftable(actmodDayType, shiftable);
		act21.addConfig(actmodDayType, exclusive);
		for(int m = 0; m < containsAppliances21.length; m++) {
			String containAppId = containsAppliances21[m];
			Appliance app  = existing.get(containAppId);
			act21.addAppliance(actmodDayType,app,1.0/containsAppliances21.length);
		}
		
		actmodDayType = "weekdays";  //any | weekdays | weekends | working | nonworking | abbreviations of specific weekdays, i.e. [Mon, Tue, Sat] | specific days formated as 1/12, 31/10 
		double[] durDist4V = {100.0, 50.0, 200.0};
		ProbabilityDistribution durDist4 = new Histogram(durDist4V);
		act21.addDuration(actmodDayType, durDist4);
		ProbabilityDistribution startDist4 = null;
		double from = 100;
		double to = 400;
		if ("startDist4".contains("start")) 
			startDist4 = new Uniform(Math.max(from-1,0), Math.min(to-1, 1439), true);
		else 
			startDist4 = new Uniform(from, to, false);	
		act21.addStartTime(actmodDayType, startDist4);
		double[] timesDist4V = {0.2, 0.3, 0.5, 0.4};
		ProbabilityDistribution timesDist4 = new Histogram(timesDist4V);
		act21.addTimes(actmodDayType, timesDist4);
		act21.addShiftable(actmodDayType, shiftable);
		act21.addConfig(actmodDayType, exclusive);
		for(int m = 0; m < containsAppliances21.length; m++) {
			String containAppId = containsAppliances21[m];
			Appliance app  = existing.get(containAppId);
			act21.addAppliance(actmodDayType,app,1.0/containsAppliances21.length);
		}
		
		
		// add appliances
		for(int m = 0; m < containsAppliances21.length; m++) {
			String containAppId = containsAppliances21[m];
			Appliance app  = existing.get(containAppId);
			act21.addAppliance(actmodDayType,app,1.0/containsAppliances21.length);
		}
		
		person2.addActivity(act21);
		
		inst2.addPerson(person2);	
		
		installations.add(inst2);
		
		
		this.simulationWorld = simParams;
  		this.mcruns = mcruns;
  		this.co2 = co2;
  		this.pricing = pricPolicy;
  		this.baseline_pricing = pricPolicyB;
  		this.numOfDays = numOfDays;
  		this.setup = setup;
  		
  		return installations;
	}
	
	public static void main(String[] args)
	{	
		String aresources_path = "/Users/fanitzima";
		int seed = 171181;
		StandAloneSimulation sim = new  StandAloneSimulation(aresources_path, "2Persons"+System.currentTimeMillis(), seed);
		
  		try {
			sim.setupStandalone(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

  		sim.runStandAlone();
	}
	

}
