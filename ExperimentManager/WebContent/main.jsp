<%@page import="backend.*"%>
<%@page import="model.*" %>
<%@page import="configuration.*" %>
<%@page import="java.util.*" %>
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
Mediator mediator = new Mediator();
if(phase==null){
%>
	<H2 style="text-align:center;">Welcome to the Experiment Management System!</H2>
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
mediator.runPhase(phase);
%>
<%= mediator.generateScreen() %>
<%} %>
</body>
</html>