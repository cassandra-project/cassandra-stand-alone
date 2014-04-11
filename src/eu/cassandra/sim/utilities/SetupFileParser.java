package eu.cassandra.sim.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import eu.cassandra.server.api.exceptions.BadParameterException;
import eu.cassandra.sim.PricingPolicy;
import eu.cassandra.sim.Simulation;
import eu.cassandra.sim.SimulationParams;
import eu.cassandra.sim.StandAloneSimulation;
import eu.cassandra.sim.entities.appliances.Appliance;
import eu.cassandra.sim.entities.appliances.ConsumptionModel;
import eu.cassandra.sim.entities.installations.Installation;
import eu.cassandra.sim.entities.people.Activity;
import eu.cassandra.sim.entities.people.Person;
import eu.cassandra.sim.math.Gaussian;
import eu.cassandra.sim.math.Histogram;
import eu.cassandra.sim.math.ProbabilityDistribution;
import eu.cassandra.sim.standalone.ConsumptionModelsLibrary;
import eu.cassandra.sim.standalone.DistributionsLibrary;

public class SetupFileParser extends Simulation{
	
	Properties propPricing = new Properties();
	Properties propPricingBaseline = new Properties();
	Properties propSimulation = new Properties();
	Properties propScenario = new Properties();
	Vector<Properties> propInstallations = new Vector<Properties>();
	Vector<Properties> propPeople = new Vector<Properties>();
	Vector<Properties> propConsModels = new Vector<Properties>();
	Vector<Properties> propAppliances = new Vector<Properties>();
	Vector<Properties> propActivities = new Vector<Properties>();
	Vector<Properties> propActModels = new Vector<Properties>();
	
	Hashtable<String, String[]> propRequired = new Hashtable<String, String[]>();
	
	public SetupFileParser(String aresources_path, String adbname, int seed) {
		super(aresources_path, adbname, seed);
		
		String[] requiredPropertiesScenario = { "name", "setup"};
		propRequired.put("scenario", requiredPropertiesScenario);
		
		String[] requiredPropertiesSimulation = { "name", "response_type"};
		propRequired.put("simulation", requiredPropertiesSimulation);
		
		String[] requiredPropertiesPricing = { "id", "name", "type"};
		propRequired.put("pricing_policy", requiredPropertiesPricing);
		propRequired.put("pricing_policy_baseline", requiredPropertiesPricing);
		
		//additional required properties per pricing policy type!!!
		
		String[] requiredPropertiesInstallation = { "id", "name"};
		propRequired.put("installation", requiredPropertiesInstallation);
		
		String[] requiredPropertiesPerson = {"id", "name", "installation"};
		propRequired.put("person", requiredPropertiesPerson);
		
		String[] requiredPropertiesConsModel = {"id", "name", "pmodel", "qmodel"};
		propRequired.put("consumption_model", requiredPropertiesConsModel);
		
		String[] requiredPropertiesAppliance = {"id", "name", "installation", "consumption_model"};
		propRequired.put("appliance", requiredPropertiesAppliance);
		
		String[] requiredPropertiesActivity = {"name", "person", "id"};
		propRequired.put("activity", requiredPropertiesActivity);
		
		String[] requiredPropertiesActModel = {"id", "name", "activity", "containsAppliances", "day_type", "duration_distrType", "duration_values", "duration_parameters", 
				"start_distrType", "start_values", "start_parameters", "repetitions_distrType", "repetitions_values", "repetitions_parameters"};
		propRequired.put("activity_model", requiredPropertiesActModel);
		
		//additional required properties per distribution type
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Vector<Installation> setupScenario()
	{
  	    String scenarioName = propScenario.getProperty("name");
  		String responseType = propSimulation.getProperty("response_type");
  	    String locationInfo = propSimulation.getProperty("locationInfo") != null ? propSimulation.getProperty("locationInfo") : "";
  	    int numOfDays = Integer.parseInt((propSimulation.getProperty("numberOfDays") != null ? propSimulation.getProperty("numberOfDays").trim() : "1")); 					
  	    int startDateDay = Integer.parseInt((propSimulation.getProperty("start_dayOfMonth") != null ? propSimulation.getProperty("start_dayOfMonth").trim() : new Date().getDate()+"")); 	
	    int startDateMonth = Integer.parseInt((propSimulation.getProperty("start_month") != null ? propSimulation.getProperty("start_month").trim() : new Date().getMonth()+"")); 		
	    int startDateYear = Integer.parseInt((propSimulation.getProperty("start_year") != null ? propSimulation.getProperty("start_year").trim() : new Date().getYear()+"")); 	
	    SimulationParams simParams = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, startDateDay,  startDateMonth, startDateYear);
		
	    // -- CHANGES HERE!!!
	    // TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
 		String pricingType = "AllInclusivePricing"; 			
  		int billingCycle = 120;  					// all cases
  		double fixedCharge = 15;				// all cases
  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);			
		builderPP.allInclusivePricing(100, 50, 100);
		PricingPolicy pricPolicy = builderPP.build();
		
