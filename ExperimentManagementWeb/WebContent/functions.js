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
    }
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
function updateConfiguration(){
	//alert("Entered the update configuration function!");
	//Get check boxes from the form
	
	var features = document.getElementsByName("features");
	//Variable that will store the parameters for the URL request
	selected = true;
	var data = features[0].value + "=" + selected + "& ";
	   //data = data + features[1].value + "=" + features[1].checked;// + "& ";
	    //data = data + features[2].value + "=" + features[2].checked;
	
	var outputDiv = document.getElementById("updatedConfiguration");
	//Clear out the DIV
	System.out.print(features);
	outputDiv.innerHTML = "";
	requestAJAX("UpdateConfiguration.jsp", data, outputDiv);
}

//Load the attendee list according to a certain sorting parameter
/*function load_list(sort_parameter){
   //Calls AJAX
   var data = "sort=" + sort_parameter;
   var list_container = document.getElementById("attendee_list");
   list_container.innerHTML = "";
   peticionAJAX("list_all.php", data, list_container);
}*/