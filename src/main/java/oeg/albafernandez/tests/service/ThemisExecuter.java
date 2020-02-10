package oeg.albafernandez.tests.service;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import oeg.albafernandez.tests.utils.Utils;
import org.apache.log4j.Logger;
import org.json.Test;
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

    static final Logger logger = Logger.getLogger(ThemisExecuter.class);

    static final String NOT_PASSED = "not passed";
    static final String CONSISTENT = "consistent";
    static final String INCONSISTENT = "inconsistent";
    static final String FALSE = "false";

    /*Here we check the ontology consistency regarding at model-level*/
    public  String executeTboxTest(String query, IRI key, OWLOntologyManager manager, OWLOntology ontology) {

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
            logger.error("Check SPARQL-DL syntax: " + e.getMessage() + " ID: " + key);
        }

        QueryEngine engine = QueryEngine.create(manager, reasoner); // create the query engine
        QueryResult result;

        try {
            result = engine.execute(queryOWLAPI); // execute query
            // check result
            if (result.ask()) {
                return "true";
            } else {
                return FALSE;
            }
        } catch (Exception e) {
            logger.error("Error while executing test: " + e.getMessage() );
            return FALSE;
        }

    }

    /*Here we check the ontology consistency regarding at instance-level*/
    public  String executeAboxTest(Set<OWLAxiom> textAxioms, Ontology ontology, String type) {
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
    public  Ontology addChanges(Set<OWLAxiom> axioms, Ontology ontology)  {

        // We will create several things, so we save an instance of the data factory
        OWLDataFactory dataFactory = ontology.getManager().getOWLDataFactory();

        for (OWLAxiom axiom : axioms) {
            AddAxiom addAxiom = null;
            if(axiom.isAnnotationAxiom()){ //Sometimes the object properties are translated as annotated axioms incorrectly and they have to be changed.
                OWLAnnotationAssertionAxiom annotationAssertionAxiom = (OWLAnnotationAssertionAxiom)axiom;
                OWLNamedIndividual instanceSubject = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getSubject().toString()));
                OWLNamedIndividual instanceObject = dataFactory.getOWLNamedIndividual(IRI.create(annotationAssertionAxiom.getValue().toString()));
                IRI axiomAsAnnotationAssertion  = IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<",""));

                if(ontology.getOntology().containsObjectPropertyInSignature(axiomAsAnnotationAssertion, true)){
                    ontology = addAnnotationChange(dataFactory,   annotationAssertionAxiom,  instanceSubject,  instanceObject,  ontology);
                }else{
                    ontology = addDataPropertyChange(dataFactory,   annotationAssertionAxiom,  instanceSubject,  ontology);
                }
            }else {
                addAxiomToOntology( ontology,  axiom, addAxiom);
            }

        }
        return ontology;
    }

    public Ontology addAxiomToOntology(Ontology ontology, OWLAxiom axiom, AddAxiom addAxiom){
        if(!ontology.getOntology().getAxioms().contains(axiom)){
            addAxiom = new AddAxiom(ontology.getOntology(), axiom);
            ontology.getManager().applyChange(addAxiom);
        }
        return ontology;
    }

    public Ontology addAnnotationChange( OWLDataFactory dataFactory,  OWLAnnotationAssertionAxiom annotationAssertionAxiom, OWLNamedIndividual ind1, OWLNamedIndividual ind2, Ontology ontology){
        AddAxiom addAxiom = null;
        OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(annotationAssertionAxiom.getProperty().toString().replace(">","").replace("<","")));
        OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(
                prop, ind1,ind2);
        addAxiomToOntology( ontology,  owlObjectPropertyAssertionAxiom, addAxiom);
        return ontology;
    }

    public Ontology addDataPropertyChange (OWLDataFactory dataFactory, OWLAnnotationAssertionAxiom annotationAssertionAxiom, OWLNamedIndividual ind1,Ontology ontology){
        AddAxiom addAxiom = null;
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

        addAxiomToOntology( ontology,  owlDataPropertyAssertionAxiom, addAxiom);

        return ontology;
    }
    /*Method to add changes to the ontology */
    public  String executeReasoner(Set<OWLAxiom> axioms, Ontology ontology) {

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
            result = INCONSISTENT;
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
                result = CONSISTENT;
        }else {
            result = CONSISTENT;
        }

        return result;
    }

    /*This method executeTest the ABox and Tbox tests. The errors are printed in a report*/
    public TestCaseResult executeTest(TestCaseImpl tc, Ontology ontology, Map<String, IRI> got) throws OWLOntologyStorageException, OWLOntologyCreationException {

        String realResultForPrecondition;
        String realResultForPreparation;
        String realResultForAssertion;
        TestCaseResult tr = new TestCaseResult();
        ArrayList<String> resultsForAbsence= new ArrayList<>();
        tr.setRelatedTestImpl(tc.getUri());
        tr.setOntologyURI(ontology.getOntology().getOntologyID().getOntologyIRI());
        int absent = 0;
         //map the test terms with ontology terms and add the axioms to the ontology. If the addition results in a consistent ontology the preconditions are passed
        for(String prec: tc.getPreconditionQueryList()) { // execute precondition query
            String precondWithURI = Utils.mapImplementationTerms(prec, (HashMap<String, IRI>) got); // all  mappings received by the webapp
            realResultForPrecondition = executeTboxTest(precondWithURI, tc.getUri(), ontology.getManager(), ontology.getOntology());

            if(realResultForPrecondition.equals(FALSE)){ // si los  terminos no existen hay dos opciones: 1) que no exista, 2) que sea de otro tipo
               tr = checkPresenceOfTermsInTest(ontology, prec,  precondWithURI,  tr );
            }

            if (tr.getUndefinedTermsList().isEmpty() &&  tr.getIncorrectTermsList().isEmpty() ) {
                // test preparation
                Set<OWLAxiom> prepWithURI = Utils.mapImplementationTerms(tc.getPreparationAxioms(), (HashMap<String, IRI>) got);
                realResultForPreparation = executeAboxTest(prepWithURI, ontology, "preparation");
                if (!realResultForPreparation.equalsIgnoreCase(CONSISTENT)) {
                    resultsForAbsence.add(INCONSISTENT);
                    tr.setTestResult(NOT_PASSED);
                    removePreparationAxioms(prepWithURI, ontology);
                } else {
                    //add assertions to the ontology, after mapping the test terms with ontology terms. Check if the real result is the same as the expected result
                    for (Map.Entry<String, OWLOntology> entry : tc.getAssertionsAxioms().entrySet()) {
                        Set<OWLAxiom> assertionWithURI = Utils.mapImplementationTerms(entry.getValue(), (HashMap<String, IRI>) got);
                        realResultForAssertion = executeAboxTest(assertionWithURI, ontology, "assertion");
                        if(realResultForAssertion.equalsIgnoreCase(CONSISTENT)){
                            resultsForAbsence.add(CONSISTENT);
                        }else{
                            resultsForAbsence.add(INCONSISTENT);
                        }
                        if(tc.getType().equals("existential") && entry.getKey().equals("Assertion 2") && realResultForAssertion.equals(INCONSISTENT)) {
                            absent=1; // caso excepcional
                        }else if (!realResultForAssertion.equalsIgnoreCase(CONSISTENT) && !realResultForAssertion.equalsIgnoreCase(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))) {
                            tr.setTestResult(NOT_PASSED);
                        }else if(realResultForAssertion.equalsIgnoreCase(CONSISTENT) && !realResultForAssertion.equalsIgnoreCase(tc.getAxiomExpectedResultAxioms().get(entry.getKey()))){
                            absent=1;
                        }
                    }

                    removePreparationAxioms(prepWithURI, ontology);
                }

            } else if(!tr.getUndefinedTermsList().isEmpty()) {
                resultsForAbsence.add("undefined");
                tr.setTestResult("undefined");
            } else{
                tr.setTestResult("incorrect");
            }
        }

        tr = checkAbsence ( resultsForAbsence,  absent,  tr );
        return tr;
    }


    public TestCaseResult checkPresenceOfTermsInTest(Ontology ontology, String prec, String precondWithURI, TestCaseResult tr){
        ArrayList<String> undefinedTerms = new ArrayList<>();
        ArrayList<String> incorrectTerms = new ArrayList<>();
        if(Utils.termsInOntology(precondWithURI, ontology.getOntology()).equals(FALSE)){
            undefinedTerms.add( Utils.getPrecTerm(prec).replace(">","").replace("<",""));
        }else{
            incorrectTerms.add( Utils.getPrecTerm(prec).replace(">","").replace("<",""));
        }
        tr.setUndefinedTermsList(undefinedTerms);
        tr.setIncorrectTermsList(incorrectTerms);
        return tr;
    }

    // check if the result is an absence
    public TestCaseResult checkAbsence( List<String> resultsforabsence, int absent, TestCaseResult tr ){
        int flag = 0;
        for(String result: resultsforabsence){
            if(!result.equals(CONSISTENT)){
                flag++;
            }
        }
        if(flag==0 && !resultsforabsence.isEmpty() || !tr.getTestResult().equals(NOT_PASSED) && absent == 1 ){
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