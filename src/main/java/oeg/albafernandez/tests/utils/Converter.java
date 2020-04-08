package oeg.albafernandez.tests.utils;

import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Converter {

 public static String jsonToHtml(JSONArray jsonArray){

     String headerTable="<table style=\"width: 100%\" vocab=\"https://w3id.org/def/vtc#\">\n" +
             "     <thead>\n" +
             "          <tr>\n" +
             "               <th>Ontology</th>\n" +
             "               <th>Test</th>\n" +
             "               <th>Result</th>\n" +
             "          </tr>\n" +
             "      </thead>";
     String endTable="</table>\n";


     String startBody="<tbody>\n";
     for (int i = 0; i < jsonArray.length(); i++) {
         String test="";
         String ontology="";
         String result="";
         try {
             JSONObject jsonObject = jsonArray.getJSONObject(i);
             if (jsonObject.has("Test")) {
                 test = jsonObject.getString("Test");
                 // Aquí hacer algo con el valor
             }

             if(jsonObject.has("Results")){
                 JSONArray obj = jsonObject.getJSONArray("Results");
                 if(obj.getJSONObject(0).has("Result")){
                     result = obj.getJSONObject(0).getString("Result");
                 }
                 if(obj.getJSONObject(0).has("Ontology")){
                     ontology = obj.getJSONObject(0).getString("Ontology");
                 }
             }
             startBody+=" <tr vocab=\"https://w3id.org/def/vtc#\" typeof=\"TestCaseResult\">\n" +
                     "              <td> <span property=\"hasExecution\" typeof=\"Execution\"><a property=\"isExecutedOn\" href=\""+ontology+"\">"+ontology+"</a><span property=\"hasTestResult\" content=\""+result+"\"></span> </span></td>\n" +
                     "               <td property=\"isRelatedToDesign\" typeof=\"TestCaseDesign\"><span property=\"desiredBehaviour\">"+test+"</span></td>               \n" +
                     "              <td> "+result+" </td>\n" +
                     "          </tr>";
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
     String endBody="</tbody>\n";

     String table = headerTable+startBody+endBody+endTable;
     return table;

 }

    public static String jsonToJUnitXML(JSONArray jsonArray){

        Integer failures=0;


        String testcases="";

        for (int i = 0; i < jsonArray.length(); i++) {
            String test="";
            String ontology="";
            String result="";
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("Test")) {
                    test = jsonObject.getString("Test");
                    testcases+= "\t\t<testcase id=\"testCase"+(i+1)+"\" name=\""+test+"\">\n";
                }

                if(jsonObject.has("Results")){
                    JSONArray obj = jsonObject.getJSONArray("Results");
                    if(obj.getJSONObject(0).has("Result")){
                        result = obj.getJSONObject(0).getString("Result");
                        if(obj.getJSONObject(0).has("Ontology")){
                            ontology = obj.getJSONObject(0).getString("Ontology");
                        }
                        if(!result.equalsIgnoreCase("passed")){
                            String msg="";
                            failures++;
                            if(result.equalsIgnoreCase("conflict")){
                                msg="There is an inconsistency between the ontology and the requirement associated to the test\n";
                            }else if(result.equalsIgnoreCase("Absent")){
                                msg="A restriction included in the test may be missing in the ontology";
                            }else if(result.equalsIgnoreCase("Undefined")){
                                if(obj.getJSONObject(0).has("Undefined")){
                                    msg="The terms "+obj.getJSONObject(0).get("Undefined").toString().replace("\"","").replace("[","").replace("]","")+" are undefined in the ontology";
                                }else
                                    msg="There are undefined terms in the ontology";
                            }else if(result.equalsIgnoreCase("Incorrect")){
                                if(obj.getJSONObject(0).has("Incorrect")){
                                    msg="The terms "+obj.getJSONObject(0).get("Incorrect").toString().replace("\"","").replace("[","").replace("]","")+" are not equally defined in the test and  in the ontology";
                                }else
                                    msg="There are tests in the test that are not defined as in the ontology";
                            }
                            testcases+="\t\t\t<failure message="+msg+" with URI "+ontology+">" +
                                    "</failure>\n";
                        }
                        testcases+="\t\t</testcase>\n";
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



        String headerTable="<?xml version=\"1.0\" encoding=\"UTF-8\" ?> \n" +
                "<testsuites  name=\"\" tests=\""+jsonArray.length()+"\">\n" +
                "\t<testsuite  name=\"\" tests=\""+jsonArray.length()+"\" failures=\""+failures+"\">\n" +
                "";

        String endTable="\t</testsuite>\n" +
                "</testsuites>\n";

        String table = headerTable+testcases+endTable;
        return table;

    }
}
