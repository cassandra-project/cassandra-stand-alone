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

/**
 * The simulation's parameter set.
 * 
 * @author Kyriakos C. Chatzidimitriou (kyrcha [at] iti [dot] gr)
 * @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 * 
 */
public class SimulationParams
{

  /** The simulation's calendar. */
  private SimCalendar simCalendar;
  
  /** The parameter set name. */
  private String name;
  
  /** The location info. */
  private String locationInfo;
  
  /** The response type. */
  private String responseType;
  
  /** The number of Monte-Carlo runs. */
  private int mcruns;
  
  /** The the CO2 factor for the simulation. */
  private double co2;
  
  /** The number of days the simulation will run for. */
  private int numOfDays;
  
  /** The scenario setup type (static or dynamic). */
  private String setup;

  /**
   * Instantiates a new simulation parameter set.
   *
   * @param responseType the response type
   * @param name the name
   * @param locationInfo the location info
   * @param duration the duration (in days)
   * @param startDateDay the start-date day
   * @param startDateMonth the start-date month
   * @param startDateYear the start-date year
   * @param mcruns the number of Monte-Carlo runs
   * @param co2 the CO2 factor
   * @param setup the scenario setup type (static or dynamic)
   */
  public SimulationParams(String responseType, String name, String locationInfo, int duration, int startDateDay,  int startDateMonth, int startDateYear, 
		  int mcruns, double co2, String setup)
  {
	  this.responseType = responseType;
	  this.name = name;
	  this.locationInfo = locationInfo;
	  this.numOfDays = duration;
	  
	  simCalendar = new SimCalendar(startDateDay, startDateMonth, startDateYear, duration);
	  
	  this.mcruns = mcruns;
	  this.co2 = co2;
	  this.setup = setup;
  }

  /**
   * Gets the simulation's calendar.
   *
   * @return the simulation's calendar
   */
  public SimCalendar getSimCalendar ()
  {
    return simCalendar;
  }

  /**
   * Gets parameter set name.
   *
   * @return the parameter set name
   */
  public String getName ()
  {
    return name;
  }

  /**
   * Gets the response type.
   *
   * @return the response type
   */
  public String getResponseType ()
  {
    return responseType;
  }
  
  /**
   * Gets the location info.
   *
   * @return the location info
   */
  public String getLocationInfo ()
  {
    return locationInfo;
  }

/**
 * Gets the number of Monte-Carlo runs.
 *
 * @return the number of Monte-Carlo runs
 */
public int getMcruns() {
	return mcruns;
}

/**
 * Gets the CO2 factor.
 *
 * @return the CO2 factor
 */
public double getCo2() {
	return co2;
}

/**
 * Gets the number of days.
 *
 * @return the number of days
 */
public int getNumOfDays() {
	return numOfDays;
}

/**
 * Sets the number of Monte-Carlo runs.
 *
 * @param mcruns the new number of Monte-Carlo runs
 */
public void setMcruns(int mcruns) {
	this.mcruns = mcruns;
}

/**
 * Sets the CO2 factor.
 *
 * @param co2 the new CO2 factor
 */
public void setCo2(double co2) {
	this.co2 = co2;
}

/**
 * Sets the number of days.
 *
 * @param numOfDays the new number of days
 */
public void setNumOfDays(int numOfDays) {
	this.numOfDays = numOfDays;
}

/**
 * Gets the scenario setup type.
 *
 * @return the scenario setup type (static or dynamic)
 */
public String getSetup() {
	return setup;
}

/**
 * Sets the scenario setup type.
 *
 * @param setup the new scenario setup type (static or dynamic)
 */
public void setSetup(String setup) {
	this.setup = setup;
}

}
