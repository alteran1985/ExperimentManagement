package ontologyinterface;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;


public class OntologyBuilder {
	
	OWLOntology currentOntology;
	OWLOntology baseOntology;

	String currentOntologyLocation;
	OWLOntologyDocumentTarget saveDocumentTarget;
	
	OntologyTree tree;
	OntologyNode rootNode;	
	
	OWLOntologyManager managerGlobal;
	OWLDataFactory factoryGlobal;
	
	PrefixManager pm;
	HashMap<String, OWLDataProperty> dataPropertiesMap; //String Key: lowercase name of the property
	HashMap<String, OWLObjectProperty> objectPropertiesMap;
	OWLClass experimentClass;
	

	public OntologyBuilder(String baseOntologyLocation, String destinationLocation) {
		this(baseOntologyLocation, destinationLocation, false);
	}
	
	/* Constructor for the OntologyBuilder class.
	 * Params: String baseOntologyLocation path (location of ontology to infer structure from)
	 * 			String locationToLoadAndSaveFrom path (location where the ontology for this experiment will be saved. 
	 * If !reload:
	 * 		Automatically adds an individual of the class "Experiment" and with the name "Experiment001" to the current ontology.
	 * Reload is currently not used. Will be used when restarting from an existing ontology/ontology build.
	 */
	public OntologyBuilder(String baseOntologyLocation, String locationToLoadAndSaveFrom, boolean reload) {
		managerGlobal = OWLManager.createOWLOntologyManager();
		factoryGlobal = managerGlobal.getOWLDataFactory();
		
		baseOntology = returnBaseOntology(baseOntologyLocation);
		currentOntologyLocation = locationToLoadAndSaveFrom;
		saveDocumentTarget = new FileDocumentTarget(new File(locationToLoadAndSaveFrom));
		
		if (!reload)
			currentOntology = createNewOntologyFromDefault(locationToLoadAndSaveFrom);
		else if (reload)
			currentOntology = createOntologyFromExisting(locationToLoadAndSaveFrom); 
		
		if (currentOntology == null)
			throw new NullPointerException();
		
		pm = new DefaultPrefixManager(currentOntology.getOntologyID().getOntologyIRI().toString());		
		experimentClass = factoryGlobal.getOWLClass("#Experiment", pm);
		
		retrieveAndStorePropertiesFromOntology();
		
		OWLNamedIndividual experimentIndiv = null;
		
		if (!reload) {
			experimentIndiv = factoryGlobal.getOWLNamedIndividual("#Experiment001", pm);
			OWLDataPropertyAssertionAxiom dataPropertyAssertion = 
					factoryGlobal.getOWLDataPropertyAssertionAxiom(getDataProperty("number"), experimentIndiv, "1"); 
			OWLDeclarationAxiom newExperimentDeclarationAxiom = factoryGlobal.getOWLDeclarationAxiom(experimentIndiv);
			OWLClassAssertionAxiom classAssertion = factoryGlobal.getOWLClassAssertionAxiom(experimentClass, experimentIndiv); 
			
			ArrayList<OWLOntologyChange> changeList = new ArrayList<OWLOntologyChange>();
			changeList.addAll(managerGlobal.addAxiom(currentOntology, newExperimentDeclarationAxiom));
			changeList.addAll(managerGlobal.addAxiom(currentOntology, dataPropertyAssertion));
			changeList.addAll(managerGlobal.addAxiom(currentOntology, classAssertion));
			managerGlobal.applyChanges(changeList);
		}
		
		try {
			managerGlobal.saveOntology(currentOntology, new OWLXMLOntologyFormat(), saveDocumentTarget);
		} catch (OWLOntologyStorageException e) { e.printStackTrace(); }
		

		if (!reload) {
			tree = new OntologyTree(experimentIndiv, currentOntology, dataPropertiesMap);
			rootNode = tree.getRootNode();
		} else if (reload) {
			tree = createTreeFromOntology();
			rootNode = tree.getRootNode();
		} 
		
	}
	

