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
package eu.cassandra.sim.entities.appliances;

import eu.cassandra.sim.PricingPolicy;
import eu.cassandra.sim.entities.Entity;
import eu.cassandra.sim.entities.installations.Installation;
import eu.cassandra.sim.entities.people.Activity;
import eu.cassandra.sim.utilities.Constants;
import eu.cassandra.sim.utilities.ORNG;

/**
 * Class modeling an electric appliance. The appliance has a stand by
 * consumption otherwise there are a number of periods along with their
 * consumption rates.
 */
public class Appliance extends Entity {
	
	/** The active power consumption model. */
	private final ConsumptionModel pcm;
	
	/** The reactive power consumption model. */
	private final ConsumptionModel qcm;
	
	/** The stand by consumption. */
	private final double standByConsumption;
	
	/** Whether the appliance is a base load. */
	private final boolean base;
	
	/** The installation the appliance belongs to. */
	private final Installation installation;

	/** Whether the appliance is in use. */
	private boolean inUse;
	
	/** The on tick. */
	private long onTick;
	
	/** Who turned the appliance on. */
	private String who;
	
	/** The activity the appliance is associated with. */
	private Activity what;

	/** The maximum power. */
	private double maxPower = 0;
	
	/** The cycle's max power. */
	private double cycleMaxPower = 0;
	
	/** The average power. */
	private double avgPower = 0;
	
	/** The energy. */
	private double energy = 0;
	
	/** The previous energy. */
	private double previousEnergy = 0;
	
	/** The energy offpeak. */
	private double energyOffpeak = 0;
	
	/** The previous energy offpeak. */
	private double previousEnergyOffpeak = 0;
	
	/** The cost. */
	private double cost = 0;
	
	/**
	 * The Builder class for Appliances. 
	 */
	public static class Builder {
		// Required variables
		private final String id;
		private final String description;
		private final String type;
		private final String name;
		private final Installation installation;
		private final ConsumptionModel pcm;
		private final ConsumptionModel qcm;
		private final double standByConsumption;
		private final boolean base;
		// Optional or state related variables
		private long onTick = -1;
		private String who = null;
		
		/**
		 * Instantiates a new builder, with required only parameters.
		 *
		 * @param aid the appliance id
		 * @param aname the appliance name
		 * @param adesc the appliance description
		 * @param atype the appliance type
		 * @param ainstallation the installation the appliance belongs to
		 * @param apcm the  active power consumption model of the appliance
		 * @param aqcm the  reactive power consumption model of the appliance
		 * @param astandy the stand-by consumption  of the appliance
		 * @param abase whether the appliance is a base load
		 */
		public Builder(String aid, String aname, String adesc, String atype,
				Installation ainstallation, ConsumptionModel apcm,
				ConsumptionModel aqcm, double astandy, boolean abase) {
			id = aid;
			name = aname;
			description = adesc;
			type = atype;		
			installation = ainstallation;
			pcm = apcm;
			qcm = aqcm;
			standByConsumption = astandy;
			base = abase;
		}
		
		/**
		 * Builds the Appliance.
		 *
		 * @param orng the random number generator
		 * @return the appliance
		 */
		public Appliance build(ORNG orng) {
			return new Appliance(this, orng);
		}
	}

	/**
	 * Instantiates a new appliance.
	 *
	 * @param builder the builder
	 * @param orng the random number generator
	 */
	private Appliance(Builder builder, ORNG orng) {
		id = builder.id;
		name = builder.name;
		description = builder.description;
		type = builder.type;
		installation = builder.installation;
		standByConsumption = builder.standByConsumption;
		pcm = builder.pcm;
		qcm = builder.qcm;
		base = builder.base;
		inUse = (base) ? true : false;
		onTick = (base) ? -orng.nextInt(Constants.MIN_IN_DAY) : builder.onTick;
		who = builder.who;
	}

	/**
	 * Gets the installation the appliance belongs to.
	 *
	 * @return the installation the appliance belongs to
	 */
	public Installation getInstallation() {
		return installation;
	}

	/**
	 * Checks whether the appliance is in use.
	 *
	 * @return whether the appliance is in use
	 */
	public boolean isInUse() {
		return inUse;
	}

	/**
	 * Checks whether the appliance is a base load.
	 *
	 * @return whether the appliance is a base load
	 */
	public boolean isBase() {
		return base;
	}

	/**
	 * Gets the reactive power consumption model.
	 *
	 * @return the reactive power consumption model
	 */
	public ConsumptionModel getQConsumptionModel() {
		return qcm;
	}

	/**
	 * Gets the active power consumption model.
	 *
	 * @return the active power consumption model
	 */
	public ConsumptionModel getPConsumptionModel() {
		return pcm;
	}

