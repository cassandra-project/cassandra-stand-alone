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

import java.util.Calendar;
import java.util.Date;

/**
 * The simulation's calendar.
 * 
 * @author Kyriakos C. Chatzidimitriou (kyrcha [at] iti [dot] gr)
 * 
 */
public class SimCalendar {
	
	/** The calendar. */
	private Calendar myCalendar;
	
	/** The calendar's base date. */
	private Date base;
	
	/** The calendar's granularity. */
	private String granularity = "Minute";
	
	/** The calendar's granularity value. */
	private int granularityValue = 1;
	
	/** The calendar's duration. */
	private int duration = 0;

	/** The Constant ABBR_DAYS. */
	private static final String[] ABBR_DAYS = { "NA", "Sun", "Mon", "Tue",
			"Wed", "Thu", "Fri", "Sat" };

	/**
	 * Instantiates a new simulation calendar.
	 *
	 * @param day the day
	 * @param month the month
	 * @param year the year
	 * @param duration the duration
	 */
	public SimCalendar(int day, int month, int year, int duration) {

		myCalendar = Calendar.getInstance();

		myCalendar.set(year, month - 1, day, 0, 0, 0);
		base = myCalendar.getTime();
		this.duration = duration;

	}

	/**
	 * Gets the calendar.
	 *
	 * @return the calendar
	 */
	public Calendar getMyCalendar() {
		return myCalendar;
	}

	/**
	 * Gets the calendar's base date.
	 *
	 * @return the calendar's base date
	 */
	public Date getBase() {
		return base;
	}

	/**
	 * Gets the calendar's granularity.
	 *
	 * @return the calendar's granularity
	 */
	public String getGranularity() {
		return granularity;
	}

	/**
	 * Gets the calendar's granularity (in raw format).
	 *
	 * @return the calendar's granularity (in raw format).
	 */
	public int getGranularityRaw() {

		switch (granularity) {
		case "Minute":

			return Calendar.MINUTE;

		case "Hour":

			return Calendar.HOUR;

		case "Day":

			return Calendar.DAY_OF_YEAR;

		case "Week":

			return Calendar.WEEK_OF_YEAR;

		case "Month":

			return Calendar.MONTH;

		}

		return 0;

	}

	/**
	 * Gets the calendar's granularity value.
	 *
	 * @return the calendar'sgranularity value
	 */
	public int getGranularityValue() {
		return granularityValue;
	}

	/**
	 * Copy a calendar.
	 *
	 * @param cal the calendar to copy
	 * @return the copied calendar
	 */
	private Calendar copyCal(Calendar cal) {
		Calendar temp = Calendar.getInstance();
		temp.set(myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
				myCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		return temp;
	}

	/**
	 * Checks if it is weekend.
	 *
	 * @param tick the current tick
	 * @return true, if it is weekend
	 */
	public boolean isWeekend(int tick) {
		Calendar temp = copyCal(myCalendar);
		int gran = getGranularityRaw();
		temp.add(gran, tick * granularityValue);
		int day = temp.get(Calendar.DAY_OF_WEEK);
		if (day == Calendar.SATURDAY || day == Calendar.SUNDAY)
			return true;
		return false;
	}

	/**
	 * Gets the current date.
	 *
	 * @param tick the current tick
	 * @return the current date
	 */
	public String getCurrentDate(int tick) {
		Calendar temp = copyCal(myCalendar);
		int gran = getGranularityRaw();
		temp.add(gran, tick * granularityValue);
		int day = temp.get(Calendar.DAY_OF_MONTH);
		int month = temp.get(Calendar.MONTH) + 1;
		return day + "/" + month;
	}

	/**
	 * Gets the current day of week.
	 *
	 * @param tick the current tick
	 * @return the current day of week
	 */
	public String getDayOfWeek(int tick) {
		Calendar temp = copyCal(myCalendar);
		int gran = getGranularityRaw();
		temp.add(gran, tick * granularityValue);
		return ABBR_DAYS[temp.get(Calendar.DAY_OF_WEEK)];
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String temp = "Base: " + base + " Granularity: " + granularity
				+ " Granularity Value: " + granularityValue + " Duration: "
				+ duration;
		return temp;
	}

}