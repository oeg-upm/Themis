package oeg.albafernandez.tests.service;

import oeg.albafernandez.tests.model.Ontology;
import oeg.albafernandez.tests.model.TestCaseImpl;
import oeg.albafernandez.tests.model.TestCaseResult;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import java.util.*;

public class ThemisResultsGenerator {
    private static final String ONTOLOGY = "Ontology";
    private static final String RESULT = "Result";

    ThemisGuideGenerator syntaxChecker = new ThemisGuideGenerator();
    static final Logger logger = Logger.getLogger(ThemisResultsGenerator.class);

    public List<Ontology> loadOntologies(List<String> ontologies, List<String> ontologiesCode){
        ArrayList<Ontology> listOfOntologies = new ArrayList<>();
        //check nulls
        if (ontologies == null)
            ontologies = new ArrayList<>();
        if (ontologiesCode == null)
            ontologiesCode = new ArrayList<>();

        //Load ontologies
        for (String ontologyURI : ontologies) {
            Ontology ontology = new Ontology();
            ontology.loadOntologyFromURL(ontologyURI);
            listOfOntologies.add(ontology);
        }
        for (String ontologyCode : ontologiesCode) {
            Ontology ontology = new Ontology();
            ontology.loadOntologyFromfile(ontologyCode);
            listOfOntologies.add(ontology);
        }
        return listOfOntologies;
    }
    public JSONArray getResults(String table, List<String> tests, List<String> ontologies, List<String> ontologiesCode) throws JSONException, OWLOntologyStorageException, OWLOntologyCreationException {

        JSONArray results = new JSONArray();

        ArrayList<Ontology> listOfOntologies  = (ArrayList<Ontology>) loadOntologies(ontologies, ontologiesCode);

        //execute each test in each ontology
        for (String test : tests) {
            //preprocess table of got
            test = test.trim().replaceAll(" +", " ").replace("\\n", "").replace("\"", "");
            ThemisImplementer impl = new ThemisImplementer();
            // process test design to store it as a TestCaseDesign
            impl.processTestCaseDesign(test);
            // generate the implementation of the TestCaseDesign
            TestCaseImpl testsuiteImpl = impl.createTestImplementation();
            if (!testsuiteImpl.getPrecondition().isEmpty() && !test.isEmpty()) {
                JSONObject resultsAggregated = new JSONObject();
                resultsAggregated.put("Test", test);
                JSONArray resultsAsJson = new JSONArray();
                for (Ontology ontology : listOfOntologies) {
                    //get the right term in got to execute the test on the given ontology
                    HashMap<String, IRI> got;
                    if (table == null || table.isEmpty()) {
                        got = (HashMap<String, IRI>) syntaxChecker.createGot(ontology);
                    } else
                        got = (HashMap<String, IRI>) getTermInGot(table, ontology);
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

    public Map<String, IRI> getTermInGot(String table, Ontology ontology) throws JSONException {
        JSONObject jsonobj = new JSONObject(table);
        JSONArray key = new JSONArray(jsonobj.getString(ontology.getKeyName()));
        HashMap<String, IRI> got = new HashMap<>();
        //get the right term in the glossary
        for (int i = 0; i < key.length(); i++) {
            JSONObject object = key.getJSONObject(i);
            if (object.has("Type")) {
                Iterator<String> it = object.keys();

                while (it.hasNext()) {
                    String type = object.getString(it.next());
                    String term = object.getString(it.next());
                    String uri = object.getString(it.next());

                    got.put(term, IRI.create(uri));
                }
            } else {
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


    public JSONArray storeResults(TestCaseResult testsuiteResult, Ontology ontology, JSONArray ontologyarray) throws JSONException {
        JSONObject testsResults = new JSONObject();
        if (testsuiteResult.getTestResult().equals("passed")) {
            // the ontology passed the test
            testsResults.put(ONTOLOGY, ontology.getProv().toString());
            testsResults.put(RESULT, "Passed");
            ontologyarray.put(testsResults);
        } else if (testsuiteResult.getTestResult().equals("undefined")) { // the terms needed to executeTest the tests are not defined inthe ontology
            testsResults.put(ONTOLOGY, ontology.getProv().toString());
            testsResults.put(RESULT, "Undefined");
            testsResults.put("Undefined", testsuiteResult.getUndefinedTerms());
            ontologyarray.put(testsResults);

        } else if (testsuiteResult.getTestResult().equals("incorrect")) { // the terms needed to executeTest the tests are not defined inthe ontology
            testsResults.put(ONTOLOGY, ontology.getProv().toString());
            testsResults.put(RESULT, "Incorrect");
            testsResults.put("Incorrect", testsuiteResult.getIncorrectTerms());
            ontologyarray.put(testsResults);
        } else if (testsuiteResult.getTestResult().equals("absent")) { // the ontology does not pass the test
            testsResults.put(ONTOLOGY, ontology.getProv().toString());
            testsResults.put(RESULT, "Absent");
            ontologyarray.put(testsResults);
        } else { // the ontology does not pass the test
            testsResults.put(ONTOLOGY, ontology.getProv().toString());
            testsResults.put(RESULT, "Conflict");
            ontologyarray.put(testsResults);
        }
        return ontologyarray;
    }

}
