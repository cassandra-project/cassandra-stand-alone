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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.cassandra.sim.entities.Entity;
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
import eu.cassandra.sim.utilities.ConsumptionModelsLibrary;
import eu.cassandra.sim.utilities.DBConn;
import eu.cassandra.sim.utilities.DBResults;
import eu.cassandra.sim.utilities.DerbyResults;
import eu.cassandra.sim.utilities.DistributionsLibrary;
import eu.cassandra.sim.utilities.MongoResults;
import eu.cassandra.sim.utilities.ORNG;
import eu.cassandra.sim.utilities.Utils;

/**
 * The Simulation class can simulate up to 4085 years of simulation.
 * 
 * @author Kyriakos C. Chatzidimitriou (kyrcha [at] iti [dot] gr)
 * @author Fani A. Tzima (fani [at] iti [dot] gr)
 * 
 */
public class Simulation { // implements Runnable {

	private Vector<Installation> installations;

	private PriorityBlockingQueue<Event> queue;
  	
	private int tick;

	private int endTick;

	protected int mcruns;

	protected double co2;

	private DBResults m;

	protected SimulationParams simulationWorld;

	protected PricingPolicy pricing;

	protected PricingPolicy baseline_pricing;
	
	private String scenario;

	private String dbname;
	
	private String resources_path;

	private ORNG orng;

	protected int numOfDays;

	protected String setup;
	
	private static boolean useDerby = false;

	public Collection<Installation> getInstallations () {
		return installations;
	}
	
/*	
	public Simulation(String aresources_path, int seed) {
		resources_path = aresources_path;
		
		if(seed > 0) {
			orng = new ORNG(seed);
		} else {
			orng = new ORNG();
		}
  		
	}
*/	
	
	public Installation getInstallation (int index) {
		return installations.get(index);
	}

	public int getCurrentTick () {
		return tick;
	}

	public int getEndTick () {
		return endTick;
	}

	public ORNG getOrng() {
		return orng;
	}
	
	public Simulation(String aresources_path, String adbname, int seed) {
		
		resources_path = aresources_path;
		dbname = adbname;
		
		if(seed > 0) {
			orng = new ORNG(seed);
		} else {
			orng = new ORNG();
		}
		
		if (useDerby)
			m = new DerbyResults(dbname);
		else
			m = new MongoResults(dbname);
		m.createIndexes();
  		
	}
	
	private int getInstId(int maxInsts, HashMap<String,Double> instGen) {
		if(maxInsts == 1) {
			return 1;
		} else {
			double prob = orng.nextFloat();
			Set<String> keys = instGen.keySet();
			double sum = 0;
			int counter = 1;
			for(String s : keys) {
				sum += instGen.get(s).doubleValue();
				if(prob < sum) {
					break;
				}
				counter++;
			}
			return counter;
		}
	}

  	public SimulationParams getSimulationWorld () {
  		return simulationWorld;
  	}

