package eu.cassandra.sim;

import java.util.Date;
import java.util.HashMap;
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
import java.util.Hashtable;
import java.util.Properties;
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
 * 
 * 
 * @author Fani A. Tzima (fani [at] iti [dot] gr)
 * 
 */
public class StandAloneSimulationFromFile2 extends Simulation {
	
	static SetupFileParser sfp;

	public StandAloneSimulationFromFile2(String aresources_path, String adbname, int seed) {
		super(aresources_path, adbname, seed);
	}
	
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
	    SimulationParams simParams = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, startDateDay,  startDateMonth, startDateYear);
			
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
		
		
	    int mcruns = Integer.parseInt((sfp.propSimulation.getProperty("mcruns") != null ? sfp.propSimulation.getProperty("mcruns").trim() : "1")); 	
  		double co2 = Integer.parseInt((sfp.propSimulation.getProperty("co2") != null ? sfp.propSimulation.getProperty("co2").trim() : "0")); 	
  		String setup = sfp.propScenario.getProperty("setup");

  		
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
			Installation inst = new Installation.Builder(instID, instName, instDescription, instDescription).build();
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
		HashMap<String, Appliance> appliances = new HashMap<String, Appliance>();
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
  		HashMap<String, Person> people = new HashMap<String, Person>();
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
  		HashMap<String, Activity> activities = new HashMap<String, Activity>();
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

			distrType = prop.getProperty("duration_distrType");
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
  			
  			for(int m = 0; m < containsAppliances.length; m++) {
  				String containAppId = containsAppliances[m].trim();
  				Appliance app  = appliances.get(containAppId);
  				act.addAppliance(actmodDayType,app,1.0/containsAppliances.length);
  			}
  		}
		
		this.simulationWorld = simParams;
  		this.mcruns = mcruns;
  		this.co2 = co2;
  		this.pricing = pricPolicy;
  		this.baseline_pricing = pricPolicyB;
  		this.numOfDays = numOfDays;
  		this.setup = setup;
  		
  		return installations;
	}
	
	private PricingPolicy constructPricingPolicy(String pricingType, PricingPolicy.Builder builderPP) throws Exception
	{
		String regexp = "[0-2]\\d:[0-5]\\d";
		switch(pricingType) {
  		case "TOUPricing":
  			String[] timezones = sfp.propPricing.getProperty("timezones").split(",");
  			String[] pricesS = sfp.propPricing.getProperty("prices").split(","); 
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
  			String[] levelsLS = sfp.propPricing.getProperty("levels").split(",");
  			String[] pricesLS = sfp.propPricing.getProperty("prices").split(","); 
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
  			double offpeakPrice = Integer.parseInt((sfp.propPricing.getProperty("offpeakPrice") != null ? sfp.propPricing.getProperty("offpeakPrice").trim() : "0"));
  			String[] levelsAS = sfp.propPricing.getProperty("levels").split(",");
  			String[] pricesAS = sfp.propPricing.getProperty("prices").split(","); 
  			if (levelsAS.length != pricesAS.length)
  				throw new Exception("ERROR: levels and prices lists must have the same length");
  			double[] levelsA = new double[levelsAS.length];
  			double[] pricesA = new double[levelsAS.length];
  			for(int i = 0; i < levelsAS.length; i++) {
  				levelsA[i] = Double.parseDouble(levelsAS[i]);
  				pricesA[i] = Double.parseDouble(pricesAS[i]);
  			}
  			String[] timezonesAS = sfp.propPricing.getProperty("timezones").split(",");
  			String[] pricesAS2 = sfp.propPricing.getProperty("prices").split(","); 
  			if (timezonesAS.length != pricesAS2.length)
  				throw new Exception("ERROR: timezones and prices lists must have the same length");
  			String[] fromsA = new String[timezonesAS.length];
  			String[] tosA = new String[timezonesAS.length];
  			double[] pricesA2 = new double[timezonesAS.length];
  			for(int i = 0; i < timezonesAS.length; i++) {
  				String from = timezonesAS[i].split("-")[0].trim();
  				String to = timezonesAS[i].split("-")[1].trim();
  				if (!from.matches(regexp) || !to.matches(regexp))
  					throw new Exception("ERROR: unrecognized time format in timezones (should be of the form hh:mm eg. 23:58)");  				
  				fromsA[i] = from;
  				tosA[i] = to;
  				pricesA2[i] = Double.parseDouble(pricesAS2[i]);
  			}
  			builderPP.scalarEnergyPricingTimeZones(offpeakPrice, pricesA, levelsA, fromsA, tosA);
  			break;
  		case "EnergyPowerPricing":
  			double contractedCapacity = Integer.parseInt((sfp.propPricing.getProperty("contractedCapacity") != null ? sfp.propPricing.getProperty("contractedCapacity").trim() : "0"));
  			double energyPricing = Double.parseDouble((sfp.propPricing.getProperty("energyPricing") != null ? sfp.propPricing.getProperty("energyPricing").trim() : "0.0"));
  			double powerPricing = Double.parseDouble((sfp.propPricing.getProperty("powerPricing") != null ? sfp.propPricing.getProperty("powerPricing").trim() : "0.0"));
  			builderPP.energyPowerPricing(contractedCapacity, energyPricing, powerPricing);
  			break;
  		case "MaximumPowerPricing":
  			double maximumPower = Double.parseDouble((sfp.propPricing.getProperty("maximumPower") != null ? sfp.propPricing.getProperty("maximumPower").trim() : "0.0"));
  			double energyPricing2 = Double.parseDouble((sfp.propPricing.getProperty("energyPricing") != null ? sfp.propPricing.getProperty("energyPricing").trim() : "0.0"));
  			double powerPricing2 =Double.parseDouble((sfp.propPricing.getProperty("powerPricing") != null ? sfp.propPricing.getProperty("powerPricing").trim() : "0.0"));
  			builderPP.maximumPowerPricing(energyPricing2, powerPricing2, maximumPower);
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
			Gaussian normal = new Gaussian(mean, std);
			normal.precompute(0, 1439, 1440);
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
				uniform = new Uniform(Math.max(from-1,0), Math.min(to-1, 1439), true);
			} else {
				uniform = new Uniform(from, to, false);
			}
			return uniform;
		case ("Gaussian Mixture Models"):
			String tempM = prop.getProperty(caseD + "_parameters").replace("[", "").replace("]", "").replace("\"", "");
			String[] paramsM = tempM.split("}");
			int length = paramsM.length;
			double[] means = new double[length];
			double[] stds = new double[length];
			double[] w = new double[length];
			double sumW = 0;
			for (int i=0; i<paramsM.length; i++)
			{
				String tempS = paramsM[i].replace("{", "").trim();
				if (tempS.startsWith(","))
					tempS = tempS.substring(1);
				String[] paramsMM = tempS.split(",");
				if (paramsMM.length != 2 || !(paramsMM[0].contains("mean") || paramsMM[1].contains("mean") || paramsMM[2].contains("mean")) 
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
			GaussianMixtureModels gmm = new GaussianMixtureModels(length, w, means, stds);
			gmm.precompute(0, 1439, 1440);
			return gmm;
		case ("Histogram"):
			String tempH = prop.getProperty(caseD + "_values").replace("[", "").replace("]", "").replace("\"", "");
			String[] values = tempH.split(",");
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
	
	public static void main(String[] args)
	{		
		sfp = new SetupFileParser();
		sfp.parseFileForProperties();
		StandAloneSimulationFromFile2 sas = new StandAloneSimulationFromFile2("/Users/fanitzima", "RunFromFile"+System.currentTimeMillis(), Integer.parseInt(sfp.propSimulation.getProperty("seed")));
		
		try {
			sas.setupStandalone(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		sas.runStandAlone();
	}

}
