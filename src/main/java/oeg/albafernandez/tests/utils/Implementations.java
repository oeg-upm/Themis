package oeg.albafernandez.tests.utils;

import com.clarkparsia.owlapiv3.XSD;
import oeg.albafernandez.tests.model.TestCaseDesign;
import oeg.albafernandez.tests.model.TestCaseImpl;
import org.apache.log4j.Logger;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Implementations {

   /*****The following functions implement each type of test implementation. They include the precondition, the
    * preparation and the assertion for each type of test*****/

   /*for the generation of an individual of a given class*/
   public static TestCaseImpl individualValue(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassOf (.*) value (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);
      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classA1 = classA+"1";

      String P = m.group(2).toString();
      String ind1 = m.group(3).toString();

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+P+">)");
      precond.add("Individual(<"+ind1+">)");
      testCase.getPrecondition().addAll(precond);

      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      /*Preparation*/
      OWLNamedIndividual individual1 = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLNamedIndividual individual2 = dataFactory.getOWLNamedIndividual(IRI.create(base+"individual2"));
      OWLClass class1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(class1);
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(individual1);
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(individual2);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont,  dataFactory.getOWLDifferentIndividualsAxiom(individual1, individual2)));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(class1, classOWLA);
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass1));
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(P));
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( class1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(individual2)));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass2));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "unsatisfiable";
      manager.removeOntology(ont1);


      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);


      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);
      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return null;
   }

   /*cardinality for DP*/
   public static TestCaseImpl cardinalityDP(String purpose, String type, TestCaseImpl testCase){
      Pattern p1 = Pattern.compile("(.*) subclassof (.*) (min|max|exactly) (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri)", Pattern.CASE_INSENSITIVE);
      Matcher m = p1.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();

      String classA1 = classA+"1";
      String R = m.group(2).toString();

      String datatype = "";
      datatype = m.group(5).toString();
      if( datatype.equals("string") || datatype.equals("integer") ||  datatype.equals("float") || datatype.equals("boolean") || datatype.equals("anyuri"))
         datatype = "<http://www.w3.org/2001/XMLSchema#"+datatype+">";
      else if(datatype.equals("rational")){
         datatype = "<http://www.w3.org/2002/07/owl#"+datatype+">";
      }

      Integer num = Integer.parseInt(m.group(4).toString());
      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/

      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      manager.addAxiom(ont,axiomClass);
      manager.applyChanges(manager.addAxiom(ont, axiomsubclass1));
      testCase.setPreparation(manager.getOntology(IRI.create(base)).getAxioms().toString());
      manager.removeOntology(ont);
      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(R));
      OWLDatatype dp = dataFactory.getOWLDatatype(IRI.create(datatype));
      OWLSubClassOfAxiom axiomsubclass4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLDataMaxCardinality(num-1, prop, dp));
      OWLDeclarationAxiom axiomdeclaration1 = dataFactory.getOWLDeclarationAxiom(dp);
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass4));
      manager.applyChanges(manager.addAxiom(ont1, axiomdeclaration1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "";
      if (type == "min" || type == "exactly")
         expectedoutputassertion1 = "unsatisfiable";
      else
         expectedoutputassertion1 = "consistent";

      manager.removeOntology(ont1);
      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLSubClassOfAxiom axiomsubclass5 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLDataMinCardinality(num+1, prop, dp));
      manager.applyChanges(manager.addAxiom(ont2, axiomdeclaration1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass5));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      String expectedoutputassertion2 = "";
      if (type == "max" || type == "exactly")
         expectedoutputassertion2 = "unsatisfiable";
      else
         expectedoutputassertion2 = "consistent";

      manager.removeOntology(ont2);

      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      OWLSubClassOfAxiom axiomsubclass6 = null;
      //String assertion3 ="";
      if(type == "max") {
         axiomsubclass6 = dataFactory.getOWLSubClassOfAxiom(classOWLA, dataFactory.getOWLDataMinCardinality(num, prop, dp) );
      }else{
         axiomsubclass6 = dataFactory.getOWLSubClassOfAxiom( dataFactory.getOWLDataMaxCardinality(num, prop, dp), classOWLA);
      }
      manager.applyChanges(manager.addAxiom(ont3, axiomdeclaration1));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass6));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion3 = "consistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);


      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;

   }

   /*for the generation of an individual of a given class*/
   public static TestCaseImpl typeTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) type (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String indA = m.group(1).toString();
      String classB = m.group(2).toString();
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Individual(<"+indA+">)");
      precond.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precond);

      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      /*Axioms to be added*/
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass2));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLNamedIndividual indOWLA = dataFactory.getOWLNamedIndividual(IRI.create(indA));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLA);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLA);
      manager.applyChanges(manager.addAxiom(ont1, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont1, classAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));

      String expectedoutputassertion1 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for the generation of symmetry*/
   public static TestCaseImpl symmetryTest(String purpose, TestCaseImpl testCase){ /*Check*/
      Pattern p = Pattern.compile("(.*) characteristic symmetricproperty",Pattern.CASE_INSENSITIVE );
      Matcher m = p.matcher(purpose);
      String classA;
      String classB;
      String R;
      String classA1;

      /*Generation of classes*/
      m.find();
      classA = "classA";
      classA1 = classA+"1";
      R = m.group(1).toString();
      classB = "classB";
      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Property(<"+R+">)\n");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      String ind1= "individual001";
      String ind2 = "individua002";
      String ind3 = "individua003";

      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLNamedIndividual individualOWL1 = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLNamedIndividual individualOWL2 = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLNamedIndividual individualOWL3 = dataFactory.getOWLNamedIndividual(IRI.create(ind3));
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClassAssertionAxiom owlClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLB, individualOWL2);
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(individualOWL2)));
      OWLDifferentIndividualsAxiom differentIndividualsAxiom = dataFactory.getOWLDifferentIndividualsAxiom(individualOWL1, individualOWL2);
      OWLDifferentIndividualsAxiom differentIndividualsAxiom2 = dataFactory.getOWLDifferentIndividualsAxiom(individualOWL3, individualOWL2);
      OWLClassAssertionAxiom owlClassAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, individualOWL1);
      OWLClassAssertionAxiom owlClassAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(classOWLB, individualOWL3);

      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(classOWLB)));
      manager.applyChanges(manager.addAxiom(ont, owlClassAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(classOWLA1)));
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(classOWLA)));
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(prop)));
      manager.applyChanges(manager.addAxiom(ont, subClassOfAxiom));
      manager.applyChanges(manager.addAxiom(ont, subClassOfAxiom2));
      manager.applyChanges(manager.addAxiom(ont,  differentIndividualsAxiom));
      manager.applyChanges(manager.addAxiom(ont,  differentIndividualsAxiom2));
      manager.applyChanges(manager.addAxiom(ont,  owlClassAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont,  owlClassAssertionAxiom3));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, individualOWL1,individualOWL2);
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "consistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, individualOWL3,individualOWL1);
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);
      String expectedoutputassertion2 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);


      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   public static TestCaseImpl symmetryWithDomainRangeTest(String purpose, TestCaseImpl testCase){ /*Check*/
      Pattern p = Pattern.compile("(.*) subclassof symmetricproperty\\((.*)\\) (some|only) (.*)",Pattern.CASE_INSENSITIVE );
      Matcher m = p.matcher(purpose);

      String classA;
      String classB;
      String R;
      String classA1;

      /*Generation of classes*/
      if(!m.find()){
         p = Pattern.compile("(.*) subclassOf coparticipateswith (some|only) (.*)",Pattern.CASE_INSENSITIVE);
         m = p.matcher(purpose);
         m.find();
         classA = m.group(1).toString().replace(" ","");
         classA1 = classA+"1";
         R = "coparticipateswith";
         classB = m.group(2).toString().replace(" ","");
      }else{
         classA = m.group(1).toString().replace(" ","");
         classA1 = classA+"1";
         R = m.group(2).toString().replace(" ","");
         classB = m.group(4).toString().replace(" ","");
      }


      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)\n");
      precond.add("Property(<"+R+">)\n");
      precond.add("Class(<"+classB+">)\n");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/

      String ind1= "individual001";
      String ind2 = "individua002";
      String ind3 = "individua003";

      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLNamedIndividual individualOWL1 = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLNamedIndividual individualOWL2 = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLNamedIndividual individualOWL3 = dataFactory.getOWLNamedIndividual(IRI.create(ind3));
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClassAssertionAxiom owlClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLB, individualOWL2);
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(individualOWL2)));
      OWLDifferentIndividualsAxiom differentIndividualsAxiom = dataFactory.getOWLDifferentIndividualsAxiom(individualOWL1, individualOWL2);
      OWLDifferentIndividualsAxiom differentIndividualsAxiom2 = dataFactory.getOWLDifferentIndividualsAxiom(individualOWL3, individualOWL2);
      OWLClassAssertionAxiom owlClassAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, individualOWL1);
      OWLClassAssertionAxiom owlClassAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(classOWLB, individualOWL3);


      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(classOWLB)));
      manager.applyChanges(manager.addAxiom(ont, owlClassAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(classOWLA1)));
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(classOWLA)));
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(prop)));
      manager.applyChanges(manager.addAxiom(ont, subClassOfAxiom));
      manager.applyChanges(manager.addAxiom(ont, subClassOfAxiom2));
      manager.applyChanges(manager.addAxiom(ont,  differentIndividualsAxiom));
      manager.applyChanges(manager.addAxiom(ont,  differentIndividualsAxiom2));
      manager.applyChanges(manager.addAxiom(ont,  owlClassAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont,  owlClassAssertionAxiom3));

      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, individualOWL1,individualOWL2);
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "consistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, individualOWL3,individualOWL1);
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);
      String expectedoutputassertion2 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for domain*/
   public static TestCaseImpl domainTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) domain (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String propertyA = m.group(1).toString();
      String classA = m.group(2).toString();
      String classAnoSymb = propertyA.replace(">","").replace("<","");
      String noClassA =  "No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"";

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+propertyA+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass2));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }

      String classAwithouturi = noClassA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];

      String ind1= classAwithouturi.toLowerCase().replace(">","").replace("<","")+"001";
      String ind2 = "thing002";

      OWLNamedIndividual indOWLnoA = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLnoA);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(noClassOWLA, indOWLnoA);
      OWLNamedIndividual indOWL = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWL);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(propertyA));
      //OWLDeclarationAxiom axiomProp1 = dataFactory.getOWLDeclarationAxiom(prop);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLnoA, indOWL);

      manager.applyChanges(manager.addAxiom(ont1, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont1, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont1, axiomInd2));
      //manager.applyChanges(manager.addAxiom(ont1, axiomProp1));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));

      OWLOntology assertion1 = manager.getOntology(IRI.create(base));

      String expectedoutputassertion1 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }


      OWLNamedIndividual indOWLA = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomIndA = dataFactory.getOWLDeclarationAxiom(indOWLA);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA, indOWLnoA);
      OWLPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA, indOWL);

      manager.applyChanges(manager.addAxiom(ont2, axiomIndA));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, axiomInd2));
      //manager.applyChanges(manager.addAxiom(ont2, axiomProp1));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));

      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);

      String expectedoutputassertion2 = "consistent";


      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;

   }

   /*for range*/
   public static TestCaseImpl rangeTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) range (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String propertyA = m.group(1).toString();
      String classA = m.group(2).toString();
      String classA1 = classA+"1";
      String noClassA =  "No"+classA.split("(#|\\/)")[classA.split("(#)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+propertyA+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass2));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      String classAwithouturi = noClassA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];
      String ind1= classAwithouturi.toLowerCase().replace(">","").replace("<","")+"001";
      String ind2 = "thing002";
      OWLNamedIndividual indOWLnoA = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLnoA);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(noClassOWLA, indOWLnoA);
      OWLNamedIndividual indOWL = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWL);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(propertyA));
     // OWLDeclarationAxiom axiomProp1 = dataFactory.getOWLDeclarationAxiom(prop);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWL, indOWLnoA);
      manager.applyChanges(manager.addAxiom(ont1, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont1, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont1, axiomInd2));
      //manager.applyChanges(manager.addAxiom(ont1, axiomProp1));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLNamedIndividual indOWLA = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomIndA = dataFactory.getOWLDeclarationAxiom(indOWLA);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA, indOWLnoA);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWL, indOWLA);
      manager.applyChanges(manager.addAxiom(ont2, axiomIndA));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, axiomInd2));
      //manager.applyChanges(manager.addAxiom(ont2, axiomProp1));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion2 = "consistent";
      manager.removeOntology(ont2);

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for range dp*/
   /*Meter datetime*/
   public static TestCaseImpl rangeTestDP(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) range (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp|literal|rdfs:literal)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String propertyA = m.group(1).toString();
      String datatypeA = m.group(2).toString();
      String nodatatypeA =  "No"+datatypeA.split("(#|\\/)")[datatypeA.split("(#)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Property(<"+propertyA+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLDataProperty datatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(datatypeA));
      OWLDataProperty nodatatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(nodatatypeA));
      OWLDeclarationAxiom axiomDatatype = dataFactory.getOWLDeclarationAxiom(datatypeOWLA);
      OWLDeclarationAxiom axiomDatatype2 = dataFactory.getOWLDeclarationAxiom(nodatatypeOWLA);
      OWLNamedIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      manager.applyChanges(manager.addAxiom(ont, axiomDatatype));
      manager.applyChanges(manager.addAxiom(ont, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(ind1)));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*
       *Assertions
       * */
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(propertyA));
      OWLDataPropertyAssertionAxiom assertionAxiom = null;
      if(datatypeA.contains("string") || datatypeA.contains("literal") || datatypeA.contains("datatime")  || datatypeA.contains("rational") || datatypeA.contains("anyuri") || datatypeA.contains("boolean")){
         assertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, 1 );
      }else{
         assertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, "temporalEntity");
      }
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(ind1)));
      manager.applyChanges(manager.addAxiom(ont1, assertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLDataPropertyAssertionAxiom assertionAxiom2 = null;
      if(datatypeA.contains("string") || datatypeA.contains("literal") || datatypeA.contains("rational")|| datatypeA.contains("anyuri")){
         assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, "temporalEntity");
      }else if(datatypeA.contains("boolean")){
         assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, true);
      }else if(datatypeA.contains("datetime")){
         assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, dataFactory.getOWLLiteral("2001-10-26T21:32:52.12679",
                 OWL2Datatype.XSD_DATE_TIME));
      }else{
         assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, 1);
      }
      manager.applyChanges(manager.addAxiom(ont2, dataFactory.getOWLDeclarationAxiom(ind1)));
      manager.applyChanges(manager.addAxiom(ont2, assertionAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion2 = "consistent";
      manager.removeOntology(ont2);

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }


   public static TestCaseImpl rangeTestDPLiteral(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) range (literal|rdfs:literal)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String propertyA = m.group(1).toString();
      String datatypeA = m.group(2).toString();
      String nodatatypeA =  "No"+datatypeA.split("(#|\\/)")[datatypeA.split("(#)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Property(<"+propertyA+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLDataProperty datatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(datatypeA));
      OWLDataProperty nodatatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(nodatatypeA));
      OWLDeclarationAxiom axiomDatatype = dataFactory.getOWLDeclarationAxiom(datatypeOWLA);
      OWLDeclarationAxiom axiomDatatype2 = dataFactory.getOWLDeclarationAxiom(nodatatypeOWLA);
      OWLNamedIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      manager.applyChanges(manager.addAxiom(ont, axiomDatatype));
      manager.applyChanges(manager.addAxiom(ont, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(ind1)));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*
       *Assertions
       * */
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(propertyA));
      OWLDataPropertyAssertionAxiom assertionAxiom = null;

      assertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, 1 );

      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(ind1)));
      manager.applyChanges(manager.addAxiom(ont1, assertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "consistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLDataPropertyAssertionAxiom assertionAxiom2 = null;

      assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, "temporalEntity");

      manager.applyChanges(manager.addAxiom(ont2, dataFactory.getOWLDeclarationAxiom(ind1)));
      manager.applyChanges(manager.addAxiom(ont2, assertionAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion2 = "consistent";
      manager.removeOntology(ont2);

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      testCase.setType("literal");
      return testCase;
   }


   /*for the generation of subclass, disjoint and equivalence*/
   public static TestCaseImpl multipleSubClassTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (.*) and (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);
      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String noClassA =  "No"+classA.split("(#|\\/)")[classA.split("(#)").length-1];
      String classA1 =  classA.split("(#|\\/)")[classA.split("(#)").length-1]+"1";
      String classB = m.group(2).toString();
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String classC = m.group(3).toString();
      String noClassC =  "No"+classC.split("(#|\\/)")[classC.split("(#|\\/)").length-1];
      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA.replaceAll(" ","")+">)");
      precond.add("Class(<"+classB.replaceAll(" ","")+">)");
      precond.add("Class(<"+classC.replaceAll(" ","")+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass4 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass noClassOWLC = dataFactory.getOWLClass(IRI.create(noClassC));
      OWLDeclarationAxiom axiomClass5 = dataFactory.getOWLDeclarationAxiom(classOWLC);
      OWLDeclarationAxiom axiomClass6 = dataFactory.getOWLDeclarationAxiom(noClassOWLC);
      OWLEquivalentClassesAxiom equivalentClassesAxiom3 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLC, dataFactory.getOWLObjectComplementOf(classOWLC));

      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass2));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomClass3));
      manager.applyChanges(manager.addAxiom(ont, axiomClass4));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom2));
      manager.applyChanges(manager.addAxiom(ont, axiomClass5));
      manager.applyChanges(manager.addAxiom(ont, axiomClass6));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom3));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass7 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLA, classOWLB));
      manager.applyChanges(manager.addAxiom(ont1, axiomClass7));
      manager.applyChanges(manager.addAxiom(ont1, subClassOfAxiom1));

      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont1);

      String expectedoutputassertion1 ="";
      expectedoutputassertion1 = "consistent";

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLB, classOWLA));
      manager.applyChanges(manager.addAxiom(ont2, axiomClass7));
      manager.applyChanges(manager.addAxiom(ont2, subClassOfAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);
      String expectedoutputassertion2 = "unsatisfiable";


      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLB, classOWLA));
      manager.applyChanges(manager.addAxiom(ont3, axiomClass7));
      manager.applyChanges(manager.addAxiom(ont3, subClassOfAxiom3));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont3);
      String expectedoutputassertion3  = "consistent";

      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLA, classOWLC));
      manager.applyChanges(manager.addAxiom(ont4, axiomClass7));
      manager.applyChanges(manager.addAxiom(ont4, subClassOfAxiom4));

      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont4);

      String expectedoutputassertion4 ="";
      expectedoutputassertion4 = "consistent";

      OWLOntology ont5 = null;
      try {
         ont5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont5 = null;
      }

      OWLSubClassOfAxiom subClassOfAxiom5 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLA, noClassOWLC));
      manager.applyChanges(manager.addAxiom(ont5, axiomClass7));
      manager.applyChanges(manager.addAxiom(ont5, subClassOfAxiom5));
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont5);
      String expectedoutputassertion5 = "unsatisfiable";

      OWLOntology ont6 = null;
      try {
         ont6 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont6 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom6 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLA, classOWLC));
      manager.applyChanges(manager.addAxiom(ont6, axiomClass7));
      manager.applyChanges(manager.addAxiom(ont6, subClassOfAxiom6));
      OWLOntology assertion6 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont6);
      String expectedoutputassertion6 = "consistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);
      hashinput.put("Assertion 5", assertion5);
      hashinput.put("Assertion 6", assertion6);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);
      hashoutput.put("Assertion 5",expectedoutputassertion5);
      hashoutput.put("Assertion 6",expectedoutputassertion6);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for the generation of subclass, disjoint and equivalence*/
   public static TestCaseImpl subClassTest(String purpose, String type, TestCaseImpl testCase){
      Pattern p = Pattern.compile("([^\\s]+) (subclassof|equivalentto|disjointwith) ([^\\s]+)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);
      String classA="";
      String classB="";
      String noClassA = "";
      String classA1 ="";
      String noClassB="";
      m.find();

      /*Generation of classes*/
      classA = m.group(1).toString();
      noClassA = "No" + classA.split("(#|\\/)")[classA.split("(#)").length - 1];
      classA1 = classA.split("(#|\\/)")[classA.split("(#)").length - 1] + "1";

      classB = m.group(3).toString();
      noClassB = "No" + classB.split("(#|\\/)")[classB.split("(#|\\/)").length - 1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA.replaceAll(" ","")+">)");
      precond.add("Class(<"+classB.replaceAll(" ","")+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();

      if(!classA.equals(classB)) {
         /*Preparation*/
         OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
         OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
         OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);
         OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
         OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
         OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
         OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
         OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(classOWLB);
         OWLDeclarationAxiom axiomClass4 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
         OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));

         manager.applyChanges(manager.addAxiom(ont, axiomClass));
         manager.applyChanges(manager.addAxiom(ont, axiomClass2));
         manager.applyChanges(manager.addAxiom(ont, axiomClass3));
         manager.applyChanges(manager.addAxiom(ont, axiomClass4));
         manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
         manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom2));
         testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
         manager.removeOntology(ont);

         /*Assertions*/
         OWLOntology ont1 = null;
         try {
            ont1 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont1 = null;
         }

         OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
         OWLDeclarationAxiom axiomClass7 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
         OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLA, classOWLB));
         manager.applyChanges(manager.addAxiom(ont1, axiomClass7));
         manager.applyChanges(manager.addAxiom(ont1, subClassOfAxiom1));

         OWLOntology assertion1 = manager.getOntology(IRI.create(base));
         manager.removeOntology(ont1);

         String expectedoutputassertion1 = "";
         if (type == "equivalence")
            expectedoutputassertion1 = "unsatisfiable";
         else
            expectedoutputassertion1 = "consistent";

         OWLOntology ont2 = null;
         try {
            ont2 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont2 = null;
         }

         OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLB, classOWLA));
         manager.applyChanges(manager.addAxiom(ont2, axiomClass7));
         manager.applyChanges(manager.addAxiom(ont2, subClassOfAxiom2));

         OWLOntology assertion2 = manager.getOntology(IRI.create(base));
         manager.removeOntology(ont2);

         String expectedoutputassertion2 = "";
         if (type == "strict subclass" || type == "equivalence")
            expectedoutputassertion2 = "unsatisfiable";
         else
            expectedoutputassertion2 = "consistent";

         OWLOntology ont3 = null;
         try {
            ont3 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont3 = null;
         }

         OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLB, classOWLA));
         manager.applyChanges(manager.addAxiom(ont3, axiomClass7));
         manager.applyChanges(manager.addAxiom(ont3, subClassOfAxiom3));

         OWLOntology assertion3 = manager.getOntology(IRI.create(base));
         manager.removeOntology(ont3);

         String expectedoutputassertion3 = "";
         if (type == "disjoint")
            expectedoutputassertion3 = "unsatisfiable";
         else
            expectedoutputassertion3 = "consistent";


         hashinput.put("Assertion 1", assertion1);
         hashinput.put("Assertion 2", assertion2);
         hashinput.put("Assertion 3", assertion3);

         hashoutput.put("Assertion 1",expectedoutputassertion1);
         hashoutput.put("Assertion 2",expectedoutputassertion2);
         hashoutput.put("Assertion 3",expectedoutputassertion3);

         for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
            hashoutput.put(entry.getKey(), entry.getValue());
         }

         for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
            hashinput.put(entry.getKey(), entry.getValue());
         }
      }else{
         OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
         OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
         OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
         OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
         OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));

         manager.applyChanges(manager.addAxiom(ont, axiomClass));
         manager.applyChanges(manager.addAxiom(ont, axiomClass1));
         manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom2));
         testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
         manager.removeOntology(ont);

         OWLOntology ont1 = null;
         try {
            ont1 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont1 = null;
         }

         OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
         OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
         OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLB);
         manager.applyChanges(manager.addAxiom(ont1, axiomClass2));
         manager.applyChanges(manager.addAxiom(ont1, subClassOfAxiom1));

         OWLOntology assertion1 = manager.getOntology(IRI.create(base));
         manager.removeOntology(ont1);

         String expectedoutputassertion1 = "consistent";
         OWLOntology ont2 = null;
         try {
            ont2 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont2 = null;
         }

         OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, noClassOWLB);
         manager.applyChanges(manager.addAxiom(ont2, axiomClass2));
         manager.applyChanges(manager.addAxiom(ont2, subClassOfAxiom2));

         OWLOntology assertion2 = manager.getOntology(IRI.create(base));
         manager.removeOntology(ont2);

         String  expectedoutputassertion2 = "unsatisfiable";

         hashinput.put("Assertion 1", assertion1);
         hashinput.put("Assertion 2", assertion2);

         hashoutput.put("Assertion 1",expectedoutputassertion1);
         hashoutput.put("Assertion 2",expectedoutputassertion2);

         for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
            hashoutput.put(entry.getKey(), entry.getValue());
         }

         for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
            hashinput.put(entry.getKey(), entry.getValue());
         }
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for range (strict) universal restriction*/
   public static TestCaseImpl existentialRange(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) some (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString().replaceAll("\\(","").replaceAll("\\)","").replaceAll("  "," ");;
      String classA1 = classA+"1";
      String R = m.group(2).toString().replaceAll("  ","");

      String classB = "";
      classB = m.group(3).toString().replaceAll("\\(","").replaceAll("\\)","").replaceAll("  "," ");
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      precond.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precond);

      String ind1= "individua001";
      String ind2 = "individua002";
      String ind3 = "individua003";


      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLnoB = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWLnoB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLnoB);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLnoB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLnoB);
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass2));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create(ind3));
      OWLDeclarationAxiom axiomInd3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLnoB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLnoB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      OWLClassAssertionAxiom classAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(classOWLB, indOWL3);

      manager.applyChanges(manager.addAxiom(ont2, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass3));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom3));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ont2);

      String expectedoutputassertion2 = "consistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      testCase.setType("existential");
      return testCase;
   }


   /*for range (strict) and for OP + universal restriction*/
   public static TestCaseImpl universalRange(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) only (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString().replaceAll("\\(","").replaceAll("\\)","").replaceAll("  "," ");;
      String classA1 = classA+"1";
      String R = m.group(2).toString().replaceAll("  ","");

      String classB = "";
      classB = m.group(3).toString().replaceAll("\\(","").replaceAll("\\)","").replaceAll("  "," ");
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      precond.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precond);

      String ind1= "individua001";
      String ind2 = "individua002";
      String ind3 = "individua003";

      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);
      /*Assertions */
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);

      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass2));
      manager.applyChanges(manager.addAxiom(ont1, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";

      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create(ind3));
      OWLDeclarationAxiom axiomInd3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLClassAssertionAxiom owlClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWL3);
      OWLSubClassOfAxiom axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);


      manager.applyChanges(manager.addAxiom(ont2, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont2, owlClassAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass3));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);
      String expectedoutputassertion2 = "inconsistent";


      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);


      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for range (strict) universal restriction DP*/
   public static TestCaseImpl existentialRangeDP(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (.*) some (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp|literal|rdfs:literal)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString().replaceAll("\\(","").replaceAll("\\)","");;
      String classA1 = classA+"1";
      String R = m.group(2).toString();
      String datatype = "";
      datatype = m.group(3).toString().replaceAll("\\(","").replaceAll("\\)","");;
      String nodatatype =  "No"+datatype.split("(#|\\/)")[datatype.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      testCase.getPrecondition().addAll(precond);

      /*Preparation*/
      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }

      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclass1 = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomclass2 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLSubClassOfAxiom subClassOfAxiomAA1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLDataProperty datatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(datatype));
      OWLDataProperty nodatatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(nodatatype));
      OWLDeclarationAxiom axiomDatatype = dataFactory.getOWLDeclarationAxiom(datatypeOWLA);
      OWLDeclarationAxiom axiomDatatype2 = dataFactory.getOWLDeclarationAxiom(nodatatypeOWLA);
      OWLNamedIndividual indOWL1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLClassAssertionAxiom assertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL1);
      manager.applyChanges(manager.addAxiom(ont, axiomclass1));
      manager.applyChanges(manager.addAxiom(ont, axiomclass2));
      manager.applyChanges(manager.addAxiom(ont, subClassOfAxiomAA1));
      manager.applyChanges(manager.addAxiom(ont, axiomDatatype));
      manager.applyChanges(manager.addAxiom(ont, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ont, subClassOfAxiomAA1));
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(indOWL1)));
      manager.applyChanges(manager.addAxiom(ont, assertionAxiom));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Axioms to be added*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(R));
      OWLSubClassOfAxiom subClassOfAxiom = null;

      if(datatype.contains("string") ||  datatype.contains("literal") || datatype.contains("datetime") || datatype.contains("rational")|| datatype.contains("anyuri")){
         subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLDataAllValuesFrom(prop, OWL2Datatype.XSD_INTEGER.getDatatype(dataFactory)));
      }else if(datatype.contains("anyuri")){
         subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLDataAllValuesFrom(prop, OWL2Datatype.XSD_INTEGER.getDatatype(dataFactory)));
      }else{
         subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLDataAllValuesFrom(prop, OWL2Datatype.XSD_STRING.getDatatype(dataFactory)));
      }

      manager.applyChanges(manager.addAxiom(ont1, subClassOfAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";

      manager.removeOntology(ont1);

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      testCase.setType("existential dp");
      return testCase;
   }

   /*for range (strict) and for DP + universal restriction*/
   public static TestCaseImpl universalRangeDP(String purpose, TestCaseImpl testCase){
      ArrayList<String> complementClasses = new ArrayList<>();
      if(purpose.contains("not(")){
         Pattern p = Pattern.compile("not\\((.*?)\\)");
         Matcher m = p.matcher(purpose);
         while(m.find()){
            complementClasses.add(m.group().toString().replace("not(","").replace(")",""));
         }
      }

      Pattern p = Pattern.compile("(.*) subclassof (.*) only (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp|literal|rdfs:literal)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString().replaceAll("\\(","").replaceAll("\\)","");;
      String classA1 = classA+"1";
      String R = m.group(2).toString();
      String datatype = "";
      datatype = m.group(3).toString().replaceAll("\\(","").replaceAll("\\)","");;
      String nodatatype =  "No"+datatype.split("(#|\\/)")[datatype.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      testCase.getPrecondition().addAll(precond);

      /*Preparation*/
      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }

      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclass1 = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomclass2 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLSubClassOfAxiom subClassOfAxiomAA1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      manager.applyChanges(manager.addAxiom(ont, axiomclass1));
      manager.applyChanges(manager.addAxiom(ont, axiomclass2));
      manager.applyChanges(manager.addAxiom(ont, subClassOfAxiomAA1));

      OWLDataProperty datatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(datatype));
      OWLDataProperty nodatatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(nodatatype));
      OWLDeclarationAxiom axiomDatatype = dataFactory.getOWLDeclarationAxiom(datatypeOWLA);
      OWLDeclarationAxiom axiomDatatype2 = dataFactory.getOWLDeclarationAxiom(nodatatypeOWLA);

      OWLNamedIndividual indOWL1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLClassAssertionAxiom assertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL1);

      manager.applyChanges(manager.addAxiom(ont, axiomDatatype));
      manager.applyChanges(manager.addAxiom(ont, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ont, dataFactory.getOWLDeclarationAxiom(indOWL1)));
      manager.applyChanges(manager.addAxiom(ont, assertionAxiom));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Axioms to be added*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(R));
      OWLClassAssertionAxiom owlClassAssertionAxiom;
      OWLDataPropertyAssertionAxiom objectPropertyAssertionAxiom2;
      if(datatype.contains("string") || datatype.contains("datetime") || datatype.contains("rational")|| datatype.contains("anyuri")){
         OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("ind3"));
         owlClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL3);
         objectPropertyAssertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL3, 1);
      }else if(datatype.contains("anyuri")){
         OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("ind3"));
         owlClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL3);
         objectPropertyAssertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL3, 1);
      }else{
         OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("ind3"));
         owlClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL3);
         objectPropertyAssertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL3, "");

      }
      manager.applyChanges(manager.addAxiom(ont1, axiomDatatype));
      manager.applyChanges(manager.addAxiom(ont1, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ont1, dataFactory.getOWLDeclarationAxiom(indOWL1)));
      manager.applyChanges(manager.addAxiom(ont1, assertionAxiom));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont1, owlClassAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";

      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom2 = null;
      OWLDeclarationAxiom axiomInd4;
      OWLClassAssertionAxiom owlClassAssertionAxiom2;
      OWLDataPropertyAssertionAxiom objectPropertyAssertionAxiom3;
      if(datatype.contains("string") || datatype.contains("datetime") || datatype.contains("rational")){
         OWLNamedIndividual indOWL4 = dataFactory.getOWLNamedIndividual(IRI.create("ind4"));
         axiomInd4 = dataFactory.getOWLDeclarationAxiom(indOWL4);
         owlClassAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL4);
         objectPropertyAssertionAxiom3 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL4, "");
      }else if(datatype.contains("anyuri")){
         OWLNamedIndividual indOWL4 = dataFactory.getOWLNamedIndividual(IRI.create("ind4"));
         owlClassAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL4);
         objectPropertyAssertionAxiom3 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL4,"");
      }else{
         OWLNamedIndividual indOWL4 = dataFactory.getOWLNamedIndividual(IRI.create("ind4"));
         owlClassAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL4);
         objectPropertyAssertionAxiom3 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL4, 3);
      }

      manager.applyChanges(manager.addAxiom(ont2, axiomDatatype));
      manager.applyChanges(manager.addAxiom(ont2, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ont2, dataFactory.getOWLDeclarationAxiom(indOWL1)));
      manager.applyChanges(manager.addAxiom(ont2, assertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, owlClassAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, owlClassAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion2 = "inconsistent";

      manager.removeOntology(ont2);

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for  cardinality */
   public static TestCaseImpl cardinality(String purpose, String type, TestCaseImpl testCase){
      Pattern p1 = Pattern.compile("(.*) subclassof (.*) (min|max|exactly) ([1-9][0-9]*) (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p1.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();

      String classA1 = classA+"1";
      String R = m.group(2).toString();
      String classB = "";
      classB = m.group(5).toString();
      Integer num = Integer.parseInt(m.group(4).toString());

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      precond.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/

      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      manager.addAxiom(ont,axiomClass);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));

      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      manager.applyChanges(manager.addAxiom(ont, axiomsubclass1));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);
      /*Assertions*/
      /*Assertions 1 */
      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      manager.addAxiom(ont,axiomClass2);
      OWLSubClassOfAxiom axiomsubclass4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectMaxCardinality(num-1, prop, classOWLB));
      manager.addAxiom(ont2, axiomsubclass4);
      manager.addAxiom(ont2, axiomClass2);
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));

      String expectedoutputassertion1 = "";
      if (type == "min" || type == "exactly")
         expectedoutputassertion1 = "unsatisfiable";
      else
         expectedoutputassertion1 = "consistent";
      manager.removeOntology(ont2);
      /*Assertions 2 */
      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num+1, prop, classOWLB) );
      manager.applyChanges( manager.addAxiom(ont4, axiomsubclass2));
      manager.applyChanges( manager.addAxiom(ont4, axiomClass2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      String expectedoutputassertion2 = "";
      if (type == "max" || type == "exactly")
         expectedoutputassertion2 = "unsatisfiable";
      else
         expectedoutputassertion2 = "consistent";
      manager.removeOntology(ont4);
      /*Assertions 3 */
      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      OWLSubClassOfAxiom axiomsubclass3 = null;

      if(type == "max") {
         axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectMinCardinality(num, prop, classOWLB));

      }else{
         axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( dataFactory.getOWLObjectMaxCardinality(num, prop, classOWLB), classOWLA1);
      }
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass3));
      manager.applyChanges(manager.addAxiom(ont3,axiomClass2));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));

      String expectedoutputassertion3 = "consistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);


      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;

   }

   /*for intersection + cardinality */ /*Poner opciones para min, exact y sin cardinalidad?*/
   public static TestCaseImpl intersectionCardTest(String purpose, String type, TestCaseImpl testCase ){ /*TODO*/
      Pattern p = Pattern.compile("(.*) subclassof (.*) (max|min|exactly) (\\d+) \\((.*) and (.*)\\)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString().replaceAll("\\(","").replaceAll("\\)","");
      String classA1 = classA+"1";

      String R = m.group(2).toString();

      String classB = m.group(5).toString().replaceAll("\\(","").replaceAll("\\)","");
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      String classC = m.group(6).toString().replaceAll("\\(","").replaceAll("\\)","");
      String noClassC =  "No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      Integer num = Integer.parseInt(m.group(4).toString().replace("min","").replace("max","").replace("exactly","").replace(" ",""));


      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)\n");
      precond.add("Property(<"+R+">)\n");
      precond.add("Class(<"+classB+">)\n");
      precond.add("Class(<"+classC+">)\n");

      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass noClassOWLC = dataFactory.getOWLClass(IRI.create(noClassC));
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLC);
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(noClassOWLC);
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLC, dataFactory.getOWLObjectComplementOf(classOWLC));
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomClass2));
      manager.applyChanges(manager.addAxiom(ont, axiomClass3));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom2));

      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMaxCardinality(num-1,prop,
              dataFactory.getOWLObjectIntersectionOf(classOWLB,classOWLC)));

      manager.applyChanges(manager.addAxiom(ont1, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass2));

      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont1);

      String expectedoutputassertion2 = "";
      if (type == "min" || type == "exactly")
         expectedoutputassertion2 = "unsatisfiable";
      else
         expectedoutputassertion2 = "consistent";

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }

      OWLSubClassOfAxiom axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num+1,prop,
              dataFactory.getOWLObjectIntersectionOf(classOWLB,classOWLC)));
      manager.applyChanges(manager.addAxiom(ont2, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass3));

      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);
      String expectedoutputassertion3 = "";
      if (type == "max" || type == "exactly")
         expectedoutputassertion3 = "unsatisfiable";
      else
         expectedoutputassertion3 = "consistent";

      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }

      OWLSubClassOfAxiom axiomsubclass4 = null;

      if(type == "max") {
         axiomsubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num,prop,
                 dataFactory.getOWLObjectIntersectionOf(classOWLB,classOWLC)));
      }else{
         axiomsubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMaxCardinality(num,prop,
                 dataFactory.getOWLObjectIntersectionOf(classOWLB,classOWLC)));
      }
      String expectedoutputassertion4 = "consistent";


      manager.applyChanges(manager.addAxiom(ont3, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass4));

      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont3);

      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }

      OWLSubClassOfAxiom axiomsubclass5 = null;
      if(type== "max") {
         axiomsubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num+1,prop,
                 classOWLB));
      }
      String expectedoutputassertion5 = "consistent";
      manager.applyChanges(manager.addAxiom(ont4, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass5));

      OWLOntology assertion5 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont4);

      OWLOntology ont5 = null;
      try {
         ont5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont5 = null;
      }

      OWLSubClassOfAxiom axiomsubclass6 = null;
      if(type =="max"){
         axiomsubclass6 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num+1,prop,
                 classOWLC));
      }
      String expectedoutputassertion6 = "consistent";
      manager.applyChanges(manager.addAxiom(ont5, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont5, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont5, axiomsubclass6));

      OWLOntology assertion6 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont5);
      OWLOntology ont6 = null;
      try {
         ont6 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont6 = null;
      }

      OWLSubClassOfAxiom axiomsubclass7 = null;
      if(type == "max"){
         axiomsubclass7 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num+1,prop,
                 dataFactory.getOWLObjectIntersectionOf(classOWLB,classOWLC)));
      }
      String expectedoutputassertion7 = "unsatisfiable";
      manager.applyChanges(manager.addAxiom(ont6, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont6, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont6, axiomsubclass7));

      OWLOntology assertion7 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont6);


      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);
      hashinput.put("Assertion 5", assertion5);
      hashinput.put("Assertion 6", assertion6);
      hashinput.put("Assertion 7", assertion7);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);
      hashoutput.put("Assertion 5",expectedoutputassertion5);
      hashoutput.put("Assertion 6",expectedoutputassertion6);
      hashoutput.put("Assertion 7",expectedoutputassertion7);


      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for union */
   public static TestCaseImpl unionTest(String purpose, String type, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassOf (.*) only (.*) or (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classA1 =  classA.split("(#|\\/)")[classA.split("(#)").length-1]+"1";

      String R =m.group(2).toString();
      String classB = m.group(3).toString();
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      String classC = m.group(4).toString();
      String noClassC =  "No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String noClassCB =  "No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Class(<"+classB+">)");
      precond.add("Class(<"+classC+">)");
      precond.add("Property(<"+R+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      /*Preconditions*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass noClassOWLC = dataFactory.getOWLClass(IRI.create(noClassC));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLClass noclassOWLCB = dataFactory.getOWLClass(IRI.create(noClassCB));
      OWLSubClassOfAxiom subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLClass(IRI.create(classA)));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLC);
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(noClassOWLC);
      OWLDeclarationAxiom axiomClass4 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLDeclarationAxiom axiomClass5 = dataFactory.getOWLDeclarationAxiom(noclassOWLCB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLC, dataFactory.getOWLObjectComplementOf(classOWLC));
      OWLEquivalentClassesAxiom equivalentClassesAxiom3 = dataFactory.getOWLEquivalentClassesAxiom(noclassOWLCB, dataFactory.getOWLObjectComplementOf(classOWLC));
      OWLEquivalentClassesAxiom equivalentClassesAxiom4 = dataFactory.getOWLEquivalentClassesAxiom(noclassOWLCB, dataFactory.getOWLObjectComplementOf(classOWLB));
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, axiomClass2));
      manager.applyChanges(manager.addAxiom(ont, axiomClass3));
      manager.applyChanges(manager.addAxiom(ont, axiomClass4));
      manager.applyChanges(manager.addAxiom(ont, subClassOfAxiom));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom2));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom3));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom4));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);
      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectSomeValuesFrom(prop, classOWLB));

      manager.applyChanges(manager.addAxiom(ont1, subClassOfAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "consistent";

      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectSomeValuesFrom(prop, classOWLC));

      manager.applyChanges(manager.addAxiom(ont2, subClassOfAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion2 = "consistent";

      manager.removeOntology(ont2);

      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectSomeValuesFrom(prop, noclassOWLCB));

      manager.applyChanges(manager.addAxiom(ont3, subClassOfAxiom3));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ont3);

      String expectedoutputassertion3 ="";
      expectedoutputassertion3 = "unsatisfiable";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for intersection*/
   public static TestCaseImpl intersectionTest(String purpose, String type, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassOf (.*) some (.*) and (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classA1 =  classA.split("(#|\\/)")[classA.split("(#)").length-1]+"1";

      String R =m.group(2).toString();
      String classB = m.group(3).toString();

      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      String classC = m.group(4).toString();
      String noClassC =  "No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Class(<"+classB+">)");
      precond.add("Class(<"+classC+">)");
      precond.add("Property(<"+R+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      /*Preconditions*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass noClassOWLC = dataFactory.getOWLClass(IRI.create(noClassC));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLSubClassOfAxiom subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLClass(IRI.create(classA)));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLC);
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(noClassOWLC);
      OWLDeclarationAxiom axiomClass4 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLC, dataFactory.getOWLObjectComplementOf(classOWLC));
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, axiomClass2));
      manager.applyChanges(manager.addAxiom(ont, axiomClass3));
      manager.applyChanges(manager.addAxiom(ont, axiomClass4));
      manager.applyChanges(manager.addAxiom(ont, subClassOfAxiom));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectIntersectionOf(classOWLB)));

      manager.applyChanges(manager.addAxiom(ont1, subClassOfAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "consistent";

      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectIntersectionOf(noClassOWLB, noClassOWLC)));

      manager.applyChanges(manager.addAxiom(ont2, subClassOfAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ont2);

      String expectedoutputassertion2 ="";
      expectedoutputassertion2 = "unsatisfiable";

      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectIntersectionOf(classOWLB, noClassOWLC)));

      manager.applyChanges(manager.addAxiom(ont3, subClassOfAxiom3));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ont3);

      String expectedoutputassertion3 = "";

      expectedoutputassertion3 = "unsatisfiable";

      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectIntersectionOf(classOWLC, noClassOWLB)));

      manager.applyChanges(manager.addAxiom(ont4, subClassOfAxiom4));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ont3);

      String expectedoutputassertion4 = "";

      expectedoutputassertion4 = "unsatisfiable";


      OWLSubClassOfAxiom subClassOfAxiom5 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectAllValuesFrom(prop, classOWLC));

      manager.applyChanges(manager.addAxiom(ont4, subClassOfAxiom5));
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ont3);

      String expectedoutputassertion5 = "";

      expectedoutputassertion5 = "consistent";


      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);
      hashinput.put("Assertion 5", assertion5);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);
      hashoutput.put("Assertion 5",expectedoutputassertion5);


      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for part-whole relations*/
   public static TestCaseImpl partWholeTestExistential(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (ispartof|partof) some (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classA1 = classA+"1>";
      String R = m.group(2).toString();
      String classB = "";
      classB = m.group(3).toString();
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      precond.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precond);

      String classA1withouturi = classA1.split("(#|\\/)")[classA1.split("(#|\\/)").length-1];
      String classBwithouturi = classB.split("(#|\\/)")[noClassB.split("(#|\\/)").length-1];


      /*Axioms to be added -- we need to test if the relation is transitive*/
      /*Preparation*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("ind1"));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("ind2"));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass2));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("ind3"));
      OWLDeclarationAxiom axiomInd3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont2, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass3));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ont2);

      String expectedoutputassertion2 = "consistent";


      /*Assertions*/
      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      String inverse = "";
      if(R.equals("ispartof"))
         inverse = "hasPart";
      else
         inverse = "isPartOf";

      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));
      OWLSubClassOfAxiom axiomsubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass4));
      manager.applyChanges(manager.addAxiom(ont3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion3 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }

      OWLSubClassOfAxiom axiomsubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);
      manager.applyChanges(manager.addAxiom(ont4, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont4, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass5));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom6));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont4);
      String expectedoutputassertion4 = "consistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);


      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for part-whole relations*/
   public static TestCaseImpl partWholeTestUniversal(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (ispartof|partof) only (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classA1 = classA+"1";
      String R = m.group(2).toString();
      String classB = "";
      classB = m.group(3).toString();
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      precond.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precond);

      String classA1withouturi = classA1.split("(#|\\/)")[classA1.split("(#|\\/)").length-1];
      String classBwithouturi = classB.split("(#|\\/)")[noClassB.split("(#|\\/)").length-1];

      /*Axioms to be added -- we need to test if the relation is transitive*/
      /*Preparation*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("ind1"));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("ind2"));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass2));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("ind3"));
      OWLDeclarationAxiom axiomInd3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont2, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass3));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ont2);

      String expectedoutputassertion2 = "consistent";


      /*Assertions*/
      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      String inverse = "";
      if(R.equals("ispartof"))
         inverse = "hasPart";
      else
         inverse = "isPartOf";

      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));

      OWLSubClassOfAxiom axiomsubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLA1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass4));
      manager.applyChanges(manager.addAxiom(ont3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion3 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }

      OWLSubClassOfAxiom axiomsubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);
      manager.applyChanges(manager.addAxiom(ont4, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont4, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass5));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom6));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont4);
      String expectedoutputassertion4 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);


      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /* for subclass of two classes and disjointness between them  */
   public static TestCaseImpl subclassDisjointTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (.*) and (.*) subclassof (.*) that disjointwith (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);
      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classAnoSymb = classA;
      String noClassA =  "No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1];
      String classA1 =  classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"1";
      String classB = m.group(2).toString();
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String classC = m.group(3).toString();
      String noClassC =  "No"+classC.split("(#|\\/)")[classC.split("(#|\\/)").length-1];
      if(classB.equals(m.group(4)) && classA.equals(m.group(5))) {

         /*Preconditions*/
         ArrayList<String> precond = new ArrayList<>();
         precond.add("Class(<" + classA + ">)");
         precond.add("Class(<" + classC + ">)");
         precond.add("Class(<" + classB + ">)");
         testCase.getPrecondition().addAll(precond);

         /*Axioms to be added*/

         /*Preparation*/
         String base = "";
         OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
         OWLOntology ont = null;
         try {
            ont = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont = null;
         }
         OWLDataFactory dataFactory = manager.getOWLDataFactory();

         OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
         OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
         OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
         OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
         OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
         OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
         OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLA);
         OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
         OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
         OWLClass noClassOWLC = dataFactory.getOWLClass(IRI.create(noClassC));
         OWLDeclarationAxiom axiomClass4 = dataFactory.getOWLDeclarationAxiom(classOWLC);
         OWLDeclarationAxiom axiomClass5 = dataFactory.getOWLDeclarationAxiom(noClassOWLC);
         OWLEquivalentClassesAxiom equivalentClassesAxiom1 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
         OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
         OWLEquivalentClassesAxiom equivalentClassesAxiom3 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLC, dataFactory.getOWLObjectComplementOf(classOWLC));
         OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
         OWLDeclarationAxiom axiomclass6 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
         OWLSubClassOfAxiom subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
         manager.applyChanges(manager.addAxiom(ont, axiomClass));
         manager.applyChanges(manager.addAxiom(ont, axiomClass1));
         manager.applyChanges(manager.addAxiom(ont, axiomClass2));
         manager.applyChanges(manager.addAxiom(ont, axiomClass3));
         manager.applyChanges(manager.addAxiom(ont, axiomClass4));
         manager.applyChanges(manager.addAxiom(ont, axiomClass5));
         manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom1));
         manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom2));
         manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom3));
         manager.applyChanges(manager.addAxiom(ont, axiomclass6));
         testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
         manager.removeOntology(ont);

         OWLOntology ont1 = null;
         try {
            ont1 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont1 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLA, classOWLB));
         manager.applyChanges(manager.addAxiom(ont1, axiomclass6));
         manager.applyChanges(manager.addAxiom(ont1, subClassOfAxiom2));
         OWLOntology assertion1 = manager.getOntology(IRI.create(base));
         String expectedoutputassertion1 = "";
         expectedoutputassertion1 = "consistent";
         manager.removeOntology(ont1);

         OWLOntology ont2 = null;
         try {
            ont2 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont2 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLA, noClassOWLB));
         manager.applyChanges(manager.addAxiom(ont2, axiomclass6));
         manager.applyChanges(manager.addAxiom(ont2, subClassOfAxiom3));
         OWLOntology assertion2 = manager.getOntology(IRI.create(base));

         String expectedoutputassertion2 = "";
         expectedoutputassertion2 = "unsatisfiable";
         manager.removeOntology(ont2);

         OWLOntology ont3 = null;
         try {
            ont3 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont3 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLA, classOWLB));
         manager.applyChanges(manager.addAxiom(ont3, axiomclass6));
         manager.applyChanges(manager.addAxiom(ont3, subClassOfAxiom4));
         OWLOntology assertion3 = manager.getOntology(IRI.create(base));
         String expectedoutputassertion3 = "consistent";
         manager.removeOntology(ont3);

         OWLOntology ont4 = null;
         try {
            ont4 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont4 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom5 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLC, classOWLB));
         manager.applyChanges(manager.addAxiom(ont4, axiomclass6));
         manager.applyChanges(manager.addAxiom(ont4, subClassOfAxiom5));
         OWLOntology assertion4 = manager.getOntology(IRI.create(base));
         String expectedoutputassertion4 = "consistent";
         manager.removeOntology(ont4);

         OWLOntology ont5 = null;
         try {
            ont5 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont5 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom6 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLC, noClassOWLB));
         manager.applyChanges(manager.addAxiom(ont5, axiomclass6));
         manager.applyChanges(manager.addAxiom(ont5, subClassOfAxiom6));
         OWLOntology assertion5 = manager.getOntology(IRI.create(base));
         String expectedoutputassertion5 = "unsatisfiable";
         manager.removeOntology(ont5);

         OWLOntology ont6 = null;
         try {
            ont6 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont6 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom7 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLC, classOWLB));
         manager.applyChanges(manager.addAxiom(ont6, axiomclass6));
         manager.applyChanges(manager.addAxiom(ont6, subClassOfAxiom7));
         OWLOntology assertion6 = manager.getOntology(IRI.create(base));
         String expectedoutputassertion6 = "consistent";
         manager.removeOntology(ont6);

         OWLOntology ont7 = null;
         try {
            ont7 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont7 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom8 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLC, classOWLA));
         manager.applyChanges(manager.addAxiom(ont7, axiomclass6));
         manager.applyChanges(manager.addAxiom(ont7, subClassOfAxiom8));
         OWLOntology assertion7 = manager.getOntology(IRI.create(base));
         String expectedoutputassertion7 = "consistent";
         manager.removeOntology(ont7);

         OWLOntology ont8 = null;
         try {
            ont8 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont8 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom9 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLC, noClassOWLA));
         manager.applyChanges(manager.addAxiom(ont8, axiomclass6));
         manager.applyChanges(manager.addAxiom(ont8, subClassOfAxiom9));
         OWLOntology assertion8 = manager.getOntology(IRI.create(base));
         String expectedoutputassertion8 = "consistent";
         manager.removeOntology(ont8);

         OWLOntology ont9 = null;
         try {
            ont9 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ont9 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom10 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLC, classOWLA));
         manager.applyChanges(manager.addAxiom(ont9, axiomclass6));
         manager.applyChanges(manager.addAxiom(ont9, subClassOfAxiom10));
         OWLOntology assertion9 = manager.getOntology(IRI.create(base));

         String expectedoutputassertion9 = "unsatisfiable";
         manager.removeOntology(ont9);


         LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
         hashinput.put("Assertion 1", assertion1);
         hashinput.put("Assertion 2", assertion2);
         hashinput.put("Assertion 3", assertion3);
         hashinput.put("Assertion 4", assertion4);
         hashinput.put("Assertion 5", assertion5);
         hashinput.put("Assertion 6", assertion6);
         hashinput.put("Assertion 7", assertion7);
         hashinput.put("Assertion 8", assertion8);
         hashinput.put("Assertion 9", assertion9);

         LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
         hashoutput.put("Assertion 1", expectedoutputassertion1);
         hashoutput.put("Assertion 2", expectedoutputassertion2);
         hashoutput.put("Assertion 3", expectedoutputassertion3);
         hashoutput.put("Assertion 4", expectedoutputassertion4);
         hashoutput.put("Assertion 5", expectedoutputassertion5);
         hashoutput.put("Assertion 6", expectedoutputassertion6);
         hashoutput.put("Assertion 7", expectedoutputassertion7);
         hashoutput.put("Assertion 8", expectedoutputassertion8);
         hashoutput.put("Assertion 9", expectedoutputassertion9);

         for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
            hashoutput.put(entry.getKey(), entry.getValue());
         }

         testCase.setAxiomExpectedResultAxioms(hashoutput);

         for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
            hashinput.put(entry.getKey(), entry.getValue());
         }
         testCase.setAssertionsAxioms(hashinput);
         return testCase;
      }else
         return null;
   }

   /*for participant ODP, location ODP and ObjectRole ODP*/
   public static TestCaseImpl participantODPTestExistential(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) some (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classA1 = classA+"1";
      String R = m.group(2).toString();
      String classB = "";
      classB = m.group(3).toString();
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String noClassA =  "No"+classA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];
      String classB1 = classB+"1";
      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      precond.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added */
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass classOWLB1 = dataFactory.getOWLClass(IRI.create(classB1));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLB1);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("ind1"));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("ind2"));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLNamedIndividual indOWLnoA = dataFactory.getOWLNamedIndividual(IRI.create("ind4"));
      OWLDeclarationAxiom axiomInd4 = dataFactory.getOWLDeclarationAxiom(indOWLnoA);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(noClassOWLA, indOWLnoA);
      OWLNamedIndividual indOWLB1 = dataFactory.getOWLNamedIndividual(IRI.create("ind5"));
      OWLDeclarationAxiom axiomInd5 = dataFactory.getOWLDeclarationAxiom(indOWLB1);
      OWLClassAssertionAxiom classAssertionAxiom4 = dataFactory.getOWLClassAssertionAxiom(classOWLB1, indOWLB1);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, axiomClass2));
      manager.applyChanges(manager.addAxiom(ont, axiomClass3));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom2));
      manager.applyChanges(manager.addAxiom(ont, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont, axiomInd4));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom3));
      manager.applyChanges(manager.addAxiom(ont, axiomInd5));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom4));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass2));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("ind3"));
      OWLDeclarationAxiom axiomInd3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont2, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass3));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);
      String expectedoutputassertion2 = "consistent";


      /*Assertions*/
      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      String inverse = "";
      if(R.toLowerCase().equals("isparticipantin"))
         inverse = "hasParticipant";
      else if(R.toLowerCase().equals("islocationof"))
         inverse = "hasLocation";
      else if(R.toLowerCase().equals("isroleof"))
         inverse = "hasRole";
      else if(R.toLowerCase().equals("haslocation"))
         inverse = "isLocationOf";
      else if(R.toLowerCase().equals("hasrole"))
         inverse = "isRoleOf";
      else
         inverse = "none";

      //ind 4 --> no agent
      //class organization1 classOWLB1
      OWLSubClassOfAxiom axiomsubclass4= dataFactory.getOWLSubClassOfAxiom(classOWLB1, classOWLB);

      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));
      OWLSubClassOfAxiom axiomsubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB1, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLnoA)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWLnoA);
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass4));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass5));
      manager.applyChanges(manager.addAxiom(ont3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion3 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }

      OWLSubClassOfAxiom axiomsubclass6 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLnoA, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWLnoA);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont4, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont4, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass6));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom6));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass4));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont4);
      String expectedoutputassertion4 = "consistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);


      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*TEST*/
   public static TestCaseImpl participantODPTestUniversal(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) only (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classA1 = classA+"1";
      String R = m.group(2).toString();
      String classB = "";
      classB = m.group(3).toString();
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String noClassA =  "No"+classA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];
      String classB1 = classB+"1";

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      precond.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass classOWLB1 = dataFactory.getOWLClass(IRI.create(classB1));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLB1);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("ind1"));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("ind2"));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLNamedIndividual indOWLnoA = dataFactory.getOWLNamedIndividual(IRI.create("ind4"));
      OWLDeclarationAxiom axiomInd4 = dataFactory.getOWLDeclarationAxiom(indOWLnoA);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(noClassOWLA, indOWLnoA);
      OWLNamedIndividual indOWLB1 = dataFactory.getOWLNamedIndividual(IRI.create("ind5"));
      OWLDeclarationAxiom axiomInd5 = dataFactory.getOWLDeclarationAxiom(indOWLB1);
      OWLClassAssertionAxiom classAssertionAxiom4 = dataFactory.getOWLClassAssertionAxiom(classOWLB1, indOWLB1);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, axiomClass2));
      manager.applyChanges(manager.addAxiom(ont, axiomClass3));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom2));
      manager.applyChanges(manager.addAxiom(ont, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont, axiomInd4));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom3));
      manager.applyChanges(manager.addAxiom(ont, axiomInd5));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom4));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass2));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";
      manager.removeOntology(ont1);



      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("ind3"));
      OWLDeclarationAxiom axiomInd3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont2, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass3));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);
      String expectedoutputassertion2 = "inconsistent";

      /*Assertions*/
      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      String inverse = "";
      if(R.equals("isparticipantin"))
         inverse = "hasParticipant";
      else if(R.equals("islocationof"))
         inverse = "hasLocation";
      else if(R.equals("isroleof"))
         inverse = "hasRole";
      else if(R.equals("haslocation"))
         inverse = "isLocationOf";
      else if(R.equals("hasrole"))
         inverse = "isRoleOf";
      else
         inverse = "none";





      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));
      OWLSubClassOfAxiom axiomsubclass4= dataFactory.getOWLSubClassOfAxiom(classOWLB1, classOWLB);
      OWLSubClassOfAxiom axiomsubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLnoA)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWLnoA);
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass4));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass5));
      manager.applyChanges(manager.addAxiom(ont3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion3 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }

      OWLSubClassOfAxiom axiomsubclass6 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLnoA, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWLnoA);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont4, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont4, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass6));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass4));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom6));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont4);
      String expectedoutputassertion4 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);


      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for subclass + OP Test*/
   public static TestCaseImpl subClassOPTest(String purpose,  TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (.*) that (.*) some (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classAnoSymb = classA.replace(">","").replace("<","");
      String noClassA =  "No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1];
      String classA1 =  classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"1";

      String classB = m.group(2).toString();
      String classB1 =  classB.split("(#|\\/)")[classB.split("(#)").length-1]+"1";
      String noClassB =  "No"+classB.replace(">","").replace("<","").replace(">","").replace("<","").split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String R = m.group(3).toString();
      String classC = "";
      classC = m.group(4).toString();

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Class(<"+classB+">)");
      precond.add("Property(<"+R+">)");
      precond.add("Class(<"+classC+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass4 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass7 = dataFactory.getOWLDeclarationAxiom(classOWLA1);

      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass2));
      manager.applyChanges(manager.addAxiom(ont, axiomClass3));
      manager.applyChanges(manager.addAxiom(ont, axiomClass4));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom2));
      manager.applyChanges(manager.addAxiom(ont, axiomClass7));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /************SUBCLASS TEST***************/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectIntersectionOf(noClassOWLA, classOWLB));
      manager.applyChanges(manager.addAxiom(ont1, subClassOfAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));

      String expectedoutputassertion1  = "consistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectIntersectionOf(classOWLA, noClassOWLB));
      manager.applyChanges(manager.addAxiom(ont2, subClassOfAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion2  = "unsatisfiable";
      manager.removeOntology(ont2);

      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectIntersectionOf(classOWLA, classOWLB));
      manager.applyChanges(manager.addAxiom(ont3, subClassOfAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion3  = "consistent";
      manager.removeOntology(ont3);
      /************OP TEST***************/
      /*Axioms to be added*/
      /*Assertions*/
      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("ind1"));
      OWLNamedIndividual indOWLC = dataFactory.getOWLNamedIndividual(IRI.create("ind2"));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWLC);
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass noClassOWLC = dataFactory.getOWLClass(IRI.create("no"+classC));
      OWLEquivalentClassesAxiom equivalentClassesAxiom3 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLC, dataFactory.getOWLObjectComplementOf(classOWLC));
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLC, indOWLC);
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLC)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLC);
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass2));
      manager.applyChanges(manager.addAxiom(ont4, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont4, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont4, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont4, equivalentClassesAxiom3));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion4 = "inconsistent";
      manager.removeOntology(ont4);

      OWLOntology ont5 = null;
      try {
         ont5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont5 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("ind3"));
      OWLDeclarationAxiom axiomInd3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLC, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLC);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont5, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont5, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont5, axiomsubclass3));
      manager.applyChanges(manager.addAxiom(ont5, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont5, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont5, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont5, objectPropertyAssertionAxiom3));
      manager.applyChanges(manager.addAxiom(ont4, equivalentClassesAxiom3));
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ont5);

      String expectedoutputassertion5 = "consistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);
      hashinput.put("Assertion 5", assertion5);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);
      hashoutput.put("Assertion 5",expectedoutputassertion5);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   /*for  cardinality + OP*/
   public static TestCaseImpl cardinalityOPTest(String purpose, String type, TestCaseImpl testCase){
      Pattern p1 = Pattern.compile("(.*) subclassof (.*) min (\\d+) (.*) and (.*) subclassof (.*) some (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p1.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classA1 = classA.replace(">","").replace("<","")+"1";
      String R1 = m.group(2).toString();
      String classB = m.group(4).toString();
      String classB1 = classB.replace(">","").replace("<","")+"1";
      String R2 = m.group(6).toString();
      String classC = m.group(7).toString();
      String noClassC =  "No"+classC.replace(">","").replace("<","").replace(">","").replace("<","").split("(#|\\/)")[classC.split("(#|\\/)").length-1];
      Integer num = Integer.parseInt(m.group(3).toString().replace(" ",""));


      String indB= "<individual001>";
      String indNoC = "<individual002>";
      String ind3 = "<individual003>";

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R1+">)");
      precond.add("Property(<"+R2+">)");
      precond.add("Class(<"+classB+">)");
      precond.add("Class(<"+classC+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/

      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLClass classOWLB1 = dataFactory.getOWLClass(IRI.create(classB1));
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass noClassOWLC = dataFactory.getOWLClass(IRI.create(noClassC));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLC);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLC,
              dataFactory.getOWLObjectComplementOf(classOWLC));
      OWLNamedIndividual indOWLB1 = dataFactory.getOWLNamedIndividual(IRI.create("indB1"));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLB1);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLB1, indOWLB1);
      OWLNamedIndividual indOWLnoC = dataFactory.getOWLNamedIndividual(IRI.create("indC"));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWLnoC);
      OWLClassAssertionAxiom classAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(noClassOWLC, indOWLnoC);
      manager.applyChanges(manager.addAxiom(ont, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom3));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);
        /*String preparation =  "\n                       \t"+classA+" rdf:type owl:Class.\n "+
                "                       \t"+classA1+" rdfs:subClassOf "+classA+".\n "+
                "                       "+noClassC+" rdf:type owl:Class .\n" +
                "                       "+noClassC+" owl:complementOf " + classC+" .\n"+
                "                       "+indB+" rdf:type  owl:NamedIndividual, "+ classB1+".\n"+
                "                       "+indNoC + " rdf:type owl:NamedIndividual, "+noClassC+".\n" ;

        testCase.setPreparation(preparation);*/

      /*A1 R max [num-1] B
       * */
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R1));
      OWLSubClassOfAxiom subClassOfAxiom1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectMaxCardinality(num-1,prop, classOWLB));
      manager.applyChanges(manager.addAxiom(ont1, subClassOfAxiom1));
      manager.applyChanges(manager.addAxiom(ont1, subClassOfAxiom2));
        /*String assertion1 =   "\n                       \t"+classA1+"    rdfs:subClassOf "+classA+" ,\n" +
                "                                                                     "+"[ rdf:type owl:Restriction ;\n" +
                "                                                                     "+ "  owl:onProperty "+R1+" ;\n" +
                "                                                                     "+ "  owl:maxQualifiedCardinality \""+(num-1)+"\"^^xsd:nonNegativeInteger ;\n" +
                "                                                                     "+ "  owl:onClass "+classB+"\n" +
                "                                                                     "+ "] .\n"+
                "                                                                     "+ R1+ " a owl:ObjectProperty.\n";*/
      String expectedoutputassertion1 = "unsatisfiable";
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont1);

      /*A1 R min [num+1] B
       * */
      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectMinCardinality(num+1,prop, classOWLB));
      manager.applyChanges(manager.addAxiom(ont2, subClassOfAxiom1));
      manager.applyChanges(manager.addAxiom(ont2, subClassOfAxiom3));
       /* String assertion2 =  "\n                       \t"+classA1+" rdfs:subClassOf "+classA+" ,\n" +
                "                                                                     "+ "[ rdf:type owl:Restriction ;\n" +
                "                                                                     "+ "  owl:onProperty "+R1+" ;\n" +
                "                                                                     "+ "  owl:minQualifiedCardinality \""+(num+1)+"\"^^xsd:nonNegativeInteger ;\n" +
                "                                                                     "+"  owl:onClass "+classB+"\n" +
                "                                                                     "+ "] .\n"+
                "                                                                     "+ R1+ " a owl:ObjectProperty.\n";*/
      String expectedoutputassertion2  = "consistent";
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);

      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom4 = null;
      subClassOfAxiom4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectMaxCardinality(num,prop, classOWLB));
      manager.applyChanges(manager.addAxiom(ont3, subClassOfAxiom1));
      manager.applyChanges(manager.addAxiom(ont3, subClassOfAxiom4));


           /* assertion3 = "\n                       \t" + classA1 + " rdfs:subClassOf " + classA + " ,\n" +
                    "                                                                     " + "[ rdf:type owl:Restriction ;\n" +
                    "                                                                     " + "  owl:onProperty " + R1 + " ;\n" +
                    "                                                                     " + "  owl:maxQualifiedCardinality \"" + (num) + "\"^^xsd:nonNegativeInteger ;\n" +
                    "                                                                     " + "  owl:onClass " + classB + "\n" +
                    "                                                                     " + "] .\n" +
                    "                                                                     " + R1 + " a owl:ObjectProperty.\n";*/

      String expectedoutputassertion3 = "consistent";
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont3);



      // String assertion4 =  "\n                       "+indB +" "+R2+ " "+indNoC+".\n";
      // String expectedoutputassertion4 = "inconsistent";

      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }
      OWLObjectProperty prop2 = dataFactory.getOWLObjectProperty(IRI.create(R2));

      OWLSubClassOfAxiom subClassOfAxiom5 = dataFactory.getOWLSubClassOfAxiom(classOWLB1,classOWLB);
      OWLSubClassOfAxiom subClassOfAxiom6 = dataFactory.getOWLSubClassOfAxiom(classOWLB1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectOneOf(indOWLnoC)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop2,
              indOWLB1, indOWLnoC);

      manager.applyChanges(manager.addAxiom(ont4, subClassOfAxiom1));
      manager.applyChanges(manager.addAxiom(ont4, subClassOfAxiom5));
      manager.applyChanges(manager.addAxiom(ont4, subClassOfAxiom6));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom));
        /*String assertion4 =                "                       "+classB1+" rdfs:subClassOf "+classB+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R2+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+indNoC+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n"+
                "                       "+indB +" "+R2+ " "+indNoC+".\n";*/
      String expectedoutputassertion4  = "inconsistent";
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont4);

      OWLOntology ont5 = null;
      try {
         ont5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont5 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create(ind3));
      OWLSubClassOfAxiom subClassOfAxiom8 = dataFactory.getOWLSubClassOfAxiom(classOWLB1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectOneOf(indOWLnoC, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop2,
              indOWLB1, indOWLnoC);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop2,
              indOWLB1, indOWL3);

      manager.applyChanges(manager.addAxiom(ont5, subClassOfAxiom5));
      manager.applyChanges(manager.addAxiom(ont5, subClassOfAxiom8));
      manager.applyChanges(manager.addAxiom(ont5, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont5, objectPropertyAssertionAxiom3));
        /*String assertion5 =                "                       "+classB1+" rdfs:subClassOf "+classB+",\n" +
                "                       "+"                    [ rdf:type owl:Restriction ;\n" +
                "                       "+"                      owl:onProperty "+R2+" ;\n" +
                "                       "+"                      owl:allValuesFrom [ rdf:type owl:Class ;\n" +
                "                       "+"                                          owl:oneOf ( "+indNoC+"\n" +
                "                       "+"                                                      "+ind3+"\n" +
                "                       "+"                                                    )\n" +
                "                       "+"                                        ]\n" +
                "                       "+"                    ] .\n"+
                "                       "+indB +" "+R2+ " "+indNoC+".\n"+
                "                       "+indB +" "+R2+ " "+ind3+".\n";*/


      String expectedoutputassertion5 ="";
      if(purpose.contains("only"))
         expectedoutputassertion5 = "inconsistent";
      else
         expectedoutputassertion5 = "consistent";
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ont5);


      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);
      hashinput.put("Assertion 5", assertion5);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);
      hashoutput.put("Assertion 5",expectedoutputassertion5);


      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;

   }

   /*for class definition*/
   public static TestCaseImpl classDefinitionTest(String purpose, TestCaseImpl testCase) throws OWLOntologyCreationException {

      Pattern p = Pattern.compile("(.*) type class",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();


      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      testCase.getPrecondition().addAll(precond);
      /*There are no axioms to be added*/
      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      testCase.setPreparationaxioms(manager.createOntology(IRI.create(base)));
      return testCase;
   }

   /*for property definition*/
   public static TestCaseImpl propertyDefinitionTest(String purpose, TestCaseImpl testCase) throws OWLOntologyCreationException {

      Pattern p = Pattern.compile("(.*) type property",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String propertyA = m.group(1).toString();

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Property(<"+propertyA+">)");
      testCase.getPrecondition().addAll(precond);
      /*There are no axioms to be added*/
      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      testCase.setPreparationaxioms(manager.createOntology(IRI.create(base)));
      return testCase;
   }

   /*--------------------------------------------------*/
   /*for participant ODP, location ODP and ObjectRole ODP*/
   public static TestCaseImpl coParticipantODPTestExistential(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) and (.*) subclassof (hascoparticipant|iscoparticipantin|cooparticipates) some (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classA1 = classA+"1";
      String R = m.group(3).toString();
      String classB = "";
      classB = m.group(4).toString();
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String classC = m.group(2).toString();
      String classC1 = classC+"1";

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      precond.add("Class(<"+classB+">)");
      precond.add("Class(<"+classC+">)");
      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added */
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("ind1"));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("ind2"));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass classOWLC1 = dataFactory.getOWLClass(IRI.create(classC1));
      OWLDeclarationAxiom axiomclassC1 = dataFactory.getOWLDeclarationAxiom(classOWLC1);
      OWLNamedIndividual indOWLC1 = dataFactory.getOWLNamedIndividual(IRI.create("indC"));
      OWLDeclarationAxiom axiomIndC1 = dataFactory.getOWLDeclarationAxiom(indOWLC1);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont, axiomclassC1));
      manager.applyChanges(manager.addAxiom(ont, axiomIndC1));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      /*Participant 1*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass2));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("ind3"));
      OWLDeclarationAxiom axiomInd3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont2, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass3));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);
      String expectedoutputassertion2 = "consistent";


      /*Assertions*/
      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      String inverse = "";
      if(R.equals("isparticipantin"))
         inverse = "hasParticipant";
      else if(R.equals("islocationof"))
         inverse = "hasLocation";
      else if(R.equals("isroleof"))
         inverse = "hasRole";
      else if(R.equals("haslocation"))
         inverse = "isLocationOf";
      else if(R.equals("hasrole"))
         inverse = "isRoleOf";
      else
         inverse = "none";

      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));
      OWLSubClassOfAxiom axiomsubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass4));
      manager.applyChanges(manager.addAxiom(ont3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion3 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }

      OWLSubClassOfAxiom axiomsubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);
      manager.applyChanges(manager.addAxiom(ont4, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont4, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass5));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom6));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont4);
      String expectedoutputassertion4 = "consistent";

      /*Participant 2*/
      OWLOntology ont5 = null;
      try {
         ont5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont5 = null;
      }
      OWLSubClassOfAxiom axiomsubclassC1= dataFactory.getOWLSubClassOfAxiom(classOWLC1, classOWLC);
      OWLSubClassOfAxiom axiomsubclassC12 = dataFactory.getOWLSubClassOfAxiom( classOWLC1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWLB);
      manager.applyChanges(manager.addAxiom(ont5, axiomsubclassC1));
      manager.applyChanges(manager.addAxiom(ont5, axiomsubclassC12));
      manager.applyChanges(manager.addAxiom(ont5, objectPropertyAssertionAxiomC1));
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion5 = "inconsistent";
      manager.removeOntology(ont5);

      OWLOntology ont6 = null;
      try {
         ont6 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont6 = null;
      }

      OWLSubClassOfAxiom axiomsubclassC13 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC12 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC13 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont6, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont6, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont6, axiomsubclassC13));
      manager.applyChanges(manager.addAxiom(ont6, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont6, objectPropertyAssertionAxiomC12));
      manager.applyChanges(manager.addAxiom(ont6, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont6, objectPropertyAssertionAxiomC13));
      OWLOntology assertion6 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont6);
      String expectedoutputassertion6 = "consistent";

      /*Assertions*/
      OWLOntology ont7 = null;
      try {
         ont7 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont7 = null;
      }

      OWLSubClassOfAxiom axiomsubclassC14 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLC1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC14 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLC1);
      manager.applyChanges(manager.addAxiom(ont7, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont7, axiomsubclassC14));
      manager.applyChanges(manager.addAxiom(ont7, objectPropertyAssertionAxiomC14));
      OWLOntology assertion7 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion7 = "inconsistent";
      manager.removeOntology(ont7);

      OWLOntology ont8 = null;
      try {
         ont8 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont8 = null;
      }

      OWLSubClassOfAxiom axiomsubclassC15 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLC1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC15 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLC1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC16 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);
      manager.applyChanges(manager.addAxiom(ont8, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont8, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont8, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont8, objectPropertyAssertionAxiomC15));
      manager.applyChanges(manager.addAxiom(ont8, axiomsubclassC15));
      manager.applyChanges(manager.addAxiom(ont8, objectPropertyAssertionAxiomC16));
      OWLOntology assertion8 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont8);
      String expectedoutputassertion8 = "consistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);
      hashinput.put("Assertion 5", assertion5);
      hashinput.put("Assertion 6", assertion6);
      hashinput.put("Assertion 7", assertion7);
      hashinput.put("Assertion 8", assertion8);


      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);
      hashoutput.put("Assertion 5",expectedoutputassertion5);
      hashoutput.put("Assertion 6",expectedoutputassertion6);
      hashoutput.put("Assertion 7",expectedoutputassertion7);
      hashoutput.put("Assertion 8",expectedoutputassertion8);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }

   public static TestCaseImpl coParticipantODPTestUniversal(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) and (.*) subclassof (hascoparticipant|iscoparticipantin|cooparticipates) only (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String classA1 = classA+"1";
      String R = m.group(3).toString();
      String classB = "";
      classB = m.group(4).toString();
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String classC = m.group(2).toString();
      String classC1 = classC+"1";

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+R+">)");
      precond.add("Class(<"+classB+">)");
      precond.add("Class(<"+classC+">)");
      testCase.getPrecondition().addAll(precond);


      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("ind1"));
      OWLDeclarationAxiom axiomInd1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("ind2"));
      OWLDeclarationAxiom axiomInd2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass classOWLC1 = dataFactory.getOWLClass(IRI.create(classC1));
      OWLDeclarationAxiom axiomclassC1 = dataFactory.getOWLDeclarationAxiom(classOWLC1);
      OWLNamedIndividual indOWLC1 = dataFactory.getOWLNamedIndividual(IRI.create("indC"));
      OWLDeclarationAxiom axiomIndC1 = dataFactory.getOWLDeclarationAxiom(indOWLC1);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClass1));
      manager.applyChanges(manager.addAxiom(ont, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd1));
      manager.applyChanges(manager.addAxiom(ont, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ont, axiomclassC1));
      manager.applyChanges(manager.addAxiom(ont, axiomIndC1));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont, axiomInd2));
      manager.applyChanges(manager.addAxiom(ont, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      /*Assertions*/
      /*Participant 1*/
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }
      OWLSubClassOfAxiom axiomsubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomsubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont1, axiomsubclass2));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion1 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont2 = null;
      try {
         ont2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("ind3"));
      OWLDeclarationAxiom axiomInd3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomsubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont2, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont2, axiomsubclass3));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont2);
      String expectedoutputassertion2 = "inconsistent";

      /*Assertions*/
      OWLOntology ont3 = null;
      try {
         ont3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont3 = null;
      }
      String inverse = "";
      if(R.equals("isparticipantin"))
         inverse = "hasParticipant";
      else if(R.equals("islocationof"))
         inverse = "hasLocation";
      else if(R.equals("isroleof"))
         inverse = "hasRole";
      else if(R.equals("haslocation"))
         inverse = "isLocationOf";
      else if(R.equals("hasrole"))
         inverse = "isRoleOf";
      else
         inverse = "none";

      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));

      OWLSubClassOfAxiom axiomsubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLA1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont3, axiomsubclass4));
      manager.applyChanges(manager.addAxiom(ont3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion3 = "inconsistent";
      manager.removeOntology(ont1);

      OWLOntology ont4 = null;
      try {
         ont4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont4 = null;
      }

      OWLSubClassOfAxiom axiomsubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);
      manager.applyChanges(manager.addAxiom(ont4, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont4, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ont4, axiomsubclass5));
      manager.applyChanges(manager.addAxiom(ont4, objectPropertyAssertionAxiom6));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont4);
      String expectedoutputassertion4 = "inconsistent";


      /*Participant 2*/
      OWLOntology ont5 = null;
      try {
         ont5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont5 = null;
      }
      OWLSubClassOfAxiom axiomsubclassC1= dataFactory.getOWLSubClassOfAxiom(classOWLC1, classOWLC);
      OWLSubClassOfAxiom axiomsubclassC12 = dataFactory.getOWLSubClassOfAxiom( classOWLC1, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWLB);
      manager.applyChanges(manager.addAxiom(ont5, axiomsubclassC1));
      manager.applyChanges(manager.addAxiom(ont5, axiomsubclassC12));
      manager.applyChanges(manager.addAxiom(ont5, objectPropertyAssertionAxiomC1));
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion5 = "inconsistent";
      manager.removeOntology(ont5);

      OWLOntology ont6 = null;
      try {
         ont6 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont6 = null;
      }

      OWLSubClassOfAxiom axiomsubclassC13 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC12 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC13 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWL3);
      manager.applyChanges(manager.addAxiom(ont6, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont6, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont6, axiomsubclassC13));
      manager.applyChanges(manager.addAxiom(ont6, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont6, objectPropertyAssertionAxiomC12));
      manager.applyChanges(manager.addAxiom(ont6, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ont6, objectPropertyAssertionAxiomC13));
      OWLOntology assertion6 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont6);
      String expectedoutputassertion6 = "inconsistent";

      /*Assertions*/
      OWLOntology ont7 = null;
      try {
         ont7 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont7 = null;
      }

      OWLSubClassOfAxiom axiomsubclassC14 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectSomeValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLC1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC14 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLC1);
      manager.applyChanges(manager.addAxiom(ont7, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont7, axiomsubclassC14));
      manager.applyChanges(manager.addAxiom(ont7, objectPropertyAssertionAxiomC14));
      OWLOntology assertion7 = manager.getOntology(IRI.create(base));
      String expectedoutputassertion7 = "inconsistent";
      manager.removeOntology(ont7);

      OWLOntology ont8 = null;
      try {
         ont8 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont8 = null;
      }

      OWLSubClassOfAxiom axiomsubclassC15 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLC1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC15 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLC1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC16 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);
      manager.applyChanges(manager.addAxiom(ont8, axiomInd3));
      manager.applyChanges(manager.addAxiom(ont8, axiomsubclass1));
      manager.applyChanges(manager.addAxiom(ont8, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont8, objectPropertyAssertionAxiomC15));
      manager.applyChanges(manager.addAxiom(ont8, axiomsubclassC15));
      manager.applyChanges(manager.addAxiom(ont8, objectPropertyAssertionAxiomC16));
      OWLOntology assertion8 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont8);
      String expectedoutputassertion8 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion1);
      hashinput.put("Assertion 2", assertion2);
      hashinput.put("Assertion 3", assertion3);
      hashinput.put("Assertion 4", assertion4);
      hashinput.put("Assertion 5", assertion5);
      hashinput.put("Assertion 6", assertion6);
      hashinput.put("Assertion 7", assertion7);
      hashinput.put("Assertion 8", assertion8);


      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1",expectedoutputassertion1);
      hashoutput.put("Assertion 2",expectedoutputassertion2);
      hashoutput.put("Assertion 3",expectedoutputassertion3);
      hashoutput.put("Assertion 4",expectedoutputassertion4);
      hashoutput.put("Assertion 5",expectedoutputassertion5);
      hashoutput.put("Assertion 6",expectedoutputassertion6);
      hashoutput.put("Assertion 7",expectedoutputassertion7);
      hashoutput.put("Assertion 8",expectedoutputassertion8);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      return testCase;
   }



   /*****************************************************/
   /*Tests for instances*/
   /*for domain*/
   public static TestCaseImpl domainRangeTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) (.*) (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String propertyA = m.group(2).toString();
      String classB = m.group(3).toString();

      String classAnoSymb = classA.replace(">","").replace("<","");
      String noClassA =  "No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"";

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+propertyA+">)");
      precond.add("Class(<"+classB+">)");

      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLDeclarationAxiom axiomClassB = dataFactory.getOWLDeclarationAxiom(classOWLB);
      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      manager.applyChanges(manager.addAxiom(ont, axiomClassB));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      String classAwithouturi = noClassA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];

      /*Assertions*/
      String ind1= classAwithouturi.toLowerCase().replace(">","").replace("<","")+"001";
      String ind2 = classAwithouturi.toLowerCase().replace(">","").replace("<","")+"002";
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }


      OWLNamedIndividual indClassA = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomIndClassA = dataFactory.getOWLDeclarationAxiom(indClassA);
      OWLClassAssertionAxiom indAClassAAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA, indClassA);

      OWLNamedIndividual indClassB = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLDeclarationAxiom axiomIndClassB = dataFactory.getOWLDeclarationAxiom(indClassB);
      OWLClassAssertionAxiom indAClassBAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLB, indClassB);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(propertyA));

      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indClassA, indClassB);

      manager.applyChanges(manager.addAxiom(ont1, axiomIndClassA));
      manager.applyChanges(manager.addAxiom(ont1, indAClassAAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont1, axiomIndClassB));
      manager.applyChanges(manager.addAxiom(ont1, indAClassBAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont1, objectPropertyAssertionAxiom2));

      OWLOntology assertion = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont1);

      String expectedoutputassertion = "consistent";


      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1", expectedoutputassertion);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      testCase.setType("individuals");
      return testCase;

   }

   public static TestCaseImpl domainRangeTestDP(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) (.*) (xsd:string|xsd:float|xsd:integer|rdfs:literal|xsd:datetime|xsd:datetimestamp|string|float|integer|datetime|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|literal|rdfs:literal)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).toString();
      String propertyA = m.group(2).toString();
      String datatypeB = m.group(3).toLowerCase().toString();
      String classAnoSymb = classA.replace(">","").replace("<","");
      String noClassA =  "No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"";

      /*Preconditions*/
      ArrayList<String> precond = new ArrayList<>();
      precond.add("Class(<"+classA+">)");
      precond.add("Property(<"+propertyA+">)");

      testCase.getPrecondition().addAll(precond);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = null;
      try {
         ont = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);

      manager.applyChanges(manager.addAxiom(ont, axiomClass));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ont);

      String classAwithouturi = noClassA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];

      /*Assertions*/
      String ind1= classAwithouturi.toLowerCase().replace(">","").replace("<","")+"001";
      String ind2 = classAwithouturi.toLowerCase().replace(">","").replace("<","")+"002";
      OWLOntology ont1 = null;
      try {
         ont1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ont1 = null;
      }


      OWLNamedIndividual indClassA = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomIndClassA = dataFactory.getOWLDeclarationAxiom(indClassA);
      OWLClassAssertionAxiom indAClassAAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA, indClassA);

      OWLNamedIndividual indClassB = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(propertyA));

      OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom;
      if(datatypeB.contains("string") || datatypeB.contains("uri") || datatypeB.contains("literal") || datatypeB.contains("rational")  ) {
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, "Example text");
      }else if(datatypeB.contains("boolean")) {
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, true);
      }else if(datatypeB.contains("integer")){
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, 1);
      }else if(datatypeB.contains("float") || datatypeB.contains("double") ){
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, 1.0);
      } else if(datatypeB.contains("datetime")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("2001-10-26T21:32:52.12679",
                 OWL2Datatype.XSD_DATE_TIME);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, dataLiteral);
      } else if(datatypeB.contains("datetimestamp")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("2001-10-26T21:32:52.12679",
                 OWL2Datatype.XSD_DATE_TIME_STAMP);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, dataLiteral);
      } else{
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, "Example text");

      }


      manager.applyChanges(manager.addAxiom(ont1, axiomIndClassA));
      manager.applyChanges(manager.addAxiom(ont1, indAClassAAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ont1, dataPropertyAssertionAxiom));

      OWLOntology assertion = manager.getOntology(IRI.create(base));
      manager.removeOntology(ont1);

      String expectedoutputassertion = "consistent";


      LinkedHashMap<String, OWLOntology> hashinput = new LinkedHashMap();
      hashinput.put("Assertion 1", assertion);

      LinkedHashMap<String, String> hashoutput = new LinkedHashMap();
      hashoutput.put("Assertion 1", expectedoutputassertion);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashoutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashoutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashinput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashinput);
      testCase.setType("individuals");
      return testCase;

   }



}