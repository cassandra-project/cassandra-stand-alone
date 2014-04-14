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
	
	public void createIndexes();
	
	public void addKPIs(String inst_id, double maxPower, double avgPower, double energy, double cost, double co2);
	
	public void addAppKPIs(String app_id, double maxPower, double avgPower, double energy, double cost, double co2);
	
	public void addActKPIs(String act_id, double maxPower, double avgPower, double energy, double cost, double co2);
	
	public void addTickResultForInstallation(int tick, String inst_id, double p, double q, String tableName);
	
	public void addExpectedPowerTick(int tick, String id, double p, double q, String tableName);
	
//	public Object getTickResultForInstallation(int tick, String inst_id, String tableName);
	
	public void addAggregatedTickResult(int tick, double p, double q, String tableName);
	
}
