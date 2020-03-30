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
    private List<String> undefinedTerms;
    private List<String> incorrectTerms;

    public TestCaseResult() {
        testResult = "";
        undefinedTerms = new ArrayList<>();
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

    public List<String> getUndefinedTerms() {
        return undefinedTerms;
    }

    public void setUndefinedTerms(List<String> undefinedTerms) {
        this.undefinedTerms = undefinedTerms;
    }

    public List<String> getIncorrectTerms() {
        return incorrectTerms;
    }

    public void setIncorrectTerms(List<String> incorrectTerms) {
        this.incorrectTerms = incorrectTerms;
    }
}
