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

import java.util.ArrayList;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.cassandra.sim.entities.Entity;

/**
 * Class for storing the variables of a consumption model. It has no 
 * functionality except parsing a JSON string and return the values on demand.
 *
 */
public class ConsumptionModel extends Entity {

	/** How many times the patterns repeats */
	private int outerN;

	/** The number of patterns */
	private int patternN;

	/** Total duration of the consumption model */
	private int totalDuration;

	/** How many times each pattern runs */
	private int[] n;

	/** Total duration per pattern */
	private int[] patternDuration;

	/** An array storing the consumption patterns */
	private ArrayList<Tripplet>[] patterns;

	/** JSON string containing the consumption model  */
	private String model;

	/**
	 * Instantiates a new consumption model.
	 */
	public ConsumptionModel() {}

	/**
	 * Instantiates a new consumption model, by parsing a JSON string
	 *
	 * @param amodel the JSON string
	 * @param type the type of the consumption model ("p" or "q" for active or reactive power, respectively)
	 * @throws Exception the exception
	 */
	public ConsumptionModel(String amodel, String type) throws Exception {
		model = amodel;
		DBObject modelObj = (DBObject) JSON.parse(model);
		init(modelObj, type);
	}

	/**
	 * Instantiates a new consumption model with the parameters provided.
	 *
	 * @param outerN how many times the patterns repeats
	 * @param n how many times each pattern runs
	 * @param patterns the patterns as active/reactive power value - duration - slope tripplets
	 */
	public ConsumptionModel(int outerN, int[] n, ArrayList<Tripplet>[] patterns) {
		this.outerN = outerN;
		this.patterns = patterns;
		this.n = n;
		this.patterns = patterns;
		this.patternN = n.length;
		patternDuration = new int[patternN];

		for(int i = 0; i < patternN; i++) {
			int tripplets = patterns[i].size();
			for(int j = 0; j < tripplets; j++) {
				Tripplet t = patterns[i].get(j);
				patternDuration[i] += t.d; 
				totalDuration += (n[i] * t.d);
			}
		}
	}

	/**
	 * Gets the total duration of the consumption model.
	 *
	 * @return the total duration of the consumption model
	 */
	public int getTotalDuration() { return totalDuration; }

	/**
	 * Gets how many times the patterns repeats.
	 *
	 * @return how many times the patterns repeats
	 */
	public int getOuterN() { return outerN; }

	/**
	 * Gets the number of patterns.
	 *
	 * @return the number of patterns
	 */
	public int getPatternN() { return patternN; }

	/**
	 * Gets the number of times that the i-th pattern runs.
	 *
	 * @param i the index i
	 * @return the the number of times that the i-th pattern runs
	 */
	public int getN(int i) { return n[i]; }

	/**
	 * Gets the i-th pattern's duration.
	 *
	 * @param i the index i
	 * @return the i-th pattern's duration
	 */
	public int getPatternDuration(int i) { return patternDuration[i]; }

	/**
	 * Gets the i-th pattern.
	 *
	 * @param i the index i
	 * @return the i-th pattern
	 */
	public ArrayList<Tripplet> getPattern(int i) { return patterns[i]; }

	/**
	 * Gets the array storing the consumption patterns.
	 *
	 * @return the array storing the consumption patterns
	 */
	public ArrayList<Tripplet>[] getPatterns() { return patterns; }

	/**
	 * Check if consumption is static.
	 *
	 * @return true, if consumption is static.
	 */
	public boolean checkStatic()
	{
		boolean result = true;

		Double[] values = getValues();

		// System.out.println("Appliance: " + name + " Model: "
		// + activeConsumptionModelString);
		//
		// System.out.println("Appliance: " + name + " Values: "
		// + Arrays.toString(values));

		for (int i = 0; i < values.length - 1; i++) {
			// System.out.println("Previous: " + values[i].doubleValue() + " Next: "
			// + values[i + 1].doubleValue());
			if (values[i].doubleValue() != values[i + 1].doubleValue()) {
				// System.out.println("IN");
				result = false;
				break;
			}
		}

		return result;
	}

	/**
	 * Gets the consumption.
	 *
	 * @return the consumption
	 */
	public Double[] getConsumption()
	{

		ArrayList<Double> temp = new ArrayList<Double>();
		int times = getOuterN();
		if (times == 0)
			times = 2;
		// Number of repeats
		for (int i = 0; i < times; i++) {
			// System.out.println("Time: " + i);
			// Number of patterns in each repeat
			for (int j = 0; j < getPatternN(); j++) {
				// System.out.println("Pattern: " + j);
				int internalTimes = getN(j);
				if (internalTimes == 0)
					internalTimes = 2;
				// System.out.println("Internal Times: " + k);
				for (int k = 0; k < internalTimes; k++) {
					ArrayList<Tripplet> tripplets = getPattern(j);
					for (int l = 0; l < tripplets.size(); l++) {
						// System.out.println("TripletPower: " + l);
						for (int m = 0; m < tripplets.get(l).d; m++) {
							temp.add(tripplets.get(l).v);
						}
					}
				}
			}
		}
		Double[] result = new Double[temp.size()];
		temp.toArray(result);
		return result;

	}

