package ontologyinterface;
import java.util.HashMap;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class OntologyTree{
	
	OWLOntology currentOnt;
	private OntologyNode currentNode; //build as you go
	private OntologyNode root;
	private HashMap<OWLIndividual, OntologyNode> treeStorage;
	
	public OntologyTree(OWLIndividual rootData, OWLOntology ont, HashMap<String, OWLDataProperty> propertyMap) {
		currentOnt = ont;
		OntologyNode.setOntologyToUse(currentOnt);
        OntologyNode.setNumberProperty(propertyMap.get("number"));
        OntologyNode.setWeightProperty(propertyMap.get("weight"));
        
        OntologyNode newRootNode = new OntologyNode(rootData, null);
        treeStorage = new HashMap<OWLIndividual, OntologyNode>();
        treeStorage.put(rootData, newRootNode);
        root = newRootNode;
        currentNode = newRootNode;
    }
    
    public OntologyNode getNode(OWLIndividual indivToRetrieve) {
    	return treeStorage.get(indivToRetrieve);
    }
    
    public OntologyNode getRootNode() {
    	return root;
    }
    
    public OntologyNode getCurrentNode() {
    	return currentNode;
    }
    public OWLIndividual getRecentIndiv() {
    	return currentNode.getData();
    }
    
    /* Creates and adds a new node to the tree, containing the given OWLIndividul data.
     * Sets currentNode to the newly created node, if created successfully.
     * Returns the node that was created. 
     * Returns null if: No node exists in tree with the provided parentData individual, 
     * 				or  A node already exists with the provided newData individual.
     */
    public OntologyNode addNewChild(OWLIndividual newData, OWLIndividual parentData) {
    	OntologyNode parentNode = getNode(parentData);
    	if (parentNode == null) {
    		return null;
    	}
    	if (getNode(newData) != null) {
    		return null;
    	}
    	OntologyNode newN = new OntologyNode(newData, parentNode);
    	treeStorage.put(newData, newN);
    	currentNode = newN;
    	return newN;
    }
      
    
    /* Returns the OWLIndividual that the new individual will be a child of. 
     * Accepts String parameter representing the type of the new individual to be added. (i.e. "Factor")
     * Finds the parent type (using base ontology) of the given indivType 
      		(i.e. parent type of Factor is Iteration.) 
     * Starts with the currentNode and works upwords, until matching type is found.
     */
    public OWLIndividual getRecentParentOfType(OWLIndividual parentTypeIndividual) {
    	String parentType = getShortForm(parentTypeIndividual.toString());
    	OntologyNode navigatingNode = currentNode;
    	
    	String navigatingNodeClassType = navigatingNode.getClassType(currentOnt);
    	
    	while (!(navigatingNodeClassType.equals(parentType))) {
    		navigatingNode = navigatingNode.getParent();
    		if (navigatingNode == null)
    			return null;
    		navigatingNodeClassType = navigatingNode.getClassType(currentOnt);
    	}
    	
    	if (navigatingNodeClassType.equals(parentType)) {
    		return navigatingNode.getData();
    	}
    	
    	return null;
    }
    
 
  
    // Returns the short form of the name provided.     * 
    String getShortForm(String longName) {
    	if (longName.indexOf("#") == -1) {
			return "";
		} else if (longName.indexOf(">") == -1) {
			return (longName.substring(longName.indexOf("#") + 1));
		} else{
			return longName.substring(longName.indexOf("#") + 1, longName.indexOf(">"));
		}
    }
}

