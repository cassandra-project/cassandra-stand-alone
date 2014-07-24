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
package eu.cassandra.sim.entities.installations;

import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import eu.cassandra.sim.Event;
import eu.cassandra.sim.PricingPolicy;
import eu.cassandra.sim.entities.Entity;
import eu.cassandra.sim.entities.appliances.Appliance;
import eu.cassandra.sim.entities.people.Activity;
import eu.cassandra.sim.entities.people.Person;
import eu.cassandra.sim.utilities.Constants;
import eu.cassandra.sim.utilities.DBResults;
import eu.cassandra.sim.utilities.ORNG;

/**
 * Class modeling an installation. 
 */
public class Installation extends Entity {
	
	/** The persons residing in the installation. */
	private Vector<Person> persons;
	
	/** The appliances in the installation. */
	private Vector<Appliance> appliances;
	
	/** The pricing policy. */
	private PricingPolicy pp;
	
	/** The basline pricing policy for demand-response scenarios. */
	private PricingPolicy bpp;
	
	/** The current active power. */
	private double currentPowerP;
	
	/** The current reactive power. */
	private double currentPowerQ;
	
	/** The maximum power. */
	private double maxPower = 0;
	
	/** The cycle's maximum power. */
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
	 * The Builder class for Installations. 
	 */
	public static class Builder {
		// Required variables
		private final String id;
		private final String name;
		private final String description;
		private final String type;
		// Optional or state related variables
		private Vector<Person> persons = new Vector<Person>();
		private Vector<Appliance> appliances = new Vector<Appliance>();
		private PricingPolicy pp;
		private PricingPolicy bpp;
		
		/**
		 * Instantiates a new builder, with required only parameters.
		 *
		 * @param aid the installation id
		 * @param aname the installation name
		 * @param adescription the installation description
		 * @param atype the installation type
		 * @param app the pricing policy 
		 * @param abpp the baseline pricing policy 
		 */
		public Builder(String aid, String aname, String adescription, String atype, PricingPolicy app, PricingPolicy abpp) {
			id = aid;
			name = aname;
			description = adescription;
			type = atype;
			pp = app;
			bpp = abpp;
		}

		/**
		 * Builds the installation.
		 * 
		 * @return the installation
		 */
		public Installation build() {
			return new Installation(this);
		}
	}

	/**
	 * Instantiates a new installation.
	 *
	 * @param builder the builder
	 */
	private Installation(Builder builder) {
		id = builder.id;
		name = builder.name;
		description = builder.description;
		type = builder.type;
		persons = builder.persons;
		appliances = builder.appliances;
		pp = builder.pp;
		bpp = builder.bpp;
	}

	/**
	 * Update daily schedule.
	 *
	 * @param tick the tick
	 * @param queue the queue
	 * @param responseType the response type
	 * @param orng the random number generator
	 */
	public void updateDailySchedule(int tick, PriorityBlockingQueue<Event> queue, String responseType, ORNG orng) {
		for(Person person : getPersons()) {
			person.updateDailySchedule(tick, queue, pp, bpp, responseType, orng);
		}
	}

	/**
	 * Update the maximum power.
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
	 * Updates the average power.
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
	 * @param tick the tick
	 */
	public void updateCost(int tick) {
		cost += pp.calculateCost(energy, previousEnergy, energyOffpeak, previousEnergyOffpeak, tick, cycleMaxPower);
		cycleMaxPower = 0;
		previousEnergy = energy;
		previousEnergyOffpeak = energyOffpeak;
		for(Appliance appliance : getAppliances()) {
			appliance.updateCost(pp, tick);
		}
	}

	/**
	 * Gets the pricing policy.
	 *
	 * @return the pricing policy
	 */
	public PricingPolicy getPricing() {
		return pp;
	}

	/**
	 * Adds the KPIs for all appliances in the installation.
	 *
	 * @param m the database wrapper
	 * @param mcrunsRatio the Monte-carlo runs ratio
	 * @param co2 the CO2 factor
	 */
	public void addAppliancesKPIs(DBResults m, double mcrunsRatio, double co2) {
		for(Appliance appliance : getAppliances()) {
			m.addAppKPIs(appliance.getId(), 
					appliance.getMaxPower() * mcrunsRatio, 
					appliance.getAvgPower() * mcrunsRatio, 
					appliance.getEnergy() * mcrunsRatio, 
					appliance.getCost() * mcrunsRatio,
					appliance.getEnergy() * co2 * mcrunsRatio);
		}
	}