	/**
	 * Gets the consumption model as a BasicDBObject.
	 *
	 * @return the consumption model as a BasicDBObject
	 */
	public BasicDBObject toDBObject() {
		BasicDBObject obj = new BasicDBObject();
		obj.put("name", name);
		obj.put("description", description);
		obj.put("app_id", parentId);
		obj.put("model", model);
		return obj;
	}


	private void init (DBObject modelObj, String type) throws Exception {

		try {

			try {
				outerN = ((Integer)modelObj.get("n")).intValue();
			} catch(ClassCastException e ) {
				try {
					outerN = ((Long)modelObj.get("n")).intValue();
				} catch(ClassCastException e1 ) {
					outerN = ((Double)modelObj.get("n")).intValue();
				}
			} catch(NullPointerException npe) {
				throw new Exception("Bad Parameter Exception: outer iterations parameter name should be n");
			}
			BasicDBList patternsObj = (BasicDBList)modelObj.get("params");
			patternN = patternsObj.size();
			patterns = new ArrayList[patternN];
			n = new int[patternN];
			patternDuration = new int[patternN];
			for(int i = 0; i < patternN; i++) {
				try {
					n[i] = ((Integer)((DBObject)patternsObj.get(i)).get("n")).intValue();
				} catch(ClassCastException e ) {
					try {
						n[i] = ((Long)((DBObject)patternsObj.get(i)).get("n")).intValue();
					} catch(ClassCastException e1 ) {
						n[i] = ((Double)((DBObject)patternsObj.get(i)).get("n")).intValue();
					}
				} catch(NullPointerException npe) {
					throw 
					new Exception("Bad Parameter Exception: inner iterations parameter name should be n");
				}
				BasicDBList values = ((BasicDBList)((DBObject)patternsObj.get(i)).get("values"));
				int tripplets = values.size();
				patterns[i] = new ArrayList<Tripplet>(tripplets);
				for(int j = 0; j < tripplets; j++) {
					Tripplet t = new Tripplet();
					try {
						t.v = ((Double)((DBObject)values.get(j)).get(type)).doubleValue();
					} catch(ClassCastException e) {
						try {
							t.v = ((Integer)((DBObject)values.get(j)).get(type)).intValue();
						} catch(ClassCastException e1) {
							t.v = ((Long)((DBObject)values.get(j)).get(type)).intValue();
						}
					} catch(NullPointerException npe) {
						throw 
						new Exception("Bad Parameter Exception: power parameter name should be " + type);
					}
					try {
						t.d = ((Integer)((DBObject)values.get(j)).get("d")).intValue();
					} catch(ClassCastException e) {
						try {
							t.d = ((Double)((DBObject)values.get(j)).get("d")).intValue();
						} catch(ClassCastException e1) {
							t.d = ((Long)((DBObject)values.get(j)).get("d")).intValue();
						}
					} catch(NullPointerException npe) {
						throw 
						new Exception("Bad Parameter Exception: duration parameter name should be d");
					}
					patternDuration[i] += t.d; 
					totalDuration += (n[i] * t.d);
					try {
						t.s = ((Double)((DBObject)values.get(j)).get("s")).doubleValue();
					} catch(ClassCastException e) {
						try {
							t.s = ((Integer)((DBObject)values.get(j)).get("s")).intValue();
						} catch(ClassCastException e1) {
							t.s = ((Long)((DBObject)values.get(j)).get("s")).intValue();
						}
					} catch(NullPointerException npe) {
						throw 
						new Exception("Bad Parameter Exception: slope parameter name should be s");
					}
					patterns[i].add(t);
				}
			}
		} catch(Exception bpe) {
			throw bpe;
		}

	}

	private Double[] getValues ()
	{
		ArrayList<Double> temp = new ArrayList<Double>();
		int times = getOuterN();
		if (times == 0)
			times = 1;
		// Number of repeats
		for (int i = 0; i < times; i++) {
			// System.out.println("Time: " + i);
			// Number of patterns in each repeat
			for (int j = 0; j < getPatternN(); j++) {
				// System.out.println("Pattern: " + j);
				int internalTimes = getN(j);
				if (internalTimes == 0)
					internalTimes = 2;
				// System.out.println("Internal Times: " + k);
				for (int k = 0; k < internalTimes; k++) {
					ArrayList<Tripplet> triplets = getPattern(j);
					for (int l = 0; l < triplets.size(); l++) {
						// System.out.println("Tripplet: " + l);
						for (int m = 0; m < triplets.get(l).d; m++) {
							temp.add(triplets.get(l).v);
						}
					}
				}
			}
		}
		ArrayList<Double> values = new ArrayList<Double>();
		for (int i = 0; i < temp.size(); i++) {
			values.add(temp.get(i));
			values.add(temp.get(i));
			values.add(temp.get(Math.min(i + 1, temp.size() - 1)));
		}
		Double[] result = new Double[values.size()];
		values.toArray(result);
		return result;
	}


}
