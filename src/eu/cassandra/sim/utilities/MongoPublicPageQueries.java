package eu.cassandra.sim.utilities;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

public class MongoPublicPageQueries {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	private static Calendar c1 = Calendar.getInstance();

	private static double getBarGraphValuesWithIndices(DBCollection collection, String aggregateBy, boolean resultsAreEnergy, String inst_id, Date[] dates, int startIndex, int numOfRecords)
	{
//		DBObject skip = new BasicDBObject("$skip", recordsToSkip);
//		DBObject limit = new BasicDBObject("$limit", N);
//		DBObject sort = new BasicDBObject("$sort", new BasicDBObject("tick", -1));
		
		BasicDBObject query = new BasicDBObject();
		query = new BasicDBObject("tick", new BasicDBObject("$gte", startIndex).append("$lt", startIndex+numOfRecords));	
		
		DBObject groupFields = new BasicDBObject( "_id", null);
		if (resultsAreEnergy)
			groupFields.put("aggrValue", new BasicDBObject( "$sum", "$p"));
		else	
			groupFields.put("aggrValue", new BasicDBObject( "$avg", "$p"));
		DBObject group = new BasicDBObject("$group", groupFields);
		
		List<DBObject> pipeline; 
		DBObject match;
		if (inst_id != null)
		{
			BasicDBObject andQuery = new BasicDBObject();
			List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
			obj.add(query);
			obj.add(new BasicDBObject("inst_id", inst_id));
			andQuery.put("$and", obj);
			match = new BasicDBObject("$match", andQuery);  
		}
		else
			match = new BasicDBObject("$match", query);  
		pipeline	 = Arrays.asList(match, group);
		AggregationOutput output = collection.aggregate(pipeline);
		double value = 0;
		for (DBObject result : output.results()) {
			value = Double.parseDouble(""+result.get("aggrValue"));
			if (resultsAreEnergy)
				value /= 60.0;
		}
		return value;
	}
	
	private static double[] getMostRecentBlockOfBarGraphValues(DBCollection collection, String aggregateBy, boolean resultsAreEnergy, String inst_id, Date[] dates)
	{
		double divideBy = 60;  	//  aggregateBy = "hour";
		double[] divideBys = null;
		switch(aggregateBy) {
		case "day":
			divideBy = 60 * 24;
			break;
		case "week":
			divideBy = 60 * 24 * 7;
			break;
		case "month":
			divideBy = 60 * 24 * 30;	//28, 30, 31 depending on the simulation dates!!!
			break;
		default:
			break;
		}
		
		int block_size = 24;
		switch(aggregateBy) {
		case "day":
			c1.setTime(dates[0]);
			block_size = c1.get(Calendar.DAY_OF_MONTH);	 // X, when on the X-th day of the month
			break;
		case "week":
			block_size = 12;
			break;
		case "month":
			block_size = 12;	
			break;
		default:
			break;
		}
		
		int numOfResults = 1; 
		if (aggregateBy != "month")
		{
			if (inst_id != null)
				numOfResults = (int) Math.ceil(collection.count(new BasicDBObject("inst_id", inst_id))/ divideBy);
			else
				numOfResults = (int) Math.ceil(collection.count() / divideBy);
		}
		else
		{
			if (dates[0].getYear() == dates[1].getYear())
				numOfResults = dates[1].getMonth() - dates[0].getMonth() +1;
			else
				numOfResults = (12 - dates[0].getMonth()) + dates[1].getMonth() + 1;
			divideBys = new double[numOfResults+1];
			divideBys[0] = 0;
			int year = dates[0].getYear();			
			int index = 1;
			for (int m=dates[0].getMonth(); ; m++)
			{
				Calendar mycal = new GregorianCalendar(year, m, 1);
				int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH); 
				divideBys[index]= 60 * 24 * daysInMonth;
				index++;
				if (m==11)
				{
					m=-1;
					year++;
				}
				if (m==dates[1].getMonth())
					break;
			}
		}
			
