<%@page import="modelreader.*"%>
<%@page import="backend.*"%>
<%@page import="model.*" %>
<%@page import="configuration.*" %>
<%@page import="java.util.*" %>
<%@page import="ontology.*" %>
<%@page import="org.semanticweb.owlapi.model.*" %>
<%@page language="java" contentType="text/html; charset=ISO-8859-1"
    	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Feature Model Tests</title>
	<script type="text/javascript" src="functions.js"></script>      <!--  //WebContent/functions.js-->
	<!-- Progress bar -->
	<meta charset="utf-8">
  	<meta name="viewport" content="width=device-width, initial-scale=1">
  	<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
  	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
  	<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
	
</head>

<body>
<h1></h1>

<% 	int pageIndex = 0;
	String modelPath = "C:/Users/azt0018/git/ExperimentManager/model.xml";
	FeatureModelLoader loader = new FeatureModelLoader(modelPath);
	FeatureModel featureModel = loader.get_feature_model();
	Configuration configuration = new Configuration(featureModel);
	
	//call FeatureMoldelFindFeatures. Finds all the leafParent Features and make a list of all the Features
	FeatureModelFindFeatures x = new FeatureModelFindFeatures();
	x.findParents(modelPath);
	int size = x.getSize();
	String[] selectedFeatures = new String[size];
	Feature[] parentArray     = new Feature[size]; //returns the parent features
	parentArray = x.toArray();
	Feature root = x.gotRoot();
	ArrayList<Feature> allFeatures = x.ListFeatures(root);
	
	//SETS all features to UNDEFINED
	for(int i = 0 ; i < x.featureSize(); i++){
		configuration.setManual(""+allFeatures.get(i), Selection.UNDEFINED);
	}
	//============================================================================================
			
	//session varialbes to send variable from JSP to JSP
	session.setAttribute("model", modelPath);
	session.setAttribute("featureModel", featureModel);
	session.setAttribute("configuration", configuration);
  	
	//ints
	session.setAttribute("size",size); 
	session.setAttribute("pageIndex", pageIndex+1);
	session.setAttribute("featureSize", x.featureSize());
	session.setAttribute("back","0");
	
	//Arrays
	session.setAttribute("selectedFeatures", selectedFeatures);  //when a feature is selected it will be added to this array on the next page
	session.setAttribute("parentArray", parentArray); // list of all the leafParent Features
	session.setAttribute("allFeatures",allFeatures);  //list of all features
	
	//progress bar filler
	int progress = (pageIndex) * 100/size;
	
	//children Features will be selected in comboBox
	List childrenFeatures = parentArray[0].getChildren(); //children of Objective
%>
	<!--  PROGRESS BAR -->
	<div class="progress">
    <div class="progress-bar" role="progressbar" aria-valuenow="70" aria-valuemin="0" aria-valuemax="100" style="width:<%out.print(progress); %>%">
    <span class="sr-only"><%out.print(progress); %></span>
    </div>
  	</div>
  	
  	<!-- DROPDOWN MENU 
  		 Loops through the children of the parent Feature and makes them a selectable object -->
	<!-- <form action="javascript:updateConfiguration();" method="post" id="EXform">-->
	<form action="NextPage.jsp">
		<p>Choose <%out.println(parentArray[pageIndex]); %></p>
		<select name = "features" id="features">	 <!-- Updates Configuration onChange -->	
		<%for(int i= 0; i < childrenFeatures.size();i++)  { 
			Feature child = featureModel.getFeature(childrenFeatures.get(i).toString()); %>
			<option value="<%out.print(child);%>"><%out.print(child);%></option>
		<%}%>
		</select> 
		
	<input type="submit" value="Submit"></p>
		
	<!-- <a href="NextPage.jsp"><input type="button" value="Next"></a>  <!--  NEXT Button -->
	
	</form>
	

		
	<div id="updatedConfiguration"></div>  <!-- ref WebContent/function -->

</body>
</html>