	/* Loads and returns the ontology saved at the provided location.
	 * Param: String baseOntologyLocation path of the default ontology.
	 * Return: OWLOntology that is the base ontology 
	 */
	private OWLOntology returnBaseOntology(String baseOntologyLocation) {
		OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
		
		File baseFile = new File(baseOntologyLocation);
		OWLOntology baseOnt = null;
		try {
			baseOnt = mgr.loadOntologyFromOntologyDocument(baseFile);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			return null;
		}
		
		return baseOnt;
	}
	
	/* Loads and returns new ontology containing all elements present in the provided base ontology.
	 * Param: String baseOntologyLocation path of the default ontology.
	 * Return: OWLOntology that is copy of the base ontology 
	 */
	private OWLOntology createNewOntologyFromDefault(String destinationFileLocation) {
		if (baseOntology == null)
			return null;
		Set<OWLOntology> ontologiesToInclude = new HashSet<OWLOntology>();
		ontologiesToInclude.add(baseOntology);

		OWLOntology duplicateOntology;
		try {
			IRI baseIRI = baseOntology.getOntologyID().getOntologyIRI(); //structured_base
			duplicateOntology = managerGlobal.createOntology(baseIRI, ontologiesToInclude);
		} catch (OWLOntologyCreationException e) {
			System.out.println("Could not create ontology: " + e);
			e.printStackTrace();
			return null;
		}
		
		managerGlobal.setOntologyFormat(duplicateOntology, new OWLXMLOntologyFormat());
		
		try {
			managerGlobal.saveOntology(duplicateOntology, new OWLXMLOntologyFormat(), saveDocumentTarget);
		} catch (OWLOntologyStorageException e) { 
			e.printStackTrace(); 
			return null;
		}
	
		return duplicateOntology;
	}	
	
	private OWLOntology createOntologyFromExisting(String sourceDestinationFileLocation) {
		File file = new File(sourceDestinationFileLocation);
		OWLOntology copyOnt = null;
		try {
			copyOnt = managerGlobal.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			return null;
		}
	
		managerGlobal.setOntologyFormat(copyOnt, new OWLXMLOntologyFormat());
		
		try {
			managerGlobal.saveOntology(copyOnt, new OWLXMLOntologyFormat(), saveDocumentTarget);
		} catch (OWLOntologyStorageException e) { 
			e.printStackTrace(); 
			return null;
		}
		return copyOnt;
	}
	
	/* Generates a structured representation of the current ontology, and stores it at the designated location.
	 * Param: String path of the location to save the xml file.
	 * Return boolean representing success of the write and save.
	 */
	public boolean writeStructureToFile(String xmlPath) {
		OntologyStructureGenerator structureGenerator = new OntologyStructureGenerator(currentOntology);
		return structureGenerator.writeTree(tree, "\t", xmlPath);
	}
	
	/* Function for debugging purposes. 
	 * Generates a structured representation of the current ontology, and prints it to the console.
	 */
	public void printStructure() {
		OntologyTree treeToPrint = getOntologyTree();
		OntologyStructureGenerator structureGenerator = new OntologyStructureGenerator(currentOntology);
		structureGenerator.printTree(treeToPrint, "\t");
	}
	
	/* Adds a new individual of the specified type, which 'belongsTo' to the most recently created individual 
	 * 		(highest 'number' property value) of its designated parent type.
	 * Parent type is found from the base ontology. Finds the most recent individual of its designated parent type.
	 * Returns false if no individual of the parent type exists in the ontology
	 * Adds the new individual to the ontology and OntologyTree structure as a child of the recent parent individual.
	 * Param: String the class name that the new individual will be  type of.
	 * Return boolean representing success of the operation.
	 */
	public boolean addNewIndividual(String typeOfNewIndividual) {
		OWLNamedIndividual newTypeIndividual = factoryGlobal.getOWLNamedIndividual("#" + typeOfNewIndividual, pm);
		OWLIndividual parentTypeIndividual = getParentType(newTypeIndividual);
		OWLIndividual parentIndiv = getLastIndividualOfType(parentTypeIndividual);
		//OWLIndividual parentIndiv = tree.getRecentParentOfType(parentClassType); //add getShortForm to findBelongsTO method for concise-tivity?
		if (parentIndiv == null)
			return false;
		return addIndividual(newTypeIndividual, parentIndiv);
	}
	