		double[] results = new double[block_size];
		int index = 0;
		for (int i=numOfResults<block_size?0:(numOfResults-block_size); i<numOfResults; i++)
		{
			int recordsToSkip = (int) (aggregateBy.equals("month") ? divideBys[i] : divideBy*i);
			int recordsToAggregate = (int)(aggregateBy.equals("month") ? divideBys[i+1] : divideBy);
//			long startTime = System.currentTimeMillis();
			results[index] = getBarGraphValuesWithIndices(collection, aggregateBy, resultsAreEnergy, inst_id, dates, recordsToSkip, recordsToAggregate);
//			long stopTime = System.currentTimeMillis();
//		    System.out.println(stopTime - startTime);
			index++;
		}
		return results;
	}
	
	private static Date[] getSimulationDates(DB db)
	{
		Date[] dates = new Date[2];
		DBCursor c = db.getCollection("sim_param").find();
		while (c.hasNext()) {
			DBObject sim_params = c.next();
			DBObject temp = (DBObject)sim_params.get("calendar");
			String year = ""+temp.get("year");
			String month = ""+temp.get("month");
			String dayOfMonth = ""+temp.get("dayOfMonth");
			
			int numOfSimDays = Integer.parseInt(""+sim_params.get("numberOfDays"));
			
			String dt = year + "/" + month + "/" + dayOfMonth;  // Start date
			try {
				c1.setTime(sdf.parse(dt));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			dates[0] = c1.getTime();
			c1.add(Calendar.DATE, numOfSimDays);  // number of days to add
			dates[1] = c1.getTime();
		}
		return dates;
	}

	private static double mean(double[] m) {
	    double sum = 0;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	    }
	    return sum / m.length;
	}
	
	private static double sum(double[] m) {
	    double sum = 0;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	    }
	    return sum;
	}
	
	public static void main(String[] args) {
		Mongo m;
		try {
			m = new Mongo();
			DB db = m.getDB("53b27869300460bd8c6825ea");
			DecimalFormat df = new DecimalFormat("#0.000"); 
			
			Date[] dates  = getSimulationDates(db);
			
			DBCollection collection = db.getCollection("aggr_results");
			String aggregateBy = "hour";						
			if ( !aggregateBy.equals("hour") && !aggregateBy.equals("day") && !aggregateBy.equals("week") && !aggregateBy.equals("month") )
			{
				System.err.println("ERROR: Possible values for the aggregation unit are: hour, day, week and month.");
				System.exit(1);
			}
			boolean resultsAreEnergy = false;
			
			String aggrValueName = "avgActivePower";
			if (resultsAreEnergy)
				aggrValueName = "sumEnergy";
			
			System.out.println("\"Block\" length is 24 when aggregating by hour, X when aggregating by day on the X-th day of the month, "
					+ "and 12 when aggregating by week or month.");
			System.out.println();
			
			int block_size = 24;
			switch(aggregateBy) {
			case "day":
				c1.setTime(dates[0]);
				block_size = c1.get(Calendar.DAY_OF_MONTH);	 // X, when on the X-th day of the month
				break;
			case "week":
				block_size = 12;
				break;
			case "month":
				block_size = 12;	
				break;
			default:
				break;
			}
			
//			System.out.println("Results per " + aggregateBy + " for all installations in the simulation (showing the most recent \"block\" of values - with size " + block_size + ")");		     
////		    long startTime = System.currentTimeMillis();
//			double[] results2 = getMostRecentBlockOfBarGraphValues(collection, aggregateBy, resultsAreEnergy, null, dates);
//			for (int i=0; i<results2.length; i++)
//				System.out.println(aggregateBy + ": " + i + "\t" + aggrValueName + ": " + results2[i]);
////			long stopTime = System.currentTimeMillis();
////		    System.out.println(stopTime - startTime);
//		     
//			System.out.println();
			
			DBCollection installations = db.getCollection("installations");
			List<ObjectId> inst_ids = installations.distinct("_id");
			collection = db.getCollection("inst_results");
			
			double maxInst = Double.MIN_VALUE;
			double minInst = Double.MAX_VALUE;
			double avgInst = 0;
			
			System.out.println("Results per " + aggregateBy + " and per installation (showing the most recent \"block\" of values - with size " + block_size + ")");
			for (ObjectId inst_id : inst_ids)
			{
				System.out.println("Installation: "+ inst_id);
				double[] resultsInst2 = getMostRecentBlockOfBarGraphValues(collection, aggregateBy, resultsAreEnergy, inst_id+"", dates);
				for (int i=0; i<resultsInst2.length; i++)
					System.out.println(aggregateBy + ": " + i + " \t" + aggrValueName + ": " + resultsInst2[i]);
				
				System.out.print("Installation: "+ inst_id + " \t" );
				double value = 0;
				if (!resultsAreEnergy)
					value = mean(resultsInst2);
				else
					value = sum(resultsInst2);
				if (value > maxInst)
					maxInst = value;
				if (value < minInst)
					minInst = value;
				avgInst += value;
				System.out.println(aggrValueName.substring(0, 3) +": \t" + value);
				
				
			    System.out.println();
			}
			avgInst /= (double)inst_ids.size();
			
			System.out.println();
			System.out.println("MAX: " + maxInst);
			System.out.println("MIN: " + minInst);
			System.out.println("AVG: " + avgInst);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	

}
