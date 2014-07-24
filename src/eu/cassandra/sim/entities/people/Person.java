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
package eu.cassandra.sim.entities.people;

import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import eu.cassandra.sim.Event;
import eu.cassandra.sim.PricingPolicy;
import eu.cassandra.sim.entities.Entity;
import eu.cassandra.sim.entities.installations.Installation;
import eu.cassandra.sim.utilities.ORNG;

public class Person extends Entity {

	/** The installation the person resides in. */
	private final Installation house;
	
	/** The person's activities. */
	private Vector<Activity> activities;
	
	/** The person's awareness. */
	private double awareness;
	
	/** The person's sensitivity. */
	private double sensitivity;
	
	/**
	 * The Builder class for Persons. 
	 */
	public static class Builder
	{
		// Required parameters
		private final String id;
		private final String name;
		private final String description;
		private final String type;
		private final Installation house;
		private final double awareness;
		private final double sensitivity;
		// Optional parameters: not available
		private Vector<Activity> activities = new Vector<Activity>();

		/**
		 * Instantiates a new builder, with required only parameters.
		 *
		 * @param aid the person's id
		 * @param aname the person's name
		 * @param desc the person's description
		 * @param atype the person's type
		 * @param ahouse the person's installation 
		 * @param aawareness the person's awareness
		 * @param asensitivity the person's sensitivity
		 */
		public Builder (String aid, String aname, String desc, String atype, Installation ahouse,
				double aawareness, double asensitivity)
		{
			id = aid;
			name = aname;
			description = desc;
			type = atype;
			house = ahouse;
			awareness = aawareness;
			sensitivity = asensitivity;
		}
		
		/**
		 * Builds the person.
		 * 
		 * @return the person
		 */
		public Person build ()
		{
			return new Person(this);
		}
	}
	
	/**
	 * Instantiates a new person.
	 *
	 * @param builder the builder
	 */
	private Person (Builder builder)
	{
		id = builder.id;
		name = builder.name;
		description = builder.description;
		type = builder.type;
		awareness = builder.awareness;
		sensitivity = builder.sensitivity;

		house = builder.house;
		activities = builder.activities;
	}

	/**
	 * Adds an activity to the person.
	 *
	 * @param a the activity
	 */
	public void addActivity(Activity a) {
		activities.add(a);
	}

	/**
	 * Update daily schedule.
	 *
	 * @param tick the tick
	 * @param queue the queue
	 * @param pricing the pricing
	 * @param baseline the baseline
	 * @param responseType the response type
	 * @param orng the orng
	 */
	public void updateDailySchedule(int tick, PriorityBlockingQueue<Event> queue,
			PricingPolicy pricing, PricingPolicy baseline, String responseType, ORNG orng) {
		for(Activity activity: activities) {
			//    		System.out.println("Activity: " + activity.getName());
			activity.updateDailySchedule(tick, queue, pricing, baseline, awareness, sensitivity, responseType, orng);
		}
	}

	/**
	 * Gets the installation the person resides in.
	 *
	 * @return the installation
	 */
	public Installation getInstallation ()
	{
		return house;
	}

	/**
	 * Gets the person's activities.
	 *
	 * @return the person's activities
	 */
	public Vector<Activity> getActivities ()
	{
		return activities;
	}

	/**
	 * Gets the person's awareness.
	 *
	 * @return the person's awareness
	 */
	public double getAwareness() {
		return awareness;
	}

	/**
	 * Sets the person's awareness.
	 *
	 * @param awareness the new person's awareness
	 */
	public void setAwareness(double awareness) {
		this.awareness = awareness;
	}

	/**
	 * Gets the person's sensitivity.
	 *
	 * @return the person's sensitivity
	 */
	public double getSensitivity() {
		return sensitivity;
	}

	/**
	 * Sets the person's sensitivity.
	 *
	 * @param sensitivity the new person's sensitivity
	 */
	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity;
	}

}