	/* Adds a new individual of the specified type, which 'belongsTo' a specified individual.
	 * Parent type is found from the base ontology. 
	 * Parent individual is found by comining the parentIDNumber string with the parent type name.
	 * Returns false if no individual of the parent type exists in the ontology
	 * Adds the new individual to the current ontology and OntologyTree structure as a child of the recent parent individual.
	 * Param:	String the class name that the new individual will be type of.
	 * 			String id number of the parent individual the new individual 'belongsTo' 
	 * Return boolean representing success of the operation.
	 */
	public boolean addNewIndividual(String typeOfNewIndividual, String parentIDNumber) {
		OWLNamedIndividual newTypeIndividual = factoryGlobal.getOWLNamedIndividual("#" + typeOfNewIndividual, pm);
		OWLIndividual parentClassType = getParentType(newTypeIndividual);
		String parentNameID = getShortForm(parentClassType) + ("000" + parentIDNumber).substring(parentIDNumber.length());
		OWLNamedIndividual parentIndiv = factoryGlobal.getOWLNamedIndividual("#" + parentNameID, pm);
		if (currentOntology.getAxioms(parentIndiv).size() == 0) {
			//there are no assertions in the current ontology that include a parent by the provided name.
			//cannot add successfully.
			return false;
		}
		
		return addIndividual(newTypeIndividual, parentIndiv);
	}
	
