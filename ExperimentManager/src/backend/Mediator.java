package backend;
import configuration.*;
import model.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.eclipse.core.resources.ResourcesPlugin;

public class Mediator {
	private OntologyManager ontologyManager;
	private FeatureModelManager featureModelManager;
	private static Configuration configuration; //The current configuration
	private FeatureModel featureModel; //The current feature model
	private static String currentPhase;
	String[] componentInformation;
	LinkedList<Feature> featureList;
	private static Path path;
	public static int phaseCounter = 1;
	public static int tempCounter = 0;
		
	//Instantiate a new mediator class
	public Mediator(){

	}
	
	public Configuration getConfiguration(){
		return configuration;
	}
	
	public String getCurrentPhase(){
		return currentPhase;
	}
	// Function that runs the whole phase and returns an XML string
	public String runPhase(String phase){
		//Load the Ontology and the Feature model managers
		ontologyManager = new OntologyManager();
		featureModelManager = new FeatureModelManager();
		configuration = new Configuration(featureModelManager.getFeatureModel());
		
		String xmlDescription = "";
		currentPhase = phase;

		//Obtain the current component information from the ontology manager
		componentInformation = ontologyManager.obtainComponentInfo(phase);
		//Load the current configuration
		loadConfiguration();
		//Obtain the current features from the feature model manager if needed
		if(componentInformation[1].equals("FeatureModel")){
			featureList = featureModelManager.obtainFeatures(phase, configuration);
		}

		//Generate screen
		//Update configuration
		//Save configuration
		//Update and save XML
		return xmlDescription;
	}
	//Function that updates a configuration
	public Configuration updateConfiguration(Configuration configuration, ArrayList<Boolean> newValues){
		Configuration newConfiguration;
		newConfiguration = featureModelManager.updateConfiguration(configuration, featureList, newValues);
		return newConfiguration;
	}
	
	public String generateScreen(){
		String outputHTML = "";
		String componentType = componentInformation[0];
		String dataSource = componentInformation[1];
		String[] values = new String[5];
		if(componentInformation[4]!=null){
			values = componentInformation[4].split("\n");
		}
		String pageTitle = "";
		String controlDescription = "";
		String componentHTMLOpenTag = "";
		String componentHTMLCloseTag = "";
		String componentValues = "";
		outputHTML = "<DIV id='updatedConfiguration' style='text-align:center;'><FORM name=\"GUI\" action=\"javascript:updateConfiguration(\'" + currentPhase + "\',\'" + componentType + "\',\'" + dataSource + "\');\" method=\'POST\'>";
		switch(currentPhase){
		case "Objective":
			pageTitle="<H2>Select an experiment objective</H2>";
			controlDescription = "The available objectives are: ";
			break;
		case "VariableType":
			pageTitle="<H2>Select the type of variable</H2>";
			controlDescription = "The available variable types are: ";
			break;
		case "FactorLevels":
			pageTitle="<H2>Number of factor levels</H2>";
			controlDescription = "Type in the number of factor levels to consider: ";
			break;
		case "FactorValue":
			pageTitle="<H2>Factor Values</H2>";
			controlDescription = "Type in the value of this factor: ";
			break;
		case "SamplingMethod":
			pageTitle="<H2>Sampling Method</H2>";
			controlDescription = "Given your previous decisions, these sampling methods are available for this experiment: ";
			break;
		case "Response":
			pageTitle="<H2>Number of responses</H2>";
			controlDescription = "Select the number of responses to evaluate: ";
			break;
		case "NumberOfFactors":
			pageTitle="<H2>Number of factors in the experiment</H2>";
			controlDescription = "How many factors will there be in your experiment?";
		}
		
		switch(componentType){
		case "ComboBox":
			componentHTMLOpenTag = "<SELECT id='" + currentPhase + "'>";
			componentHTMLCloseTag = "</SELECT>";
			break;
		case "TextBox":
			componentHTMLOpenTag = "<INPUT type='text' id='" + currentPhase + "'>";
			break;
		}
		
		switch(dataSource){
		case "FeatureModel":
			Iterator<Feature> iter = featureList.iterator();
			while (iter.hasNext()){
				componentValues = componentValues + ("<OPTION>" + iter.next().getName() + "</OPTION>");
			}
			break;
		case "ListOfVariableTypes":
			for(int i=0;i<values.length;i++){
				componentValues = componentValues + ("<OPTION>" + values[i] + "</OPTION>");
			}
			break;
		case "User":
			break;
		}
		
		String nextButton = "<INPUT type='submit' value='Next'>";
		outputHTML = outputHTML + pageTitle + controlDescription + componentHTMLOpenTag + componentValues + componentHTMLCloseTag + "<BR /><BR />" + nextButton;
		outputHTML = outputHTML + "</FORM></DIV>";
		return outputHTML;
	}
	//Function that loads a configuration file
	public boolean loadConfiguration(){
		//configuration = new Configuration();
		ConfigurationReader reader = new ConfigurationReader(configuration);
		InputStream inputStream = null;
		boolean returnValue = false;
		//Read the configuration from a file
		path = Paths.get("C:/Users/azt0018/workspace/ExperimentManager/configs/default.config");
		try {
			inputStream = new FileInputStream(path.toString());
			//inputStream = new FileInputStream("C:/Users/azt0018/workspace/ExperimentManager/configs/default.config");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File not found!");
		}
		try {
			return reader.readFromInputStream(inputStream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		configuration = reader.getConfiguration();
		return returnValue;
	}
	//Function that saves a configuration file
	public void saveConfiguration(){
		ConfigurationWriter writer = new ConfigurationWriter(configuration);
		writer.saveConfigurationToFile(path);
	}
	
	//Temporary function that, given the current phase, returns the parent tag for the XML
	public String getInfo(String phase){
		String parentTag = "";
		
		switch(phase){
		case "Objective":
			parentTag = "experiment";
			break;
		case "VariableType":
			parentTag = "Variable";
			break;
		case "FactorLevels":
			parentTag = "Factor";
			break;
		case "FactorValue":
			parentTag = "FactorLevel";
			break;
		case "SamplingMethod":
			parentTag = "experiment";
			break;
		case "Response":
			parentTag = "experiment";
			break;
		case "NumberOfFactors":
			parentTag = "experiment";
			break;
		}
		
		return parentTag;
	}
	
	//Temporary function that, given the current phase, moves on to the next one
	public String nextPhase(){
		String next = "";
		
		if(phaseCounter == 0){ //Objective
			next="Objective";
		}else if(phaseCounter <= 3){
			next="VariableType";
		}else if(phaseCounter == 4){
			next="Response";
		}else if(phaseCounter == 5){
			next="NumberOfFactors";
		}else if(phaseCounter <= 11){
			if (tempCounter==0){
				next="FactorLevels";
				tempCounter++;
			}else{
				next="FactorValue";
				tempCounter++;
				if(tempCounter==3){
					tempCounter=0;
				}
			}
		}else if(phaseCounter==12){
			next="SamplingMethod";
		}else{
			next="Done";
		}
		phaseCounter++;
		
		return next;
	}
}
