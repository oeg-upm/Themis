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
             "               <th>Test</th>\n" +
             "               <th>Result</th>\n" +
             "               <th>Ontology</th>\n" +
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
                     "               <td property=\"isRelatedToDesign\" typeof=\"TestCaseDesign\"><span property=\"desiredBehaviour\">"+test+"</span></td>               \n" +
                     "\n" +
                     "              <td> <span property=\"hasExecution\" typeof=\"Execution\"><a property=\"isExecutedOn\" href=\""+ontology+"\">"+ontology+"</a><span property=\"hasTestResult\" content=\""+result+"\"></span> </span></td>\n" +
                     "\n" +
                     "              <td> "+result+" </td>\n" +
                     "            \n" +
                     "          </tr>";
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
     String endBody="</tbody>\n";

     String table = headerTable+startBody+endBody+endTable;
     return table;

 }
}
