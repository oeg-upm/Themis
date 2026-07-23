package oeg.albafernandez.tests.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OntologyTest {

    private Ontology ontology;

    @BeforeEach
    public void setUp() {
        ontology = new Ontology();
    }

    @Test
    @DisplayName("loadOntologyFromFile should load valid Turtle ontology code and populate signature maps")
    public void testLoadOntologyFromFile() {
        String ttlContent = "@prefix : <http://example.org/test#> .\n" +
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
                "<http://example.org/test> rdf:type owl:Ontology .\n" +
                ":Person rdf:type owl:Class .\n" +
                ":Customer rdf:type owl:Class ; rdfs:subClassOf :Person .\n" +
                ":knows rdf:type owl:ObjectProperty .\n" +
                ":hasAge rdf:type owl:DatatypeProperty .\n" +
                ":John rdf:type :Person .\n";

        String result = ontology.loadOntologyFromFile(ttlContent);
        assertNotNull(result, "loadOntologyFromFile should return non-null result indicator");
        assertNotNull(ontology.getOntology(), "OWLOntology object should be loaded");

        Map<String, IRI> classes = ontology.getClasses();
        assertNotNull(classes, "Classes map should not be null");
        assertTrue(classes.containsKey("Person"), "Classes should include Person");
        assertTrue(classes.containsKey("Customer"), "Classes should include Customer");

        Map<String, IRI> objectProperties = ontology.getObjectProperties();
        assertNotNull(objectProperties, "Object properties map should not be null");
        assertTrue(objectProperties.containsKey("knows"), "Object properties should include knows");

        Map<String, IRI> datatypeProperties = ontology.getDatatypeProperties();
        assertNotNull(datatypeProperties, "Datatype properties map should not be null");
        assertTrue(datatypeProperties.containsKey("hasAge"), "Datatype properties should include hasAge");

        Map<String, IRI> individuals = ontology.getIndividuals();
        assertNotNull(individuals, "Individuals map should not be null");
        assertTrue(individuals.containsKey("John"), "Individuals should include John");
    }
}
