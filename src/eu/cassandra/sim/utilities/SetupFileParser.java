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
*/package eu.cassandra.sim.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * 
 * 
 *  @author Fani A. Tzima (fani [dot] tzima [at] iti [dot] gr)
 * 
 */

public class SetupFileParser {
	
	public Properties generalProps = new Properties();
	public Properties propPricing = new Properties();
	public Properties propPricingBaseline = new Properties();
	public Properties propSimulation = new Properties();
	public Properties propScenario = new Properties();
	public Vector<Properties> propInstallations = new Vector<Properties>();
	public Vector<Properties> propPeople = new Vector<Properties>();
	public Vector<Properties> propConsModels = new Vector<Properties>();
	public Vector<Properties> propAppliances = new Vector<Properties>();
	public Vector<Properties> propActivities = new Vector<Properties>();
	public Vector<Properties> propActModels = new Vector<Properties>();
	public Properties demographics = new Properties();
	
	Hashtable<String, String[]> propRequired = new Hashtable<String, String[]>();
	
	boolean foundSimulationSegment = false;
	boolean foundScenarioSegment = false;
	boolean foundPricingSegment = false;
	boolean foundPricingBaselineSegment = false;
	
	public SetupFileParser() {
		
		String[] requiredPropertiesGeneral = {}; //{ "seed", "useDerby", "printKPIs"};
		propRequired.put("general_properties", requiredPropertiesGeneral);
		
		String[] requiredPropertiesDemographics= { "name", "number_of_entities", "participation_probs_installations", "participation_probs_persons", "participation_probs_apps"};
		propRequired.put("demographics", requiredPropertiesDemographics); 
		
		String[] requiredPropertiesScenario = { "name", "setup"};
		propRequired.put("scenario", requiredPropertiesScenario);
		
		String[] requiredPropertiesSimulation = { "name", "response_type"};
		propRequired.put("simulation", requiredPropertiesSimulation);
		
		String[] requiredPropertiesPricing = { "name", "type", "billingCycle", "fixedCharge"};
		propRequired.put("pricing_policy", requiredPropertiesPricing);
		propRequired.put("pricing_policy_baseline", requiredPropertiesPricing);
		
		String[] requiredPropertiesPricingTOU = { "timezones", "prices"};
		propRequired.put("TOUPricing", requiredPropertiesPricingTOU);
		
		String[] requiredPropertiesPricingSEP = { "levels", "prices"};
		propRequired.put("ScalarEnergyPricing", requiredPropertiesPricingSEP);
		
		String[] requiredPropertiesPricingSEPTZ = { "offpeakPrice", "offpeakHours", "levels", "prices"};
		propRequired.put("ScalarEnergyPricingTimeZones", requiredPropertiesPricingSEPTZ);
		
		String[] requiredPropertiesPricingEPP = { "contractedCapacity", "energyPrice", "powerPrice"};
		propRequired.put("EnergyPowerPricing", requiredPropertiesPricingEPP);

		String[] requiredPropertiesPricingMPP = { "energyPrice", "powerPrice"};
		propRequired.put("MaximumPowerPricing", requiredPropertiesPricingMPP);
		
		String[] requiredPropertiesPricingAIP = { "fixedCost", "additionalCost", "contractedEnergy"};
		propRequired.put("AllInclusivePricing", requiredPropertiesPricingAIP);
		
		String[] requiredPropertiesInstallation = { "id", "name"};
		propRequired.put("installation", requiredPropertiesInstallation);
		
		String[] requiredPropertiesPerson = {"id", "name", "installation"};
		propRequired.put("person", requiredPropertiesPerson);
		
		String[] requiredPropertiesConsModel = {"id", "name", "pmodel", "qmodel"};
		propRequired.put("consumption_model", requiredPropertiesConsModel);
		
		String[] requiredPropertiesAppliance = {"id", "name", "installation", "consumption_model"};
		propRequired.put("appliance", requiredPropertiesAppliance);
		
		String[] requiredPropertiesActivity = {"name", "person", "id"};
		propRequired.put("activity", requiredPropertiesActivity);
		
		String[] requiredPropertiesActModel = {"id", "name", "activity", "containsAppliances", "day_type", "duration_distrType", "start_distrType", "repetitions_distrType"};
		propRequired.put("activity_model", requiredPropertiesActModel);
		
	}
	
