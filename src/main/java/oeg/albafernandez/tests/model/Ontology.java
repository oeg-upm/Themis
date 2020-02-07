package oeg.albafernandez.tests.model;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;


public class Ontology {

    final static Logger logger = Logger.getLogger(Ontology.class);

    private OWLOntology OWLontology;
    private OWLOntologyManager manager;
    private IRI prov;
    private String key;
    private String got;

    public OWLOntology getOntology() {
        return OWLontology;
    }

    public void setOntology(OWLOntology ontology) {
        this.OWLontology = ontology;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    public String getGot() {
        return got;
    }

    public void setGot(String got) {
        this.got = got;
    }

    public IRI getProv() {
        return prov;
    }

    public void setProv(IRI prov) {
        this.prov = prov;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public HashMap<String, IRI> getClasses(){
        HashMap<String, IRI> hashMapClasses = new HashMap<String, IRI>();
        Iterator<OWLClass> iter = OWLontology.getClassesInSignature(true).iterator();
        while(iter.hasNext()){
            OWLClass nextClass=iter.next();

            if(!hashMapClasses.containsKey(nextClass.getIRI().getFragment().toString()))
                hashMapClasses.put(nextClass.getIRI().getFragment().toString(),nextClass.getIRI());
            else{
                String[] uri = nextClass.getIRI().getNamespace().toString().split("/");
                hashMapClasses.put(uri[uri.length-1]+nextClass.getIRI().getFragment().toString(),nextClass.getIRI());
            }
        }
        return  hashMapClasses;

    }

    public HashMap<String, IRI> getIndividuals(){
        HashMap<String, IRI> hashMapIndividuals = new HashMap<String, IRI>();
        Iterator<OWLNamedIndividual> iter = OWLontology.getIndividualsInSignature(true).iterator();
        while(iter.hasNext()){
            OWLNamedIndividual nextIndividual=iter.next();
            if(!nextIndividual.getIRI().toString().endsWith("/") && !nextIndividual.getIRI().toString().endsWith("#")) { //si es solo una uri
                 if(!hashMapIndividuals.containsKey(nextIndividual.getIRI().getFragment().toString())) {

                    hashMapIndividuals.put(nextIndividual.getIRI().getFragment().toString(), nextIndividual.getIRI());
                }
                 else{
                     String[] uri = nextIndividual.getIRI().getNamespace().toString().split("/");
                     hashMapIndividuals.put(uri[uri.length-1]+nextIndividual.getIRI().getFragment().toString(),nextIndividual.getIRI());
                 }
            }

        }
        return  hashMapIndividuals;

    }

    public HashMap<String, IRI> getObjectProperties(){
        HashMap<String, IRI> hashMapProp = new HashMap<String, IRI>();
        Iterator<OWLObjectProperty> iter = OWLontology.getObjectPropertiesInSignature(true).iterator();

        while(iter.hasNext()){
            OWLObjectProperty nextProp=iter.next();
            hashMapProp.put(nextProp.getIRI().getFragment().toString(),nextProp.getIRI());
        }
        return  hashMapProp;
    }

    public HashMap<String, IRI> getDatatypeProperties(){
        HashMap<String, IRI> hashMapdataProp = new HashMap<String, IRI>();
        Iterator<OWLDataProperty> iter = OWLontology.getDataPropertiesInSignature(true).iterator();

        while(iter.hasNext()){
            OWLDataProperty nextProp=iter.next();
            hashMapdataProp.put(nextProp.getIRI().getFragment().toString(),nextProp.getIRI());
        }
        return  hashMapdataProp;
    }

    public String getKeyName(){
        if(prov.getFragment().toString().contains(".")) {
            return prov.getFragment().toString().split(Pattern.quote("."))[0];
        }else {
            return prov.getFragment().toString();
        }

    }

    public String load_ontologyURL(String prov) {
        String response = " ";
        prov = prov.replace("\"","");
        this.manager = OWLManager.createOWLOntologyManager();
        if(prov.endsWith("#") || prov.endsWith("/") )
            prov = prov.substring(0, prov.length() - 1);
        IRI path = IRI.create(prov.replace("\"",""));
        logger.info("Analysing ontology with URI: " + prov);
        try {
            OWLOntology ontology = this.manager.loadOntology(path);
            this.setOntology(ontology);
            IRI iri = ontology.getOntologyID().getOntologyIRI();
            if(iri.toString().endsWith("#") || iri.toString().endsWith("/") )
                this.setProv(IRI.create(iri.toString().substring(0, iri.toString().length() - 1)));
            else
                this.setProv(IRI.create(iri.toString()));
        } catch (Exception e) {
            logger.error("could not load vocabulary. " + e.getMessage());
            response = null;
        }
        return response;
    }

}
