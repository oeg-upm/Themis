package oeg.albafernandez.tests.service;

import com.google.gson.Gson;
import oeg.albafernandez.tests.model.Ontology;
import oeg.albafernandez.tests.utils.Got;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.IRI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThemisGuideGenerator {
    static final Logger logger = Logger.getLogger(ThemisGuideGenerator.class);
    private static final String FINAL = "[^\\s]*$";
    private static final String LABEL = "label";
    private static final String VALUE = "value";


    public String syntaxChecker(String completetest)  {
        String testClean = completetest;
        if (completetest.contains(";")) {
            testClean = completetest.split(";")[1].replaceAll("\n", "").trim();
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

        for (Pattern patternInd : patterns) {
            Matcher matcher = patternInd.matcher(testClean.toLowerCase().trim());
            if (matcher.matches()) {
                return "true";
            }
        }
        return "false";
    }

    public List<Matcher> createPatterns(String completetest, String term, String ontology) {
        ArrayList<Matcher> matchers = new ArrayList<>();
        if (completetest != null && term != null && ontology != null) {
            String testClean = completetest;
            if (term.trim().equals(";")) {
                testClean = term.trim();
            } else if (completetest.contains(";")) {
                testClean = completetest.split(";")[1].replaceAll("\n", "").trim();
            }

            term = term.trim().replaceAll("\n", "");
            Pattern p = Pattern.compile("^.*" + term.toLowerCase() + ".*");
            Pattern test0 = Pattern.compile("^[^\\s]+");
            Pattern test1 = Pattern.compile("^[^\\s]+\\s*$");
            Pattern test2 = Pattern.compile("^[^\\s]+\\s+" + term.toLowerCase() + FINAL);
            Pattern test3 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+" + term.toLowerCase() + FINAL);
            Pattern test4 = Pattern.compile("^[^\\s]+\\s+(type)\\s+" + term.toLowerCase() + FINAL);
            Pattern test5 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s" + term.toLowerCase() + FINAL);
            Pattern test6 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(some|only|max\\s+\\d|min\\s+\\d|exactly\\s+\\d|and)\\s+" + term.toLowerCase() + FINAL);
            Pattern test7 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+" + term.toLowerCase() + FINAL); // property
            Pattern test8 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+[^\\s]+\\s+" + term.toLowerCase() + FINAL); // some
            Pattern test9 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+[^\\s]+\\s+(some)\\s+" + term.toLowerCase() + FINAL);
            Pattern test10 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+" + term.toLowerCase() + FINAL);
            Pattern test11 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+" + term.toLowerCase() + FINAL);
            Pattern test12 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+[^\\s]+\\s+" + term.toLowerCase() + FINAL);
            Pattern test13 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+[^\\s]+\\s+(subclassof)\\s+" + term.toLowerCase() + FINAL);
            Pattern test14 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+" + term.toLowerCase() + FINAL);
            Pattern test15 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(min\\s+\\d)\\s+[^\\s]+\\s+(and)\\s+[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(some|only)\\s+" + term.toLowerCase() + FINAL);
            Pattern test16 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*" + term.toLowerCase() + FINAL);
            Pattern test17 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+" + term.toLowerCase() + FINAL);
            Pattern test18 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+" + term.toLowerCase() + FINAL);
            Pattern test19 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+" + term.toLowerCase() + FINAL);
            Pattern test20 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+" + term.toLowerCase() + FINAL);
            Pattern test21 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+[^\\s]+\\s+" + term.toLowerCase() + FINAL);
            Pattern test22 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s*(and)\\s*[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+(that)\\s+(disjointwith)\\s+" + term.toLowerCase() + FINAL);
            Pattern test23 = Pattern.compile("^[^\\s]+\\s+(characteristic symmetricproperty)$"); // got
            Pattern test24 = Pattern.compile("^[^\\s]+\\s+(disjointwith)\\s+" + term.toLowerCase() + FINAL);
            Pattern test25 = Pattern.compile("^[^\\s]+\\s+(equivalentto)\\s+" + term.toLowerCase() + FINAL);
            Pattern test26 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+only\\s*[^\\s]+\\s+" + term.toLowerCase() + FINAL);//and or
            Pattern test27 = Pattern.compile("^[^\\s]+\\s+(subclassof)\\s+[^\\s]+\\s+only\\s*[^\\s]+\\s+(and|or)\\s+" + term.toLowerCase() + FINAL); //got
            Pattern test28 = Pattern.compile("^[^\\s]+\\s+(domain|range)\\s+" + term.toLowerCase() + FINAL);
            Pattern test29 = Pattern.compile("^;$");

            Matcher mp = p.matcher(testClean.toLowerCase());
            Matcher m0 = test0.matcher(testClean.toLowerCase());
            Matcher m1 = test1.matcher(testClean.toLowerCase());
            Matcher m2 = test2.matcher(testClean.toLowerCase());
            Matcher m3 = test3.matcher(testClean.toLowerCase());
            Matcher m4 = test4.matcher(testClean.toLowerCase());
            Matcher m5 = test5.matcher(testClean.toLowerCase());
            Matcher m6 = test6.matcher(testClean.toLowerCase());
            Matcher m7 = test7.matcher(testClean.toLowerCase());
            Matcher m8 = test8.matcher(testClean.toLowerCase());
            Matcher m9 = test9.matcher(testClean.toLowerCase());
            Matcher m10 = test10.matcher(testClean.toLowerCase());
            Matcher m11 = test11.matcher(testClean.toLowerCase());
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
            Matcher m24 = test24.matcher(testClean.toLowerCase());
            Matcher m25 = test25.matcher(testClean.toLowerCase());
            Matcher m26 = test26.matcher(testClean.toLowerCase());
            Matcher m27 = test27.matcher(testClean.toLowerCase());
            Matcher m28 = test28.matcher(testClean.toLowerCase());
            Matcher m29 = test29.matcher(testClean.toLowerCase());

            matchers.add(m0);
            matchers.add(m1);
            matchers.add(m2);
            matchers.add(m3);
            matchers.add(m4);
            matchers.add(m5);
            matchers.add(m6);
            matchers.add(m7);
            matchers.add(m8);
            matchers.add(m9);
            matchers.add(m10);
            matchers.add(m11);
            matchers.add(m12);
            matchers.add(m13);
            matchers.add(m14);
            matchers.add(m15);
            matchers.add(m16);
            matchers.add(m17);
            matchers.add(m18);
            matchers.add(m19);
            matchers.add(m20);
            matchers.add(m21);
            matchers.add(m22);
            matchers.add(m23);
            matchers.add(m24);
            matchers.add(m25);
            matchers.add(m26);
            matchers.add(m27);
            matchers.add(m28);
            matchers.add(m29);
            matchers.add(mp);


        }

        return matchers;
    }

    public JSONArray createGotKeywords(List<Matcher> matchers,  ArrayList<String> values){
        JSONArray gotTerms = new JSONArray();
        for (String value : values) {
            if (matchers.get(30).matches()) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put(LABEL, value.trim());
                } catch (JSONException e) {
                   logger.error(e.getMessage());
                }
                try {
                    obj.put(VALUE, value.trim());
                } catch (JSONException e) {
                    logger.error(e.getMessage());
                }
                gotTerms.put(obj);
            }
        }
        return gotTerms;
    }

    public JSONArray getKeywordsForPattern(List<Matcher> matchers) {
        JSONArray gotTerms;
        ArrayList<String> values = new ArrayList<>();

        if (matchers.get(12).matches() || matchers.get(17).matches()) {
            values.add("subClassOf");
        } else if (matchers.get(2).matches() || matchers.get(24).matches()) {
            values.add("subClassOf");
            values.add("characteristic symmetricproperty");
            values.add("type");
            values.add("domain");
            values.add("range");
            values.add("disjointWith");
            values.add("equivalentTo");
        } else if (matchers.get(4).matches()) {
            values.add("Class");
            values.add("Individual");
            values.add("Property");
        } else if (matchers.get(5).matches()) {
            values.add("some");
            values.add("only");
            values.add("and");
            values.add("min 1");
            values.add("max 1");
            values.add("exactly 1");
            values.add("value");
            values.add("that");
        } else if (matchers.get(8).matches()) {
            values.add("some");
        } else if (matchers.get(10).matches() || matchers.get(19).matches()) {
            values.add("and");
        } else if (matchers.get(14).matches()) {
            values.add("some");
            values.add("only");
        } else if (matchers.get(21).matches()) {
            values.add("disjointWith");
        } else if (matchers.get(26).matches()) {
            values.add("and");
            values.add("or");
        } else if (matchers.get(29).matches()) {
            values.add(";");
        }

        gotTerms = createGotKeywords( matchers,  values);
        return gotTerms;

    }

    public JSONArray createGotOntologyTerms( HashMap<String, IRI> got,  ArrayList<String> keys, String term){
        JSONArray gotTerms = new JSONArray();
        Pattern p = Pattern.compile("^.*" + term.toLowerCase() + ".*");

        for (Map.Entry<String, IRI> entry : got.entrySet()) {
            Matcher m = p.matcher(entry.getKey().toLowerCase());

            if (m.matches() && !keys.contains(entry.getKey())) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put(LABEL, entry.getKey().trim());
                } catch (JSONException e) {
                    logger.error(e.getMessage());
                }
                try {
                    obj.put(VALUE, entry.getKey().trim());
                } catch (JSONException e) {
                    logger.error(e.getMessage());
                }
                gotTerms.put(obj);
                keys.add(entry.getKey());
            }
        }
        return gotTerms;
    }

    public JSONArray getOntologyTermsForPattern(List<Matcher> matchers, String ontology, String filename, String completeTest, String term) {
        JSONArray gotTerms ;
        ArrayList<String> keys = new ArrayList<>();
        HashMap<String, IRI> got = new HashMap<>();
        Ontology owlOntology = new Ontology();
        if (!ontology.equals(""))
            owlOntology.loadOntologyFromURL(ontology);
        else
            owlOntology.loadOntologyFromfile(filename);

        if (matchers.get(7).matches() || matchers.get(20).matches()) {

            got = (HashMap<String, IRI>) createGotOnlyProperties(owlOntology);
        }

        if (matchers.get(4).matches() || matchers.get(6).matches() || matchers.get(9).matches() || matchers.get(22).matches() || matchers.get(11).matches() || matchers.get(15).matches() || matchers.get(28).matches() || matchers.get(24).matches() || matchers.get(25).matches()) {

            got = (HashMap<String, IRI>) createGotOnlyClasses(owlOntology);
        }

        if ( completeTest.equals("") || matchers.get(2).matches() ||  matchers.get(3).matches() || matchers.get(0).matches() ||   matchers.get(13).matches() ||  matchers.get(16).matches() || matchers.get(18).matches() ||  matchers.get(23).matches() ||  matchers.get(27).matches() || matchers.get(28).matches()) {

            got = (HashMap<String, IRI>) createGot(owlOntology);
        }

        gotTerms = createGotOntologyTerms(  got,   keys,  term);

        return gotTerms;
    }

    public String autoComplete(String completeTest, String term, String ontology, String filename) throws JSONException {
        JSONArray gotComplete = new JSONArray();

        List<Matcher> matchers = createPatterns(completeTest, term, ontology);
        JSONArray keywords = getKeywordsForPattern(matchers);
        JSONArray properties = getOntologyTermsForPattern(matchers, ontology, filename, completeTest, term);

        for(int i = 0; i< keywords.length();i++){
            gotComplete.put(keywords.getJSONObject(i));
        }
        for(int i = 0; i< properties.length();i++){
            gotComplete.put(properties.getJSONObject(i));
        }
        return sort(gotComplete).toString();
    }

    public JSONArray sort(JSONArray gotTerms) throws JSONException {
        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < gotTerms.length(); i++) {
            jsonValues.add(gotTerms.getJSONObject(i));
        }
        JSONArray sortedJsonArray = new JSONArray();
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = "";
                String valB = "";

                try {
                    valA = (String) a.get(LABEL);
                    valB = (String) b.get(LABEL);
                } catch (JSONException e) {
                    logger.error(e.getMessage());
                }
                return valA.compareTo(valB);

            }
        });

        for (int i = 0; i < gotTerms.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }
        return sortedJsonArray;
    }

    public Map createGot(Ontology ontology) {
        HashMap<String, IRI> got = new HashMap<>();
        got.putAll(ontology.getClasses());
        got.putAll(ontology.getDatatypeProperties());
        got.putAll(ontology.getObjectProperties());
        got.putAll(ontology.getIndividuals());
        return got;
    }

    public Map createGotOnlyClasses(Ontology ontology) {
        HashMap<String, IRI> got = new HashMap<>();
        got.putAll(ontology.getClasses());
        got.putAll(ontology.getIndividuals());
        return got;
    }

    public Map createGotOnlyProperties(Ontology ontology) {
        HashMap<String, IRI> got = new HashMap<>();
        got.putAll(ontology.getDatatypeProperties());
        got.putAll(ontology.getObjectProperties());
        return got;
    }

    public String getGoT(Ontology onto) throws JSONException {

        HashMap<String, IRI> elements = new HashMap<>();
        elements.putAll(onto.getClasses());
        elements.putAll(onto.getObjectProperties());
        elements.putAll(onto.getDatatypeProperties());
        elements.putAll(onto.getIndividuals());

        if (!elements.isEmpty()) {
            Got report = new Got();
            String reportText = report.generateReport(onto.getKeyName(), (HashMap<String, IRI>) onto.getClasses(), onto.getObjectProperties(), onto.getDatatypeProperties(), onto.getIndividuals());
            JSONObject linkToReportGoT = new JSONObject();
            linkToReportGoT.put("got", reportText);
            linkToReportGoT.put("key", onto.getKeyName());
            linkToReportGoT.put("uri", onto.getProv());
            return linkToReportGoT.toString();
        } else {
            return null;

        }

    }

    public String getGoTFromURI(String uri) throws JSONException {
        Ontology onto = new Ontology();
        onto.loadOntologyFromURL(uri.replace("\"", "").trim());
        return getGoT(onto);

    }

    public String getGoTFromFilename(String ontologycode) throws JSONException {
        Ontology onto = new Ontology();
        onto.loadOntologyFromfile(ontologycode);
        return getGoT(onto);
    }

    public String getPlainGoT(Ontology onto) throws JSONException {

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

    public String getPlainGoTFromURI(String uri) throws JSONException {

        Ontology onto = new Ontology();
        onto.loadOntologyFromURL(uri.replace("\"", "").trim());
        return getPlainGoT(onto);
    }

    public String getPlainGoTFromFile(String filename) throws JSONException {
        Ontology onto = new Ontology();
        onto.loadOntologyFromfile(filename);
        return getPlainGoT(onto);


    }


}
