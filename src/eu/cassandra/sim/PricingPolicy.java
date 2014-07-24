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

import java.util.ArrayList;
import java.util.Iterator;

import eu.cassandra.sim.utilities.Constants;

/**
 * A pricing policy to be used in the simulation.
 */
public class PricingPolicy {

	/** The type of the pricing policy. */
	private String type;

	/** The fixed charge for every billing cycle. */
	private double fixedCharge;

	/** The billing cycle duration in days. */
	private int billingCycle;

	/** The contracted power capacity. */
	private double contractedCapacity;

	/** The contracted energy. */
	private double contractedEnergy;

	/** The price of energy consumed. */
	private double energyPricing;

	/** The power pricing of the contracted capacity. */
	private double powerPricing;

	/** The price of contracted energy. */
	private double fixedCost;

	/** The price of additional energy. */
	private double additionalCost;

	/** The price during offpeak hours. */
	private double offpeakPrice;

	/** The pricing levels (level-price pairs). */
	private ArrayList<Level> levels;

	/** The offpeak periods (from-to pairs). */
	private ArrayList<Offpeak> offpeaks;

	/** The pricing periods (from-to-price tripplets). */
	private ArrayList<Period> periods;

	/**
	 * Instantiates a new pricing policy.
	 */
	public PricingPolicy() {
		billingCycle = 1;
		fixedCharge = 0;
		type = "NoPricing";
	}

	/**
	 * The Builder class for Pricing Policies. 
	 */
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
		private double offpeakPrice;
		private ArrayList<Level> levels;
		private ArrayList<Offpeak> offpeaks;
		private ArrayList<Period> periods;

		/**
		 * Instantiates a new builder, with required only parameters.
		 *
		 * @param atype the type of the pricing policy
		 * @param afixedCharge the fixed charge for every billing cycle
		 * @param abillingCycle the billing cycle duration in days
		 */
		public Builder(String atype, double afixedCharge, int abillingCycle) {
			type = atype;
			billingCycle = abillingCycle;
			fixedCharge = afixedCharge;
			contractedCapacity = 0;
			contractedEnergy = 0;
			energyPricing = 0;	
			powerPricing = 0;
			fixedCost = 0;
			additionalCost = 0;
			offpeakPrice = 0;
			levels = new ArrayList<Level>();
			periods = new ArrayList<Period>();
			offpeaks = new ArrayList<Offpeak>();
		}	

		/**
		 * Sets additional parameters for an "Energy Power Pricing" policy.
		 *
		 * @param acontractedCapacity the contracted power capacity
		 * @param aenergyPricing the price of energy consumed
		 * @param apowerPricing the power pricing of the contracted capacity
		 * @return the builder
		 */
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

		/**
		 * Sets additional parameters for an "All Inclusive Pricing" policy.
		 *
		 * @param afixedCost the afixed cost
		 * @param aadditionalCost the aadditional cost
		 * @param acontractedEnergy the acontracted energy
		 * @return the builder
		 */
		public Builder allInclusivePricing(double afixedCost, double aadditionalCost, double acontractedEnergy) {
			if (!type.equals("AllInclusivePricing"))
			{
				System.err.println("PricingPolicy type mismatch: method only applicable to the AllInclusivePricing scheme");
				return this;
			}
			contractedEnergy = acontractedEnergy;
			fixedCost = afixedCost;
			additionalCost = aadditionalCost;
			return this;
		}

		/**
		 * Sets additional parameters for an "TOU (Time Of Use) Pricing" policy.
		 *
		 * @param froms the froms
		 * @param tos the tos
		 * @param prices the prices
		 * @return the builder
		 */
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

		/**
		 * Sets additional parameters for an "Scalar Energy Pricing" policy.
		 *
		 * @param prices the prices
		 * @param alevels the alevels
		 * @return the builder
		 */
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

		/**
		 * Sets additional parameters for an "Scalar Energy Pricing Timezones" policy.
		 *
		 * @param aoffpeakPrice the aoffpeak price
		 * @param prices the prices
		 * @param alevels the alevels
		 * @param froms the froms
		 * @param tos the tos
		 * @return the builder
		 */
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

		/**
		 * Builds the Pricing Policy.
		 *
		 * @return the pricing policy
		 */
		public PricingPolicy build () {
			return new PricingPolicy(this);
		}
	}

	/**
	 * Instantiates a new pricing policy.
	 *
	 * @param builder the builder
	 */
	private PricingPolicy (Builder builder) {
		type = builder.type;
		billingCycle = builder.billingCycle;
		fixedCharge = builder.fixedCharge;
		contractedCapacity = builder.contractedCapacity;
		contractedEnergy = builder.contractedEnergy;
		energyPricing = builder.energyPricing;
		powerPricing = builder.powerPricing;
		fixedCost = builder.fixedCost;
		additionalCost = builder.additionalCost;
		offpeakPrice = builder.offpeakPrice;
		levels = builder.levels;
		periods = builder.periods;
		offpeaks = builder.offpeaks;
	}


	/**
	 * Gets the billing cycle duration in days.
	 *
	 * @return the billing cycle duration in days
	 */
	public int getBillingCycle() {
		return billingCycle;
	}

	/**
	 * Gets the fixed charge for every billing cycle.
	 *
	 * @return the fixed charge for every billing cycle
	 */
	public double getFixedCharge() {
		return fixedCharge;
	}

	/**
	 * Gets the type of the pricing policy.
	 *
	 * @return the type of the pricing policy
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the TOU array.
	 *
	 * @return the TOU array
	 */
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

	/**
	 * Calculate cost.
	 *
	 * @param toEnergy the current energy
	 * @param fromEnergy the previous energy
	 * @param toEnergyOffpeak the current energy offpeak
	 * @param fromEnergyOffpeak the previous energy offpeak
	 * @param tick the current tick
	 * @param maxPower the maximum power
	 * @return the cost
	 */
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

	/**
	 * Checks if a tick is in an offpeak period or not.
	 *
	 * @param tick the tick to check
	 * @return true, if the tick is in an offpeak period
	 */
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
