package oeg.albafernandez.tests.service;

import oeg.albafernandez.tests.model.Ontology;
import oeg.albafernandez.tests.model.TestCaseDesign;
import org.json.Test;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.Iterator;

public class ThemisExampleGenerator {


    public ArrayList<String> generateExampleFromOntology(Ontology ontology){
        ArrayList<String> testCaseDesigns = new ArrayList<>();

        if(!generateTypeExampleFromOntology(ontology).isEmpty())
            testCaseDesigns.add(generateTypeExampleFromOntology(ontology));
        if(!generateDomainExampleFromOntology(ontology).isEmpty())
            testCaseDesigns.add(generateDomainExampleFromOntology(ontology));
        if(!generateRangeExampleFromOntology(ontology).isEmpty())
            testCaseDesigns.add(generateRangeExampleFromOntology(ontology));
        if(!generateSubClassExampleFromOntology(ontology).isEmpty())
            testCaseDesigns.add(generateSubClassExampleFromOntology(ontology));

        return testCaseDesigns;

    }

    public String generateTypeExampleFromOntology(Ontology ontology){
        String testCaseDesign = "";
        Iterator it = ontology.getOntology().getClassesInSignature().iterator();
        OWLClass owlClass=null;
        if(it.hasNext()) {
            owlClass = (OWLClass) it.next();
            testCaseDesign = owlClass.getIRI().getFragment()+" type Class";

        }

        return testCaseDesign;
    }

    public String generateSubClassExampleFromOntology(Ontology ontology){
        String testCaseDesign = "";
        Iterator it = ontology.getOntology().getClassesInSignature().iterator();
        OWLClass owlClass=null;

        if(it.hasNext()) {
            Iterator<OWLSubClassOfAxiom>  it2 = ontology.getOntology().getSubClassAxiomsForSubClass((OWLClass) it.next()).iterator();

            if(it2.hasNext()) {
                OWLSubClassOfAxiom OWLSubClassOf = it2.next();
                testCaseDesign = OWLSubClassOf.getSubClass().asOWLClass().getIRI().getFragment() +" subClassOf " + OWLSubClassOf.getSuperClass().asOWLClass().getIRI().getFragment();
            }
        }


        return testCaseDesign;
    }

    public String generateRangeExampleFromOntology(Ontology ontology){
        String testCaseDesign = "";
        Iterator it = ontology.getOntology().getObjectPropertiesInSignature().iterator();
        OWLObjectProperty owlProperty=null;
        OWLObjectPropertyRangeAxiom OWLrange=null;

        if(it.hasNext()) {
            Iterator it2 = ontology.getOntology().getObjectPropertyRangeAxioms((OWLObjectProperty) it.next()).iterator();

            if(it2.hasNext()) {
                OWLrange = (OWLObjectPropertyRangeAxiom) it2.next();
                testCaseDesign = OWLrange.getRange().asOWLClass().getIRI().getFragment() +" and "+ owlProperty.asOWLObjectProperty().getIRI().getFragment();
            }
        }


        return testCaseDesign;
    }

    public String generateDomainExampleFromOntology(Ontology ontology){
        String testCaseDesign = "" ;
        Iterator it = ontology.getOntology().getObjectPropertiesInSignature().iterator();
        OWLObjectProperty owlProperty=null;
        OWLObjectPropertyDomainAxiom OWLdomain=null;

        if(it.hasNext()) {
            Iterator it2 = ontology.getOntology().getObjectPropertyRangeAxioms((OWLObjectProperty) it.next()).iterator();

            if(it2.hasNext()) {
                OWLdomain = (OWLObjectPropertyDomainAxiom) it2.next();
                testCaseDesign = OWLdomain.getDomain().asOWLClass().getIRI().getFragment() +" and "+ owlProperty.asOWLObjectProperty().getIRI().getFragment();
            }
        }


        return testCaseDesign;
    }
}
