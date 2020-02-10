package oeg.albafernandez.tests.model;

import org.semanticweb.owlapi.model.IRI;
import java.util.ArrayList;
import java.util.List;


/*
* Class to define the results of each test
* */
public class TestCaseResult {
    private IRI relatedTestImpl;
    private String testResult;
    private IRI ontologyURI;
    private List<String> undefinedTermsList;
    private List<String> incorrectTermsList;

    public TestCaseResult() {
        testResult = "";
        undefinedTermsList = new ArrayList<>();
        incorrectTermsList = new ArrayList<>();
    }

    public IRI getRelatedTestImpl() {
        return relatedTestImpl;
    }

    public void setRelatedTestImpl(IRI relatedTestImpl) {
        this.relatedTestImpl = relatedTestImpl;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public IRI getOntologyURI() {
        return ontologyURI;
    }

    public void setOntologyURI(IRI ontologyURI) {
        this.ontologyURI = ontologyURI;
    }

    public List<String> getUndefinedTermsList() {
        return undefinedTermsList;
    }

    public void setUndefinedTermsList(List<String> undefinedTermsList) {
        this.undefinedTermsList = undefinedTermsList;
    }

    public List<String> getIncorrectTermsList() {
        return incorrectTermsList;
    }

    public void setIncorrectTermsList(List<String> incorrectTermsList) {
        this.incorrectTermsList = incorrectTermsList;
    }
}
