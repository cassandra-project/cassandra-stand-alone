package eu.cassandra.sim.standalone;

import java.util.ArrayList;

import eu.cassandra.sim.entities.appliances.ConsumptionModel;
import eu.cassandra.sim.entities.appliances.Tripplet;

public class ConsumptionModelsLibrary {
	
	public static ConsumptionModel getConsumptionModelForWashingMachine(String type)
	{
		if (!type.equals("p") && !type.equals("q"))
		{
			System.err.println("non-existent consumption model");
			System.exit(15);
		}
		int outerN = 0;
		int numberOfPatters = 1;
		int[] n =new int[numberOfPatters];
		n[0] = 1;
		ArrayList[] patterns = new ArrayList[numberOfPatters];
		// create patterns
		Tripplet t = new Tripplet();
		if (type.equals("p"))
			t.setV(107.74000000000001);
		else if (type.equals("q"))
			t.setV(107.74000000000001);
		t.setD(10);
		t.setS(0);
		ArrayList tripplets1 = new ArrayList();
		tripplets1.add(t);
		patterns[0] = tripplets1;
		
		return new ConsumptionModel(outerN, n, patterns);
	}
	
	public static ConsumptionModel getConsumptionModelForLighting(String type)
	{
		if (!type.equals("p") && !type.equals("q"))
		{
			System.err.println("non-existent consumption model");
			System.exit(15);
		}
		int outerN = 0;
		int numberOfPatters = 1;
		int[] n =new int[numberOfPatters];
		n[0] = 1;
		ArrayList[] patterns = new ArrayList[numberOfPatters];
		// create patterns
		Tripplet t = new Tripplet();
		if (type.equals("p"))
			t.setV(18.16818181818182);
		else if (type.equals("q"))
			t.setV(-6.6377272727272745);
		t.setD(10);
		t.setS(0);
		ArrayList tripplets1 = new ArrayList();
		tripplets1.add(t);
		patterns[0] = tripplets1;
		
		return new ConsumptionModel(outerN,n, patterns);
	}
	
	public static ConsumptionModel getConsumptionModelForVacuumCleaner1(String type)
	{
		if (!type.equals("p") && !type.equals("q"))
		{
			System.err.println("non-existent consumption model");
			System.exit(15);
		}
		int outerN = 0;
		int numberOfPatters = 1;
		int[] n =new int[numberOfPatters];
		n[0] = 1;
		ArrayList[] patterns = new ArrayList[numberOfPatters];
		// create patterns
		Tripplet t = new Tripplet();
		if (type.equals("p"))
			t.setV(1193.8874999999998);
		else if (type.equals("q"))
			t.setV(-164.385);
		t.setD(10);
		t.setS(0);
		ArrayList tripplets1 = new ArrayList();
		tripplets1.add(t);
		patterns[0] = tripplets1;
		
		return new ConsumptionModel(outerN,n, patterns);
	}
	
	public static ConsumptionModel getConsumptionModelForVacuumCleaner2(String type)
	{
		if (!type.equals("p") && !type.equals("q"))
		{
			System.err.println("non-existent consumption model");
			System.exit(15);
		}
		int outerN = 0;
		int numberOfPatters = 1;
		int[] n =new int[numberOfPatters];
		n[0] = 1;
		ArrayList[] patterns = new ArrayList[numberOfPatters];
		// create patterns
		Tripplet t = new Tripplet();
		if (type.equals("p"))
			t.setV(1215.9583333333335);
		else if (type.equals("q"))
			t.setV(-152.03083333333336);
		t.setD(10);
		t.setS(0);
		ArrayList tripplets1 = new ArrayList();
		tripplets1.add(t);
		patterns[0] = tripplets1;
		
		return new ConsumptionModel(outerN,n, patterns);
	}
	
	public static ConsumptionModel getConsumptionModelForWaterHeater(String type)
	{
		if (!type.equals("p") && !type.equals("q"))
		{
			System.err.println("non-existent consumption model");
			System.exit(15);
		}
		int outerN = 0;
		int numberOfPatters = 1;
		int[] n =new int[numberOfPatters];
		n[0] = 1;
		ArrayList[] patterns = new ArrayList[numberOfPatters];
		// create patterns
		Tripplet t = new Tripplet();
		if (type.equals("p"))
			t.setV(4264.666666666667);
		else if (type.equals("q"))
			t.setV(-559.4066666666666);
		t.setD(10);
		t.setS(0);
		ArrayList tripplets1 = new ArrayList();
		tripplets1.add(t);
		patterns[0] = tripplets1;
		
		return new ConsumptionModel(outerN,n, patterns);
	}

}