	/* Adds information to ontology and ontology tree asserting relationship between two provided OWLIndividuals.
	 * Method is called by the addNewIndividual(String typeOfNewIndividual) and addNewIndividual(String typeOfNewIndividual, String parentID) methods.
	 * Adds assertions to currentOntology and saves the updated ontology.
	 * Param: OWLNamedIndividual representing the type of the new individual to be added,
	 * 		OWLIndividual the parent individual, as found in the OntologyTree structure.
	 * Returns: false if individual was not able to be created, the ontology was not able to be saved, 
	 * 		or if a new node belong to the provided parent could not be created with the child data 
	 */
	private boolean addIndividual(OWLNamedIndividual newIndividualType, OWLIndividual parentIndividual) {
		
		OWLClass newIndividualClass = factoryGlobal.getOWLClass("#" + getShortForm(newIndividualType), pm);
		int instanceNumber = newIndividualClass.getIndividuals(currentOntology).size();
		String instanceNumString = instanceNumber + "";
		int assignedWeight = getWeightPropertyValue(newIndividualType);

		String newIndivID = getShortForm(newIndividualType) + ("000" + instanceNumber).substring(instanceNumString.length());
		OWLNamedIndividual newIndividual = factoryGlobal.getOWLNamedIndividual("#" + newIndivID, pm);
		OWLDeclarationAxiom newIndivDeclarationAxiom = 
				factoryGlobal.getOWLDeclarationAxiom(newIndividual);
		OWLClassAssertionAxiom classAssertion = 
				factoryGlobal.getOWLClassAssertionAxiom(newIndividualClass, newIndividual); 
		
		OWLObjectPropertyAssertionAxiom hasAAssertion = 
			factoryGlobal.getOWLObjectPropertyAssertionAxiom(getObjectProperty("hasA"), parentIndividual, newIndividual);
		OWLObjectPropertyAssertionAxiom belongsToAssertion = 
			factoryGlobal.getOWLObjectPropertyAssertionAxiom(getObjectProperty("belongsTo"), newIndividual, parentIndividual);
		OWLDataPropertyAssertionAxiom numberPropertyAssertion = 
				factoryGlobal.getOWLDataPropertyAssertionAxiom(getDataProperty("number"), newIndividual, instanceNumber); 

		OWLDataPropertyAssertionAxiom weightPropertyAssertion = 
				factoryGlobal.getOWLDataPropertyAssertionAxiom(getDataProperty("weight"), newIndividual, assignedWeight); 

		ArrayList<OWLOntologyChange> changeList = new ArrayList<OWLOntologyChange>();
		changeList.addAll(managerGlobal.addAxiom(currentOntology, newIndivDeclarationAxiom));
		changeList.addAll(managerGlobal.addAxiom(currentOntology, classAssertion));
		changeList.addAll(managerGlobal.addAxiom(currentOntology, hasAAssertion));
		changeList.addAll(managerGlobal.addAxiom(currentOntology, belongsToAssertion));
		changeList.addAll(managerGlobal.addAxiom(currentOntology, numberPropertyAssertion));
		changeList.addAll(managerGlobal.addAxiom(currentOntology, weightPropertyAssertion));
		
		managerGlobal.applyChanges(changeList);
		OWLOntologyDocumentTarget outputTarget = new FileDocumentTarget(new File(currentOntologyLocation));
		try {
			managerGlobal.saveOntology(currentOntology, new OWLXMLOntologyFormat(), outputTarget);
		} catch (OWLOntologyStorageException e) { 
			e.printStackTrace(); 
			return false;
		}
				
		OntologyNode newNode = tree.addNewChild(newIndividual, parentIndividual);	
		if (newNode == null)
			return false;
		return true;
	}
	
	
	/* Associates a data property and value with the most recently created individual.
	 * Adds the OWLDataPropertyAssertion to the current ontology.
	 * Param:	String name of the OWLDataProperty as defined in the ontology. 
	 * 			String value of the data to be assigned.
	 * (Note:	All data property values in the ontology will be stored as Strings.
	 * 			Individuals may have data data property called #type that states the true data type of the values.
	 * Return:	Boolean value indicating success of operation. 
	 * 			Returns false if the property to apply is not defined in the base ontology, 
	 * 			or if the ontology cannot be saved.
	 */
	public boolean addNewDataProperty(String propertyType, String data) {
		
		OWLDataProperty propertyToApply = getDataProperty(propertyType);
		
		if (propertyToApply == null)
			return false;
		
		OWLIndividual targetIndiv = tree.getRecentIndiv();
		try {
			OWLDataPropertyAssertionAxiom dataPropertyAssertion = factoryGlobal.getOWLDataPropertyAssertionAxiom(propertyToApply, targetIndiv, data); 
			OWLOntologyDocumentTarget outputTarget = new FileDocumentTarget(new File(currentOntologyLocation));
			managerGlobal.applyChanges(managerGlobal.addAxiom(currentOntology, dataPropertyAssertion));
			managerGlobal.saveOntology(currentOntology, new OWLXMLOntologyFormat(), outputTarget);
		} catch (OWLOntologyStorageException e) { 
			e.printStackTrace(); 
			return false;
		}	
		return true;
	}
	
	/* Associates a data property and value with a specified existing individual.
	 * Adds the new OWLDataPropertyAssertion to the current ontology, and saves the ontology file.
	 * Param:	String name of the OWLDataProperty, as defined in the ontology. 
	 * 			String value of the data to be assigned.
	 * 			String type name of the individual adding property to
	 * 			String id number of the individual adding property to
	 * i.e. addNewDataProperty("value", "10", "Factor_Value", "5") --> adds property to Factor_Value005
	 * Return:	Boolean value indicating success of operation. 
	 * 			Returns false if the property to apply is not defined in the base ontology, 
	 * 			or if the ontology cannot be saved.
	 */
	public boolean addNewDataProperty(String propertyType, String data, String indivType, String indivNumID) {
		String nameID = indivType + ("000" + indivNumID).substring(indivNumID.length());
		return addNewDataProperty(propertyType, data, nameID);
	}

