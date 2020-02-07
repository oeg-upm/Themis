package oeg.albafernandez.tests.service;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import oeg.albafernandez.tests.utils.Utils;
import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import java.util.*;
import oeg.albafernandez.tests.model.*;

/*
* Class to executeTest the tests on an ontology
* */
public class ThemisExecuter {
    final static Logger logger = Logger.getLogger(ThemisExecuter.class);

    /*Here we check the ontology consistency regarding at model-level*/
    public  String tboxTest(String query, IRI key, OWLOntologyManager manager, OWLOntology ontology) {

        StructuralReasonerFactory factory = new StructuralReasonerFactory();
        /*init reasoner*/
        OWLReasonerConfiguration config = new SimpleConfiguration();
        OWLReasoner reasoner = factory.createReasoner(ontology,config);
        reasoner.precomputeInferences();
        /*execute precondition SPARQL query to check that all classes and terms in the test exist in the ontology*/

        Query queryOWLAPI = null;
        try {
            queryOWLAPI = Query.create(query); // create the query
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Check SPARQL-DL syntax: " + e.getMessage() + " ID: " + key);
        }

        QueryEngine engine = QueryEngine.create(manager, reasoner); // create the query engine
        QueryResult result;

        try {
            result = engine.execute(queryOWLAPI); // execute query

        } catch (Exception e) {
            e.printStackTrace();
            return "false";
        }

        if (queryOWLAPI.isAsk()) { // check result
            if (result.ask()) {
                return "true";
            } else {
                return "false";
            }
        } else
            return "false";
    }

    /*Here we check the ontology consistency regarding at instance-level*/
    public  String aboxTest(Set<OWLAxiom> textAxioms, Ontology ontology, String type) throws OWLOntologyStorageException, OWLOntologyCreationException {
        //add changes to the ontology
        ontology = addChanges(textAxioms, ontology);
        //executeTest the reasoner to check the ontolgy status
        String result = executeReasoner( textAxioms, ontology);
        //remove axioms if it is a preparation set
        if(!type.equals("preparation")) {
            ontology.getManager().removeAxioms(ontology.getOntology(), textAxioms);
        }
        return result;
    }

