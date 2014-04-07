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

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import eu.cassandra.sim.entities.appliances.Appliance;
import eu.cassandra.sim.entities.people.Activity;
import eu.cassandra.sim.entities.people.Activity.Builder;
import eu.cassandra.sim.math.ProbabilityDistribution;
import eu.cassandra.sim.utilities.Constants;

public class PricingPolicy {
		
	private String type;
	
	private double fixedCharge;
	
	private int billingCycle;
	
	private double contractedCapacity;
	
	private double contractedEnergy;
	
	private double energyPricing;
	
	private double powerPricing;
	
	private double fixedCost;
	
	private double additionalCost;
	
	private double maximumPower;
	
	private double offpeakPrice;
	
	private ArrayList<Level> levels;
	
	private ArrayList<Offpeak> offpeaks;
	
	private ArrayList<Period> periods;
	
	public PricingPolicy() {
		billingCycle = 1;
		fixedCharge = 0;
		type = "NoPricing";
	}
	
	public PricingPolicy(DBObject dbo) throws ParseException {
		type = dbo.get("type").toString();
		switch(type) {
			case "TOUPricing":
				billingCycle = Integer.parseInt(dbo.get("billingCycle").toString());
				fixedCharge = Double.parseDouble(dbo.get("fixedCharge").toString());
				// Parse levels
				BasicDBList periodsObj = (BasicDBList)dbo.get("timezones");
				DBObject periodObj;
				periods = new ArrayList<Period>();
				for(int i = 0; i < periodsObj.size(); i++) {
					periodObj =  (DBObject)periodsObj.get(i);
					String from = periodObj.get("starttime").toString();
					String to = periodObj.get("endtime").toString();
					double price = Double.parseDouble(periodObj.get("price").toString());
					Period p = new Period(from, to, price);
					periods.add(p);
				}
				break;
			case "ScalarEnergyPricing":
				billingCycle = Integer.parseInt(dbo.get("billingCycle").toString());
				fixedCharge = Double.parseDouble(dbo.get("fixedCharge").toString());
				// Parse levels
				BasicDBList levelsObj = (BasicDBList)dbo.get("levels");
				DBObject levelObj;
				levels = new ArrayList<Level>();
				for(int i = 0; i < levelsObj.size(); i++) {
					levelObj =  (DBObject)levelsObj.get(i);
					double price = Double.parseDouble(levelObj.get("price").toString());
					double level = Double.parseDouble(levelObj.get("level").toString());
					Level l = new Level(price, level);
					levels.add(l);
				}
				break;
			case "ScalarEnergyPricingTimeZones":
				billingCycle = Integer.parseInt(dbo.get("billingCycle").toString());
				fixedCharge = Double.parseDouble(dbo.get("fixedCharge").toString());
				offpeakPrice = Double.parseDouble(dbo.get("offpeakPrice").toString());
				// Parse levels
				BasicDBList levelsObj2 = (BasicDBList)dbo.get("levels");
				DBObject levelObj2;
				levels = new ArrayList<Level>();
				for(int i = 0; i < levelsObj2.size(); i++) {
					levelObj2 =  (DBObject)levelsObj2.get(i);
					double price = Double.parseDouble(levelObj2.get("price").toString());
					double level = Double.parseDouble(levelObj2.get("level").toString());
					Level l = new Level(price, level);
					levels.add(l);
				}
				// Parse timezones
				BasicDBList tzs = (BasicDBList)dbo.get("offpeak");
				DBObject tz;
				offpeaks = new ArrayList<Offpeak>();
				for(int i = 0; i < tzs.size(); i++) {
					tz =  (DBObject)tzs.get(i);
					String from = tz.get("from").toString();
					String to = tz.get("to").toString();
					Offpeak o = new Offpeak(from, to);
					offpeaks.add(o);
				}
				break;
			case "EnergyPowerPricing":
				billingCycle = Integer.parseInt(dbo.get("billingCycle").toString());
				fixedCharge = Double.parseDouble(dbo.get("fixedCharge").toString());
				contractedCapacity = Integer.parseInt(dbo.get("contractedCapacity").toString());
				energyPricing = Double.parseDouble(dbo.get("energyPrice").toString());
				powerPricing = Double.parseDouble(dbo.get("powerPrice").toString());
				break;
			case "MaximumPowerPricing":
				billingCycle = Integer.parseInt(dbo.get("billingCycle").toString());
				fixedCharge = Double.parseDouble(dbo.get("fixedCharge").toString());
				maximumPower = Double.parseDouble(dbo.get("maximumPower").toString());
				energyPricing = Double.parseDouble(dbo.get("energyPrice").toString());
				powerPricing = Double.parseDouble(dbo.get("powerPrice").toString());
				break;
			case "AllInclusivePricing":
				billingCycle = Integer.parseInt(dbo.get("billingCycle").toString());
				fixedCharge = Double.parseDouble(dbo.get("fixedCharge").toString());
				fixedCost = Integer.parseInt(dbo.get("fixedCost").toString());
				additionalCost = Double.parseDouble(dbo.get("additionalCost").toString());
				contractedEnergy = Double.parseDouble(dbo.get("contractedEnergy").toString());
			default:
				break;
		}
	}
	
	
	public static class Builder {
		// Required parameters
		private final String type;
		private final double fixedCharge;
		private final int billingCycle;
		// Optional parameters: different per pricing type
		private double contractedCapacity;	
		private double contractedEnergy;	
		private double energyPricing;	
		private double powerPricing;
		private double fixedCost;
		private double additionalCost;
		private double maximumPower;
		private double offpeakPrice;
		private ArrayList<Level> levels;
		private ArrayList<Offpeak> offpeaks;
		private ArrayList<Period> periods;
		
