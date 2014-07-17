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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.cassandra.sim.utilities.Utils;

/**
 * The Simulation class can simulate up to 4085 years of simulation.
 * 
 * @author Kyriakos C. Chatzidimitriou (kyrcha [at] iti [dot] gr)
 * 
 */
public class SimulationParams
{

  private SimCalendar simCalendar;
  private String name;
  private String locationInfo;
  private String responseType;
  
  private int mcruns;
  private double co2;
  private int numOfDays;
  private String setup;


  public SimulationParams(String responseType, String name, String locationInfo, int duration, int startDateDay,  int startDateMonth, int startDateYear)
  {
	  this.responseType = responseType;
	  this.name = name;
	  this.locationInfo = locationInfo;
	  this.numOfDays = duration;
	  
	  simCalendar = new SimCalendar(startDateDay, startDateMonth, startDateYear, duration);
  }
  
  
  public SimCalendar getSimCalendar ()
  {
    return simCalendar;
  }

  public String getName ()
  {
    return name;
  }

  public String getResponseType ()
  {
    return responseType;
  }
  
  public String getLocationInfo ()
  {
    return locationInfo;
  }

public int getMcruns() {
	return mcruns;
}

public double getCo2() {
	return co2;
}

public int getNumOfDays() {
	return numOfDays;
}

public void setMcruns(int mcruns) {
	this.mcruns = mcruns;
}

public void setCo2(double co2) {
	this.co2 = co2;
}

public void setNumOfDays(int numOfDays) {
	this.numOfDays = numOfDays;
}

public String getSetup() {
	return setup;
}

public void setSetup(String setup) {
	this.setup = setup;
}

//  /**
//   * @param args
//   * @throws IOException
//   * @throws ParseException
//   */
//  public static void main (String[] args) throws IOException, ParseException
//  {
//    String s = Utils.readFile("simparam.json");
//
//    DBObject obj = (DBObject) JSON.parse(s);
//
//    SimulationParams sp = new SimulationParams(obj);
//
//    System.out.println("Name:" + sp.getName());
//    System.out.println("Location Info:" + sp.getLocationInfo());
//    System.out.println("SimCalendar:" + sp.getSimCalendar().toString());
//  }

}
