package oeg.albafernandez.tests.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class AutocompleteResource {
    @Schema(description = "Test", example = "Sensor type")
    private String test;
    @Schema(description = "Last term", example = "type")
    private String lastTerm;
    @Schema(description = "Code of the ontology to be analysed", required = false)
    private String code;
    @Schema(description = "URI of the ontology to be analysed", required = false)
    private String ontologyUri;
    private String imports;

    public String getImports() {
        return imports;
    }

    public void setImports(String imports) {
        this.imports = imports;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getLastTerm() {
        return lastTerm;
    }

    public void setLastTerm(String lastTerm) {
        this.lastTerm = lastTerm;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOntologyUri() {
        return ontologyUri;
    }

    public void setOntologyUri(String ontologyUri) {
        this.ontologyUri = ontologyUri;
    }
}
