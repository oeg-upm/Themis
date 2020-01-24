package oeg.albafernandez.tests.model;

import org.semanticweb.owlapi.model.IRI;

import java.util.ArrayList;


/*
* Class to define the results of each test
* */
public class TestCaseResult {
    private IRI relatedTestImpl;
    private String testResult;
    private IRI ontologyURI;
    private ArrayList<String> undefinedTerms;
    private ArrayList<String> incorrectTerms;

    public TestCaseResult() {
        testResult = "";
        undefinedTerms = new ArrayList<String>();
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

    public ArrayList<String> getUndefinedTerms() {
        return undefinedTerms;
    }

    public void setUndefinedTerms(ArrayList<String> undefinedTerms) {
        this.undefinedTerms = undefinedTerms;
    }

    public ArrayList<String> getIncorrectTerms() {
        return incorrectTerms;
    }

    public void setIncorrectTerms(ArrayList<String> incorrectTerms) {
        this.incorrectTerms = incorrectTerms;
    }
}
