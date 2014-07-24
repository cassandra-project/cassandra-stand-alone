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
package eu.cassandra.sim.utilities;

import java.net.UnknownHostException;

import java.util.HashMap;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * The connection to the MongoDB database.
 */
public class DBConn {

	/** The database name. */
	private static String DB_NAME = "test";

	/** The database host. */
	private static String DB_HOST = "localhost";

	/** The database connection. */
	private static DBConn dbConn = new DBConn();

	/** The runs. */
	private static HashMap<String, DB> dbRuns = new HashMap<String,DB>();

	/** The MongoDB instance. */
	private Mongo m;

	/** The database. */
	private DB db;
	
	/**
	 * Instantiates a new database connection.
	 */
	private DBConn() {
		try {
			m = new Mongo(DB_HOST);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		db = m.getDB(DB_NAME);
	}

	/**
	 * Gets the database connection.
	 *
	 * @return the database connection
	 */
	public static DB getConn() {
		return dbConn.db;
	}
	
	/**
	 * Gets a connection to the specified database.
	 *
	 * @param dbname the database name
	 * @return the database connection
	 */
	public static DB getConn(String dbname) {
		if(dbname == null)
			return getConn();
		else if(dbRuns.containsKey(dbname)) {
			return dbRuns.get(dbname);
		} else {
			try {
				Mongo m = new Mongo(DB_HOST);
				dbRuns.put(dbname, m.getDB(dbname));
				return dbRuns.get(dbname);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (MongoException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
