package eu.cassandra.sim.utilities;

import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class MongoResults implements DBResults{	
	
	private String dbname;
	
	public MongoResults(String adbname) {
 		dbname = adbname;
	}
	
	@Override
	public void createIndexes() {
		DBObject index = new BasicDBObject("tick", 1);
		DBConn.getConn(dbname).getCollection(COL_AGGRRESULTS).createIndex(index);
		index = new BasicDBObject("tick", 1);
		DBConn.getConn(dbname).getCollection(COL_AGGRRESULTS_HOURLY).createIndex(index);
		index = new BasicDBObject("inst_id", 1);
		index.put("tick", 1);
		DBConn.getConn(dbname).getCollection(COL_INSTRESULTS).createIndex(index);
		index = new BasicDBObject("inst_id", 1);
		index.put("tick", 1);
		DBConn.getConn(dbname).getCollection(COL_INSTRESULTS_HOURLY).createIndex(index);
	}
	
	/**
	 * @param inst_id
	 * @param maxPower
	 * @param avgPower
	 * @param energy
	 */
	@Override
	public void addKPIs(String inst_id, double maxPower, double avgPower, double energy, double cost, double co2) {
		boolean first = false;
		DBObject query = new BasicDBObject();
		String collection;
		String tick_collection;
		String id;
		DBObject order = new BasicDBObject();
		order.put("p", -1);
		if(inst_id.equalsIgnoreCase(AGGR)) {
			id = AGGR;
			collection = COL_AGGRKPIS;
			tick_collection = COL_AGGRRESULTS;
		} else {
			id = inst_id;
			collection = COL_INSTKPIS;
			tick_collection = COL_INSTRESULTS;
		}
		query.put("inst_id", id);
		DBObject data = DBConn.getConn(dbname).getCollection(collection).findOne(query);
		double newMaxPower = maxPower;
		double newAvgPower = avgPower;
		double newEnergy = energy;
		double newCost = cost;
		double newCo2 = co2;
		if(data == null) {
			data = new BasicDBObject();
			first = true;
			data.put("inst_id", id);
		} else {
			newMaxPower += ((Double)data.get("maxPower")).doubleValue();
			newAvgPower += ((Double)data.get("avgPower")).doubleValue();
			newEnergy += ((Double)data.get("energy")).doubleValue();
			newCost += ((Double)data.get("cost")).doubleValue();
			newCo2 += ((Double)data.get("co2")).doubleValue();
		}
		DBObject maxavg = null;
		if(inst_id.equalsIgnoreCase(AGGR)) {
			maxavg = DBConn.getConn(dbname).getCollection(tick_collection).find().sort(order).limit(1).next();
		} else {
			DBObject query2 = new BasicDBObject();
			query2.put("inst_id", inst_id);
			maxavg = DBConn.getConn(dbname).getCollection(tick_collection).find(query2).sort(order).limit(1).next();
		}
		double maxavgValue = newAvgPower;
		if(maxavg != null) maxavgValue = ((Double)maxavg.get("p")).doubleValue();
		data.put("avgPeak", maxavgValue);
		data.put("maxPower", newMaxPower);
		data.put("avgPower", newAvgPower);
		data.put("energy", newEnergy);
		data.put("cost", newCost);
		data.put("co2", newCo2);
		if(first) {
			DBConn.getConn(dbname).getCollection(collection).insert(data);
		} else {
			DBConn.getConn(dbname).getCollection(collection).update(query, data, false, false);
		}
	}
	
	@Override
	public HashMap<String, Double> getKPIs(String inst_id) {
		HashMap<String, Double> temp =  new  HashMap<String, Double>();
		DBObject query = new BasicDBObject();
		String collection;
		String id;
		if(inst_id.equalsIgnoreCase(AGGR)) {
			id = AGGR;
			collection = COL_AGGRKPIS;
		} else {
			id = inst_id;
			collection = COL_INSTKPIS;
		}
		query.put("inst_id", id);
		DBObject data = DBConn.getConn(dbname).getCollection(collection).findOne(query);
		
		if(data == null) {
			System.err.println("No data found for installation " + inst_id);
		} else {
			temp.put("Avg Peak (W)", ((Double)data.get("avgPeak")).doubleValue());
			temp.put("Max Power (W)", ((Double)data.get("maxPower")).doubleValue());
			temp.put("Avg Power (W)", ((Double)data.get("avgPower")).doubleValue());
			temp.put("Energy (KWh)", ((Double)data.get("energy")).doubleValue());
			temp.put("Cost (EUR)", ((Double)data.get("cost")).doubleValue());
			temp.put("CO2", ((Double)data.get("co2")).doubleValue());
		}
		
		return temp;
	}

	@Override
	public void addAppKPIs(String app_id, double maxPower, double avgPower, double energy, double cost, double co2) {
		boolean first = false;
		DBObject query = new BasicDBObject();
		String collection;
		String id = app_id;
		collection = COL_APPKPIS;
		query.put("app_id", id);
		DBObject data = DBConn.getConn(dbname).getCollection(collection).findOne(query);
		double newMaxPower = maxPower;
		double newAvgPower = avgPower;
		double newEnergy = energy;
		double newCost = cost;
		double newCo2 = co2;
		if(data == null) {
			data = new BasicDBObject();
			first = true;
			data.put("app_id", id);
		} else {
			newMaxPower += ((Double)data.get("maxPower")).doubleValue();
			newAvgPower += ((Double)data.get("avgPower")).doubleValue();
			newEnergy += ((Double)data.get("energy")).doubleValue();
			newCost += ((Double)data.get("cost")).doubleValue();
			newCo2 += ((Double)data.get("co2")).doubleValue();
		}
		data.put("maxPower", newMaxPower);
		data.put("avgPower", newAvgPower);
		data.put("energy", newEnergy);
		data.put("cost", newCost);
		data.put("co2", newCo2);
		if(first) {
			DBConn.getConn(dbname).getCollection(collection).insert(data);
		} else {
			DBConn.getConn(dbname).getCollection(collection).update(query, data, false, false);
		}
	}
	
	@Override
	public HashMap<String, Double> getAppKPIs(String app_id) {
		HashMap<String, Double> temp =  new  HashMap<String, Double>();
		DBObject query = new BasicDBObject();
		String collection = DBResults.COL_APPKPIS;
		query.put("app_id", app_id);
		DBObject data = DBConn.getConn(dbname).getCollection(collection).findOne(query);
		
		if(data == null) {
			System.err.println("No data found for appliance " + app_id);
		} else {
			temp.put("Max Power (W)", ((Double)data.get("maxPower")).doubleValue());
			temp.put("Avg Power (W)", ((Double)data.get("avgPower")).doubleValue());
			temp.put("Energy (KWh)", ((Double)data.get("energy")).doubleValue());
			temp.put("Cost (EUR)", ((Double)data.get("cost")).doubleValue());
			temp.put("CO2", ((Double)data.get("co2")).doubleValue());
		}
		
		return temp;
	}
	
	@Override
	public void addActKPIs(String app_id, double maxPower, double avgPower, double energy, double cost, double co2) {
		boolean first = false;
		DBObject query = new BasicDBObject();
		String collection;
		String id = app_id;
		collection = COL_ACTKPIS;
		query.put("act_id", id);
		DBObject data = DBConn.getConn(dbname).getCollection(collection).findOne(query);
		double newMaxPower = maxPower;
		double newAvgPower = avgPower;
		double newEnergy = energy;
		double newCost = cost;
		double newCo2 = co2;
		if(data == null) {
			data = new BasicDBObject();
			first = true;
			data.put("act_id", id);
		} else {
			newMaxPower += ((Double)data.get("maxPower")).doubleValue();
			newAvgPower += ((Double)data.get("avgPower")).doubleValue();
			newEnergy += ((Double)data.get("energy")).doubleValue();
			newCost += ((Double)data.get("cost")).doubleValue();
			newCo2 += ((Double)data.get("co2")).doubleValue();
		}
		data.put("maxPower", newMaxPower);
		data.put("avgPower", newAvgPower);
		data.put("energy", newEnergy);
		data.put("cost", newCost);
		data.put("co2", newCo2);
		if(first) {
			DBConn.getConn(dbname).getCollection(collection).insert(data);
		} else {
			DBConn.getConn(dbname).getCollection(collection).update(query, data, false, false);
		}
	}

	@Override
	public HashMap<String, Double> getActKPIs(String act_id) {
		HashMap<String, Double> temp =  new  HashMap<String, Double>();
		DBObject query = new BasicDBObject();
		String collection = DBResults.COL_ACTKPIS;
		query.put("act_id", act_id);
		DBObject data = DBConn.getConn(dbname).getCollection(collection).findOne(query);
		
		if(data == null) {
			System.err.println("No data found for activity " + act_id);
		} else {
			temp.put("Max Power (W)", ((Double)data.get("maxPower")).doubleValue());
			temp.put("Avg Power (W)", ((Double)data.get("avgPower")).doubleValue());
			temp.put("Energy (KWh)", ((Double)data.get("energy")).doubleValue());
			temp.put("Cost (EUR)", ((Double)data.get("cost")).doubleValue());
			temp.put("CO2", ((Double)data.get("co2")).doubleValue());
		}
		
		return temp;
	}
	
	/**
	 * 
	 * @param tick
	 * @param inst_id
	 * @param p
	 * @param q
	 */
	@Override
	public void addTickResultForInstallation(int tick,
			String inst_id, double p, double q, String collection) {
		boolean first = false;
		DBObject query = new BasicDBObject();
		query.put("inst_id", inst_id);
		query.put("tick", tick);
		DBObject data = DBConn.getConn(dbname).getCollection(collection).findOne(query);
		double newp = p;
		double newq = q;
		if(data == null) {
			data = new BasicDBObject();
			first = true;
			data.put("inst_id",inst_id);
			data.put("tick",tick);
		} else {
			newp += ((Double)data.get("p")).doubleValue();
			newq += ((Double)data.get("q")).doubleValue();
		}
		data.put("p",newp);
		data.put("q",newq);
		if(first) {
			DBConn.getConn(dbname).getCollection(collection).insert(data);
		} else {
			DBConn.getConn(dbname).getCollection(collection).update(query, data, false, false);
		}
	}
	
	@Override
	public void addExpectedPowerTick(int tick, String id, double p, double q, String collection) {
		DBObject data = new BasicDBObject();
		data.put("id", id);
		data.put("tick", tick);
		data.put("p", p);
		data.put("q", q);
		DBConn.getConn(dbname).getCollection(collection).insert(data);
	}
	
	public DBObject getTickResultForInstallation(int tick,
			String inst_id, String collection) {
		DBObject query = new BasicDBObject();
		query.put("inst_id", inst_id);
		query.put("tick", tick);
		return DBConn.getConn(dbname).getCollection(collection).findOne(query);
	}
	
	public DBObject getExpectedPowerTickResultForInstallation(int tick,
			String inst_id, String collection) {
		DBObject query = new BasicDBObject();
		query.put("id", inst_id);
		query.put("tick", tick);
		return DBConn.getConn(dbname).getCollection(collection).findOne(query);
	}
	
	/**
	 * 
	 * @param tick
	 * @param p
	 * @param q
	 */
	@Override
	public void addAggregatedTickResult(int tick, double p, double q, String collection) {
		boolean first = false;
		DBObject query = new BasicDBObject();
		query.put("tick", tick);
		DBObject data = DBConn.getConn(dbname).getCollection(collection).findOne(query);
		double newp = p;
		double newq = q;
		if(data == null) {
			data = new BasicDBObject();
			first = true;
			data.put("tick",tick);
		} else {
			newp += ((Double)data.get("p")).doubleValue();
			newq += ((Double)data.get("q")).doubleValue();
			
		}
		data.put("p",newp);
		data.put("q",newq);
		if(first) {
			DBConn.getConn(dbname).getCollection(collection).insert(data);
		} else {
			DBConn.getConn(dbname).getCollection(collection).update(query, data, false, false);
		}
	}
	
}

