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
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
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
import eu.cassandra.sim.math.Histogram;
import eu.cassandra.sim.math.ProbabilityDistribution;
import eu.cassandra.sim.utilities.Constants;
import eu.cassandra.sim.utilities.ConsumptionModelsLibrary;
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

  	public SimulationParams getSimulationWorld () {
  		return simulationWorld;
  	}

	public void runStandAlone () {
  		try {
//  			DBObject objRun = DBConn.getConn().getCollection(MongoRuns.COL_RUNS).findOne(query);
  			System.out.println("Run " + dbname + " started @ " + Calendar.getInstance().getTimeInMillis());
  			calculateExpectedPower();
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
  	  			double cycleMaxPower = 0;
  	  			double avgPower = 0;
  	  			double energy = 0;
  	  			double energyOffpeak = 0;
  	  			double cost = 0;
  	  			double billingCycleEnergy = 0;
  	  			double billingCycleEnergyOffpeak = 0;
  	  			while (tick < endTick) {
  	  				// If it is the beginning of the day create the events
  	  				if (tick % Constants.MIN_IN_DAY == 0) {
//  	  				System.out.println("Day " + ((tick / Constants.MIN_IN_DAY) + 1));
  	  					for (Installation installation: installations) {
//  						System.out.println(installation.getName());
  	  						installation.updateDailySchedule(tick, queue, pricing, baseline_pricing, simulationWorld.getResponseType(), orng);
  	  						
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
		  				if(pricing.isOffpeak(tick)) {
		  					installation.updateEnergyOffpeak(p);
		  				} else {
		  					installation.updateEnergy(p);
		  				}
		  				installation.updateAppliancesAndActivitiesConsumptions(tick, endTick, pricing);
		  				m.addTickResultForInstallation(tick, 
		  						installation.getId(), 
		  						p * mcrunsRatio, 
		  						q * mcrunsRatio, 
		  						DerbyResults.COL_INSTRESULTS);
		  				sumP += p;
		  				sumQ += q;
		  				avgPPowerPerHour += p;
		  				avgQPowerPerHour += q;
		  				avgPPowerPerHourPerInst[counter] += p;
		  				avgQPowerPerHourPerInst[counter] += q;
		  				String name = installation.getName();
//		  				System.out.println("INFO: Tick: " + tick + " \t " + "Name: " + name + " \t " 
//		  		  				+ "Power: " + p);
		  				if((tick + 1) % (Constants.MIN_IN_DAY *  pricing.getBillingCycle()) == 0 || pricing.getType().equalsIgnoreCase("TOUPricing")) {
		  					installation.updateCost(pricing, tick);
		  				}
		  				counter++;
		  			}
		  			if(sumP > maxPower) maxPower = sumP;
		  			if(sumP > cycleMaxPower) cycleMaxPower = sumP;
		  			avgPower += sumP/endTick;
		  			if(pricing.isOffpeak(tick)) {
		  				energyOffpeak += (sumP/1000.0) * Constants.MINUTE_HOUR_RATIO;
		  			} else {
		  				energy += (sumP/1000.0) * Constants.MINUTE_HOUR_RATIO;
		  			}
		  			if((tick + 1) % (Constants.MIN_IN_DAY *  pricing.getBillingCycle()) == 0 || pricing.getType().equalsIgnoreCase("TOUPricing")) {
		  				cost += pricing.calculateCost(energy, 
		  						billingCycleEnergy, 
		  						energyOffpeak,
		  						billingCycleEnergyOffpeak,
		  						tick,
		  						cycleMaxPower);
		  				billingCycleEnergy = energy;
		  				billingCycleEnergyOffpeak = energyOffpeak;
		  				cycleMaxPower = 0;
		  			}
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
			  						DerbyResults.COL_INSTRESULTS_HOURLY);
			  				m.addTickResultForInstallation((tick/Constants.MIN_IN_HOUR), 
			  						installation.getId(),
			  						(avgPPowerPerHourPerInst[counter]) * mcrunsRatio, 
			  						(avgQPowerPerHourPerInst[counter]) * mcrunsRatio, 
			  						DerbyResults.COL_INSTRESULTS_HOURLY_EN);
			  				avgPPowerPerHourPerInst[counter] = 0;
			  				avgQPowerPerHourPerInst[counter] = 0;
			  				counter++;
			  			}
		  			}
		  			mccount++;
//		  			percentage = (int)(mccount * 100.0 / (mcruns * endTick));
//		  			objRun.put("percentage", percentage);
//		  	  		DBConn.getConn().getCollection(MongoRuns.COL_RUNS).update(query, objRun);
  	  			}
  	  			for(Installation installation: installations) {
  	  				installation.updateCost(pricing, tick); // update the rest of the energy
  	  				m.addKPIs(installation.getId(), 
  	  						installation.getMaxPower() * mcrunsRatio, 
  	  						installation.getAvgPower() * mcrunsRatio, 
  	  						installation.getEnergy() * mcrunsRatio, 
  	  						installation.getCost() * mcrunsRatio,
  	  						installation.getEnergy() * co2 * mcrunsRatio);
  	  				installation.addAppliancesKPIs(m, mcrunsRatio, co2);
  	  				installation.addActivitiesKPIs(m, mcrunsRatio, co2);
  	  			}
  	  			cost += pricing.calculateCost(energy, 
  	  					billingCycleEnergy,
  						energyOffpeak,
  						billingCycleEnergyOffpeak,
  						tick,
  						cycleMaxPower);
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
  				row += "," + installation.getId() + "_p";
  				row += "," + installation.getId() + "_q";
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

	public void setupStandalone(boolean jump) throws Exception {
  		
  		System.out.println("Simulation setup started");
  		
  		installations = setupScenario();
  		
  		endTick = Constants.MIN_IN_DAY * numOfDays;
  		
  		// Check type of setup
  		if(setup.equalsIgnoreCase("static")) {
  			staticSetupStandalone(installations);
  		} else if(setup.equalsIgnoreCase("dynamic")) {
  			System.err.println("Dynamic setup for scenarios not yet supported");
//  			dynamicSetup(jsonScenario, jump);
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
  		
  		
  		//Create the installation
		String instName = "Milioudis Base";			// installation names
		String instID= "inst1";								// installation ids
		String instDescription = "Milioudis Base";	// installation descriptions
		String instType = "lala1";							// installation types
		Installation inst = new Installation.Builder(instID, instName, instDescription, instDescription).build();
		
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


  	
	private void calculateExpectedPower() {
  		System.out.println("Start exp power calc.");
  		double[] aggr_exp = new double[Constants.MIN_IN_DAY];
  		for(Installation installation: installations) {
  			double[] inst_exp = new double[Constants.MIN_IN_DAY];
  			Person person = installation.getPersons().get(0);
  			for(Activity activity: person.getActivities()) {
  				double[] act_exp = activity.calcExpPower();
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
  		}
  		for(int i = 0; i < aggr_exp.length; i++) {
  			m.addExpectedPowerTick(i, "aggr", aggr_exp[i], 0, MongoResults.COL_AGGRRESULTS_EXP);
				System.out.println(aggr_exp[i]);
			}
  		System.out.println("End exp power calc.");
  	}
	
//	private String addEntity(Entity e, boolean jump) {
//  		BasicDBObject obj = e.toDBObject();
//  		if(!jump) DBConn.getConn(dbname).getCollection(e.getCollection()).insert(obj);
//  		ObjectId objId = (ObjectId)obj.get("_id");
//  		return objId.toString();
//  	}
//
//
//
//	public void dynamicSetupStandalone(DBObject jsonScenario, boolean jump) throws Exception {
//  		DBObject scenario = (DBObject)jsonScenario.get("scenario");
//  		String scenario_id =  ((ObjectId)scenario.get("_id")).toString();
//  		DBObject demog = (DBObject)jsonScenario.get("demog");
//  		BasicDBList generators = (BasicDBList) demog.get("generators");
//  		// Initialize simulation variables
//  		int numOfInstallations = Utils.getInt(demog.get("numberOfEntities"));
//  		//System.out.println(numOfInstallations+"");
//  		queue = new PriorityBlockingQueue<Event>(2 * numOfInstallations);
//  		for (int i = 1; i <= numOfInstallations; i++) {
//	    	DBObject instDoc = (DBObject)jsonScenario.get("inst"+1);
//	    	String id = i+"";
//	    	String name = ((String)instDoc.get("name")) + i;
//	    	String description = (String)instDoc.get("description");
//	    	String type = (String)instDoc.get("type");
//	    	Installation inst = new Installation.Builder(
//	    			id, name, description, type).build();
//	    	inst.setParentId(scenario_id);
//	    	String inst_id = addEntity(inst, jump);
//	    	inst.setId(inst_id);
//	    	int appcount = Utils.getInt(instDoc.get("appcount"));
//	    	// Create the appliances
//	    	HashMap<String,Appliance> existing = new HashMap<String,Appliance>();
//	    	for (int j = 1; j <= appcount; j++) {
//	    		DBObject applianceDoc = (DBObject)instDoc.get("app"+j);
//	    		String appid = ((ObjectId)applianceDoc.get("_id")).toString();
//	    		String appname = (String)applianceDoc.get("name");
//		    	String appdescription = (String)applianceDoc.get("description");
//		    	String apptype = (String)applianceDoc.get("type");
//		    	double standy = Utils.getDouble(applianceDoc.get("standy_consumption"));
//		    	boolean base = Utils.getBoolean(applianceDoc.get("base"));
//		    	DBObject consModDoc = (DBObject)applianceDoc.get("consmod");
//		    	ConsumptionModel pconsmod = new ConsumptionModel(consModDoc.get("pmodel").toString(), "p");
//		    	ConsumptionModel qconsmod = new ConsumptionModel(consModDoc.get("qmodel").toString(), "q");
//	    		Appliance app = new Appliance.Builder(
//	    				appid,
//	    				appname,
//	    				appdescription,
//	    				apptype, 
//	    				inst,
//	    				pconsmod,
//	    				qconsmod,
//	    				standy,
//	            		base).build(orng);
//	    		existing.put(appid, app);
//	    	}
//	    	
//	    	HashMap<String,Double> gens = new HashMap<String,Double>();
//	    	for(int k = 0; k < generators.size(); k++) {
//    			DBObject generator = (DBObject)generators.get(k);
//    			String entityId = (String)generator.get("entity_id");
//    			double prob = Utils.getDouble(generator.get("probability"));
//    			gens.put(entityId, new Double(prob));
//	    	}
//	    	
//	    	Set<String> keys = existing.keySet();
//	    	for(String key : keys) {
//	    		Double prob = gens.get(key);
//	    		if(prob != null) {
//	    			double probValue = prob.doubleValue();
//	    			if(orng.nextDouble() < probValue) {
//    					Appliance selectedApp = existing.get(key);
//    					selectedApp.setParentId(inst.getId());
//    			    	String app_id = addEntity(selectedApp, jump);
//    			    	selectedApp.setId(app_id);
//    			    	inst.addAppliance(selectedApp);
//    			    	ConsumptionModel cm = selectedApp.getPConsumptionModel();
//    			    	cm.setParentId(app_id);
//    			    	String cm_id = addEntity(cm, jump);
//    			    	cm.setId(cm_id);
//    				}
//	    		}
//	    	}
//
//	    	int personcount = Utils.getInt(instDoc.get("personcount"));
//	    	// Create the appliances
//	    	HashMap<String,Person> existingPersons = new HashMap<String,Person>();
//	    	for (int j = 1; j <= personcount; j++) {
//	    		DBObject personDoc = (DBObject)instDoc.get("person"+j);
//		    	String personid = ((ObjectId)personDoc.get("_id")).toString();
//	    		String personName = (String)personDoc.get("name");
//		    	String personDescription = (String)personDoc.get("description");
//		    	String personType = (String)personDoc.get("type");
//		    	double awareness = Utils.getDouble(personDoc.get("awareness"));
//		    	double sensitivity = Utils.getDouble(personDoc.get("sensitivity"));
//		    	Person person = new Person.Builder(
//		    	        		  personid,
//		    	        		  personName, 
//		    	        		  personDescription,
//		    	                  personType, inst, awareness, sensitivity).build();
//		    	int actcount = Utils.getInt(personDoc.get("activitycount"));
//		    	//System.out.println("Act-Count: " + actcount);
//		    	for (int k = 1; k <= actcount; k++) {
//		    		DBObject activityDoc = (DBObject)personDoc.get("activity"+k);
//		    		String activityName = (String)activityDoc.get("name");
//		    		String activityType = (String)activityDoc.get("type");
//		    		String actid = ((ObjectId)activityDoc.get("_id")).toString();
//		    		int actmodcount = Utils.getInt(activityDoc.get("actmodcount"));
//		    		Activity act = new Activity.Builder(actid, activityName, "", 
//		    				activityType, simulationWorld).build();
//		    		ProbabilityDistribution startDist;
//		    		ProbabilityDistribution durDist;
//		    		ProbabilityDistribution timesDist;
//		    		for (int l = 1; l <= actmodcount; l++) {
//		    			DBObject actmodDoc = (DBObject)activityDoc.get("actmod"+l);
//		    			act.addModels(actmodDoc);
//		    			String actmodName = (String)actmodDoc.get("name");
//		    			String actmodType = (String)actmodDoc.get("type");
//		    			String actmodDayType = (String)actmodDoc.get("day_type");
//		    			boolean shiftable = Utils.getBoolean(actmodDoc.get("shiftable"));
//		    			boolean exclusive = Utils.getEquality(actmodDoc.get("config"), "exclusive", true);
//		    			DBObject duration = (DBObject)actmodDoc.get("duration");
//		    			act.addDurations(duration);
//		    			durDist = json2dist(duration, "duration");
//		    			//System.out.println(durDist.getPrecomputedBin());
//		    			DBObject start = (DBObject)actmodDoc.get("start");
//		    			act.addStarts(start);
//		    			startDist = json2dist(start, "start");
//		    			//System.out.println(startDist.getPrecomputedBin());
//		    			DBObject rep = (DBObject)actmodDoc.get("repetitions");
//		    			act.addTimes(rep);
//		    			timesDist = json2dist(rep, "reps");
//		    			//System.out.println(timesDist.getPrecomputedBin());
//		    			act.addDuration(actmodDayType, durDist);
//		    			act.addStartTime(actmodDayType, startDist);
//		    			act.addTimes(actmodDayType, timesDist);
//		    			act.addShiftable(actmodDayType, shiftable);
//		    			act.addConfig(actmodDayType, exclusive);
//		    			// add appliances
//			    		BasicDBList containsAppliances = (BasicDBList)actmodDoc.get("containsAppliances");
//			    		for(int m = 0; m < containsAppliances.size(); m++) {
//			    			String containAppId = (String)containsAppliances.get(m);
//			    			Appliance app  = existing.get(containAppId);
//			    			//act.addAppliance(actmodDayType,app,1.0/containsAppliances.size());
//			    			act.addAppliance(actmodDayType,app,1.0);
//			    		}
//		    		}
//		    		person.addActivity(act);
//		    	}
//		    	existingPersons.put(personid, person);
//	    	}
//	    	
//	    	double roulette = orng.nextDouble();
//	    	double sum = 0;
//	    	for(int k = 0; k < generators.size(); k++) {
//	    		DBObject generator = (DBObject)generators.get(k);
//	    		String entityId = (String)generator.get("entity_id");
//	    		if(existingPersons.containsKey(entityId)) {
//	    			double prob = Utils.getDouble(generator.get("probability"));
//	    			sum += prob;
//	    			if(roulette < sum) {
//	    				Person selectedPerson = existingPersons.get(entityId);
//	    				selectedPerson.setParentId(inst.getId());
//    			    	String person_id = addEntity(selectedPerson, jump);
//    			    	selectedPerson.setId(person_id);
//	    				inst.addPerson(selectedPerson);
//	    				Vector<Activity> activities = selectedPerson.getActivities();
//	    				for(Activity a : activities) {
//	    					a.setParentId(person_id);
//	    					String act_id = addEntity(a, jump);
//	    					a.setId(act_id);
//	    					Vector<DBObject> models = a.getModels();
//	    					Vector<DBObject> starts = a.getStarts();
//	    					Vector<DBObject> durations = a.getDurations();
//	    					Vector<DBObject> times = a.getTimes();
//	    					for(int l = 0; l < models.size();  l++ ) {
//	    						DBObject m = models.get(l);
//	    						m.put("act_id", act_id);
//	    						if(!jump)DBConn.getConn(dbname).getCollection("act_models").insert(m);
//	    				  		ObjectId objId = (ObjectId)m.get("_id");
//	    				  		String actmod_id = objId.toString();
//	    				  		DBObject s = starts.get(l);
//	    				  		s.put("actmod_id", actmod_id);
//	    				  		if(!jump)DBConn.getConn(dbname).getCollection("distributions").insert(s);
//	    				  		DBObject d = durations.get(l);
//	    				  		d.put("actmod_id", actmod_id);
//	    				  		if(!jump)DBConn.getConn(dbname).getCollection("act_models").insert(d);
//	    				  		DBObject t = times.get(l);
//	    				  		t.put("actmod_id", actmod_id);
//	    				  		if(!jump)DBConn.getConn(dbname).getCollection("act_models").insert(t);
//	    					}
//	    				}
//	    				break;
//	    			}
//	    		}
//	    	}
//	    	
//	    	installations.add(inst);
//	    }
//  		
//  	}
  




}
