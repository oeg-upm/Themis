package oeg.albafernandez.tests.model;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/*
* Class to define the implmenetation of the tests
* */

public class TestCaseImpl {
    private LinkedHashMap<String, String> axiomExpectedResult;
    private LinkedHashMap<String, String> assertions;
    private LinkedHashMap<String, OWLOntology> assertionsAxioms;
    private LinkedHashMap<String, String> axiomExpectedResultAxioms;

    private String preparation;
    private OWLOntology preparationaxioms;
    private ArrayList<String> precondition;
    private ArrayList<String> preconditionQuery;
    private IRI relatedTestDesign;
    private IRI uri;
    private String type;

    public TestCaseImpl() {
        this.type = "";
        this.assertionsAxioms = new LinkedHashMap<>();
        this.axiomExpectedResultAxioms = new LinkedHashMap<String, String>();
        this.axiomExpectedResult = new LinkedHashMap<String, String>();
        this.axiomExpectedResult = new LinkedHashMap<String, String>();
        this.assertions = new LinkedHashMap<>();
        this.precondition = new ArrayList<>();
        this.preparation ="";
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LinkedHashMap<String, String> getAxiomExpectedResultAxioms() {
        return axiomExpectedResultAxioms;
    }

    public void setAxiomExpectedResultAxioms(LinkedHashMap<String, String> axiomExpectedResultAxioms) {
        this.axiomExpectedResultAxioms = axiomExpectedResultAxioms;
    }

    public OWLOntology getPreparationaxioms() {
        return preparationaxioms;
    }

    public void setPreparationaxioms(OWLOntology preparationaxioms) {
        this.preparationaxioms = preparationaxioms;
    }

    public LinkedHashMap<String, String> getAxiomExpectedResult() {
        return axiomExpectedResult;
    }

    public void setAxiomExpectedResult(LinkedHashMap<String, String> axiomExpectedResult) {
        this.axiomExpectedResult = axiomExpectedResult;
    }

    public LinkedHashMap<String, String> getAssertions() {
        return assertions;
    }

    public void setAssertions(LinkedHashMap<String, String> assertions) {
        this.assertions = assertions;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public ArrayList<String> getPrecondition() {
        return precondition;
    }

    public void setPrecondition(ArrayList<String> precondition) {
        this.precondition = precondition;
    }

    public ArrayList<String> getPreconditionQuery() {
        return preconditionQuery;
    }

    public void setPreconditionQuery(ArrayList<String> preconditionQuery) {
        this.preconditionQuery = preconditionQuery;
    }

    public IRI getRelatedTestDesign() {
        return relatedTestDesign;
    }

    public void setRelatedTestDesign(IRI relatedTestDesign) {
        this.relatedTestDesign = relatedTestDesign;
    }

    public IRI getUri() {
        return uri;
    }

    public void setUri(IRI uri) {
        this.uri = uri;
    }

    public LinkedHashMap<String, OWLOntology> getAssertionsAxioms() {
        return assertionsAxioms;
    }

    public void setAssertionsAxioms(LinkedHashMap<String, OWLOntology> assertionsAxioms) {
        this.assertionsAxioms = assertionsAxioms;
    }
}
