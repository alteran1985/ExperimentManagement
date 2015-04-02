<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="backend.*"%>
<%
	Mediator mediator = (Mediator)session.getAttribute("mediator");
	int numberOfFactorLevels = Integer.parseInt(request.getParameter("numberOfFactorLevels"));
	String outputHTML = mediator.generateFactorValueScreen(numberOfFactorLevels);	
%>
<%= outputHTML %>