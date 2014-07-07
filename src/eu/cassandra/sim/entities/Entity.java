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

import com.mongodb.BasicDBObject;

// TODO: Auto-generated Javadoc
/**
 * The Class Entity.
 */
public abstract class Entity {
	
	/** The id. */
	protected String id;
	
	/** The name. */
	protected String name;
	
	/** The description. */
	protected String description;
	
	/** The type. */
	protected String type;
	
	/** The parent id. */
	protected String parentId;
	
	/**
	 * To db object.
	 *
	 * @return the basic db object
	 */
	public abstract BasicDBObject toDBObject();
	
	/**
	 * Gets the collection.
	 *
	 * @return the collection
	 */
	public abstract String getCollection();
	
	/**
	 * Sets the parent id.
	 *
	 * @param aparent the new parent id
	 */
	public void setParentId(String aparent) {
		parentId = aparent;
	}
	
	/**
	 * Sets the id.
	 *
	 * @param aid the new id
	 */
	public void setId(String aid) {
		id = aid;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param aname the new name
	 */
	public void setName(String aname) {
		name = aname;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the type.
	 *
	 * @param atype the new type
	 */
	public void setType(String atype) {
		type = atype;
	}
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Sets the description.
	 *
	 * @param adescription the new description
	 */
	public void setDescription(String adescription) {
		description = adescription;
	}
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
