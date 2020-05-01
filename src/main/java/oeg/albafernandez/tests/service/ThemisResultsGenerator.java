package oeg.albafernandez.tests.service;

import de.derivo.sparqldlapi.exceptions.QueryParserException;
import oeg.albafernandez.tests.model.Ontology;
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
    ThemisSyntaxChecker syntaxChecker = new ThemisSyntaxChecker();

    public  JSONArray  getResults(  String table, List<String>  tests,  List<String> ontologies, List<String> ontologiesCode) throws JSONException, OWLOntologyStorageException, IOException, OWLOntologyCreationException, QueryParserException {

        JSONArray results = new JSONArray();
        //check nulls
        if(ontologies== null)
            ontologies = new ArrayList<>();
        if(ontologiesCode == null)
            ontologiesCode= new ArrayList<>();

        //Load ontologies
        ArrayList<Ontology> listOfOntologies = new ArrayList<Ontology>();
        for (String ontologyURI : ontologies) {
            Ontology ontology = new Ontology();
            ontology.loadOntologyURL(ontologyURI);
            listOfOntologies.add(ontology);
        }
        for (String ontologyCode : ontologiesCode) {
            Ontology ontology = new Ontology();
            ontology.loadOntologyfile(ontologyCode);
            listOfOntologies.add(ontology);
        }

        //execute each test in each ontology
        for (String test : tests) {
            //preprocess table of got
            test = test.trim().replaceAll(" +", " ").replace("\\n", "").replace("\"","");
            ThemisImplementer impl = new ThemisImplementer();
            // process test design to store it as a TestCaseDesign
            impl.processTestCaseDesign(test);
            // generate the implementation of the TestCaseDesign
            TestCaseImpl testsuiteImpl = impl.createTestImplementation();
            if (!testsuiteImpl.getPrecondition().isEmpty() && test != "") {
                JSONObject resultsAggregated = new JSONObject();
                resultsAggregated.put("Test", test);
                JSONArray resultsAsJson = new JSONArray();
                for (Ontology ontology : listOfOntologies) {
                    //get the right term in got to execute the test on the given ontology
                    HashMap<String, IRI> got;
                    if(table == null  || table.isEmpty()){
                        got = (HashMap<String, IRI>) syntaxChecker.createGot(ontology);
                    }else
                        got = getTermInGot( table,  ontology);
                    /*Results of the test*/
                    ThemisExecuter exec = new ThemisExecuter();
                    TestCaseResult testsuiteResult = exec.executeTest(testsuiteImpl, ontology, got);
                    resultsAsJson = storeResults(testsuiteResult, ontology, resultsAsJson);
                }
                resultsAggregated.put("Results", resultsAsJson);
                results.put(resultsAggregated);
            } else {
                return results;
            }
        }
        return results;

    }

    public  HashMap<String, IRI>  getTermInGot(String table, Ontology ontology) throws JSONException {
        JSONObject jsonobj = new JSONObject(table);
        JSONArray key = new JSONArray(jsonobj.getString(ontology.getKeyName()));
        HashMap<String, IRI> got = new HashMap<>();
        //get the right term in the glossary
        for (int i = 0; i < key.length(); i++) {
            JSONObject object = key.getJSONObject(i);
            if(object.has("Type")){
                Iterator<String> it = object.keys();

                while (it.hasNext()) {
                    String type = object.getString(it.next());
                    String term = object.getString(it.next());
                    String uri = object.getString(it.next());

                    got.put(term, IRI.create(uri));
                }
            }else{
                Iterator<String> it = object.keys();

                while (it.hasNext()) {
                    String term = object.getString(it.next());
                    String uri = object.getString(it.next());

                    got.put(term, IRI.create(uri));
                }
            }
        }
        return got;
    }


    public  JSONArray  storeResults(TestCaseResult testsuiteResult, Ontology ontology, JSONArray ontologyarray) throws JSONException {
        JSONObject testsResults = new JSONObject();
        if (testsuiteResult.getTestResult().equals("passed")) {
            // the ontology passed the test
            testsResults.put("Ontology", ontology.getProv().toString());
            testsResults.put("Result", "Passed");
            ontologyarray.put(testsResults);
        } else if (testsuiteResult.getTestResult().equals("undefined")) { // the terms needed to executeTest the tests are not defined inthe ontology
            testsResults.put("Ontology", ontology.getProv().toString());
            testsResults.put("Result", "Undefined");
            testsResults.put("Undefined", testsuiteResult.getUndefinedTerms());
            ontologyarray.put(testsResults);

        } else if (testsuiteResult.getTestResult().equals("incorrect")) { // the terms needed to executeTest the tests are not defined inthe ontology
            testsResults.put("Ontology", ontology.getProv().toString());
            testsResults.put("Result", "Incorrect");
            testsResults.put("Incorrect", testsuiteResult.getIncorrectTerms());
            ontologyarray.put(testsResults);
        }else if (testsuiteResult.getTestResult().equals("absent")) { // the ontology does not pass the test
            testsResults.put("Ontology", ontology.getProv().toString());
            testsResults.put("Result", "Absent");
            ontologyarray.put(testsResults);
        } else { // the ontology does not pass the test
            testsResults.put("Ontology", ontology.getProv().toString());
            testsResults.put("Result", "Conflict");
            ontologyarray.put(testsResults);
        }
        return ontologyarray;
    }

}
