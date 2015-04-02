package backend;
import configuration.*;
import model.*;
import ontologyinterface.OntologyBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.parsers.*;

import org.w3c.dom.*;

//import com.google.common.io.Files;
import static java.nio.file.StandardCopyOption.*;

public class Mediator {
	private OntologyManager ontologyManager;
	private FeatureModelManager featureModelManager;
	private static Configuration configuration; //The current configuration
	private static String currentPhase;
	private OntologyBuilder ontologyBuilder;
	private LinkedHashMap<String, LinkedHashMap<String, String[]>> modelData;
	private LinkedHashMap<String, String[]> factorList;
	String[] componentInformation;
	LinkedList<Feature> featureList;
	private static Path path;
	private String experimentName;
	public static int phaseCounter = 0;
	public static int auxiliaryCounter = 0;
	public static int tempCounter = 0;
	//private String basePath = "C:/Users/ALejandro/workspace/ExperimentManager/";
	private String basePath = "C:/Users/azt0018/git/ExperimentManager/";
	private String currentModelFolder = "";
		
	//Instantiate a new mediator class
	public Mediator(){
		ontologyBuilder = new OntologyBuilder(basePath + "ExperimentOntology_base.owl", basePath +"newExperiment.owl");
	}
	
	public Mediator(String expName, String modelFileName, String modelFolder){
		experimentName = expName;
		//Find the current path
		System.out.println("Model file: " + modelFileName);
		//Generate the model data hash map
		loadModelFile(modelFileName);
		//Create a new folder for the output of this experiment
		try{
			System.out.println("Able to create new folder? " + new File(basePath + experimentName).mkdir());	
		}catch(Exception e){
			System.out.println("Error creating the folder: " + e.getMessage());
		}
		ontologyBuilder = new OntologyBuilder(basePath + "ExperimentOntology_base.owl", modelFolder + "/newExperiment.owl");
		currentModelFolder = modelFolder;
	}
	
	public Configuration getConfiguration(){
		return configuration;
	}
	
	public String getBasePath(){
		return basePath;
	}
	
	public String getCurrentPhase(){
		return currentPhase;
	}
	
