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

import java.util.TreeMap;
import java.util.Vector;

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
import eu.cassandra.sim.model_library.ConsumptionModelsLibrary;
import eu.cassandra.sim.model_library.DistributionsLibrary;

/**
 * Class MySimpleDynamicScenarioSimulation provides an example implementation of the setupScenario() method that
 * programmatically builds all entities involved in the scenario "described" in the input file "SimpleDynamicScenario.txt". <br>
 * Public constructors and methods offered by the CASSANDRA stand-alone software library are employed, with the overall 
 * method implementation meant as an example of how CASSANDRA functionalities can be integrated with other 
 * (Java-based) projects.
 * 
 * @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 * 
 */
public class MySimpleDynamicScenarioSimulation extends Simulation{
	
	/**
	 * Instantiates a new simulation.
	 *
	 * @param outputPath the path to the output directory
	 * @param dbName the name of the database where output data will be stored
	 * @param seed the seed for the random number generator
	 * @param useDerby whether to use MongoDB (false) or Apache Derby (true) for storing simulation output data
	 */
	public MySimpleDynamicScenarioSimulation(String outputPath, String dbName, int seed, boolean useDerby) {
		super(outputPath, dbName, seed, useDerby);
	}
	
	/**
	 * Specific implementation of the  {@link eu.cassandra.sim.Simulation#setupScenario() setupScenario} method
	 * that programmatically builds all entities involved in the scenario "described" in the input file "SimpleDynamicScenario.txt".
	 */
	@Override
	public Vector<Installation> setupScenario()
	{
		//set up the simulation parameters
		String scenarioName = "Scenario2";
		String responseType = "None"; 		
		String locationInfo ="Thessaloniki";
		int numOfDays = 3; 					
		int startDateDay = 5;
		int startDateMonth = 4;
		int startDateYear = 2014;
		int mcruns = 1;
		double co2 = 2; 
		String setup = "dynamic"; 			

		this.simulationWorld = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, 
				startDateDay,  startDateMonth, startDateYear, mcruns, co2, setup);

		//set up the pricing policy
		String pricingType = "EnergyPowerPricing"; 			
		int billingCycle = 30;  					
		double fixedCharge = 2;		
		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
		double contractedCapacity = 10;
		double energyPricing = 0.08;
		double powerPricing = 2.5;
		builderPP.energyPowerPricing(contractedCapacity, energyPricing, powerPricing);
		PricingPolicy pricPolicy = builderPP.build();
		this.pricing = pricPolicy;

		//set up the simulation entities
		Vector<Installation> installations = new Vector<Installation>();

		//Create the "Collection" installation including person types and appliances to be dynamically instantiated
		String instName = "Collection";			
		String instID= "col1";								
		String instDescription = "Installation including person types and appliances to be dynamically instantiated";	
		Installation inst = new Installation.Builder(instID, instName, instDescription, instDescription, this.pricing, this.baseline_pricing).build();

		// Create the appliances
		TreeMap<String,Appliance> appliances = new TreeMap<String,Appliance>();