	/* Associates a data property and value with a specified existing individual.
	 * Adds the new OWLDataPropertyAssertion to the current ontology, and saves the ontology file.
	 * Param:	String name of the OWLDataProperty, as defined in the ontology. 
	 * 			String value of the data to be assigned.
	 * 			String full short form name of the individual to add the property to
	 * i.e. addNewDataProperty("value", "10", "Factor_Value005") --> adds property to Factor_Value005
	 * Return:	Boolean value indicating success of operation. 
	 * 			Returns false if the property to apply is not defined in the base ontology, 
	 * 			or if the ontology cannot be saved.
	 */
	public boolean addNewDataProperty(String propertyType, String data, String fullIndivID) {
		OWLDataProperty propertyToApply = getDataProperty(propertyType);
		if (propertyToApply == null)
			return false;
		
		OWLNamedIndividual targetIndiv = factoryGlobal.getOWLNamedIndividual("#" + fullIndivID, pm);
		if (currentOntology.getAxioms(targetIndiv).size() == 0) {
			//there are no assertions in the current ontology that include a parent by the provided name. cannot add successfully
			return false;
		}
		
		try {
			OWLDataPropertyAssertionAxiom dataPropertyAssertion = factoryGlobal.getOWLDataPropertyAssertionAxiom(propertyToApply, targetIndiv, data); 
			OWLOntologyDocumentTarget outputTarget = new FileDocumentTarget(new File(currentOntologyLocation));
			managerGlobal.applyChanges(managerGlobal.addAxiom(currentOntology, dataPropertyAssertion));
			managerGlobal.saveOntology(currentOntology, new OWLXMLOntologyFormat(), outputTarget);
		} catch (OWLOntologyStorageException e) { 
			e.printStackTrace(); 
			return false;
		}	
		
		return true;
	}
	
	
	/* Builds an instance of a OntologyTree object, from an existing OWLOntology .owl file,
	 * in order to represent an ontology's structure.
	 * Examines the OWLOntology stored in the currentOntology variable.
	 * Locates and begins with the individual that is an instance of the Experiment class (There should only be one.)
	 * Calls the addSubIndividualsToTree in order to start the recursive call of adding an individual's children to the tree.
	 * Returns:		OntologyTree that was created, containing the elements from the current ontology
	 */
	OntologyTree createTreeFromOntology() {
		Set<OWLIndividual> experimentIndividuals = experimentClass.getIndividuals(currentOntology);
		OWLIndividual experimentIndiv = null;
		for (OWLIndividual exp : experimentIndividuals) {
			String shortName = getShortForm(exp);
			if (!(shortName.equals("Experiment"))) {
				//finds the first experiment individual that is not the base "#Experiment" individual
				experimentIndiv = exp;
			}
		}
		OntologyTree newTree = new OntologyTree(experimentIndiv, currentOntology, dataPropertiesMap);
		newTree = addSubIndividualsToTree(newTree, experimentIndiv, null);
		
		return newTree;
	}

	/* Recursive method; adds relationships in an OntologyTree between provided child and parent OWLIndividuals
	 * For use with restarting OntologyBuilder from an existing ontology.
	 * Param:	OntologyTree tre created so far, OWLIndividual the individual to be examined, 
	 * 			and OWLIndividual the individual for which the currentIndiv individual should be considered a 'child.'
	 * Return:	OntologyTree object with all children/sub-individuals of a given parent associated with that parent
	 */
	OntologyTree addSubIndividualsToTree(OntologyTree currentTree, OWLIndividual currentIndiv, OWLIndividual parentIndiv) {
		if (parentIndiv != null) {
			currentTree.addNewChild(currentIndiv, parentIndiv);
		}
		
		Set<OWLIndividual> subIndivs = findSubIndividuals(currentIndiv);
		for (OWLIndividual indiv : subIndivs)
			addSubIndividualsToTree(currentTree, indiv, currentIndiv);
		
		return currentTree;
	}
	
