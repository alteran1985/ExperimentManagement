package backend;
import java.io.File;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;


public class OntologyManager {

	OWLOntology ont;
	IRI globalIRI;
	int numComponentInfo = 5;
	//return array: [hasCorrespondingControl, hasCorrespondingDataSource, hasCorrespondingValueType, DataSource literal value, DataSource literal type]
	
	public OntologyManager() { }
	
	boolean setOntology() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		String fileLocation = "C:/Users/azt0018/workspace/ExperimentManager/ExperimentOntology.owl"; //String location of the ontology (path, filename, extension)
		
		File file = new File(fileLocation);
		OWLOntology localOnt = null;
		try {
			localOnt = manager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			return false;
		}
		OWLOntologyID id = localOnt.getOntologyID();
		globalIRI = id.getOntologyIRI();
		ont = localOnt;
		return true;
	}

	String[] obtainComponentInfo(String phase) {
		setOntology();

		String phaseName = "#" + phase;
		String[] componentInfo = new String[numComponentInfo];
		
		OWLOntology localOnt = ont;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
		//ex: base = "http://www.semanticweb.org/azt0018/ontologies/2014/9/ExperimentOntology";
		String base = globalIRI.toString();
		PrefixManager pm = new DefaultPrefixManager(base);
		
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		OWLNamedIndividual targetIndiv = dataFactory.getOWLNamedIndividual(phaseName, pm);
		
		Map<OWLObjectPropertyExpression, Set<OWLIndividual> > objPropertiesMap = targetIndiv.getObjectPropertyValues(localOnt);	
		Set<OWLObjectPropertyExpression> objectPropertySet = objPropertiesMap.keySet();
		
		for (OWLObjectPropertyExpression propertyExp : objectPropertySet) {
			int designatedIndex = getDesignatedIndex(propertyExp.toString());
			String propertyInfo = getShortForm(objPropertiesMap.get(propertyExp));
			componentInfo[designatedIndex] = propertyInfo;
			
			//Stores the DataSource literal info in the componentInfo array
			//If the current individual's DataSource type does not have any literal values, stores null values in the designated componentInfo array.
			if (designatedIndex == 1) {
				String[] literalValues = findDataSourceLiteral(propertyInfo);
				if (literalValues != null) {
					int indexOfDataSourceLiteralType = getDesignatedIndex("dataSourceLiteralType");
					int indexOfDataSourceLiteralValue = getDesignatedIndex("dataSourceLiteralValue");
					componentInfo[indexOfDataSourceLiteralType] = literalValues[0];
					componentInfo[indexOfDataSourceLiteralValue] = literalValues[1];
				}
			}
		}
		
		return componentInfo;
	}
	
	/* param: String targetName; name of a DataSource individual of which you are trying to find the literal values.
	 * Returns information from the OWLLiteral of a DataSource subclass that has OWLLiteral values
	 * Ex: findDataSourceLiteral("ListOfVariableTypes")
	 * returns String["xsd:string", "Dependent
			Independent
			Control
			Nuisance"
	 * If the target individual does not have any literal values, will return an array with null values.
	 */
	String[] findDataSourceLiteral(String targetName) {
		String base = globalIRI.toString();
		IRI dataSourceIRI = IRI.create(base, "#DataSource");
		OWLClassImpl dataSourceClass = new OWLClassImpl(dataSourceIRI);
		Set<OWLClassExpression> subClassOfDataSourceSet = dataSourceClass.getSubClasses(ont);
		
		OWLIndividual targetIndiv = null; 
		OWLLiteral listLiteral = null;	
		//identify the target individual (individual that matches the name passed to this method):
		for (OWLClassExpression subclass : subClassOfDataSourceSet) {
			Set<OWLIndividual> indivSet = subclass.asOWLClass().getIndividuals(ont);
			for (OWLIndividual currentIndiv : indivSet) {
				if (targetName.equals(getShortForm(currentIndiv.toString()))) {
					targetIndiv = currentIndiv;
				}
			}
		}

		Map<OWLDataPropertyExpression, Set<OWLLiteral> > dataPropertyMap = targetIndiv.getDataPropertyValues(ont);
		Set<OWLDataPropertyExpression> dataPropertySet = dataPropertyMap.keySet();
		if (!dataPropertySet.isEmpty()) {
			OWLDataPropertyExpression dataPropExprToGet = dataPropertySet.iterator().next();
			
			Set<OWLLiteral> literalSet = targetIndiv.getDataPropertyValues(dataPropExprToGet, ont);
			if (literalSet.iterator().hasNext()) {
				listLiteral = literalSet.iterator().next();
			}
		}
		
		String[] literalInfo = null;
		if (listLiteral != null) {
			literalInfo = new String[2];
			literalInfo[0] = listLiteral.getDatatype().toString();
			literalInfo[1] = listLiteral.getLiteral();
		}
		return literalInfo;
	}
	
	/*
	 * Returns The intended index for this property
	 * This property will be found at that index in the componentInfo Array that is ultimately returned to the Mediator.
	 * These designated index values are defined in this method.
	 * //return array: [hasCorrespondingControl, hasCorrespondingDataSource, hasCorrespondingValueType, DataSource literal type, DataSource literal value]
	 */
	static int getDesignatedIndex(String expression) {
		String exp = expression.toString();
				
		if (exp.contains("hasCorrespondingControl")) {
			return 0;
		} else if (exp.contains("hasCorrespondingDataSource")) {
			return 1;
		} else if (exp.contains("hasCorrespondingValueType")) {
			return 2;
		} else if (exp.contains("dataSourceLiteralType")) {
			return 3;
		} else if (exp.contains("dataSourceLiteralValue")) {
			return 4;
		} 
		else {
			return -1;
		}	
	}


	/* Param: Set<OWLIndividual> that represents a set of properties of an individual.
	 * Returns string representing the short form name of the property (The string between '#' and '>' in the complete name of an OWLIndividual. 
	 * Assumes that there is only one value in the set.
	 * TODO: What if there are than one property? Does that happen... -- check VariableType
	 * Ex: getShortForm([<http://www.semanticweb.org/azt0018/ontologies/2014/9/ExperimentOntology#ComboBox>])
	 * 		returns "ComboBox"
	 */
	static String getShortForm(Set<OWLIndividual> propertySet) {
		String name = (propertySet.toArray())[0].toString();
		String substr = name.substring(name.indexOf("#") + 1, name.indexOf(">"));
		return substr;
	}
	

	/* Param: String that represents the complete name of an OWLIndividual.
	 * Returns string representing the short form name of the property (The string between '#' and '>' in the complete name of an OWLIndividual. 
	 * Ex: getShortForm("http://www.semanticweb.org/azt0018/ontologies/2014/9/ExperimentOntology#FeatureModel")
	 * 		returns "FeatureModel"
	 */
	static String getShortForm(String fullName) {
		String str = fullName.substring(fullName.indexOf("#") + 1, fullName.indexOf(">"));
		return str;
	}
	
}
