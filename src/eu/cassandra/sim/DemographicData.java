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

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.cassandra.sim.utilities.Utils;

/**
 * 
 * 
 * @author Fani A. Tzima (fani [at] iti [dot] gr)
 * 
 */
public class DemographicData
{

	private String name;
	private String description;
	private String type;
	private int numEntities;
	private HashMap<String, Double> inst_probs; 
	private HashMap<String, Double> person_probs; 
	private HashMap<String, Double> app_probs; 



	public DemographicData(String name, String description, String type, int numEntities, 
			 HashMap<String, Double> inst_probs, HashMap<String, Double> person_probs, HashMap<String, Double> app_probs)
	{
		this.type = type;
		this.name = name;
		this.description = description;
		this.numEntities = numEntities;
		this.inst_probs = inst_probs;
		this.person_probs = person_probs;
		this.app_probs = app_probs;
	}



	public String getName() {
		return name;
	}



	public String getDescription() {
		return description;
	}



	public String getType() {
		return type;
	}



	public int getNumEntities() {
		return numEntities;
	}

	public HashMap<String, Double> getInst_probs() {
		return inst_probs;
	}

	public HashMap<String, Double> getPerson_probs() {
		return person_probs;
	}



	public HashMap<String, Double> getApp_probs() {
		return app_probs;
	}



	public void setName(String name) {
		this.name = name;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public void setType(String type) {
		this.type = type;
	}



	public void setNumEntities(int numEntities) {
		this.numEntities = numEntities;
	}

	public void setInst_probs(HashMap<String, Double> inst_probs) {
		this.inst_probs = inst_probs;
	}

	public void setPerson_probs(HashMap<String, Double> person_probs) {
		this.person_probs = person_probs;
	}



	public void setApp_probs(HashMap<String, Double> app_probs) {
		this.app_probs = app_probs;
	}




}
