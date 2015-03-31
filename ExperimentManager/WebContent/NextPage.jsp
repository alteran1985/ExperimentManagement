<%@page import="modelreader.*"%>
<%@page import="backend.*"%>
<%@page import="model.*" %>
<%@page import="configuration.*" %>
<%@page import="java.util.*" %>
<%@page import="ontology.*" %>
<%@page import="org.semanticweb.owlapi.model.*" %>
<%@page language="java" contentType="text/html; charset=ISO-8859-1"
    	pageEncoding="ISO-8859-1"%>
<%@page import= "java.io.PrintWriter" %>  			
<%@page import= "java.io.FileOutputStream" %>		
<%@page import= "java.io.FileReader"%>
<%@page import= "java.io.BufferedReader"%>



<HTML>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Feature Model Tests</title>
	<script type="text/javascript" src="functions.js"></script>      <!--  //WebContent/functions.js-->
	<!--  PROGRESS BAR -->
	<meta charset="utf-8">
  	<meta name="viewport" content="width=device-width, initial-scale=1">
  	<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
  	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
  	<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
</head>

<BODY>
<h1></h1>
 <% 
 //load session vars ******************************************************************************
 FeatureModel featureModel   = (FeatureModel)session.getAttribute("featureModel");
 Configuration configuration = (Configuration)session.getAttribute("configuration");

 //LOAD SESSION INTS
 int pageIndex   = (Integer)session.getAttribute("pageIndex");
 int size  = (Integer)session.getAttribute("size");
 int featureSize = (Integer)session.getAttribute("featureSize");
 
 //LOAD Session Arrays
 ArrayList<Feature> allFeatures = (ArrayList<Feature>)session.getAttribute("allFeatures");
 Feature[] parentArray          = (Feature[])session.getAttribute("parentArray");
 String[] selectedFeatures      = (String[])session.getAttribute("selectedFeatures");
 
 String param = request.getParameter("back");
 int back = 0;
 try{ 
 
 back= Integer.parseInt(param);
 pageIndex = pageIndex-back;
 
 
 }catch(NumberFormatException e){
	 
 }
 session.setAttribute("back","0"); //set back to zero;
 //************************************************************************************************
 
 
 // UPATE CONFIG *************************************************************************88
 if(back == 0){ //only add feature to selected if move forward a page.
 	String str = request.getParameter("features"); //submittion from previous page
 	selectedFeatures[pageIndex-1] = str;  //pageIndex -1 = last page
 }
 System.out.println(pageIndex); 
 
 //Reset Config resets the configuration
 //This is used just incase a user goes back a page.
 for(int i = 0 ; i < allFeatures.size(); i++){
	 //if(allFeatures.get(i) != null){ 
		 configuration.setManual(""+allFeatures.get(i), Selection.UNDEFINED); //}
 }

 //Update config for upto pageINDEX
 for(int i = 0 ; i <= pageIndex-1 ; i++){
	 if(selectedFeatures[i] != null){ 
		 System.out.println("SELECTED "+ selectedFeatures[i]);
		 configuration.setManual(selectedFeatures[i], Selection.SELECTED); 
		 }
 }
 //*****************************************************************************************
 
 
 //update session variables for next page
 session.setAttribute("selectedFeatures", selectedFeatures);
 session.setAttribute("pageIndex", pageIndex+1);
 
 int progress   = (pageIndex) * 100/size;
 
 
//Loop throught the child features of the current parent Feature.
//compares the children with the unselected List.
//If the children are unselected they will not be added to the comboBox.
List childrenFeatures = parentArray[pageIndex].getChildren(); //
Set<Feature> unselected = configuration.getUnSelectedFeatures();
//testing. print of unselectabe
for(Feature f : unselected){
	System.out.println("UNSELECTED"+f);
}
		
Set<Feature> selectable = new HashSet<Feature>();
boolean add = true;
//loop through the Children Features  to find valid children 
for(int i= 0; i < childrenFeatures.size();i++) 
  {
	add = true;
	Feature child = featureModel.getFeature(childrenFeatures.get(i).toString());
	for(Feature f : unselected){
		if(f == child)
 		{  add = false;
 			break;
 		}
	}
	if(add == true){
		selectable.add(child);
	}
 }
 %>
 
 <!-- PROGRESS BAR -->
 <div class="progress">
	 <div class="progress-bar" role="progressbar" aria-valuenow="70" aria-valuemin="0" aria-valuemax="100" style="width:<%out.print(progress); %>%">
	 <span class="sr-only"><%out.print(progress); %></span>
	 </div>
 </div>
 
 <!--  DROPDOWN MENU -->
 <form action="NextPage.jsp">
	<p> Choose <%out.println(parentArray[pageIndex]); %>: </p>
	<select name = "features" id="features">	 	
	 	<%  for(Feature f : selectable){
	 		%><option value="<%out.print(f);%>"><%out.print(f);%></option>
	 	<%}%> 
	</select>
	<input type="submit" value="Submit">


	<!-- When PageIndex reaches the size of the parentArray show finish button -->
	
	<%if(pageIndex+1 >= size){%> 
	<a href="ExitPage.jsp"><input type="button" value="Finish"></a>
	<%}%>

	</form>
	<form  name="backbut" action="NextPage.jsp" method="post">
		<input type="hidden" name="back" value="2">
		<input type="submit" value="Back">
	</form>

	
	
	
	<div id="updatedConfiguration"></div>
	
</BODY>
</HTML>