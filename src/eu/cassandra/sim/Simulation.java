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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import eu.cassandra.sim.utilities.Constants;
import eu.cassandra.sim.utilities.DBResults;
import eu.cassandra.sim.utilities.DerbyResults;
import eu.cassandra.sim.utilities.MongoResults;
import eu.cassandra.sim.utilities.ORNG;
import eu.cassandra.sim.utilities.SetupFileParser;
import eu.cassandra.sim.utilities.Utils;

/**
 * The Simulation class includes methods for 
 * setting up the scenario to be simulated and running the simulation, provided the 
 * implementation of the abstract setupScenario() method. It can simulate up to 4085 years. 
 * <br><br>
 * The implementation of the setupScenario() method must include: <br>
 * (a) setting up the installations (or installation types) to be included in the simulation, <br>
 * (b) setting up the pricing scheme(s) to be used in the simulation, <br>
 * (c) defining the set of simulation parameters to be used and, in case of dynamic scenarios, <br>
 * (d) the demographic data, according to which the entities involved in the simulation will be instantiated.
 * <br><br>
 * After providing the implementation of the setupScenario() method, the setup() and 
 * runSimulation() methods need to be called, in order to instantiate the entities involved 
 * in the scenario and simulate it. 
 * 
 * @author Kyriakos C. Chatzidimitriou (kyrcha [at] iti [dot] gr)
 * @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 * 
 */
public abstract class Simulation { 

	/** The installations. */
	private Vector<Installation> installations;

	/** The queue. */
	private PriorityBlockingQueue<Event> queue;

	/** The random seed for the simulation. */
	private int seed; 

	/** The tick. */
	private int tick;

	/** The end tick. */
	private int endTick;

	/** The mcruns. */
	protected int mcruns;

	/** The co2. */
	protected double co2;

	/** The m. */
	private DBResults m;

	/** The simulation world. */
	protected SimulationParams simulationWorld;

	/** The demographics. */
	protected DemographicData demographics;

	/** The pricing. */
	protected PricingPolicy pricing;

	/** The baseline_pricing. */
	protected PricingPolicy baseline_pricing;

	/** The dbname. */
	private String dbname;

	/** The resources_path. */
	private String resources_path;

	/** The orng. */
	private ORNG orng;

	/** The num of days. */
	protected int numOfDays;

	/** The setup type. Can be "static" or "dynamic". */
	protected String setup;

	/** The use derby. */
	private boolean useDerby = false;

	/** The setup file parser. */
	protected static SetupFileParser sfp;
	
	/**
	 * Instantiates a new simulation.
	 *
	 * @param outputPath the path to the output directory
	 * @param dbName the name of the database where output data will be stored
	 * @param seed the seed for the random number generator
	 * @param useDerby whether to use MongoDB (false) or Apache Derby (true) for storing simulation output data
	 */
	public Simulation(String outputPath, String dbName, int seed, boolean useDerby) {

		resources_path = outputPath;
		dbname = dbName;
		this.seed = seed;
		this.useDerby = useDerby;

		if(seed > 0) {
			orng = new ORNG(seed);
		} else {
			orng = new ORNG();
		}

		if (useDerby)
			m = new DerbyResults(dbname);
		else
			m = new MongoResults(dbname);
		m.createTablesAndIndexes();

	}

	
	/**
	 * Abstract method for setting up a scenario. The implementation of the method must include: <br>
	 * (a) setting up the installations (or installation types) to be included in the simulation, <br>
     * (b) setting up the pricing scheme(s) to be used in the simulation, <br>
     * (c) defining the set of simulation parameters to be used and, in case of dynamic scenarios, <br>
     * (d) the demographic data, according to which the entities involved in the simulation will be instantiated.
	 *
	 * @return the vector of installation (types) to be used in the simulation
	 */
	public abstract Vector<Installation> setupScenario();

	/**
	 * Sets the scenario up according to the   {@link #setupScenario() setupScenario} method
	 * and instantiates all involved entities, taking into account the nature (static or dynamic) of the scenario 
	 * and, when applicable, the demographic data provided.  
	 *
	 * @param isCallDuringRun whether this call to the method happens during the run or not
	 * @throws Exception the exception
	 */
	public void setup(boolean isCallDuringRun) throws Exception {

		System.out.println("Simulation setup started");

		Vector<Installation> insts = setupScenario();
		
		if (insts == null)
			throw new Exception("No installations defined for the current scenario.");
		
		if (simulationWorld == null)
			throw new Exception("No simulation parameters defined for the current scenario.");
		
		this.mcruns = this.simulationWorld.getMcruns();
		this.co2 = this.simulationWorld.getCo2();
		this.numOfDays = this.simulationWorld.getNumOfDays();
		this.setup = this.simulationWorld.getSetup();
		
		endTick = Constants.MIN_IN_DAY * numOfDays;

		// Check type of setup
		if (setup.equalsIgnoreCase("static")) {
			//  			this.installations = insts;
			staticSetup(insts);
		} 
		else if (setup.equalsIgnoreCase("dynamic")) {
			if (this.demographics == null)
				throw new Exception("Demographics undefined for \"dynamic\" scenario.");
			this.installations = new Vector<Installation>();
			dynamicSetup(insts, isCallDuringRun);
			//  			dynamicSetupStandalone(jump);
		} else {
			throw new Exception("Problem with setup property: a scenario can be either \"static\" or \"dynamic\"");
		}

		if (this.pricing == null)
			this.pricing = new PricingPolicy();
		if (this.baseline_pricing == null)
			this.baseline_pricing = new PricingPolicy();

		System.out.println("Simulation setup finished");
	}

