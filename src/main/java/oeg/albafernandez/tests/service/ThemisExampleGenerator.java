package oeg.albafernandez.tests.service;

import oeg.albafernandez.tests.model.Ontology;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.Iterator;

public class ThemisExampleGenerator {


    public ArrayList<String> generateExampleFromOntology(Ontology ontology){
        ArrayList<String> testCaseDesigns = new ArrayList<>();

        String typeExample = generateTypeExampleFromOntology(ontology);
        if (typeExample != null && !typeExample.isEmpty()) {
            testCaseDesigns.add(typeExample);
        }
        String domainExample = generateDomainExampleFromOntology(ontology);
        if (domainExample != null && !domainExample.isEmpty()) {
            testCaseDesigns.add(domainExample);
        }
        String rangeExample = generateRangeExampleFromOntology(ontology);
        if (rangeExample != null && !rangeExample.isEmpty()) {
            testCaseDesigns.add(rangeExample);
        }
        String subClassExample = generateSubClassExampleFromOntology(ontology);
        if (subClassExample != null && !subClassExample.isEmpty()) {
            testCaseDesigns.add(subClassExample);
        }

        return testCaseDesigns;

    }

    public String generateTypeExampleFromOntology(Ontology ontology){
        Iterator<OWLClass> it = ontology.getOntology().getClassesInSignature().iterator();
        while(it.hasNext()) {
            OWLClass owlClass = it.next();
            return owlClass.getIRI().getFragment() + " type Class";
        }
        return "";
    }

    public String generateSubClassExampleFromOntology(Ontology ontology){
        Iterator<OWLClass> it = ontology.getOntology().getClassesInSignature().iterator();

        while(it.hasNext()) {
            Iterator<OWLSubClassOfAxiom> it2 = ontology.getOntology().getSubClassAxiomsForSubClass(it.next()).iterator();

            while(it2.hasNext()) {
                OWLSubClassOfAxiom owlSubClassOf = it2.next();
                return owlSubClassOf.getSubClass().asOWLClass().getIRI().getFragment() + " subClassOf " + owlSubClassOf.getSuperClass().asOWLClass().getIRI().getFragment();
            }
        }

        return "";
    }

    public String generateRangeExampleFromOntology(Ontology ontology){
        Iterator<OWLObjectProperty> it = ontology.getOntology().getObjectPropertiesInSignature().iterator();
        while(it.hasNext()) {
            OWLObjectProperty obj = it.next();
            Iterator<OWLObjectPropertyRangeAxiom> it2 = ontology.getOntology().getObjectPropertyRangeAxioms(obj).iterator();

            while(it2.hasNext()) {
                OWLObjectPropertyRangeAxiom owlRange = it2.next();
                return obj.getIRI().getFragment() + " range " + owlRange.getRange().asOWLClass().getIRI().getFragment();
            }
        }

        return "";
    }

    public String generateDomainExampleFromOntology(Ontology ontology){
        Iterator<OWLObjectProperty> it = ontology.getOntology().getObjectPropertiesInSignature().iterator();
        while(it.hasNext()) {
            OWLObjectProperty obj = it.next();
            Iterator<OWLObjectPropertyDomainAxiom> it2 = ontology.getOntology().getObjectPropertyDomainAxioms(obj).iterator();

            while(it2.hasNext()) {
                OWLObjectPropertyDomainAxiom owlDomain = it2.next();
                return obj.getIRI().getFragment() + " domain " + owlDomain.getDomain().asOWLClass().getIRI().getFragment();
            }

        }

        return "";
    }
}
