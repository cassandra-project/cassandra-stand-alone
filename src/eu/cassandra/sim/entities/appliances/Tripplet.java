package eu.cassandra.sim.entities.appliances;

public class Tripplet {
	double v, s;
	
	int d;
	
	public double getV() {
		return v;
	}
	public double getS() {
		return s;
	}
	public int getD() {
		return d;
	}
	public void setV(double v) {
		this.v = v;
	}
	public void setS(double s) {
		this.s = s;
	}
	public void setD(int d) {
		this.d = d;
	}
	
	public Tripplet() {
		v = s = 0;
		d = 0;
	}
}
