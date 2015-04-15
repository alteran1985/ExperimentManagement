package backend;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import model.*;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
 



public class WriteXML{
	public String root;
	public int index;
	
	public void model(String modelIN){
		Feature rootx;
		FeatureModelLoader loader = new FeatureModelLoader(modelIN);
		FeatureModel featureModel = loader.get_feature_model();
		rootx = featureModel.getRoot();
		
		String x = "" + rootx;
		setRoot(x);
	}
	
	public void setRoot(String rootIN){
		root = rootIN;
	}
	public String getRoot(){
		return root;
	}
	
	public void setSize(int indexIN){
		index = indexIN;
	}
	public int getSize(){
		return index;
	}
	
	
	public void writeXML(String[] parent , String[] feature){
		int size = getSize();
		//System.out.println(size);
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			//root
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement(root);
			doc.appendChild(rootElement);
			
			Element parentFeature;
			Element selectedFeature;
			for(int i = 0 ; i < size ; i++){
				//parent
				parentFeature = doc.createElement(parent[i]);
				rootElement.appendChild(parentFeature);
				
				//selectedFeature
				selectedFeature = doc.createElement("selected");
				selectedFeature.appendChild(doc.createTextNode(feature[i]));
				parentFeature.appendChild(selectedFeature);
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("/home/kullen/workspace/MDA/ExperimentManagementWeb/file.xml"));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
			
			
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} 
		catch (TransformerException tfe) {
			tfe.printStackTrace();
		} 
	}
}