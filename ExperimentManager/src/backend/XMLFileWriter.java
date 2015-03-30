package backend;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 




import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class XMLFileWriter {
	public XMLFileWriter(){
		
	}
	
	public Document loadXML(String filePath){
		File xmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Document doc;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (SAXException | ParserConfigurationException | IOException e1) {
            e1.printStackTrace();
            return null;
        }
	}
	
	/*
	 * The input to the update XML function is an array of strings:
	 * [0]: tag name
	 * [1]: parent
	 * [2] onward: values to be added
	 */
	public String updateXML(boolean isNew, ArrayList<String> info){
		String updatedXML = "";
		//Check if its a new experiment, in which case we load the template, otherwise, load the current experiment XML
		String filePath = "";
		if(isNew){
			filePath="C:/Users/azt0018/git/ExperimentManager/template.xml";
		}else{
			System.out.println("Loading XML file...");
			filePath="C:/Users/azt0018/git/ExperimentManager/current.xml";
		}
		Document doc = loadXML(filePath);

		//Obtain the information we need from the input
        String tagName = info.get(0);
        String parentTag = info.get(1);
        String value = info.get(2);
        Element parent = null;
		//Add the new element to the XML file, if null, create the parent tag
        if(doc.getElementsByTagName(parentTag).item(0)!=null){
        	parent = (Element)doc.getElementsByTagName(parentTag).item(0);
        }else{
        	//Create parent tag
        	Element temp = doc.createElement(parentTag);
        	parent = (Element) doc.getFirstChild().appendChild(temp);
        }
        Element child = doc.createElement(tagName);
        parent.appendChild(child);
        child.appendChild(doc.createTextNode(value));

        //parent.getElementsByTagName("tagName").item(0).getFirstChild().setNodeValue(value);
        //Write the updated document to file
        doc.getDocumentElement().normalize();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("C:/Users/azt0018/git/ExperimentManager/current.xml"));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			System.out.println("XML file updated successfully");
			printDocument(doc, System.out);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updatedXML = printDocument(doc, updatedXML);
		return updatedXML;
	}
	
	public static void printDocument(Document doc, OutputStream out) {
	    try{
		TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	    }catch(Exception e){
	    	System.out.println("An error occurred");
	    }
	}
	public static String printDocument(Document doc, String output) {
	    try{
		TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	    DOMSource source = new DOMSource(doc);
	    StreamResult result = new StreamResult(new StringWriter());
	    
	    transformer.transform(source, result);
	    return result.getWriter().toString();
	    }catch(Exception e){
	    	System.out.println("An error occurred");
	    	return null;
	    }
	}
}
