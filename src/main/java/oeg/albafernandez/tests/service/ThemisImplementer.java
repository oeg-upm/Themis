package oeg.albafernandez.tests.service;


import oeg.albafernandez.tests.model.TestCaseDesign;
import oeg.albafernandez.tests.model.TestCaseImpl;
import org.apache.log4j.Logger;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import java.io.OutputStream;
import java.util.*;


import static oeg.albafernandez.tests.utils.Implementations.*;

/*This class implements the test case based on the test design. */
public class ThemisImplementer {

    static final Logger logger = Logger.getLogger(ThemisImplementer.class);
    static final String VERIF =  "http://w3id.org/def/vtc#";
    static final String DESCRIPTION = "http://purl.org/dc/terms/description";

    private TestCaseDesign testCaseDesign;
    private TestCaseImpl testCaseImpl;

    public TestCaseDesign getTestCaseDesign() {
        return testCaseDesign;
    }

    public void setTestCaseDesign(TestCaseDesign testCaseDesign) {
        this.testCaseDesign = testCaseDesign;
    }

    public TestCaseImpl getTestCaseImpl() {
        return testCaseImpl;
    }

    public void setTestCaseImpl(TestCaseImpl testCaseImpl) {
        this.testCaseImpl = testCaseImpl;
    }

    /*Generate a TestCaseDesign object from the purpose given by the users*/
    public  void processTestCaseDesign(String purpose)  {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        String base = "http://www.semanticweb.org/untitled-ontology-53#";
        OWLOntology ontology = null;
        try {
            ontology = manager.createOntology(IRI.create(base));
        } catch (OWLOntologyCreationException e) {
            ontology = null;
        }
        testCaseDesign = new TestCaseDesign();
        testCaseDesign.setPurpose(purpose);  // create a test design only with the purpose (test expression)

        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        OWLClass verifTestDesignClass = dataFactory.getOWLClass(IRI.create(VERIF + "TestCaseDesign"));

        OWLIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(base + "testDesign"));
        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(verifTestDesignClass, subject);
        manager.addAxiom(ontology, classAssertion);

