var browser;

function GetXmlHttpObject()
{
var xmlHttp=null;
browser = navigator.appName;

 // Internet Explorer
if(browser== "Microsoft Internet Explorer")
  {
  xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
  }
else{
  xmlHttp=new XMLHttpRequest();
}

return xmlHttp;
}

//Function that makes an AJAX request to the server
function requestAJAX(url, info, container){
 var request = GetXmlHttpObject();
     request.open("POST", url, true);

     //Send the proper header information along with the request
     request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
     request.setRequestHeader("Content-length", info.length);
     request.setRequestHeader("Connection", "close");
     
     request.onreadystatechange = function() {
      if (request.readyState == 4) {
       var response = request.responseText;
       container.innerHTML = response;
     }
    };
    request.send(info);
}

//Function that validates whether a field has a numeric value
function IsNumeric(strString)
   //  check for valid numeric strings	
   {
   var strValidChars = "0123456789.-";
   var strChar;
   var blnResult = true;

   if (strString.length == 0) return false;

   //  test strString consists of valid characters listed above
   for (var i = 0; i < strString.length && blnResult == true; i++)
      {
      strChar = strString.charAt(i);
      if (strValidChars.indexOf(strChar) == -1)
         {
         blnResult = false;
         }
      }
   return blnResult;
}

//Updates the configuration
function updateConfiguration(currentPhase, controlType, dataSource){
	//alert("Entered the update configuration function!");
	//Get check boxes from the form
	var features = document.getElementById(currentPhase);
	//Variable that will store the parameters for the URL request
	var data = "currentPhase=" + currentPhase + "&controlType=" + controlType + "&dataSource=" + dataSource + "&value=";
	switch (controlType){
	case "ComboBox": //If combo box, construct the data string by sending the value of the selected index
		data = data + features.options[features.selectedIndex].text;
		break;
	case "TextBox":
		data = data + features.value;
		break;
	default:
		break;
	}
	//alert(data);
	var outputDiv = document.getElementById("updatedConfiguration");
	//Clear out the DIV
	outputDiv.innerHTML = "";
	requestAJAX("UpdateConfiguration.jsp", data, outputDiv);
}

function beginNewExperiment(){
	var experimentName = document.getElementById("experimentName").value;
	var modelFileName = document.getElementById("modelFile").value;
	window.location.replace("main.jsp?experimentName=" + experimentName + "&modelFileName=" + modelFileName);
}