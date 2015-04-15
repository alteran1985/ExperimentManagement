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
	<meta charset="utf-8">
  	<meta name="viewport" content="width=device-width, initial-scale=1">
  	<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
  	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
  	<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
</head>

<BODY>
<h1></h1>
 <% 
/* **********************************************************************************************
 * LOAD SESSION VARIABLES
 */
 
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
 
 // if back button is pressed.
 String param = request.getParameter("back");
 int back = 0;
 try{ 
	 back= Integer.parseInt(param);
	 pageIndex = pageIndex-back;
 }
 catch(NumberFormatException e){
	 //didn't select back
 }
 session.setAttribute("back","0"); //set back to zero;
 
 //************************************************************************************************
 
 
 
/* ***********************************************************************************************
 *  UPDATE CONFIG
 */
 if(back == 0){ //only add feature to selectedFeatures[] if moving forward a page.
 	String str = request.getParameter("features"); //submittion from previous page
 	selectedFeatures[pageIndex-1] = str;  //pageIndex -1 = last page
 }
 
 //Reset Config resets the configuration
 //This is used just incase a user goes back a page.
 for(int i = 0 ; i < allFeatures.size(); i++){
	 //if(allFeatures.get(i) != null){ 
 	configuration.setManual(""+allFeatures.get(i), Selection.UNDEFINED); //}
 }

 /*
  * Updates SELECTED feature for up to pageINDEX
  * BECAUSE: if a user navigates backward you do not want to include selected features
  * from a pageIndex that is greater than the current pageIndex
  */
 for(int i = 0 ; i <= pageIndex-1 ; i++){
	 if(selectedFeatures[i] != null){ 
		 configuration.setManual(selectedFeatures[i], Selection.SELECTED); 
	}
 }
  // *********************************************************************************
 
  
  
  
 /* *********************************************************************************
  * UPDATE XML
  * This writes a new XML file: First it retrieves the session variable "model"
  * Then calls the file WriteXML() which builds the XML file.
 */
 	String model = (String) session.getAttribute("model");
	WriteXML write = new WriteXML();
 	write.model(model);  //finds root of feature model
	write.setSize(pageIndex);
 	
 	String[] pArray = new String[size];  //Must convert parent array from Feature to String
 	for(int i = 0; i < pageIndex ; i++){ //only looks the size of pageIndex this is to support backwards nav.
 		pArray[i] = parentArray[i]+"";
 	}
 	
 	write.writeXML(pArray, selectedFeatures); //writes XML file based off of pArray and selectedFeatures

 //*****************************************************************************************
 
 
 
/* *****************************************************************************************
 *	update session variables for next page
 * 	PageIndex and Selected Features needs to be updated before moving to next page
 */
	session.setAttribute("selectedFeatures", selectedFeatures);
	session.setAttribute("pageIndex", pageIndex+1);
	int progress   = (pageIndex) * 100/size;  //
// ****************************************************************************************


%> <!-- back to html -->



<!-- PROGRESS BAR **************************************************************************-->
<div class="progress">
	 <div class="progress-bar" role="progressbar" aria-valuenow="70" aria-valuemin="0" aria-valuemax="100" style="width:<%out.print(progress); %>%">
	 	<span class="sr-only"><%out.print(progress); %></span>
	 </div>
</div> 
<!-- *************************************************************************** -->

<%   //java


if(pageIndex < size){  //if page index is less than size
	/* ********************************************************************************
	 *	Update Unselected Features
	 *	This helps enforce constraints
	 * 	If a childFeature (leaf node) is in the unselected features list will not
	 *	be visable for selection.
	 *  Loop through the child features of the current parent Feature.
	 *  compares the children with the unselected List.
	 *  If the children are unselected they will not be added to the comboBox.
	 * ******************************************************************************* */
	List childrenFeatures = parentArray[pageIndex].getChildren(); //
	Set<Feature> unselected = configuration.getUnSelectedFeatures();
	Set<Feature> selected = configuration.getSelectedFeatures();
	Set<Feature> selectable = new HashSet<Feature>();
	boolean add = true;
	boolean alreadySelected = false; //if a constraint in the feature model selects a feature on its own
	
	//testing
//	System.out.println("*****SELECTED FEATURES ****");
//	for(Feature f : selected){
//		System.out.println(f);
//		
//	}
//	System.out.println("**********************");
//	
//	System.out.println("*****UNSELECTED FEATURES ****");
//	for(Feature f : unselected){
//		System.out.println(f);
//	}
//	System.out.println("**********************");
//	
	//loop through the Children Features  to find valid children 
	for(int i= 0; i < childrenFeatures.size();i++) {
		add = true;
		alreadySelected = false;
		Feature child = featureModel.getFeature(childrenFeatures.get(i).toString());
		
		//if a constraint in the feature model selects a feature on its own
		for(Feature f : selected){
				if(f == child){
					alreadySelected = true;
					break;
				}
			
		}
		
		if (alreadySelected){
			selectable.clear();
			selectable.add(child);
			break;
		}
		else{ //Feature was not automatically selected by the configuration file.
			  //check if any have been unselected
			for(Feature f : unselected){
				if(f == child) { 
					System.out.println(child);
					add = false;
		 			break;
		 		}
				else{
					add = true;
					//selectable.add(child);
				}
			} // end for
		} // end else
		
		if(add == true){ selectable.add(child); }
	 } //end for (i < childrenFeatures)
	
		 
%> <!-- back to html -->
 
 
	<!--  DROPDOWN MENU  *************************************************************************-->
	<form action="NextPage.jsp">
		<p> Choose <%out.println(parentArray[pageIndex]); %>: </p>
		<select name = "features" id="features">	 	
		 	<%  
		 	for(Feature f : selectable){ %>
		 		<option value="<%out.print(f);%>"><%out.print(f);%></option>
		 	<%
		 	}
		 	%> 
		</select>
		<input type="submit" value="Submit">
	</form>
	
	<!-- Back Button sends a session variable of 2 to the "next" page
	and decrements the current page number by the back value --> <%
	
	if( pageIndex >= 1){%>
	<form  name="backbut" action="NextPage.jsp" method="post">
		<input type="hidden" name="back" value="2">
		<input type="submit" value="Back">
	</form>
	<%} %>
	
	<div id="updatedConfiguration"></div>
	
	<!-- ********************************************************************************************* -->


<% }  //end if(pageIndex < size){ 



else{  //Once Page = Size print off Selected Array Print out all the selected features
		
 	
	for(int i = 0; i < pageIndex ; i++){
 		%> <p> <% out.println(parentArray[i] + " : " + selectedFeatures[i]); %>
 		
    <% } %> 
    <!-- BACK BUTTON -->
 	<form  name="backbut" action="NextPage.jsp" method="post">
		<input type="hidden" name="back" value="2">
		<input type="submit" value="Back">
	</form>
 <% }%>



</BODY>
</HTML>