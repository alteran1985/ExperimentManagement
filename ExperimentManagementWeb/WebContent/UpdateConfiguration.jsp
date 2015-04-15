<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="backend.FeatureModelLoader"%>
<%@page import="model.*" %>
<%@page import="configuration.*" %>
<%@page import="java.util.*" %>
<%@page import="java.util.jar.*" %>

<HTML>
<%	
	
	int arraySize = (Integer)session.getAttribute("arraySize");
	int parentSize = (Integer)session.getAttribute("parentSize");
	int pageIndex = (Integer)session.getAttribute("pageIndex") - 1;
	FeatureModel featureModel = (FeatureModel)session.getAttribute("featureModel");
	 
	String[]configArray = (String[])session.getAttribute("configArray");
	String[]childArray =(String[])session.getAttribute("childArray");
	String str = request.getParameter("features");
	childArray[pageIndex] = str;
	
	
	Feature selected= featureModel.getFeature(str);
	Feature feature = new Feature(featureModel);
	Configuration configuration = new Configuration(featureModel);
	SelectableFeature select = new SelectableFeature(configuration,selected);
	configuration.setManual(str, Selection.SELECTED);
	
	Set<Feature> unselect = configuration.getUnSelectedFeatures();
	Iterator<Feature> it = unselect.iterator();

	
	int toggle = 1;
	int index = 0;

	//read configArray ===================================================
	toggle = 1;
	String featureName;
	for(int i = 0; i < arraySize; i++){
		if(toggle == 1){
			featureName = configArray[i];
		}
		else{
			if(configArray[i] != null){
				if(configArray[i].equalsIgnoreCase("SELECTED")){
					configuration.setManual(configArray[i-1],Selection.SELECTED);
				}
				else if(configArray[i].equalsIgnoreCase("UNSELECTED")){
		    		configuration.setManual(configArray[i-1],Selection.UNSELECTED);
		    	}
				index++;
			}
		}
		toggle *= -1;
	}
	//array End ====================================================
			
			
	String responseToClient = "<p><br />Pull Information from Ontology:<br />";
	//Obtain the list of feature names and feature values
	Enumeration<String> paramNames = request.getParameterNames();
	//Attributes features = new Attributes();
	
	String parameterName;
	int i = 0;
	while (paramNames.hasMoreElements()){
		parameterName = paramNames.nextElement().toString();
		Feature testFeature = featureModel.getFeature(parameterName);
		
	 	if(!(testFeature == null))
	 	{
	 		String parentName = testFeature.getParent().toString();
	 	}
		
		String value = request.getParameter(parameterName);

		%>
		<!--  <%= parameterName %>
		<%= value %>-->
		<% 
		
		/*	Update ConfigArray Selected Variables  */
		if(value.contains("true"))
		{ 
			String compare;
			for(int j = 0; j < arraySize; j++)
			{
				compare = configArray[j];
				
				if (parameterName.equals(compare))
				{
					configArray[j+1] = "SELECTED";
				}
			}
			//responseToClient = responseToClient + parameterName;
			configuration.setManual(parameterName,Selection.SELECTED);
		}
		i++;
	}
	
	
	//Return the updated list of features
	Set<Feature> unselected_features = configuration.getUnSelectedFeatures();
	Iterator<Feature> iter = unselected_features.iterator();
	//responseToClient = responseToClient + "</p><p>Updated unselected features: ";
	while (iter.hasNext())
	{
		String unselected = iter.next().getName(); //unselected values
		//responseToClient = responseToClient + ("<br />" + unselected);
		String compare;
		
		/*	IF unselected is a selected file the for loop will find the paramName
		* 	and set its joining index to SELECTED  */
		for(int j = 0; j < arraySize; j++)
		{
			compare = configArray[j];
			if (unselected.equals(compare)){
				configArray[j+1] = "UNSELECTED";
			}
		}
	}
	responseToClient = responseToClient + "</p>";
	

	session.setAttribute("configArray",configArray); //update configArray
%>

<%= responseToClient %>


</HTML>