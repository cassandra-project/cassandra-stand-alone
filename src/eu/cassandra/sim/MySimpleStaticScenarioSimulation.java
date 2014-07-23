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
import eu.cassandra.sim.math.Histogram;
import eu.cassandra.sim.math.ProbabilityDistribution;
import eu.cassandra.sim.model_library.ConsumptionModelsLibrary;
import eu.cassandra.sim.model_library.DistributionsLibrary;

/**
 * Class MySimpleStaticScenarioSimulation provides an example implementation of the setupScenario() method that
 * programmatically builds all entities involved in the scenario "described" in the input file "SimpleStaticScenario.txt". <br>
 * Public constructors and methods offered by the CASSANDRA stand-alone software library are employed, with the overall 
 * method implementation meant as an example of how CASSANDRA functionalities can be integrated with other 
 * (Java-based) projects.
 * 
 * @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 * 
 */
public class MySimpleStaticScenarioSimulation extends Simulation{
	
	/**
	 * Instantiates a new simulation.
	 *
	 * @param outputPath the path to the output directory
	 * @param dbName the name of the database where output data will be stored
	 * @param seed the seed for the random number generator
	 * @param useDerby whether to use MongoDB (false) or Apache Derby (true) for storing simulation output data
	 */
	public MySimpleStaticScenarioSimulation(String outputPath, String dbName, int seed, boolean useDerby) {
		super(outputPath, dbName, seed, useDerby);
	}
	
	/**
	 * Specific implementation of the  {@link eu.cassandra.sim.Simulation#setupScenario() setupScenario} method
	 * that programmatically builds all entities involved in the scenario "described" in the input file "SimpleStaticScenario.txt".
	 */
	@Override
	public Vector<Installation> setupScenario()
	{
		//set up the simulation parameters
		String scenarioName = "Scenario1";
  		String responseType = "None"; 		
  	    String locationInfo ="Thessaloniki";
  	    int numOfDays = 3; 					
  	    int startDateDay = 5;
	    int startDateMonth = 4;
	    int startDateYear = 2014;
	    int mcruns = 5;
  		double co2 = 2; 
  		String setup = "static"; 			
  		
	    this.simulationWorld = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, 
	    		startDateDay,  startDateMonth, startDateYear, mcruns, co2, setup);
	    
		//set up the pricing policy
	    String pricingType = "AllInclusivePricing"; 			
		int billingCycle = 120;  					
		double fixedCharge = 15;				
		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);			
		builderPP.allInclusivePricing(100, 50, 100);
		PricingPolicy pricPolicy = builderPP.build();
		this.pricing = pricPolicy;
		
		//set up the simulation entities
		Vector<Installation> installations = new Vector<Installation>();
  		
  		//Create the installation
		String instName = "Fani's house";			
		String instID= "inst1";								
		String instDescription = "Sample installation";	
		Installation inst = new Installation.Builder(instID, instName, instDescription, instDescription, this.pricing, this.baseline_pricing).build();
		
		//Create the appliances
		TreeMap<String, Appliance> appliances = new TreeMap<String,Appliance>();
		
		String applName ="Cleaning Washing Machine";
		String appliID = "appl1";
		String applDescription = "Description of Cleaning Washing Machine";
		String applType = "Cleaning";
		double applStandByCons = 0;
		boolean applIsBase = false;
		ConsumptionModel consModelsP = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("p");
		ConsumptionModel consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForWashingMachine("q");
		Appliance app1 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
		appliances.put(appliID, app1);
		inst.addAppliance(app1);
		
		applName ="Cleaning Vacuum Cleaner";
		appliID = "appl2";
		applDescription = "Description of Cleaning Vacuum Cleaner";
		applType = "Cleaning";
		applStandByCons = 0;
		applIsBase = false;
		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner1("p");
		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner1("q");
		Appliance app2 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
		appliances.put(appliID, app2);
		inst.addAppliance(app2);
		
		// Create the people
		String personName = "Fani";
		String personID = "person1";
		String personDesc ="Single person";	
		String personType ="Girl";	
		double awareness= 0.8;
		double sensitivity=0.3;
		Person person = new Person.Builder(personID, personName, personDesc, personType, inst, awareness, sensitivity).build();
		
		// Create the activities
		String activityName = "Person Cleaning Activity"; 
		String activityID = "act1";
		String activityDesc = "Person Cleaning Activity"; 
		String activityType = "Cleaning"; 
		Activity act1 = new Activity.Builder(activityID, activityName, activityDesc, activityType, this.simulationWorld).build();
		
		String actmodDayType = "any";   
		
		ProbabilityDistribution durDist = new Gaussian(1, 1, true); 			
		act1.addDuration(actmodDayType, durDist);
		
		ProbabilityDistribution startDist = new Histogram(DistributionsLibrary.getStartTimeHistForCleaning());
		act1.addStartTime(actmodDayType, startDist);
		
		double[] v4 = {0.25, 0.375, 0.25, 0, 0, 0, 0, 0.125};
		ProbabilityDistribution timesDist = new Histogram(v4);
		act1.addTimes(actmodDayType, timesDist);
		
		boolean shiftable = false;
		act1.addShiftable(actmodDayType, shiftable);
		boolean exclusive = true;
		act1.addConfig(actmodDayType, exclusive);
		
		String[] containsAppliances = {"appl1", "appl2"};
		act1.addAppliances(containsAppliances, appliances, actmodDayType);
		
		person.addActivity(act1);
		
		inst.addPerson(person);	
		
		installations.add(inst);
		
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
		boolean useDerby = true;
		boolean printKPIs = true;
		
		MySimpleStaticScenarioSimulation sim = new MySimpleStaticScenarioSimulation(output_path, "SimpleStatic"+System.currentTimeMillis(), seed, useDerby);
  		try {
			sim.setup(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
  		sim.runSimulation();
		if (printKPIs)
			sim.printKPIs();
	}
	
}