	/**
	 * Static setup.
	 *
	 * @param installations the installations
	 * @throws Exception the exception
	 */
	private void staticSetup (Vector<Installation> installations) throws Exception {
		this.installations = installations;
		int numOfInstallations = installations.size();
		queue = new PriorityBlockingQueue<Event>(2 * numOfInstallations);
		//	    	for (Installation inst: installations)
		//  		{
		//  			inst.printInstallationInfo();
		//  		}
	}

	/**
	 * Dynamic setup.
	 *
	 * @param instTypes the inst types
	 * @param jump the jump
	 * @throws Exception the exception
	 */
	private void dynamicSetup(Vector<Installation> instTypes, boolean jump) throws Exception {		

		int numOfInstallations = this.demographics.getNumEntities();
		TreeMap<String,Double> instGen = this.demographics.getInst_probs();
		double sum=0; boolean wrongValueDetected = false;
		for (String key: instGen.keySet())
		{
			sum += instGen.get(key);
			if (instGen.get(key) < 0 || instGen.get(key) > 1)
				wrongValueDetected = true;
		}
		if (sum!=1)
			throw new Exception("Problem with scenario demographics: Installation probabilities should sum up to 1.");
		if (wrongValueDetected)
			throw new Exception("Problem with scenario demographics: Installation probabilities should have a value >=0 and <=1.");
		TreeMap<String,Double> applGen = this.demographics.getApp_probs();
		sum=0; wrongValueDetected = false;
		TreeMap<String,Double> personGen = this.demographics.getPerson_probs();
		for (String key: personGen.keySet())
		{
			sum += personGen.get(key);
			if (personGen.get(key) < 0 || personGen.get(key) > 1)
				wrongValueDetected = true;
		}
		if (sum!=1)
			throw new Exception("Problem with scenario demographics: Person probabilities should sum up to 1.");
		if (wrongValueDetected)
			throw new Exception("Problem with scenario demographics: Person probabilities should have a value >=0 and <=1.");
		
		boolean collectionFound = false;
		for (Installation temp: instTypes)
		{
			if (temp.getName().trim().equals("Collection"))
				collectionFound = true;
		}
		if (!collectionFound)
		{
			System.out.println("No \"Collection\" installation found in the current dynamic scenario. Renaming installation " + instTypes.get(0).getName() + " to \"Collection\".");
			instTypes.get(0).setName("Collection");
		}
		
		// number of installation types defined (including collection)
		int maxInsts = instTypes.size();  					
		queue = new PriorityBlockingQueue<Event>(2 * numOfInstallations);

		for (int i = 1; i <= numOfInstallations; i++) {
			String instIndex = getInstId(maxInsts, instGen);
			Installation instDoc = instTypes.get(installationIdToIndex(instTypes, instIndex));
			String instName = instDoc.getName();
//			System.out.println(instName);

			if (instName.equalsIgnoreCase("Collection")) {
				String id = instDoc.getId();
				String name = instDoc.getName() + i;
				String description = instDoc.getDescription();
				String type = instDoc.getType();
				Installation inst = new Installation.Builder(id, name, description, type, pricing, baseline_pricing).build();
				//				inst.setParentId(scenario_id);
				String inst_id = id + i;
				inst.setId(inst_id);
				Vector<Appliance> apps = instDoc.getAppliances();
				int appcount = apps.size();  
				// Create the appliances
				TreeMap<String,Appliance> existing = new TreeMap<String,Appliance>();
				for (int j = 0; j < appcount; j++) {
					Appliance applianceDoc = apps.get(j);
					String appid = applianceDoc.getId();
					String appname = applianceDoc.getName();
					String appdescription = applianceDoc.getDescription();
					String apptype = applianceDoc.getType();
					double standy = applianceDoc.getStandByConsumption();
					boolean base = applianceDoc.isBase();
					ConsumptionModel pconsmod = new ConsumptionModel(applianceDoc.getPConsumptionModel().toDBObject().get("model").toString(), "p");
					ConsumptionModel qconsmod = new ConsumptionModel(applianceDoc.getQConsumptionModel().toDBObject().get("model").toString(), "q");
					Appliance app = new Appliance.Builder( appid, appname, appdescription, apptype, inst, pconsmod, qconsmod, standy, base).build(orng);
					String app_id =  inst_id + "_" + appid;
					existing.put(app_id, app);
				}

				Set<String> keys = existing.keySet();
				for(String key  : keys) {
					String key2 = key.split("_")[1];
					Double prob = applGen.get(key2);
					if(prob != null) {
						double probValue = prob.doubleValue();
						double temp = orng.nextDouble();
						if( temp < probValue) {
							Appliance selectedApp = existing.get(key);
//							selectedApp.setParentId(inst.getId());
							String app_id =  inst_id + "_" + selectedApp.getId();
							selectedApp.setId(app_id);
							inst.addAppliance(selectedApp);
							//							ConsumptionModel cm = selectedApp.getPConsumptionModel();
							//							cm.setParentId(app_id);
							//							String cm_id = "lala"; //addEntity(cm, jump);
							//							cm.setId(cm_id);
						}
					}
				}

				int personcount = instDoc.getPersons().size();
				// Create the appliances
				TreeMap<String,Person> existingPersons = new TreeMap<String,Person>();
				for (int j = 0; j < personcount; j++) {
					Person personDoc = instDoc.getPersons().get(j);
					String personid = personDoc.getId();
					String personName = personDoc.getName();
					String personDescription = personDoc.getDescription();
					String personType = personDoc.getType();
					double awareness = personDoc.getAwareness();
					double sensitivity = personDoc.getSensitivity();
					Person person = new Person.Builder(personid, personName, personDescription, personType, inst, awareness, sensitivity).build();
					Vector<Activity> acts = personDoc.getActivities();
					int actcount = acts.size();
					//System.out.println("Act-Count: " + actcount);
					for (int k = 0; k < actcount; k++) {
						Activity activityDoc = acts.get(k);
						String activityName = activityDoc.getName();
						String activityType = activityDoc.getType();
						String actid = activityDoc.getId();
						Activity act = new Activity.Builder(actid, activityName, "", activityType, simulationWorld).build();
						TreeMap<String, Vector<Appliance>> actModApps = activityDoc.getAppliances();
						TreeMap<String, Boolean> shiftables = activityDoc.getShiftable();
						TreeMap<String, Boolean> exclusives = activityDoc.getConfig();
						TreeMap<String, ProbabilityDistribution> probStartTime = activityDoc.getProbStartTime();
						TreeMap<String, ProbabilityDistribution> probDuration = activityDoc.getProbDuration();
						TreeMap<String, ProbabilityDistribution> probeTimes = activityDoc.getnTimesGivenDay();
						ProbabilityDistribution startDist;
						ProbabilityDistribution durDist;
						ProbabilityDistribution timesDist;
						Vector<Appliance> appliances;
						for (String key: shiftables.keySet()) {
							boolean shiftable = shiftables.get(key);
							boolean exclusive = exclusives.get(key);
							durDist =  copyProbabilityDistribution(probDuration.get(key), "duration");
							startDist = copyProbabilityDistribution(probStartTime.get(key), "start");
							timesDist = copyProbabilityDistribution(probeTimes.get(key), "times");
							act.addDuration(key, durDist);
							act.addStartTime(key, startDist);
							act.addTimes(key, timesDist);
							act.addShiftable(key, shiftable);
							act.addConfig(key, exclusive);
							// add appliances
							appliances = actModApps.get(key);
							String[] containsAppliances = new String[appliances.size()];
							for(int m = 0; m < appliances.size(); m++) 
								containsAppliances[m] = inst_id + "_" + appliances.get(m).getId();
							act.addAppliances(containsAppliances, existing, key);
						}
						person.addActivity(act);
					}
					existingPersons.put(personid, person);
				}

				double roulette = orng.nextDouble();
				sum = 0;
				for( String entityId : personGen.keySet() ) {
					if(existingPersons.containsKey(entityId)) {
						double prob = personGen.get(entityId);
						sum += prob;
						if(roulette < sum) {
							Person selectedPerson = existingPersons.get(entityId);
//							selectedPerson.setParentId(inst.getId());
							String person_id = inst_id + "_" + selectedPerson.getId();
							selectedPerson.setId(person_id);
							inst.addPerson(selectedPerson);
							Vector<Activity> activities = selectedPerson.getActivities();
							for(Activity a : activities) {
//								a.setParentId(person_id);
								String act_id = person_id + a.getId();
								a.setId(act_id);
								//								Vector<DBObject> models = a.getModels();
								//								Vector<DBObject> starts = a.getStarts();
								//								Vector<DBObject> durations = a.getDurations();
								//								Vector<DBObject> times = a.getTimes();
								//								for(int l = 0; l < models.size(); l++ ) {
								//									DBObject m = models.get(l);
								//									m.put("act_id", act_id);
								//									if(!jump)DBConn.getConn(dbname).getCollection("act_models").insert(m);
								//									ObjectId objId = (ObjectId)m.get("_id");
								//									String actmod_id = objId.toString();
								//									DBObject s = starts.get(l);
								//									s.put("actmod_id", actmod_id);
								//									if(!jump)DBConn.getConn(dbname).getCollection("distributions").insert(s);
								//									DBObject d = durations.get(l);
								//									d.put("actmod_id", actmod_id);
								//									if(!jump)DBConn.getConn(dbname).getCollection("act_models").insert(d);
								//									DBObject t = times.get(l);
								//									t.put("actmod_id", actmod_id);
								//									if(!jump)DBConn.getConn(dbname).getCollection("act_models").insert(t);
								//								}
							}
							break;
						}
					}
				}
				installations.add(inst);
			}  // end if (instName.equalsIgnoreCase("Collection")) {
			else 
			{
				String id = instDoc.getId();
				String name = instDoc.getName() + i;
				String description = instDoc.getDescription();
				String type = instDoc.getType();
				//				String clustername = instDoc.getClu
				PricingPolicy instPricing = pricing;
				PricingPolicy instBaseline_pricing = baseline_pricing;
				Installation inst = new Installation.Builder(id, name, description, type, instPricing, instBaseline_pricing).build();
				//				inst.setParentId(scenario_id);
				String inst_id = id + i;
				inst.setId(inst_id);
				Vector<Appliance> apps = instDoc.getAppliances();
				int appcount = apps.size();  
				// Create the appliances
				TreeMap<String,Appliance> existing = new TreeMap<String,Appliance>();
				for (int j = 0; j < appcount; j++) {
					Appliance applianceDoc = apps.get(j);
					String appid = applianceDoc.getId();
					String appname = applianceDoc.getName();
					String appdescription = applianceDoc.getDescription();
					String apptype = applianceDoc.getType();
					double standy = applianceDoc.getStandByConsumption();
					boolean base = applianceDoc.isBase();
					ConsumptionModel pconsmod = new ConsumptionModel(applianceDoc.getPConsumptionModel().toDBObject().get("model").toString(), "p");
					ConsumptionModel qconsmod = new ConsumptionModel(applianceDoc.getQConsumptionModel().toDBObject().get("model").toString(), "q");
					Appliance app = new Appliance.Builder( appid, appname, appdescription, apptype, inst, pconsmod, qconsmod, standy, base).build(orng);
//					app.setParentId(inst.getId());
					String app_id =  inst_id + "_" + appid;
					app.setId(app_id);
					existing.put(app_id, app);
					inst.addAppliance(app);
					//					ConsumptionModel cm = app.getPConsumptionModel();
					//					cm.setParentId(app_id);
					//					String cm_id = addEntity(cm, jump);
					//					cm.setId(cm_id);
				}

				Person personDoc = instDoc.getPersons().get(0);
				String personid = personDoc.getId();
				String personName = personDoc.getName();
				String personDescription = personDoc.getDescription();
				String personType = personDoc.getType();
				double awareness = personDoc.getAwareness();
				double sensitivity = personDoc.getSensitivity();
				Person person = new Person.Builder(personid, personName, personDescription, personType, inst, awareness, sensitivity).build();
//				person.setParentId(inst.getId());
				String person_id =  inst_id + "_" + personid;
				person.setId(person_id);
				inst.addPerson(person);
				Vector<Activity> acts = personDoc.getActivities();
				int actcount = acts.size();
				for (int j = 0; j < actcount; j++) {
					Activity activityDoc = acts.get(j);
					String activityName = activityDoc.getName();
					String activityType = activityDoc.getType();
					String actid = activityDoc.getId();
					Activity act = new Activity.Builder(actid, activityName, "", activityType, simulationWorld).build();
					TreeMap<String, Vector<Appliance>> actModApps = activityDoc.getAppliances();
					TreeMap<String, Boolean> shiftables = activityDoc.getShiftable();
					TreeMap<String, Boolean> exclusives = activityDoc.getConfig();
					TreeMap<String, ProbabilityDistribution> probStartTime = activityDoc.getProbStartTime();
					TreeMap<String, ProbabilityDistribution> probDuration = activityDoc.getProbDuration();
					TreeMap<String, ProbabilityDistribution> probeTimes = activityDoc.getnTimesGivenDay();

					ProbabilityDistribution startDist;
					ProbabilityDistribution durDist;
					ProbabilityDistribution timesDist;
					Vector<Appliance> appliances;
					for (String key: shiftables.keySet()) {
						boolean shiftable = shiftables.get(key);
						boolean exclusive = exclusives.get(key);
						durDist =  copyProbabilityDistribution(probDuration.get(key), "duration");
						startDist = copyProbabilityDistribution(probStartTime.get(key), "start");
						timesDist = copyProbabilityDistribution(probeTimes.get(key), "times");
						act.addDuration(key, durDist);
						act.addStartTime(key, startDist);
						act.addTimes(key, timesDist);
						act.addShiftable(key, shiftable);
						act.addConfig(key, exclusive);
						// add appliances
						appliances = actModApps.get(key);
						String[] containsAppliances = new String[appliances.size()];
						for(int l = 0; l < appliances.size(); l++) 
							containsAppliances[l] = inst_id + "_" + appliances.get(l).getId();
						act.addAppliances(containsAppliances, existing, key);
					}
					person.addActivity(act);
//					act.setParentId(person_id);
					String act_id = person_id + actid;
					act.setId(act_id);
				}
				installations.add(inst);
			}
		}

//		for (Installation inst: installations)
//		{
//			inst.printInstallationInfo();
//		}

	}