		String pricingTypeB = "None"; 		
		PricingPolicy pricPolicyB = new PricingPolicy();
		
		
	    int mcruns = Integer.parseInt((propSimulation.getProperty("mcruns") != null ? propSimulation.getProperty("mcruns").trim() : "1")); 	
  		double co2 = Integer.parseInt((propSimulation.getProperty("co2") != null ? propSimulation.getProperty("co2").trim() : "0")); 	
  		String setup = propScenario.getProperty("setup");

  		
  		Vector<Installation> installations = new Vector<Installation>();
  		
  		//Create the installations
  		Hashtable<String, Integer> instIndexes = new Hashtable<String, Integer>();
  		int index = 0;
  		for(Properties prop : propInstallations) 
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
  		for(Properties prop : propConsModels) 
  		{
  			String id = prop.getProperty("id").trim();
  			ConsumptionModel consModelP = new ConsumptionModel();
  			ConsumptionModel consModelQ = new ConsumptionModel();
			try {
				consModelP = new ConsumptionModel(prop.getProperty("pmodel"), "p");
				consModelQ = new ConsumptionModel(prop.getProperty("qmodel"), "q");
			} catch (BadParameterException e) {
				e.printStackTrace();
			}
  			 
  			consModels.put(id+"_p", consModelP);
  			consModels.put(id+"_q", consModelQ);
  		}
		
		// Create the appliances
		HashMap<String, Appliance> appliances = new HashMap<String, Appliance>();
  		for(Properties prop : propAppliances) 
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
  		for(Properties prop : propPeople) 
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
  		for(Properties prop : propActivities) 
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
  		for(Properties prop : propActModels) 
  		{
  			String id = prop.getProperty("id");
  			String actmodDayType = prop.getProperty("day_type");
  			boolean shiftable = Boolean.parseBoolean((prop.getProperty("shiftable") != null ? prop.getProperty("shiftable").trim() : "false")); 	
  			boolean exclusive = Boolean.parseBoolean((prop.getProperty("exclusive") != null ? prop.getProperty("exclusive").trim() : "true")); 	 
  			String temp = prop.getProperty("containsAppliances");
  			String[] containsAppliances = temp.split(",");
  			ProbabilityDistribution actModelStart =  new Histogram(DistributionsLibrary.getStartTimeHistForCleaning());			// -- CHANGES HERE!!!
  			ProbabilityDistribution actModelDuration = new Gaussian(1, 1); 		// -- CHANGES HERE!!!
  			actModelDuration.precompute(0, 1439, 1440);
  			double[] v4 = {0.25,0.375,0.25,0,0,0,0,0.125};
  			ProbabilityDistribution actModelTimes = new Histogram(v4);			// -- CHANGES HERE!!!			
  		
  			Activity act = activities.get(prop.getProperty("activity").trim());
  			act.addDuration(actmodDayType, actModelDuration);
  			act.addStartTime(actmodDayType, actModelStart);
  			act.addTimes(actmodDayType, actModelTimes);
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
	
	private void parseFileForProperties()
	{
		String filename = "properties.txt";
		parseFileForProperties(filename);	
	}
	
	private void parseFileForProperties(String filename)
	{
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			String segment = "";
			String segmentTitle = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith("-->"))
				{
					if (!segment.equals(""))
					{
						parseSegment(segmentTitle, segment);
						segment = "";
					}
					segmentTitle = line.replace("-->", "").trim();
				}
				else
					segment += line + "\n";
			}

			if (!segment.equals(""))
			{
				parseSegment(segmentTitle, segment);
			}

			br.close();
		
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void parseSegment(String segmentTitle, String segment) throws Exception
	{
		Properties prop = new Properties();
		switch(segmentTitle) {
		case "simulation":
			prop = this.propSimulation;
			break;
		case "scenario" :
			prop = this.propScenario;
			break;
		case "installation" :
			this.propInstallations.add(prop);
			break;
		case "person" :
			this.propPeople.add(prop);
			break;
		case "consumption_model" :
			this.propConsModels.add(prop);
			break;
		case "appliance" :
			this.propAppliances.add(prop);
			break;
		case "activity" :
			this.propActivities.add(prop);
			break;
		case "activity_model" :
			this.propActModels.add(prop);
			break;
		case "pricing_policy" :
			prop = this.propPricing;
			break;
		case "pricing_policy_baseline" :
			prop = this.propPricingBaseline;
			break;
		default:
			System.out.println("INFO: Unknown segment \"" + segmentTitle + "\" ignored." + "\n");
			return;
		}
		
		prop.clear();
		prop.load(new ByteArrayInputStream(segment.getBytes()));
		prop.put("segment_title", segmentTitle);
		
		String[] requiredProperties = propRequired.get(segmentTitle);
		for(String property : requiredProperties) {
		   if (prop.get(property) == null)
		   {
			   throw new Exception("ERROR: required property \"" + property + "\" missing for segment \"" + segmentTitle + "\"");
		   }	
		}
	}
	
	
	public static void main(String[] args) {
		SetupFileParser sfp = new SetupFileParser("/Users/fanitzima", "RunFromFile"+System.currentTimeMillis(), 171181);
		sfp.parseFileForProperties();
		
		try {
			sfp.setupStandalone(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		sfp.runStandAlone();
	}

}
