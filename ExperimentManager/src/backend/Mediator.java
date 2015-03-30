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
	private HashMap<String, HashMap<String, String[]>> modelData;
	String[] componentInformation;
	LinkedList<Feature> featureList;
	private static Path path;
	private String experimentName;
	public static int phaseCounter = 1;
	public static int auxiliaryCounter = 1;
	public static int tempCounter = 0;
	//private String basePath = "C:/Users/ALejandro/workspace/ExperimentManager/";
	private String basePath = "C:/Users/azt0018/git/ExperimentManager/";
		
	//Instantiate a new mediator class
	public Mediator(){
		ontologyBuilder = new OntologyBuilder(basePath + "ExperimentOntology_base.owl", basePath +"newExperiment.owl");
	}
	
	public Mediator(String expName, String modelFileName){
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
		ontologyBuilder = new OntologyBuilder(basePath + "ExperimentOntology_base.owl", basePath + experimentName + "/newExperiment.owl");
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
	public HashMap<String, HashMap<String,String[]>> getModelData(){
		return modelData;
	}
	// Function that runs the whole phase and returns an XML string
	public String runPhase(String phase){
		//Load the Ontology and the Feature model managers
		ontologyManager = new OntologyManager(basePath);
		featureModelManager = new FeatureModelManager(basePath);
		configuration = new Configuration(featureModelManager.getFeatureModel());
		currentPhase = phase;

		//Obtain the current component information from the ontology manager
		componentInformation = ontologyManager.obtainComponentInfo(phase);
		//Load the current configuration
		loadConfiguration();
		//Obtain the current features from the feature model manager if needed
		if(componentInformation[1].equals("FeatureModel")){
			featureList = featureModelManager.obtainFeatures(phase, configuration);
		}

		return generateScreen();
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
			Path experimentConfiguration = Paths.get(basePath + experimentName + "/" + experimentName + ".config");
			Files.copy(templateConfiguration, experimentConfiguration, REPLACE_EXISTING);
			inputStream = new FileInputStream(basePath + experimentName + "/" + experimentName + ".config");
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
		}else if(phaseCounter == 2){
			next="Response";
		}else if(phaseCounter == 3){
			next="Variable";
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
	}
	
	public void addFactor(int numberOfFactorLevels){
		ontologyBuilder.addNewIndividual("Factor");
	}
	
	public void loadModelFile(String fileName){
		//Instantiate our hash map
		modelData = new HashMap<String, HashMap<String, String[]>>();
		//Create a document builder factor for parsing an XML file
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			Document doc = db.parse(new File(fileName));
			//Obtain the model's meta-data
			NodeList model = doc.getElementsByTagName("model");
			//Create hash map to store the model's meta data
			HashMap<String, String[]> modelMetadata = new HashMap<String, String[]>();
			//Add the model's name to the hash map
			Element modelNode = (Element) model.item(0);
			String modelName[] = {modelNode.getAttribute("name")};
			//Add the hash map to the top level hash map
			modelMetadata.put("name", modelName);
			modelData.put("model", modelMetadata);
			//Obtain the responses or outputs
			NodeList output = doc.getElementsByTagName("response");
			HashMap<String, String[]> responseMap = new HashMap<String, String[]>();
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
			HashMap<String, String[]> parameterMap = new HashMap<String, String[]>();
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
}