		public Builder(String atype, double afixedCharge, int abillingCycle) {
			type = atype;
			billingCycle = abillingCycle;
			fixedCharge = afixedCharge;
			contractedCapacity = Double.MIN_VALUE;
			contractedEnergy = Double.MIN_VALUE;
			energyPricing = Double.MIN_VALUE;	
			powerPricing = Double.MIN_VALUE;
			fixedCost = Double.MIN_VALUE;
			additionalCost = Double.MIN_VALUE;
			maximumPower = Double.MIN_VALUE;
			offpeakPrice = Double.MIN_VALUE;
			levels = new ArrayList<Level>();
			periods = new ArrayList<Period>();
			offpeaks = new ArrayList<Offpeak>();
		}	

		public Builder energyPowerPricing(double acontractedCapacity, double aenergyPricing, double apowerPricing) {
			if (!type.equals("EnergyPowerPricing"))
			{
				System.err.println("PricingPolicy type mismatch: method only applicable to the EnergyPowerPricing scheme");
				return this;
			}
			contractedCapacity = acontractedCapacity;
			energyPricing = aenergyPricing;
			powerPricing = apowerPricing;
			return this;
		}	
		
		public Builder maximumPowerPricing(double aenergyPricing, double apowerPricing, double amaximumPower) {
			if (!type.equals("MaximumPowerPricing"))
			{
				System.err.println("PricingPolicy type mismatch: method only applicable to the MaximumPowerPricing scheme");
				return this;
			}
			energyPricing = aenergyPricing;
			powerPricing = apowerPricing;
			maximumPower = amaximumPower;
			return this;	
		}

		public Builder allInclusivePricing(double afixedCost, double aadditionalCost, double acontractedEnergy) {
			if (!type.equals("AllInclusivePricing"))
			{
				System.err.println("PricingPolicy type mismatch: method only applicable to the AllInclusivePricing scheme");
				return this;
			}
			contractedEnergy = acontractedEnergy;
			fixedCost = afixedCost;
			return this;
		}

		public Builder touPricing(String[] froms, String[] tos, double[] prices) {
			if (!type.equals("TOUPricing"))
			{
				System.err.println("PricingPolicy type mismatch: method only applicable to the TOUPricing scheme");
				return this;
			}
			if (! (froms.length == tos.length && tos.length == prices.length))
			{
				System.err.println("PricingPolicy initialization error: all input tables must have the same length");
				return this;
			}
			
			for(int i = 0; i < froms.length; i++) {
				String from = froms[i];
				String to = tos[i];
				double price = prices[i];
				Period p = new Period(from, to, price);
				periods.add(p);
			}
			return this;
		}
		