	/**
	 * Gets the power.
	 *
	 * @param tick the tick
	 * @param type the type
	 * @return the power
	 */
	public double getPower(long tick, String type) {
		try {

			ConsumptionModel cm = null; 
			if(type == "p") {
				cm = pcm;
			} else {
				cm = qcm;
			}
			double power = 0;
			if(isInUse()) {
				long relativeTick = Math.abs(tick - onTick);
				// If the device has a limited operational duration
				long divTick = relativeTick / cm.getTotalDuration();
				if(divTick >= cm.getOuterN() && cm.getOuterN() > 0) {
					power = 0;
				} else {
					int sum = 0;
					long moduloTick = relativeTick % cm.getTotalDuration();
					int index1 = -1;
					for(int i = 0; i < cm.getPatternN(); i++) {
						sum += (cm.getN(i) * cm.getPatternDuration(i));
						long whichPattern = moduloTick / sum;
						if(whichPattern == 0) {
							index1 = i;
							break;
						}
					}
					sum = 0;
					long moduloTick2 = moduloTick % cm.getPatternDuration(index1);
					int index2 = -1;
					for(int j = 0; j < cm.getPattern(index1).size(); j++) {
						sum += cm.getPattern(index1).get(j).d;
						long whichPattern = moduloTick2 / sum;
						if(whichPattern == 0) {
							index2 = j;
							break;
						}
					}
					relativeTick++;		
					power = cm.getPattern(index1).get(index2).v; 
				}
			} else {
				power = standByConsumption;
				//			power = 0;
			}
			return power;
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Turns off the appliance.
	 */
	public void turnOff() {
		if(!base) {
			inUse = false;
			onTick = -1;
		}
	}

	/**
	 * Turn on the appliance.
	 *
	 * @param tick the tick
	 * @param awho the awho
	 * @param awhat the awhat
	 */
	public void turnOn(long tick, String awho, Activity awhat) {
		inUse = true;
		onTick = tick;
		who = awho;
		what = awhat;
	}

	/**
	 * Gets the on tick.
	 *
	 * @return the on tick
	 */
	public long getOnTick() {
		return onTick;
	}

	/**
	 * Gets who turned the appliance on.
	 *
	 * @return who turned the appliance on
	 */
	public String getWho() {
		return who;
	}

	/**
	 * Gets the activity the appliance is associated with.
	 *
	 * @return the activity the appliance is associated with
	 */
	public Activity getWhat() {
		return what;
	}

	/**
	 * Gets the active consumption.
	 *
	 * @return the active consumption
	 */
	public Double[] getActiveConsumption () {
		return pcm.getConsumption();
	}

	/**
	 * Checks whether the consumption is static.
	 *
	 * @return true, if the consumption is static 
	 */
	public boolean isStaticConsumption() {
		return pcm.checkStatic();
	}

	/**
	 * Update maximum power.
	 *
	 * @param power the power
	 */
	public void updateMaxPower(double power) {
		if(power > maxPower) maxPower = power;
		if(power > cycleMaxPower) cycleMaxPower = power;
	}

	/**
	 * Gets the maximum power.
	 *
	 * @return the maximum power
	 */
	public double getMaxPower() {
		return maxPower;
	}

	/**
	 * Update average power.
	 *
	 * @param powerFraction the power fraction
	 */
	public void updateAvgPower(double powerFraction) {
		avgPower += powerFraction;
	}

	/**
	 * Gets the average power.
	 *
	 * @return the average power
	 */
	public double getAvgPower() {
		return avgPower;
	}

	/**
	 * Update energy.
	 *
	 * @param power the power
	 */
	public void updateEnergy(double power) {
		energy += (power/1000.0) * Constants.MINUTE_HOUR_RATIO; 
	}

	/**
	 * Update energy offpeak.
	 *
	 * @param power the power
	 */
	public void updateEnergyOffpeak(double power) {
		energyOffpeak += (power/1000.0) * Constants.MINUTE_HOUR_RATIO; 
	}

	/**
	 * Update cost.
	 *
	 * @param pp the pp
	 * @param tick the tick
	 */
	public void updateCost(PricingPolicy pp, int tick) {
		cost += pp.calculateCost(energy, previousEnergy, energyOffpeak, previousEnergyOffpeak, tick, cycleMaxPower);
		cycleMaxPower = 0;
		previousEnergy = energy;
		previousEnergyOffpeak = energyOffpeak;
		if(what != null) {
			what.updateCost(pp, tick);
		}
	}

	/**
	 * Gets the energy.
	 *
	 * @return the energy
	 */
	public double getEnergy() {
		return energy;
	}

	/**
	 * Gets the cost.
	 *
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Gets the stand by consumption.
	 *
	 * @return the stand by consumption
	 */
	public double getStandByConsumption() {
		return standByConsumption;
	}

}
