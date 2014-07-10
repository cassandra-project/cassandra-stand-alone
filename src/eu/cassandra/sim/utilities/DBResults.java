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

/**
 * 
 * 
 * @author Fani A. Tzima (fani [at] iti [dot] gr)
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
	
	public void createIndexes();
	
	public void addKPIs(String inst_id, double maxPower, double avgPower, double energy, double cost, double co2);
	
	public void addAppKPIs(String app_id, double maxPower, double avgPower, double energy, double cost, double co2);
	
	public void addActKPIs(String act_id, double maxPower, double avgPower, double energy, double cost, double co2);
	
	public void addTickResultForInstallation(int tick, String inst_id, double p, double q, String tableName);
	
	public void addExpectedPowerTick(int tick, String id, double p, double q, String tableName);
	
//	public Object getTickResultForInstallation(int tick, String inst_id, String tableName);
	
	public void addAggregatedTickResult(int tick, double p, double q, String tableName);
	
}
