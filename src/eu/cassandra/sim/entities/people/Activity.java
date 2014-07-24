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

import java.util.TreeMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import eu.cassandra.sim.Event;
import eu.cassandra.sim.PricingPolicy;
import eu.cassandra.sim.SimulationParams;
import eu.cassandra.sim.entities.Entity;
import eu.cassandra.sim.entities.appliances.Appliance;
import eu.cassandra.sim.math.ProbabilityDistribution;
import eu.cassandra.sim.math.response.Response;
import eu.cassandra.sim.utilities.Constants;
import eu.cassandra.sim.utilities.ORNG;
import eu.cassandra.sim.utilities.Utils;

public class Activity extends Entity {

	private final static String WEEKDAYS = "weekdays";
	private final static String WEEKENDS = "weekends";
	private final static String NONWORKING = "nonworking";
	private final static String WORKING = "working";
	private final static String ANY = "any";
	
	/** The start time probability distributions per activity model. */
	private final TreeMap<String, ProbabilityDistribution> probStartTime;
	
	/** The start time probability distributions per activity model, in response mode. */
	private final TreeMap<String, ProbabilityDistribution> responseprobStartTime;
	
	/** The duration probability distributions per activity model. */
	private final TreeMap<String, ProbabilityDistribution> probDuration;
	
	/** The repetitions probability distributions per activity model. */
	private final TreeMap<String, ProbabilityDistribution> nTimesGivenDay;
	
	/** The repetitions probability distributions per activity model, in response mode. */
	private final TreeMap<String, ProbabilityDistribution> responsenTimesGivenDay;

	/** Whether the activity is shiftable per activity model. */
	private final TreeMap<String, Boolean> shiftable;
	
	/** Whether the activity is exclusive per activity model. */
	private final TreeMap<String, Boolean> config;
	
	/** The appliances involved in the activity per activity model. */
	private TreeMap<String, Vector<Appliance>> appliances;
	
	/** The probabilities that the appliances are used per activity model.. */
	private TreeMap<String, Vector<Double>> probApplianceUsed;
	
	/** The simulation world. */
	private SimulationParams simulationWorld;

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
	 * The Builder class for Activities. 
	 */
	public static class Builder {
		// Required parameters
		private final String id;
		private final String name;
		private final String description;
		private final String type;
		private SimulationParams simulationWorld;
		// Optional or state related variables
		private final TreeMap<String, ProbabilityDistribution> nTimesGivenDay;
		private final TreeMap<String, ProbabilityDistribution> probStartTime;
		private final TreeMap<String, ProbabilityDistribution> probDuration;
		private final TreeMap<String, ProbabilityDistribution> responsenTimesGivenDay;
		private final TreeMap<String, ProbabilityDistribution> responseprobStartTime;
		private final TreeMap<String, Boolean> shiftable;
		private final TreeMap<String, Boolean> config;		
		private TreeMap<String, Vector<Appliance>> appliances;
		private TreeMap<String, Vector<Double>> probApplianceUsed;

		/**
		 * Instantiates a new builder, with required only parameters.
		 *
		 * @param aid the activity id
		 * @param aname the activity name
		 * @param adesc the activity description
		 * @param atype the activity type
		 * @param world the simulation world
		 */
		public Builder (String aid, String aname, String adesc, String atype, SimulationParams world) {
			id = aid;
			name = aname;
			description = adesc;
			type = atype;
			appliances = new TreeMap<String, Vector<Appliance>>();
			probApplianceUsed = new TreeMap<String, Vector<Double>>();
			nTimesGivenDay = new TreeMap<String, ProbabilityDistribution>();
			probStartTime = new TreeMap<String, ProbabilityDistribution>();
			probDuration = new TreeMap<String, ProbabilityDistribution>();
			responsenTimesGivenDay = new TreeMap<String, ProbabilityDistribution>();
			responseprobStartTime = new TreeMap<String, ProbabilityDistribution>();
			shiftable = new TreeMap<String, Boolean>();
			config = new TreeMap<String, Boolean>();
			simulationWorld = world;
		}

		/**
		 * Sets the start time distribution for a specific activity model.
		 *
		 * @param day the activity model
		 * @param probDist the probability distribution
		 * @return the builder
		 */
		public Builder startTime(String day, ProbabilityDistribution probDist) {
			probStartTime.put(day, probDist);
			return this;
		}

