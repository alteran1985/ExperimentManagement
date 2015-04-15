<%@page import="backend.FeatureModelLoader"%>
<%@page import="model.*" %>
<%@page import="configuration.*" %>
<%@page import="java.util.*" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%
	FeatureModelLoader loader = new FeatureModelLoader("model.xml");
	FeatureModel model = loader.get_feature_model();
	Configuration configuration = new Configuration(model);
	
	out.println("<br /><br />Unselected features");
	Set<Feature> unselected_features = configuration.getUnSelectedFeatures();
	Iterator<Feature> iter = unselected_features.iterator();
	while (iter.hasNext()){
		out.println("<br />" + iter.next().getName());
	}
	
%>