package oeg.albafernandez.tests.service;

import com.hp.hpl.jena.ontology.AnnotationProperty;
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
        while(it.hasNext()) {
            owlClass = (OWLClass) it.next();
            testCaseDesign = owlClass.getIRI().getFragment()+" type Class";
            return testCaseDesign;
        }

        return testCaseDesign;
    }

    public String generateSubClassExampleFromOntology(Ontology ontology){
        String testCaseDesign = "";
        Iterator it = ontology.getOntology().getClassesInSignature().iterator();
        OWLClass owlClass=null;

        while(it.hasNext()) {
            Iterator<OWLSubClassOfAxiom>  it2 = ontology.getOntology().getSubClassAxiomsForSubClass((OWLClass) it.next()).iterator();

            while(it2.hasNext()) {
                OWLSubClassOfAxiom OWLSubClassOf = it2.next();
                testCaseDesign = OWLSubClassOf.getSubClass().asOWLClass().getIRI().getFragment() +" subClassOf " + OWLSubClassOf.getSuperClass().asOWLClass().getIRI().getFragment();
                return testCaseDesign;
            }
        }


        return testCaseDesign;
    }

    public String generateRangeExampleFromOntology(Ontology ontology){
        String testCaseDesign = "" ;
        Iterator it = ontology.getOntology().getObjectPropertiesInSignature().iterator();
        OWLObjectProperty owlProperty=null;
        OWLObjectPropertyRangeAxiom OWLRange=null;
        while(it.hasNext()) {
            OWLObjectProperty obj = (OWLObjectProperty) it.next();
            Iterator it2 = ontology.getOntology().getObjectPropertyRangeAxioms(obj).iterator();

            while(it2.hasNext()) {
                OWLRange = (OWLObjectPropertyRangeAxiom) it2.next();
                testCaseDesign =  obj.getIRI().getFragment()+" range "+ OWLRange.getRange().asOWLClass().getIRI().getFragment();
                return testCaseDesign;
            }

        }

        return testCaseDesign;
    }

    public String generateDomainExampleFromOntology(Ontology ontology){
        String testCaseDesign = "" ;
        Iterator it = ontology.getOntology().getObjectPropertiesInSignature().iterator();
        OWLObjectProperty owlProperty=null;
        OWLObjectPropertyDomainAxiom OWLdomain=null;
        while(it.hasNext()) {
            OWLObjectProperty obj = (OWLObjectProperty) it.next();
            Iterator it2 = ontology.getOntology().getObjectPropertyDomainAxioms(obj).iterator();

            while(it2.hasNext()) {
                OWLdomain = (OWLObjectPropertyDomainAxiom) it2.next();
                testCaseDesign =  obj.getIRI().getFragment()+" domain "+ OWLdomain.getDomain().asOWLClass().getIRI().getFragment();
                return testCaseDesign;
            }

        }

        return testCaseDesign;
    }
}