		/**
		 * Sets the shiftable property for a specific activity model.
		 *
		 * @param day the activity model
		 * @param value the value for the property
		 * @return the builder
		 */
		public Builder shiftable(String day, Boolean value) {
			shiftable.put(day, value);
			return this;
		}

		/**
		 * Sets the duration distribution for a specific activity model.
		 *
		 * @param day the activity model
		 * @param probDist the probability distribution
		 * @return the builder
		 */
		public Builder duration(String day, ProbabilityDistribution probDist) {
			probDuration.put(day, probDist);
			return this;
		}

		/**
		 * Sets the repetitions distribution for a specific activity model.
		 *
		 * @param day the activity model
		 * @param probDist the probability distribution
		 * @return the builder
		 */
		public Builder times(String day, ProbabilityDistribution probDist) {
			nTimesGivenDay.put(day, probDist);
			return this;
		}
		
		/**
		 * Builds the Activity.
		 *
		 * @return the activity
		 */
		public Activity build () {
			return new Activity(this);
		}
	}

	/**
	 * Instantiates a new activity.
	 *
	 * @param builder the builder
	 */
	private Activity (Builder builder) {
		id = builder.id;
		name = builder.name;
		description = builder.description;
		type = builder.type;
		appliances = builder.appliances;
		nTimesGivenDay = builder.nTimesGivenDay;
		probStartTime = builder.probStartTime;
		probDuration = builder.probDuration;
		responsenTimesGivenDay = builder.responsenTimesGivenDay;
		responseprobStartTime = builder.responseprobStartTime;
		shiftable = builder.shiftable;
		config = builder.config;
		probApplianceUsed = builder.probApplianceUsed;
		simulationWorld = builder.simulationWorld;
	}

	/**
	 * Adds an appliance to the specified activity model with probability prob.
	 *
	 * @param day the activity model
	 * @param a the appliance
	 * @param prob the probability of addition 
	 */
	private void addAppliance (String day, Appliance a, Double prob) {
		if(appliances.get(day) == null) {
			appliances.put(day, new Vector<Appliance>());
		}
		Vector<Appliance> vector = appliances.get(day);
		vector.add(a);
		if(probApplianceUsed.get(day) == null) {
			probApplianceUsed.put(day, new Vector<Double>());
		}
		Vector<Double> probVector = probApplianceUsed.get(day);
		probVector.add(prob);
	}

	/**
	 * Adds a start time probability distribution to the specified activity model.
	 *
	 * @param day the activity model
	 * @param probDist the probability distribution
	 */
	public void addStartTime(String day, ProbabilityDistribution probDist) {
		probStartTime.put(day, probDist);
	}

	/**
	 * Adds a duration probability distribution to the specified activity model.
	 *
	 * @param day the activity model
	 * @param probDist the probability distribution
	 */
	public void addDuration(String day, ProbabilityDistribution probDist) {
		probDuration.put(day, probDist);
	}

	/**
	 * Adds a repetitions probability distribution to the specified activity model.
	 *
	 * @param day the activity model
	 * @param probDist the probability distribution
	 */
	public void addTimes(String day, ProbabilityDistribution probDist) {
		nTimesGivenDay.put(day, probDist);
	}

	/**
	 * Adds the shiftable property for the specified activity model.
	 *
	 * @param day the activity model
	 * @param value the value for the shiftable property
	 */
	public void addShiftable(String day, Boolean value) {
		shiftable.put(day, new Boolean(value));
	}

