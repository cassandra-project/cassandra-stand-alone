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

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
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
import eu.cassandra.sim.utilities.SetupFileParser;

/**
 * The StandAloneSimulation class includes an implementation of the setupScenario() method that
 * parses a properly formatted input text file and  <br>
 * (a) sets up the installations (or installation types) to be included in the simulation, <br>
 * (b) sets up the pricing scheme(s) to be used in the simulation, <br>
 * (c) defines the set of simulation parameters to be used and, in case of dynamic scenarios, <br>
 * (d) the demographic data, according to which the entities involved in the simulation will be instantiated.
 * 
 * @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 * 
 */
public class StandAloneSimulation extends Simulation {
	
	/**
	 * Instantiates a new stand alone simulation, whose scenario setup is stored in a properly formatted input file.
	 *
	 * @param outputPath the path to the output directory
	 * @param dbName the name of the database where output data will be stored
	 * @param seed the seed for the random number generator
	 * @param useDerby whether to use MongoDB (false) or Apache Derby (true) for storing simulation output data
	 */
	public StandAloneSimulation(String outputPath, String dbName, int seed, boolean useDerby) {
		super(outputPath, dbName, seed, useDerby);
	}
	
	
	/**
	 * Specific implementation of the  {@link eu.cassandra.sim.Simulation#setupScenario() setupScenario} method
	 * that parses the information required to setup a scenario from a properly formatted input file.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Vector<Installation> setupScenario()
	{
  	    String scenarioName = sfp.propScenario.getProperty("name");
  		String responseType = sfp.propSimulation.getProperty("response_type");
  	    String locationInfo = sfp.propSimulation.getProperty("locationInfo") != null ? sfp.propSimulation.getProperty("locationInfo") : "";
  	    int numOfDays = Integer.parseInt((sfp.propSimulation.getProperty("numberOfDays") != null ? sfp.propSimulation.getProperty("numberOfDays").trim() : "1")); 					
  	    int startDateDay = Integer.parseInt((sfp.propSimulation.getProperty("start_dayOfMonth") != null ? sfp.propSimulation.getProperty("start_dayOfMonth").trim() : new Date().getDate()+"")); 	
	    int startDateMonth = Integer.parseInt((sfp.propSimulation.getProperty("start_month") != null ? sfp.propSimulation.getProperty("start_month").trim() : new Date().getMonth()+"")); 		
	    int startDateYear = Integer.parseInt((sfp.propSimulation.getProperty("start_year") != null ? sfp.propSimulation.getProperty("start_year").trim() : new Date().getYear()+"")); 	
	    int mcruns = Integer.parseInt((sfp.propSimulation.getProperty("mcruns") != null ? sfp.propSimulation.getProperty("mcruns").trim() : "1")); 	
  		double co2 = Integer.parseInt((sfp.propSimulation.getProperty("co2") != null ? sfp.propSimulation.getProperty("co2").trim() : "0")); 	
  		String setup = sfp.propScenario.getProperty("setup");
	    SimulationParams simParams = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, 
	    		startDateDay,  startDateMonth, startDateYear, mcruns, co2, setup);
	    
	   
			
	    if (setup.equals("dynamic"))
	    {
	    		if (sfp.demographics.isEmpty())
	    		{
					try {
						throw new Exception("ERROR: Demographic data have to be provided for dynamic scenarios.");
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(0);
					}
	    		}
	    		else
	    		{
			    String dName = sfp.demographics.getProperty("name") != null ? sfp.demographics.getProperty("name") : "";
			    String dType = sfp.demographics.getProperty("type") != null ? sfp.demographics.getProperty("type") : "";
			    String dDescription = sfp.demographics.getProperty("description") != null ? sfp.demographics.getProperty("description") : "";
			    int number_of_entities = Integer.parseInt((sfp.demographics.getProperty("number_of_entities") != null ? sfp.demographics.getProperty("number_of_entities").trim() : "1")); 
			    String participation_probs_installations = sfp.demographics.getProperty("participation_probs_installations") != null ? sfp.demographics.getProperty("participation_probs_installations") : "";
			    String participation_probs_persons = sfp.demographics.getProperty("participation_probs_persons") != null ? sfp.demographics.getProperty("participation_probs_persons") : "";
			    String participation_probs_apps = sfp.demographics.getProperty("participation_probs_apps") != null ? sfp.demographics.getProperty("participation_probs_apps") : "";
			    
			    this.demographics = new DemographicData(dName, dDescription, dType, number_of_entities, 
			    		parseProbabilities(participation_probs_installations), parseProbabilities(participation_probs_persons), parseProbabilities(participation_probs_apps));
	    		}
	    }
	    
 		PricingPolicy pricPolicy = new PricingPolicy();
 		if (!sfp.propPricing.isEmpty() && !sfp.propPricing.getProperty("type").equals("None"))
 		{
 			String pricingType = sfp.propPricing.getProperty("type");		
	  		int billingCycle = Integer.parseInt((sfp.propPricing.getProperty("billingCycle") != null ? sfp.propPricing.getProperty("billingCycle").trim() : "0")); 
	  		double fixedCharge = Integer.parseInt((sfp.propPricing.getProperty("fixedCharge") != null ? sfp.propPricing.getProperty("fixedCharge").trim() : "0")); 
	  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);	
	  		try {
				pricPolicy = constructPricingPolicy(pricingType, builderPP);
			} catch (Exception e) {
				e.printStackTrace();
			}
 		} 			
  		
 		PricingPolicy pricPolicyB = new PricingPolicy(); 
 		if (!sfp.propPricingBaseline.isEmpty() && !sfp.propPricingBaseline.getProperty("type").equals("None"))
 		{
 			String pricingType = sfp.propPricingBaseline.getProperty("type");		
	  		int billingCycle = Integer.parseInt((sfp.propPricingBaseline.getProperty("billingCycle") != null ? sfp.propPricingBaseline.getProperty("billingCycle").trim() : "0")); 
	  		double fixedCharge = Integer.parseInt((sfp.propPricingBaseline.getProperty("fixedCharge") != null ? sfp.propPricingBaseline.getProperty("fixedCharge").trim() : "0")); 
	  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);	
	  		try {
	  			pricPolicyB = constructPricingPolicy(pricingType, builderPP);
	  		} catch (Exception e) {
				e.printStackTrace();
			}
 		}
		
		
	   

  		
  		Vector<Installation> installations = new Vector<Installation>();
  		
  		//Create the installations
  		Hashtable<String, Integer> instIndexes = new Hashtable<String, Integer>();
  		int index = 0;
  		for(Properties prop : sfp.propInstallations) 
  		{
			String instName = prop.getProperty("name");
			String instID= prop.getProperty("id").trim();
			String instDescription = prop.getProperty("description") != null ? prop.getProperty("description") : "";
			String instType = prop.getProperty("type") != null ? prop.getProperty("type") : "";
			Installation inst = new Installation.Builder(instID, instName, instDescription, instDescription, pricPolicy, pricPolicyB).build();
			installations.add(inst);
			instIndexes.put(instID, index);
			index++;
  		}
  		
  		// Create the consumption models
  		Hashtable<String, ConsumptionModel> consModels = new Hashtable<String, ConsumptionModel>();
  		for(Properties prop : sfp.propConsModels) 
  		{
  			String id = prop.getProperty("id").trim();
  			ConsumptionModel consModelP = new ConsumptionModel();
  			ConsumptionModel consModelQ = new ConsumptionModel();
			try {
				consModelP = new ConsumptionModel(prop.getProperty("pmodel"), "p");
				consModelQ = new ConsumptionModel(prop.getProperty("qmodel"), "q");
			} catch (Exception e) {
				e.printStackTrace();
			}
  			 
  			consModels.put(id+"_p", consModelP);
  			consModels.put(id+"_q", consModelQ);
  		}
		
		// Create the appliances
  		TreeMap<String, Appliance> appliances = new TreeMap<String, Appliance>();
  		for(Properties prop : sfp.propAppliances) 
  		{
			String applName = prop.getProperty("name");
			String appliID = prop.getProperty("id").trim();
			String applDescription = prop.getProperty("description") != null ? prop.getProperty("description") : "";
			String applType = prop.getProperty("type") != null ? prop.getProperty("type") : "";
			double applStandByCons = Double.parseDouble((prop.getProperty("standy_consumption") != null ? prop.getProperty("standy_consumption").trim() : "0.0")); 	
			boolean applIsBase = Boolean.parseBoolean((prop.getProperty("base") != null ? prop.getProperty("base").trim() : "false")); 	
			Installation inst = installations.get(instIndexes.get(prop.getProperty("installation")));
			String consumption_model =  prop.getProperty("consumption_model").trim();
			ConsumptionModel consMP = consModels.get(consumption_model+"_p");
  			ConsumptionModel consMQ = consModels.get(consumption_model+"_q");
			Appliance app1 = new Appliance.Builder(appliID,  applName, applDescription, applType, inst, consMP, consMQ, applStandByCons, applIsBase).build(getOrng());
			appliances.put(appliID, app1);
			inst.addAppliance(app1);
  		}
		
		// Create the people
  		TreeMap<String, Person> people = new TreeMap<String, Person>();
  		for(Properties prop : sfp.propPeople) 
  		{
			String personName = prop.getProperty("name");
			String personID = prop.getProperty("id");
			String personDesc = prop.getProperty("description") != null ? prop.getProperty("description") : "";
			String personType = prop.getProperty("type") != null ? prop.getProperty("type") : "";
			double awareness= Double.parseDouble((prop.getProperty("awareness") != null ? prop.getProperty("awareness").trim() : "0.0")); 	
			double sensitivity=Double.parseDouble((prop.getProperty("sensitivity") != null ? prop.getProperty("sensitivity").trim() : "0.0")); 	
			Installation inst = installations.get(instIndexes.get(prop.getProperty("installation").trim()));
			Person person = new Person.Builder(personID, personName, personDesc, personType, inst, awareness, sensitivity).build();
			inst.addPerson(person);	
			people.put(personID, person);
  		}
  		
  		// Create the activities
  		TreeMap<String, Activity> activities = new TreeMap<String, Activity>();
  		for(Properties prop : sfp.propActivities) 
  		{
			String activityName = prop.getProperty("name");
			String activityID = prop.getProperty("id");
			String activityDesc = prop.getProperty("description") != null ? prop.getProperty("description") : "";
			String activityType = prop.getProperty("type") != null ? prop.getProperty("type") : "";
			Activity act = new Activity.Builder(activityID, activityName, activityDesc, activityType, simParams).build();
			activities.put(activityID, act);
			Person person = people.get(prop.getProperty("person").trim());
			person.addActivity(act);
  		}
  		
  		// Create the activity models 
  		for(Properties prop : sfp.propActModels) 
  		{
  			String id = prop.getProperty("id");
  			String actmodDayType = prop.getProperty("day_type");
  			boolean shiftable = Boolean.parseBoolean((prop.getProperty("shiftable") != null ? prop.getProperty("shiftable").trim() : "false")); 	
  			boolean exclusive = Boolean.parseBoolean((prop.getProperty("exclusive") != null ? prop.getProperty("exclusive").trim() : "true")); 	 
  			String temp = prop.getProperty("containsAppliances");
  			String[] containsAppliances = temp.split(",");
  			
  			Activity act = activities.get(prop.getProperty("activity").trim());
  			
  			String distrType = prop.getProperty("start_distrType");
  			ProbabilityDistribution actModelStart;
			try {
				actModelStart = constructDistribution(distrType, prop, "start");
				act.addStartTime(actmodDayType, actModelStart);
			} catch (Exception e) {
				e.printStackTrace();
			}

			distrType = prop.getProperty("duration_distrType").trim();
  			ProbabilityDistribution actModelDuration;
  			try {
  				actModelDuration = constructDistribution(distrType, prop, "duration");
  				act.addDuration(actmodDayType, actModelDuration);
			} catch (Exception e) {
				e.printStackTrace();
			}
  			
  			distrType = prop.getProperty("repetitions_distrType");
  			ProbabilityDistribution actModelTimes;
  			try {
  				actModelTimes = constructDistribution(distrType, prop, "repetitions");
  				act.addTimes(actmodDayType, actModelTimes);
			} catch (Exception e) {
				e.printStackTrace();
			}
  			
  			act.addShiftable(actmodDayType, shiftable);
  			act.addConfig(actmodDayType, exclusive);
  			
  			act.addAppliances(containsAppliances, appliances, actmodDayType);
  			
//  			for(int m = 0; m < containsAppliances.length; m++) {
//  				String containAppId = containsAppliances[m].trim();
//  				Appliance app  = appliances.get(containAppId);
//  				act.addAppliance(actmodDayType,app,1.0/containsAppliances.length);
//  			}
  		}
		
		this.simulationWorld = simParams;
  		this.pricing = pricPolicy;
  		this.baseline_pricing = pricPolicyB;
  		
  		
  		return installations;
	}
	
	
	private PricingPolicy constructPricingPolicy(String pricingType, PricingPolicy.Builder builderPP) throws Exception
	{
		String regexp = "[0-2]\\d:[0-5]\\d";
		switch(pricingType) {
  		case "TOUPricing":
  			String[] timezones = sfp.propPricing.getProperty("timezones").replace("[", "").replace("]", "").split(",");
  			String[] pricesS = sfp.propPricing.getProperty("prices").replace("[", "").replace("]", "").split(","); 
  			if (timezones.length != pricesS.length)
  				throw new Exception("ERROR: timezones and prices lists must have the same length");
  			String[] froms = new String[timezones.length];
  			String[] tos = new String[timezones.length];
  			double[] prices = new double[timezones.length];
  			for(int i = 0; i < timezones.length; i++) {
  				String from = timezones[i].split("-")[0].trim();
  				String to = timezones[i].split("-")[1].trim();
  				if (!from.matches(regexp) || !to.matches(regexp))
  					throw new Exception("ERROR: unrecognized time format in timezones (should be of the form hh:mm eg. 23:58)");  				
  				froms[i] = from;
  				tos[i] = to;
  				prices[i] = Double.parseDouble(pricesS[i]);
  			}
  			builderPP.touPricing(froms, tos, prices);
  			break;
  		case "ScalarEnergyPricing":
  			String[] levelsLS = sfp.propPricing.getProperty("levels").replace("[", "").replace("]", "").split(",");
  			String[] pricesLS = sfp.propPricing.getProperty("prices").replace("[", "").replace("]", "").split(","); 
  			if (levelsLS.length != pricesLS.length)
  				throw new Exception("ERROR: levels and prices lists must have the same length");
  			double[] levelsL = new double[levelsLS.length];
  			double[] pricesL = new double[levelsLS.length];
  			for(int i = 0; i < levelsLS.length; i++) {
  				levelsL[i] = Double.parseDouble(levelsLS[i]);
  				pricesL[i] = Double.parseDouble(pricesLS[i]);
  			}
  			builderPP.scalarEnergyPricing(pricesL, levelsL);
  			break;
  		case "ScalarEnergyPricingTimeZones":
  			double offpeakPrice = Double.parseDouble((sfp.propPricing.getProperty("offpeakPrice") != null ? sfp.propPricing.getProperty("offpeakPrice").trim() : "0"));
  			String[] levelsAS = sfp.propPricing.getProperty("levels").replace("[", "").replace("]", "").split(",");
  			String[] pricesAS = sfp.propPricing.getProperty("prices").replace("[", "").replace("]", "").split(","); 
  			if (levelsAS.length != pricesAS.length)
  				throw new Exception("ERROR: levels and prices lists must have the same length");
  			double[] levelsA = new double[levelsAS.length];
  			double[] pricesA = new double[levelsAS.length];
  			for(int i = 0; i < levelsAS.length; i++) {
  				levelsA[i] = Double.parseDouble(levelsAS[i]);
  				pricesA[i] = Double.parseDouble(pricesAS[i]);
  			}
  			String[] offpeakHours = sfp.propPricing.getProperty("offpeakHours").replace("[", "").replace("]", "").split(",");
  			if (offpeakHours.length == 1 && offpeakHours[0].equals(""))
  				offpeakHours = new String[0];
  			String[] fromsA = new String[offpeakHours.length];
  			String[] tosA = new String[offpeakHours.length];
  			for(int i = 0; i < offpeakHours.length; i++) {
  				String from = offpeakHours[i].split("-")[0].trim();
  				String to = offpeakHours[i].split("-")[1].trim();
  				fromsA[i] = from;
  				tosA[i] = to;
  			}
  			builderPP.scalarEnergyPricingTimeZones(offpeakPrice, pricesA, levelsA, fromsA, tosA);
  			break;
  		case "EnergyPowerPricing":
  			double contractedCapacity = Integer.parseInt((sfp.propPricing.getProperty("contractedCapacity") != null ? sfp.propPricing.getProperty("contractedCapacity").trim() : "0"));
  			double energyPricing = Double.parseDouble((sfp.propPricing.getProperty("energyPrice") != null ? sfp.propPricing.getProperty("energyPrice").trim() : "0.0"));
  			double powerPricing = Double.parseDouble((sfp.propPricing.getProperty("powerPrice") != null ? sfp.propPricing.getProperty("powerPrice").trim() : "0.0"));
  			builderPP.energyPowerPricing(contractedCapacity, energyPricing, powerPricing);
  			break;
  		case "AllInclusivePricing":
  			int fixedCost = Integer.parseInt((sfp.propPricing.getProperty("fixedCost") != null ? sfp.propPricing.getProperty("fixedCost").trim() : "0"));
  			double additionalCost = Double.parseDouble((sfp.propPricing.getProperty("additionalCost") != null ? sfp.propPricing.getProperty("additionalCost").trim() : "0.0"));
  			double contractedEnergy = Double.parseDouble((sfp.propPricing.getProperty("contractedEnergy") != null ? sfp.propPricing.getProperty("contractedEnergy").trim() : "0.0"));
  			builderPP.allInclusivePricing(fixedCost, additionalCost, contractedEnergy);
  			break;
  		default:
			throw new Exception("ERROR: unkown pricing policy type \"" + pricingType + "\" employed");
  		}
		return builderPP.build();
	}
	
	private ProbabilityDistribution constructDistribution(String distType, Properties prop, String caseD) throws Exception
	{
		switch (distType) {
		case ("Normal Distribution"):
			String tempN = prop.getProperty(caseD + "_parameters").replace("{", "").replace("}", "").replace("[", "").replace("]", "").replace("\"", "");
			String[] paramsN = tempN.split(",");
			if (paramsN.length != 2 || !(paramsN[0].contains("mean") || paramsN[1].contains("mean")) || !(paramsN[0].contains("std") || paramsN[1].contains("std")) )
				throw new Exception("ERROR: Normal Distribution requires exaclty 2 parameters, named \"mean\" and \"std\". E.g. [{\"mean\":45,\"std\":10}]");
			
			double mean = 1;
			double std = 1;
			if (paramsN[0].contains("mean"))
			{
				mean = Double.parseDouble(paramsN[0].replace("mean", "").replace(":", "").trim());
				std = Double.parseDouble(paramsN[1].replace("std", "").replace(":", "").trim());
			}
			else
			{
				mean = Double.parseDouble(paramsN[1].replace("mean", "").replace(":", "").trim());
				std = Double.parseDouble(paramsN[0].replace("std", "").replace(":", "").trim());
			}		
			Gaussian normal = new Gaussian(mean, std, true);
			return normal;
		case ("Uniform Distribution"):
			String tempU = prop.getProperty(caseD + "_parameters").replace("{", "").replace("}", "").replace("[", "").replace("]", "").replace("\"", "");
			String[] paramsU = tempU.split(",");
			if (paramsU.length != 2 || !(paramsU[0].contains("start") || paramsU[1].contains("start")) || !(paramsU[0].contains("end") || paramsU[1].contains("end")) )
				throw new Exception("ERROR: Uniform Distribution requires exaclty 2 parameters, named \"start\" and \"end\". E.g. [{\"start\":100,\"end\":200}]");
			double from = 1;
			double to = 1;
			if (paramsU[0].contains("start"))
			{
				from = Double.parseDouble(paramsU[0].replace("start", "").replace(":", "").trim());
				to = Double.parseDouble(paramsU[1].replace("end", "").replace(":", "").trim());
			}
			else
			{
				from = Double.parseDouble(paramsU[1].replace("start", "").replace(":", "").trim());
				to = Double.parseDouble(paramsU[0].replace("end", "").replace(":", "").trim());
			}		
			Uniform uniform = null;
			if(caseD.equalsIgnoreCase("start")) {
				uniform = new Uniform(from, to, true);
			} else {
				uniform = new Uniform(from, to, false);
			}
			return uniform;
		case ("Gaussian Mixture Models"):
			String tempM = prop.getProperty(caseD + "_parameters").replace("[", "").replace("]", "").replace("\"", "");
			String[] paramsM = tempM.split("}");
			int length = paramsM.length;//-1;
			double[] means = new double[length];
			double[] stds = new double[length];
			double[] w = new double[length];
			double sumW = 0;
			for (int i=0; i<length; i++)
			{
				String tempS = paramsM[i].replace("{", "").trim();
				if (tempS.startsWith(","))
					tempS = tempS.substring(1);
				String[] paramsMM = tempS.split(",");
				if (paramsMM.length != 3 || !(paramsMM[0].contains("mean") || paramsMM[1].contains("mean") || paramsMM[2].contains("mean")) 
						|| !(paramsMM[0].contains("std") || paramsMM[1].contains("std") || paramsMM[2].contains("std")) 
						|| !(paramsMM[0].contains("w") || paramsMM[1].contains("w") || paramsMM[2].contains("w")) )
					throw new Exception("ERROR: Gaussian Mixture Models require exaclty 3 parameters for each tuple, named \"mean\", \"std\" and \"w\". "+
						" E.g. [{\"w\":0.5 , \"mean\":45,\"std\":10}, {\"w\":0.5 , \"mean\":100,\"std\":10}]");
				for (int j=0; j<3; j++)
				{
					if (paramsMM[j].contains("mean"))
						means[i] = Double.parseDouble(paramsMM[j].replace("mean", "").replace(":", "").trim());
					else if (paramsMM[j].contains("std"))
						stds[i] = Double.parseDouble(paramsMM[j].replace("std", "").replace(":", "").trim());
					else
					{
						w[i] = Double.parseDouble(paramsMM[j].replace("w", "").replace(":", "").trim());
						sumW += w[i];
					}
				}
			}
			if (sumW != 1)
				throw new Exception("ERROR: Gaussian Mixture Models require for tuple weights to sum up to 1");
			GaussianMixtureModels gmm = new GaussianMixtureModels(length, w, means, stds, true);
			return gmm;
		case ("Histogram"):
			String tempH = prop.getProperty(caseD + "_values").replace("[", "").replace("]", "").replace("\"", "").trim();
			String[] values = tempH.split(",");
			if (values.length == 1 && values[0].equals(""))
				values = new String[0];
			double[] v = new double[values.length];
			for (int i=0; i<values.length; i++)
				try {
					v[i] = Double.parseDouble(values[i]);
				} 
				catch (NumberFormatException e) {
					throw new Exception("ERROR: Histogram requires a list of (double) values. E.g. [1,2,3,4...]");
				}
			Histogram h = new Histogram(v);
			return h;
		default:
			throw new Exception("ERROR: Non-existing distribution type. Problem in setting up the simulation.");
		}
	}
	
	private TreeMap<String, Double> parseProbabilities(String probsText)
	{
		TreeMap<String, Double> results = new TreeMap<String, Double>();
		String tempN = probsText.replace("[", "").replace("]", "").replace("\"", "");
		String[] pairs = tempN.split(",");
		for (String pair : pairs)
		{
			String[] parts = pair.split(":");
			results.put(parts[0].trim(), Double.parseDouble(parts[1]));
		}
		return results;
	}
	
	
	/**
	 * The main method. Parses the scenario input file, sets up and runs the simulation, and, finally, outputs results. 
	 *
	 * @param args The table of arguments. Two arguments are required: "setup_file_name" and "ouput_dir_path".
	 * If more than two arguments are provided, the ones after the first two are ignored.
	 * If less than two arguments are provided, simulation is run using the default values "properties.txt" and "./", respectively.
	 */
	public static void main(String[] args)
	{	
		String filename = "SimpleDynamicScenario.txt";
//		String filename = "properties.txt";
		String outputPath = "./";
		if (args.length >= 2)
		{
			filename = args[0];
			outputPath = args[1];
		}
		if (args.length > 2)
			System.err.println("WARNING: Only two arguments required. Ignoring arguments after the first two.");
		if (args.length <= 1)
		{
			System.err.println("WARNING: Two arguments required <setup_file_name> <ouput_dir_path>. Running simulation with default values \"properties.txt\" and \"./\".");
		}
			
		sfp = new SetupFileParser();
		sfp.parseFileForProperties(filename);	
		StandAloneSimulation sas = new StandAloneSimulation(outputPath, "RunFrom" + (new File(filename)).getName().replace(".", "_")+System.currentTimeMillis(), 
					sfp.generalProps.getProperty("seed") != null ? Integer.parseInt(sfp.generalProps.getProperty("seed")) : 0,
					sfp.generalProps.getProperty("useDerby") != null ? Boolean.parseBoolean(sfp.generalProps.getProperty("useDerby")) : true );
		
		try {
			sas.setup(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		sas.runSimulation();
		
		boolean printKPIs =sfp.generalProps.getProperty("printKPIs") != null ? Boolean.parseBoolean(sfp.generalProps.getProperty("printKPIs")) : false;
		if (printKPIs)
			sas.printKPIs();
	}

}
