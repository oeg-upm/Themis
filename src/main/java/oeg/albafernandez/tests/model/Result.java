package oeg.albafernandez.tests.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class Result {
    @Schema(description = "Glossary of terms as a JSON that includes the fragment of the ontology URI, and the glossary using the keys Term and URI.", example = "{\"core\":[{\"Term\":\"Sensor\",\"URI\":\"http://iot.linkeddata.es/def/core#Sensor\"}]}",required = false)
    private String got;
    @Schema(description = "List of ontologies URIs to be analysed",example = "[\"http://iot.linkeddata.es/def/core\"]",required = false)
    private List<String> ontologies;
    @Schema(description = "List of ontologies code to be analysed", example = "[]",required = false)
    private List<String> ontologiesCode;
    @Schema(description = "Test file with the tests to be executed",
            example = "@prefix : <http://vicinity.iot.linkeddata.es/vicinity/testing/testsuite-datatype.ttl#> . @prefix owl: <http://www.w3.org/2002/07/owl#> . @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . @prefix vtc: <https://w3id.org/def/vtc#> . @base <http://vicinity.iot.linkeddata.es/vicinity/testing/testsuite-datatype.ttl#> . :Test1 rdf:type <https://w3id.org/def/vtc#TestCaseDesign> , owl:NamedIndividual ; vtc:isRelatedToRequirement <http://vicinity.iot.linkeddata.es/vicinity/requirements/report-datatypes.html#datatypes-11>; https://w3id.org/def/vtc#desiredBehaviour> \"Array subclassOf firstArraryItem only JSONSchema\"^^xsd:string .")
    private String testfile;
    @Schema(description = "HTML + RDFa document with test cases following the VTC ontology", example = "<table prefix=\"rdf: http://www.w3.org/1999/02/22-rdf-syntax-ns# vtc: https://w3id.org/def/vtc# owl: http://www.w3.org/2002/07/owl# xsd: http://www.w3.org/2001/XMLSchema#\" > <thead> <tr><th class=\"col-md-2\">Identifier </th> <th class=\"col-md-1\">Category</th> <th class=\"col-md-4\">Competency Question</th> </tr></thead> <tbody> <tr resource=\"#1\" typeof=\"vtc:TestCaseDesign owl:NamedIndividual\"> <td class=\"tg-031e\"><span property=\"vtc:isRelatedToRequirement\">WoT1</span></td> <td class=\"tg-031e\"><span property=\"vtc:desiredBehaviour\">Thing type Class</span></td> <td class=\"tg-031e\">What is a thing in the web thing context?</td> </tr> </tbody></table>")
    private String documentationFile;
    @Schema(description = "List of tests to be executed", example = "[\"Sensor type Class\", \"Sensor subclassOf Device\"]")
    private List<String> tests;
    @Schema(description = "Format of the results. Supported formats: JSON, HTML and JUnit ", defaultValue = "json", example="json")
    private String format = "json";


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
