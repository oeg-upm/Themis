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
      String classA = m.group(1);
      String classA1 = classA+"1";

      String P = m.group(2);
      String individual = m.group(3);

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+P+">)");
      precondition.add("Individual(<"+individual+">)");
      testCase.getPrecondition().addAll(precondition);

      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
          ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
          ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      /*Preparation*/
      OWLNamedIndividual individual1 = dataFactory.getOWLNamedIndividual(IRI.create(individual));
      OWLNamedIndividual individual2 = dataFactory.getOWLNamedIndividual(IRI.create(base+"individual2"));
      OWLClass class1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(class1);
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(individual1);
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(individual2);

      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology,  dataFactory.getOWLDifferentIndividualsAxiom(individual1, individual2)));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(class1, classOWLA);
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass1));
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(P));
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( class1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(individual2)));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass2));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "unsatisfiable";
      manager.removeOntology(ontology1);

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);
      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return null;
   }

   /*cardinality for DP*/
   public static TestCaseImpl cardinalityDP(String purpose, String type, TestCaseImpl testCase){
      Pattern p1 = Pattern.compile("(.*) subclassof (.*) (min|max|exactly) (\\d+) (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|xsd:double|double|xsd:long|long)", Pattern.CASE_INSENSITIVE);
      Matcher m = p1.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);

      String classA1 = classA+"1";
      String R = m.group(2);

      String datatype = "";
      datatype = m.group(5);
      if( datatype.equals("string") || datatype.equals("integer") || datatype.equals("double")  || datatype.equals("long") ||  datatype.equals("float") || datatype.equals("boolean") || datatype.equals("anyuri"))
         datatype = "<http://www.w3.org/2001/XMLSchema#"+datatype+">";
      else if(datatype.equals("rational")){
         datatype = "<http://www.w3.org/2002/07/owl#"+datatype+">";
      }

      Integer num = Integer.parseInt(m.group(4).toString());
      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+R+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/

      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);

      manager.addAxiom(ontology,axiomClass);
      manager.applyChanges(manager.addAxiom(ontology, axiomSubclass1));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(R));
      OWLDatatype dp = dataFactory.getOWLDatatype(IRI.create(datatype));
      OWLSubClassOfAxiom axiomSubclass4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLDataMaxCardinality(num-1, prop, dp));
      OWLDeclarationAxiom axiomDeclaration1 = dataFactory.getOWLDeclarationAxiom(dp);

      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass4));
      manager.applyChanges(manager.addAxiom(ontology1, axiomDeclaration1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutputAssertion1 = "";
      if (type == "min" || type == "exactly")
         expectedOutputAssertion1 = "unsatisfiable";
      else
         expectedOutputAssertion1 = "consistent";

      manager.removeOntology(ontology1);
      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLSubClassOfAxiom axiomSubclass5 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLDataMinCardinality(num+1, prop, dp));

      manager.applyChanges(manager.addAxiom(ontology2, axiomDeclaration1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass5));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      String expectedOutputAssertion2 = "";
      if (type == "max" || type == "exactly")
         expectedOutputAssertion2 = "unsatisfiable";
      else
         expectedOutputAssertion2 = "consistent";

      manager.removeOntology(ontology2);

      /*Assertion 3 Problema de memoria con Pellet*/
     /* OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      OWLSubClassOfAxiom axiomSubclass6 = null;

      if(type == "max") {
         axiomSubclass6 = dataFactory.getOWLSubClassOfAxiom(classOWLA, dataFactory.getOWLDataMinCardinality(num, prop, dp) );
      }else{
         axiomSubclass6 = dataFactory.getOWLSubClassOfAxiom( dataFactory.getOWLDataMaxCardinality(num, prop, dp), classOWLA);
      }
      manager.applyChanges(manager.addAxiom(ontology3, axiomDeclaration1));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass6));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedOutputAssertion3 = "consistent";
      */

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
     // hashInput.put("Assertion 3", assertion3);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutputAssertion1);
      hashOutput.put("Assertion 2",expectedOutputAssertion2);
    //  hashOutput.put("Assertion 3",expectedOutputAssertion3);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;

   }

   /*for the generation of an individual of a given class*/
   public static TestCaseImpl typeTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) type (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String individual = m.group(1);
      String classA = m.group(2);
      String noClassA =  "No"+classA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Individual(<"+individual+">)");
      precondition.add("Class(<"+classA+">)");
      testCase.getPrecondition().addAll(precondition);

      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      /*Axioms to be added*/
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));

      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLNamedIndividual indOWLA = dataFactory.getOWLNamedIndividual(IRI.create(individual));
      OWLDeclarationAxiom axiomIndividualividual1 = dataFactory.getOWLDeclarationAxiom(indOWLA);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(noClassOWLA, indOWLA);

      manager.applyChanges(manager.addAxiom(ontology1, axiomIndividualividual1));
      manager.applyChanges(manager.addAxiom(ontology1, classAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));

      String expectedOutAssertion1 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for the generation of symmetry*/
   public static TestCaseImpl symmetryTest(String purpose, TestCaseImpl testCase){ /*Check*/
      Pattern p = Pattern.compile("(.*) characteristic symmetricproperty",Pattern.CASE_INSENSITIVE );
      Matcher m = p.matcher(purpose);
      String classA;
      String classB;
      String relation;
      String classA1;

      /*Generation of classes*/
      m.find();
      classA = "classA";
      classA1 = classA+"1";
      relation = m.group(1);
      classB = "classB";
      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Property(<"+relation+">)\n");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      String individual001= "individual001";
      String individual002 = "individual002";
      String individua003 = "individual003";

      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLNamedIndividual individualOWL1 = dataFactory.getOWLNamedIndividual(IRI.create(individual001));
      OWLNamedIndividual individualOWL2 = dataFactory.getOWLNamedIndividual(IRI.create(individual002));
      OWLNamedIndividual individualOWL3 = dataFactory.getOWLNamedIndividual(IRI.create(individua003));
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClassAssertionAxiom owlClassAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(classOWLB, individualOWL2);
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(relation));
      OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(individualOWL2)));
      OWLDifferentIndividualsAxiom differentIndividualsAxiom1 = dataFactory.getOWLDifferentIndividualsAxiom(individualOWL1, individualOWL2);
      OWLDifferentIndividualsAxiom differentIndividualsAxiom2 = dataFactory.getOWLDifferentIndividualsAxiom(individualOWL3, individualOWL2);
      OWLClassAssertionAxiom owlClassAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, individualOWL1);
      OWLClassAssertionAxiom owlClassAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(classOWLB, individualOWL3);

      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(classOWLB)));
      manager.applyChanges(manager.addAxiom(ontology, owlClassAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(classOWLA1)));
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(classOWLA)));
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(prop)));
      manager.applyChanges(manager.addAxiom(ontology, subClassOfAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, subClassOfAxiom2));
      manager.applyChanges(manager.addAxiom(ontology,  differentIndividualsAxiom1));
      manager.applyChanges(manager.addAxiom(ontology,  differentIndividualsAxiom2));
      manager.applyChanges(manager.addAxiom(ontology,  owlClassAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology,  owlClassAssertionAxiom3));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, individualOWL1,individualOWL2);
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "consistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, individualOWL3,individualOWL1);
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);
      String expectedOutAssertion2 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*TODO*/
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
         classA = m.group(1).replace(" ","");
         classA1 = classA+"1";
         R = "coparticipateswith";
         classB = m.group(2).replace(" ","");
      }else{
         classA = m.group(1).replace(" ","");
         classA1 = classA+"1";
         R = m.group(2).replace(" ","");
         classB = m.group(4).replace(" ","");
      }


      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)\n");
      precondition.add("Property(<"+R+">)\n");
      precondition.add("Class(<"+classB+">)\n");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/

      String ind1= "individual001";
      String ind2 = "individua002";
      String ind3 = "individua003";

      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
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

      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(classOWLB)));
      manager.applyChanges(manager.addAxiom(ontology, owlClassAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(classOWLA1)));
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(classOWLA)));
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(prop)));
      manager.applyChanges(manager.addAxiom(ontology, subClassOfAxiom));
      manager.applyChanges(manager.addAxiom(ontology, subClassOfAxiom2));
      manager.applyChanges(manager.addAxiom(ontology,  differentIndividualsAxiom));
      manager.applyChanges(manager.addAxiom(ontology,  differentIndividualsAxiom2));
      manager.applyChanges(manager.addAxiom(ontology,  owlClassAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology,  owlClassAssertionAxiom3));

      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, individualOWL1,individualOWL2);
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "consistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, individualOWL3,individualOWL1);
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);
      String expectedOutAssertion2 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for domain*/
   public static TestCaseImpl domainTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) domain (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String relation = m.group(1);
      String classA = m.group(2);
      String classAnoSymb = relation.replace(">","").replace("<","");
      String noClassA =  "No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"";

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+relation+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));

      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }

      String classAwithoutURI = noClassA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];

      String individual1= classAwithoutURI.toLowerCase().replace(">","").replace("<","")+"001";
      String individual2 = "thing002";

      OWLNamedIndividual indOWLnoA = dataFactory.getOWLNamedIndividual(IRI.create(individual1));
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLnoA);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(noClassOWLA, indOWLnoA);
      OWLNamedIndividual indOWLThing = dataFactory.getOWLNamedIndividual(IRI.create(individual2));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLThing);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(relation));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLnoA, indOWLThing);

      manager.applyChanges(manager.addAxiom(ontology1, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology1, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology1, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom));

      OWLOntology assertion1 = manager.getOntology(IRI.create(base));

      String expectedOutAssertion1 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }

      OWLNamedIndividual indOWLA = dataFactory.getOWLNamedIndividual(IRI.create(individual1));
      OWLDeclarationAxiom axiomIndividualA = dataFactory.getOWLDeclarationAxiom(indOWLA);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA, indOWLA);
      OWLPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA, indOWLThing);

      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividualA));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));

      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);

      String expectedOutAssertion2 = "consistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;

   }

   /*for range*/
   public static TestCaseImpl rangeTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) range (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String propertyA = m.group(1);
      String classA = m.group(2);
      String classA1 = classA+"1";
      String noClassA =  "No"+classA.split("(#|\\/)")[classA.split("(#)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+propertyA+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));

      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      String classAwithouturi = noClassA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];
      String ind1= classAwithouturi.toLowerCase().replace(">","").replace("<","")+"001";
      String ind2 = "thing002";
      OWLNamedIndividual indOWLnoA = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomIndividualividual1 = dataFactory.getOWLDeclarationAxiom(indOWLnoA);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(noClassOWLA, indOWLnoA);
      OWLNamedIndividual indOWL = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLDeclarationAxiom axiomIndividualividual2 = dataFactory.getOWLDeclarationAxiom(indOWL);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(propertyA));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWL, indOWLnoA);

      manager.applyChanges(manager.addAxiom(ontology1, axiomIndividualividual1));
      manager.applyChanges(manager.addAxiom(ontology1, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology1, axiomIndividualividual2));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }

      String ind3= classA.split("(#|\\/)")[classA.split("(#|\\/)").length-1].toLowerCase().replace(">","").replace("<","")+"002";
      OWLNamedIndividual indOWLA = dataFactory.getOWLNamedIndividual(IRI.create(ind3));
      OWLDeclarationAxiom axiomIndividualividualA = dataFactory.getOWLDeclarationAxiom(indOWLA);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA, indOWLA);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWL, indOWLA);

      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividualividualA));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividualividual2));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion2 = "consistent";
      manager.removeOntology(ontology2);

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for range dp*/
   /*Meter datetime*/
   public static TestCaseImpl rangeTestDP(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) range (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp|literal|rdfs:literal|xsd:double|double|xsd:long|long)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String propertyA = m.group(1);
      String datatypeA = m.group(2);
      String noDatatypeA =  "No"+datatypeA.split("(#|\\/)")[datatypeA.split("(#)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Property(<"+propertyA+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLDataProperty datatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(datatypeA));
      OWLDataProperty noDatatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(noDatatypeA));
      OWLDeclarationAxiom axiomDatatype1 = dataFactory.getOWLDeclarationAxiom(datatypeOWLA);
      OWLDeclarationAxiom axiomDatatype2 = dataFactory.getOWLDeclarationAxiom(noDatatypeOWLA);
      OWLNamedIndividual ind1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));

      manager.applyChanges(manager.addAxiom(ontology, axiomDatatype1));
      manager.applyChanges(manager.addAxiom(ontology, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(ind1)));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*
       *Assertions
       * */
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(propertyA));
      OWLDataPropertyAssertionAxiom assertionAxiom = null;
      if(datatypeA.contains("string") || datatypeA.contains("literal") || datatypeA.contains("datatime")  || datatypeA.contains("rational") || datatypeA.contains("anyuri") || datatypeA.contains("boolean")){
         assertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, 1 );
      }else{
         assertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, "temporalEntity");
      }
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(ind1)));
      manager.applyChanges(manager.addAxiom(ontology1, assertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLDataPropertyAssertionAxiom assertionAxiom2 = null;
      if(datatypeA.contains("string") || datatypeA.contains("literal") || datatypeA.contains("rational")|| datatypeA.contains("anyuri")){
         assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, "temporalEntity");
      }else if(datatypeA.contains("boolean")){
         assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, true);
      }else if(datatypeA.contains("datetime")){
         assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, dataFactory.getOWLLiteral("2001-10-26T21:32:52.12679",
                 OWL2Datatype.XSD_DATE_TIME));
      }else if( datatypeA.contains("double") ){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1.0",
                 OWL2Datatype.XSD_FLOAT);
         assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, dataLiteral);
      }else if( datatypeA.contains("double") ){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1.0",
                 OWL2Datatype.XSD_DOUBLE);
         assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, dataLiteral);
      }else{
         assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, ind1, 1);
      }
      manager.applyChanges(manager.addAxiom(ontology2, dataFactory.getOWLDeclarationAxiom(ind1)));
      manager.applyChanges(manager.addAxiom(ontology2, assertionAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion2 = "consistent";
      manager.removeOntology(ontology2);

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }


   public static TestCaseImpl rangeTestDPLiteral(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) range (literal|rdfs:literal)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String propertyA = m.group(1);
      String datatypeA = m.group(2);
      String noDatatypeA =  "No"+datatypeA.split("(#|\\/)")[datatypeA.split("(#)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Property(<"+propertyA+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLDataProperty datatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(datatypeA));
      OWLDataProperty noDatatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(noDatatypeA));
      OWLDeclarationAxiom axiomDatatype1 = dataFactory.getOWLDeclarationAxiom(datatypeOWLA);
      OWLDeclarationAxiom axiomDatatype2 = dataFactory.getOWLDeclarationAxiom(noDatatypeOWLA);
      OWLNamedIndividual individual1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));

      manager.applyChanges(manager.addAxiom(ontology, axiomDatatype1));
      manager.applyChanges(manager.addAxiom(ontology, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(individual1)));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*
       *Assertions
       * */
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(propertyA));
      OWLDataPropertyAssertionAxiom assertionAxiom1 = null;

      assertionAxiom1 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, individual1, 1 );

      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(individual1)));
      manager.applyChanges(manager.addAxiom(ontology1, assertionAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "consistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLDataPropertyAssertionAxiom assertionAxiom2 = null;

      assertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, individual1, "temporalEntity");

      manager.applyChanges(manager.addAxiom(ontology2, dataFactory.getOWLDeclarationAxiom(individual1)));
      manager.applyChanges(manager.addAxiom(ontology2, assertionAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion2 = "consistent";
      manager.removeOntology(ontology2);

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      testCase.setType("literal");
      return testCase;
   }


   /*for the generation of subclass, disjoint and equivalence*/
   public static TestCaseImpl multipleSubClassTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (.*) and (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);
      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String noClassA =  "No"+classA.split("(#|\\/)")[classA.split("(#)").length-1];
      String classA1 =  classA.split("(#|\\/)")[classA.split("(#)").length-1]+"1";
      String classB = m.group(2);
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String classC = m.group(3);
      String noClassC =  "No"+classC.split("(#|\\/)")[classC.split("(#|\\/)").length-1];
      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA.replaceAll(" ","")+">)");
      precondition.add("Class(<"+classB.replaceAll(" ","")+">)");
      precondition.add("Class(<"+classC.replaceAll(" ","")+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom1 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
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

      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass3));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass4));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom2));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass5));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass6));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom3));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass7 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLA, classOWLB));
      manager.applyChanges(manager.addAxiom(ontology1, axiomClass7));
      manager.applyChanges(manager.addAxiom(ontology1, subClassOfAxiom1));

      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology1);

      String expectedOutAssertion1 ="";
      expectedOutAssertion1 = "consistent";

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLB, classOWLA));
      manager.applyChanges(manager.addAxiom(ontology2, axiomClass7));
      manager.applyChanges(manager.addAxiom(ontology2, subClassOfAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);
      String expectedOutAssertion2 = "unsatisfiable";


      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLB, classOWLA));
      manager.applyChanges(manager.addAxiom(ontology3, axiomClass7));
      manager.applyChanges(manager.addAxiom(ontology3, subClassOfAxiom3));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology3);
      String expectedOutAssertion3  = "consistent";

      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLA, classOWLC));
      manager.applyChanges(manager.addAxiom(ontology4, axiomClass7));
      manager.applyChanges(manager.addAxiom(ontology4, subClassOfAxiom4));

      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology4);

      String expectedOutAssertion4 ="consistent";


      OWLOntology ontology5 = null;
      try {
         ontology5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology5 = null;
      }

      OWLSubClassOfAxiom subClassOfAxiom5 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLA, noClassOWLC));
      manager.applyChanges(manager.addAxiom(ontology5, axiomClass7));
      manager.applyChanges(manager.addAxiom(ontology5, subClassOfAxiom5));
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology5);
      String expectedOutAssertion5 = "unsatisfiable";

      OWLOntology ontology6 = null;
      try {
         ontology6 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology6 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom6 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLA, classOWLC));
      manager.applyChanges(manager.addAxiom(ontology6, axiomClass7));
      manager.applyChanges(manager.addAxiom(ontology6, subClassOfAxiom6));
      OWLOntology assertion6 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology6);
      String expectedOutAssertion6 = "consistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);
      hashInput.put("Assertion 5", assertion5);
      hashInput.put("Assertion 6", assertion6);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);
      hashOutput.put("Assertion 5",expectedOutAssertion5);
      hashOutput.put("Assertion 6",expectedOutAssertion6);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
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
      classA = m.group(1);
      noClassA = "No" + classA.split("(#|\\/)")[classA.split("(#)").length - 1];
      classA1 = classA.split("(#|\\/)")[classA.split("(#)").length - 1] + "1";

      classB = m.group(3);
      noClassB = "No" + classB.split("(#|\\/)")[classB.split("(#|\\/)").length - 1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA.replaceAll(" ","")+">)");
      precondition.add("Class(<"+classB.replaceAll(" ","")+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();

      if(!classA.equals(classB)) {
         /*Preparation*/
         OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
         OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
         OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);
         OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
         OWLEquivalentClassesAxiom equivalentClassesAxiom1 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
         OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
         OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
         OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(classOWLB);
         OWLDeclarationAxiom axiomClass4 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
         OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));

         manager.applyChanges(manager.addAxiom(ontology, axiomClass));
         manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
         manager.applyChanges(manager.addAxiom(ontology, axiomClass3));
         manager.applyChanges(manager.addAxiom(ontology, axiomClass4));
         manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom1));
         manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom2));
         testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
         manager.removeOntology(ontology);

         /*Assertions*/
         OWLOntology ontology1 = null;
         try {
            ontology1 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology1 = null;
         }

         OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
         OWLDeclarationAxiom axiomClass7 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
         OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLA, classOWLB));
         manager.applyChanges(manager.addAxiom(ontology1, axiomClass7));
         manager.applyChanges(manager.addAxiom(ontology1, subClassOfAxiom1));

         OWLOntology assertion1 = manager.getOntology(IRI.create(base));
         manager.removeOntology(ontology1);

         String expectedOutAssertion1 = "";
         if (type == "equivalence")
            expectedOutAssertion1 = "unsatisfiable";
         else
            expectedOutAssertion1 = "consistent";

         OWLOntology ontology2 = null;
         try {
            ontology2 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology2 = null;
         }

         OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLB, classOWLA));
         manager.applyChanges(manager.addAxiom(ontology2, axiomClass7));
         manager.applyChanges(manager.addAxiom(ontology2, subClassOfAxiom2));

         OWLOntology assertion2 = manager.getOntology(IRI.create(base));
         manager.removeOntology(ontology2);

         String expectedOutAssertion2 = "";
         if (type == "strict subclass" || type == "equivalence")
            expectedOutAssertion2 = "unsatisfiable";
         else
            expectedOutAssertion2 = "consistent";

         OWLOntology ontology3 = null;
         try {
            ontology3 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology3 = null;
         }

         OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLB, classOWLA));
         manager.applyChanges(manager.addAxiom(ontology3, axiomClass7));
         manager.applyChanges(manager.addAxiom(ontology3, subClassOfAxiom3));

         OWLOntology assertion3 = manager.getOntology(IRI.create(base));
         manager.removeOntology(ontology3);

         String expectedOutAssertion3 = "";
         if (type == "disjoint")
            expectedOutAssertion3 = "unsatisfiable";
         else
            expectedOutAssertion3 = "consistent";


         hashInput.put("Assertion 1", assertion1);
         hashInput.put("Assertion 2", assertion2);
         hashInput.put("Assertion 3", assertion3);

         hashOutput.put("Assertion 1",expectedOutAssertion1);
         hashOutput.put("Assertion 2",expectedOutAssertion2);
         hashOutput.put("Assertion 3",expectedOutAssertion3);

         for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
            hashOutput.put(entry.getKey(), entry.getValue());
         }

         for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
            hashInput.put(entry.getKey(), entry.getValue());
         }
      }else{
         OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
         OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
         OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLB);
         OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
         OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));

         manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
         manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
         manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom2));
         testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
         manager.removeOntology(ontology);

         OWLOntology ontology1 = null;
         try {
            ontology1 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology1 = null;
         }

         OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
         OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
         OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLB);
         manager.applyChanges(manager.addAxiom(ontology1, axiomClass3));
         manager.applyChanges(manager.addAxiom(ontology1, subClassOfAxiom1));

         OWLOntology assertion1 = manager.getOntology(IRI.create(base));
         manager.removeOntology(ontology1);

         String expectedOutAssertion1 = "consistent";
         OWLOntology ontology2 = null;
         try {
            ontology2 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology2 = null;
         }

         OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, noClassOWLB);
         manager.applyChanges(manager.addAxiom(ontology2, axiomClass3));
         manager.applyChanges(manager.addAxiom(ontology2, subClassOfAxiom2));

         OWLOntology assertion2 = manager.getOntology(IRI.create(base));
         manager.removeOntology(ontology2);

         String  expectedOutAssertion2 = "unsatisfiable";

         hashInput.put("Assertion 1", assertion1);
         hashInput.put("Assertion 2", assertion2);

         hashOutput.put("Assertion 1",expectedOutAssertion1);
         hashOutput.put("Assertion 2",expectedOutAssertion2);

         for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
            hashOutput.put(entry.getKey(), entry.getValue());
         }

         for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
            hashInput.put(entry.getKey(), entry.getValue());
         }
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for range (strict) universal restriction*/
   public static TestCaseImpl existentialRange(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) some (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).replaceAll("\\(","").replaceAll("\\)","").replaceAll("  "," ");;
      String classA1 = classA+"1";
      String R = m.group(2).replaceAll("  ","");

      String classB = m.group(3).replaceAll("\\(","").replaceAll("\\)","").replaceAll("  "," ");
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+R+">)");
      precondition.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precondition);

      String individual001= "individual001";
      String individual002 = "individual002";
      String individual003 = "individual003";


      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create(individual001));
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLnoB = dataFactory.getOWLNamedIndividual(IRI.create(individual002));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLnoB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLnoB);
      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClassA1));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLnoB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLnoB);
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass2));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create(individual003));
      OWLDeclarationAxiom axiomIndividual3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLnoB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLnoB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      OWLClassAssertionAxiom classAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(classOWLB, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass3));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom3));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ontology2);

      String expectedOutAssertion2 = "consistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      testCase.setType("existential");
      return testCase;
   }


   /*for range (strict) and for OP + universal restriction*/
   public static TestCaseImpl universalRange(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (\\w*)(?!hasparticipant | ?!isparticipantin | ?!haslocation| ?!islocationof | ?!hasrole| ?!isroleof) only (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).replaceAll("\\(","").replaceAll("\\)","").replaceAll("  "," ");;
      String classA1 = classA+"1";
      String R = m.group(2).replaceAll("  ","");

      String classB = "";
      classB = m.group(3).replaceAll("\\(","").replaceAll("\\)","").replaceAll("  "," ");
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+R+">)");
      precondition.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precondition);

      String individual001= "individual001";
      String individual002 = "individual002";
      String individual003 = "individual003";

      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create(individual001));
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create(individual002));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);

      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClassA1));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);
      /*Assertions */
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);

      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass2));
      manager.applyChanges(manager.addAxiom(ontology1, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";

      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create(individual003));
      OWLDeclarationAxiom axiomIndividual3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLClassAssertionAxiom owlClassAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWL3);
      OWLSubClassOfAxiom axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology2, owlClassAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass3));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);
      String expectedOutAssertion2 = "inconsistent";


      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);


      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for range (strict) universal restriction DP*/
   public static TestCaseImpl existentialRangeDP(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (.*) some (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp|literal|rdfs:literal|xsd:double|double|xsd:long|long)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).replaceAll("\\(","").replaceAll("\\)","");;
      String classA1 = classA+"1";
      String R = m.group(2);
      String datatype = "";
      datatype = m.group(3).replaceAll("\\(","").replaceAll("\\)","");;
      String nodatatype =  "No"+datatype.split("(#|\\/)")[datatype.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+R+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Preparation*/
      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }

      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLSubClassOfAxiom subClassOfAxiomAA1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLDataProperty datatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(datatype));
      OWLDataProperty noDatatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(nodatatype));
      OWLDeclarationAxiom axiomDatatype1 = dataFactory.getOWLDeclarationAxiom(datatypeOWLA);
      OWLDeclarationAxiom axiomDatatype2 = dataFactory.getOWLDeclarationAxiom(noDatatypeOWLA);
      OWLNamedIndividual indOWL1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLClassAssertionAxiom assertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL1);

      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, subClassOfAxiomAA1));
      manager.applyChanges(manager.addAxiom(ontology, axiomDatatype1));
      manager.applyChanges(manager.addAxiom(ontology, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ontology, subClassOfAxiomAA1));
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(indOWL1)));
      manager.applyChanges(manager.addAxiom(ontology, assertionAxiom));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Axioms to be added*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
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

      manager.applyChanges(manager.addAxiom(ontology1, subClassOfAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";

      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }

      OWLNamedIndividual indOWL5 = dataFactory.getOWLNamedIndividual(IRI.create("individual5"));
      OWLDeclarationAxiom axiomIndividualClassA = dataFactory.getOWLDeclarationAxiom(indOWL5);
      OWLClassAssertionAxiom indAClassAAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA, indOWL5);

      OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom;
      if(datatype.contains("string") || datatype.contains("literal") || datatype.contains("rational")  ) {
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("true",
                 OWL2Datatype.XSD_BOOLEAN);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("boolean")) {
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("true",
                 OWL2Datatype.XSD_STRING);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("integer") || datatype.contains("long")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1.0",
                 OWL2Datatype.XSD_FLOAT);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("float")  ){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1",
                 OWL2Datatype.XSD_INTEGER);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("double") ){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1",
                 OWL2Datatype.XSD_INTEGER);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("uri")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1.0",
                 OWL2Datatype.XSD_FLOAT);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("datetime")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("2001-10-26T21:32:52.12679",
                 OWL2Datatype.XSD_STRING);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      } else if(datatype.contains("datetimestamp")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("2001-10-26T21:32:52.12679",
                 OWL2Datatype.XSD_STRING);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      } else{
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, "Example text");

      }

      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividualClassA));
      manager.applyChanges(manager.addAxiom(ontology2, indAClassAAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology2, dataPropertyAssertionAxiom));

      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      String expectedOutAssertion2 = "consistent";

      manager.removeOntology(ontology2);


      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }


      if(datatype.contains("string") || datatype.contains("literal") || datatype.contains("rational")  ) {
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, "Example text");
      }else if(datatype.contains("boolean")) {
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("true",
                 OWL2Datatype.XSD_BOOLEAN);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("integer") || datatype.contains("long")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1",
                 OWL2Datatype.XSD_INTEGER);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("float")  ){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1.0",
                 OWL2Datatype.XSD_FLOAT);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("double") ){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1.0",
                 OWL2Datatype.XSD_DOUBLE);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("uri")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("http://example.org/ns#",
                 OWL2Datatype.XSD_ANY_URI);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("datetime")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("2001-10-26T21:32:52.12679",
                 OWL2Datatype.XSD_DATE_TIME);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      } else if(datatype.contains("datetimestamp")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("2001-10-26T21:32:52.12679",
                 OWL2Datatype.XSD_DATE_TIME_STAMP);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      } else{
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, "Example text");

      }

      manager.applyChanges(manager.addAxiom(ontology3, axiomIndividualClassA));
      manager.applyChanges(manager.addAxiom(ontology3, indAClassAAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology3, dataPropertyAssertionAxiom));

      OWLOntology assertion3 = manager.getOntology(IRI.create(base));

      String expectedOutAssertion3 = "consistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);


      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);



      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
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
            complementClasses.add(m.group().replace("not(","").replace(")",""));
         }
      }

      Pattern p = Pattern.compile("(.*) subclassof (.*) only (xsd:string|xsd:float|xsd:integer|string|float|integer|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|datetime|xsd:datetime|datetimestamp|xsd:datetimestamp|literal|rdfs:literal|xsd:double|double|xsd:long|long)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).replaceAll("\\(","").replaceAll("\\)","");;
      String classA1 = classA+"1";
      String relation = m.group(2);
      String datatype = "";
      datatype = m.group(3).replaceAll("\\(","").replaceAll("\\)","").toLowerCase();;
      String nodatatype =  "No"+datatype.split("(#|\\/)")[datatype.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+relation+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Preparation*/
      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }

      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLSubClassOfAxiom subClassOfAxiomAA1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, subClassOfAxiomAA1));

      OWLDataProperty datatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(datatype));
      OWLDataProperty noDatatypeOWLA = dataFactory.getOWLDataProperty(IRI.create(nodatatype));
      OWLDeclarationAxiom axiomDatatype1 = dataFactory.getOWLDeclarationAxiom(datatypeOWLA);
      OWLDeclarationAxiom axiomDatatype2 = dataFactory.getOWLDeclarationAxiom(noDatatypeOWLA);

      OWLNamedIndividual indOWL1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLClassAssertionAxiom assertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL1);

      manager.applyChanges(manager.addAxiom(ontology, axiomDatatype1));
      manager.applyChanges(manager.addAxiom(ontology, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ontology, dataFactory.getOWLDeclarationAxiom(indOWL1)));
      manager.applyChanges(manager.addAxiom(ontology, assertionAxiom));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Axioms to be added*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(relation));
      OWLClassAssertionAxiom owlClassAssertionAxiom1;
      OWLDataPropertyAssertionAxiom objectPropertyAssertionAxiom2;
      if(datatype.contains("string") || datatype.contains("datetime") || datatype.contains("rational")|| datatype.contains("anyuri")){
         OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("individual3"));
         owlClassAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL3);
         objectPropertyAssertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL3, 1);
      }else if(datatype.contains("anyuri")){
         OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("individual3"));
         owlClassAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL3);
         objectPropertyAssertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL3, 1);
      }else{
         OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("individual3"));
         owlClassAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL3);
         objectPropertyAssertionAxiom2 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL3, "");

      }
      manager.applyChanges(manager.addAxiom(ontology1, axiomDatatype1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ontology1, dataFactory.getOWLDeclarationAxiom(indOWL1)));
      manager.applyChanges(manager.addAxiom(ontology1, assertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology1, owlClassAssertionAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";

      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }

      OWLClassAssertionAxiom owlClassAssertionAxiom2;
      OWLDataPropertyAssertionAxiom objectPropertyAssertionAxiom3;
      if(datatype.contains("string") || datatype.contains("datetime") || datatype.contains("rational")){
         OWLNamedIndividual indOWL4 = dataFactory.getOWLNamedIndividual(IRI.create("individual4"));
         owlClassAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL4);
         objectPropertyAssertionAxiom3 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL4, "");
      }else if(datatype.contains("anyuri")){
         OWLNamedIndividual indOWL4 = dataFactory.getOWLNamedIndividual(IRI.create("individual4"));
         owlClassAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL4);
         objectPropertyAssertionAxiom3 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL4,"");
      }else{
         OWLNamedIndividual indOWL4 = dataFactory.getOWLNamedIndividual(IRI.create("individual4"));
         owlClassAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWL4);
         objectPropertyAssertionAxiom3 = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL4, 3);
      }

      manager.applyChanges(manager.addAxiom(ontology2, axiomDatatype1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomDatatype2));
      manager.applyChanges(manager.addAxiom(ontology2, dataFactory.getOWLDeclarationAxiom(indOWL1)));
      manager.applyChanges(manager.addAxiom(ontology2, assertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, owlClassAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology2, owlClassAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion2 = "inconsistent";

      manager.removeOntology(ontology2);


      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }

      OWLNamedIndividual indOWL5 = dataFactory.getOWLNamedIndividual(IRI.create("individual5"));
      OWLDeclarationAxiom axiomIndividualClassA = dataFactory.getOWLDeclarationAxiom(indOWL5);
      OWLClassAssertionAxiom indAClassAAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA, indOWL5);

      OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom;
      if(datatype.contains("string") || datatype.contains("literal") || datatype.contains("rational")  ) {
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, "Example text");
      }else if(datatype.contains("boolean")) {
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("true",
                 OWL2Datatype.XSD_BOOLEAN);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("integer") || datatype.contains("long")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1",
                 OWL2Datatype.XSD_INTEGER);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("float")  ){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1.0",
                 OWL2Datatype.XSD_FLOAT);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("double") ){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1.0",
                 OWL2Datatype.XSD_DOUBLE);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("uri")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("http://example.org/ns#",
                 OWL2Datatype.XSD_ANY_URI);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      }else if(datatype.contains("datetime")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("2001-10-26T21:32:52.12679",
                 OWL2Datatype.XSD_DATE_TIME);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      } else if(datatype.contains("datetimestamp")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("2001-10-26T21:32:52.12679",
                 OWL2Datatype.XSD_DATE_TIME_STAMP);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, dataLiteral);
      } else{
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indOWL5, "Example text");

      }



      manager.applyChanges(manager.addAxiom(ontology3, axiomIndividualClassA));
      manager.applyChanges(manager.addAxiom(ontology3, indAClassAAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology3, dataPropertyAssertionAxiom));

      OWLOntology assertion3 = manager.getOntology(IRI.create(base));

      String expectedOutAssertion3 = "consistent";

      manager.removeOntology(ontology3);

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for  cardinality */
   public static TestCaseImpl cardinality(String purpose, String type, TestCaseImpl testCase){
      Pattern p1 = Pattern.compile("(.*) subclassof (.*) (min|max|exactly) ([1-9][0-9]*) (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p1.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);

      String classA1 = classA+"1";
      String relation = m.group(2);
      String classB = "";
      classB = m.group(5);
      Integer num = Integer.parseInt(m.group(4));

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+relation+">)");
      precondition.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/

      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      manager.addAxiom(ontology,axiomClass);
      manager.applyChanges(manager.addAxiom(ontology, axiomClass));

      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      manager.applyChanges(manager.addAxiom(ontology, axiomSubclass1));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      /*Assertions 1 */
      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(relation));
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      manager.addAxiom(ontology,axiomClass2);
      OWLSubClassOfAxiom axiomSubclass4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectMaxCardinality(num-1, prop, classOWLB));
      manager.addAxiom(ontology2, axiomSubclass4);
      manager.addAxiom(ontology2, axiomClass2);
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));

      String expectedOutAssertion1 = "";
      if (type == "min" || type == "exactly")
         expectedOutAssertion1 = "unsatisfiable";
      else
         expectedOutAssertion1 = "consistent";
      manager.removeOntology(ontology2);
      /*Assertions 2 */
      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num+1, prop, classOWLB) );
      manager.applyChanges( manager.addAxiom(ontology4, axiomSubclass2));
      manager.applyChanges( manager.addAxiom(ontology4, axiomClass2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      String expectedOutAssertion2 = "";
      if (type == "max" || type == "exactly")
         expectedOutAssertion2 = "unsatisfiable";
      else
         expectedOutAssertion2 = "consistent";
      manager.removeOntology(ontology4);

      /*Assertions 3 */ //The Pellet reasoner consumes too much memory with cardinalities
      /*OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      OWLSubClassOfAxiom axiomSubclass3 = null;

      if(type == "max") {
         axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectMinCardinality(num, prop, classOWLB));

      }else{
         axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( dataFactory.getOWLObjectMaxCardinality(num, prop, classOWLB), classOWLA1);
      }
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass3));
      manager.applyChanges(manager.addAxiom(ontology3,axiomClass2));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));

      String expectedOutAssertion3 = "consistent";*/
       LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
       LinkedHashMap<String, String> hashOutput = new LinkedHashMap();

       hashInput.put("Assertion 1", assertion1);
       hashInput.put("Assertion 2", assertion2);

       hashOutput.put("Assertion 1",expectedOutAssertion1);
       hashOutput.put("Assertion 2",expectedOutAssertion2);
      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;

   }

   /*for intersection + cardinality */ /*Poner opciones para min, exact y sin cardinalidad?*/
   public static TestCaseImpl intersectionCardTest(String purpose, String type, TestCaseImpl testCase ){ /*TODO*/
      Pattern p = Pattern.compile("(.*) subclassof (.*) (max|min|exactly) (\\d+) \\((.*) and (.*)\\)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1).replaceAll("\\(","").replaceAll("\\)","");
      String classA1 = classA+"1";

      String R = m.group(2);

      String classB = m.group(5).replaceAll("\\(","").replaceAll("\\)","");
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      String classC = m.group(6).replaceAll("\\(","").replaceAll("\\)","");
      String noClassC =  "No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      Integer num = Integer.parseInt(m.group(4).replace("min","").replace("max","").replace("exactly","").replace(" ",""));


      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)\n");
      precondition.add("Property(<"+R+">)\n");
      precondition.add("Class(<"+classB+">)\n");
      precondition.add("Class(<"+classC+">)\n");

      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
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
      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass3));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom2));

      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMaxCardinality(num-1,prop,
              dataFactory.getOWLObjectIntersectionOf(classOWLB,classOWLC)));

      manager.applyChanges(manager.addAxiom(ontology1, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass2));

      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology1);

      String expectedOutAssertion2 = "";
      if (type == "min" || type == "exactly")
         expectedOutAssertion2 = "unsatisfiable";
      else
         expectedOutAssertion2 = "consistent";

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }

      OWLSubClassOfAxiom axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num+1,prop,
              dataFactory.getOWLObjectIntersectionOf(classOWLB,classOWLC)));
      manager.applyChanges(manager.addAxiom(ontology2, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass3));

      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);
      String expectedOutAssertion3 = "";
      if (type == "max" || type == "exactly")
         expectedOutAssertion3 = "unsatisfiable";
      else
         expectedOutAssertion3 = "consistent";

      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }

      OWLSubClassOfAxiom axiomSubclass4 = null;

      if(type == "max") {
         axiomSubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num,prop,
                 dataFactory.getOWLObjectIntersectionOf(classOWLB,classOWLC)));
      }else{
         axiomSubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMaxCardinality(num,prop,
                 dataFactory.getOWLObjectIntersectionOf(classOWLB,classOWLC)));
      }
      String expectedOutAssertion4 = "consistent";


      manager.applyChanges(manager.addAxiom(ontology3, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass4));

      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology3);

      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }

      OWLSubClassOfAxiom axiomSubclass5 = null;
      if(type== "max") {
         axiomSubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num+1,prop,
                 classOWLB));
      }
      String expectedOutAssertion5 = "consistent";
      manager.applyChanges(manager.addAxiom(ontology4, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass5));

      OWLOntology assertion5 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology4);

      OWLOntology ontology5 = null;
      try {
         ontology5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology5 = null;
      }

      OWLSubClassOfAxiom axiomSubclass6 = null;
      if(type =="max"){
         axiomSubclass6 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num+1,prop,
                 classOWLC));
      }
      String expectedOutAssertion6 = "consistent";
      manager.applyChanges(manager.addAxiom(ontology5, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ontology5, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology5, axiomSubclass6));

      OWLOntology assertion6 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology5);
      OWLOntology ontology6 = null;
      try {
         ontology6 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology6 = null;
      }

      OWLSubClassOfAxiom axiomSubclass7 = null;
      if(type == "max"){
         axiomSubclass7 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectMinCardinality(num+1,prop,
                 dataFactory.getOWLObjectIntersectionOf(classOWLB,classOWLC)));
      }
      String expectedOutAssertion7 = "unsatisfiable";
      manager.applyChanges(manager.addAxiom(ontology6, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ontology6, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology6, axiomSubclass7));

      OWLOntology assertion7 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology6);


      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);
      hashInput.put("Assertion 5", assertion5);
      hashInput.put("Assertion 6", assertion6);
      hashInput.put("Assertion 7", assertion7);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);
      hashOutput.put("Assertion 5",expectedOutAssertion5);
      hashOutput.put("Assertion 6",expectedOutAssertion6);
      hashOutput.put("Assertion 7",expectedOutAssertion7);


      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for union */
   public static TestCaseImpl unionTest(String purpose, String type, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassOf (.*) only (.*) or (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String classA1 =  classA.split("(#|\\/)")[classA.split("(#)").length-1]+"1";

      String relation =m.group(2);
      String classB = m.group(3);
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      String classC = m.group(4);
      String noClassC =  "No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String noClassCB =  "No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1]+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Class(<"+classB+">)");
      precondition.add("Class(<"+classC+">)");
      precondition.add("Property(<"+relation+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      /*Preconditions*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass noClassOWLC = dataFactory.getOWLClass(IRI.create(noClassC));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLClass noclassOWLCB = dataFactory.getOWLClass(IRI.create(noClassCB));
      OWLSubClassOfAxiom subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLClass(IRI.create(classA)));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(classOWLC);
      OWLDeclarationAxiom axiomClass4 = dataFactory.getOWLDeclarationAxiom(noClassOWLC);
      OWLDeclarationAxiom axiomClass5 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLEquivalentClassesAxiom equivalentClassesAxiom1 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLC, dataFactory.getOWLObjectComplementOf(classOWLC));
      OWLEquivalentClassesAxiom equivalentClassesAxiom3 = dataFactory.getOWLEquivalentClassesAxiom(noclassOWLCB, dataFactory.getOWLObjectComplementOf(classOWLC));
      OWLEquivalentClassesAxiom equivalentClassesAxiom4 = dataFactory.getOWLEquivalentClassesAxiom(noclassOWLCB, dataFactory.getOWLObjectComplementOf(classOWLB));

      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass3));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass4));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass5));
      manager.applyChanges(manager.addAxiom(ontology, subClassOfAxiom));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom2));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom3));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom4));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);
      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(relation));
      OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectSomeValuesFrom(prop, classOWLB));

      manager.applyChanges(manager.addAxiom(ontology1, subClassOfAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "consistent";

      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectSomeValuesFrom(prop, classOWLC));

      manager.applyChanges(manager.addAxiom(ontology2, subClassOfAxiom2));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion2 = "consistent";

      manager.removeOntology(ontology2);

      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectSomeValuesFrom(prop, noclassOWLCB));

      manager.applyChanges(manager.addAxiom(ontology3, subClassOfAxiom3));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ontology3);

      String expectedOutAssertion3 ="";
      expectedOutAssertion3 = "unsatisfiable";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for intersection*/
   public static TestCaseImpl intersectionTest(String purpose, String type, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassOf (.*) some (.*) and (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String classA1 =  classA.split("(#|\\/)")[classA.split("(#)").length-1]+"1";

      String relation =m.group(2);
      String classB = m.group(3);

      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      String classC = m.group(4);
      String noClassC =  "No"+classC.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Class(<"+classB+">)");
      precondition.add("Class(<"+classC+">)");
      precondition.add("Property(<"+relation+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      /*Preconditions*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass noClassOWLC = dataFactory.getOWLClass(IRI.create(noClassC));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLClass(IRI.create(classA)));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(classOWLC);
      OWLDeclarationAxiom axiomClass4 = dataFactory.getOWLDeclarationAxiom(noClassOWLC);
      OWLDeclarationAxiom axiomClass5 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLEquivalentClassesAxiom equivalentClassesAxiom1 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLC, dataFactory.getOWLObjectComplementOf(classOWLC));

      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass3));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass4));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass5));
      manager.applyChanges(manager.addAxiom(ontology, subClassOfAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(relation));
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectIntersectionOf(classOWLB)));

      manager.applyChanges(manager.addAxiom(ontology1, subClassOfAxiom2));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "consistent";

      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectIntersectionOf(noClassOWLB, noClassOWLC)));

      manager.applyChanges(manager.addAxiom(ontology2, subClassOfAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ontology2);

      String expectedOutAssertion2 ="";
      expectedOutAssertion2 = "unsatisfiable";

      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectIntersectionOf(classOWLB, noClassOWLC)));

      manager.applyChanges(manager.addAxiom(ontology3, subClassOfAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ontology3);

      String expectedOutAssertion3 = "";

      expectedOutAssertion3 = "unsatisfiable";

      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom5 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectIntersectionOf(classOWLC, noClassOWLB)));

      manager.applyChanges(manager.addAxiom(ontology4, subClassOfAxiom5));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ontology3);

      String expectedOutAssertion4 = "";

      expectedOutAssertion4 = "unsatisfiable";


      OWLSubClassOfAxiom subClassOfAxiom6 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectAllValuesFrom(prop, classOWLC));

      manager.applyChanges(manager.addAxiom(ontology4, subClassOfAxiom6));
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ontology3);

      String expectedOutAssertion5 = "";

      expectedOutAssertion5 = "consistent";


      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);
      hashInput.put("Assertion 5", assertion5);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);
      hashOutput.put("Assertion 5",expectedOutAssertion5);


      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for part-whole relations*/
   public static TestCaseImpl partWholeTestExistential(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (ispartof|partof) some (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String classA1 = classA+"1>";
      String R = m.group(2);
      String classB = m.group(3);
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+R+">)");
      precondition.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precondition);


      /*Axioms to be added -- we need to test if the relation is transitive*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("individual2"));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);

      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClassA1));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass2));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("individual3"));
      OWLDeclarationAxiom axiomIndividual3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass3));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ontology2);

      String expectedOutAssertion2 = "consistent";


      /*Assertions*/
      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      String inverse = "";
      if(R.equals("ispartof"))
         inverse = "hasPart";
      else
         inverse = "isPartOf";

      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));
      OWLSubClassOfAxiom axiomSubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass4));
      manager.applyChanges(manager.addAxiom(ontology3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion3 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }

      OWLSubClassOfAxiom axiomSubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);
      manager.applyChanges(manager.addAxiom(ontology4, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology4, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass5));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom6));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology4);
      String expectedOutAssertion4 = "consistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);


      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for part-whole relations*/
   public static TestCaseImpl partWholeTestUniversal(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (ispartof|partof) only (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String classA1 = classA+"1";
      String R = m.group(2);
      String classB = "";
      classB = m.group(3);
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+R+">)");
      precondition.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precondition);

      String classA1withouturi = classA1.split("(#|\\/)")[classA1.split("(#|\\/)").length-1];
      String classBwithouturi = classB.split("(#|\\/)")[noClassB.split("(#|\\/)").length-1];

      /*Axioms to be added -- we need to test if the relation is transitive*/
      /*Preparation*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("individual2"));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClassA1));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass2));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("individual3"));
      OWLDeclarationAxiom axiomIndividual3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass3));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ontology2);

      String expectedOutAssertion2 = "consistent";


      /*Assertions*/
      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      String inverse = "";
      if(R.equals("ispartof"))
         inverse = "hasPart";
      else
         inverse = "isPartOf";

      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));

      OWLSubClassOfAxiom axiomSubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLA1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass4));
      manager.applyChanges(manager.addAxiom(ontology3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion3 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }

      OWLSubClassOfAxiom axiomSubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);
      manager.applyChanges(manager.addAxiom(ontology4, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology4, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass5));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom6));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology4);
      String expectedOutAssertion4 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);


      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /* for subclass of two classes and disjointness between them  */
   public static TestCaseImpl subclassDisjointTest(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (.*) and (.*) subclassof (.*) that disjointwith (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);
      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String classAnoSymb = classA;
      String noClassA =  "No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1];
      String classA1 =  classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"1";
      String classB = m.group(2);
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String classC = m.group(3);
      String noClassC =  "No"+classC.split("(#|\\/)")[classC.split("(#|\\/)").length-1];
      if(classB.equals(m.group(4)) && classA.equals(m.group(5))) {

         /*Preconditions*/
         ArrayList<String> precondition = new ArrayList<>();
         precondition.add("Class(<" + classA + ">)");
         precondition.add("Class(<" + classC + ">)");
         precondition.add("Class(<" + classB + ">)");
         testCase.getPrecondition().addAll(precondition);

         /*Axioms to be added*/

         /*Preparation*/
         String base = "";
         OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
         OWLOntology ontology = null;
         try {
            ontology = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology = null;
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
         OWLDeclarationAxiom axiomClass6 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
         manager.applyChanges(manager.addAxiom(ontology, axiomClass));
         manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
         manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
         manager.applyChanges(manager.addAxiom(ontology, axiomClass3));
         manager.applyChanges(manager.addAxiom(ontology, axiomClass4));
         manager.applyChanges(manager.addAxiom(ontology, axiomClass5));
         manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom1));
         manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom2));
         manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom3));
         manager.applyChanges(manager.addAxiom(ontology, axiomClass6));
         testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
         manager.removeOntology(ontology);

         OWLOntology ontology1 = null;
         try {
            ontology1 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology1 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLA, classOWLB));
         manager.applyChanges(manager.addAxiom(ontology1, axiomClass6));
         manager.applyChanges(manager.addAxiom(ontology1, subClassOfAxiom2));
         OWLOntology assertion1 = manager.getOntology(IRI.create(base));
         String expectedOutAssertion1 = "";
         expectedOutAssertion1 = "consistent";
         manager.removeOntology(ontology1);

         OWLOntology ontology2 = null;
         try {
            ontology2 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology2 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLA, noClassOWLB));
         manager.applyChanges(manager.addAxiom(ontology2, axiomClass6));
         manager.applyChanges(manager.addAxiom(ontology2, subClassOfAxiom3));
         OWLOntology assertion2 = manager.getOntology(IRI.create(base));

         String expectedOutAssertion2 = "";
         expectedOutAssertion2 = "unsatisfiable";
         manager.removeOntology(ontology2);

         OWLOntology ontology3 = null;
         try {
            ontology3 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology3 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLA, classOWLB));
         manager.applyChanges(manager.addAxiom(ontology3, axiomClass6));
         manager.applyChanges(manager.addAxiom(ontology3, subClassOfAxiom4));
         OWLOntology assertion3 = manager.getOntology(IRI.create(base));
         String expectedOutAssertion3 = "consistent";
         manager.removeOntology(ontology3);

         OWLOntology ontology4 = null;
         try {
            ontology4 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology4 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom5 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLC, classOWLB));
         manager.applyChanges(manager.addAxiom(ontology4, axiomClass6));
         manager.applyChanges(manager.addAxiom(ontology4, subClassOfAxiom5));
         OWLOntology assertion4 = manager.getOntology(IRI.create(base));
         String expectedOutAssertion4 = "consistent";
         manager.removeOntology(ontology4);

         OWLOntology ontology5 = null;
         try {
            ontology5 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology5 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom6 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLC, noClassOWLB));
         manager.applyChanges(manager.addAxiom(ontology5, axiomClass6));
         manager.applyChanges(manager.addAxiom(ontology5, subClassOfAxiom6));
         OWLOntology assertion5 = manager.getOntology(IRI.create(base));
         String expectedOutAssertion5 = "unsatisfiable";
         manager.removeOntology(ontology5);

         OWLOntology ontology6 = null;
         try {
            ontology6 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology6 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom7 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLC, classOWLB));
         manager.applyChanges(manager.addAxiom(ontology6, axiomClass6));
         manager.applyChanges(manager.addAxiom(ontology6, subClassOfAxiom7));
         OWLOntology assertion6 = manager.getOntology(IRI.create(base));
         String expectedOutAssertion6 = "consistent";
         manager.removeOntology(ontology6);

         OWLOntology ontology7 = null;
         try {
            ontology7 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology7 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom8 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(noClassOWLC, classOWLA));
         manager.applyChanges(manager.addAxiom(ontology7, axiomClass6));
         manager.applyChanges(manager.addAxiom(ontology7, subClassOfAxiom8));
         OWLOntology assertion7 = manager.getOntology(IRI.create(base));
         String expectedOutAssertion7 = "consistent";
         manager.removeOntology(ontology7);

         OWLOntology ontology8 = null;
         try {
            ontology8 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology8 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom9 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLC, noClassOWLA));
         manager.applyChanges(manager.addAxiom(ontology8, axiomClass6));
         manager.applyChanges(manager.addAxiom(ontology8, subClassOfAxiom9));
         OWLOntology assertion8 = manager.getOntology(IRI.create(base));
         String expectedOutAssertion8 = "consistent";
         manager.removeOntology(ontology8);

         OWLOntology ontology9 = null;
         try {
            ontology9 = manager.createOntology(IRI.create(base));
         } catch (OWLOntologyCreationException e) {
            ontology9 = null;
         }
         OWLSubClassOfAxiom subClassOfAxiom10 = dataFactory.getOWLSubClassOfAxiom(classOWLA1, dataFactory.getOWLObjectIntersectionOf(classOWLC, classOWLA));
         manager.applyChanges(manager.addAxiom(ontology9, axiomClass6));
         manager.applyChanges(manager.addAxiom(ontology9, subClassOfAxiom10));
         OWLOntology assertion9 = manager.getOntology(IRI.create(base));

         String expectedOutAssertion9 = "unsatisfiable";
         manager.removeOntology(ontology9);


         LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
         hashInput.put("Assertion 1", assertion1);
         hashInput.put("Assertion 2", assertion2);
         hashInput.put("Assertion 3", assertion3);
         hashInput.put("Assertion 4", assertion4);
         hashInput.put("Assertion 5", assertion5);
         hashInput.put("Assertion 6", assertion6);
         hashInput.put("Assertion 7", assertion7);
         hashInput.put("Assertion 8", assertion8);
         hashInput.put("Assertion 9", assertion9);

         LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
         hashOutput.put("Assertion 1", expectedOutAssertion1);
         hashOutput.put("Assertion 2", expectedOutAssertion2);
         hashOutput.put("Assertion 3", expectedOutAssertion3);
         hashOutput.put("Assertion 4", expectedOutAssertion4);
         hashOutput.put("Assertion 5", expectedOutAssertion5);
         hashOutput.put("Assertion 6", expectedOutAssertion6);
         hashOutput.put("Assertion 7", expectedOutAssertion7);
         hashOutput.put("Assertion 8", expectedOutAssertion8);
         hashOutput.put("Assertion 9", expectedOutAssertion9);

         for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
            hashOutput.put(entry.getKey(), entry.getValue());
         }

         testCase.setAxiomExpectedResultAxioms(hashOutput);

         for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
            hashInput.put(entry.getKey(), entry.getValue());
         }
         testCase.setAssertionsAxioms(hashInput);
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
      String classA = m.group(1);
      String classA1 = classA+"1";
      String R = m.group(2);
      String classB = "";
      classB = m.group(3);
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String noClassA =  "No"+classA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];
      String classB1 = classB+"1";

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+R+">)");
      precondition.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added */
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
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
      OWLDeclarationAxiom axiomClassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("individual2"));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLNamedIndividual indOWLnoA = dataFactory.getOWLNamedIndividual(IRI.create("individual4"));
      OWLDeclarationAxiom axiomIndividual4 = dataFactory.getOWLDeclarationAxiom(indOWLnoA);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(noClassOWLA, indOWLnoA);
      OWLNamedIndividual indOWLB1 = dataFactory.getOWLNamedIndividual(IRI.create("individual5"));
      OWLDeclarationAxiom axiomIndividual5 = dataFactory.getOWLDeclarationAxiom(indOWLB1);
      OWLClassAssertionAxiom classAssertionAxiom4 = dataFactory.getOWLClassAssertionAxiom(classOWLB1, indOWLB1);

      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass3));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom2));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClassA1));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual4));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom3));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual5));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom4));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);

      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass2));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("individual3"));
      OWLDeclarationAxiom axiomIndividual3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass3));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);
      String expectedOutAssertion2 = "consistent";


      /*Assertions*/
      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
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
      OWLSubClassOfAxiom axiomSubclass4= dataFactory.getOWLSubClassOfAxiom(classOWLB1, classOWLB);

      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));
      OWLSubClassOfAxiom axiomSubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB1, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLnoA)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWLnoA);

      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass4));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass5));
      manager.applyChanges(manager.addAxiom(ontology3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion3 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }

      OWLSubClassOfAxiom axiomSubclass6 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLnoA, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWLnoA);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology4, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology4, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass6));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom6));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass4));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology4);
      String expectedOutAssertion4 = "consistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);


      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*TEST*/
   public static TestCaseImpl participantODPTestUniversal(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (hasparticipant|isparticipantin|haslocation|islocationof|hasrole|isroleof) only (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String classA1 = classA+"1";
      String R = m.group(2);
      String classB = "";
      classB = m.group(3);
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String noClassA =  "No"+classA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];
      String classB1 = classB+"1";

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+R+">)");
      precondition.add("Class(<"+classB+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass classOWLB1 = dataFactory.getOWLClass(IRI.create(classB1));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(classOWLB1);
      OWLEquivalentClassesAxiom equivalentClassesAxiom1 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomclassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("individual2"));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLNamedIndividual indOWLnoA = dataFactory.getOWLNamedIndividual(IRI.create("individual4"));
      OWLDeclarationAxiom axiomIndividual4 = dataFactory.getOWLDeclarationAxiom(indOWLnoA);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(noClassOWLA, indOWLnoA);
      OWLNamedIndividual indOWLB1 = dataFactory.getOWLNamedIndividual(IRI.create("individual5"));
      OWLDeclarationAxiom axiomIndividual5 = dataFactory.getOWLDeclarationAxiom(indOWLB1);
      OWLClassAssertionAxiom classAssertionAxiom4 = dataFactory.getOWLClassAssertionAxiom(classOWLB1, indOWLB1);

      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass3));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom2));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology, axiomclassA1));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual4));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom3));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual5));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom4));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R));
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);

      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass2));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";
      manager.removeOntology(ontology1);



      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("individual3"));
      OWLDeclarationAxiom axiomIndividual3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass3));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);
      String expectedOutAssertion2 = "inconsistent";

      /*Assertions*/
      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
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
      OWLSubClassOfAxiom axiomSubclass4= dataFactory.getOWLSubClassOfAxiom(classOWLB1, classOWLB);
      OWLSubClassOfAxiom axiomSubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLnoA)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWLnoA);

      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass4));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass5));
      manager.applyChanges(manager.addAxiom(ontology3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion3 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }

      OWLSubClassOfAxiom axiomSubclass6 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLnoA, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWLnoA);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB1, indOWL3);
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));

      manager.applyChanges(manager.addAxiom(ontology4, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology4, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass6));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass4));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom6));
      manager.removeOntology(ontology4);
      String expectedOutAssertion4 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);


      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for subclass + OP Test*/
   public static TestCaseImpl subClassOPTest(String purpose,  TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) subclassof (.*) that (.*) some (.*)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String classAnoSymb = classA.replace(">","").replace("<","");
      String noClassA =  "No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1];
      String classA1 =  classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"1";

      String classB = m.group(2);
      String classB1 =  classB.split("(#|\\/)")[classB.split("(#)").length-1]+"1";
      String noClassB =  "No"+classB.replace(">","").replace("<","").replace(">","").replace("<","").split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String relation = m.group(3);
      String classC = "";
      classC = m.group(4);

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Class(<"+classB+">)");
      precondition.add("Property(<"+relation+">)");
      precondition.add("Class(<"+classC+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass noClassOWLA = dataFactory.getOWLClass(IRI.create(noClassA));
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLDeclarationAxiom axiomClass2 = dataFactory.getOWLDeclarationAxiom(noClassOWLA);
      OWLEquivalentClassesAxiom equivalentClassesAxiom1 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLA, dataFactory.getOWLObjectComplementOf(classOWLA));
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass3 = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass4 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom2 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClass7 = dataFactory.getOWLDeclarationAxiom(classOWLA1);

      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass2));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass3));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass4));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom2));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass7));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /************SUBCLASS TEST***************/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectIntersectionOf(noClassOWLA, classOWLB));
      manager.applyChanges(manager.addAxiom(ontology1, subClassOfAxiom1));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));

      String expectedOutAssertion1  = "consistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectIntersectionOf(classOWLA, noClassOWLB));
      manager.applyChanges(manager.addAxiom(ontology2, subClassOfAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion2  = "unsatisfiable";
      manager.removeOntology(ontology2);

      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectIntersectionOf(classOWLA, classOWLB));
      manager.applyChanges(manager.addAxiom(ontology3, subClassOfAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion3  = "consistent";
      manager.removeOntology(ontology3);
      /************OP TEST***************/
      /*Axioms to be added*/
      /*Assertions*/
      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(relation));
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLNamedIndividual indOWLC = dataFactory.getOWLNamedIndividual(IRI.create("individual2"));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLC);
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass noClassOWLC = dataFactory.getOWLClass(IRI.create("no"+classC));
      OWLEquivalentClassesAxiom equivalentClassesAxiom3 = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLC, dataFactory.getOWLObjectComplementOf(classOWLC));
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLC, indOWLC);
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLC)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLC);

      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass2));
      manager.applyChanges(manager.addAxiom(ontology4, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology4, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology4, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology4, equivalentClassesAxiom3));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion4 = "inconsistent";
      manager.removeOntology(ontology4);

      OWLOntology ontology5 = null;
      try {
         ontology5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology5 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("individual3"));
      OWLDeclarationAxiom axiomIndividual3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLC, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLC);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);
      manager.applyChanges(manager.addAxiom(ontology5, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology5, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology5, axiomSubclass3));
      manager.applyChanges(manager.addAxiom(ontology5, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology5, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology5, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology5, objectPropertyAssertionAxiom3));
      manager.applyChanges(manager.addAxiom(ontology4, equivalentClassesAxiom3));
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ontology5);

      String expectedOutAssertion5 = "consistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);
      hashInput.put("Assertion 5", assertion5);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);
      hashOutput.put("Assertion 5",expectedOutAssertion5);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   /*for  cardinality + OP*/
   public static TestCaseImpl cardinalityOPTest(String purpose, String type, TestCaseImpl testCase){
      Pattern p1 = Pattern.compile("(.*) subclassof (.*) min (\\d+) (.*) and (.*) subclassof (.*) some (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p1.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String classA1 = classA.replace(">","").replace("<","")+"1";
      String R1 = m.group(2);
      String classB = m.group(4);
      String classB1 = classB.replace(">","").replace("<","")+"1";
      String R2 = m.group(6);
      String classC = m.group(7);
      String noClassC =  "No"+classC.replace(">","").replace("<","").replace(">","").replace("<","").split("(#|\\/)")[classC.split("(#|\\/)").length-1];
      Integer num = Integer.parseInt(m.group(3).replace(" ",""));


      String ind3 = "<individual003>";

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+R1+">)");
      precondition.add("Property(<"+R2+">)");
      precondition.add("Class(<"+classB+">)");
      precondition.add("Class(<"+classC+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/

      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
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
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLB1);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(classOWLB1, indOWLB1);
      OWLNamedIndividual indOWLnoC = dataFactory.getOWLNamedIndividual(IRI.create("indC"));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLnoC);
      OWLClassAssertionAxiom classAssertionAxiom3 = dataFactory.getOWLClassAssertionAxiom(noClassOWLC, indOWLnoC);
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom3));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*A1 R max [num-1] B
       * */
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(R1));
      OWLSubClassOfAxiom subClassOfAxiom1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectMaxCardinality(num-1,prop, classOWLB));
      manager.applyChanges(manager.addAxiom(ontology1, subClassOfAxiom1));
      manager.applyChanges(manager.addAxiom(ontology1, subClassOfAxiom2));
      String expectedOutAssertion1 = "unsatisfiable";
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology1);

      /*A1 R min [num+1] B
       * */
      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom3 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectMinCardinality(num+1,prop, classOWLB));
      manager.applyChanges(manager.addAxiom(ontology2, subClassOfAxiom1));
      manager.applyChanges(manager.addAxiom(ontology2, subClassOfAxiom3));

      String expectedOutAssertion2  = "consistent";
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);

      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      OWLSubClassOfAxiom subClassOfAxiom4 = null;
      subClassOfAxiom4 = dataFactory.getOWLSubClassOfAxiom(classOWLA1,
              dataFactory.getOWLObjectMaxCardinality(num,prop, classOWLB));
      manager.applyChanges(manager.addAxiom(ontology3, subClassOfAxiom1));
      manager.applyChanges(manager.addAxiom(ontology3, subClassOfAxiom4));
      String expectedOutAssertion3 = "consistent";
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology3);

      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }
      OWLObjectProperty prop2 = dataFactory.getOWLObjectProperty(IRI.create(R2));

      OWLSubClassOfAxiom subClassOfAxiom5 = dataFactory.getOWLSubClassOfAxiom(classOWLB1,classOWLB);
      OWLSubClassOfAxiom subClassOfAxiom6 = dataFactory.getOWLSubClassOfAxiom(classOWLB1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectOneOf(indOWLnoC)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop2,
              indOWLB1, indOWLnoC);

      manager.applyChanges(manager.addAxiom(ontology4, subClassOfAxiom1));
      manager.applyChanges(manager.addAxiom(ontology4, subClassOfAxiom5));
      manager.applyChanges(manager.addAxiom(ontology4, subClassOfAxiom6));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom1));

      String expectedOutAssertion4  = "inconsistent";
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology4);

      OWLOntology ontology5 = null;
      try {
         ontology5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology5 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create(ind3));
      OWLSubClassOfAxiom subClassOfAxiom8 = dataFactory.getOWLSubClassOfAxiom(classOWLB1,
              dataFactory.getOWLObjectAllValuesFrom(prop, dataFactory.getOWLObjectOneOf(indOWLnoC, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop2,
              indOWLB1, indOWLnoC);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop2,
              indOWLB1, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology5, subClassOfAxiom5));
      manager.applyChanges(manager.addAxiom(ontology5, subClassOfAxiom8));
      manager.applyChanges(manager.addAxiom(ontology5, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology5, objectPropertyAssertionAxiom3));

      String expectedOutAssertion5 ="";
      if(purpose.contains("only"))
         expectedOutAssertion5 = "inconsistent";
      else
         expectedOutAssertion5 = "consistent";
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));

      manager.removeOntology(ontology5);


      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);
      hashInput.put("Assertion 5", assertion5);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);
      hashOutput.put("Assertion 5",expectedOutAssertion5);


      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;

   }

   /*for class definition*/
   public static TestCaseImpl classDefinitionTest(String purpose, TestCaseImpl testCase) throws OWLOntologyCreationException {

      Pattern p = Pattern.compile("(.*) type class",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);


      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      testCase.getPrecondition().addAll(precondition);
      /*There are no axioms to be added*/
      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
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
      String propertyA = m.group(1);

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Property(<"+propertyA+">)");
      testCase.getPrecondition().addAll(precondition);
      /*There are no axioms to be added*/
      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
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
      String classA = m.group(1);
      String classA1 = classA+"1";
      String relation = m.group(3);
      String classB = "";
      classB = m.group(4);
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String classC = m.group(2);
      String classC1 = classC+"1";

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+relation+">)");
      precondition.add("Class(<"+classB+">)");
      precondition.add("Class(<"+classC+">)");
      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added */
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("individual2"));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass classOWLC1 = dataFactory.getOWLClass(IRI.create(classC1));
      OWLDeclarationAxiom axiomClassC1 = dataFactory.getOWLDeclarationAxiom(classOWLC1);
      OWLNamedIndividual indOWLC1 = dataFactory.getOWLNamedIndividual(IRI.create("indC"));
      OWLDeclarationAxiom axiomIndividualC1 = dataFactory.getOWLDeclarationAxiom(indOWLC1);
      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClassA1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClassC1));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividualC1));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      /*Participant 1*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(relation));
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass2));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("individual3"));
      OWLDeclarationAxiom axiomIndividual3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass3));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);
      String expectedOutAssertion2 = "consistent";


      /*Assertions*/
      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      String inverse = "";
      if(relation.equals("isparticipantin"))
         inverse = "hasParticipant";
      else if(relation.equals("islocationof"))
         inverse = "hasLocation";
      else if(relation.equals("isroleof"))
         inverse = "hasRole";
      else if(relation.equals("haslocation"))
         inverse = "isLocationOf";
      else if(relation.equals("hasrole"))
         inverse = "isRoleOf";
      else
         inverse = "none";

      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));
      OWLSubClassOfAxiom axiomSubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass4));
      manager.applyChanges(manager.addAxiom(ontology3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion3 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }

      OWLSubClassOfAxiom axiomSubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology4, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology4, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass5));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom6));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology4);
      String expectedOutAssertion4 = "consistent";

      /*Participant 2*/
      OWLOntology ontology5 = null;
      try {
         ontology5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology5 = null;
      }
      OWLSubClassOfAxiom axiomSubclassC1= dataFactory.getOWLSubClassOfAxiom(classOWLC1, classOWLC);
      OWLSubClassOfAxiom axiomSubclassC12 = dataFactory.getOWLSubClassOfAxiom( classOWLC1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWLB);

      manager.applyChanges(manager.addAxiom(ontology5, axiomSubclassC1));
      manager.applyChanges(manager.addAxiom(ontology5, axiomSubclassC12));
      manager.applyChanges(manager.addAxiom(ontology5, objectPropertyAssertionAxiomC1));
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion5 = "inconsistent";
      manager.removeOntology(ontology5);

      OWLOntology ontology6 = null;
      try {
         ontology6 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology6 = null;
      }

      OWLSubClassOfAxiom axiomSubclassC13 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC12 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC13 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology6, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology6, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology6, axiomSubclassC13));
      manager.applyChanges(manager.addAxiom(ontology6, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology6, objectPropertyAssertionAxiomC12));
      manager.applyChanges(manager.addAxiom(ontology6, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology6, objectPropertyAssertionAxiomC13));
      OWLOntology assertion6 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology6);
      String expectedOutAssertion6 = "consistent";

      /*Assertions*/
      OWLOntology ontology7 = null;
      try {
         ontology7 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology7 = null;
      }

      OWLSubClassOfAxiom axiomSubclassC14 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLC1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC14 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLC1);

      manager.applyChanges(manager.addAxiom(ontology7, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology7, axiomSubclassC14));
      manager.applyChanges(manager.addAxiom(ontology7, objectPropertyAssertionAxiomC14));
      OWLOntology assertion7 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion7 = "inconsistent";
      manager.removeOntology(ontology7);

      OWLOntology ontology8 = null;
      try {
         ontology8 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology8 = null;
      }

      OWLSubClassOfAxiom axiomSubclassC15 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLC1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC15 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLC1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC16 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology8, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology8, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology8, classAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology8, objectPropertyAssertionAxiomC15));
      manager.applyChanges(manager.addAxiom(ontology8, axiomSubclassC15));
      manager.applyChanges(manager.addAxiom(ontology8, objectPropertyAssertionAxiomC16));
      OWLOntology assertion8 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology8);
      String expectedOutAssertion8 = "consistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);
      hashInput.put("Assertion 5", assertion5);
      hashInput.put("Assertion 6", assertion6);
      hashInput.put("Assertion 7", assertion7);
      hashInput.put("Assertion 8", assertion8);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);
      hashOutput.put("Assertion 5",expectedOutAssertion5);
      hashOutput.put("Assertion 6",expectedOutAssertion6);
      hashOutput.put("Assertion 7",expectedOutAssertion7);
      hashOutput.put("Assertion 8",expectedOutAssertion8);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      return testCase;
   }

   public static TestCaseImpl coParticipantODPTestUniversal(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) and (.*) subclassof (hascoparticipant|iscoparticipantin|cooparticipates) only (.*)", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String classA1 = classA+"1";
      String relation = m.group(3);
      String classB = m.group(4);
      String noClassB =  "No"+classB.split("(#|\\/)")[classB.split("(#|\\/)").length-1];
      String classC = m.group(2);
      String classC1 = classC+"1";

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+relation+">)");
      precondition.add("Class(<"+classB+">)");
      precondition.add("Class(<"+classC+">)");
      testCase.getPrecondition().addAll(precondition);


      /*Axioms to be added*/
      /*Preparation*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();

      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLClass noClassOWLB = dataFactory.getOWLClass(IRI.create(noClassB));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLB);
      OWLDeclarationAxiom axiomClass1 = dataFactory.getOWLDeclarationAxiom(noClassOWLB);
      OWLEquivalentClassesAxiom equivalentClassesAxiom = dataFactory.getOWLEquivalentClassesAxiom(noClassOWLB, dataFactory.getOWLObjectComplementOf(classOWLB));
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLClass classOWLA1 = dataFactory.getOWLClass(IRI.create(classA1));
      OWLDeclarationAxiom axiomClassA1 = dataFactory.getOWLDeclarationAxiom(classOWLA1);
      OWLNamedIndividual indOWLA1 = dataFactory.getOWLNamedIndividual(IRI.create("individual1"));
      OWLDeclarationAxiom axiomIndividual1 = dataFactory.getOWLDeclarationAxiom(indOWLA1);
      OWLClassAssertionAxiom classAssertionAxiom1 = dataFactory.getOWLClassAssertionAxiom(classOWLA1, indOWLA1);
      OWLNamedIndividual indOWLB = dataFactory.getOWLNamedIndividual(IRI.create("individual2"));
      OWLDeclarationAxiom axiomIndividual2 = dataFactory.getOWLDeclarationAxiom(indOWLB);
      OWLClassAssertionAxiom classAssertionAxiom2 = dataFactory.getOWLClassAssertionAxiom(noClassOWLB, indOWLB);
      OWLClass classOWLC = dataFactory.getOWLClass(IRI.create(classC));
      OWLClass classOWLC1 = dataFactory.getOWLClass(IRI.create(classC1));
      OWLDeclarationAxiom axiomClassC1 = dataFactory.getOWLDeclarationAxiom(classOWLC1);
      OWLNamedIndividual indOWLC1 = dataFactory.getOWLNamedIndividual(IRI.create("indC"));
      OWLDeclarationAxiom axiomIndividualC1 = dataFactory.getOWLDeclarationAxiom(indOWLC1);

      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, axiomClass1));
      manager.applyChanges(manager.addAxiom(ontology, equivalentClassesAxiom));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClassA1));
      manager.applyChanges(manager.addAxiom(ontology, axiomClassC1));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividualC1));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology, axiomIndividual2));
      manager.applyChanges(manager.addAxiom(ontology, classAssertionAxiom2));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      /*Assertions*/
      /*Participant 1*/
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }
      OWLSubClassOfAxiom axiomSubclass1= dataFactory.getOWLSubClassOfAxiom(classOWLA1, classOWLA);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(relation));
      OWLSubClassOfAxiom axiomSubclass2 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);

      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology1, axiomSubclass2));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom));
      OWLOntology assertion1 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion1 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology2 = null;
      try {
         ontology2 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology2 = null;
      }
      OWLNamedIndividual indOWL3 = dataFactory.getOWLNamedIndividual(IRI.create("individual3"));
      OWLDeclarationAxiom axiomIndividual3 = dataFactory.getOWLDeclarationAxiom(indOWL3);
      OWLSubClassOfAxiom axiomSubclass3 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom3 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLA1, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology2, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology2, axiomSubclass3));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology2, objectPropertyAssertionAxiom3));
      OWLOntology assertion2 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology2);
      String expectedOutAssertion2 = "inconsistent";

      /*Assertions*/
      OWLOntology ontology3 = null;
      try {
         ontology3 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology3 = null;
      }
      String inverse = "";
      if(relation.equals("isparticipantin"))
         inverse = "hasParticipant";
      else if(relation.equals("islocationof"))
         inverse = "hasLocation";
      else if(relation.equals("isroleof"))
         inverse = "hasRole";
      else if(relation.equals("haslocation"))
         inverse = "isLocationOf";
      else if(relation.equals("hasrole"))
         inverse = "isRoleOf";
      else
         inverse = "none";

      OWLObjectProperty propInverse = dataFactory.getOWLObjectProperty(IRI.create(inverse));

      OWLSubClassOfAxiom axiomSubclass4 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLA1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom4 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);

      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology3, axiomSubclass4));
      manager.applyChanges(manager.addAxiom(ontology3, objectPropertyAssertionAxiom4));
      OWLOntology assertion3 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion3 = "inconsistent";
      manager.removeOntology(ontology1);

      OWLOntology ontology4 = null;
      try {
         ontology4 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology4 = null;
      }

      OWLSubClassOfAxiom axiomSubclass5 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLA1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom5 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLA1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom6 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology4, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology4, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom5));
      manager.applyChanges(manager.addAxiom(ontology4, axiomSubclass5));
      manager.applyChanges(manager.addAxiom(ontology4, objectPropertyAssertionAxiom6));
      OWLOntology assertion4 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology4);
      String expectedOutAssertion4 = "inconsistent";

      /*Participant 2*/
      OWLOntology ontology5 = null;
      try {
         ontology5 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology5 = null;
      }
      OWLSubClassOfAxiom axiomSubclassC1= dataFactory.getOWLSubClassOfAxiom(classOWLC1, classOWLC);
      OWLSubClassOfAxiom axiomSubclassC12 = dataFactory.getOWLSubClassOfAxiom( classOWLC1, dataFactory.getOWLObjectSomeValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC1 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWLB);

      manager.applyChanges(manager.addAxiom(ontology5, axiomSubclassC1));
      manager.applyChanges(manager.addAxiom(ontology5, axiomSubclassC12));
      manager.applyChanges(manager.addAxiom(ontology5, objectPropertyAssertionAxiomC1));
      OWLOntology assertion5 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion5 = "inconsistent";
      manager.removeOntology(ontology5);

      OWLOntology ontology6 = null;
      try {
         ontology6 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology6 = null;
      }

      OWLSubClassOfAxiom axiomSubclassC13 = dataFactory.getOWLSubClassOfAxiom( classOWLA1, dataFactory.getOWLObjectAllValuesFrom(prop,dataFactory.getOWLObjectOneOf(indOWLB, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC12 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWLB);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC13 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indOWLC1, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology6, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology6, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology6, axiomSubclassC13));
      manager.applyChanges(manager.addAxiom(ontology6, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology6, objectPropertyAssertionAxiomC12));
      manager.applyChanges(manager.addAxiom(ontology6, classAssertionAxiom2));
      manager.applyChanges(manager.addAxiom(ontology6, objectPropertyAssertionAxiomC13));
      OWLOntology assertion6 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology6);
      String expectedOutAssertion6 = "inconsistent";

      /*Assertions*/
      OWLOntology ontology7 = null;
      try {
         ontology7 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology7 = null;
      }

      OWLSubClassOfAxiom axiomSubclassC14 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectSomeValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLC1)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC14 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLC1);

      manager.applyChanges(manager.addAxiom(ontology7, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology7, axiomSubclassC14));
      manager.applyChanges(manager.addAxiom(ontology7, objectPropertyAssertionAxiomC14));
      OWLOntology assertion7 = manager.getOntology(IRI.create(base));
      String expectedOutAssertion7 = "inconsistent";
      manager.removeOntology(ontology7);

      OWLOntology ontology8 = null;
      try {
         ontology8 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology8 = null;
      }

      OWLSubClassOfAxiom axiomSubclassC15 = dataFactory.getOWLSubClassOfAxiom( classOWLB, dataFactory.getOWLObjectAllValuesFrom(propInverse,dataFactory.getOWLObjectOneOf(indOWLC1, indOWL3)));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC15 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWLC1);
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiomC16 = dataFactory.getOWLObjectPropertyAssertionAxiom(propInverse, indOWLB, indOWL3);

      manager.applyChanges(manager.addAxiom(ontology8, axiomIndividual3));
      manager.applyChanges(manager.addAxiom(ontology8, axiomSubclass1));
      manager.applyChanges(manager.addAxiom(ontology8, classAssertionAxiom1));
      manager.applyChanges(manager.addAxiom(ontology8, objectPropertyAssertionAxiomC15));
      manager.applyChanges(manager.addAxiom(ontology8, axiomSubclassC15));
      manager.applyChanges(manager.addAxiom(ontology8, objectPropertyAssertionAxiomC16));
      OWLOntology assertion8 = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology8);
      String expectedOutAssertion8 = "inconsistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion1);
      hashInput.put("Assertion 2", assertion2);
      hashInput.put("Assertion 3", assertion3);
      hashInput.put("Assertion 4", assertion4);
      hashInput.put("Assertion 5", assertion5);
      hashInput.put("Assertion 6", assertion6);
      hashInput.put("Assertion 7", assertion7);
      hashInput.put("Assertion 8", assertion8);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1",expectedOutAssertion1);
      hashOutput.put("Assertion 2",expectedOutAssertion2);
      hashOutput.put("Assertion 3",expectedOutAssertion3);
      hashOutput.put("Assertion 4",expectedOutAssertion4);
      hashOutput.put("Assertion 5",expectedOutAssertion5);
      hashOutput.put("Assertion 6",expectedOutAssertion6);
      hashOutput.put("Assertion 7",expectedOutAssertion7);
      hashOutput.put("Assertion 8",expectedOutAssertion8);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
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
      String classA = m.group(1);
      String propertyA = m.group(2);
      String classB = m.group(3);

      String classAnoSymb = classA.replace(">","").replace("<","");
      String noClassA =  "No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"";

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+propertyA+">)");
      precondition.add("Class(<"+classB+">)");

      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);
      OWLClass classOWLB = dataFactory.getOWLClass(IRI.create(classB));
      OWLDeclarationAxiom axiomClassB = dataFactory.getOWLDeclarationAxiom(classOWLB);

      manager.applyChanges(manager.addAxiom(ontology, axiomClass));
      manager.applyChanges(manager.addAxiom(ontology, axiomClassB));
      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      String classAwithoutURI = noClassA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];

      /*Assertions*/
      String ind1= classAwithoutURI.toLowerCase().replace(">","").replace("<","")+"001";
      String ind2 = classAwithoutURI.toLowerCase().replace(">","").replace("<","")+"002";
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }


      OWLNamedIndividual indClassA = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomIndividualClassA = dataFactory.getOWLDeclarationAxiom(indClassA);
      OWLClassAssertionAxiom indAClassAAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA, indClassA);
      OWLNamedIndividual indClassB = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLDeclarationAxiom axiomIndividualClassB = dataFactory.getOWLDeclarationAxiom(indClassB);
      OWLClassAssertionAxiom indAClassBAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLB, indClassB);
      OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(propertyA));
      OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom2 = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, indClassA, indClassB);

      manager.applyChanges(manager.addAxiom(ontology1, axiomIndividualClassA));
      manager.applyChanges(manager.addAxiom(ontology1, indAClassAAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology1, axiomIndividualClassB));
      manager.applyChanges(manager.addAxiom(ontology1, indAClassBAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology1, objectPropertyAssertionAxiom2));

      OWLOntology assertion = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology1);

      String expectedOutAssertion = "consistent";


      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1", expectedOutAssertion);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      testCase.setType("individuals");
      return testCase;

   }

   public static TestCaseImpl domainRangeTestDP(String purpose, TestCaseImpl testCase){
      Pattern p = Pattern.compile("(.*) (.*) (xsd:string|xsd:float|xsd:integer|rdfs:literal|xsd:datetime|xsd:datetimestamp|string|float|integer|datetime|owl:rational|rational|boolean|xsd:boolean|anyuri|xsd:anyuri|literal|rdfs:literal|xsd:double|double|xsd:long|long)",Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(purpose);

      /*Generation of classes*/
      m.find();
      String classA = m.group(1);
      String propertyA = m.group(2);
      String datatypeB = m.group(3).toLowerCase();
      String classAnoSymb = classA.replace(">","").replace("<","");
      String noClassA =  "No"+classAnoSymb.split("(#|\\/)")[classA.split("(#)").length-1]+"";

      /*Preconditions*/
      ArrayList<String> precondition = new ArrayList<>();
      precondition.add("Class(<"+classA+">)");
      precondition.add("Property(<"+propertyA+">)");

      testCase.getPrecondition().addAll(precondition);

      /*Axioms to be added*/
      String base = "";
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ontology = null;
      try {
         ontology = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology = null;
      }
      OWLDataFactory dataFactory = manager.getOWLDataFactory();
      OWLClass classOWLA = dataFactory.getOWLClass(IRI.create(classA));
      OWLDeclarationAxiom axiomClass = dataFactory.getOWLDeclarationAxiom(classOWLA);

      testCase.setPreparationaxioms(manager.getOntology(IRI.create(base)));
      manager.removeOntology(ontology);

      String classAwithouturi = noClassA.split("(#|\\/)")[classA.split("(#|\\/)").length-1];

      /*Assertions*/
      String ind1= classAwithouturi.toLowerCase().replace(">","").replace("<","")+"001";
      String ind2 = classAwithouturi.toLowerCase().replace(">","").replace("<","")+"002";
      OWLOntology ontology1 = null;
      try {
         ontology1 = manager.createOntology(IRI.create(base));
      } catch (OWLOntologyCreationException e) {
         ontology1 = null;
      }


      OWLNamedIndividual indClassA = dataFactory.getOWLNamedIndividual(IRI.create(ind1));
      OWLDeclarationAxiom axiomIndividualClassA = dataFactory.getOWLDeclarationAxiom(indClassA);
      OWLClassAssertionAxiom indAClassAAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(classOWLA, indClassA);

      OWLNamedIndividual indClassB = dataFactory.getOWLNamedIndividual(IRI.create(ind2));
      OWLDataProperty prop = dataFactory.getOWLDataProperty(IRI.create(propertyA));

      OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom;
      if(datatypeB.contains("string") || datatypeB.contains("literal") || datatypeB.contains("rational")  ) {
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, "Example text");
      }else if(datatypeB.contains("boolean")) {
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("true",
                 OWL2Datatype.XSD_BOOLEAN);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, dataLiteral);
      }else if(datatypeB.contains("integer") || datatypeB.contains("long")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1",
                 OWL2Datatype.XSD_INTEGER);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, dataLiteral);
      }else if(datatypeB.contains("float")  ){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1.0",
                 OWL2Datatype.XSD_FLOAT);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, dataLiteral);
      }else if( datatypeB.contains("double") ){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("1.0",
                 OWL2Datatype.XSD_DOUBLE);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, dataLiteral);
      }else if(datatypeB.contains("uri")){
         OWLLiteral dataLiteral = dataFactory.getOWLLiteral("http://example.org/ns#",
                 OWL2Datatype.XSD_ANY_URI);
         dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, indClassA, dataLiteral);
      }else if(datatypeB.contains("datetime")){
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

      manager.applyChanges(manager.addAxiom(ontology1, axiomIndividualClassA));
      manager.applyChanges(manager.addAxiom(ontology1, indAClassAAssertionAxiom));
      manager.applyChanges(manager.addAxiom(ontology1, dataPropertyAssertionAxiom));

      OWLOntology assertion = manager.getOntology(IRI.create(base));
      manager.removeOntology(ontology1);

      String expectedOutAssertion = "consistent";

      LinkedHashMap<String, OWLOntology> hashInput = new LinkedHashMap();
      hashInput.put("Assertion 1", assertion);

      LinkedHashMap<String, String> hashOutput = new LinkedHashMap();
      hashOutput.put("Assertion 1", expectedOutAssertion);

      for (Map.Entry<String, String> entry : testCase.getAxiomExpectedResultAxioms().entrySet()) {
         hashOutput.put(entry.getKey(), entry.getValue());
      }

      testCase.setAxiomExpectedResultAxioms(hashOutput);

      for (Map.Entry<String, OWLOntology> entry : testCase.getAssertionsAxioms().entrySet()) {
         hashInput.put(entry.getKey(), entry.getValue());
      }
      testCase.setAssertionsAxioms(hashInput);
      testCase.setType("individuals");
      return testCase;

   }



}