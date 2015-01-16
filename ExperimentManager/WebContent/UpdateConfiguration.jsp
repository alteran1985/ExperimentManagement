<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="backend.FeatureModelLoader"%>
<%@page import="backend.XMLFileWriter"%>
<%@page import="model.*" %>
<%@page import="configuration.*" %>
<%@page import="java.util.*" %>
<%@page import="java.util.jar.*" %>
<%@page import="backend.Mediator" %>
<HTML>
<%
	Mediator mediator = new Mediator();
	FeatureModelLoader loader = new FeatureModelLoader();
	FeatureModel featureModel = loader.get_feature_model();
	Configuration configuration = mediator.getConfiguration();
	XMLFileWriter writer = new XMLFileWriter();
	String responseToClient = "";

	//Obtain the parameters and their values
	String currentPhase = request.getParameter("currentPhase");
	String controlType = request.getParameter("controlType");
	String dataSource = request.getParameter("dataSource");
	String value = request.getParameter("value");
	//Depending on the data source, do something different
	if (dataSource.equals("FeatureModel")){ //Update the feature model directly
		configuration.setManual(value, Selection.SELECTED);
	}else{
		if(currentPhase.equals("FactorLevels")){ //Based on the user input, select the corresponding feature
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
	//Update the XML File
	boolean isNew = false;
	ArrayList<String> info = new ArrayList<String>();
	//Add the current tag name
	info.add(currentPhase);
	//Add the parent tag
	info.add(mediator.getInfo(currentPhase));
	//Add the value
	info.add(value);
	System.out.println(info.toString());
	if(currentPhase.equals("Objective")){
		isNew = true;
	}
	responseToClient = "<p>" + writer.updateXML(isNew, info) + "</p>";
	System.out.println(responseToClient);
	mediator.saveConfiguration();
	response.sendRedirect("http://localhost:8080/ExperimentManager/main.jsp?phase=" + mediator.nextPhase());
%>
</HTML>