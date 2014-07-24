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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The wrapper class for storing simulation output results in an Apache Derby database.
 *
 * @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 */
public class DerbyResults implements DBResults{
	
	/** The database name. */
	private String dbname;
	
	/** The database connection. */
	private Connection conn;
	
	/**
	 * Instantiates a new DerbyResults wrapper object.
	 *
	 * @param adbname the target database name
	 */
	public DerbyResults(String adbname) {
 		dbname = adbname;
	}
	
	/* (non-Javadoc)
	 * @see eu.cassandra.sim.utilities.DBResults#createTablesAndIndexes()
	 */
	@Override
	public void createTablesAndIndexes() {
		conn = getConnection();
		createTableAndIndex(COL_AGGRRESULTS, null, true);
		createTableAndIndex(COL_AGGRRESULTS_HOURLY, null, true);
		createTableAndIndex(COL_AGGRRESULTS_HOURLY_EN, null, false);
		createTableAndIndex(COL_AGGRRESULTS_EXP, "id", false);
		
		createTableAndIndex(COL_INSTRESULTS, "inst_id", true);
		createTableAndIndex(COL_INSTRESULTS_HOURLY, "inst_id", true);
		createTableAndIndex(COL_INSTRESULTS_EXP, "inst_id", false);
		createTableAndIndex(COL_INSTRESULTS_HOURLY_EN, "inst_id", false);
		
		createTableAndIndex(COL_ACTRESULTS_EXP, "id", false);
		
		createKPITable(COL_INSTKPIS, "inst_id", true);
		createKPITable(COL_APPKPIS, "app_id", false);
		createKPITable(COL_ACTKPIS, "act_id", false);
		createKPITable(COL_AGGRKPIS, "inst_id", true);
	
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.utilities.DBResults#addKPIs(java.lang.String, double, double, double, double, double)
	 */
	@Override
	public void addKPIs(String inst_id, double maxPower, double avgPower, double energy, double cost, double co2) {
		boolean first = false;
		
		String tableName;
		String tick_tableName;
		String id;
		
		if(inst_id.equalsIgnoreCase(AGGR)) {
			id = AGGR;
			tableName = COL_AGGRKPIS;
			tick_tableName = COL_AGGRRESULTS;
		} else {
			id = inst_id;
			tableName = COL_INSTKPIS;
			tick_tableName = COL_INSTRESULTS;
		}
				
		double[] data = getKPIData(tableName, "inst_id", id);
		
		double newMaxPower = maxPower;
		double newAvgPower = avgPower;
		double newEnergy = energy;
		double newCost = cost;
		double newCo2 = co2;
		if(data == null) 
			first = true;
		else {
			newMaxPower += data[0];
			newAvgPower += data[1];
			newEnergy += data[2];
			newCost +=data[3];
			newCo2 += data[4];
		}
		
		double maxavgValue = newAvgPower;
		
		String maxQuery = "SELECT MAX(P) AS maxp FROM " + tick_tableName;
		if(!inst_id.equalsIgnoreCase(AGGR))
			maxQuery += " WHERE inst_id='" + inst_id +"'";
		ResultSet maxavg = executeSelectQuery(maxQuery);
		if(maxavg != null)
			try {
				while (maxavg.next())
					maxavgValue = maxavg.getDouble("maxp");
			} catch (SQLException e) {
				System.out.println(" . . . exception thrown:");
				errorPrint(e);
			}
		
		String psQuery = "";
		if(first) 
			psQuery = "insert into " + tableName + " values('" + id + "'," + maxavgValue + ", " + newMaxPower + ", " + newAvgPower + ", " + newEnergy + ", " + newCost + ", " + newCo2+ ")";
		else 
			psQuery = "update " + tableName + " set AVGPEAK=" + maxavgValue +", MAXPOWER=" + newMaxPower + ", AVGPOWER=" + newAvgPower 
					+ ", ENERGY=" + newEnergy + ", COST=" + newCost + ", CO2=" + newCo2 + " where inst_id='" + id + "'";
		executeUpdateQuery(psQuery);
	
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.utilities.DBResults#addAppKPIs(java.lang.String, double, double, double, double, double)
	 */
	@Override
	public void addAppKPIs(String app_id, double maxPower, double avgPower, double energy, double cost, double co2) {
		String psQuery = "";
		String tableName = COL_APPKPIS;
		double[] data = getKPIData(tableName, "app_id", app_id);
		
		double newMaxPower = maxPower;
		double newAvgPower = avgPower;
		double newEnergy = energy;
		double newCost = cost;
		double newCo2 = co2;
		if(data == null) {
			psQuery = "insert into " + tableName + " values('" + app_id + "'," + newMaxPower + ", " + newAvgPower + ", " + newEnergy + ", " + newCost + ", " + newCo2+ ")";
			
		} else {
			newMaxPower += data[0];
			newAvgPower += data[1];
			newEnergy += data[2];
			newCost += data[3];
			newCo2 += data[4];
			psQuery = "update " + tableName + " set MAXPOWER=" + newMaxPower + ", AVGPOWER=" + newAvgPower 
					+ ", ENERGY=" + newEnergy + ", COST=" + newCost + ", CO2=" + newCo2 + " where app_id='" + app_id + "'";
		}		
		executeUpdateQuery(psQuery);
	}
	
	/* (non-Javadoc)
	 * @see eu.cassandra.sim.utilities.DBResults#addActKPIs(java.lang.String, double, double, double, double, double)
	 */
	@Override
	public void addActKPIs(String act_id, double maxPower, double avgPower, double energy, double cost, double co2) {
		String psQuery = "";
		String tableName = COL_ACTKPIS;
		double[] data = getKPIData(tableName, "act_id", act_id);
		
		double newMaxPower = maxPower;
		double newAvgPower = avgPower;
		double newEnergy = energy;
		double newCost = cost;
		double newCo2 = co2;
		if(data == null) {
			psQuery = "insert into " + tableName + " values('" + act_id + "',"+ newMaxPower + ", " + newAvgPower + ", " + newEnergy + ", " + newCost + ", " + newCo2+ ")";
			
		} else {
			newMaxPower += data[0];
			newAvgPower += data[1];
			newEnergy += data[2];
			newCost += data[3];
			newCo2 += data[4];
			psQuery = "update " + tableName + " set MAXPOWER=" + newMaxPower + ", AVGPOWER=" + newAvgPower 
					+ ", ENERGY=" + newEnergy + ", COST=" + newCost + ", CO2=" + newCo2 + " where act_id='" + act_id + "'";
		}		
		executeUpdateQuery(psQuery);
	}
	
	/* (non-Javadoc)
	 * @see eu.cassandra.sim.utilities.DBResults#addTickResultForInstallation(int, java.lang.String, double, double, java.lang.String)
	 */
	@Override
	public void addTickResultForInstallation(int tick, String inst_id, double p, double q, String tableName) {
		
		double[] data = getData(tableName, tick, "inst_id", inst_id);
		
		double newp = p;
		double newq = q;
		
		String psQuery = "";
		if(data == null) 
			psQuery = "insert into " + tableName + " values('" + inst_id + "', " + tick + ", " + newp + ", " + newq + ")";
		else {
			newp += data[0];
			newq += data[1];
			psQuery = "update " + tableName + " set P=" + newp + ", Q=" + newq + " where inst_id='" + inst_id + "' and tick=" + tick;
		}
		executeUpdateQuery(psQuery);
	}
	
	/* (non-Javadoc)
	 * @see eu.cassandra.sim.utilities.DBResults#addExpectedPowerTick(int, java.lang.String, double, java.lang.String)
	 */
	@Override
	public void addExpectedPowerTick(int tick, String id, double p, String tableName) {
		String psQuery = "insert into " + tableName + " values ('" + id + "', " + tick + ", " + p + ", " + 0 + ")";
		executeUpdateQuery(psQuery);		
	}
	
	/**
	 * Get a tick result for the specified installation.
	 *
	  * @param tick the tick
	 * @param inst_id the installation id
	 * @param tableName the name of the table where the values are stored
	 * @return the tick result for the specified installation
	 */
	public ResultSet getTickResultForInstallation(int tick, String inst_id, String tableName) {
		String query = "SELECT * FROM " + tableName + " WHERE inst_id='" + inst_id + "' AND tick=" + tick;
		return executeSelectQuery(query);
	}
	
	/**
	 * Gets an expected power tick result for the specified installation.
	 *
	 * @param tick the tick
	 * @param inst_id the installation id
	 * @param tableName the name of the table where the values are stored
	 * @return the expected power tick result for the specified installation
	 */
	public ResultSet getExpectedPowerTickResultForInstallation(int tick, String inst_id, String tableName) {
		String query = "SELECT * FROM " + tableName + " WHERE inst_id='" + inst_id + "' AND tick=" + tick;
		return executeSelectQuery(query);
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.utilities.DBResults#addAggregatedTickResult(int, double, double, java.lang.String)
	 */
	@Override
	public void addAggregatedTickResult(int tick, double p, double q, String tableName) {
		double[] data = getData(tableName, tick, null, null);
		
		double newp = p;
		double newq = q;
		
		String psQuery = "";
		if(data == null) 
			psQuery = "insert into " + tableName + " values(" + tick + ", " + newp + ", " + newq + ")";
		else {
			newp += data[0];
			newq += data[1];
			psQuery = "update " + tableName + " set P=" + newp + ", Q=" + newq + " where tick=" + tick;
		}
		executeUpdateQuery(psQuery);
	}
	
	private Connection getConnection()
	{
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		String connectionURL = "jdbc:derby:" + dbname + ";create=true";	// define the Derby connection URL to use
		Connection conn = null;
		
		try {
			Class.forName(driver); // Load the Derby driver.
			System.out.println(driver + " loaded. ");
		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
			System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
		}
		try {
			// Create (if needed) and connect to the database
			conn = DriverManager.getConnection(connectionURL);
		} catch (Throwable e) {
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		return conn;
	}
	
	private void createTableAndIndex(String tableName, String idColName, boolean createIndex)
	{
		Statement s;
		String createString = "CREATE TABLE  " + tableName + "(";
		String createStringPK = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + tableName + "_PK ";
		String createIndexString = "CREATE INDEX " + tableName + "_INDEX ON " + tableName;
		if (idColName != null) 
		{
			createString += idColName + " VARCHAR(50) NOT NULL, ";
			createStringPK += "Primary Key (" + idColName + ", TICK)";
			createIndexString += "(" + idColName + ", TICK)";
		}
		else
		{
			createStringPK += "Primary Key (TICK)";
			createIndexString += " (TICK)" ;
		}
		createString += "TICK INTEGER NOT NULL, P DOUBLE PRECISION NOT NULL, Q DOUBLE PRECISION NOT NULL) ";	
			 
		try {
			// Create a statement to issue simple commands.
			s = conn.createStatement();	
			// Call utility method to check if table exists & create the table if needed
			if (!checkIfTableExists(conn, tableName)) {
//				System.out.println("Creating table " + tableName);
				s.execute(createString);
				s.execute(createStringPK);
				if (createIndex)
				{
//					System.out.println("Creating index " + tableName + "_INDEX");
					s.execute(createIndexString);
				}
			}
			s.close();
		} catch (Throwable e) {
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		
	}
	
	private void createKPITable(String tableName, String idColName, boolean addAvgPeak)
	{
		Statement s;
		String createString = "CREATE TABLE  " + tableName + "(" + idColName + " VARCHAR(50) NOT NULL, ";
		if (addAvgPeak)
			createString += "AVGPEAK DOUBLE PRECISION, ";
		createString += "MAXPOWER DOUBLE PRECISION NOT NULL, AVGPOWER DOUBLE PRECISION NOT NULL, ENERGY DOUBLE PRECISION NOT NULL, "
							+ " COST DOUBLE PRECISION NOT NULL,  CO2 DOUBLE PRECISION NOT NULL) ";	
		String createStringPK = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + tableName + "_PK Primary Key (" + idColName + ")";
			 
		try {
			// Create a statement to issue simple commands.
			s = conn.createStatement();	
			// Call utility method to check if table exists & create the table if needed
			if (!checkIfTableExists(conn, tableName)) {
//				System.out.println("Creating table " + tableName);
				s.execute(createString);
				s.execute(createStringPK);
			}
			s.close();
		} catch (Throwable e) {
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
	}
	
	private static boolean checkIfTableExists(Connection conTst, String tableName) throws SQLException {
		try {
			Statement s = conTst.createStatement();
			s.execute("update " + tableName + " set ENTRY_DATE = CURRENT_TIMESTAMP, WISH_ITEM = 'TEST ENTRY' where 1=3");
		} catch (SQLException sqle) {
			String theError = (sqle).getSQLState();
			/** If table exists will get - WARNING 02000: No row was found **/
			if (theError.equals("42X05")) // Table does not exist
			{
				return false;
			} 
			else if (theError.equals("42X14") || theError.equals("42821")) {
				System.out.println("WwdChk4Table: Incorrect table definition. Drop table WISH_LIST and rerun this program");
				throw sqle;
			} 
			else {
				System.out.println("WwdChk4Table: Unhandled SQLException");
				throw sqle;
			}
		}
		// System.out.println("Just got the warning - table exists OK ");
		return true;
	}
	
	private static void errorPrint(Throwable e) {
		if (e instanceof SQLException)
			SQLExceptionPrint((SQLException) e);
		else {
			System.out.println("A non SQL error occured.");
			e.printStackTrace();
		}
	}
	
	private static void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			System.out.println("\n---SQLException Caught---\n");
			System.out.println("SQLState:   " + (sqle).getSQLState());
			System.out.println("Severity: " + (sqle).getErrorCode());
			System.out.println("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	} 

	private void executeUpdateQuery(String query)
	{	
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(query);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
	}
	
	private ResultSet executeSelectQuery(String query)
	{	
		Statement s;
		ResultSet temp = null;
		try {
			s = conn.createStatement();
			temp = s.executeQuery(query);
		} catch (SQLException e) {
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		return temp;
	}
	
	private double[] getData(String tableName, int tick, String idColName, String idValue)
	{
		double[] pqData = null; 
		
		String sQuery = "";
		if (idColName != null)
			sQuery = "select * from " + tableName + " where tick=" + tick + " and " + idColName + "='" + idValue + "'";
		else
			sQuery = "select * from " + tableName + " where tick=" + tick;
		
		ResultSet temp = executeSelectQuery(sQuery);
		try {
			while (temp.next()) {
				pqData = new double[2];
				pqData[0] = temp.getDouble("P");
				pqData[1] = temp.getDouble("Q");
			}
			temp.getStatement().close();
		} catch (SQLException e) {
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}

		return pqData;
	}
	
	private double[] getKPIData(String tableName, String idColName, String idValue)
	{
		double[] pqData = null; 
		String sQuery = "select * from " + tableName + " where "+ idColName + "='" + idValue + "'";
		ResultSet temp = executeSelectQuery(sQuery);
		try {
			while (temp.next()) {
				pqData = new double[5];
				pqData[0] = temp.getDouble("MAXPOWER");
				pqData[1] = temp.getDouble("AVGPOWER");
				pqData[2] = temp.getDouble("ENERGY");
				pqData[3] = temp.getDouble("COST");
				pqData[4] = temp.getDouble("CO2");
			}

		} catch (SQLException e) {
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		return pqData;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.utilities.DBResults#getKPIs(java.lang.String)
	 */
	@Override
	public HashMap<String, Double> getKPIs(String inst_id) {
		HashMap<String, Double> result =  new  HashMap<String, Double>();
		String collection;
		String id;
		if(inst_id.equalsIgnoreCase(AGGR)) {
			id = AGGR;
			collection = COL_AGGRKPIS;
		} else {
			id = inst_id;
			collection = COL_INSTKPIS;
		}
		String sQuery = "select * from " + collection + " where inst_id ='" + id + "'";
		ResultSet temp = executeSelectQuery(sQuery);
		try {
			while (temp.next()) {
				result.put("Avg Peak (W)", temp.getDouble("AVGPEAK"));
				result.put("Max Power (W)", temp.getDouble("MAXPOWER"));
				result.put("Avg Power (W)",  temp.getDouble("AVGPOWER"));
				result.put("Energy (KWh)", temp.getDouble("ENERGY"));
				result.put("Cost (EUR)", temp.getDouble("COST"));
				result.put("CO2", temp.getDouble("CO2"));
			}
		} catch (SQLException e) {
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		return result;
		
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.utilities.DBResults#getAppKPIs(java.lang.String)
	 */
	@Override
	public HashMap<String, Double> getAppKPIs(String app_id) {
		HashMap<String, Double> result =  new  HashMap<String, Double>();
		String sQuery = "select * from " + DBResults.COL_APPKPIS + " where app_id='" + app_id + "'";
		ResultSet temp = executeSelectQuery(sQuery);
		try {
			while (temp.next()) {
				result.put("Max Power (W)", temp.getDouble("MAXPOWER"));
				result.put("Avg Power (W)",  temp.getDouble("AVGPOWER"));
				result.put("Energy (KWh)", temp.getDouble("ENERGY"));
				result.put("Cost (EUR)", temp.getDouble("COST"));
				result.put("CO2", temp.getDouble("CO2"));
			}

		} catch (SQLException e) {
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.cassandra.sim.utilities.DBResults#getActKPIs(java.lang.String)
	 */
	@Override
	public HashMap<String, Double> getActKPIs(String act_id) {
		HashMap<String, Double> result =  new  HashMap<String, Double>();
		String sQuery = "select * from " + DBResults.COL_ACTKPIS + " where act_id='" + act_id + "'";
		ResultSet temp = executeSelectQuery(sQuery);
		try {
			while (temp.next()) {
				result.put("Max Power (W)", temp.getDouble("MAXPOWER"));
				result.put("Avg Power (W)",  temp.getDouble("AVGPOWER"));
				result.put("Energy (KWh)", temp.getDouble("ENERGY"));
				result.put("Cost (EUR)", temp.getDouble("COST"));
				result.put("CO2", temp.getDouble("CO2"));
			}

		} catch (SQLException e) {
			System.out.println(" . . . exception thrown:");
			errorPrint(e);
		}
		return result;
	}
}
