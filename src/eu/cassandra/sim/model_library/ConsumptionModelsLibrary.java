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
package eu.cassandra.sim.model_library;

import java.util.ArrayList;

import eu.cassandra.sim.entities.appliances.ConsumptionModel;
import eu.cassandra.sim.entities.appliances.Tripplet;

/**
 * Class with various ready-to-use consumption models.
 * 
 *  @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 * 
 */
public class ConsumptionModelsLibrary {
	
	public static ConsumptionModel getConsumptionModelForWashingMachine(String type)
	{
		if (!type.equals("p") && !type.equals("q"))
		{
			System.err.println("non-existent consumption model");
			System.exit(15);
		}
//		int outerN = 0;
//		int numberOfPatters = 1;
//		int[] n =new int[numberOfPatters];
//		n[0] = 1;
//		ArrayList[] patterns = new ArrayList[numberOfPatters];
//		// create patterns
//		Tripplet t = new Tripplet();
//		if (type.equals("p"))
//			t.setV(107.74000000000001);
//		else if (type.equals("q"))
//			t.setV(107.74000000000001);
//		t.setD(10);
//		t.setS(0);
//		ArrayList tripplets1 = new ArrayList();
//		tripplets1.add(t);
//		patterns[0] = tripplets1;
//		
//		return new ConsumptionModel(outerN, n, patterns);
		
		if (type.equals("p"))
		{
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"p\":107.74000000000001,\"d\":10,\"s\":0}]}]}";
			try {
				return new ConsumptionModel(message, "p");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		else if (type.equals("q"))
		{
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"q\":107.74000000000001,\"d\":10,\"s\":0}]}]}";
			try {
				return new ConsumptionModel(message, "q");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static ConsumptionModel getConsumptionModelForLighting(String type)
	{
		if (!type.equals("p") && !type.equals("q"))
		{
			System.err.println("non-existent consumption model");
			System.exit(15);
		}
//		int outerN = 0;
//		int numberOfPatters = 1;
//		int[] n =new int[numberOfPatters];
//		n[0] = 1;
//		ArrayList[] patterns = new ArrayList[numberOfPatters];
//		// create patterns
//		Tripplet t = new Tripplet();
//		if (type.equals("p"))
//			t.setV(18.16818181818182);
//		else if (type.equals("q"))
//			t.setV(-6.6377272727272745);
//		t.setD(10);
//		t.setS(0);
//		ArrayList tripplets1 = new ArrayList();
//		tripplets1.add(t);
//		patterns[0] = tripplets1;
//		
//		return new ConsumptionModel(outerN,n, patterns);
		
		if (type.equals("p"))
		{
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"p\":18.16818181818182,\"d\":10,\"s\":0}]}]}";
			try {
				return new ConsumptionModel(message, "p");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		else if (type.equals("q"))
		{
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"q\":-6.6377272727272745,\"d\":10,\"s\":0}]}]}";
			try {
				return new ConsumptionModel(message, "q");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static ConsumptionModel getConsumptionModelForVacuumCleaner1(String type)
	{
		if (!type.equals("p") && !type.equals("q"))
		{
			System.err.println("non-existent consumption model");
			System.exit(15);
		}
//		int outerN = 0;
//		int numberOfPatters = 1;
//		int[] n =new int[numberOfPatters];
//		n[0] = 1;
//		ArrayList[] patterns = new ArrayList[numberOfPatters];
//		// create patterns
//		Tripplet t = new Tripplet();
//		if (type.equals("p"))
//			t.setV(1193.8874999999998);
//		else if (type.equals("q"))
//			t.setV(-164.385);
//		t.setD(10);
//		t.setS(0);
//		ArrayList tripplets1 = new ArrayList();
//		tripplets1.add(t);
//		patterns[0] = tripplets1;
//		
//		return new ConsumptionModel(outerN,n, patterns);
		
		if (type.equals("p"))
		{
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"p\":1193.8874999999998,\"d\":10,\"s\":0}]}]}";
			try {
				return new ConsumptionModel(message, "p");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		else if (type.equals("q"))
		{
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"q\":-164.385,\"d\":10,\"s\":0}]}]}";
			try {
				return new ConsumptionModel(message, "q");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static ConsumptionModel getConsumptionModelForVacuumCleaner2(String type)
	{
		if (!type.equals("p") && !type.equals("q"))
		{
			System.err.println("non-existent consumption model");
			System.exit(15);
		}
//		int outerN = 0;
//		int numberOfPatters = 1;
//		int[] n =new int[numberOfPatters];
//		n[0] = 1;
//		ArrayList[] patterns = new ArrayList[numberOfPatters];
//		// create patterns
//		Tripplet t = new Tripplet();
//		if (type.equals("p"))
//			t.setV(1215.9583333333335);
//		else if (type.equals("q"))
//			t.setV(-152.03083333333336);
//		t.setD(10);
//		t.setS(0);
//		ArrayList tripplets1 = new ArrayList();
//		tripplets1.add(t);
//		patterns[0] = tripplets1;
//		
//		return new ConsumptionModel(outerN,n, patterns);
		
		if (type.equals("p"))
		{
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"p\":1215.9583333333335,\"d\":10,\"s\":0}]}]}";
			try {
				return new ConsumptionModel(message, "p");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		else if (type.equals("q"))
		{
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"q\":-152.03083333333336,\"d\":10,\"s\":0}]}]}";
			try {
				return new ConsumptionModel(message, "q");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static ConsumptionModel getConsumptionModelForWaterHeater(String type)
	{
		if (!type.equals("p") && !type.equals("q"))
		{
			System.err.println("non-existent consumption model");
			System.exit(15);
		}
//		int outerN = 0;
//		int numberOfPatters = 1;
//		int[] n =new int[numberOfPatters];
//		n[0] = 1;
//		ArrayList[] patterns = new ArrayList[numberOfPatters];
//		// create patterns
//		Tripplet t = new Tripplet();
//		if (type.equals("p"))
//			t.setV(4264.666666666667);
//		else if (type.equals("q"))
//			t.setV(-559.4066666666666);
//		t.setD(10);
//		t.setS(0);
//		ArrayList tripplets1 = new ArrayList();
//		tripplets1.add(t);
//		patterns[0] = tripplets1;
//		
//		return new ConsumptionModel(outerN,n, patterns);
		
		if (type.equals("p"))
		{
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"p\":4264.666666666667,\"d\":10,\"s\":0}]}]}";
			try {
				return new ConsumptionModel(message, "p");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		else if (type.equals("q"))
		{
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"q\":-559.4066666666666,\"d\":10,\"s\":0}]}]}";
			try {
				return new ConsumptionModel(message, "q");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	
	public static void main(String[] args)
	{   
		
		try {
			ConsumptionModel test = ConsumptionModelsLibrary.getConsumptionModelForLighting("p");
			String message = "{\"n\":0,\"params\":[{\"n\":1,\"values\":[{\"p\":18.16818181818182,\"d\":10,\"s\":0}]}]}";   
			ConsumptionModel test2 = new ConsumptionModel(message, "p");
			System.out.println("Compared patters are the same: " + compareConsumptionModels(test, test2));
						
			int outerN = 1;
			int numberOfPatters = 2;
			int[] n =new int[numberOfPatters];
			n[0] = 1;
			n[1] = 0;
			ArrayList[] patterns = new ArrayList[numberOfPatters];
			// create patterns
			Tripplet t = new Tripplet();
			t.setV(1900);
			t.setD(1);
			t.setS(0);
			ArrayList tripplets1 = new ArrayList();
			tripplets1.add(t);
			patterns[0] = tripplets1;
			t = new Tripplet();
			t.setV(300);
			t.setD(1);
			t.setS(0);
			tripplets1 = new ArrayList();
			tripplets1.add(t);
			patterns[1] = tripplets1;
			test = new ConsumptionModel(outerN, n, patterns);
			
			message = "{\"n\":1,\"params\":[{\"n\":1,\"values\":[{\"p\":1900,\"d\":1,\"s\":0}]},{\"n\":0,\"values\":[{\"p\":300,\"d\":1,\"s\":0}]}]}";
			test2 = new ConsumptionModel(message, "p");
			System.out.println("Compared patters are the same: " + compareConsumptionModels(test, test2));
			 
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private static boolean compareConsumptionModels(ConsumptionModel test, ConsumptionModel test2)
	{
		boolean theSame = true;
		if (test.getOuterN() !=  test2.getOuterN())
			return false;
		if (test.getTotalDuration() != test2.getTotalDuration())
			return false;
		for (int i=0; i<test.getPatterns().length; i++)
		{
			if (test.getN(i) !=  test2.getN(i))
				return false;
			if (test.getPatternDuration(i) != test2.getPatternDuration(i))
				return false;
			for (int j=0; j<test.getPattern(i).size(); j++) 
			{
				if (test.getPattern(i).get(j).getD() != test2.getPattern(i).get(j).getD()) 
					return false;
				if (test.getPattern(i).get(j).getS() != test2.getPattern(i).get(j).getS())
					return false;
				if (test.getPattern(i).get(j).getV() != test2.getPattern(i).get(j).getV())
					return false;
			}
		}
		return theSame;
	}
	
}