		String applName ="Washing Machine 1";
		String appliID = "appl1";
		String applDescription = "Description of Washing Machine 1";
		String applType = "Washing";
		double applStandByCons = 0;
		boolean applIsBase = false;
		ConsumptionModel consModelsP = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("p");
		ConsumptionModel consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("q");
		Appliance app1 = new Appliance.Builder(appliID,  applName, applDescription, applType, inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
		appliances.put(appliID, app1);
		inst.addAppliance(app1);

		applName ="Lighting";
		appliID = "appl2";
		applDescription = "Description of Lighting";
		applType = "Lighting";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForLighting("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForLighting("q");
		Appliance app2 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
		appliances.put(appliID, app2);
		inst.addAppliance(app2);

		applName ="Vacuum Cleaner 1";
		appliID = "appl3";
		applDescription = "Description of Vacuum Cleaner 1";
		applType = "Cleaning";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner1("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner1("q");
		Appliance app3 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
		appliances.put(appliID, app3);
		inst.addAppliance(app3);

		applName ="Water Heater";
		appliID = "appl4";
		applDescription = "Description of Water Heater";
		applType = "Water Heating";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForWaterHeater("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForWaterHeater("q");
		Appliance app4 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
		appliances.put(appliID, app4);
		inst.addAppliance(app4);

		applName ="Vacuum Cleaner 2";
		appliID = "appl5";
		applDescription = "Description of Vacuum Cleaner 2";
		applType = "Cleaning";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner2("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner2("q");
		Appliance app5 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
		appliances.put(appliID, app5);
		inst.addAppliance(app5);

		applName ="Washing Machine 2";
		appliID = "appl6";
		applDescription = "Description of Washing Machine 2";
		applType = "Washing";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("q");
		Appliance app21 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
		appliances.put(appliID, app21);
		inst.addAppliance(app21);


		// Create the first person type
		String personName = "Nikos";
		String personID = "person1";
		String personDesc ="Male Person";	
		String personType ="Boy";	
		double awareness = 0.8;
		double sensitivity = 0.3;
		Person person = new Person.Builder(personID, personName, personDesc, personType, inst, awareness, sensitivity).build();

		// Create the activities for the first person type
		String activityName = "Cleaning"; 
		String activityID = "act1";
		String activityDesc = "Person Cleaning Activity"; 
		String activityType = "Cleaning"; 
		Activity act1 = new Activity.Builder(activityID, activityName, activityDesc, activityType, this.simulationWorld).build();

		String actmodDayType = "any";  

		ProbabilityDistribution durDist = new Gaussian(1, 1, true); 			
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
		act1.addAppliances(containsAppliances, appliances, actmodDayType);

		person.addActivity(act1);


		activityName = "Lighting";
		activityID = "act2";
		activityDesc = "Person Lighting Activity";
		activityType = "Lighting"; 
		Activity.Builder actBuilder = new Activity.Builder(activityID, activityName, activityDesc, activityType, this.simulationWorld);

		actmodDayType = "any";  

		ProbabilityDistribution durDist2 = new Gaussian(1, 1, true); 				
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
		act2.addAppliances(containsAppliances2, appliances, actmodDayType);

		person.addActivity(act2);

		inst.addPerson(person);	

		// Create the second person type
		personName = "Fani";
		personID = "person2";
		personDesc ="Female Person";	
		personType ="Girl";	
		awareness= 0.9;
		sensitivity=0.7;
		Person person2 = new Person.Builder(personID, personName, personDesc, personType, inst, awareness, sensitivity).build();

		// Create the activities
		activityName = "Cleaning"; 
		activityID = "act21";
		activityDesc = "Person Cleaning Activity"; 
		activityType = "Cleaning"; 
		Activity act21 = new Activity.Builder(activityID, activityName, activityDesc, activityType, this.simulationWorld).build();

		String[] containsAppliances21 = {"appl6"};

		actmodDayType = "weekends";   	
		double[] w = {0.7, 0.3};
		double[] means = {480, 1200};
		double[] stds = {40, 60};
		ProbabilityDistribution durDist3 = new GaussianMixtureModels(w.length, w, means, stds, true);
		act21.addDuration(actmodDayType, durDist3);
		ProbabilityDistribution startDist3 = new Histogram(DistributionsLibrary.getStartTimeHistForCleaning());
		act21.addStartTime(actmodDayType, startDist3);
		double[] v42 = {0.25,0.375,0.25,0,0,0,0,0.125};
		ProbabilityDistribution timesDist3 = new Histogram(v42);
		act21.addTimes(actmodDayType, timesDist3);
		act21.addShiftable(actmodDayType, shiftable);
		act21.addConfig(actmodDayType, exclusive);
		act21.addAppliances(containsAppliances21, appliances, actmodDayType);

		actmodDayType = "weekdays";  
		double[] durDist4V = {100.0, 50.0, 200.0};
		ProbabilityDistribution durDist4 = new Histogram(durDist4V);
		act21.addDuration(actmodDayType, durDist4);
		ProbabilityDistribution startDist4 = null;
		double from = 100;
		double to = 400;
		startDist4 = new Uniform(from, to, true);	
		act21.addStartTime(actmodDayType, startDist4);
		double[] timesDist4V = {0.2, 0.3, 0.5, 0.4};
		ProbabilityDistribution timesDist4 = new Histogram(timesDist4V);
		act21.addTimes(actmodDayType, timesDist4);
		act21.addShiftable(actmodDayType, shiftable);
		act21.addConfig(actmodDayType, exclusive);
		act21.addAppliances(containsAppliances21, appliances, actmodDayType);

		person2.addActivity(act21);

		inst.addPerson(person2);	

		installations.add(inst);


		//Create an installation to be instantiated "as-is"
		instName = "Odysseas' house";			
		instID= "inst1";								
		instDescription = "Installation to be instantiated \"as-is\"";	
		Installation inst2 = new Installation.Builder(instID, instName, instDescription, instDescription, this.pricing, this.baseline_pricing).build();

		//Create the appliances
		TreeMap<String, Appliance> appliances2 = new TreeMap<String,Appliance>();

		applName ="Odysseas' Washing Machine";
		appliID = "appl7";
		applDescription = "Description of Odysseas' Washing Machine";
		applType = "Washing";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("q");
		Appliance app7 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst2, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
		appliances2.put(appliID, app7);
		inst2.addAppliance(app7);

		// Create the people
		personName = "Odysseas";
		personID = "person3";
		personDesc ="Single person";	
		personType ="Boy";	
		awareness= 0.8;
		sensitivity=0.3;
		Person person3 = new Person.Builder(personID, personName, personDesc, personType, inst2, awareness, sensitivity).build();

		// Create the activities
		activityName = "Person Laundry Activity"; 
		activityID = "act3";
		activityDesc = "Person Laundry Activity"; 
		activityType = "Cleaning"; 
		Activity act3 = new Activity.Builder(activityID, activityName, activityDesc, activityType, this.simulationWorld).build();
		actmodDayType = "weekends";   
		ProbabilityDistribution durDistO = new Gaussian(1, 1, true); 			
		act3.addDuration(actmodDayType, durDistO);
		ProbabilityDistribution startDistO = new Histogram(DistributionsLibrary.getStartTimeHistForCleaning());
		act3.addStartTime(actmodDayType, startDistO);
		ProbabilityDistribution timesDistO = new Histogram(v4);
		act3.addTimes(actmodDayType, timesDistO);
		shiftable = false;
		act3.addShiftable(actmodDayType, shiftable);
		exclusive = true;
		act3.addConfig(actmodDayType, exclusive);

		String[] containsAppliances3 = {"appl7"};
		act3.addAppliances(containsAppliances3, appliances2, actmodDayType);

		person3.addActivity(act3);

		inst2.addPerson(person3);	

		installations.add(inst2);


		// set up demographic data
		int numOfInstallations = 10;		
		TreeMap<String,Double>instGen = new TreeMap<String,Double>();			
		instGen.put("col1", 0.5);
		instGen.put("inst1", 0.5);
		TreeMap<String,Double> applGen = new TreeMap<String,Double>();		
		double applianceProb = 0.75;
		applGen.put("appl1", applianceProb);
		applGen.put("appl2", applianceProb);
		applGen.put("appl3", applianceProb);
		applGen.put("appl4", applianceProb);
		applGen.put("appl5", applianceProb);
		applGen.put("appl6", applianceProb);
		TreeMap<String,Double> personGen = new TreeMap<String,Double>();				
		personGen.put("person1", 0.7);
		personGen.put("person2", 0.3);
		this.demographics = new DemographicData("SimpleDynamic_10insts", "Demographics for the simple dynamic scenario.", "Demographics type", numOfInstallations, instGen, personGen, applGen);

		return installations;
	}
	
	/**
	 * The main method. Sets up and runs the simulation, and, finally, outputs results. 
	 *
	 * @param args The table of arguments. No arguments are required, so if any are provided, they are just ignored.
	 */
	public static void main(String[] args)
	{	
		String output_path = "./";
		int seed = 171181;
		boolean useDerby = false;
		boolean printKPIs = true;

		MySimpleDynamicScenarioSimulation sim = new MySimpleDynamicScenarioSimulation(output_path, "SimpleDynamic"+System.currentTimeMillis(), seed, useDerby);
		try {
			sim.setup(false);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}
		sim.runSimulation();
		if (printKPIs)
			sim.printKPIs();
	}


}
