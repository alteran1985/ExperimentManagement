package owl_api;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.util.OWLObjectVisitorExAdapter;

/**
 * helper function see owlapi source
 */
@SuppressWarnings({ "javadoc" })
public class LabelExtractor extends OWLObjectVisitorExAdapter<String> implements OWLAnnotationObjectVisitorEx<String> {

	@Override
	public String visit(OWLAnnotation annotation) {
		/*
		 * If it's a label, grab it as the result. Note that if there are
		 * multiple labels, the last one will be used.
		 */
		if (annotation.getProperty().isLabel()) {
		    OWLLiteral c = (OWLLiteral) annotation.getValue();
		    return c.getLiteral();
		}
		return null;
	}

	@Override
	public String visit(OWLAnnotationAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(OWLSubAnnotationPropertyOfAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(OWLAnnotationPropertyDomainAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(OWLAnnotationPropertyRangeAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(IRI arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(OWLAnonymousIndividual arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visit(OWLLiteral arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}