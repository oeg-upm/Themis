package oeg.albafernandez.tests.utils;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    static final Logger logger = Logger.getLogger(Utils.class);
    static final String PATTERN = "\\<(.*?)\\>";


    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getPrecTerm(String query){

        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(query);
        while(m.find()){
            return  m.group();
        }
        return  m.group();
    }

    public static String termsInOntology(String query, OWLOntology ontology){
        Pattern p = Pattern.compile(PATTERN); // VERIFICAR ESTO
        Matcher m = p.matcher(query);
        while(m.find()){
            if(ontology.containsEntityInSignature(IRI.create(m.group().toString().replace(
                    "<","").replace(">",""))))
                return  "true";
        }
        return  "false";
    }

    public static String mapImplementationTerms(String  query, HashMap<String, IRI> allvalues) {
            Pattern p = Pattern.compile(PATTERN);
            Matcher m = p.matcher(query);
            String querym = query;
            while (m.find()) {
                try {
                    for (Map.Entry<String, IRI> entry : allvalues.entrySet()) {
                        if (entry.getKey().toLowerCase().equals(m.group().toLowerCase().replace("<", "").replace(">", ""))) {
                            querym = querym.replace("<" + entry.getKey() + ">", "<" + entry.getValue() + ">");
                        }
                    }
                    querym = querym.replace("<string>", "<http://www.w3.org/2001/XMLSchema#string>");

                } catch (Exception e) {
                    logger.error("ERROR WHILE PARSING IMPLEMENTATION TERMS: "+ e.getMessage());
                }
            }
            return querym;

    }

    public static Set<OWLAxiom> mapImplementationTerms(OWLOntology queries, HashMap<String, IRI> allvalues) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(queries));

        for(OWLAxiom axiom: queries.getAxioms()) {

            Pattern p = Pattern.compile(PATTERN);
            String query = axiom.toString();
            Matcher m = p.matcher(query);
            while (m.find()) {
                try {
                    for (Map.Entry<String, IRI> entry : allvalues.entrySet()) {
                        if (entry.getKey().equals(m.group().replace("<", "").replace(">", ""))) {
                            queries.getOWLOntologyManager().applyChanges(renamer.changeIRI(IRI.create(entry.getKey()), entry.getValue()));
                        }
                    }
                    queries.getOWLOntologyManager().applyChanges(renamer.changeIRI(IRI.create("string"), IRI.create("http://www.w3.org/2001/XMLSchema#string")));

                } catch (Exception e) {
                    logger.error("ERROR WHILE PARSING IMPLEMENTATION TERMS IN OWL ONTOLOGY QUERIES: "+ e.getMessage());
                }
            }
        }
        return queries.getAxioms();
    }




}