	/**
	 * Runs the simulation and outputs results to the csv files and the selected database.
	 * Results include the expected, active and reactive power per installation, 
	 * along with various KPIs that can be printed out using the  {@link #printKPIs() printKPIs} method.
	 */
	public void runSimulation () {
		if(seed > 0) {
			orng = new ORNG(seed);
		} else {
			orng = new ORNG();
		}

		try {
			//  			DBObject objRun = DBConn.getConn().getCollection(MongoRuns.COL_RUNS).findOne(query);
			System.out.println("Run " + dbname + " started @ " + Calendar.getInstance().getTime());
			calculateExpectedPower(dbname);
			//  			System.out.println("EP calculated");
			long startTime = System.currentTimeMillis();
			int mccount = 0;
			double mcrunsRatio = 1.0/mcruns;
			for(int i = 0; i < mcruns; i++) {
				tick = 0;
				double avgPPowerPerHour = 0;
				double avgQPowerPerHour = 0;
				double[] avgPPowerPerHourPerInst = new double[installations.size()];
				double[] avgQPowerPerHourPerInst = new double[installations.size()];
				double maxPower = 0;
				//  	  			double cycleMaxPower = 0;
				double avgPower = 0;
				double energy = 0;
				double energyOffpeak = 0;
				double cost = 0;
				//  	  			double billingCycleEnergy = 0;
				//  	  			double billingCycleEnergyOffpeak = 0;
				while (tick < endTick) {
					// If it is the beginning of the day create the events
					if (tick % Constants.MIN_IN_DAY == 0) {
						//  	  				System.out.println("Day " + ((tick / Constants.MIN_IN_DAY) + 1));
						for (Installation installation: installations) {
							//  						System.out.println("Installation: " + installation.getName());
							installation.updateDailySchedule(tick, queue, simulationWorld.getResponseType(), orng);

						}
						//  					System.out.println("Daily queue size: " + queue.size() + "(" + 
						//  					simulationWorld.getSimCalendar().isWeekend(tick) + ")");
					}
					Event top = queue.peek();
					while (top != null && top.getTick() == tick) {
						Event e = queue.poll();
						boolean applied = e.apply();
						if(applied) {
							if(e.getAction() == Event.SWITCH_ON) {
								try {
									//m.addOpenTick(e.getAppliance().getId(), tick);
								} catch (Exception exc) {
									throw exc;
								}
							} else if(e.getAction() == Event.SWITCH_OFF){
								//m.addCloseTick(e.getAppliance().getId(), tick);
							}
						}
						top = queue.peek();
					}

					/*
					 *  Calculate the total power for this simulation step for all the
					 *  installations.
					 */
					float sumP = 0;
					float sumQ = 0;
					int counter = 0;
					for(Installation installation: installations) {
						installation.nextStep(tick);
						double p = installation.getCurrentPowerP();
						double q = installation.getCurrentPowerQ();
						//		  				if(p> 0.001) System.out.println(p);
						installation.updateMaxPower(p);
						installation.updateAvgPower(p/endTick);
						if(installation.getPricing().isOffpeak(tick)) {
							installation.updateEnergyOffpeak(p);
						} else {
							installation.updateEnergy(p);
						}
						installation.updateAppliancesAndActivitiesConsumptions(tick, endTick);
						m.addTickResultForInstallation(tick, 
								installation.getId(), 
								p * mcrunsRatio, 
								q * mcrunsRatio, 
								DBResults.COL_INSTRESULTS);
						sumP += p;
						sumQ += q;
						avgPPowerPerHour += p;
						avgQPowerPerHour += q;
						avgPPowerPerHourPerInst[counter] += p;
						avgQPowerPerHourPerInst[counter] += q;
						String name = installation.getName();
						//		  				System.out.println("INFO: Tick: " + tick + " \t " + "Name: " + name + " \t " 
						//		  		  				+ "Power: " + p);
						if((tick + 1) % (Constants.MIN_IN_DAY *  installation.getPricing().getBillingCycle()) == 0 || installation.getPricing().getType().equalsIgnoreCase("TOUPricing")) {
							installation.updateCost(tick);
						}
						counter++;
					}
					if(sumP > maxPower) maxPower = sumP;
					//		  			if(sumP > cycleMaxPower) cycleMaxPower = sumP;
					avgPower += sumP/endTick;
					energy += (sumP/1000.0) * Constants.MINUTE_HOUR_RATIO;
					//		  			if(pricing.isOffpeak(tick)) {
					//		  				energyOffpeak += (sumP/1000.0) * Constants.MINUTE_HOUR_RATIO;
					//		  			} else {
					//		  				energy += (sumP/1000.0) * Constants.MINUTE_HOUR_RATIO;
					//		  			}
					//		  			if((tick + 1) % (Constants.MIN_IN_DAY *  pricing.getBillingCycle()) == 0 || pricing.getType().equalsIgnoreCase("TOUPricing")) {
					//		  				cost = totalInstCost(); //alternate method
					//		  				billingCycleEnergy = energy;
					//		  				billingCycleEnergyOffpeak = energyOffpeak;
					//		  				cycleMaxPower = 0;
					//		  			}
					m.addAggregatedTickResult(tick, 
							sumP * mcrunsRatio, 
							sumQ * mcrunsRatio, 
							DBResults.COL_AGGRRESULTS);
					tick++;
					if(tick % Constants.MIN_IN_HOUR == 0) {
						m.addAggregatedTickResult((tick/Constants.MIN_IN_HOUR), 
								(avgPPowerPerHour/Constants.MIN_IN_HOUR) * mcrunsRatio, 
								(avgQPowerPerHour/Constants.MIN_IN_HOUR) * mcrunsRatio, 
								DBResults.COL_AGGRRESULTS_HOURLY);
						m.addAggregatedTickResult((tick/Constants.MIN_IN_HOUR), 
								(avgPPowerPerHour) * mcrunsRatio, 
								(avgQPowerPerHour) * mcrunsRatio, 
								DBResults.COL_AGGRRESULTS_HOURLY_EN);
						avgPPowerPerHour = 0;
						avgQPowerPerHour = 0;
						counter = 0;
						for(Installation installation: installations) {
							m.addTickResultForInstallation((tick/Constants.MIN_IN_HOUR), 
									installation.getId(),
									(avgPPowerPerHourPerInst[counter]/Constants.MIN_IN_HOUR) * mcrunsRatio, 
									(avgQPowerPerHourPerInst[counter]/Constants.MIN_IN_HOUR) * mcrunsRatio, 
									DBResults.COL_INSTRESULTS_HOURLY);
							m.addTickResultForInstallation((tick/Constants.MIN_IN_HOUR), 
									installation.getId(),
									(avgPPowerPerHourPerInst[counter]) * mcrunsRatio, 
									(avgQPowerPerHourPerInst[counter]) * mcrunsRatio, 
									DBResults.COL_INSTRESULTS_HOURLY_EN);
							avgPPowerPerHourPerInst[counter] = 0;
							avgQPowerPerHourPerInst[counter] = 0;
							counter++;
						}
					}
					mccount++;
					//		  			percentage = (int)(0.75 * mccount * 100.0 / (mcruns * endTick));
					//		  			System.out.println("Percentage: " + percentage + " - " + mccount);
					//		  			objRun.put("percentage", 25 + percentage);
					//		  	  		DBConn.getConn().getCollection(MongoRuns.COL_RUNS).update(query, objRun);
				}
				for(Installation installation: installations) {
					installation.updateCost(tick); // update the rest of the energy
					m.addKPIs(installation.getId(), 
							installation.getMaxPower() * mcrunsRatio, 
							installation.getAvgPower() * mcrunsRatio, 
							installation.getEnergy() * mcrunsRatio, 
							installation.getCost() * mcrunsRatio,
							installation.getEnergy() * co2 * mcrunsRatio);
					installation.addAppliancesKPIs(m, mcrunsRatio, co2);
					installation.addActivitiesKPIs(m, mcrunsRatio, co2);
				}
				cost = totalInstCost();
				m.addKPIs(DBResults.AGGR, 
						maxPower * mcrunsRatio, 
						avgPower * mcrunsRatio, 
						energy * mcrunsRatio, 
						cost * mcrunsRatio,
						energy * co2 * mcrunsRatio);
				if(i+1 != mcruns) setup(true);

			}
			// Write installation results to csv file
			if (!resources_path.endsWith("/"))
				resources_path += "/";
			String filename = resources_path + dbname + ".csv";
			//  			System.out.println(filename);
			File csvFile = new File(filename);
			FileWriter fw = new FileWriter(csvFile);
			String row = "tick";
			for(Installation installation: installations) {
				row += "," + installation.getName() + "_p";
				row += "," + installation.getName() + "_q";
			}
			fw.write(row+"\n");
			for(int i = 0; i < endTick; i++) {
				row = String.valueOf(i);
				for(Installation installation: installations) {
					if (useDerby)
					{
						ResultSet tickResult = ((DerbyResults)m).getTickResultForInstallation(i, 
								installation.getId(),  
								DBResults.COL_INSTRESULTS);
						while (tickResult.next())
						{
							double p = tickResult.getDouble(3);
							double q = tickResult.getDouble(4);
							row += "," + p;
							row += "," + q;
						}	
					}
					else
					{
						DBObject tickResult =  ((MongoResults)m).getTickResultForInstallation(i, 
								installation.getId(),  
								DBResults.COL_INSTRESULTS);
						double p = ((Double)tickResult.get("p")).doubleValue();
						double q = ((Double)tickResult.get("q")).doubleValue();
						row += "," + p;
						row += "," + q;
					}
				}  				
				fw.write(row+"\n");
			}
			fw.flush();
			fw.close();
			// End of file writing
			System.out.println("Zipping output files...");
			byte[] buffer = new byte[1024];
			FileOutputStream fos = new FileOutputStream(filename + ".zip");
			ZipOutputStream zos = new ZipOutputStream(fos);
			ZipEntry ze = new ZipEntry(dbname + ".csv");
			zos.putNextEntry(ze);
			FileInputStream in = new FileInputStream(filename);
			int len;
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			in.close();
			zos.closeEntry();

			ze = new ZipEntry(dbname + "_exp_pow.csv");
			zos.putNextEntry(ze);
			filename = resources_path + dbname + "_exp_pow.csv";
			File csvFile2 = new File(filename);
			in = new FileInputStream(filename);
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			in.close();
			zos.closeEntry();

			zos.close();
			fos.close();
			csvFile.delete();
			csvFile2.delete();
			System.out.println("End of Zipping...");
			long endTime = System.currentTimeMillis();
			System.out.println("Time elapsed for Run " + dbname + ": " + ((endTime - startTime)/(1000.0 * 60)) + " mins");
			System.out.println("Run " + dbname + " ended @ " + Calendar.getInstance().getTime());
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(Utils.stackTraceToString(e.getStackTrace()));
		}
	}
	

