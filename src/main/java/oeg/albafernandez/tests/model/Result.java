package oeg.albafernandez.tests.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class Result {
    @Schema(description = "Glossary of terms as a json that includes the fragment of the ontology URI, and the glossary using the keys Term and URI.", example = "{\"core\":[{\"Term\":\"Sensor\",\"URI\":\"http://iot.linkeddata.es/def/core#Sensor\"}]}")
    private String got;
    @Schema(description = "List of ontologies to be analysed",example = "[\"http://iot.linkeddata.es/def/core\"]")
    private List<String> ontologies;
    @Schema(description = "List of tests to be executed", example = "[\"Sensor type Class\", \"Sensor subclassOf Device\"]")
    private List<String> tests;

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
