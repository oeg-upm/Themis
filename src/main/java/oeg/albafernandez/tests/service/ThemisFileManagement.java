package oeg.albafernandez.tests.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.IOException;
import java.util.ArrayList;

public class ThemisFileManagement {

    public String loadTests(String testuri) throws OWLOntologyStorageException, IOException, OWLOntologyCreationException, JSONException {
        if(testuri!=null) {
            ArrayList<String> testsuiteDesign = new ArrayList<>();
            ThemisImplementationService impl = new ThemisImplementationService();
            testsuiteDesign.addAll(impl.loadTestCaseDesign(testuri));
            JSONArray tests = new JSONArray();
            for (String test : testsuiteDesign) {
                JSONObject obj = new JSONObject();
                obj.put("Test", test);
                tests.put(obj);
            }

            return tests.toString();
        }else
            return null;
    }

}