	/* Finds the individual of a provided type which has the highest "number" property
	 * Param:	OWLIndividual typeIndividual (an OWLIndividual with the name of the type, i.e. "#Factor")
	 * Return:	OWLIndividual the specific individual that is of the type provided and has the highest "number" property value		
	 */
	public OWLIndividual getLastIndividualOfType(OWLIndividual typeIndividual) {
		String typeName = getShortForm(typeIndividual.toString());
    	OWLClass parentTypeClass = factoryGlobal.getOWLClass("#" + typeName, pm);
		Set<OWLIndividual> sameClass = parentTypeClass.getIndividuals(currentOntology);
		
		OWLIndividual maxIndiv = null;
		int maxNum = 0;
		for (OWLIndividual indivOfClass : sameClass) {
			int num = getNumberPropertyValue(indivOfClass);
			if (num > maxNum) {
				maxIndiv = indivOfClass;
				maxNum = num;
			}
		}
		
		return maxIndiv;
		
    }
	
	/* Retrieves and returns a OWLDataProperty with the given name from the OntologyBuilder's 'dataPropertiesMap' object. 
	 * Param:	String representing the short form name of the property to retrieve.
	 * Return:	OWLDataProperty with the given short form name, as defined in the base ontology. 
	 * 			null if no OWLDataProperty with this name was defined in the base ontology.
	 */
	OWLDataProperty getDataProperty(String name) {
		if (dataPropertiesMap == null)
			dataPropertiesMap = new HashMap<String, OWLDataProperty>();
		OWLDataProperty property = dataPropertiesMap.get(name.toLowerCase());
		if (property == null) {
			/* could add data property to map, and then to the ontology, if it wasn't already there:
			 * property = factoryGlobal.getOWLDataProperty("#" + name, pm);
			 * dataPropertiesMap.put(name.toLowerCase(), property);
			 * + then add declaration to ontology.
			 */
			return null;
		}
		return property;
	}

	/* Retrieves and returns a OWLObjectProperty with the given name from the OntologyBuilder's 'objectPropertiesMap' object. 
	 * Param:	String representing the short form name of the property to retrieve.
	 * Return:	OWLObjectProperty with the given short form name, as defined in the base ontology. 
	 * 			null if no OWLObjectProperty with this name was defined in the base ontology.
	 */
	OWLObjectProperty getObjectProperty(String name) {
		if (objectPropertiesMap == null)
			objectPropertiesMap = new HashMap<String, OWLObjectProperty>();
		OWLObjectProperty property = objectPropertiesMap.get(name.toLowerCase());
		if (property == null) {
			/* Could add object property to map, and then to the ontology, if it wasn't already there:
			 * property = factoryGlobal.getOWLObjectProperty("#" + name, pm);
			 * objectPropertiesMap.put(name.toLowerCase(), property);
			 * + add to ontology
			 */
			return null;
		}
		return property;
	}
	
	/* Retrieves all OWLDataProperty and OWLObjectProperty items from the currentOntology, 
	 * 		and stores them in global HashMap variables (dataPropertiesMap and objectPropertiesMap, respectively) in the OntologyBuilder class.
	 * Instantiates the dataPropertiesMap and objectPropertiesMap.
	 * For use within the OntologyBuilder constructor.
	 * Note:
	 * 	HashMap<String, OWLDataProperty> dataPropertiesMap
	 * 		key: String, lowercase short form of property name
	 * 		value: OWLDataProperty, as defined in the base ontology (and thus in the current ontology)
	 * 	HashMap<String, OWLObjectProperty> objectPropertiesMap
	 * 		key: String, lowercase short form of property name
	 * 		value: OWLObjectProperty, as defined in the base ontology (and thus in the current ontology)
	 */
	void retrieveAndStorePropertiesFromOntology() {
		Set<OWLDataProperty> allDataProps = currentOntology.getDataPropertiesInSignature();
		dataPropertiesMap = new HashMap<String, OWLDataProperty>();
		for (OWLDataProperty prop : allDataProps) {
			String name = getShortForm(prop);
			dataPropertiesMap.put(name.toLowerCase(), factoryGlobal.getOWLDataProperty("#" + name, pm));
		}
		
		Set<OWLObjectProperty> allObjProps = currentOntology.getObjectPropertiesInSignature();
		objectPropertiesMap = new HashMap<String, OWLObjectProperty>();
		for (OWLObjectProperty prop : allObjProps) {
			String name = getShortForm(prop);
			objectPropertiesMap.put(name.toLowerCase(), factoryGlobal.getOWLObjectProperty("#" + name, pm));	
		}
	}
	