	/**
	 * Gets the installations.
	 *
	 * @return the installations
	 */
	public Collection<Installation> getInstallations () {
		return installations;
	}

	/**
	 * Gets the installation at {@code index}.
	 *
	 * @param index the index
	 * @return the installation
	 */
	public Installation getInstallation (int index) {
		return installations.get(index);
	}

	/**
	 * Gets the current tick.
	 *
	 * @return the current tick
	 */
	public int getCurrentTick () {
		return tick;
	}

	/**
	 * Gets the end tick.
	 *
	 * @return the end tick
	 */
	public int getEndTick () {
		return endTick;
	}

	/**
	 * Gets the random number generator.
	 *
	 * @return the orng
	 */
	public ORNG getOrng() {
		return orng;
	}
	
	/**
	 * Gets the simulation parameters set.
	 *
	 * @return the simulation world
	 */
	public SimulationParams getSimulationWorld () {
		return simulationWorld;
	}

	
	/**
	 * Prints the aggregate KPIs, followed by the KPIs for all installations, appliances and activities.
	 */
	public void printKPIs()
	{
		System.out.println();
		printKPIs(m.getKPIs(DBResults.AGGR), null, DBResults.AGGR);
		for (Installation inst: this.getInstallations())
		{
			printKPIs(m.getKPIs(inst.getId()), "installation", inst.getId());
			for (Appliance app: inst.getAppliances())
				printKPIs(m.getAppKPIs(app.getId()), "appliance", app.getId());
			for (Person p: inst.getPersons())
				for (Activity a: p.getActivities())
					printKPIs(m.getActKPIs(a.getId()), p.getName() + "'s activity", a.getId());
		}
	}

