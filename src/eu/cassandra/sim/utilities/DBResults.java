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
package eu.cassandra.sim.utilities;

import java.util.HashMap;

/**
 * Interface defining the methods that database access classes should implement
 * 
 *  @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 * 
 */
public interface DBResults {
	
	public final static String COL_APPRESULTS = "app_results";
	public final static String COL_ACTRESULTS_EXP = "act_expected";
	public final static String COL_INSTRESULTS = "inst_results";
	public final static String COL_INSTRESULTS_EXP = "inst_expected";
	public final static String COL_INSTRESULTS_HOURLY = "inst_results_hourly";
	public final static String COL_INSTRESULTS_HOURLY_EN = "inst_results_hourly_energy";
	public final static String COL_AGGRRESULTS = "aggr_results";
	public final static String COL_AGGRRESULTS_EXP = "aggr_expected";
	public final static String COL_AGGRRESULTS_HOURLY = "aggr_results_hourly";
	public final static String COL_AGGRRESULTS_HOURLY_EN = "aggr_results_hourly_energy";
	public final static String COL_INSTKPIS = "inst_kpis";
	public final static String COL_APPKPIS = "app_kpis";
	public final static String COL_ACTKPIS = "act_kpis";
	public final static String COL_AGGRKPIS = "aggr_kpis";
	public final static String AGGR = "aggr";
	
	/**
	 * Creates the tables/collections to be used for storing simulation output values
	 * and, possible, the indexes required to boost performance.
	 */
	public void createTablesAndIndexes();
	
	/**
	 * Adds aggregate KPIs or KPIs for a specific installation.
	 *
	 * @param inst_id the installation id 
	 * @param maxPower the maximum power
	 * @param avgPower the average power
	 * @param energy the energy
	 * @param cost the cost
	 * @param co2 the CO2 factor
	 */
	public void addKPIs(String inst_id, double maxPower, double avgPower, double energy, double cost, double co2);
	
	/**
	 * Gets the aggregate KPIs or KPIs for a specific installation.
	 *
	 * @param inst_id the installation id 
	 * @return the KPIs
	 */
	public HashMap<String, Double> getKPIs(String inst_id);
	
	/**
	 * Adds KPIs for a specific appliance.
	 *
	 * @param app_id the appliance id
	 * @param maxPower the maximum power
	 * @param avgPower the average power
	 * @param energy the energy
	 * @param cost the cost
	 * @param co2 the CO2 factor
	 */
	public void addAppKPIs(String app_id, double maxPower, double avgPower, double energy, double cost, double co2);
	
	/**
	 * Gets the appliance KPIs.
	 *
	 * @param app_id the appliance id
	 * @return the appliance KPIs
	 */
	public HashMap<String, Double> getAppKPIs(String app_id);
	
	/**
	 * Adds KPIs for a specific activity.
	 *
	 * @param act_id the activity id
	 * @param maxPower the maximum power
	 * @param avgPower the average power
	 * @param energy the energy
	 * @param cost the cost
	 * @param co2 the CO2 factor
	 */
	public void addActKPIs(String act_id, double maxPower, double avgPower, double energy, double cost, double co2);
	
	/**
	 * Gets the activity KPIs.
	 *
	 * @param act_id the activity id
	 * @return the activity KPIs
	 */
	public HashMap<String, Double> getActKPIs(String act_id);
	
	/**
	 * Adds a tick result for a specific installation.
	 *
	 * @param tick the tick
	 * @param inst_id the installation id 
	 * @param p the active power value
	 * @param q the reactive power value
	 * @param tableName the name of the table/collection where the values are to be stored
	 */
	public void addTickResultForInstallation(int tick, String inst_id, double p, double q, String tableName);
	
	/**
	 * Adds an aggregate expected power tick or a power tick for a specific installation or activity
	 *
	 * @param tick the tick
	 * @param id the installation/activity id
	 * @param p the expected power value
	 * @param tableName the name of the table/collection where the values are to be stored
	 */
	public void addExpectedPowerTick(int tick, String id, double p, String tableName);
	
	/**
	 * Adds an aggregated tick result.
	 *
	 * @param tick the tick
	 * @param p the active power value
	 * @param q the reactive power value
	 * @param tableName the name of the table/collection where the values are to be stored
	 */
	public void addAggregatedTickResult(int tick, double p, double q, String tableName);
	
}
