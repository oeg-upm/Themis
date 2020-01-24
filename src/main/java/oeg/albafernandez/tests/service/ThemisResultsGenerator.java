package oeg.albafernandez.tests.service;

import com.sun.org.apache.xpath.internal.SourceTree;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import oeg.albafernandez.tests.model.Ontology;
import oeg.albafernandez.tests.model.TestCaseDesign;
import oeg.albafernandez.tests.model.TestCaseImpl;
import oeg.albafernandez.tests.model.TestCaseResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.IOException;
import java.util.*;

public class ThemisResultsGenerator {

    public  String  getResults(  String table, List<String>  tests,  List<String> ontologies) throws JSONException, OWLOntologyStorageException, IOException, OWLOntologyCreationException, QueryParserException {

        if(table!= null && tests!=null && ontologies != null) {
            JSONArray results = new JSONArray();
            //execute each test in each ontology
            for (String test : tests) {
                //preprocess table of got
                test = test.trim().replaceAll(" +", " ").replace("\\n", "").replace("\"","");
                ThemisImplementationService impl = new ThemisImplementationService();
                // process test design to store it as a TestCaseDesign
                impl.processTestCaseDesign(test);
                // generate the implementation of the TestCaseDesign
                TestCaseImpl testsuiteImpl = impl.createTestImplementation();
                if (testsuiteImpl.getPrecondition().size() != 0 && test != "") {
                    JSONObject resultsAggregated = new JSONObject();
                    resultsAggregated.put("Test", test);
                    JSONArray resultsAsJson = new JSONArray();
                    for (String ontologyURI : ontologies) {
                        Ontology ontology = new Ontology();
                        ontology.load_ontologyURL(ontologyURI);
                        //get the right term in got to execute the test on the given ontology
                        HashMap<String, IRI> got = getTermInGot( table,  ontology);
                        /*Results of the test*/
                        ThemisExecutionService exec = new ThemisExecutionService();
                        TestCaseResult testsuiteResult = exec.executeTest(testsuiteImpl, ontology, got);
                        resultsAsJson = storeResults(testsuiteResult, ontology);
                    }
                    resultsAggregated.put("Results", resultsAsJson);
                    results.put(resultsAggregated);
                } else {
                    return results.toString();
                }
            }
            return results.toString();
        }else
            return "";
    }

    public  HashMap<String, IRI>  getTermInGot(String table, Ontology ontology) throws JSONException {
        JSONObject jsonobj = new JSONObject(table);
        JSONArray key = new JSONArray(jsonobj.getString(ontology.getKeyName()));
        HashMap<String, IRI> got = new HashMap<>();
        //get the right term in the glossary
        for (int i = 0; i < key.length(); i++) {
            JSONObject object = key.getJSONObject(i);
            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                String term = object.getString(it.next());
                String uri = object.getString(it.next());
                got.put(term, IRI.create(uri));
            }
        }
        return got;
    }


    public  JSONArray  storeResults(TestCaseResult testsuiteResult, Ontology ontology) throws JSONException {
        JSONObject testsResults = new JSONObject();
        JSONArray ontologyarray = new JSONArray();
        if (testsuiteResult.getTestResult().equals("passed")) {
            // the ontology passed the test
            testsResults.put("Ontology", ontology.getProv().toString());
            testsResults.put("Result", "passed");
            ontologyarray.put(testsResults);
        } else if (testsuiteResult.getTestResult().equals("undefined")) { // the terms needed to executeTest the tests are not defined inthe ontology
            testsResults.put("Ontology", ontology.getProv().toString());
            testsResults.put("Result", "undefined");
            testsResults.put("Undefined", testsuiteResult.getUndefinedTerms());
            ontologyarray.put(testsResults);

        } else if (testsuiteResult.getTestResult().equals("incorrect")) { // the terms needed to executeTest the tests are not defined inthe ontology
            testsResults.put("Ontology", ontology.getProv().toString());
            testsResults.put("Result", "incorrect");
            testsResults.put("Incorrect", testsuiteResult.getIncorrectTerms());
            ontologyarray.put(testsResults);
        }else if (testsuiteResult.getTestResult().equals("absent")) { // the ontology does not pass the test
            testsResults.put("Ontology", ontology.getProv().toString());
            testsResults.put("Result", "absent");
            ontologyarray.put(testsResults);
        } else { // the ontology does not pass the test
            testsResults.put("Ontology", ontology.getProv().toString());
            testsResults.put("Result", "notpassed");
            ontologyarray.put(testsResults);
        }
        return ontologyarray;
    }

}
