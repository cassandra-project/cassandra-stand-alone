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

import java.util.TreeMap;

/**
 * The simulation's demographic data, used to instantiate entities in dynamic scenarios.
 *
 * @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 */
public class DemographicData
{

	/** The demographic data set's name. */
	private String name;

	/** The demographic data set's description. */
	private String description;

	/** The demographic data set's type. */
	private String type;

	/** The number of entities (installations) to be created. */
	private int numEntities;

	/** The probabilities of participation for defined installations. */
	private TreeMap<String, Double> inst_probs; 

	/** The probabilities of participation for defined persons. */
	private TreeMap<String, Double> person_probs; 

	/** The probabilities of participation for defined appliances. */
	private TreeMap<String, Double> app_probs; 


	/**
	 * Instantiates a new demographic data object.
	 *
	 * @param name the demographic data set's name
	 * @param description the demographic data set's description
	 * @param type the demographic data set's type
	 * @param numEntities the number of entities (installations) to be created
	 * @param inst_probs the probabilities of participation for defined installations
	 * @param person_probs the probabilities of participation for defined persons
	 * @param app_probs the probabilities of participation for defined appliances
	 */
	public DemographicData(String name, String description, String type, int numEntities, 
			TreeMap<String, Double> inst_probs, TreeMap<String, Double> person_probs, TreeMap<String, Double> app_probs)
	{
		this.type = type;
		this.name = name;
		this.description = description;
		this.numEntities = numEntities;
		this.inst_probs = inst_probs;
		this.person_probs = person_probs;
		this.app_probs = app_probs;
	}

	/**
	 * Gets the demographic data set's name.
	 *
	 * @return the  demographic data set's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the demographic data set's description.
	 *
	 * @return the  demographic data set's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the demographic data set's type.
	 *
	 * @return the  demographic data set's type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the number of entities (installations) to be created.
	 *
	 * @return the number of entities (installations) to be created
	 */
	public int getNumEntities() {
		return numEntities;
	}

	/**
	 * Gets the probabilities of participation for defined installations.
	 *
	 * @return the the probabilities of participation for defined installations
	 */
	public TreeMap<String, Double> getInst_probs() {
		return inst_probs;
	}

	/**
	 * Gets the probabilities of participation for defined persons.
	 *
	 * @return the probabilities of participation for defined persons
	 */
	public TreeMap<String, Double> getPerson_probs() {
		return person_probs;
	}

	/**
	 * Gets the probabilities of participation for defined appliances.
	 *
	 * @return the probabilities of participation for defined appliances
	 */
	public TreeMap<String, Double> getApp_probs() {
		return app_probs;
	}

	/**
	 * Sets the demographic data set's name.
	 *
	 * @param name the new demographic data set's name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the demographic data set's description.
	 *
	 * @param description the new demographic data set's description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the demographic data set's type.
	 *
	 * @param type the new demographic data set's type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Sets the num entities.
	 *
	 * @param numEntities the new num entities
	 */
	public void setNumEntities(int numEntities) {
		this.numEntities = numEntities;
	}

	/**
	 * Sets the probabilities of participation for defined installations.
	 *
	 * @param inst_probs the new probabilities of participation for defined installations
	 */
	public void setInst_probs(TreeMap<String, Double> inst_probs) {
		this.inst_probs = inst_probs;
	}

	/**
	 * Sets the probabilities of participation for defined persons.
	 *
	 * @param person_probs the new probabilities of participation for defined persons
	 */
	public void setPerson_probs(TreeMap<String, Double> person_probs) {
		this.person_probs = person_probs;
	}

	/**
	 * Sets the probabilities of participation for defined appliances.
	 *
	 * @param app_probs the probabilities of participation for defined appliances
	 */
	public void setApp_probs(TreeMap<String, Double> app_probs) {
		this.app_probs = app_probs;
	}

}
