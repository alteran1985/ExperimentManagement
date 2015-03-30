<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Experiment Management System > Begin a new experiment</title>
<script type="text/javascript" src="functions.js"></script>
</head>
<body style="margin:0px;padding: 0px;font: 8pt/14pt 'Lucida Grande', Verdana, Helvetica, sans-serif;color:#413e35;">
	<h2 style="text-align:center;">Begin a new experiment</h2>
	<p style="text-align:center;"><form action="javascript:beginNewExperiment();">
		<p style="text-align:center;">Select a simulation model XML file:</p>
		<p style="text-align:center;"><input type="file" id="modelFile" accept=".xml"></p>
		<p style="text-align:center;">Type a name for this experiment:</p>
		<p style="text-align:center;"><input type="text" id="experimentName"></p>
		<p style="text-align:center;"><input type="submit" value="Begin experiment"></p>
	</form>
</body>
</html>