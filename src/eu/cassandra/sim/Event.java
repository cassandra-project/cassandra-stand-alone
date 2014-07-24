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

import eu.cassandra.sim.entities.appliances.Appliance;
import eu.cassandra.sim.entities.people.Activity;

/**
 * A simulation event.
 */
public class Event implements Comparable<Event> {


	/** The event's hashcode. */
	public String hashcode;

	/** The Constant SWITCH_OFF. */
	public final static int SWITCH_OFF = 0;

	/** The Constant SWITCH_ON. */
	public final static int SWITCH_ON = 1; 

	/** The event's tick. */
	private int tick;

	/** The event's action. */
	private int action;

	/** The event's appliance. */
	private Appliance app;

	/** The event's activity. */
	private Activity act;

	/**
	 * Instantiates a new event.
	 *
	 * @param atick the event's tick
	 * @param aaction the event's action
	 * @param aapp the event's appliance
	 * @param ahashcode the event's hashcode
	 * @param aact the event's activity
	 */
	public Event(int atick, int aaction, Appliance aapp, String ahashcode, Activity aact) {
		tick = atick;
		action = aaction;
		app = aapp;
		hashcode = ahashcode;
		act = aact;
	}

	/**
	 * Gets the event's appliance.
	 *
	 * @return the event's appliance
	 */
	public Appliance getAppliance() {
		return app;
	}

	/**
	 * Gets the event's activity.
	 *
	 * @return the event's activity
	 */
	public Activity getActivity() {
		return act;
	}

	/**
	 * Gets the event's action.
	 *
	 * @return the event's action
	 */
	public int getAction() {
		return action;
	}

	/**
	 * Gets the event's tick.
	 *
	 * @return the event's tick
	 */
	public int getTick() {
		return tick;
	}

	/**
	 * Apply the event.
	 *
	 * @return true, if successful
	 */
	public boolean apply() {
		switch(action) {
		case SWITCH_ON:
			if(!app.isInUse()) {
				app.turnOn(tick, hashcode, act);
				return true;
			} else {
				System.out.println("WARNING: Tried to switch on appliance while on.");
				return false;
			}
		case SWITCH_OFF:
			//				System.out.println(app.getId() + " " + app.getName() + " " + app.getWho());
			if(app.isInUse() && app.getWho().equalsIgnoreCase(hashcode)) {
				app.turnOff();
				return true;
			} else if(!app.getWho().equalsIgnoreCase(hashcode)){
				System.out.println("WARNING: Someone else tried to switch off " +
						"appliance while off.");
				return false;
			}
		default:
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Event o) {
		if(tick < o.getTick()) {
			return -1;
		} else if(tick > o.getTick()) {
			return 1;
		} else {
			return 0;
		}
	}

}
