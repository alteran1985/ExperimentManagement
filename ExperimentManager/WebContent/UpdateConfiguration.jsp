<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="backend.FeatureModelLoader"%>
<%@page import="backend.XMLFileWriter"%>
<%@page import="model.*" %>
<%@page import="configuration.*" %>
<%@page import="java.util.*" %>
<%@page import="java.util.jar.*" %>
<%@page import="backend.Mediator" %>
<%@page import="ontologyinterface.*" %>
<HTML>
<%  
	Mediator mediator = (Mediator) session.getAttribute("mediator");
	//Get the current phase
	String currentPhase = request.getParameter("currentPhase");
	//Get the values provided by the user
	String value = request.getParameter("value");
	
	if(currentPhase.equals("Objective")){
		mediator.processObjective(value);
	}else if(currentPhase.equals("Response")){
		mediator.processResponses(value);
	}else if(currentPhase.equals("Factors")){
		mediator.processFactors(value);
	}else if(currentPhase.equals("SpecificFactor")){
		mediator.processFactor(value);
	}else{
		mediator.processSamplingMethod(value);
	}
	
	/*
	FeatureModelLoader loader = new FeatureModelLoader(mediator.getBasePath());
	FeatureModel featureModel = loader.get_feature_model();
	Configuration configuration = mediator.getConfiguration();
	//XMLFileWriter writer = new XMLFileWriter();
	//String responseToClient = "";
	OntologyBuilder ontologyBuilder = mediator.getOntologyBuilder();

	//Obtain the parameters and their values
	String currentPhase = request.getParameter("currentPhase");
	String controlType = request.getParameter("controlType");
	String dataSource = request.getParameter("dataSource");
	String value = request.getParameter("value");
	//Create the individuals in the ontology
	if(currentPhase.equals("Response")){
		mediator.addResponse(value);
	}else if(currentPhase.equals("FactorValue")){
		mediator.addFactorValue(value);
	}
	//Depending on the data source, do something different
	if (dataSource.equals("FeatureModel")){ //Update the feature model directly
		configuration.setManual(value, Selection.SELECTED);
	}else{
		if(currentPhase.equals("FactorLevels")){ //Based on the user input, select the corresponding feature
			// Create the factor level individuals
			for(int i = 0; i<Integer.parseInt(value); i++){
				ontologyBuilder.addNewIndividual("Factor_Level");
			}
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
		}else if(currentPhase.equals("NumberOfFactors")){
			for(int i = 0; i<Integer.parseInt(value); i++){
				ontologyBuilder.addNewIndividual("Factor");
			}
			switch(Integer.parseInt(value)){
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
	}
	//System.out.println(responseToClient);*/
	mediator.saveConfiguration();
	mediator.printStructure();
	response.sendRedirect("main.jsp?phase=" + mediator.nextPhase());
%>
</HTML>