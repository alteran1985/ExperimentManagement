<%@page import="modelreader.*"%>
<%@page import="backend.*"%>
<%@page import="model.*" %>
<%@page import="configuration.*" %>
<%@page import="java.util.*" %>
<%@page import="ontology.*" %>
<%@page import="org.semanticweb.owlapi.model.*" %>
<%@page language="java" contentType="text/html; charset=ISO-8859-1"
    	pageEncoding="ISO-8859-1"%>


<html>
<head>
	<!--  PROGRESS BAR -->
	<meta charset="utf-8">
  	<meta name="viewport" content="width=device-width, initial-scale=1">
  	<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
  	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
  	<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
</head>
<body>
<h1></h1>
<%
int pageIndex = (Integer)session.getAttribute("pageIndex");
int parentSize = (Integer)session.getAttribute("parentSize");
Feature[] parentArray = (Feature[])session.getAttribute( "parentArray" );
FeatureModel featureModel = (FeatureModel)session.getAttribute("featureModel");
//List childrenFeatures = parentArray[pageIndex].getChildren();
String[] configArray = (String[])session.getAttribute("configArray");
int arraySize = (Integer)session.getAttribute("arraySize");

String[] selectedFeatures = new String[parentSize];
int counter = 0;
for(int i = 0 ; i < arraySize; i++){
	//System.out.println(configArray[i]);
	
	if(configArray[i] != null){
	if(configArray[i].equalsIgnoreCase("SELECTED")){
		selectedFeatures[counter] = configArray[i-1];
		counter++;
	}
	}
}

int progress = (pageIndex) * 100/parentSize;
%>

<div class="progress">
<div class="progress-bar" role="progressbar" aria-valuenow="70" aria-valuemin="0" aria-valuemax="100" style="width:<%out.print(progress); %>%">
	<span ><%out.print("FINISHED");%></span>
</div>
</div> 

<%
for(int i = 0; i < parentSize ; i++){
%> <p><%out.print(parentArray[i]); %>:     <%out.print(selectedFeatures[i]); %></p>
<%
}


%>
</body>


</html>