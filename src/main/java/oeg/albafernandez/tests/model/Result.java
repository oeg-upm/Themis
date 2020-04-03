package oeg.albafernandez.tests.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class Result {
    @Schema(description = "Glossary of terms as a JSON that includes the fragment of the ontology URI, and the glossary using the keys Term and URI.", example = "{\"core\":[{\"Term\":\"Sensor\",\"URI\":\"http://iot.linkeddata.es/def/core#Sensor\"}]}",required = false)
    private String got;
    @Schema(description = "List of ontologies URIs to be analysed",example = "[\"http://iot.linkeddata.es/def/core\"]",required = false)
    private List<String> ontologies;
    @Schema(description = "List of ontologies code to be analysed", example="[]",required = false)
    private List<String> ontologiesCode;
    @Schema(description = "Test file with the tests to be executed", example = "@prefix : <http://vicinity.iot.linkeddata.es/vicinity/testing/testsuite-datatype.ttl#> .\n" +
            "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix dcterms: <http://purl.org/dc/terms/> .\n" +
            "@prefix vtc: <http://w3id.org/def/vtc#> .\n" +
            "@base <http://vicinity.iot.linkeddata.es/vicinity/testing/testsuite-datatype.ttl#> . :Test1 rdf:type <http://w3id.org/def/vtc#TestCaseDesign> ,\n" +
            "                         owl:NamedIndividual ;\n" +
            "             vtc:belongsTo :TestsuiteDatatype;\n" +
            "             vtc:isRelatedToRequirement <http://vicinity.iot.linkeddata.es/vicinity/requirements/report-datatypes.html#datatypes-11>;\n" +
            "             <http://w3id.org/def/vtc#desiredBehaviour> \"Array subclassOf firstArraryItem only JSONSchema\"^^xsd:string .")
    private String testfile;
    @Schema(description = "HTML + RDFa document with test cases following the VTC ontology", example=" ")
    private String documentationFile;
    @Schema(description = "List of tests to be executed", example = "[\"Sensor type Class\", \"Sensor subclassOf Device\"]")
    private List<String> tests;
    @Schema(description = "Format of the results. Supported formats: JSON, HTML and JUnit ", defaultValue = "json", example="json")
    private String format;


    public String getDocumentationFile() {
        return documentationFile;
    }

    public void setDocumentationFile(String documentationFile) {
        this.documentationFile = documentationFile;
    }

    public String getTestfile() {
        return testfile;
    }

    public void setTestfile(String testFile) {
        this.testfile = testFile;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getOntologiesCode() {
        return ontologiesCode;
    }

    public void setOntologiesCode(List<String> ontologiesCode) {
        this.ontologiesCode = ontologiesCode;
    }

    public String getGot() {
        return got;
    }

    public void setGot(String got) {
        this.got = got;
    }

    public List<String> getOntologies() {
        return ontologies;
    }

    public void setOntologies(List<String> ontologies) {
        this.ontologies = ontologies;
    }

    public List<String> getTests() {
        return tests;
    }

    public void setTests(List<String> tests) {
        this.tests = tests;
    }
}
