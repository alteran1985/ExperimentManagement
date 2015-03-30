package ontologyinterface;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;

public class OntologyStructureGenerator {
	
	static OWLOntology currentOntology;
	OWLObjectProperty prop_HasA;
	OWLClass experimentClass;
	String xmlStruct = "";
	

	public OntologyStructureGenerator(OWLOntology ontToUse) {
		currentOntology = ontToUse;
	}

	/* Creates structured text representation of the provided OntologyTree object.
	 * Writes this structured xml String object to an xml file, located at the designated location.
	 * Param:	OntologyTree the tree built from the current ontology, which is to be saved to an xml file. 
	 * 			String the indent to use at the beginning of each line. Incremented by the value of indentToUse for each new indentation.
	 * 			String path of the location to save the xml file.
	 * Return:	boolean value representing success of operation.
	 */
	public boolean writeTree(OntologyTree treeToUse, String indentToUse, String xmlDestinationLocation) {
		getTreeStructure(treeToUse.getRootNode(), indentToUse, indentToUse);
		if (xmlStruct.equals("")) {
			return false;
		}
		File xmlFile = new File(xmlDestinationLocation);
		FileWriter output;
		try {
			output = new FileWriter(xmlFile);
			 try {
					output.write(xmlStruct);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
						return false;
					}
				}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		return true;
	}
	
	public void printTree(OntologyTree treeToUse, String indentToUse) {
		getTreeStructure(treeToUse.getRootNode(), indentToUse, indentToUse);
		System.out.println(xmlStruct);
	}
	

	
	/* Recursive method.
	 * Saves the String value of the structured xml representing the experiment in the the global String variable xmlStruct
	 * Recursive method.
	 * Sets xmlStruct to empty string if the currentOntology is not set.
	 * */
    private void getTreeStructure(OntologyNode node, String indent, String originalIndent) {
    	if (currentOntology == null) {
    		xmlStruct = "";
    	} else {
	    	xmlStruct += indent + getXMLTag(node.getData()) + "\n";
	        Iterator<OntologyNode> iter = node.getChildren().iterator();
	        while (iter.hasNext()) {
	        	getTreeStructure(iter.next(), originalIndent + indent, originalIndent);
	        }
	        xmlStruct += indent + getXMLCloseTagFromIndiv(node.getData()) + "\n";	
    	}
    }
    
    /* Generates xml tag for a specific OWLIndividual, formatted and containing the appropriate information as provided in the current ontology
     * Param:	OWLIndividual to examine
     * Return:	String formatted xml tag 
	 */
    static String getXMLTag(OWLIndividual indivToDescribe) {
		HashMap<String, String> indivInfo = 
    			findDataOfIndiv(indivToDescribe, currentOntology);

		String xml = "<";
		xml += indivInfo.get("classType") + " ";
		
		Set<String> keySet = indivInfo.keySet();
		ArrayList<String> ignoreValues = new ArrayList<String>();
		ignoreValues.add("classType"); ignoreValues.add("belongsTo"); ignoreValues.add("hasA"); 
		ignoreValues.add("weight"); //ignoreValues.add("weight");  

		for (String str : keySet) {
			if (!ignoreValues.contains(str)) {
				xml += str;
				xml += "=\"";
				xml += indivInfo.get(str) + "\" ";
			}
		}
		
		xml = xml.substring(0, xml.length() - 1) + ">";
		
		return xml;
	}

	
    /* Gets the closing tag for an OWLIndividual
     * Param:	OWLIndividual a specific OWLIndividual, for which to generate the appropriate closing xml tag 
     * Return:	closing xml tag formatted as <\typeName>
     * 			"" empty string if the targetIndividual is asserted to be more than one type (logical error.)
     */
    String getXMLCloseTagFromIndiv(OWLIndividual targetIndiv) {
    	Set<OWLClassExpression> classExps = targetIndiv.getTypes(currentOntology);
    	if (classExps.size() > 1) {
    		return "";
    	}
		String typeName = getShortForm(classExps.iterator().next());
    	return "<\\" + typeName + ">";
    }

	/* Finds information about an individual, as defined in the provided ontology.
	 * Returns information in a HashMp<String, String> object
	 */
	static HashMap<String, String> findDataOfIndiv(OWLIndividual targetIndiv, OWLOntology ontologyToNavigate) {
				
		HashMap<String, String> dataInfoMap = new HashMap<String, String>();
		
		{
			Map<OWLDataPropertyExpression, Set<OWLLiteral> > dataPropertyMap 
				= targetIndiv.getDataPropertyValues(ontologyToNavigate);
			Set<OWLDataPropertyExpression> dataPropertySet = dataPropertyMap.keySet();
			
			if (!dataPropertySet.isEmpty()) {
				for (OWLDataPropertyExpression dataPropExpr : dataPropertySet) {
					String property = getShortForm(dataPropExpr.asOWLDataProperty().toString());
					Set<OWLLiteral> literalSet = targetIndiv.getDataPropertyValues(dataPropExpr, ontologyToNavigate);
					for (OWLLiteral literal : literalSet) {
						dataInfoMap.put(property, literal.getLiteral());
					}
				}
			}
		}
		
		{
			Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objPropertyMap = targetIndiv.getObjectPropertyValues(ontologyToNavigate);
			Set<OWLObjectPropertyExpression> objPropertySet = objPropertyMap.keySet();

			if (!objPropertySet.isEmpty()) {
				for (OWLObjectPropertyExpression objPropExpr : objPropertySet) {
					String property = getShortForm(objPropExpr.asOWLObjectProperty().toString());
					Set<OWLIndividual> indivSet = targetIndiv.getObjectPropertyValues(objPropExpr, ontologyToNavigate);
					for (OWLIndividual indivInProperty : indivSet) {
						dataInfoMap.put(property, getShortForm(indivInProperty));
					}
				}
			}
		}
		dataInfoMap.put("classType", getShortForm(targetIndiv.getTypes(ontologyToNavigate).iterator().next().toString()));
		return dataInfoMap;
		
	}
	
	/* Returns the short form name of an item. 
	 * (i.e. the substring after the '#' in the item's iri)
	 * Works for most OWL API objects/classes/types.
	 */
    static <E> String getShortForm(E item) {
		String fullName = item.toString();
		if (fullName.indexOf("#") == -1) {
			return "";
		} else if (fullName.indexOf(">") == -1) {
			return (fullName.substring(fullName.indexOf("#") + 1));
		} else{
			return fullName.substring(fullName.indexOf("#") + 1, fullName.indexOf(">"));
		}
	}
	
	
}
