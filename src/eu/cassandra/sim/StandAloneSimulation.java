package eu.cassandra.sim;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import eu.cassandra.sim.entities.appliances.Appliance;
import eu.cassandra.sim.entities.appliances.ConsumptionModel;
import eu.cassandra.sim.entities.appliances.Tripplet;
import eu.cassandra.sim.entities.installations.Installation;
import eu.cassandra.sim.entities.people.Activity;
import eu.cassandra.sim.entities.people.Person;
import eu.cassandra.sim.math.Gaussian;
import eu.cassandra.sim.math.Histogram;
import eu.cassandra.sim.math.ProbabilityDistribution;
import eu.cassandra.sim.standalone.ConsumptionModelsLibrary;
import eu.cassandra.sim.standalone.DistributionsLibrary;
import eu.cassandra.sim.utilities.Constants;

public class StandAloneSimulation {
		
	public static void main(String[] args)
	{	
		String aresources_path = "/Users/fanitzima";
		int seed = 171181;
		Simulation sim = new  Simulation(aresources_path, "2Persons"+System.currentTimeMillis(), seed);
		
		

  		try {
			sim.setupStandalone(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//  		System.out.println("Simulation setup finished");
//		sim.run();
  		sim.runStandAlone();
	}
	

}