	/**
	 * Adds the KPIs for all activities of all the persons in the installation.
	 *
	 * @param m the database wrapper
	 * @param mcrunsRatio the Monte-carlo runs ratio
	 * @param co2 the CO2 factor
	 */
	public void addActivitiesKPIs(DBResults m, double mcrunsRatio, double co2) {
		for(Person person : getPersons()) {
			for(Activity activity: person.getActivities()) {
				m.addActKPIs(activity.getId(), 
						activity.getMaxPower() * mcrunsRatio, 
						activity.getAvgPower() * mcrunsRatio, 
						activity.getEnergy() * mcrunsRatio, 
						activity.getCost() * mcrunsRatio,
						activity.getEnergy() * co2 * mcrunsRatio);
			}
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
	 * Next step.
	 *
	 * @param tick the tick
	 */
	public void nextStep(int tick) {
		updateRegistry(tick);
	}

	/**
	 * Update appliances and activities consumptions.
	 *
	 * @param tick the tick
	 * @param endTick the end tick
	 */
	public void updateAppliancesAndActivitiesConsumptions(int tick, int endTick) {
		for(Appliance appliance : getAppliances()) {
			double p = appliance.getPower(tick, "p");
			Activity act = appliance.getWhat();
			if(act != null) {
				act.updateMaxPower(p);
				act.updateAvgPower(p/endTick);
				if(pp.isOffpeak(tick)) {
					act.updateEnergyOffpeak(p);
				} else {
					act.updateEnergy(p);
				}
			}
			appliance.updateMaxPower(p);
			appliance.updateAvgPower(p/endTick);
			if(pp.isOffpeak(tick)) {
				appliance.updateEnergyOffpeak(p);
			} else {
				appliance.updateEnergy(p);
			}
		}
	}

	/**
	 * Update registry.
	 *
	 * @param tick the tick
	 */
	public void updateRegistry(int tick) {
		float p = 0f;
		float q = 0f;
		for(Appliance appliance : getAppliances()) {
			p += appliance.getPower(tick, "p");
			q += appliance.getPower(tick, "q");
		}
		currentPowerP = p;
		currentPowerQ = q;
	}

	/**
	 * Gets the current active power.
	 *
	 * @return the current active power
	 */
	public double getCurrentPowerP() {
		return currentPowerP;
	}

	/**
	 * Gets the current reactive power.
	 *
	 * @return the current reactive power
	 */
	public double getCurrentPowerQ() {
		return currentPowerQ;
	}

	/**
	 * Gets the persons residing in the installation.
	 *
	 * @return the persons residing in the installation
	 */
	public Vector<Person> getPersons() {
		return persons;    
	}

	/**
	 * Adds a person to the installation.
	 *
	 * @param person the person to be added
	 */
	public void addPerson (Person person) {
		persons.add(person);
	}

	/**
	 * Gets the appliances in the installation.
	 *
	 * @return the appliances in the installation
	 */
	public Vector<Appliance> getAppliances () {
		return appliances;
	}

	/**
	 * Adds an appliance to the installation.
	 *
	 * @param appliance the appliance to be added
	 */
	public void addAppliance (Appliance appliance) {
		appliances.add(appliance);
	}

	/**
	 * Check if an appliance exists and returns the corresponding Appliance object.
	 *
	 * @param name the appliance name
	 * @return the appliance
	 */
	public Appliance applianceExists (String name) {
		for (Appliance a: appliances) {
			if (a.getName().equalsIgnoreCase(name))
				return a;
		}
		return null;
	}


	/**
	 * Prints the entity tree of the installation (appliances, persons, activities, and activity models).
	 */
	public void printInstallationInfo()
	{
		System.out.println("Installation name: " + this.getName());
		System.out.println("... contains appliances:");
		for (Appliance app: this.getAppliances())
			System.out.println("\tAppliance name: " + app.getName());
		System.out.println("... contains persons:");
		for (Person p: this.getPersons())
		{
			System.out.println("\tPerson name: " + p.getName());
			System.out.println("\t... contains activities:");
			for (Activity a: p.getActivities())
			{
				System.out.println("\t\tActivity name: " + a.getName());
				System.out.println("\t\t... contains activity models for:");
				for (String key: a.getProbDuration().keySet())
				{
					System.out.print("\t\t\t " + key + ": ");
					for (Appliance temp: a.getAppliances().get(key))
						System.out.print(temp.getName() + ", \t");
					System.out.println();
				}
			}
		}
	}

}