	public OntologyBuilder getOntologyBuilder(){
		return ontologyBuilder;
	}
	public LinkedHashMap<String, LinkedHashMap<String,String[]>> getModelData(){
		return modelData;
	}
	// Function that runs the whole phase and returns an HTML string
	public String runPhase(String phase){
		String outputHTML = "";
		//Load the Ontology and the Feature model managers
		ontologyManager = new OntologyManager(basePath);
		featureModelManager = new FeatureModelManager(basePath);
		configuration = new Configuration(featureModelManager.getFeatureModel());
		currentPhase = phase;

		if(phase.equals("Response")){
			HashMap<String, String[]> responses = modelData.get("responses");
			outputHTML = generateResponsesScreen(responses);
		}else if(phase.equals("Factors")){
			HashMap<String, String[]> parameters = modelData.get("parameters");
			outputHTML = generateFactorsScreen(parameters);
		}else if(phase.equals("SpecificFactor")){ //The auxiliaryCounter keeps track of the current factor
			Entry<String, String[]> factor = (Entry<String, String[]>)factorList.entrySet().toArray()[auxiliaryCounter];
			outputHTML = generateSpecificFactorScreen(factor);
		}else{
			//Obtain the current component information from the ontology manager
			componentInformation = ontologyManager.obtainComponentInfo(phase);
			//Load the current configuration
			loadConfiguration();
			//Obtain the current features from the feature model manager if needed
			if(componentInformation[1].equals("FeatureModel")){
				featureList = featureModelManager.obtainFeatures(phase, configuration);
			}
			outputHTML = generateScreen();
		}
		return outputHTML;
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
		//Copy the configuration template file to the experiment folder
		path = Paths.get(basePath + "configs/default.config");
		try {
			Path templateConfiguration = Paths.get(basePath + "configs/default.config");
			Path experimentConfiguration = Paths.get(currentModelFolder + experimentName + ".config");
			Files.copy(templateConfiguration, experimentConfiguration, REPLACE_EXISTING);
			inputStream = new FileInputStream(currentModelFolder + experimentName + ".config");
			//inputStream = new FileInputStream("C:/Users/azt0018/workspace/ExperimentManager/configs/default.config");
		} catch (Exception e) {
			System.out.println("There was a problem loading the configuration file!");
		}
		try {
			return reader.readFromInputStream(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
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
	
	//Temporary function that, given the current phase, moves on to the next one
	public String nextPhase(){
		String next = "";
		//TODO change this so that the next phase is chosen from the process flow and the user provided decisions.
		if(phaseCounter == 0){ //Objective
			next="Objective";
		}else if(phaseCounter == 1){
			next="Response";
		}else if(phaseCounter == 2){
			next="Factors";
		}else if(phaseCounter == 3){
			next="SpecificFactor";
		}else if(phaseCounter==4){
			next="SamplingMethod";
		}else{
			next="Done";
		}
		return next;
	}
	
	public void loadModelFile(String fileName){
		//Instantiate our hash map
		modelData = new LinkedHashMap<String, LinkedHashMap<String, String[]>>();
		//Create a document builder factor for parsing an XML file
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			Document doc = db.parse(new File(fileName));
			//Obtain the model's meta-data
			NodeList model = doc.getElementsByTagName("model");
			//Create hash map to store the model's meta data
			LinkedHashMap<String, String[]> modelMetadata = new LinkedHashMap<String, String[]>();
			//Add the model's name to the hash map
			Element modelNode = (Element) model.item(0);
			String modelName[] = {modelNode.getAttribute("name")};
			//Add the hash map to the top level hash map
			modelMetadata.put("name", modelName);
			modelData.put("model", modelMetadata);
			//Obtain the responses or outputs
			NodeList output = doc.getElementsByTagName("response");
			LinkedHashMap<String, String[]> responseMap = new LinkedHashMap<String, String[]>();
			for(int i=0; i<output.getLength(); i++){
				Element currentResponse = (Element)output.item(i);
				String responseName = currentResponse.getAttribute("name");
				String displayName = currentResponse.getAttribute("displayName");
				String type = currentResponse.getAttribute("type");
				String[] responseData = {displayName, type};
				//Add to hash map
				responseMap.put(responseName, responseData);
				//Add to top hash map
			}
			modelData.put("responses", responseMap);
			//Obtain the parameters or inputs
			NodeList input = doc.getElementsByTagName("parameter");
			LinkedHashMap<String, String[]> parameterMap = new LinkedHashMap<String, String[]>();
			for(int i=0; i<input.getLength(); i++){
				Element currentParameter = (Element)input.item(i);
				String parameterName = currentParameter.getAttribute("name");
				String displayName = currentParameter.getAttribute("displayName");
				String type = currentParameter.getAttribute("type");
				String[] parameterData = {displayName, type};
				//Add to hash map
				parameterMap.put(parameterName, parameterData);
				//Add to top hash map
			}
			modelData.put("parameters", parameterMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("There was an error reading the simulation model XML file:" + e.getMessage());
		}
		System.out.println("Model data hash map: " + modelData.toString());
		System.out.println("Number of parameters: " + modelData.get("parameters").size());
		System.out.println("Number of responses: " + modelData.get("responses").size());
	}
	
	/*
	 * Screen generation procedures
	 */
	public String generateObjectivesScreen(){
		String componentType = componentInformation[0];
		String dataSource = componentInformation[1];
		String[] values = new String[5];
		if(componentInformation[4]!=null){
			values = componentInformation[4].split("\n");
		}
		
		String outputHTML = "<DIV id='updatedConfiguration' style='text-align:center;'><FORM name=\"GUI\" action=\"javascript:updateConfiguration(\'" + currentPhase + "\',\'" + componentType + "\',\'" + dataSource + "\');\" method=\'POST\'><H2>Select an experiment objective</H2><P>The available objectives are: ";
		outputHTML += "<SELECT id='" + currentPhase + "'>";
		Iterator<Feature> iter = featureList.iterator();
		while (iter.hasNext()){
			outputHTML += "<OPTION>" + iter.next().getName() + "</OPTION>";
		}
		outputHTML = "</SELECT><BR /><BR /><INPUT type='submit' value='Next'></FORM></DIV>";
		
		return outputHTML;
	}
	
	public String generateResponsesScreen(HashMap<String, String[]> responseList){
		String outputHTML = "";
		String componentType = "CheckBox";
		String dataSource = "User";
		//DIV and FORM
		outputHTML += "<DIV id='updatedConfiguration' style='text-align:center;'><FORM name=\"GUI\" action=\"javascript:updateConfiguration(\'" + currentPhase + "\',\'" + componentType + "\',\'" + dataSource + "\');\" method=\'POST\'>";
		//Components
		outputHTML += "<H2>Experiment Responses</H2><P>Select the model output values that you would like to use as experiment responses:</P>";
		//Display all the responses with a check box
		Iterator<Entry<String, String[]>> iter = responseList.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String[]> currentResponse = iter.next();
			outputHTML += "<INPUT type='checkbox' name='Response' value='" + currentResponse.getKey() + "'>" + currentResponse.getValue()[0] + "<BR>";
		}
		//Close the form and the DIV
		outputHTML += "<BR /><BR /><INPUT type='submit' value='Next'></FORM></DIV>";

		return outputHTML;
	}
	
	public String generateFactorsScreen(HashMap<String, String[]> parameterList){
		String outputHTML = "";
		String componentType = "CheckBox";
		String dataSource = "User";
		//DIV and FORM
		outputHTML += "<DIV id='updatedConfiguration' style='text-align:center;'><FORM name=\"GUI\" action=\"javascript:updateConfiguration(\'" + currentPhase + "\',\'" + componentType + "\',\'" + dataSource + "\');\" method=\'POST\'>";
		//Components
		outputHTML += "<H2>Experiment Factors</H2><P>Select the model parameters that you would like to experiment with:</P>";
		//Display all the responses with a check box
		Iterator<Entry<String, String[]>> iter = parameterList.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String[]> currentResponse = iter.next();
			outputHTML += "<INPUT type='checkbox' name='Factors' value='" + currentResponse.getKey() + "'>" + currentResponse.getValue()[0] + "<BR>";
		}
		//Close the form and the DIV
		outputHTML += "<BR /><BR /><INPUT type='submit' value='Next'></FORM></DIV>";

		return outputHTML;
	}
	
	public String generateSpecificFactorScreen(Entry<String, String[]> factor){
		String componentType = "Multiple Text Boxes";
		String dataSource = "User";
		String outputHTML = "<H2 align='center'>Factor Levels for factor " + factor.getValue()[0] + "</H2>";
		outputHTML += "<DIV id='updatedConfiguration' style='text-align:center;'><FORM name=\"GUI\" action=\"javascript:updateConfiguration(\'" + currentPhase + "\',\'" + componentType + "\',\'" + dataSource + "\');\" method=\'POST\'>";
		outputHTML += "<P>How many factor levels do you want to consider for this factor? <INPUT type='number' id='numberOfFactorLevels' value='1' min='1' max='10' onchange='javascript:updateFactorValues();'></P>";
		outputHTML += "<H3>Factor levels for this factor:</H3>";
		outputHTML += "<DIV id='factorLevelEntries'>"+ generateFactorValueScreen(1) + "</DIV>";
		outputHTML += "<BR><BR><INPUT type='submit' value='Next'></FORM></DIV>";
		return outputHTML;
	}
	
	public String generateFactorValueScreen(int factorValueNumber){
		String outputHTML = "";
		for(int i=0; i<factorValueNumber; i++){
			outputHTML += "Factor Level Value " + (i+1) + " <INPUT type='number' id='factorLevelValue" + (i+1) + "'><BR>";
		}
		return outputHTML;
	}
	
	/*
	 * Input processing procedures
	 */
	//Function that initializes a new experiment
	public void beginNewExperiment(){
		//Begin a new iteration
		beginNewIteration();
	}
	
	public void beginNewIteration(){
		//Add a new iteration individual in our ontology
		ontologyBuilder.addNewIndividual("Iteration");
		//Add its children Results and Design
		ontologyBuilder.addNewIndividual("Results");
		ontologyBuilder.addNewIndividual("Design");
		//Initialize the auxiliaryCounter
		auxiliaryCounter = 0;
	}
	
	public void addResponse(String value){
		ontologyBuilder.addNewIndividual("Response");
		ontologyBuilder.addNewDataProperty("value", value);
	}
	
	public void addFactorValue(String value){
		ontologyBuilder.addNewIndividual("Level_Value");
		ontologyBuilder.addNewDataProperty("value", value);
	}
	
	public void addFactor(String factorName, int numberOfFactorLevels, String[] factorValues){
		ontologyBuilder.addNewIndividual("Factor");
		ontologyBuilder.addNewDataProperty("name", factorName);
		//Add its children factor levels
		for(int i=0; i<numberOfFactorLevels; i++){
			ontologyBuilder.addNewIndividual("Factor_Level");
			ontologyBuilder.addNewDataProperty("value", factorValues[i]);
		}
	}
	
	public void addSamplingMethod(String value){
		ontologyBuilder.addNewIndividual("SamplingMethod");
		ontologyBuilder.addNewDataProperty("value", value);
	}
	
	public void addObjective(String value){
		ontologyBuilder.addNewIndividual("Objective");
		ontologyBuilder.addNewDataProperty("value", value);
	}
	
	public void processNumberOfFactors(int value){
		switch(value){
		case 1:
			configuration.setManual("OneFactor", Selection.SELECTED);
			break;
		case 2:
			configuration.setManual("TwoFactors", Selection.SELECTED);
			break;
		case 3:
			configuration.setManual("ThreeFactors", Selection.SELECTED);
			break;
		case 4:
			configuration.setManual("FourFactors", Selection.SELECTED);
			break;
		default:
			configuration.setManual("FiveOrMoreFactors", Selection.SELECTED);
			break;
		}
	}
	
	public void processNumberOfFactorLevels(String value){
		switch(Integer.parseInt(value)){
		case 1:
			configuration.setManual("OneFactorLevel", Selection.SELECTED);
			break;
		case 2:
			configuration.setManual("TwoFactorLevels", Selection.SELECTED);
			break;
		case 3:
			configuration.setManual("ThreeFactorLevels", Selection.SELECTED);
			break;
		default:
			configuration.setManual("FourOrMoreFactorLevels", Selection.SELECTED);
			break;
		}
	}
	
	public void processObjective(String value){
		configuration.setManual(value, Selection.SELECTED);
		addObjective(value);
		changePhaseCounter();
	}
	
	public void processSamplingMethod(String value){
		configuration.setManual(value, Selection.SELECTED);
		addSamplingMethod(value);
		changePhaseCounter();
	}
	
	public void processResponses(String value){
		//Separate the individual responses
		String[] responses = value.split(",");
		//For each response, add a Response individual to the ontology
		for(int i=0; i<responses.length; i++){
			addResponse(responses[i]);
		}
		changePhaseCounter();
	}
	
	public void processFactors(String value){
		//Retrieve the indices from 'value', and use them to generate a list of factors
		factorList = new LinkedHashMap<String, String[]>();
		String[] factorIndices = value.split(",");
		//Store number of factors and update configuration
		int numberOfFactors = factorIndices.length;
		processNumberOfFactors(numberOfFactors);
		int[] indices = new int[factorIndices.length];
		//Parse to integer values so we can use them
		LinkedHashMap<String, String[]> parameterList = modelData.get("parameters");
		System.out.println("Selected factors:");
		for(int i=0; i<factorIndices.length; i++){
			indices[i] = Integer.parseInt(factorIndices[i]);
			//Fill in the Hash map with the factor's information
			Entry<String, String[]> currentFactor = (Entry<String, String[]>)parameterList.entrySet().toArray()[indices[i]];
			factorList.put(currentFactor.getKey(), currentFactor.getValue());
			System.out.println("Factor: " + currentFactor.getValue()[0]);
		}
		changePhaseCounter();
	}
	
	public void processFactor(String value){ //auxiliaryCounter lets us know the current factor being considered
		//Use the list of factors to obtain the name of the current factor
		Entry<String, String[]> currentFactor = (Entry<String, String[]>)factorList.entrySet().toArray()[auxiliaryCounter];
		String factorName = currentFactor.getValue()[0];
		//Split 'value' to get the number of, and values of the factor levels
		String values[] = value.split(",");
		int numberOfFactors = values.length;
		addFactor(factorName, numberOfFactors, values);
		//Check whether the phase counter needs to be updated
		if(auxiliaryCounter < factorList.size()-1){
			auxiliaryCounter++;
		}else{
			changePhaseCounter();
		}		
	}
	
	/*
	 * Other useful functions
	 */
	public void printStructure(){
		ontologyBuilder.printStructure();
	}
	
	public void changePhaseCounter(){
		phaseCounter++;
	}
}
