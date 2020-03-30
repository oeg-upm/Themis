package oeg.albafernandez.tests.model;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/*
 * Class to define the implmenetation of the tests
 * */

public class TestCaseImpl {
    private Map<String, String> axiomExpectedResult;
    private Map<String, String> assertions;
    private Map<String, OWLOntology> assertionsAxioms;
    private Map<String, String> axiomExpectedResultAxioms;
    private String preparation;
    private OWLOntology preparationaxioms;
    private List<String> precondition;
    private List<String> preconditionQuery;
    private IRI relatedTestDesign;
    private IRI uri;
    private String type;

    public TestCaseImpl() {
        this.type = "";
        this.assertionsAxioms = new LinkedHashMap<>();
        this.axiomExpectedResultAxioms = new LinkedHashMap<>();
        this.axiomExpectedResult = new LinkedHashMap<>();
        this.axiomExpectedResult = new LinkedHashMap<>();
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

    public Map<String, String> getAxiomExpectedResultAxioms() {
        return axiomExpectedResultAxioms;
    }

    public void setAxiomExpectedResultAxioms(Map<String, String> axiomExpectedResultAxioms) {
        this.axiomExpectedResultAxioms = axiomExpectedResultAxioms;
    }

    public OWLOntology getPreparationaxioms() {
        return preparationaxioms;
    }

    public void setPreparationaxioms(OWLOntology preparationaxioms) {
        this.preparationaxioms = preparationaxioms;
    }

    public Map<String, String> getAxiomExpectedResult() {
        return axiomExpectedResult;
    }

    public void setAxiomExpectedResult(Map<String, String> axiomExpectedResult) {
        this.axiomExpectedResult = axiomExpectedResult;
    }

    public Map<String, String> getAssertions() {
        return assertions;
    }

    public void setAssertions(Map<String, String> assertions) {
        this.assertions = assertions;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public List<String> getPrecondition() {
        return precondition;
    }

    public void setPrecondition(List<String> precondition) {
        this.precondition = precondition;
    }

    public List<String> getPreconditionQuery() {
        return preconditionQuery;
    }

    public void setPreconditionQuery(List<String> preconditionQuery) {
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

    public Map<String, OWLOntology> getAssertionsAxioms() {
        return assertionsAxioms;
    }

    public void setAssertionsAxioms(Map<String, OWLOntology> assertionsAxioms) {
        this.assertionsAxioms = assertionsAxioms;
    }
}
