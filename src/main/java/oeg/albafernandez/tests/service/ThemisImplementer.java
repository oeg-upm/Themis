package oeg.albafernandez.tests.service;


import oeg.albafernandez.tests.model.TestCaseDesign;
import oeg.albafernandez.tests.model.TestCaseImpl;
import org.apache.log4j.Logger;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


import static oeg.albafernandez.tests.utils.Implementations.*;

/*This class implements the test case based on the test design. */
public class ThemisImplementer {

    static final Logger logger = Logger.getLogger(ThemisImplementer.class);

    private TestCaseDesign tc;
    private TestCaseImpl testCase;

    public TestCaseDesign getTc() {
        return tc;
    }

    public void setTc(TestCaseDesign tc) {
        this.tc = tc;
    }

    public TestCaseImpl getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCaseImpl testCase) {
        this.testCase = testCase;
    }

    /*Generate a TestCaseDesign object from the purpose given by the users*/
    public  void processTestCaseDesign(String purpose)  {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        String base = "http://www.semanticweb.org/untitled-ontology-53#";
        String verif = "http://w3id.org/def/vtc#";
        OWLOntology ont = null;
        try {
            ont = manager.createOntology(IRI.create(base));
        } catch (OWLOntologyCreationException e) {
            ont = null;
        }
        tc = new TestCaseDesign();
        tc.setPurpose(purpose);  // create a test design only with the purpose (test expression)

        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        OWLClass verifTestDesignClass = dataFactory.getOWLClass(IRI.create(verif + "TestCaseDesign"));

        OWLIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(base + "testDesign"));
        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(verifTestDesignClass, subject);
        manager.addAxiom(ont, classAssertion);

        OWLDataProperty desiredBehaviour = dataFactory.getOWLDataProperty(IRI.create(verif + "desiredBehaviour"));
        OWLAxiom axiomprecondition = dataFactory.getOWLDataPropertyAssertionAxiom(desiredBehaviour, subject, tc.getPurpose());
        manager.addAxiom(ont, axiomprecondition);

    }

    /*Load a set of tests provided in a file */
    public List<String> loadTestCaseDesign(String filename, String filecontent) throws IOException, OWLOntologyCreationException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology=null;
        if(!filename.equals("")) {
            ontology = manager.loadOntology(IRI.create(filename.replace("\"", "")));
        }
        if(!filecontent.equals("")){
            System.out.println(filecontent);
            OWLOntologyDocumentSource docs = new StringDocumentSource(filecontent);
            ontology = manager.loadOntologyFromOntologyDocument(docs);

        }
        ArrayList<String> testsuite = new ArrayList<>();
        for (OWLIndividual cls: ontology.getIndividualsInSignature()) {
            String purpose = "";
            String source = "";
            String description = "";
            tc = new TestCaseDesign();
            tc.setUri(IRI.create(cls.toString().replace("<","").replace(">","")));
            for (OWLAnnotationAssertionAxiom op : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
                if (cls.toString().replace(">","").replace("<","").equals(op.getSubject().toString().replace("<","").replace(">",""))) {
                    if (op.getProperty().toString().contains("http://w3id.org/def/vtc#desiredBehaviour")) {
                        purpose = op.getValue().toString().replace("\"","");
                        tc.setPurpose(purpose);
                    } else if (op.getProperty().toString().contains("http://purl.org/dc/terms/identifier")) {
                        source = op.getValue().toString().replace("\"","");
                        tc.setSource(source);
                    } else if (op.getProperty().toString().contains("http://purl.org/dc/terms/description")) {
                        description = op.getValue().toString().replace("\"","");
                        tc.setDescription(description);
                    }
                }
            }
            for (OWLDataPropertyAssertionAxiom dp : ontology.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION)) {
                if (cls.toString().replace(">","").replace("<","").equals(dp.getSubject().toString().replace("<","").replace(">",""))) {
                    if (dp.getProperty().toString().contains("http://w3id.org/def/vtc#desiredBehaviour")) {
                        purpose = dp.getObject().toString().replace("\"","");
                        tc.setPurpose(purpose);
                    } else if (dp.getProperty().toString().contains("http://purl.org/dc/terms/identifier")) {
                        source = dp.getObject().toString().replace("\"","");
                        tc.setSource(source);
                    } else if (dp.getProperty().toString().contains("http://purl.org/dc/terms/description")) {
                        description = dp.getObject().toString().replace("\"","");
                        tc.setDescription(description);
                    }
                }
            }
            testsuite.add(tc.getPurpose());
        }
        return testsuite;
    }

    /*Store the  test design*/
    public static OutputStream storeTestCaseDesign(List<String> tests, OutputStream outputStream) throws OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        String base = "";
        String verif = "http://w3id.org/def/vtc#";
        OWLOntology ont = null;
        try {
            ont = manager.createOntology(IRI.create(base));
        } catch (OWLOntologyCreationException e) {
            ont = null;
        }
        int i=1;
        for(String purpose:tests) {

            TestCaseDesign tc = new TestCaseDesign();
            tc.setPurpose(purpose);  // create a test design only with the purpose (test expression)

            OWLDataFactory dataFactory = manager.getOWLDataFactory();

            OWLClass verifTestDesignClass = dataFactory.getOWLClass(IRI.create(verif + "TestCaseDesign"));

            OWLIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(base + "Test"+i));
            OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(verifTestDesignClass, subject);
            manager.addAxiom(ont, classAssertion);

            OWLDataProperty desiredBehaviour = dataFactory.getOWLDataProperty(IRI.create(verif + "desiredBehaviour"));
            OWLAxiom axiomprecondition = dataFactory.getOWLDataPropertyAssertionAxiom(desiredBehaviour, subject, tc.getPurpose());
            manager.addAxiom(ont, axiomprecondition);
            i++;
        }

        TurtleOntologyFormat turtleFormat = new TurtleOntologyFormat();
        turtleFormat.setDefaultPrefix(base);
        manager.saveOntology(ont, turtleFormat, outputStream);
        return outputStream;
    }

    //select the type of test to be implemented
    public void mapExpressionTemplates(String purpose) throws OWLOntologyCreationException {
        String purposecloned= purpose.toLowerCase().replace("  "," ");

        if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ only [^\\s]+ or [^\\s]+")){
            this.setTestCase(unionTest(purpose.replace(","," "),"union", testCase));
        }else if(purposecloned.matches("[^\\s]+ domain [^\\s]+")){
            this.setTestCase(domainTest(purpose.replace(","," "), testCase));
        }else if(purposecloned.matches("[^\\s]+ range (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            this.setTestCase(rangeTestDP(purpose.replace(","," "), testCase));
        }else if(purposecloned.matches("[^\\s]+ range (rdfs:literal|literal)")){
            this.setTestCase(rangeTestDPLiteral(purpose.replace(","," "), testCase));
        }else if(purposecloned.matches("[^\\s]+ range [^\\s]+")){
            this.setTestCase(rangeTest(purpose.replace(","," "), testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ some [^\\s]+ and [^\\s]+")){
            this.setTestCase(intersectionTest(purpose.replace(","," "),"union", testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ value [^\\s]+")){
            this.setTestCase(individualValue(purpose.replace(","," "),testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ and [^\\s]+ subclassof [^\\s]+ that disjointwith [^\\s]+")){
            this.setTestCase(subclassDisjointTest(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ equivalentto [^\\s]+")){
            this.setTestCase(subClassTest(purpose, "equivalence",testCase));
        }else if(purposecloned.matches("[^\\s]+ disjointwith [^\\s]+")){
            this.setTestCase( subClassTest(purpose, "disjoint",testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ min (\\d+) [^\\s]+ and [^\\s]+ subclassof [^\\s]+ some [^\\s]+")){
            this.setTestCase(cardinalityOPTest(purpose,"min",testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ (max|min|exactly) (\\d+) \\([^\\s]+ and [^\\s]+\\)")){
            this.setTestCase(intersectionCardTest(purpose,"max",testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ that [^\\s]+ some [^\\s]+")){
            this.setTestCase(subClassOPTest(purpose,testCase));
        }else if(purposecloned.matches("[^\\s]+ characteristic symmetricproperty")){
            this.setTestCase(symmetryTest(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof symmetricproperty\\([^\\s]+\\) (some|only) [^\\s]+") || purposecloned.matches("[^\\s]+ subclassOf <coparticipatesWith> (some|only) [^\\s]+")){
            this.setTestCase(symmetryWithDomainRangeTest(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) some [^\\s]+")){
            this.setTestCase(participantODPTestExistential(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) only [^\\s]+")){
            this.setTestCase(participantODPTestUniversal(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof (hascoparticipant|iscoparticipantin|cooparticipates) some [^\\s]+")){
            this.setTestCase(coParticipantODPTestExistential(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ and [^\\s]+ subclassof (hascoparticipant|iscoparticipantin|cooparticipates) only [^\\s]+")){
            this.setTestCase(coParticipantODPTestUniversal(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ some (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            this.setTestCase(existentialRangeDP(purpose.replace("\\","\\\""),testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ only (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            this.setTestCase(universalRangeDP(purpose.replace("\\","\\\""),testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) some [^\\s]+")){
            this.setTestCase(existentialRange(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) only [^\\s]+")){
            this.setTestCase(universalRange(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ min (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            this.setTestCase(cardinalityDP(purpose, "min",testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ max (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            this.setTestCase(cardinalityDP(purpose, "max",testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ exactly (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp)")){
            this.setTestCase(cardinalityDP(purpose, "exactly",testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ min (\\d+) [^\\s]+")){
            this.setTestCase(cardinality(purpose, "min",testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ max (\\d+) [^\\s]+")){
            this.setTestCase(cardinality(purpose, "max",testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ exactly (\\d+) [^\\s]+")){
            this.setTestCase(cardinality(purpose, "exactly",testCase));
        }else if(purposecloned.matches("[^\\s]+ type class")) {
            this.setTestCase(classDefinitionTest(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ type property")) {
            this.setTestCase(propertyDefinitionTest(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ type [^\\s]+")){
            this.setTestCase(typeTest(purpose, testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof (ispartof|partof) some [^\\s]+")){
            this.setTestCase(partWholeTestExistential(purpose,testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof (ispartof|partof) only [^\\s]+")){
            this.setTestCase(partWholeTestUniversal(purpose,testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+ and [^\\s]+")){
            this.setTestCase(multipleSubClassTest(purpose,testCase));
        }else if(purposecloned.matches("[^\\s]+ subclassof [^\\s]+")){
            this.setTestCase(subClassTest(purpose, "strict subclass",testCase));
        }else if(purposecloned.matches("[^\\s]+ [^\\s]+ (xsd:string|xsd:float|xsd:integer|rdfs:literal|xsd:datetime|xsd:datetimestamp|string|float|integer|datetime|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri)")){
            this.setTestCase(domainRangeTestDP(purpose, testCase));
        } else if(purposecloned.matches("[^\\s]+ [^\\s]+ [^\\s]+")){
            this.setTestCase(domainRangeTest(purpose, testCase));
        }
        else{
            logger.error("NOT MATCH FOUND IN " +purpose+": ");
        }
    }

    //create the RDF of the implementation, which will be executed on the ontology
    public TestCaseImpl  createTestImplementation() {

        this.setTestCase(new TestCaseImpl());
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        String base = "http://www.semanticweb.org/untitled-ontology-53#";
        String verif = "http://w3id.org/def/vtc#";
        OWLOntology ont = null;
        try {
            ont = manager.createOntology(IRI.create(base));
        } catch (OWLOntologyCreationException e) {
            ont = null;
        }
        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        OWLClass verifTestImplClass = dataFactory.getOWLClass(IRI.create(verif + "TestCaseImplementation"));
        OWLClass verifTestPrepClass = dataFactory.getOWLClass(IRI.create(verif + "TestPreparation"));
        OWLClass verifTestAssertionClass = dataFactory.getOWLClass(IRI.create(verif + "TestAssertion"));

        /*Create individual of type testimplementation*/
        OWLIndividual subject = dataFactory.getOWLNamedIndividual(IRI.create(base + "testImplementation"));
        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(verifTestImplClass, subject);
        manager.addAxiom(ont, classAssertion);
        /*Add related test design*/
        OWLObjectProperty relatedToDesign = dataFactory.getOWLObjectProperty(IRI.create(verif + "relatedToDesign"));
        OWLIndividual design = dataFactory.getOWLNamedIndividual(IRI.create(base));
        OWLAxiom relatedToDesignAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(relatedToDesign, subject, design);
        manager.addAxiom(ont, relatedToDesignAssertion);
        /*Add precondition*/
        try {
            mapExpressionTemplates(tc.getPurpose());
        } catch (OWLOntologyCreationException e) {
            logger.error(e.getMessage());
        }
        ArrayList<String> precondarray=new ArrayList<>();
        for(String query: this.testCase.getPrecondition()){
            StringBuilder bld = new StringBuilder();
            bld.append( "ASK{");
            bld.append(query);
            bld.append("}");
            precondarray.add(bld.toString());
        }

        this.testCase.setPreconditionQuery(precondarray);

        OWLDataProperty preconditionProp = dataFactory.getOWLDataProperty(IRI.create(verif + "precondition"));
        for(String prec: this.testCase.getPrecondition()) {
            OWLAxiom axiomprecondition = dataFactory.getOWLDataPropertyAssertionAxiom(preconditionProp, subject, prec);
            manager.addAxiom(ont, axiomprecondition);
        }

        /*Add test preparation*/
        OWLObjectProperty preparationProperty = dataFactory.getOWLObjectProperty(IRI.create(verif + "hasPreparation"));
        OWLIndividual preparation = dataFactory.getOWLNamedIndividual(IRI.create(base + "preparation1"));
        OWLAxiom preparationAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(preparationProperty, subject, preparation);
        manager.addAxiom(ont, preparationAssertion);
        /*Add assertions*/
        if (this.testCase.getAssertions().size() > 1) {
            OWLObjectProperty assertionProperty = dataFactory.getOWLObjectProperty(IRI.create(verif + "hasAssertion"));
            for (int j = 1; j <= this.testCase.getAssertions().size(); j++) {
                OWLIndividual assertion = dataFactory.getOWLNamedIndividual(IRI.create(base + "assertion" + j));
                OWLAxiom assertionAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(assertionProperty, subject, assertion);
                manager.addAxiom(ont, assertionAssertion);
            }
        }

        /*Create preparation individual*/
        OWLIndividual preparation1 = dataFactory.getOWLNamedIndividual(IRI.create(base + "preparation1"));
        OWLClassAssertionAxiom classAssertion2 = dataFactory.getOWLClassAssertionAxiom(verifTestPrepClass, preparation1);
        manager.addAxiom(ont, classAssertion2);
        /*Add description*/
        OWLDataProperty descrProp = dataFactory.getOWLDataProperty(IRI.create("http://purl.org/dc/terms/description"));
        OWLAxiom descrAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(descrProp, preparation1, "");
        manager.addAxiom(ont, descrAssertion);
        /*add preparation*/
        OWLDataProperty axiomsProp = dataFactory.getOWLDataProperty(IRI.create(verif + "testAxioms"));
        OWLAxiom axiomsAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(axiomsProp, preparation1, this.testCase.getPreparation());
        manager.addAxiom(ont, axiomsAssertion);

        /*Create assertion individual*/
        int i = 1;
        for (Map.Entry<String, String> entry : this.testCase.getAssertions().entrySet()) {
            OWLIndividual assertion1 = dataFactory.getOWLNamedIndividual(IRI.create(base + "assertion" + i));
            OWLClassAssertionAxiom classAssertion3 = dataFactory.getOWLClassAssertionAxiom(verifTestAssertionClass, assertion1);
            manager.addAxiom(ont, classAssertion3);
            /*add description*/
            OWLAxiom descrAssertion2 = dataFactory.getOWLDataPropertyAssertionAxiom(descrProp, assertion1, "");
            manager.addAxiom(ont, descrAssertion2);
            /*add test axioms*/
            OWLAxiom axiomsAssertion2 = dataFactory.getOWLDataPropertyAssertionAxiom(axiomsProp, assertion1, entry.getKey());
            manager.addAxiom(ont, axiomsAssertion2);
            OWLIndividual assresult = null;
            if (this.testCase.getAxiomExpectedResult().get(entry.getKey()).equals("unsatisfiable"))
                assresult = dataFactory.getOWLNamedIndividual(IRI.create(verif + "Unsatisfiable"));
            else if (this.testCase.getAxiomExpectedResult().get(entry.getKey()).equals("inconsistent"))
                assresult = dataFactory.getOWLNamedIndividual(IRI.create(verif + "Inconsistent"));
            else
                assresult = dataFactory.getOWLNamedIndividual(IRI.create(verif + "Consistent"));

            OWLObjectProperty assertionResultProperty = dataFactory.getOWLObjectProperty(IRI.create(verif + "hasAssertionResult"));
            OWLAxiom assertionResultAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(assertionResultProperty, assertion1, assresult);
            manager.addAxiom(ont, assertionResultAssertion);
            i++;
        }
        return this.testCase;
    }


}