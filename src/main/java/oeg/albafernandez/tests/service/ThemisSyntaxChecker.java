package oeg.albafernandez.tests.service;

import com.google.gson.Gson;
import oeg.albafernandez.tests.model.Ontology;
import oeg.albafernandez.tests.utils.GoT;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThemisSyntaxChecker {
    static final Logger logger = Logger.getLogger(ThemisImplementer.class);

    public String syntaxChecker(String completetest) throws JSONException {
        String testClean=completetest;
        if (completetest.contains(";")) {
            testClean=completetest.split(";")[1].replaceAll("\n", "").trim();
        }
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile("^[^\\s]+"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(type)\\s+[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(some|only|max\\s+\\d|min\\s+\\d|exactly\\s+\\d|and)\\s+[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+[^\\s]+\\s+(some)\\s+[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(some|only)\\s+[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+(disjointwith)\\s+[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(disjointwith)\\s+[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(characteristic symmetricproperty)\\s*$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(equivalentto)\\s+[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+only\\s*[^\\s]+\\s+(or)\\s+[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+(domain|range)\\s+[^\\s]+$"));
        patterns.add(Pattern.compile("^[^\\s]+\\s+[^\\s]+\\s+[^\\s]+$"));

        for (Pattern patternInd: patterns) {
            Matcher matcher = patternInd.matcher(testClean.toLowerCase().trim());
            if (matcher.matches()) {
                return "true";
            }
        }
        return "false";
    }


    public String autocomplete(String completetest, String term,   String ontology, String filename) throws JSONException {

        if(completetest != null && term != null && ontology != null) {
            JSONArray gotterms = new JSONArray();
            ArrayList<String> keys = new ArrayList<>();
            String testClean=completetest;
            if(term.trim().equals(";")){
                testClean = term.trim();
            }else if (completetest.contains(";")) {
                testClean=completetest.split(";")[1].replaceAll("\n", "").trim();
            }

            term = term.trim().replaceAll("\n", "");
            Pattern p = Pattern.compile("^.*" + term.toLowerCase() + ".*");
            Pattern test0 = Pattern.compile("^[^\\s]+");
            Pattern test1 = Pattern.compile("^[^\\s]+\\s*$");
            Pattern test11 = Pattern.compile("^[^\\s]+\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test2 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test3 = Pattern.compile("^[^\\s]+\\s+(type)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test4 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s" + term.toLowerCase() + "[^\\s]*$");
            Pattern test5 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(some|only|max\\s+\\d|min\\s+\\d|exactly\\s+\\d|and)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test6 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+" + term.toLowerCase() + "[^\\s]*"); // property
            Pattern test7 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+[^\\s]+\\s+" + term.toLowerCase() + "[^\\s]*"); // some
            Pattern test9 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+[^\\s]+\\s+(some)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test10 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test111 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test12 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+[^\\s]+\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test13 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+[^\\s]+\\s+(subclassof)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test14 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test15 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(some|only)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test16 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*" + term.toLowerCase() + "[^\\s]*$");
            Pattern test17 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test18 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test19 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test20 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test21 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+[^\\s]+\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test22 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+(disjointwith)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test23 = Pattern.compile("^[^\\s]+\\s+(characteristic symmetricproperty)$"); // got
            Pattern test27 = Pattern.compile("^[^\\s]+\\s+(disjointwith)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test28 = Pattern.compile("^[^\\s]+\\s+(equivalentto)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test29 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+only\\s*[^\\s]+\\s+" + term.toLowerCase() + "[^\\s]*$");//and or
            Pattern test30 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+only\\s*[^\\s]+\\s+(and|or)\\s+" + term.toLowerCase() + "[^\\s]*$"); //got
            Pattern test31 = Pattern.compile("^[^\\s]+\\s+(domain|range)\\s+" + term.toLowerCase() + "[^\\s]*$");
            Pattern test32 = Pattern.compile("^;$");


            Matcher m0 = test0.matcher(testClean.toLowerCase());
            Matcher m1 = test1.matcher(testClean.toLowerCase());
            Matcher m11 = test11.matcher(testClean.toLowerCase());
            Matcher m2 = test2.matcher(testClean.toLowerCase());
            Matcher m3 = test3.matcher(testClean.toLowerCase());
            Matcher m4 = test4.matcher(testClean.toLowerCase());
            Matcher m5 = test5.matcher(testClean.toLowerCase());
            Matcher m6 = test6.matcher(testClean.toLowerCase());
            Matcher m7 = test7.matcher(testClean.toLowerCase());
            Matcher m9 = test9.matcher(testClean.toLowerCase());
            Matcher m10 = test10.matcher(testClean.toLowerCase());
            Matcher m111 = test111.matcher(testClean.toLowerCase());
            Matcher m12 = test12.matcher(testClean.toLowerCase());
            Matcher m13 = test13.matcher(testClean.toLowerCase());
            Matcher m14 = test14.matcher(testClean.toLowerCase());
            Matcher m15 = test15.matcher(testClean.toLowerCase());
            Matcher m16 = test16.matcher(testClean.toLowerCase());
            Matcher m17 = test17.matcher(testClean.toLowerCase());
            Matcher m18 = test18.matcher(testClean.toLowerCase());
            Matcher m19 = test19.matcher(testClean.toLowerCase());
            Matcher m20 = test20.matcher(testClean.toLowerCase());
            Matcher m21 = test21.matcher(testClean.toLowerCase());
            Matcher m22 = test22.matcher(testClean.toLowerCase());
            Matcher m23 = test23.matcher(testClean.toLowerCase());
            Matcher m27 = test27.matcher(testClean.toLowerCase());
            Matcher m28 = test28.matcher(testClean.toLowerCase());
            Matcher m29 = test29.matcher(testClean.toLowerCase());
            Matcher m30 = test30.matcher(testClean.toLowerCase());
            Matcher m31 = test31.matcher(testClean.toLowerCase());
            Matcher m32 = test32.matcher(testClean.toLowerCase());

            ArrayList<String> values = new ArrayList<>();

            if (m12.matches() || m17.matches()) {
                values.add("subClassOf");
            } else if (m1.matches() || m11.matches() || m27.matches() || m28.matches()) {
                values.add("subClassOf");
                values.add("characteristic symmetricproperty");
                values.add("type");
                values.add("domain");
                values.add("range");
                values.add("disjointWith");
                values.add("equivalentTo");
            } else if (m3.matches()) {
                values.add("Class");
                values.add("Individual");
                values.add("Property");
            } else if (m4.matches()) {
                values.add("some");
                values.add("only");
                values.add("and");
                values.add("min 1");
                values.add("max 1");
                values.add("exactly 1");
                values.add("value");
                values.add("that");
            } else if (m7.matches()) {
                values.add("some");
            } else if (m10.matches() || m19.matches()) {
                values.add("and");
            }  else if (m14.matches() ) {
                values.add("some");
                values.add("only");
            } else if (m21.matches()) {
                values.add("disjointWith");
            }  else if (m2.matches()) {
                values.add("symmetricProperty(");
            } else if (m29.matches()) {
                values.add("and");
                values.add("or");
            }else if (m32.matches()){
                values.add(";");
            }

            if ( m1.matches() || m11.matches()) {
                Ontology owlOntology = new Ontology();
                if(!ontology.equals(""))
                    owlOntology.loadOntologyURL(ontology);
                else
                    owlOntology.loadOntologyfile(filename);

                HashMap<String, IRI> got;

                got = (HashMap<String, IRI>) createGotOnlyProperties(owlOntology);


                for (Map.Entry<String, IRI> entry : got.entrySet()) {
                    Matcher m = p.matcher(entry.getKey().toLowerCase());
                    if (m.matches() && !keys.contains(entry.getKey())) {
                        JSONObject obj = new JSONObject();
                        obj.put("label", entry.getKey().trim());
                        obj.put("value", entry.getKey().trim());
                        gotterms.put(obj);
                        keys.add(entry.getKey());
                    }
                }
            }

            if ( m6.matches() || completetest.equals("") || m2.matches() || m3.matches() || m0.matches() || m5.matches() || m9.matches() || m111.matches() || m13.matches() || m15.matches() || m16.matches() || m18.matches() || m20.matches() || m22.matches() || m23.matches() || m27.matches() || m28.matches() || m30.matches() || m31.matches()) {
                Ontology owlOntology = new Ontology();
                if(!ontology.equals(""))
                    owlOntology.loadOntologyURL(ontology);
                else
                    owlOntology.loadOntologyfile(filename);

                HashMap<String, IRI> got;

                got = (HashMap<String, IRI>) createGot(owlOntology);


                for (Map.Entry<String, IRI> entry : got.entrySet()) {
                    Matcher m = p.matcher(entry.getKey().toLowerCase());
                    if (m.matches() && !keys.contains(entry.getKey())) {
                        JSONObject obj = new JSONObject();
                        obj.put("label", entry.getKey().trim());
                        obj.put("value", entry.getKey().trim());
                        gotterms.put(obj);
                        keys.add(entry.getKey());
                    }
                }
            }

            for (String value : values) {
                Matcher m = p.matcher(value.toLowerCase());
                if (m.matches()) {
                    JSONObject obj = new JSONObject();
                    obj.put("label", value.trim());
                    obj.put("value", value.trim());
                    gotterms.put(obj);
                }
            }

            return sort(gotterms).toString();
        }else
            return "";

    }

    public JSONArray sort(JSONArray gotterms) throws JSONException {
        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < gotterms.length(); i++) {
            jsonValues.add(gotterms.getJSONObject(i));
        }
        JSONArray sortedJsonArray = new JSONArray();
        Collections.sort( jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "label";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(KEY_NAME);
                    valB = (String) b.get(KEY_NAME);
                }
                catch (JSONException e) {
                   logger.error(e.getMessage());
                }
                return valA.compareTo(valB);

            }
        });

        for (int i = 0; i < gotterms.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }
        return sortedJsonArray;
    }

    public Map createGot(Ontology ontology){
        HashMap<String, IRI> got = new HashMap<>();
        got.putAll(ontology.getClasses());
        got.putAll(ontology.getDatatypeProperties());
        got.putAll(ontology.getObjectProperties());
        got.putAll(ontology.getIndividuals());
        return got;
    }
    public Map createGotOnlyProperties(Ontology ontology){
        HashMap<String, IRI> got = new HashMap<>();
        got.putAll(ontology.getDatatypeProperties());
        got.putAll(ontology.getObjectProperties());
        return got;
    }

    public  String  getGoT(Ontology onto) throws JSONException, OWLOntologyStorageException {

            HashMap<String, IRI> elements = new HashMap<>();
            elements.putAll(onto.getClasses());
            elements.putAll(onto.getObjectProperties());
            elements.putAll(onto.getDatatypeProperties());
            elements.putAll(onto.getIndividuals());

            GoT report = new GoT();
            String reportText = report.generateReport(onto.getKeyName(), elements);
            JSONObject linkToReportGoT = new JSONObject();
            linkToReportGoT.put("got", reportText);
            linkToReportGoT.put("key", onto.getKeyName());
            linkToReportGoT.put("uri", onto.getProv());

        return linkToReportGoT.toString();
    }

    public  String  getGoTFromURI(String uri) throws JSONException, OWLOntologyStorageException {
            Ontology onto = new Ontology();
            onto.loadOntologyURL(uri.replace("\"", "").trim());
            return getGoT(onto);

    }
    public  String  getGoTFromFilename(String ontologycode) throws JSONException, OWLOntologyStorageException {
            Ontology onto = new Ontology();
            onto.loadOntologyfile(ontologycode);
            return getGoT(onto);
    }

    public  String  getPlainGoT(Ontology onto) throws JSONException, OWLOntologyStorageException {

            HashMap<String, IRI> elements = new HashMap<>();
            elements.putAll(onto.getClasses());
            elements.putAll(onto.getObjectProperties());
            elements.putAll(onto.getDatatypeProperties());
            elements.putAll(onto.getIndividuals());

            String json = new Gson().toJson(elements);

            JSONObject linkToReportGoT = new JSONObject();
            linkToReportGoT.put("got", json);
            linkToReportGoT.put("key", onto.getKeyName());


            return linkToReportGoT.toString();

    }
    public  String  getPlainGoTFromURI(String uri) throws JSONException, OWLOntologyStorageException {

            Ontology onto = new Ontology();
            onto.loadOntologyURL(uri.replace("\"", "").trim());
           return getPlainGoT(onto);
    }
    public  String  getPlainGoTFromFile(String filename) throws JSONException, OWLOntologyStorageException {
            Ontology onto = new Ontology();
            onto.loadOntologyfile(filename);
            return getPlainGoT(onto);


    }


}
