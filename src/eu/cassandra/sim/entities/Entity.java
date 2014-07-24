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
package eu.cassandra.sim.entities;

/**
 * An Entity used in the simulation.
 */
public abstract class Entity {
	
	/** The entity's id. */
	protected String id;
	
	/** The entity's name. */
	protected String name;
	
	/** The entity's description. */
	protected String description;
	
	/** The entity's type. */
	protected String type;
	
	/** The entity's parent id. */
	protected String parentId;
	
	
	/**
	 * Sets the entity's id.
	 *
	 * @param aid the new entity's id
	 */
	public void setId(String aid) {
		id = aid;
	}
	
	/**
	 * Gets the entity's id.
	 *
	 * @return the entity's id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the entity's name.
	 *
	 * @param aname the new entity's name
	 */
	public void setName(String aname) {
		name = aname;
	}
	
	/**
	 * Gets the entity's name.
	 *
	 * @return the entity's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the entity's type.
	 *
	 * @param atype the new entity's type
	 */
	public void setType(String atype) {
		type = atype;
	}
	
	/**
	 * Gets the entity's type.
	 *
	 * @return the entity's type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Sets the entity's description.
	 *
	 * @param adescription the new entity's description
	 */
	public void setDescription(String adescription) {
		description = adescription;
	}
	
	/**
	 * Gets the entity's description.
	 *
	 * @return the entity's description
	 */
	public String getDescription() {
		return description;
	}

}