	protected void printKPIs( HashMap<String, Double> kpis, String entityType, String entityId)
	{
		if (entityType == null && entityId == DBResults.AGGR)
			System.out.println("Aggregate KPIs");
		else
			System.out.println("KPIs for " + entityType + " " + entityId);
		for (String key: kpis.keySet())
			System.out.println(key + " \t" + kpis.get(key));
		System.out.println();
	}


	private void calculateExpectedPower(String dbname) {
		double[] aggr_exp = new double[Constants.MIN_IN_DAY];
		for(Installation installation: installations) {
			double[] inst_exp = new double[Constants.MIN_IN_DAY];
			Person person = installation.getPersons().get(0);
			for(Activity activity: person.getActivities()) {
				double[] act_exp = activity.calcExpPower();
				for(int i = 0; i < act_exp.length; i++) {
					inst_exp[i] += act_exp[i];
					m.addExpectedPowerTick(i, activity.getId(), act_exp[i], DBResults.COL_ACTRESULTS_EXP);
				}
			}
			// For every appliance that is a base load find mean value and add
			for(Appliance appliance: installation.getAppliances()) {
				if(appliance.isBase()) {
					double mean = 0;
					Double[] cons = appliance.getActiveConsumption();
					for(int i = 0; i < cons.length; i++) {
						mean += cons[i].doubleValue();
					}
					mean /= cons.length;
					for(int i = 0; i < inst_exp.length; i++) {
						inst_exp[i] += mean;
					}
				}
			}

			for(int i = 0; i < inst_exp.length; i++) {
				aggr_exp[i] += inst_exp[i];
				m.addExpectedPowerTick(i, installation.getId(), inst_exp[i], DBResults.COL_INSTRESULTS_EXP);
			}
		}
		for(int i = 0; i < aggr_exp.length; i++) 
			m.addExpectedPowerTick(i, "aggr", aggr_exp[i], DBResults.COL_AGGRRESULTS_EXP);

		// Write installation results to csv file
		if (!resources_path.endsWith("/"))
			resources_path += "/";
		String filename = resources_path + dbname + "_exp_pow.csv";
		File csvFile = new File(filename);
		FileWriter fw;
		try {
			fw = new FileWriter(csvFile);
			String row = "tick";
			for(Installation installation: installations) {
				row += "," + installation.getName() + "_p";
				//	  			row += "," + installation.getName() + "_q";
			}
			fw.write(row+"\n");
			for(int i = 0; i < Constants.MIN_IN_DAY; i++) {
				row = String.valueOf(i);
				for(Installation installation: installations) {
					if (useDerby)
					{
						ResultSet tickResult = ((DerbyResults)m).getExpectedPowerTickResultForInstallation(i, 
								installation.getId(),  
								DBResults.COL_INSTRESULTS_EXP);
						while (tickResult.next())
						{
							double p = tickResult.getDouble(3);
							//	  						double q = tickResult.getDouble(4);
							row += "," + p;
							//	  						row += "," + q;
						}	
					}
					else
					{
						DBObject tickResult =  ((MongoResults)m).getExpectedPowerTickResultForInstallation(i, 
								installation.getId(),  
								DBResults.COL_INSTRESULTS_EXP);
						double p = ((Double)tickResult.get("p")).doubleValue();
						//	  					double q = ((Double)tickResult.get("q")).doubleValue();
						row += "," + p;
						//	  					row += "," + q;
					}
				}  				
				fw.write(row+"\n");
			}
			fw.flush();
			fw.close();
			// End of file writing
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private double totalInstCost() {

		double cost = 0;
		for(Installation installation: installations) {
			cost += installation.getCost();
		}
		return cost;
	}

	private String getInstId(int maxInsts, TreeMap<String,Double> instGen) {
		ArrayList<String> temp = new ArrayList(instGen.keySet());
		if (maxInsts == 1) {
			return temp.get(0);
		} 
		else {
			double prob = orng.nextFloat();
			Set<String> keys = instGen.keySet();
			double sum = 0;
			for(String s : keys) {
				sum += instGen.get(s).doubleValue();
				if(prob < sum) {
					return s;
				}
			}
		}
		return temp.get(temp.size());
	}
	
	private ProbabilityDistribution copyProbabilityDistribution(ProbabilityDistribution source, String flag) throws Exception
	{
		switch (source.getType()) { 
		case ( "Gaussian"):
			return new Gaussian((Gaussian)source);
		case ("Uniform"):
			return new Uniform((Uniform)source);
		case ("GMM"):
			return new GaussianMixtureModels((GaussianMixtureModels)source);
		case ("Histogram"):
			return new Histogram((Histogram)source);
		default:
			throw new Exception("Non existing distribution type. Problem in setting up the simulation.");
		}
	}
	
	private int installationIdToIndex(Vector<Installation> instTypes, String instID)
	{
		for (int i=0; i < instTypes.size(); i++)
		{
			Installation temp = instTypes.get(i);
			if (temp.getId().equals(instID))
				return i;
		}
		return -1;
	}
	
}