		public Builder scalarEnergyPricing(double[] prices, double[] alevels) {
			if (!type.equals("ScalarEnergyPricing"))
			{
				System.err.println("PricingPolicy type mismatch: method only applicable to the ScalarEnergyPricing scheme");
				return this;
			}
			if (! (alevels.length == prices.length))
			{
				System.err.println("PricingPolicy initialization error: all input tables must have the same length");
				return this;
			}
			
			for(int i = 0; i < prices.length; i++) {
				double price = prices[i];
				double level = alevels[i];
				Level l = new Level(price, level);
				levels.add(l);
			}
			return this;
		}
		
		public Builder scalarEnergyPricingTimeZones(double aoffpeakPrice, double[] prices, double[] alevels, String[] froms, String[] tos) {
			if (!type.equals("ScalarEnergyPricingTimeZones"))
			{
				System.err.println("PricingPolicy type mismatch: method only applicable to the ScalarEnergyPricingTimeZones scheme");
				return this;
			}
			if (! (alevels.length == prices.length && froms.length == tos.length))
			{
				System.err.println("PricingPolicy initialization error: all input tables must have the same length");
				return this;
			}
			offpeakPrice = aoffpeakPrice;
			
			for(int i = 0; i < prices.length; i++) {
				double price = prices[i];
				double level = alevels[i];
				Level l = new Level(price, level);
				levels.add(l);
			}
		
			for(int i = 0; i < froms.length; i++) {
				String from = froms[i];
				String to = tos[i];
				Offpeak o = new Offpeak(from, to);
				offpeaks.add(o);
			}
			
			return this;
		}
		
		public PricingPolicy build () {
			return new PricingPolicy(this);
		}
	}
	
	private PricingPolicy (Builder builder) {
		type = builder.type;
		billingCycle = builder.billingCycle;
		fixedCharge = builder.fixedCharge;
		contractedCapacity = builder.contractedCapacity;
		contractedEnergy = builder.contractedEnergy;
		energyPricing = builder.energyPricing;
		powerPricing = builder.powerPricing;
		fixedCost = builder.powerPricing;
		additionalCost = builder.additionalCost;
		maximumPower = builder.maximumPower;
		offpeakPrice = builder.offpeakPrice;
		levels = builder.levels;
		periods = builder.periods;
		offpeaks = builder.offpeaks;
	}
	
	
	
	public int getBillingCycle() {
		return billingCycle;
	}
	
	public double getFixedCharge() {
		return fixedCharge;
	}
	
	public String getType() {
		return type;
	}
	
	public double calcOneKw24() {
		double cost = 0;
		if(type.equalsIgnoreCase("TOUPricing")) {
			Iterator<Period> iter = periods.iterator();
			while(iter.hasNext()) {
				Period p = iter.next();
				if(p.getTo().equalsIgnoreCase("00:00")) p.setTo("24:00");
				String[] fromTokens = p.getFrom().split(":");
				String[] toTokens = p.getTo().split(":");
				int from = Integer.parseInt(fromTokens[0]) * 60 + Integer.parseInt(fromTokens[1]);
				int to = Integer.parseInt(toTokens[0]) * 60 + Integer.parseInt(toTokens[1]);
				cost += Math.max(0, to-from) * p.getPrice() * Constants.MINUTE_HOUR_RATIO;
			}
		}
		return cost;
	}
	