    /*Method to add changes to the ontology */
    public  Ontology addChanges(Set<OWLAxiom> axioms, Ontology ontology) throws OWLOntologyStorageException, OWLOntologyCreationException {

        // We will create several things, so we save an instance of the data factory
        OWLDataFactory dataFactory = ontology.getManager().getOWLDataFactory();

        for (OWLAxiom axiom : axioms) {
            AddAxiom addAxiom;
            if(axiom.isAnnotationAxiom()){ //Sometimes the object properties are translated as annotated axioms incorrectly and they have to be changed.
                OWLAnnotationAssertionAxiom annotationAssertionAxiom = (OWLAnnotationAssertionAxiom)axiom;
                OWLNamedIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getSubject().toString()));
                OWLNamedIndividual ind2 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getValue().toString()));

                if(ontology.getOntology().containsObjectPropertyInSignature(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")),true)){
                    OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")));
                    OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(
                            prop, ind1,ind2);
                    if(!ontology.getOntology().getAxioms().contains(axiom)) {
                        addAxiom = new AddAxiom(ontology.getOntology(), owlObjectPropertyAssertionAxiom);
                        ontology.getManager().applyChange(addAxiom);
                    }
                }else{
                    OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")));
                    OWLDataPropertyAssertionAxiom owlDataPropertyAssertionAxiom = null;
                    if(annotationAssertionAxiom.getValue().toString().contains("string")){
                        int i = 1;
                        owlDataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(
                                prop, ind1,i);
                    }else if(annotationAssertionAxiom.getValue().toString().contains("int") || annotationAssertionAxiom.getValue().toString().contains("float")){
                        owlDataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(
                                prop, ind1,"value");
                    }else{
                        float i = (float)1.0;
                        owlDataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(
                                prop, ind1,i);
                    }

                    if(!ontology.getOntology().getAxioms().contains(axiom)) {
                        addAxiom = new AddAxiom(ontology.getOntology(), owlDataPropertyAssertionAxiom);
                        ontology.getManager().applyChange(addAxiom);
                    }
                }

            }else {
                if(!ontology.getOntology().getAxioms().contains(axiom)){
                    addAxiom = new AddAxiom(ontology.getOntology(), axiom);
                    ontology.getManager().applyChange(addAxiom);
                }
            }

        }
        return ontology;
    }

    /*Method to add changes to the ontology */
    public  String executeReasoner(Set<OWLAxiom> axioms, Ontology ontology) throws OWLOntologyStorageException, OWLOntologyCreationException {

        PelletReasoner reasoner = null;
        Configuration configuration = new Configuration();
        configuration.throwInconsistentOntologyException = false;
        configuration.ignoreUnsupportedDatatypes = true;
        reasoner = com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory.getInstance().createReasoner(ontology.getOntology(), configuration);
        reasoner.precomputeInferences();
        Set<OWLClass> classesintest =  new HashSet();
        for (OWLAxiom axiom : axioms) {
            classesintest.addAll(axiom.getClassesInSignature());
        }
        String result = "";
        if(!reasoner.isConsistent()) {
            result = "inconsistent";
        }else if(reasoner.getUnsatisfiableClasses().getSize()>1) {
            int flag = 0;
            // check if the unsatisfiable classes are because of the test
            for(OWLClass classintest: classesintest){
                if(reasoner.getUnsatisfiableClasses().contains(classintest)){
                    result="unsatisfiable";
                    flag++;
                }
            }
            if(flag==0)
                result = "consistent";
        }else {
            result = "consistent";
        }

        return result;
    }

    /*This method executeTest the ABox and Tbox tests. The errors are printed in a report*/
    public TestCaseResult executeTest(TestCaseImpl tc, Ontology ontology, HashMap<String, IRI> got) throws OWLOntologyStorageException, OWLOntologyCreationException {
        ArrayList<String> undefinedTerms = new ArrayList<String>();
        ArrayList<String> incorrectTerms = new ArrayList<String>();
        String realResult;
        TestCaseResult tr = new TestCaseResult();
        ArrayList<String> resultsforabsence= new ArrayList<>();
        tr.setRelatedTestImpl(tc.getUri());
        tr.setOntologyURI(ontology.getOntology().getOntologyID().getOntologyIRI());
        int absent = 0;
        if(tc.getPreconditionQuery()!=null){ //map the test terms with ontology terms and add the axioms to the ontology. If the addition results in a consistent ontology the preconditions are passed
            for(String prec: tc.getPreconditionQuery()) { // execute precondition query
                String precondWithURI = Utils.mapImplementationTerms(prec, got); // all  mappings received by the webapp
                realResult = tboxTest(precondWithURI, tc.getUri(), ontology.getManager(), ontology.getOntology());

                if(realResult.equals("false")){ // si los  terminos no existen hay dos opciones: 1) que no exista, 2) que sea de otro tipo
                    if(Utils.termsInOntology(precondWithURI, ontology.getOntology()).equals("false")){
                        undefinedTerms.add( Utils.getPrecTerm(prec).replace(">","").replace("<",""));
                    }else{
                        incorrectTerms.add( Utils.getPrecTerm(prec).replace(">","").replace("<",""));
                    }
                }

            }
            tr.setUndefinedTerms(undefinedTerms);
            tr.setIncorrectTerms(incorrectTerms);
            if (undefinedTerms.size()==0 &&  incorrectTerms.size()==0 ) {
                if (tc.getPreparationaxioms() != null) {
                    // test preparation
                    Set<OWLAxiom> prepWithURI = Utils.mapImplementationTerms(tc.getPreparationaxioms(), got);
                    realResult = aboxTest(prepWithURI, ontology, "preparation");
                    if(realResult.equals("error")){
                        return null;
                    }
                    else if (!realResult.toLowerCase().contains("consistent")) {
                        resultsforabsence.add("inconsistent");
                        tr.setTestResult("not passed");
                        removePreparationAxioms(prepWithURI, ontology);
                    } else {
                        //add assertions to the ontology, after mapping the test terms with ontology terms. Check if the real result is the same as the expected result
                        for (Map.Entry<String, OWLOntology> entry : tc.getAssertionsAxioms().entrySet()) {
                            Set<OWLAxiom> assertionWithURI = Utils.mapImplementationTerms(entry.getValue(), got);
                            realResult = aboxTest(assertionWithURI, ontology, "assertion");
                            if(realResult.toLowerCase().equals("consistent")){
                                resultsforabsence.add("consistent");
                            }else{
                                resultsforabsence.add("inconsistent");
                            }
                            if(tc.getType().equals("existential") && entry.getKey().equals("Assertion 2") && realResult.equals("inconsistent")) {
                                absent=1; // caso excepcional
                            }else if (!realResult.toLowerCase().equals("consistent") && !realResult.toLowerCase().equals(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))) {
                                tr.setTestResult("not passed");
                            }else if(realResult.toLowerCase().equals("consistent") && !realResult.toLowerCase().equals(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))){
                                absent=1;
                            }
                        }

                        removePreparationAxioms(prepWithURI, ontology);
                    }
                }
            } else if(undefinedTerms.size()> 0) {
                resultsforabsence.add("undefined");
                tr.setTestResult("undefined");
            }else{
                tr.setTestResult("incorrect");
            }
        }else{
            resultsforabsence.add("undefined");
            tr.setTestResult("undefined");
        }

        tr = checkAbsence ( resultsforabsence,  absent,  tr );
        return tr;
    }

    // check if the result is an absence
    public TestCaseResult checkAbsence ( ArrayList<String> resultsforabsence, int absent, TestCaseResult tr ){
        int flag = 0;
        for(String result: resultsforabsence){
            if(!result.equals("consistent")){
                flag++;
            }
        }
        if(flag==0 && resultsforabsence.size()>0 || !tr.getTestResult().equals("not passed") && absent == 1 ){
            tr.setTestResult("absent");
        }

        if(tr.getTestResult()=="")
            tr.setTestResult("passed");

        return tr;

    }

    /*Method to remove the preparation axioms after each test case has been executed*/
    public void removePreparationAxioms(Set<OWLAxiom> textAxioms, Ontology ontology){
        OWLDataFactory dataFactory = ontology.getManager().getOWLDataFactory();
        Configuration configuration = new Configuration();
        configuration.throwInconsistentOntologyException = false;
        configuration.ignoreUnsupportedDatatypes = true;

        for (OWLAxiom axiom : textAxioms) {
            if (axiom.isAnnotationAxiom()) {
                OWLAnnotationAssertionAxiom annotationAssertionAxiom = (OWLAnnotationAssertionAxiom) axiom;
                OWLNamedIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getSubject().toString()));
                OWLNamedIndividual ind2 = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getValue().toString()));
                OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">", "").replace("<", "")));
                OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(
                        prop, ind1, ind2);
                if (ontology.getOntology().getAxioms().contains(owlObjectPropertyAssertionAxiom)) {
                    ontology.getManager().removeAxiom(ontology.getOntology(),owlObjectPropertyAssertionAxiom);
                }

            }else{
                if (ontology.getOntology().getAxioms().contains(axiom)) {
                    ontology.getManager().removeAxiom(ontology.getOntology(),axiom);
                }
            }
        }
    }








}