	public void runStandAlone () {
  		try {
//  			DBObject objRun = DBConn.getConn().getCollection(MongoRuns.COL_RUNS).findOne(query);
  			System.out.println("Run " + dbname + " started @ " + Calendar.getInstance().getTimeInMillis());
  			calculateExpectedPower(dbname);
//  			System.out.println("EP calculated");
  			long startTime = System.currentTimeMillis();
  			int mccount = 0;
  			double mcrunsRatio = 1.0/(double)mcruns;
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
		  					DerbyResults.COL_AGGRRESULTS);
		  			tick++;
		  			if(tick % Constants.MIN_IN_HOUR == 0) {
		  				m.addAggregatedTickResult((tick/Constants.MIN_IN_HOUR), 
		  						(avgPPowerPerHour/Constants.MIN_IN_HOUR) * mcrunsRatio, 
		  						(avgQPowerPerHour/Constants.MIN_IN_HOUR) * mcrunsRatio, 
		  						DerbyResults.COL_AGGRRESULTS_HOURLY);
		  				m.addAggregatedTickResult((tick/Constants.MIN_IN_HOUR), 
		  						(avgPPowerPerHour) * mcrunsRatio, 
		  						(avgQPowerPerHour) * mcrunsRatio, 
		  						DerbyResults.COL_AGGRRESULTS_HOURLY_EN);
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
  	  			m.addKPIs(DerbyResults.AGGR, 
  	  					maxPower * mcrunsRatio, 
  	  					avgPower * mcrunsRatio, 
  	  					energy * mcrunsRatio, 
  	  					cost * mcrunsRatio,
  	  					energy * co2 * mcrunsRatio);
  	  			if(i+1 != mcruns) setupStandalone(true);
  	  			
  			}
  			// Write installation results to csv file
  			String filename = resources_path + "/csvs/" + dbname + ".csv";
  			System.out.println(filename);
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
	  							DerbyResults.COL_INSTRESULTS);
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
  	  							MongoResults.COL_INSTRESULTS);
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
  			// zip file
  			// http://www.mkyong.com/java/how-to-compress-files-in-zip-format/
  			System.out.println("Zipping...");
  			byte[] buffer = new byte[1024];
  			FileOutputStream fos = new FileOutputStream(filename + ".zip");
  			ZipOutputStream zos = new ZipOutputStream(fos);
  			ZipEntry ze= new ZipEntry(dbname + ".csv");
  			zos.putNextEntry(ze);
    		FileInputStream in = new FileInputStream(filename);
    		int len;
    		while ((len = in.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
    		in.close();
    		zos.closeEntry();
    		//remember close it
    		zos.close();
    		fos.close();
  			csvFile.delete();
  			// End of zip file
  			System.out.println("End of Zipping...");
	  		long endTime = System.currentTimeMillis();
//	  		objRun.put("ended", endTime);
	  		System.out.println("Updating DB...");
//	  		DBConn.getConn().getCollection(MongoRuns.COL_RUNS).update(query, objRun);
	  		System.out.println("End of Updating DB...");
	  		System.out.println("Time elapsed for Run " + dbname + ": " + ((endTime - startTime)/(1000.0 * 60)) + " mins");
	  		System.out.println("Run " + dbname + " ended @ " + Calendar.getInstance().toString());
  		} catch(Exception e) {
  			e.printStackTrace();
  			System.out.println(Utils.stackTraceToString(e.getStackTrace()));
  			// Change the run object in the db to reflect the exception
//  			if(objRun != null) {
//  				objRun.put("percentage", -1);
//  				objRun.put("state", e.getMessage());
//  				DBConn.getConn().getCollection(MongoRuns.COL_RUNS).update(query, objRun);
//  			}
  		}
  	}
	
	private double totalInstCost() {

		double cost = 0;
		for(Installation installation: installations) {
			cost += installation.getCost();
		}
		return cost;
	}
	
	public void setupStandalone(boolean jump) throws Exception {
  		
  		System.out.println("Simulation setup started");
  		
  		installations = setupScenario();
  		endTick = Constants.MIN_IN_DAY * numOfDays;
  		
  		// Check type of setup
  		if(setup.equalsIgnoreCase("static")) {
  			staticSetupStandalone(installations);
  		} else if(setup.equalsIgnoreCase("dynamic")) {
  			dynamicSetupStandalone(null);
  		} else {
  			throw new Exception("Problem with setup property!!!");
  		}
  		
  		System.out.println("Simulation setup finished");
  	}
	
	public Vector<Installation> setupScenario()
	{
  	    String scenarioName = "Scenario1";
  		String responseType = "None"; 		// "None", "Optimal", "Normal", "Discrete", "Daily"
  	    String locationInfo ="Katerini";
  	    int numOfDays = 3; 						// duration
  	    int startDateDay = 7;
	    int startDateMonth = 4;
	    int startDateYear = 2014;
	    SimulationParams simParams = new SimulationParams(responseType, scenarioName, locationInfo, numOfDays, startDateDay,  startDateMonth, startDateYear);
	    
	    
//  		String pricingType = "ScalarEnergyPricing"; 			// TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
//  		int billingCycle = 120;  					// all cases
//  		double fixedCharge = 15;				// all cases
//  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);
//		double[] prices = {0.06, 0.07, 0.07, 0.10};		
//		double[] levels = {500, 400, 400, 0};				
//		builderPP.scalarEnergyPricing(prices, levels);
//		PricingPolicy pricPolicy = builderPP.build();
		
 		String pricingType = "AllInclusivePricing"; 			// TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
  		int billingCycle = 120;  					// all cases
  		double fixedCharge = 15;				// all cases
  		PricingPolicy.Builder builderPP = new PricingPolicy.Builder(pricingType, fixedCharge, billingCycle);			
		builderPP.allInclusivePricing(100, 50, 100);
		PricingPolicy pricPolicy = builderPP.build();
		
		String pricingTypeB = "None"; 		// TOUPricing, ScalarEnergyPricing, ScalarEnergyPricingTimeZones, EnergyPowerPricing, MaximumPowerPricing, AllInclusivePricing, None (default)
		PricingPolicy pricPolicyB = new PricingPolicy();
		
		
	    int mcruns = 5;
  		double co2 = 2; 
  		String setup = "static"; 							// static, dynamic

  		
  		Vector<Installation> installations = new Vector<Installation>();
  		
  		
//	    	String clustername = (String)instDoc.get("cluster");
//	    	PricingPolicy instPricing = pricing;
//	    	PricingPolicy instBaseline_pricing = baseline_pricing;
//	    	if(jsonScenario.get("pricing-" + clustername + "-" + id) != null) {
//	    		DBObject pricingDoc = (DBObject) jsonScenario.get("pricing-" + clustername + "-" + id);
//	    		instPricing = new PricingPolicy(pricingDoc);
//	    	}	
//	    	if(jsonScenario.get("baseline_pricing-" + clustername + "-" + id) != null) {
//	    		DBObject basePricingDoc = (DBObject) jsonScenario.get("baseline_pricing-" + clustername + "-" + id);
//	    		instBaseline_pricing = new PricingPolicy(basePricingDoc);
//	    	}	    	
//  		Installation inst = new Installation.Builder(id, name, description, type, clustername, instPricing, instBaseline_pricing).build();
//  		
//	    	// Thermal module if exists
//	    	DBObject thermalDoc = (DBObject)instDoc.get("thermal");
//	    	if(thermalDoc != null && inst.getPricing().getType().equalsIgnoreCase("TOUPricing")) {
//	    		ThermalModule tm = new ThermalModule(thermalDoc, inst.getPricing().getTOUArray());
//	    		inst.setThermalModule(tm);
//	    	}
  		
  		
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
		
//		applName ="Lighting Lighting 0";
//		appliID = "appl2";
//		applDescription = "Description of Lighting Lighting 0";
//		applType = "Lighting";
//		applStandByCons = 0;
//		applIsBase = false;
//		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForLighting("p");
//		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForLighting("q");
//		Appliance app2 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
//				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(getOrng());
//		existing.put(appliID, app2);
//		inst.addAppliance(app2);
		
//		applName ="Cleaning Vacuum Cleaner 0";
//		appliID = "appl3";
//		applDescription = "Description of Cleaning Vacuum Cleaner 0";
//		applType = "Cleaning";
//		applStandByCons = 0;
//		applIsBase = false;
//		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner1("p");
//		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner1("q");
//		Appliance app3 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
//				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(sim.getOrng());
//		existing.put(appliID, app3);
//		inst.addAppliance(app3);
//		
//		applName ="Cleaning Water Heater";
//		appliID = "appl4";
//		applDescription = "Description of Cleaning Water Heater";
//		applType = "Cleaning";
//		applStandByCons = 0;
//		applIsBase = false;
//		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForWaterHeater("p");
//		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForWaterHeater("q");
//		Appliance app4 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
//				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(sim.getOrng());
//		existing.put(appliID, app4);
//		inst.addAppliance(app4);
//
//		applName ="Cleaning Vacuum Cleaner 1";
//		appliID = "appl5";
//		applDescription = "Description of Cleaning Vacuum Cleaner 1";
//		applType = "Cleaning";
//		applStandByCons = 0;
//		applIsBase = false;
//		consModelsP = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner2("p");
//		consModelsQ = ConsumptionModelsLibrary.getConsumptionModelForVacuumCleaner2("q");
//		Appliance app5 = new Appliance.Builder(appliID,  applName, applDescription, applType, 
//				inst, consModelsP, consModelsQ, applStandByCons, applIsBase).build(sim.getOrng());
//		existing.put(appliID, app5);
//		inst.addAppliance(app5);
		
		
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
		
//		String[] containsAppliances = {"appl1", "appl3", "appl4", "appl5"};
		String[] containsAppliances = {"appl1"};
		// add appliances
		for(int m = 0; m < containsAppliances.length; m++) {
			String containAppId = containsAppliances[m];
			Appliance app  = existing.get(containAppId);
			act1.addAppliance(actmodDayType,app,1.0/containsAppliances.length);
		}
		
		person.addActivity(act1);
		
		
//		activityName = "Lighting";
//		activityID = "act2";
//		activityDesc = "Person Lighting Activity";
//		Activity.Builder actBuilder = new Activity.Builder(activityID, activityName, activityDesc, "", simParams);
//		
//		actmodDayType = "any";  //any | weekdays | weekends | working | nonworking | abbreviations of specific weekdays, i.e. [Mon, Tue, Sat] | specific days formated as 1/12, 31/10 
//		
//		durDist = new Gaussian(1, 1); 				
//		durDist.precompute(0, 1439, 1440);
//		actBuilder.duration(actmodDayType, durDist);
//		
//		startDist = new Histogram(DistributionsLibrary.getStartTimeHistForLighting());
//		actBuilder.startTime(actmodDayType, startDist);
//		
//		double[] v2 = {0.22222,0.33333,0.44444};
//		timesDist = new Histogram(v2);
//		actBuilder.times(actmodDayType, timesDist);
//		
//		shiftable = false;
//		actBuilder.shiftable(actmodDayType, shiftable);
//		
//		Activity act2 = actBuilder.build();
//		exclusive = true;
//		act2.addConfig(actmodDayType, exclusive);
//		
//		String[] containsAppliances2 = {"appl2"};
//		// add appliances
//		for(int m = 0; m < containsAppliances2.length; m++) {
//			String containAppId = containsAppliances2[m];
//			Appliance app  = existing.get(containAppId);
//			act2.addAppliance(actmodDayType,app,1.0/containsAppliances2.length);
//		}
//		
//		person.addActivity(act2);
		
		inst.addPerson(person);	
		installations.add(inst);
		
		this.simulationWorld = simParams;
  		this.mcruns = mcruns;
  		this.co2 = co2;
  		this.pricing = pricPolicy;
  		this.baseline_pricing = pricPolicyB;
  		this.numOfDays = numOfDays;
  		this.setup = setup;
  		
  		return installations;
	}
  	
  	public void staticSetupStandalone (Vector<Installation> installations) throws Exception {
	    int numOfInstallations = installations.size();
	    queue = new PriorityBlockingQueue<Event>(2 * numOfInstallations);
	    	this.installations = installations;
	}


  	
	private void calculateExpectedPower(String dbname) {
//		DBObject query = new BasicDBObject();
//		query.put("_id", new ObjectId(dbname));
//		DBObject objRun = DBConn.getConn().getCollection(MongoRuns.COL_RUNS).findOne(query);
  		System.out.println("Start exp power calc.");
//  		int percentage = 0;
  		double[] aggr_exp = new double[Constants.MIN_IN_DAY];
//  		int count = 0;
  		for(Installation installation: installations) {
  			double[] inst_exp = new double[Constants.MIN_IN_DAY];
  			Person person = installation.getPersons().get(0);
  			for(Activity activity: person.getActivities()) {
  				System.out.println("CEP: " + activity.getName());
  				double[] act_exp = activity.calcExpPower();
//  				NumberFormat nf = new DecimalFormat("0.#");
//  				for (double c : act_exp) {
//  					System.out.print(nf.format(c) + " ");
//  				}
//  				System.out.println(" ");
  				for(int i = 0; i < act_exp.length; i++) {
  	  				inst_exp[i] += act_exp[i];
  	  				m.addExpectedPowerTick(i, activity.getId(), act_exp[i], 0, MongoResults.COL_ACTRESULTS_EXP);
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
  				m.addExpectedPowerTick(i, installation.getId(), inst_exp[i], 0, MongoResults.COL_INSTRESULTS_EXP);
  			}
  			
//  			count++;
//  			percentage = (int)(0.25 * count * 100.0) / (installations.size());
//  			objRun.put("percentage", percentage);
//  			DBConn.getConn().getCollection(MongoRuns.COL_RUNS).update(query, objRun);
  		}
  		for(int i = 0; i < aggr_exp.length; i++) {
  			m.addExpectedPowerTick(i, "aggr", aggr_exp[i], 0, MongoResults.COL_AGGRRESULTS_EXP);
//				System.out.println(aggr_exp[i]);
			}
  		System.out.println("End exp power calc.");
  	}
	
//	private String addEntity(Entity e, boolean jump) {
//  		BasicDBObject obj = e.toDBObject();
//  		if(!jump) DBConn.getConn(dbname).getCollection(e.getCollection()).insert(obj);
//  		ObjectId objId = (ObjectId)obj.get("_id");
//  		return objId.toString();
//  	}



	public void dynamicSetupStandalone(DBObject jsonScenario) throws Exception {		
		
		int numOfInstallations = 10;																				// INPUT   <-----------------
		Vector<Installation> instTypes = setupScenario();												// INPUT   <-----------------
		instTypes.get(0).setName("Collection");
//		String scenario_id = this.simulationWorld.getName();
		// number of installation types defined (including collection)
		int maxInsts = instTypes.size();  					
		queue = new PriorityBlockingQueue<Event>(2 * numOfInstallations);
		HashMap<String,Double> instGen = new HashMap<String,Double>();				// INPUT   <-----------------
		instGen.put("inst1", 0.5);
		instGen.put("inst2", 0.5);
		HashMap<String,Double> applGen = new HashMap<String,Double>();				// INPUT   <-----------------
		applGen.put("appl1", 0.75);
		HashMap<String,Double> personGen = new HashMap<String,Double>();				// INPUT   <-----------------
		personGen.put("person1", 0.7);
		personGen.put("person2", 0.3);
		
		
		for (int i = 1; i <= numOfInstallations; i++) {
			Installation instDoc = instTypes.get(getInstId(maxInsts, instGen));
			String instName = instDoc.getName();
			System.out.println(instName);
			
			if (instName.equalsIgnoreCase("Collection")) {
				String id = instDoc.getId();
				String name = instDoc.getName() + i;
				String description = instDoc.getDescription();
				String type = instDoc.getType();
				Installation inst = new Installation.Builder(id, name, description, type, null, pricing, baseline_pricing).build();
//				inst.setParentId(scenario_id);
				String inst_id = id + i;
				inst.setId(inst_id);
				Vector<Appliance> apps = instDoc.getAppliances();
				int appcount = apps.size();  
				// Create the appliances
				HashMap<String,Appliance> existing = new HashMap<String,Appliance>();
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
					existing.put(appid, app);
				}

				Set<String> keys = existing.keySet();
				for(String key : keys) {
					Double prob = applGen.get(key);
					if(prob != null) {
						double probValue = prob.doubleValue();
						if(orng.nextDouble() < probValue) {
							Appliance selectedApp = existing.get(key);
							selectedApp.setParentId(inst.getId());
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
				HashMap<String,Person> existingPersons = new HashMap<String,Person>();
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
						HashMap<String, Vector<Appliance>> actModApps = act.getAppliances();
						HashMap<String, Boolean> shiftables = act.getShiftable();
						HashMap<String, Boolean> exclusives = act.getConfig();
						HashMap<String, ProbabilityDistribution> probStartTime = act.getProbStartTime();
						HashMap<String, ProbabilityDistribution> probDuration = act.getProbDuration();
						HashMap<String, ProbabilityDistribution> probeTimes = act.getnTimesGivenDay();
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
							for(int m = 0; m < appliances.size(); m++) {
								String containAppId = inst_id + "_" + appliances.get(m).getId();
								Appliance app = existing.get(containAppId);
								//act.addAppliance(actmodDayType,app,1.0/containsAppliances.size());
								act.addAppliance(key,app,1.0);
							}
						}
						person.addActivity(act);
					}
					existingPersons.put(personid, person);
				}

				double roulette = orng.nextDouble();
				double sum = 0;
				for( String entityId : personGen.keySet() ) {
					if(existingPersons.containsKey(entityId)) {
						double prob = personGen.get(entityId);
						sum += prob;
						if(roulette < sum) {
							Person selectedPerson = existingPersons.get(entityId);
							selectedPerson.setParentId(inst.getId());
							String person_id = inst_id + "_" + selectedPerson.getId();
							selectedPerson.setId(person_id);
							inst.addPerson(selectedPerson);
							Vector<Activity> activities = selectedPerson.getActivities();
							for(Activity a : activities) {
								a.setParentId(person_id);
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
				Installation inst = new Installation.Builder(
						id, name, description, type, "", instPricing, instBaseline_pricing).build();
//				inst.setParentId(scenario_id);
				String inst_id = id + i;
				inst.setId(inst_id);
				Vector<Appliance> apps = instDoc.getAppliances();
 				int appcount = apps.size();  
				// Create the appliances
				HashMap<String,Appliance> existing = new HashMap<String,Appliance>();
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
					app.setParentId(inst.getId());
					String app_id =  inst_id + "_" + appid;
					app.setId(app_id);
					existing.put(appid, app);
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
				person.setParentId(inst.getId());
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
					HashMap<String, Vector<Appliance>> actModApps = act.getAppliances();
					HashMap<String, Boolean> shiftables = act.getShiftable();
					HashMap<String, Boolean> exclusives = act.getConfig();
					HashMap<String, ProbabilityDistribution> probStartTime = act.getProbStartTime();
					HashMap<String, ProbabilityDistribution> probDuration = act.getProbDuration();
					HashMap<String, ProbabilityDistribution> probeTimes = act.getnTimesGivenDay();
					
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
						for(int l = 0; l < appliances.size(); l++) {
							String containAppId = inst_id + "_" + appliances.get(l).getId();
							Appliance app = existing.get(containAppId);
							act.addAppliance(key,app,1.0/appliances.size());
						}
					}
					person.addActivity(act);
					act.setParentId(person_id);
					String act_id = person_id + actid;
					act.setId(act_id);
				}
				installations.add(inst);
			}
		}

	}
	
	public ProbabilityDistribution copyProbabilityDistribution(ProbabilityDistribution source, String flag) throws Exception
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

	
  




}