	/**
	 * Adds the config property for the specified activity model.
	 *
	 * @param day the activity model
	 * @param value the value for the config property
	 */
	public void addConfig(String day, Boolean value) {
		config.put(day, new Boolean(value));
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.entities.Entity#getName()
	 */
	@Override
	public String getName () {
		return name;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.entities.Entity#getDescription()
	 */
	@Override
	public String getDescription () {
		return description;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.entities.Entity#getType()
	 */
	@Override
	public String getType () {
		return type;
	}

	/**
	 * Gets the simulation world.
	 *
	 * @return the simulation world
	 */
	public SimulationParams getSimulationWorld () {
		return simulationWorld;
	}

	/**
	 * Gets the key.
	 *
	 * @param keyword the keyword
	 * @return the key
	 */
	private String getKey(String keyword) {
		Set<String> set = nTimesGivenDay.keySet();
		Iterator<String> iter = set.iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			if(key.contains(keyword)) {
				return key;
			}
		}
		return new String();
	}

	/**
	 * Calculate expected power per minute for a full day.
	 *
	 * @return the expected power values per minute for a full day
	 */
	public double[] calcExpPower() {
		double[] act_exp = new double[Constants.MIN_IN_DAY];
		ProbabilityDistribution numOfTimesProb = null;
		ProbabilityDistribution responseNumOfTimesProb = null;
		ProbabilityDistribution startProb = null;
		ProbabilityDistribution responseStartProb = null;
		ProbabilityDistribution durationProb = null;
		Boolean isShiftable = null;
		Boolean isExclusive = null;
		Vector<Double> probVector = new Vector<Double>();
		Vector<Appliance> vector = new Vector<Appliance>();
		int tick = 0;

		// First search for specific days
		String date = simulationWorld.getSimCalendar().getCurrentDate(tick);
		String dayOfWeek = simulationWorld.getSimCalendar().getDayOfWeek(tick);
		String dateKey = getKey(date);
		String dayOfWeekKey = getKey(dayOfWeek);

		boolean weekend = simulationWorld.getSimCalendar().isWeekend(tick);
		numOfTimesProb = nTimesGivenDay.get(dateKey);
		startProb = probStartTime.get(dateKey);
		durationProb = probDuration.get(dateKey);
		probVector = probApplianceUsed.get(dateKey);
		vector = appliances.get(dateKey);
		isShiftable = shiftable.get(dateKey);
		isExclusive = config.get(dateKey);
		// Then search for specific days
		if(!notNull(numOfTimesProb, startProb, durationProb, probVector, vector)) {
			numOfTimesProb = nTimesGivenDay.get(dayOfWeekKey);
			startProb = probStartTime.get(dayOfWeekKey);
			durationProb = probDuration.get(dayOfWeekKey);
			probVector = probApplianceUsed.get(dayOfWeekKey);
			isShiftable = shiftable.get(dayOfWeekKey);
			vector = appliances.get(dayOfWeekKey);
			// Then for weekdays and weekends
			if(!notNull(numOfTimesProb, startProb, durationProb, probVector, vector)) {
				if (weekend) {
					numOfTimesProb = nTimesGivenDay.get(WEEKENDS);
					startProb = probStartTime.get(WEEKENDS);
					durationProb = probDuration.get(WEEKENDS);
					probVector = probApplianceUsed.get(WEEKENDS);
					isShiftable = shiftable.get(WEEKENDS);
					isExclusive = config.get(WEEKENDS);
					vector = appliances.get(WEEKENDS);
				} else {
					numOfTimesProb = nTimesGivenDay.get(WEEKDAYS);
					startProb = probStartTime.get(WEEKDAYS);
					durationProb = probDuration.get(WEEKDAYS);
					probVector = probApplianceUsed.get(WEEKDAYS);
					isShiftable = shiftable.get(WEEKDAYS);
					isExclusive = config.get(WEEKDAYS);
					vector = appliances.get(WEEKDAYS);
				}
				// Backwards compatibility
				if(!notNull(numOfTimesProb, startProb, durationProb, probVector, vector)) {
					if (weekend) {
						numOfTimesProb = nTimesGivenDay.get(NONWORKING);
						startProb = probStartTime.get(NONWORKING);
						durationProb = probDuration.get(NONWORKING);
						probVector = probApplianceUsed.get(NONWORKING);
						isShiftable = shiftable.get(NONWORKING);
						isExclusive = config.get(NONWORKING);
						vector = appliances.get(NONWORKING);
					} else {
						numOfTimesProb = nTimesGivenDay.get(WORKING);
						startProb = probStartTime.get(WORKING);
						durationProb = probDuration.get(WORKING);
						probVector = probApplianceUsed.get(WORKING);
						isShiftable = shiftable.get(WORKING);
						isExclusive = config.get(WORKING);
						vector = appliances.get(WORKING);
					}
					// Then for any
					if(!notNull(numOfTimesProb, startProb, durationProb, probVector, vector)) {
						numOfTimesProb = nTimesGivenDay.get(ANY);
						startProb = probStartTime.get(ANY);
						durationProb = probDuration.get(ANY);
						probVector = probApplianceUsed.get(ANY);
						isShiftable = shiftable.get(ANY);
						isExclusive = config.get(ANY);
						vector = appliances.get(ANY);
					}
				}				
			}
		}

		if(vector != null) {
			int durationMax = Math.min(Constants.MIN_IN_DAY, durationProb.getHistogram().length - 1);
			for(Appliance app : vector) {
				//				System.out.println("Appliance:" + app.getName());
				Double[] applianceConsumption = app.getActiveConsumption();
				//				NumberFormat nf = new DecimalFormat("0.#");
				//				for (double c : applianceConsumption) {
				//					System.out.println(nf.format(c));
				//				}
				boolean staticConsumption = app.isStaticConsumption();
				//				System.out.println("Is static: " + staticConsumption);
				for (int j = 0; j < act_exp.length; j++)
					act_exp[j] += aggregatedProbability(durationProb, startProb, applianceConsumption, j, durationMax, staticConsumption);
				//				for (double c : act_exp) {
				//					System.out.print(nf.format(c) + " ");
				//				}
				//				System.out.println(" ");
			}
			for (int j = 0; j < act_exp.length; j++)
				act_exp[j] = (act_exp[j] * estimateNumberOfTimesFactor(numOfTimesProb) / appliances.size());
		}

		return act_exp;
	}

	private double estimateNumberOfTimesFactor (ProbabilityDistribution dailyTimes)
	{
		double result = 0;
		for (int i = 0; i < dailyTimes.getHistogram().length; i++) {
			result += i * dailyTimes.getHistogram()[i];
		}
		return result;
	}

	private double aggregatedProbability (ProbabilityDistribution duration, ProbabilityDistribution startTime,
			Double[] consumption, int index,
			int durationMax,
			boolean staticConsumption) {

		double result = 0;
		for (int i = 0; i < durationMax; i++) {
			if (staticConsumption)
				result += duration.getProbabilityGreater(i) * startTime.getProbability((Constants.MIN_IN_DAY + index - i) % Constants.MIN_IN_DAY) * consumption[0];
			else
				result += duration.getProbabilityGreater(i) * startTime.getProbability((Constants.MIN_IN_DAY + index - i) % Constants.MIN_IN_DAY) * consumption[i % consumption.length];
		}
		return result;
	}

	/**
	 * Update daily schedule.
	 *
	 * @param tick the tick
	 * @param queue the queue
	 * @param pricing the pricing policy
	 * @param baseline the baseline pricing policy
	 * @param awareness the person's awareness
	 * @param sensitivity the person's  sensitivity
	 * @param responseType the response type
	 * @param orng the random number generator
	 */
	public void updateDailySchedule(int tick, PriorityBlockingQueue<Event> queue,
			PricingPolicy pricing, PricingPolicy baseline, double awareness,
			double sensitivity, String responseType, ORNG orng) {
		/*
		 *  Decide on the number of times the activity is going to be activated
		 *  during a day
		 */
		ProbabilityDistribution numOfTimesProb = null;
		ProbabilityDistribution responseNumOfTimesProb = null;
		ProbabilityDistribution startProb = null;
		ProbabilityDistribution responseStartProb = null;
		ProbabilityDistribution durationProb = null;
		Boolean isShiftable = null;
		Boolean isExclusive = null;
		Vector<Double> probVector = null;
		Vector<Appliance> vector = null;

		// First search for specific days
		String date = simulationWorld.getSimCalendar().getCurrentDate(tick);
		String dayOfWeek = simulationWorld.getSimCalendar().getDayOfWeek(tick);
		String dateKey = getKey(date);
		String dayOfWeekKey = getKey(dayOfWeek);
		//				System.out.println(date);
		String selector = "date";
		boolean weekend = simulationWorld.getSimCalendar().isWeekend(tick);
		numOfTimesProb = nTimesGivenDay.get(dateKey);
		startProb = probStartTime.get(dateKey);
		durationProb = probDuration.get(dateKey);
		probVector = probApplianceUsed.get(dateKey);
		vector = appliances.get(dateKey);
		isShiftable = shiftable.get(dateKey);
		isExclusive = config.get(dateKey);
		// Then search for specific days
		if(!notNull(numOfTimesProb, startProb, durationProb, probVector, vector)) {
			selector = "dateofweek";
			numOfTimesProb = nTimesGivenDay.get(dayOfWeekKey);
			startProb = probStartTime.get(dayOfWeekKey);
			durationProb = probDuration.get(dayOfWeekKey);
			probVector = probApplianceUsed.get(dayOfWeekKey);
			isShiftable = shiftable.get(dayOfWeekKey);
			vector = appliances.get(dayOfWeekKey);
			// Then for weekdays and weekends
			if(!notNull(numOfTimesProb, startProb, durationProb, probVector, vector)) {
				if (weekend) {
					selector = "weekend";
					numOfTimesProb = nTimesGivenDay.get(WEEKENDS);
					startProb = probStartTime.get(WEEKENDS);
					durationProb = probDuration.get(WEEKENDS);
					probVector = probApplianceUsed.get(WEEKENDS);
					isShiftable = shiftable.get(WEEKENDS);
					isExclusive = config.get(WEEKENDS);
					vector = appliances.get(WEEKENDS);
				} else {
					selector = "weekday";
					numOfTimesProb = nTimesGivenDay.get(WEEKDAYS);
					startProb = probStartTime.get(WEEKDAYS);
					durationProb = probDuration.get(WEEKDAYS);
					probVector = probApplianceUsed.get(WEEKDAYS);
					isShiftable = shiftable.get(WEEKDAYS);
					isExclusive = config.get(WEEKDAYS);
					vector = appliances.get(WEEKDAYS);
				}
				// Backwards compatibility
				if(!notNull(numOfTimesProb, startProb, durationProb, probVector, vector)) {
					if (weekend) {
						selector = "weekend";
						numOfTimesProb = nTimesGivenDay.get(NONWORKING);
						startProb = probStartTime.get(NONWORKING);
						durationProb = probDuration.get(NONWORKING);
						probVector = probApplianceUsed.get(NONWORKING);
						isShiftable = shiftable.get(NONWORKING);
						isExclusive = config.get(NONWORKING);
						vector = appliances.get(NONWORKING);
					} else {
						selector = "weekday";
						numOfTimesProb = nTimesGivenDay.get(WORKING);
						startProb = probStartTime.get(WORKING);
						durationProb = probDuration.get(WORKING);
						probVector = probApplianceUsed.get(WORKING);
						isShiftable = shiftable.get(WORKING);
						isExclusive = config.get(WORKING);
						vector = appliances.get(WORKING);
					}
					// Then for any
					if(!notNull(numOfTimesProb, startProb, durationProb, probVector, vector)) {
						selector = "any";
						numOfTimesProb = nTimesGivenDay.get(ANY);
						startProb = probStartTime.get(ANY);
						durationProb = probDuration.get(ANY);
						probVector = probApplianceUsed.get(ANY);
						isShiftable = shiftable.get(ANY);
						isExclusive = config.get(ANY);
						vector = appliances.get(ANY);
					}
				}				
			}
		}

		if(notNull(numOfTimesProb, startProb, durationProb, probVector, vector)) {
			// Response
			responseStartProb = startProb;
			responseNumOfTimesProb = numOfTimesProb;
			if(isShiftable.booleanValue()) {
				if(pricing.getType().equalsIgnoreCase("TOUPricing") && 
						baseline.getType().equalsIgnoreCase("TOUPricing")) {
					if(responsenTimesGivenDay.containsKey(selector)) {
						responseNumOfTimesProb = responsenTimesGivenDay.get(selector);
					} else {
						responseNumOfTimesProb = Response.respond(numOfTimesProb, pricing,
								baseline, awareness, sensitivity, "Daily");
						responsenTimesGivenDay.put(selector, responseNumOfTimesProb);
					}
					if(responseprobStartTime.containsKey(selector)) {
						responseStartProb = responseprobStartTime.get(selector);
					} else {
						responseStartProb = Response.respond(startProb, pricing,
								baseline, awareness, sensitivity, responseType);
						responseprobStartTime.put(selector, responseStartProb);
					}
				}
			}

			int numOfTimes = 0;
			try {
				numOfTimes = responseNumOfTimesProb.getPrecomputedBin(orng.nextDouble());
			} catch (Exception e) {
				System.err.println(Utils.stackTraceToString(e.getStackTrace()));
				e.printStackTrace();
			}

			/*
			 * Decide the duration and start time for each activity activation
			 */
			while (numOfTimes > 0) {
				int duration = Math.max(durationProb.getPrecomputedBin(orng.nextDouble()), 1);
				int startTime = Math.min(Math.max(responseStartProb.getPrecomputedBin(orng.nextDouble()), 0), 1439);
				if(vector.size() > 0) {
					if(isExclusive.booleanValue()) {
						int selectedApp = orng.nextInt(vector.size());
						Appliance a = vector.get(selectedApp);
						addApplianceActivation(a, duration, startTime, tick, queue, this, orng);
					} else {
						for (int j = 0; j < vector.size(); j++) {
							Appliance a = vector.get(j);
							addApplianceActivation(a, duration, startTime, tick, queue, this, orng);

						}
					}
				}
				numOfTimes--;
			}
		}
	}

	private void addApplianceActivation(Appliance a, int duration, int startTime, int tick, PriorityBlockingQueue<Event> queue, Activity act, ORNG orng) {
		int appDuration = duration;
		int appStartTime = startTime;
		String hash = Utils.hashcode((new Long(orng.nextLong()).toString()));
		Event eOn = new Event(tick + appStartTime, Event.SWITCH_ON, a, hash, act);
		queue.offer(eOn);
		Event eOff = new Event(tick + appStartTime + appDuration, Event.SWITCH_OFF, a, hash, act);
		queue.offer(eOff);
	}

	private boolean notNull(Object a, 
			Object b, 
			Object c, 
			Object d, 
			Object e) {
		return a != null && 
				b != null && 
				c != null && 
				d != null &&
				e != null;
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
	 * Update the average power.
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
	 * Gets the start time probability distributions per activity model.
	 *
	 * @return the start time probability distributions per activity model
	 */
	public TreeMap<String, ProbabilityDistribution> getProbStartTime() {
		return probStartTime;
	}

	/**
	 * Gets the duration probability distributions per activity model.
	 *
	 * @return the duration probability distributions per activity model
	 */
	public TreeMap<String, ProbabilityDistribution> getProbDuration() {
		return probDuration;
	}

	/**
	 * Gets the shiftable property values per activity model.
	 *
	 * @return the shiftable property values per activity model
	 */
	public TreeMap<String, Boolean> getShiftable() {
		return shiftable;
	}

	/**
	 * Gets the config property values per activity model.
	 *
	 * @return the config property values per activity model
	 */
	public TreeMap<String, Boolean> getConfig() {
		return config;
	}

	/**
	 * Gets the appliances involved in the activity per activity model.
	 *
	 * @return the appliances involved in the activity per activity model
	 */
	public TreeMap<String, Vector<Appliance>> getAppliances() {
		return appliances;
	}

	/**
	 * Gets the repetitions probability distributions per activity model.
	 *
	 * @return the  repetitions probability distributions per activity model.
	 */
	public TreeMap<String, ProbabilityDistribution> getnTimesGivenDay() {
		return nTimesGivenDay;
	}

	/**
	 * Sets the appliances involved in the activity per activity model.
	 *
	 * @param appliances the new appliances involved in the activity per activity model
	 */
	public void setAppliances(TreeMap<String, Vector<Appliance>> appliances) {
		this.appliances = appliances;
	}

	/**
	 * Adds the appliances in applianceIds to the specified activity model.
	 * The actual Appliance objects are also supplied as an input argument (the second one).
	 *
	 * @param applianceIds the ids of the appliances to be added
	 * @param appliances the actual Appliance objects 
	 * @param actmodDayType the activity model
	 */
	public void addAppliances(String[] applianceIds, TreeMap<String, Appliance> appliances, String actmodDayType)
	{
		for(int m = 0; m < applianceIds.length; m++) {
			String containAppId = applianceIds[m].trim();
			Appliance app  = appliances.get(containAppId);
			addAppliance(actmodDayType,app,1.0/applianceIds.length);
		}
	}


}
