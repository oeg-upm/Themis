package oeg.albafernandez.tests.model;

import org.semanticweb.owlapi.model.IRI;


/*
* Class to define the design of each test
* */
public class TestCaseDesign {
    private IRI uri;
    private String purpose;
    private String description;
    private String source; // link between the test case and the ontology requirement
    private String subject;

    public IRI getUri() {
        return uri;
    }

    public void setUri(IRI uri) {
        this.uri = uri;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