	/* Finds the parent type of a given type, as defined by a "belongsTo" property in the base ontology.
	 * The targetTypeIndiv "belongsTo" the parentTypeIndiv in the base ontology. 
	 * Finds all the OWLIndividuals that the targetType is asserted to "belongTo". Logically, there should only be one.
	 * Param:	OWLIndividual with the name of the desired type. 
	 * Return:	OWLIndividual with the name of the parent type.
	 * 			null if there is no parent type asserted by a "belongsTo" property for this type,
	 * 				or if there is more than one parent type asserted (which should not occur)
	 */
	private OWLIndividual getParentType(OWLIndividual targetTypeIndiv) {
		Set<OWLIndividual> superIndivs = targetTypeIndiv.getObjectPropertyValues(getObjectProperty("belongsTo"), baseOntology);
		if (superIndivs.size() == 0) {
			return null;
		} else if (superIndivs.size() > 1) {
			return null;
		} else {
			OWLIndividual parent = superIndivs.iterator().next();
			return parent;
		}
	}
	
	/* Returns a Set containing OWLIndividual objects in the current ontology that are associated 
	 * 		with the provided OWLIndividual by a 'hasA' OWLDataProperty relationship.
	 * 
	 */
	Set<OWLIndividual> findSubIndividuals(OWLIndividual targetIndiv) {
		Set<OWLIndividual> subIndivs = targetIndiv.getObjectPropertyValues(getObjectProperty("hasA"), currentOntology);
		if (subIndivs.size() == 0) {
			return new HashSet<OWLIndividual>();
		}
		return subIndivs;
	}
	
	/* Returns the designated weight property value for a specified type.
	 * This is used when adding new individuals to the ontology, and then when sorting the ontology into the correct structure.
	 * Return:	int weight value
	 * 			0 if there is no weight associated with this type in the base ontology
	 * 			-1 if there is more than one weight property associated with this type in the base ontology (logical error).
	 */
	private int getWeightPropertyValue(OWLIndividual typeIndiv) {
    	Set<OWLLiteral> literalSet = typeIndiv.getDataPropertyValues(getDataProperty("weight"), baseOntology);
    	if (literalSet.size() == 0)
			return 0;
    	else if (literalSet.size() > 1) 
    		return -1;
		int wt = literalSet.iterator().next().parseInteger();
		return wt;
	}
	
	/* Returns the number property value asserted for a specified individual.
	 * Return:	int number value
	 * 			0 if there is no number associated with this individual in the ontology
	 * 			-1 if there is more than one weight property associated with this individual in the ontology (logical error).
	 */
    public int getNumberPropertyValue(OWLIndividual targetIndiv) {
    	Set<OWLLiteral> literalSet = targetIndiv.getDataPropertyValues(getDataProperty("number"), currentOntology);
    	if (literalSet.size() == 0)
			return 0;
		else if (literalSet.size() > 1) 
    		return -1;
		int num = literalSet.iterator().next().parseInteger();
		return num;
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
	
	/* Returns the currentOntology as built so far by the current OntologyBuilder instance*/
	public OWLOntology getCurrentOntology() {
		return currentOntology;
	}
	
	/* Returns the OntologyTree structure as built so far by the current OntologyBuilder instance*/
	public OntologyTree getOntologyTree() {
		return tree;
	}
	

	
}
	