package oeg.albafernandez.tests.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConverterTest {

    @Test
    @DisplayName("jsonToHtml should generate an HTML table with VTC RDFa annotations")
    public void testJsonToHtml() throws Exception {
        JSONArray jsonArray = new JSONArray();

        JSONObject testObj = new JSONObject();
        testObj.put("Test", "Customer subclassof Person");

        JSONArray resultsArray = new JSONArray();
        JSONObject resultObj = new JSONObject();
        resultObj.put("Result", "Passed");
        resultObj.put("Ontology", "http://example.org/ontology.ttl");
        resultsArray.put(resultObj);

        testObj.put("Results", resultsArray);
        jsonArray.put(testObj);

        String htmlOutput = Converter.jsonToHtml(jsonArray);

        assertNotNull(htmlOutput);
        assertTrue(htmlOutput.contains("<table"), "Output should contain <table> tag");
        assertTrue(htmlOutput.contains("vocab=\"https://w3id.org/def/vtc#\""), "Output should contain VTC vocab annotation");
        assertTrue(htmlOutput.contains("Customer subclassof Person"), "Output should contain the test statement");
        assertTrue(htmlOutput.contains("http://example.org/ontology.ttl"), "Output should contain ontology URI");
        assertTrue(htmlOutput.contains("Passed"), "Output should contain test result");
    }

    @Test
    @DisplayName("jsonToJUnitXML should generate well-formed JUnit XML for passed test cases")
    public void testJsonToJUnitXMLPassed() throws Exception {
        JSONArray jsonArray = new JSONArray();

        JSONObject testObj = new JSONObject();
        testObj.put("Test", "Customer subclassof Person");

        JSONArray resultsArray = new JSONArray();
        JSONObject resultObj = new JSONObject();
        resultObj.put("Result", "Passed");
        resultObj.put("Ontology", "http://example.org/ontology.ttl");
        resultsArray.put(resultObj);

        testObj.put("Results", resultsArray);
        jsonArray.put(testObj);

        String xmlOutput = Converter.jsonToJUnitXML(jsonArray);

        assertNotNull(xmlOutput);
        assertTrue(xmlOutput.contains("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"), "Output should contain XML declaration");
        assertTrue(xmlOutput.contains("<testsuites"), "Output should contain <testsuites> tag");
        assertTrue(xmlOutput.contains("<testcase id=\"testCase1\" name=\"Customer subclassof Person\">"), "Output should contain <testcase>");
        assertTrue(xmlOutput.contains("failures=\"0\""), "Failures count should be 0");
    }

    @Test
    @DisplayName("jsonToJUnitXML should generate failure tags for conflict and absent results")
    public void testJsonToJUnitXMLFailures() throws Exception {
        JSONArray jsonArray = new JSONArray();

        JSONObject testObj = new JSONObject();
        testObj.put("Test", "Customer subclassof Agent");

        JSONArray resultsArray = new JSONArray();
        JSONObject resultObj = new JSONObject();
        resultObj.put("Result", "conflict");
        resultObj.put("Ontology", "http://example.org/ontology.ttl");
        resultsArray.put(resultObj);

        testObj.put("Results", resultsArray);
        jsonArray.put(testObj);

        String xmlOutput = Converter.jsonToJUnitXML(jsonArray);

        assertNotNull(xmlOutput);
        assertTrue(xmlOutput.contains("<failure message="), "Output should contain <failure> tag for conflict");
        assertTrue(xmlOutput.contains("failures=\"1\""), "Failures count should be 1");
    }
}