        OWLDataProperty desiredBehaviour = dataFactory.getOWLDataProperty(IRI.create(VERIF + "desiredBehaviour"));
        OWLAxiom axiomPrecondition = dataFactory.getOWLDataPropertyAssertionAxiom(desiredBehaviour, subject, testCaseDesign.getPurpose());
        manager.addAxiom(ontology, axiomPrecondition);

    }

    /*Load a set of tests provided in a file */
    public List<String> loadTestCaseDesign(String filename) throws  OWLOntologyCreationException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology= manager.loadOntology(IRI.create(filename.replace("\"","")));
        ArrayList<String> testSuite = new ArrayList<>();
        for (OWLIndividual cls: ontology.getIndividualsInSignature()) {
            testCaseDesign = new TestCaseDesign();
            testCaseDesign.setUri(IRI.create(cls.toString().replace("<","").replace(">","")));
            for (OWLAnnotationAssertionAxiom op : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
                loadAnnotationInTestCaseDesign(op , cls);
            }
            for (OWLDataPropertyAssertionAxiom dp : ontology.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION)) {
                loadDatatypePropertyInTestCaseDesign( dp ,  cls);
            }
            testSuite.add(testCaseDesign.getPurpose());
        }
        return testSuite;
    }

    public void loadAnnotationInTestCaseDesign(OWLAnnotationAssertionAxiom op , OWLIndividual cls){
        if (cls.toString().replace(">","").replace("<","").equals(op.getSubject().toString().replace("<","").replace(">",""))) {
            if (op.getProperty().toString().contains("http://w3id.org/def/vtc#desiredBehaviour")) {
                String purpose = op.getValue().toString().replace("\"","");
                testCaseDesign.setPurpose(purpose);
            } else if (op.getProperty().toString().contains("http://purl.org/dc/terms/identifier")) {
                String source = op.getValue().toString().replace("\"","");
                testCaseDesign.setSource(source);
            } else if (op.getProperty().toString().contains(DESCRIPTION)) {
                String description = op.getValue().toString().replace("\"","");
                testCaseDesign.setDescription(description);
            }
        }
    }

    public void loadDatatypePropertyInTestCaseDesign(OWLDataPropertyAssertionAxiom dp , OWLIndividual cls){
        if (cls.toString().replace(">","").replace("<","").equals(dp.getSubject().toString().replace("<","").replace(">",""))) {
            if (dp.getProperty().toString().contains("http://w3id.org/def/vtc#desiredBehaviour")) {
                String purpose = dp.getObject().toString().replace("\"","");
                testCaseDesign.setPurpose(purpose);
            } else if (dp.getProperty().toString().contains("http://purl.org/dc/terms/identifier")) {
                String source = dp.getObject().toString().replace("\"","");
                testCaseDesign.setSource(source);
            } else if (dp.getProperty().toString().contains(DESCRIPTION)) {
                String description = dp.getObject().toString().replace("\"","");
                testCaseDesign.setDescription(description);
            }
        }
    }

    /*Store the  test design*/
    public static OutputStream storeTestCaseDesign(List<String> tests, OutputStream outputStream) throws OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        String base = "";
        OWLOntology ontology = null;
        try {
            ontology = manager.createOntology(IRI.create(base));
        } catch (OWLOntologyCreationException e) {
            ontology = null;
        }
        int i=1;

        for(String purpose:tests) {

            TestCaseDesign tc = new TestCaseDesign();
            tc.setPurpose(purpose);  // create a test design only with the purpose (test expression)

            OWLDataFactory dataFactory = manager.getOWLDataFactory();

            OWLClass verifTestDesignClass = dataFactory.getOWLClass(IRI.create(VERIF + "TestCaseDesign"));

            OWLIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(base + "Test"+i));
            OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(verifTestDesignClass, subject);
            manager.addAxiom(ontology, classAssertion);

            OWLDataProperty desiredBehaviour = dataFactory.getOWLDataProperty(IRI.create(VERIF + "desiredBehaviour"));
            OWLAxiom axiomPrecondition = dataFactory.getOWLDataPropertyAssertionAxiom(desiredBehaviour, subject, tc.getPurpose());
            manager.addAxiom(ontology, axiomPrecondition);
            i++;
        }

        TurtleOntologyFormat turtleFormat = new TurtleOntologyFormat();
        turtleFormat.setDefaultPrefix(base);
        manager.saveOntology(ontology, turtleFormat, outputStream);
        return outputStream;
    }

    //select the type of test to be implemented
    public void mapExpressionTemplates(String purpose) throws OWLOntologyCreationException {
        purpose= purpose.replace("  "," ");

        if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ only [^\\s]+ or [^\\s]+")){
            this.setTestCaseImpl(unionTest(purpose.replace(","," "),"union", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ domain [^\\s]+")){
            this.setTestCaseImpl(domainTest(purpose.replace(","," "), testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ range (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri)")){
            this.setTestCaseImpl(rangeTestDP(purpose.replace(","," "), testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ range [^\\s]+")){
            this.setTestCaseImpl(rangeTest(purpose.replace(","," "), testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ some [^\\s]+ and [^\\s]+")){
            this.setTestCaseImpl(intersectionTest(purpose.replace(","," "),"union", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ value [^\\s]+")){
            this.setTestCaseImpl(individualValue(purpose.replace(","," "), testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ and [^\\s]+ subclassof [^\\s]+ that disjointwith [^\\s]+")){
            this.setTestCaseImpl(subclassDisjointTest(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ equivalentto [^\\s]+")){
            this.setTestCaseImpl(subClassTest(purpose, "equivalence", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ disjointwith [^\\s]+")){
            this.setTestCaseImpl( subClassTest(purpose, "disjoint", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ min (\\d+) [^\\s]+ and [^\\s]+ subclassof [^\\s]+ some [^\\s]+")){
            this.setTestCaseImpl(cardinalityOPTest(purpose,"min", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ (max|min|exactly) (\\d+) \\([^\\s]+ and [^\\s]+\\)")){
            this.setTestCaseImpl(intersectionCardTest(purpose,"max", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ that [^\\s]+ some [^\\s]+")){
            this.setTestCaseImpl(subClassOPTest(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ characteristic symmetricproperty")){
            this.setTestCaseImpl(symmetryTest(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof symmetricproperty\\([^\\s]+\\) (some|only) [^\\s]+") || purpose.matches("[^\\s]+ subclassOf <coparticipatesWith> (some|only) [^\\s]+")){
            this.setTestCaseImpl(symmetryWithDomainRangeTest(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) some [^\\s]+")){
            this.setTestCaseImpl(participantODPTestExistential(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) only [^\\s]+")){
            this.setTestCaseImpl(participantODPTestUniversal(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof (hascoparticipant|iscoparticipantin|cooparticipates) some [^\\s]+")){
            this.setTestCaseImpl(coParticipantODPTestExistential(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ and [^\\s]+ subclassof (hascoparticipant|iscoparticipantin|cooparticipates) only [^\\s]+")){
            this.setTestCaseImpl(coParticipantODPTestUniversal(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ some (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri)")){
            this.setTestCaseImpl(existentialRangeDP(purpose.replace("\\","\\\""), testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ only (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri)")){
            this.setTestCaseImpl(universalRangeDP(purpose.replace("\\","\\\""), testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) some [^\\s]+")){
            this.setTestCaseImpl(existentialRange(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) only [^\\s]+")){
            this.setTestCaseImpl(universalRange(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ min (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri)")){
            this.setTestCaseImpl(cardinalityDP(purpose, "min", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ max (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri)")){
            this.setTestCaseImpl(cardinalityDP(purpose, "max", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ exactly (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri)")){
            this.setTestCaseImpl(cardinalityDP(purpose, "exactly", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ min (\\d+) [^\\s]+")){
            this.setTestCaseImpl(cardinality(purpose, "min", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ max (\\d+) [^\\s]+")){
            this.setTestCaseImpl(cardinality(purpose, "max", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ exactly (\\d+) [^\\s]+")){
            this.setTestCaseImpl(cardinality(purpose, "exactly", testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ type class")) {
            this.setTestCaseImpl(classDefinitionTest(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ type property")) {
            this.setTestCaseImpl(propertyDefinitionTest(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ type [^\\s]+")){
            this.setTestCaseImpl(typeTest(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof (ispartof|partof) some [^\\s]+")){
            this.setTestCaseImpl(partWholeTestExistential(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof (ispartof|partof) only [^\\s]+")){
            this.setTestCaseImpl(partWholeTestUniversal(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+ and [^\\s]+")){
            this.setTestCaseImpl(multipleSubClassTest(purpose, testCaseImpl));
        }else if(purpose.matches("(?i)[^\\s]+ subclassof [^\\s]+")){
            this.setTestCaseImpl(subClassTest(purpose, "strict subclass", testCaseImpl));
        }
        else{
            logger.error("NOT MATCH FOUND IN " +purpose+": ");
        }
    }

    //create the RDF of the implementation, which will be executed on the ontology
    public TestCaseImpl  createTestImplementation() {

        this.setTestCaseImpl(new TestCaseImpl());
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        String base = "http://www.semanticweb.org/untitled-ontology-53#";
        OWLOntology ontology = null;
        try {
            ontology = manager.createOntology(IRI.create(base));
        } catch (OWLOntologyCreationException e) {
            ontology = null;
        }
        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        OWLClass verifTestImplClass = dataFactory.getOWLClass(IRI.create(VERIF + "TestCaseImplementation"));
        OWLClass verifTestPrepClass = dataFactory.getOWLClass(IRI.create(VERIF + "TestPreparation"));
        OWLClass verifTestAssertionClass = dataFactory.getOWLClass(IRI.create(VERIF + "TestAssertion"));

        /*Create individual of type test implementation*/
        OWLIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(base + "testImplementation"));
        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(verifTestImplClass, subject);
        manager.addAxiom(ontology, classAssertion);
        /*Add related test design*/
        OWLObjectProperty relatedToDesign = dataFactory.getOWLObjectProperty(IRI.create(VERIF + "relatedToDesign"));
        OWLIndividual testDesign = dataFactory.getOWLNamedIndividual(IRI.create(base));
        OWLAxiom assertionToAddTestDesignRelated = dataFactory.getOWLObjectPropertyAssertionAxiom(relatedToDesign, subject, testDesign);
        manager.addAxiom(ontology, assertionToAddTestDesignRelated);
        /*Create content of the test*/
        /*Add precondition*/
        ontology =  addPrecondition(dataFactory, ontology, manager, subject);
        /*Add test preparation*/
        ontology = addPreparation(dataFactory, ontology, manager, base, verifTestPrepClass,  subject);
        /*Create assertion individual*/
        addAssertions(dataFactory, ontology, manager, base, verifTestAssertionClass);

        return this.testCaseImpl;
    }

    public OWLOntology addPrecondition(OWLDataFactory dataFactory, OWLOntology ontology, OWLOntologyManager manager, OWLIndividual subject ){
        try {
            mapExpressionTemplates(testCaseDesign.getPurpose());
        } catch (OWLOntologyCreationException e) {
            logger.error(e.getMessage());
        }
        ArrayList<String> precondarray=new ArrayList<>();
        StringBuilder bld = new StringBuilder();
        for(String query: this.testCaseImpl.getPreconditionList()){
            bld.append( "ASK{");
            bld.append(query);
            bld.append("}");
            precondarray.add(bld.toString());
        }

        this.testCaseImpl.setPreconditionQueryList(precondarray);

        OWLDataProperty preconditionProp = dataFactory.getOWLDataProperty(IRI.create(VERIF + "precondition"));
        for(String prec: this.testCaseImpl.getPreconditionList()) {
            OWLAxiom axiomprecondition = dataFactory.getOWLDataPropertyAssertionAxiom(preconditionProp, subject, prec);
            manager.addAxiom(ontology, axiomprecondition);
        }
        return ontology;

    }

    public OWLOntology addPreparation(OWLDataFactory dataFactory, OWLOntology ontology, OWLOntologyManager manager, String base, OWLClass verifTestPrepClass,  OWLIndividual subject){
        OWLObjectProperty preparationProperty = dataFactory.getOWLObjectProperty(IRI.create(VERIF + "hasPreparation"));
        OWLIndividual preparation = dataFactory.getOWLNamedIndividual(IRI.create(base + "preparation1"));
        OWLAxiom preparationAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(preparationProperty, subject, preparation);
        manager.addAxiom(ontology, preparationAssertion);
        /*Add assertions*/
        if (this.testCaseImpl.getAssertions().size() > 1) {
            OWLObjectProperty assertionProperty = dataFactory.getOWLObjectProperty(IRI.create(VERIF + "hasAssertion"));
            for (int j = 1; j <= this.testCaseImpl.getAssertions().size(); j++) {
                OWLIndividual assertion = dataFactory.getOWLNamedIndividual(IRI.create(base + "assertion" + j));
                OWLAxiom assertionAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(assertionProperty, subject, assertion);
                manager.addAxiom(ontology, assertionAssertion);
            }
        }

        /*Create preparation individual*/
        OWLIndividual preparation1 = dataFactory.getOWLNamedIndividual(IRI.create(base + "preparation1"));
        OWLClassAssertionAxiom classAssertion2 = dataFactory.getOWLClassAssertionAxiom(verifTestPrepClass, preparation1);
        manager.addAxiom(ontology, classAssertion2);
        /*Add description*/
        OWLDataProperty descriptionProperty = dataFactory.getOWLDataProperty(IRI.create(DESCRIPTION));
        OWLAxiom descriptionAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(descriptionProperty, preparation1, "");
        manager.addAxiom(ontology, descriptionAssertion);
        /*add preparation*/
        OWLDataProperty axiomsProp = dataFactory.getOWLDataProperty(IRI.create(VERIF + "testAxioms"));
        OWLAxiom axiomsAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(axiomsProp, preparation1, this.testCaseImpl.getPreparation());
        manager.addAxiom(ontology, axiomsAssertion);
        return ontology;
    }

    public OWLOntology addAssertions(OWLDataFactory dataFactory, OWLOntology ontology, OWLOntologyManager manager, String base, OWLClass verifTestAssertionClass ){
        int i = 1;
        for (Map.Entry<String, String> entry : this.testCaseImpl.getAssertions().entrySet()) {
            OWLIndividual assertion = dataFactory.getOWLNamedIndividual(IRI.create(base + "assertion" + i));
            OWLClassAssertionAxiom classAssertion3 = dataFactory.getOWLClassAssertionAxiom(verifTestAssertionClass, assertion);
            manager.addAxiom(ontology, classAssertion3);
            /*add description*/
            OWLDataProperty descriptionProperty = dataFactory.getOWLDataProperty(IRI.create(DESCRIPTION));
            OWLAxiom descriptionAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(descriptionProperty, assertion, "");
            manager.addAxiom(ontology, descriptionAssertion);
            /*add test axioms*/
            OWLDataProperty axiomsProp = dataFactory.getOWLDataProperty(IRI.create(VERIF + "testAxioms"));
            OWLAxiom axiomsAssertion2 = dataFactory.getOWLDataPropertyAssertionAxiom(axiomsProp, assertion, entry.getKey());
            manager.addAxiom(ontology, axiomsAssertion2);
            OWLIndividual assertionResult = null;
            if (this.testCaseImpl.getAxiomExpectedResult().get(entry.getKey()).equals("unsatisfiable"))
                assertionResult = dataFactory.getOWLNamedIndividual(IRI.create(VERIF + "Unsatisfiable"));
            else if (this.testCaseImpl.getAxiomExpectedResult().get(entry.getKey()).equals("inconsistent"))
                assertionResult = dataFactory.getOWLNamedIndividual(IRI.create(VERIF + "Inconsistent"));
            else
                assertionResult = dataFactory.getOWLNamedIndividual(IRI.create(VERIF + "Consistent"));

            OWLObjectProperty assertionResultProperty = dataFactory.getOWLObjectProperty(IRI.create(VERIF + "hasAssertionResult"));
            OWLAxiom assertionResultAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(assertionResultProperty, assertion, assertionResult);
            manager.addAxiom(ontology, assertionResultAssertion);
            i++;
        }
        return ontology;

    }

}