	public double[] getTOUArray() {
		if(type.equalsIgnoreCase("TOUPricing")) {
			double[] arr = new double[Constants.MIN_IN_DAY];
			Iterator<Period> iter = periods.iterator();
			while(iter.hasNext()) {
				Period p = iter.next();
				if(p.getTo().equalsIgnoreCase("00:00")) p.setTo("24:00");
				String[] fromTokens = p.getFrom().split(":");
				String[] toTokens = p.getTo().split(":");
				int from = Integer.parseInt(fromTokens[0]) * 60 + Integer.parseInt(fromTokens[1]);
				int to = Integer.parseInt(toTokens[0]) * 60 + Integer.parseInt(toTokens[1]);
				for(int i = from; i < to; i++) {
					arr[i] = p.getPrice();
				}
			}
			return arr;
		} else {
			return null;
		}
	}
	
	public double calculateCost(double toEnergy, double fromEnergy,
			double toEnergyOffpeak, double fromEnergyOffpeak, int tick,
			double maxPower) {
		double remainingEnergy = toEnergy - fromEnergy;
		double cost = 0;
		switch(type) {
			case "TOUPricing":
				int minutesInDay = tick % Constants.MIN_IN_DAY;
				Iterator<Period> iter = periods.iterator();
				while(iter.hasNext()) {
					Period p = iter.next();
					if(p.getTo().equalsIgnoreCase("00:00")) p.setTo("24:00");
					String[] fromTokens = p.getFrom().split(":");
					String[] toTokens = p.getTo().split(":");
					int from = Integer.parseInt(fromTokens[0]) * 60 + Integer.parseInt(fromTokens[1]);
					int to = Integer.parseInt(toTokens[0]) * 60 + Integer.parseInt(toTokens[1]);
					if(minutesInDay >= from && minutesInDay < to) {
						cost += (toEnergy - fromEnergy) * p.getPrice();
					}
				}
				break;
			case "ScalarEnergyPricing":
				cost += fixedCharge;
				for(int i = levels.size()-1; i >= 0; i--) {
					double level = levels.get(i).getLevel();
					double price = levels.get(i).getPrice();
					if(remainingEnergy < level) {
						cost += remainingEnergy * price;
						break;
					} else if(!(level > 0)) {
						cost += remainingEnergy * price;
						break;
					} else {
						remainingEnergy -= level;
						cost += level * price;
					}
				}
				break;
			case "ScalarEnergyPricingTimeZones":
				cost += fixedCharge;
				for(int i = levels.size()-1; i >= 0; i--) {
					double level = levels.get(i).getLevel();
					double price = levels.get(i).getPrice();
					if(remainingEnergy < level) {
						cost += remainingEnergy * price;
						break;
					} else if(!(level > 0)) {
						cost += remainingEnergy * price;
						break;
					} else {
						remainingEnergy -= level;
						cost += level * price;
					}
				}
				double remainingEnergyOffpeak = toEnergyOffpeak - fromEnergyOffpeak;
				cost += remainingEnergyOffpeak * offpeakPrice;
				break;
			case "EnergyPowerPricing":
				cost += fixedCharge;
				cost += remainingEnergy * energyPricing;
				cost += contractedCapacity * powerPricing;
				break;
			case "MaximumPowerPricing":
				cost += fixedCharge;
				cost += remainingEnergy * energyPricing;
				cost += maxPower * powerPricing;
				break;
			case "AllInclusivePricing":
				cost += fixedCharge;
				cost += fixedCost;
				cost += Math.max((remainingEnergy-contractedEnergy),0) * additionalCost;
				break;
			case "NoPricing" :
				break;
			default:
				break;
		}
		return cost;
	}
	
	public boolean isOffpeak(int tick) {
		if(type.equalsIgnoreCase("ScalarEnergyPricingTimeZones")) {
			int minutesInDay = tick % Constants.MIN_IN_DAY;
			Iterator<Offpeak> iter = offpeaks.iterator();
			while(iter.hasNext()) {
				Offpeak o = iter.next();
				String[] fromTokens = o.getFrom().split(":");
				String[] toTokens = o.getTo().split(":");
				int from = Integer.parseInt(fromTokens[0]) * 60 + Integer.parseInt(fromTokens[1]);
				int to = Integer.parseInt(toTokens[0]) * 60 + Integer.parseInt(toTokens[1]);
				if(minutesInDay >= from && minutesInDay < to) {
					return true;
				}
			}
		}
		return false;
	}

}
