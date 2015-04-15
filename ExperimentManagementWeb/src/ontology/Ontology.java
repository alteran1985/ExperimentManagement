package ontology;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import owl_api.LabelExtractor;

/**
 * @author Robert
 *
 * @class Ontology
 * 
 * this is the wrapper for an Ontology
 */
public class Ontology {
	
	private static final File file = new File("/home/kullen/workspace/MDA/ExperiementManagementWeb/simulation_experiment.owl");
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLClass owlclass;
	private OWLDataFactory df = OWLManager.getOWLDataFactory();
	private OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	
	private String errorMessages = "";
	private String ontologyMessages = "";
	private String ontologyClasses = "";
	private String ontologyProperties = "";
	
	/**
	 * @return OntologyManager
	 * 
	 * create a manager for Ontology
	 */
	public OWLOntologyManager create() {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		m.addIRIMapper(new AutoIRIMapper(new File("src/materializedOntologies"),true));
		return m;
	}
	
	/**
	 * self-explanatory this loads the ontology file
	 * @throws OWLOntologyCreationException
	 */
	public void loadOntology() throws OWLOntologyCreationException {
		manager = create();
		ontology = manager.loadOntologyFromOntologyDocument(file);
		assert(ontology != null);
	}
	
	/**
	 * @throws OWLOntologyStorageException
	 */
	public void saveOntology() throws OWLOntologyStorageException {
		manager = OWLManager.createOWLOntologyManager();
		manager.saveOntology(ontology);
	}
	
	/**
	 * this method shows all the relevant information from an ontology,
	 * not needed but is guarantee/check to see that information was properly loaded and api works
	 * @throws Exception
	 */
	public void showHierarchy() throws Exception {
		owlclass = df.getOWLThing();
		Hierarchy h = new Hierarchy();
		h.show(ontology, owlclass);
		h.showClasses(ontology);
		//OWLReasoner r = reasonerFactory.createNonBufferingReasoner(ontology);
		//h.showProperties(ontology,r,owlclass);
	}
	
	/**
	 * @return any error messages
	 */
	public String getErrorMessages() {
		return errorMessages;
	}

	/**
	 * @return any messages
	 */
	public String getOntologyMessages() {
		return ontologyMessages;
	}
	
	/**
	 * @return 'classes'from ontology model
	 */
	public String getOntologyClasses() {
		return ontologyClasses;
	}

	/**
	 * @return properties from model
	 */
	public String getOntologyProperties() {
		return ontologyProperties;
	}

	/**
	 * @author Robert
	 * 
	 * helper hierarchy class for ontology
	 * used to help display ontology
	 */
	private class Hierarchy {
		
			LabelExtractor le = new LabelExtractor();
		
			/**
			 * adds error messages for display
			 * 
			 * @param o
			 * @param oclass
			 * @throws OWLException
			 */
			public void show(OWLOntology o, OWLClass oclass) throws OWLException {
			assert(o!=null);
			OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(o);
			show(reasoner, oclass, 0, new HashSet<OWLClass>());
			for(OWLClass cl : o.getClassesInSignature()) {
				if(!reasoner.isSatisfiable(cl)) {
					errorMessages += "XXX: " + labelFor(cl, o) + "\n\r";
				}
			}
			reasoner.dispose();
		}

			/**
			 * adds to ontology messages for display
			 * 
			 * @param reasoner
			 * @param oclass
			 * @param level
			 * @param visited
			 * @throws OWLException
			 */
			public void show(OWLReasoner reasoner, OWLClass oclass, int level, Set<OWLClass> visited) throws OWLException {
		        // Only print satisfiable classes to skip Nothing
		        if (!visited.contains(oclass) && reasoner.isSatisfiable(oclass)) {
		            visited.add(oclass);
		            for (int i = 0; i < level * 4; i++) {
		            	ontologyMessages += " ";
		            }
		            ontologyMessages += labelFor(oclass, reasoner.getRootOntology());
		            ontologyMessages += reasoner.getRootOntology().toString();
		            ontologyMessages += "\n\r";
		            /* Find the children and recurse */
		            NodeSet<OWLClass> subClasses = reasoner.getSubClasses(oclass, true);
		            for (OWLClass child : subClasses.getFlattened()) {
		                show(reasoner, child, level + 1, visited);
		            }
		        }
		    }
		
			/**
			 * @param oclass
			 * @param o
			 * @return
			 */
			public String labelFor(OWLClass oclass, OWLOntology o) {
				Set<OWLAnnotation> annotations = oclass.getAnnotations(o);
		        for (OWLAnnotation anno : annotations) {
		            String result = anno.accept(le);
		            if (result != null) {
		                return result;
		            }
		        }
		        return oclass.getIRI().toString();
		    }
		 
			/**
			 * adds all classes to a message for display
			 * 
			 * @param o
			 * @throws Exception
			 */
			public void showClasses(OWLOntology o) throws Exception {
			 for(OWLClass cls : o.getClassesInSignature()){
				ontologyClasses += cls.toString() + "\n\r";
			 }
		 }
	
		 /**
		     * Prints out the properties that instances of a class expression must have
		     * 
		     * @param o
		     *        The ontology
		     * @param reasoner
		     *        The reasoner
		     * @param cls
		     *        The class expression
		     */
		    private void printProperties(OWLOntology o, OWLReasoner reasoner,
		            OWLClass cls) {
		        if (!o.containsClassInSignature(cls.getIRI())) {
		            throw new RuntimeException("Class not in signature of the ontology");
		        }
		        ontologyProperties += "Properties of " + cls + "\n\r";
		        for (OWLObjectPropertyExpression prop : o.getObjectPropertiesInSignature()) {
		            // To test whether an instance of A MUST have a property p with a
		            // filler,
		            // check for the satisfiability of A and not (some p Thing)
		            // if this is satisfiable, then there might be instances with no
		            // p-filler
		            OWLClassExpression restriction = df.getOWLObjectSomeValuesFrom(
		                    prop, df.getOWLThing());
		            OWLClassExpression intersection = df.getOWLObjectIntersectionOf(
		                    cls, df.getOWLObjectComplementOf(restriction));
		            boolean sat = !reasoner.isSatisfiable(intersection);
		            if (sat) {
		                ontologyProperties += "Instances of " + cls + " necessarily have the property " + prop + "\n\r";
		            }
		        }
		    }
	
		    /**
		     * see printProperties for additional info
		     * 
		     * @param o
		     * @param reasoner
		     * @param cls
		     */
		    public void showProperties(OWLOntology o, OWLReasoner reasoner, OWLClass cls) {
		    	printProperties(o,reasoner,cls);
		    }
	}
}
