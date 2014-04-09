package eu.cassandra.sim.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
		
		String[] requiredPropertiesSimulation = { "name", "responseType"};
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
		
		String[] requiredPropertiesActivity = {"name", "person", "activity_model"};
		propRequired.put("activity", requiredPropertiesActivity);
		
		String[] requiredPropertiesActModel = {"id", "name", "containsAppliances", "day_type", "duration_distrType", "duration_values", "duration_parameters", 
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
  	    int numOfDays = (Integer) (propSimulation.getProperty("numberOfDays") != null ? propSimulation.getProperty("numberOfDays") : 1); 					
  	    int startDateDay =  (Integer) (propSimulation.getProperty("start_dayOfMonth") != null ? propSimulation.getProperty("start_dayOfMonth") : new Date().getDate()); 	
	    int startDateMonth =(Integer) (propSimulation.getProperty("start_month") != null ? propSimulation.getProperty("start_month") : new Date().getMonth()); 	
	    int startDateYear = (Integer) (propSimulation.getProperty("start_year") != null ? propSimulation.getProperty("start_year") : new Date().getYear()); 	
	    SimulationParams simParams = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, startDateDay,  startDateMonth, startDateYear);
		
 		String pricingType = "AllInclusivePricing"; 			// TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
  		int billingCycle = 120;  					// all cases
  		double fixedCharge = 15;				// all cases
  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);			
		builderPP.allInclusivePricing(100, 50, 100);
		PricingPolicy pricPolicy = builderPP.build();
		
		String pricingTypeB = "None"; 		// TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
		PricingPolicy pricPolicyB = new PricingPolicy();
		
		
	    int mcruns = (Integer) (propSimulation.getProperty("mcruns") != null ? propSimulation.getProperty("mcruns") : 1); 	
  		double co2 = (Integer) (propSimulation.getProperty("co2") != null ? propSimulation.getProperty("co2") : 0); 	
  		String setup =propSimulation.getProperty("setup");

  		
  		Vector<Installation> installations = new Vector<Installation>();
  		
  		//Create the installations
  		Hashtable<String, Integer> instIndexes = new Hashtable<String, Integer>();
  		int index = 0;
  		for(Properties prop : propInstallations) 
  		{
			String instName = prop.getProperty("name");
			String instID= prop.getProperty("id");
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
  			String id = prop.getProperty("id");
  			ConsumptionModel consModelP = new ConsumptionModel(prop.getProperty("pmodel"), "p");
  			ConsumptionModel consModelQ = new ConsumptionModel(prop.getProperty("qmodel"), "q");
  			consModels.put(id+"_p", consModelP);
  			consModels.put(id+"_q", consModelQ);
  		}
		
		// Create the appliances
		HashMap<String, Appliance> appliances = new HashMap<String, Appliance>();
  		for(Properties prop : propAppliances) 
  		{
			String applName = prop.getProperty("name");
			String appliID = prop.getProperty("id");
			String applDescription = prop.getProperty("description") != null ? prop.getProperty("description") : "";
			String applType = prop.getProperty("type") != null ? prop.getProperty("type") : "";
			double applStandByCons = (Double) (prop.getProperty("standy_consumption") != null ? prop.getProperty("standy_consumption") : 0); 	
			boolean applIsBase = (Boolean) (prop.getProperty("base") != null ? prop.getProperty("base") : false); 	
			Installation inst = installations.get(instIndexes.get(prop.getProperty("installation")));
			ConsumptionModel consMP = consModels.get("consumption_model"+"_p");
  			ConsumptionModel consMQ = consModels.get("consumption_model"+"_q");
			Appliance app1 = new Appliance.Builder(appliID,  applName, applDescription, applType, inst, consMP, consMP, applStandByCons, applIsBase).build(getOrng());
			appliances.put(appliID, app1);
			inst.addAppliance(app1);
  		}
		
		// Create the people
  		HashMap<String, Person> people = new HashMap<String, Person>();
  		for(Properties prop : propAppliances) 
  		{
			String personName = prop.getProperty("name");
			String personID = prop.getProperty("id");
			String personDesc =prop.getProperty("description") != null ? prop.getProperty("description") : "";
			String personType = prop.getProperty("type") != null ? prop.getProperty("type") : "";
			double awareness= (Double) (prop.getProperty("awareness") != null ? prop.getProperty("awareness") : 0); 	
			double sensitivity= (Double) (prop.getProperty("sensitivity") != null ? prop.getProperty("sensitivity") : 0); 	
			Installation inst = installations.get(instIndexes.get(prop.getProperty("installation")));
			Person person = new Person.Builder(personID, personName, personDesc, personType, inst, awareness, sensitivity).build();
			inst.addPerson(person);	
			people.put(personID, person);
  		}
  		
  		
  		// Create the activity models -- CHANGES HERE!!!
  		Hashtable<String, ProbabilityDistribution> actModels = new Hashtable<String, ProbabilityDistribution>();
  		for(Properties prop : propConsModels) 
  		{
  			String id = prop.getProperty("id");
  			ProbabilityDistribution actModelStart = new Gaussian(1, 1); 
  			ProbabilityDistribution actModelDuration = new Gaussian(1, 1); 
  			ProbabilityDistribution actModelTimes =new Gaussian(1, 1); 
  			actModels.put(id+"_s", actModelStart);
  			actModels.put(id+"_d", actModelDuration);
  			actModels.put(id+"_t", actModelTimes);
  		}
		
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
			Appliance app  = appliances.get(containAppId);
			act1.addAppliance(actmodDayType,app,1.0/containsAppliances.length);
		}
		
//		person.addActivity(act1); -- CHANGES HERE!!!

		
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
			
//			propSimulation.list(System.out);
//			System.out.println();
//			
//			
//			
//			
//			
//			propScenario.list(System.out);
//			System.out.println();
//			propPricing.list(System.out);
//			System.out.println();
//			propPricingBaseline.list(System.out);
//			System.out.println();
//			for (int i=0; i<propInstallations.size();i++)
//				propInstallations.get(i).list(System.out);
//			System.out.println();
//			for (int i=0; i<propPeople.size();i++)
//				propPeople.get(i).list(System.out);
//			System.out.println();
//			for (int i=0; i<propConsModels.size();i++)
//				propConsModels.get(i).list(System.out);
//			System.out.println();
//			for (int i=0; i<propAppliances.size();i++)
//				propAppliances.get(i).list(System.out);
//			System.out.println();
//			for (int i=0; i<propActivities.size();i++)
//				propActivities.get(i).list(System.out);
//			System.out.println();
//			for (int i=0; i<propActModels.size();i++)
//				propActModels.get(i).list(System.out);
//			System.out.println();
		

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
		SetupFileParser sfp = new SetupFileParser("/Users/fanitzima", "RunFromFile"+System.currentTimeMillis(), 55);
		sfp.parseFileForProperties();
		
		try {
			sfp.setupStandalone(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		sfp.runStandAlone();
	}

}
