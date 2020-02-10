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
    private OWLOntology preparationAxioms;
    private List<String> preconditionList;
    private List<String> preconditionQueryList;
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
        this.preconditionList = new ArrayList<>();
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

    public OWLOntology getPreparationAxioms() {
        return preparationAxioms;
    }

    public void setPreparationAxioms(OWLOntology preparationAxioms) {
        this.preparationAxioms = preparationAxioms;
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

    public List<String> getPreconditionList() {
        return preconditionList;
    }

    public void setPreconditionList(List<String> preconditionList) {
        this.preconditionList = preconditionList;
    }

    public List<String> getPreconditionQueryList() {
        return preconditionQueryList;
    }

    public void setPreconditionQueryList(List<String> preconditionQueryList) {
        this.preconditionQueryList = preconditionQueryList;
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
