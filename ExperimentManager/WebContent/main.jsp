<%@page import="backend.*"%>
<%@page import="model.*" %>
<%@page import="configuration.*" %>
<%@page import="java.util.*" %>
<%@page import="java.io.*" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript" src="functions.js"></script>
<title>Experiment Management System</title>
</head>
<body style="margin:0px;padding: 0px;font: 8pt/14pt 'Lucida Grande', Verdana, Helvetica, sans-serif;color:#413e35;">

<%
//Retrieve the phase value from the url
String phase = request.getParameter("phase");

//Load the mediator
Mediator mediator;
if((Mediator)session.getAttribute("mediator")==null){
	//Retrieve the experiment name from the url
	String experimentName = request.getParameter("experimentName");
	String modelFileName = request.getParameter("modelFileName");
	String modelLocation = request.getParameter("modelFolder");
	System.out.println("Experiment name: " + experimentName);
	System.out.println("Experiment file location: " + modelFileName);
	mediator = new Mediator(experimentName, modelFileName, modelLocation);
	session.setAttribute("mediator", mediator);
	//Begin the experiment
	mediator.beginNewExperiment();
}else{
	mediator = (Mediator)session.getAttribute("mediator");
}

if(phase==null){
	//Recover model data
	HashMap<String, HashMap<String, String[]>> modelData = mediator.getModelData();
	//Retrieve the model information for display
	String modelName = modelData.get("model").get("name")[0];
	HashMap<String, String[]> responses = modelData.get("responses");
	HashMap<String, String[]> parameters = modelData.get("parameters");
	int numberOfResponses = responses.size();
	int numberOfParameters = parameters.size();
%>
	<H2 style="text-align:center">Experiment Management System</H2>
	<P style="text-align:center;">You will be creating an experiment for the <%= modelName  %> simulation model.</P>
	<P style="text-align:center;">This model has the following <%= numberOfResponses %> responses available:</P>
	<P style="text-align:center;">
	<%
	Iterator<Map.Entry<String, String[]>> iter = responses.entrySet().iterator();
	int counter = 1;
	while(iter.hasNext()){ 
		Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)iter.next();
		%>
		[<%=counter %>]<%=entry.getValue()[0] %><BR />
		<%
		counter++;
	}%>
	</P>
	
	<P style="text-align:center;">This model has the following <%= numberOfParameters %> configurable parameters available:</P>
	<P style="text-align:center;">
	<%
	iter = parameters.entrySet().iterator();
	counter = 1;
	while(iter.hasNext()){ 
		Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)iter.next();
		%>
		[<%=counter %>]<%=entry.getValue()[0] %><BR />
		<%
		counter++;	
	}%>
	</P>
	
	<FORM action='main.jsp' method='GET' style="text-align:center;">
	 <INPUT type='hidden' value='Objective' name='phase'>
	 <INPUT type='submit' value='Start!'>
	</FORM>
<%}else if(phase.equals("Done")){
%>
	<DIV id="header" style="background-color: #03244d;height:50px;padding-top:10px;padding-bottom:10px;">
	<H1 style="color:white;text-align:center;">Simulation Experiment Management System</H1>
	</DIV>
	<H2 style="text-align:center;">Your experiment design has been completed!</H2>
<%
}else{
%><%= mediator.runPhase(phase)
%>
<%} %>
</body>
</html>