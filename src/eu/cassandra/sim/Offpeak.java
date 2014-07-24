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
 * A from-to pair defining an offpeak period of pricing, used in pricing policies.
 */
public class Offpeak {

	/** The starting hour of the offpeak period. */
	private String from;

	/** The ending hour of the offpeak period. */
	private String to;

	/**
	 * Instantiates a new offpeak period of pricing.
	 *
	 * @param afrom the starting hour of the offpeak period
	 * @param ato the ending hour of the offpeak period
	 */
	public Offpeak(String afrom, String ato) {
		from = afrom;
		to = ato;
	}

	/**
	 * Gets the starting hour of the offpeak period.
	 *
	 * @return the starting hour of the offpeak period
	 */
	public String getFrom() { return from; }

	/**
	 * Gets the ending hour of the offpeak period.
	 *
	 * @return the ending hour of the offpeak period
	 */
	public String getTo() { return to; }

}
