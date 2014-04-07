package eu.cassandra.sim.standalone;

import java.sql.ResultSet;

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
