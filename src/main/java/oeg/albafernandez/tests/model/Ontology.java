package oeg.albafernandez.tests.model;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;


public class Ontology {

    static final Logger logger = Logger.getLogger(Ontology.class);

    private OWLOntology owlOntology;
    private OWLOntologyManager manager;
    private IRI prov;
    private String key;
    private String got;

    public OWLOntology getOntology() {
        return owlOntology;
    }

    public void setOntology(OWLOntology ontology) {
        this.owlOntology = ontology;
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

    public Map<String, IRI> getClasses() {

        HashMap<String, IRI> hashMapClasses = new HashMap<>();
        try {
            Iterator<OWLClass> iter = owlOntology.getClassesInSignature(true).iterator();
            while (iter.hasNext()) {
                OWLClass nextClass = iter.next();

                if (!hashMapClasses.containsKey(nextClass.getIRI().getFragment()) &&
                        !nextClass.getIRI().getFragment().equalsIgnoreCase("Class") && !nextClass.getIRI().getFragment().equalsIgnoreCase("Property"))
                    hashMapClasses.put(nextClass.getIRI().getFragment(), nextClass.getIRI());
                else {
                    String[] uri = nextClass.getIRI().getNamespace().split("/");
                    hashMapClasses.put(uri[uri.length - 1] + ":" + nextClass.getIRI().getFragment(), nextClass.getIRI());
                }
            }
        } catch (NullPointerException e) {
            logger.error("Classes from the ontology could not be extracted");
        }
        return hashMapClasses;

    }

    public Map<String, IRI> getIndividuals() {
        HashMap<String, IRI> hashMapIndividuals = new HashMap<>();
        try {
            Iterator<OWLNamedIndividual> iter = owlOntology.getIndividualsInSignature(true).iterator();
            while (iter.hasNext()) {
                OWLNamedIndividual nextIndividual = iter.next();
                if (!nextIndividual.getIRI().toString().endsWith("/") && !nextIndividual.getIRI().toString().endsWith("#")) { //si es solo una uri
                    if (!hashMapIndividuals.containsKey(nextIndividual.getIRI().getFragment())) {

                        hashMapIndividuals.put(nextIndividual.getIRI().getFragment(), nextIndividual.getIRI());
                    } else {
                        String[] uri = nextIndividual.getIRI().getNamespace().split("/");
                        hashMapIndividuals.put(uri[uri.length - 1] + nextIndividual.getIRI().getFragment(), nextIndividual.getIRI());
                    }
                }

            }
        } catch (NullPointerException e) {
            logger.error("Individuals from the ontology could not be extracted");
        }
        return hashMapIndividuals;

    }

    public Map<String, IRI> getObjectProperties() {
        HashMap<String, IRI> hashMapProp = new HashMap<>();
        try {
            Iterator<OWLObjectProperty> iter = owlOntology.getObjectPropertiesInSignature(true).iterator();

            while (iter.hasNext()) {
                OWLObjectProperty nextProp = iter.next();
                hashMapProp.put(nextProp.getIRI().getFragment(), nextProp.getIRI());
            }
        } catch (NullPointerException e) {
            logger.error("Object properties from the ontology could not be extracted");
        }
        return hashMapProp;
    }

    public Map<String, IRI> getDatatypeProperties() {
        HashMap<String, IRI> hashMapdataProp = new HashMap<>();
        try {
            Iterator<OWLDataProperty> iter = owlOntology.getDataPropertiesInSignature(true).iterator();

            while (iter.hasNext()) {
                OWLDataProperty nextProp = iter.next();
                hashMapdataProp.put(nextProp.getIRI().getFragment(), nextProp.getIRI());
            }
        } catch (NullPointerException e) {
            logger.error("Datatype properties from the ontology could not be extracted");
        }
        return hashMapdataProp;
    }

    public String getKeyName() {
        try {
            if (prov.getFragment().contains(".")) {
                return prov.getFragment().split(Pattern.quote("."))[0];
            } else {
                return prov.getFragment();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }

    }

    public String loadOntologyFromURL(String prov) {
        String response = " ";
        prov = prov.replace("\"", "");
        this.manager = OWLManager.createOWLOntologyManager();
        if (prov.endsWith("#") || prov.endsWith("/"))
            prov = prov.substring(0, prov.length() - 1);
        IRI path = IRI.create(prov.replace("\"", ""));
        logger.info("Analysing ontology with URI: " + prov);
        try {
            OWLOntology ontology = this.manager.loadOntology(path);
            this.setOntology(ontology);
            IRI iri = ontology.getOntologyID().getOntologyIRI();
            if (iri.toString().endsWith("#") || iri.toString().endsWith("/"))
                this.setProv(IRI.create(iri.toString().substring(0, iri.toString().length() - 1)));
            else
                this.setProv(IRI.create(iri.toString()));
        } catch (Exception e) {
            logger.error("could not load vocabulary. " + e.getMessage());
            response = null;
        }
        return response;
    }


    public String loadOntologyFromfile(String filename) {
        String response = " ";
        this.manager = OWLManager.createOWLOntologyManager();

        OWLOntologyDocumentSource docs = new StringDocumentSource(filename);

        try {
            OWLOntology ontology = this.manager.loadOntologyFromOntologyDocument(docs);
            this.setOntology(ontology);
            IRI iri = ontology.getOntologyID().getOntologyIRI();
            if (iri.toString().endsWith("#") || iri.toString().endsWith("/"))
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
