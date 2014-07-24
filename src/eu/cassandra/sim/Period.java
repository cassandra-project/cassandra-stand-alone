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
 * A from-to-price tripplet defining a period of specific pricing, used in pricing policies.
 */
public class Period {

	/** The starting hour of the period. */
	private String from;

	/** The ending hour of the period. */
	private String to;

	/** The price of the period. */
	private double price;

	/**
	 * Instantiates a new period of with a specific price.
	 *
	 * @param afrom the starting hour of the period
	 * @param ato the ending hour of the period
	 * @param aprice the price of the period
	 */
	public Period(String afrom, String ato, double aprice) {
		from = afrom;
		to = ato;
		price = aprice;
	}

	/**
	 * Gets the starting hour of the period.
	 *
	 * @return the starting hour of the period
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * Gets the ending hour of the period.
	 *
	 * @return the ending hour of the period
	 */
	public String getTo() {
		return to;
	}

	/**
	 * Sets the ending hour of the period.
	 *
	 * @param ato the new ending hour of the period
	 */
	public void setTo(String ato) {
		to = ato;
	}

	/**
	 * Gets the price of the period.
	 *
	 * @return the price of the period
	 */
	public double getPrice() {
		return price;
	}

}
