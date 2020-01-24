package oeg.albafernandez.tests.utils;

import org.semanticweb.owlapi.model.IRI;

import java.util.*;

public class GoT {
    private String table;
    private String uri;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String generateTable(HashMap<String, IRI> terms){
        String tableLocal ="";
        List<String> sortedByKey = new ArrayList<>(terms.keySet());
        Collections.sort(sortedByKey);
        for (String key : sortedByKey) {
            tableLocal += "\t\t\t\t<tr class = \"got\">\n";
            tableLocal += "\t\t\t\t\t<td  class=\"term got\" class=\"tg-031e\">" + key + "</td>\n";
            tableLocal += "\t\t\t\t\t<td class=\"uri got tg-031e \">" + terms.get(key) + "</td>\n";
            tableLocal += "\t\t\t\t\t<td class=\"tg-031e\"> <button  id=\"bEdit\" type=\"button\" class=\"btn btn-sm btn-default\" onclick=\"rowEdit(this);\"><span class=\"glyphicon glyphicon-pencil\"></span>EDIT TERM</button> </td>\n";
            tableLocal += "\t\t\t\t\t<td class=\"tg-031e\"  style=\"display: none;\">  <button  id=\"bRemove\" type=\"button\" class=\"btn btn-sm btn-default\" onclick=\"rowSave(this);\">SAVE </button> </td>\n";
            tableLocal += "\t\t\t\t</tr>\n";
        }
        return tableLocal;
    }

    public String generateReport(String key, HashMap<String, IRI> elements){
        String report="";

        report += TextConstants.headclasseswithkey(key);
        report += generateTable(elements);
        report += TextConstants.endTables;
        return report;

    }

}
