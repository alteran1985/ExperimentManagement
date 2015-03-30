package ontologyinterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

//import OntologyTree;

public class OntologyNode implements Comparable<OntologyNode>{
 
	private OWLIndividual data;
    private OntologyNode parent;
    private List<OntologyNode> children;

	private static OWLOntology ontologyToQuery;
	private static OWLDataProperty property_number;
	private static OWLDataProperty property_weight;

    
    static void setOntologyToUse(OWLOntology ontToUse) {
    	ontologyToQuery = ontToUse;
    }
    static void setNumberProperty(OWLDataProperty prop) {
    	property_number = prop;
    }
    static void setWeightProperty(OWLDataProperty prop) {
    	property_weight = prop;
    }
    
    public OntologyNode(OWLIndividual nodeData, OntologyNode parentNode) {
    	this.data = nodeData;
    	this.parent = parentNode;
    	this.children = new ArrayList<OntologyNode>();
    	
    	if (parentNode != null) {
    		//parentNode is null for root experiment node
        	this.parent.children.add(this);
        	Collections.sort(this.parent.children);
    	}
    }
    
    public OWLIndividual getData() {
    	return data;
    }

    public OntologyNode getParent() {
    	return parent;
    }
    
    public List<OntologyNode> getChildren() {
    	return children;
    }
    
    @Override public String toString() {
    	String longName = data.toString();
    	
    	if (longName.indexOf("#") == -1) return "";
		else if (longName.indexOf(">") == -1)
			return (longName.substring(longName.indexOf("#") + 1));
		else
			return longName.substring(longName.indexOf("#") + 1, longName.indexOf(">"));
    }
    
    
    String getClassType(OWLOntology ontologyToUse) {
    	OWLIndividual indiv = data;
    	
    	//Set<OWLClassExpression> types = indiv.getTypes(ontologyToUse); //validity - should be size == 1
    	String typeName = indiv.getTypes(ontologyToUse).iterator().next().toString();
    	if (typeName.indexOf("#") == -1) {
			return "";
		} else if (typeName.indexOf(">") == -1) {
			return (typeName.substring(typeName.indexOf("#") + 1));
		} else{
			String shortName = typeName.substring(typeName.indexOf("#") + 1, typeName.indexOf(">"));
			return shortName;
		}
	}

    /* Returns the number property value asserted for a specified individual.
	 * This is used within the compareTo method for sorting OntologyNodes/individuals so that the OntologyTree is structured appropriately. 
	 * Return:	int number value
	 * 			0 if there is no number associated with this individual in the ontology
	 * 			-1 if there is more than one weight property associated with this individual in the ontology (logical error).
	 */
    public int getNumberPropertyValue(OWLIndividual targetIndiv) {
    	Set<OWLLiteral> literalSet = targetIndiv.getDataPropertyValues(property_number, ontologyToQuery);
    	if (literalSet.size() == 0)
			return 0;
		else if (literalSet.size() > 1) 
    		return -1;
		int num = literalSet.iterator().next().parseInteger();
		return num;
	}
    
	/* Returns the designated weight property value for a specified individual.
	 * This is used within the compareTo method, for sorting OntologyNodes/individuals so that the OntologyTree is structured appropriately. 
	 * Return:	int weight value
	 * 			0 if there is no weight associated with this individual in the ontology
	 * 			-1 if there is more than one weight property associated with this individual in the ontology (logical error).
	 */
	int getWeightPropertyValue(OWLIndividual targetIndiv) {
    	Set<OWLLiteral> literalSet = targetIndiv.getDataPropertyValues(property_weight, ontologyToQuery);
    	if (literalSet.size() == 0)
			return 0;
    	else if (literalSet.size() > 1) 
    		return -1;
		int wt = literalSet.iterator().next().parseInteger();
		return wt;
	}

    
	/* Compares two OntologyNode objects based primarily on the values of their weight OWLDataProperties, and then their number OWLDataProperties.
	 * i.e. node1.compareTo(node2)
	 * Returns:	-1	if (weight1 < weight2) or if ((weight1 == weight2) && (num1 < num2))
	 *			0	if (weight1 == weight2) && (num1 == num2)
	 *			1	if (weight1 > weight2) or if ((weight1 == weight2) && (num1 > num2))
	 */
    @Override
    public int compareTo(OntologyNode node2) {
    	
		int weight1 = getWeightPropertyValue(this.getData());
		int weight2 = getWeightPropertyValue(node2.getData());
		int num1 = getNumberPropertyValue(this.getData());
		int num2 = getNumberPropertyValue(node2.getData());
	
		if (weight1 < weight2) {
			return -1;
		} else if (weight1 > weight2) {
			return 1;
		} else if (weight1 == weight2) {
			if (num1 < num2) {
				return -1;
			} else if (num1 > num2) {
				return 1;
			} else if (num1 == num2) {
				return 0;
			}
		} 
		
		return 0;
	}
	
}