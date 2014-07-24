package eu.cassandra.sim.entities.appliances;

/**
 * Pattern of a consumption model as a tripplet: active/reactive power value, 
 * duration in minutes and slope.
 *
 *  @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 * 
 */
public class Tripplet {
	/** The active/reactive power value */
	double v;
	/** The slope */
	double s;
	/** The duration in minutes */
	int d;
	
	/**
	 * Gets the active/reactive power value.
	 *
	 * @return the active/reactive power value
	 */
	public double getV() {
		return v;
	}
	
	/**
	 * Gets the slope.
	 *
	 * @return the slope
	 */
	public double getS() {
		return s;
	}
	
	/**
	 * Gets the duration in minutes.
	 *
	 * @return the duration in minutes
	 */
	public int getD() {
		return d;
	}
	
	/**
	 * Sets the active/reactive power value.
	 *
	 * @param v the new active/reactive power value
	 */
	public void setV(double v) {
		this.v = v;
	}
	
	/**
	 * Sets the slope.
	 *
	 * @param s the new slope
	 */
	public void setS(double s) {
		this.s = s;
	}
	
	/**
	 * Sets the duration in minutes.
	 *
	 * @param d the new duration in minutes
	 */
	public void setD(int d) {
		this.d = d;
	}
	
	/**
	 * Instantiates a new tripplet.
	 */
	public Tripplet() {
		v = s = 0;
		d = 0;
	}
}
