package oeg.albafernandez.tests.service;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ThemisGuideGeneratorTest {

    private ThemisGuideGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new ThemisGuideGenerator();
    }

    @Test
    @DisplayName("syntaxChecker should return 'true' for valid subclassof expression")
    public void testValidSubClassOfSyntax() throws JSONException {
        String test = "Customer subclassof Person";
        assertEquals("true", generator.syntaxChecker(test));
    }

    @Test
    @DisplayName("syntaxChecker should return 'true' for valid type expression")
    public void testValidTypeSyntax() throws JSONException {
        String test = "JohnAlice type Person";
        assertEquals("true", generator.syntaxChecker(test));
    }

    @Test
    @DisplayName("syntaxChecker should return 'true' for valid domain expression")
    public void testValidDomainSyntax() throws JSONException {
        String test = "knows domain Person";
        assertEquals("true", generator.syntaxChecker(test));
    }

    @Test
    @DisplayName("syntaxChecker should return 'true' for valid disjointwith expression")
    public void testValidDisjointWithSyntax() throws JSONException {
        String test = "Person disjointwith Vehicle";
        assertEquals("true", generator.syntaxChecker(test));
    }

    @Test
    @DisplayName("syntaxChecker should return 'false' for invalid expression syntax")
    public void testInvalidSyntax() throws JSONException {
        String test = "This is definitely an invalid syntax statement that fails matching 1234";
        assertEquals("false", generator.syntaxChecker(test));
    }

    @Test
    @DisplayName("getGoTFromFilename should load ontology code and return JSON report")
    public void testGetGoTFromFilename() throws Exception {
        String ttlContent = "@prefix : <http://example.org/test#> .\n" +
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "<http://example.org/test> rdf:type owl:Ontology .\n" +
                ":Person rdf:type owl:Class .\n" +
                ":Agent rdf:type owl:Class .\n";

        String resultJson = generator.getGoTFromFilename(ttlContent);
        assertNotNull(resultJson, "JSON output should not be null for valid ontology code");
        assertTrue(resultJson.contains("got"), "JSON should contain 'got' field");
    }
}
