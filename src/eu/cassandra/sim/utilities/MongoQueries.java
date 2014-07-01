package eu.cassandra.sim.utilities;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoQueries {
	
	/** 	Returns energy percentages (based on the simulation experiment's KPI calculations) per appliance/activity type 
	 * 		and, if required, per installation. 
	 * @param collection	The collection, based on which the energy sums will be computed ("appliances" or "activities")
	 * @param inst_id		The id of the target installation. If null computations are about the whole simulation.
	 * @return
	 */
	private static HashMap<String, Double> getEnergyPercentagesPerType(DBCollection collection, String inst_id)
	{
		HashMap<String, Double> results = getEnergySumsPerType(collection, inst_id);
		double sum = 0;
		for (String key : results.keySet())
			sum += results.get(key);  
		for (String key : results.keySet())
			results.put(key, results.get(key)/sum*100);
		return results;
	}
	
	
	/** 	Returns energy sums (based on the simulation experiment's KPI calculations) per appliance/activity type 
	 * 		and, if required, per installation. 
	 * @param collection	The collection, based on which the energy sums will be computed ("appliances" or "activities")
	 * @param inst_id		The id of the target installation. If null computations are about the whole simulation.
	 * @return
	 */
	private static HashMap<String, Double> getEnergySumsPerType(DBCollection collection, String inst_id)
	{
		if (!collection.getName().equals("appliances") && ! collection.getName().equals("activities"))
		{
			System.err.println("Method only applicable to collections 'appliances' and 'activities'");
			System.exit(5);
		}
		
		HashMap<String, Double> results = new HashMap<String, Double>();
		
		List<String> applTypes = collection.distinct("type");
		
		for (String temp : applTypes)
		{
			String type = temp.equals("")?"other":temp;
			
			DBCursor c;
			
			
			if (inst_id != null)
			{
				BasicDBObject andQuery = new BasicDBObject();
				List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
				obj.add(new BasicDBObject("type", temp));
				if (collection.getName().equals("appliances"))
				{
					obj.add(new BasicDBObject("inst_id", inst_id));
					andQuery.put("$and", obj);
				}
				else 
				{
					BasicDBList persons = new BasicDBList();
					BasicDBObject query = new BasicDBObject("inst_id", inst_id);
					c = collection.getDB().getCollection("persons").find(query);
					while (c.hasNext()) {
						String tempS = ""+c.next().get("_id");
						persons.add(tempS);
					}
					BasicDBObject query2 = new BasicDBObject("pers_id", new BasicDBObject("$in", persons));
					obj.add(query2);
					andQuery.put("$and", obj);
				}
				c = collection.find(andQuery, new BasicDBObject("_id", 1));
			}
			else
			{
				BasicDBObject query = new BasicDBObject("type", temp);
				c = collection.find(query, new BasicDBObject("_id", 1));
			}
			
			BasicDBList list = new BasicDBList();
			while (c.hasNext()) {
				String tempS = ""+c.next().get("_id");
				list.add(tempS);
			}
			
			String kpis_coll_name = collection.getName().equals("appliances")?"app_kpis":"act_kpis";
			String id_name = collection.getName().equals("appliances")?"app_id":"act_id";
			DBCollection coll_kpis = collection.getDB().getCollection(kpis_coll_name);
			
			BasicDBObject query2 = new BasicDBObject(id_name, new BasicDBObject("$in", list));
			DBObject match = new BasicDBObject("$match", query2);  
			
			DBObject fields = new BasicDBObject("energy", 1);
			DBObject project = new BasicDBObject("$project", fields);
			
			DBObject groupFields = new BasicDBObject( "_id", null);
			groupFields.put("sumEnergy", new BasicDBObject( "$sum", "$energy"));
			DBObject group = new BasicDBObject("$group", groupFields);
							
			List<DBObject> pipeline = Arrays.asList(match, project, group);
			AggregationOutput output = coll_kpis.aggregate(pipeline);
			for (DBObject result : output.results()) {
				double sum = Double.parseDouble(""+result.get("sumEnergy"));
				results.put(type, sum);
			}
		}
		return results;
	}
	
	
	public static void main(String args[])
	{
		Mongo m;
		try {
			m = new Mongo();
			DB db = m.getDB("534d2fd530043658c9a0ee94");
			DecimalFormat df = new DecimalFormat("#0.000"); 
			
			System.out.println("Energy sum per appliance type");
			DBCollection appliances = db.getCollection("appliances");
			HashMap<String, Double> results = getEnergySumsPerType(appliances, null);
			for (String key : results.keySet())
			{
	            double value = Double.parseDouble(results.get(key).toString());  
	            System.out.println(key + ": \t" + df.format(value)); 
			}	 
			
			System.out.println();
			
			System.out.println("Energy sum per activity type");
			DBCollection activities = db.getCollection("activities");
			HashMap<String, Double> results2 = getEnergySumsPerType(activities, null);
			for (String key : results2.keySet())
			{
				double value = Double.parseDouble(results2.get(key).toString());  
	            System.out.println(key + ": \t" + df.format(value)); 
			}	
			
			System.out.println();
			
			DBCollection installations = db.getCollection("installations");
			List<ObjectId> inst_ids = installations.distinct("_id");
			
			for (ObjectId inst_id : inst_ids)
			{
				System.out.println("Energy sum per appliance type for installation " + inst_id);
				HashMap<String, Double> resultsPerInst = getEnergyPercentagesPerType(appliances, inst_id+"");
				for (String key : resultsPerInst.keySet())
				{
					double value = Double.parseDouble(resultsPerInst.get(key).toString());  
		            System.out.println(key + ": \t" + df.format(value) + "%");  
				}	 
				
				System.out.println();
				
				System.out.println("Energy sum per activity type for installation " + inst_id);
				HashMap<String, Double> resultsPerInst2 = getEnergyPercentagesPerType(activities, inst_id+"");
				for (String key : resultsPerInst2.keySet())
				{
					double value = Double.parseDouble(resultsPerInst2.get(key).toString());  
		            System.out.println(key + ": \t" + df.format(value) + "%"); 
				}	
				
				System.out.println();
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