	public void parseFileForProperties(String filename)
	{
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			String segment = "";
			String segmentTitle = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith("-->"))
				{
					if (!segment.equals(""))
					{
						parseSegment(segmentTitle, segment);
						segment = "";
					}
					segmentTitle = line.replace("-->", "").trim();
				}
				else
					segment += line + "\n";
			}

			if (!segment.equals(""))
			{
				parseSegment(segmentTitle, segment);
			}

			br.close();
			
			if (this.propSimulation.isEmpty())
				throw new Exception("ERROR: required \"simulation\" segment missing from the setup file");
			
			if (this.propScenario.isEmpty())
				throw new Exception("ERROR: required \"scenario\" segment missing from the setup file");
			
			if (this.propInstallations.isEmpty())
				throw new Exception("ERROR: at least one \"installation\" segment must be present in the setup file");
			
			checkResponseType(propSimulation.getProperty("response_type"), propPricingBaseline, propPricing);
			
			
		
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage());
			System.exit(0);
		}
	}
	
	private void parseSegment(String segmentTitle, String segment) throws Exception
	{
		Properties prop = new Properties();
		switch(segmentTitle) {
		case "demographics":
			prop = this.demographics;
			break;
		case "general_properties":
			prop = this.generalProps;
			break;
		case "simulation":
			if (foundSimulationSegment)
				throw new Exception("ERROR: only one \"simulation\" segment allowed per setup file");
			foundSimulationSegment = true;
			prop = this.propSimulation;
			break;
		case "scenario" :
			if (foundScenarioSegment)
				throw new Exception("ERROR: only one \"scenario\" segment allowed per setup file");
			foundScenarioSegment = true;
			prop = this.propScenario;
			break;
		case "installation" :
			this.propInstallations.add(prop);
			break;
		case "person" :
			this.propPeople.add(prop);
			break;
		case "consumption_model" :
			this.propConsModels.add(prop);
			break;
		case "appliance" :
			this.propAppliances.add(prop);
			break;
		case "activity" :
			this.propActivities.add(prop);
			break;
		case "activity_model" :
			this.propActModels.add(prop);
			break;
		case "pricing_policy" :
			if (foundPricingSegment)
				throw new Exception("ERROR: only one \"pricing_policy\" segment allowed per setup file");
			prop = this.propPricing;
			foundPricingSegment = true;
			break;
		case "pricing_policy_baseline" :
			if (foundPricingBaselineSegment)
				throw new Exception("ERROR: only one \"pricing_policy_baseline\" segment allowed per setup file");
			prop = this.propPricingBaseline;
			foundPricingBaselineSegment = true;
			break;
		default:
			System.out.println("INFO: Unknown segment \"" + segmentTitle + "\" ignored." + "\n");
			return;
		}
		
		prop.clear();
		prop.load(new ByteArrayInputStream(segment.getBytes()));
		prop.put("segment_title", segmentTitle);
		
		String[] requiredProperties = propRequired.get(segmentTitle);
		for(String property : requiredProperties) {
		   if (prop.get(property) == null)
			   throw new Exception("ERROR: required property \"" + property + "\" missing for segment \"" + segmentTitle + "\"");
		}
		
		if (segmentTitle.equals("pricing_policy") || segmentTitle.equals("pricing_policy_baseline"))
		{
			String type = prop.getProperty("type");
			String[] requiredProperties2 = propRequired.get(type); 
			if (requiredProperties2 == null)
				 throw new Exception("ERROR: unkown pricing policy type \"" + type + "\" employed");
			for(String property : requiredProperties2) {
			   if (prop.get(property) == null)
				   throw new Exception("ERROR: required property \"" + property + "\" missing for pricing policy of type \"" + type + "\"");
			}
		}
		
		if (segmentTitle.equals("activity_model"))
		{
			String duration_distrType = prop.getProperty("duration_distrType").trim();
			checkDistribution(duration_distrType, prop, "duration");
			String start_distrType = prop.getProperty("start_distrType");
			checkDistribution(start_distrType, prop, "start");
			String repetitions_distrType = prop.getProperty("repetitions_distrType");
			checkDistribution(repetitions_distrType, prop, "repetitions");
		}
	}
	
	private void checkResponseType(String responseType, Properties propBP, Properties propP) throws Exception
	{
		if (responseType.equals("Discrete") || responseType.equals("Optimal") || responseType.equals("Normal")) 
		{
			if (propBP.get("type") == null)
				   throw new Exception("ERROR: baseline pricing scheme undefined  for demand response scenario of type \"" + responseType + "\"");
			if (propP.get("type") == null)
				 throw new Exception("ERROR: pricing scheme undefined  for demand response scenario of type \"" + responseType + "\"");
		}
		else if (responseType.equals("None")) 
		{
			if (propBP.get("type") != null)
				  System.err.println("WARNING: baseline pricing scheme ignored for scenarios with response type \"" + responseType + "\".");
		}
		else
			throw new Exception("ERROR: unkown response type \"" + responseType + "\" employed");
	}
	
	private void checkDistribution(String distrType, Properties prop, String distrCase) throws Exception
	{
		if (distrType.equals("Normal Distribution") || distrType.equals("Uniform Distribution") || distrType.equals("Gaussian Mixture Models")) 
		{
			if (prop.get(distrCase + "_parameters") == null)
				   throw new Exception("ERROR: required property \"" + distrCase + "_parameters\" missing for distribution of type \"" + distrType + "\"");
		}
		else if (distrType.equals("Histogram")) 
		{
			if (prop.get(distrCase + "_values") == null)
				   throw new Exception("ERROR: required property \"" + distrCase + "_values\" missing for distribution of type \"" + distrType + "\"");
		}
		else
			throw new Exception("ERROR: unkown distribution type \"" + distrType + "\" employed");
	}


}
