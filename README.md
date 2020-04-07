# Themis 

This repository is a web service that publishes the services of Themis, providing an interface to execute tests on one or more ontologies, and also a REST API to be used by third-party services.

## Inputs
Themis needs two inputs:
* *Ontology URI*: the user can add one or more than one ontology, which will be loaded and its glossary of terms automatically generated
* *Test*: the user can add one or more tests. [Check here the supported tests](http://themis.linkeddata.es/tests-info.html)


## Outputs
Themis will provide the result for each test and each ontology, indicating as well the problem found (if any). Users can also download the test suite as an RDF file.

# Using the Rest API
In order to use our REST API, check our [online documentation](http://themis.linkeddata.es/swagger-ui/index.html) 

# Using the JAR 
Command:  java-jar themis.jar -t test file  -o ontology file [-g glossary of terms]  [-f format of the results]

#### Example
 java -jar themis.jar -t testsuite.ttl -f rdf -o ontology.ttl -f junit

## Inputs
The command can receive the following inputs:
* *Ontology file*: the user can add one or more than one ontology, which will be loaded and its glossary of terms automatically generated
* *Test file *: the user can add one or more tests. [Check here the supported tests](http://themis.linkeddata.es/tests-info.html),  written in RDF or RDFa following the [VTC ontology](https://w3id.org/def/vtc#).  Moreover, Themis accepts as input a text file with a list of tests that uses the following  [Themis syntax](http://themis.linkeddata.es/tests-info.html) and that are separated by ";". 
* *Glossary of terms [optional]*: the user can add a glossary of terms of the ontology to be analysed as a JSON array. If there is not glossary of terms, Themis creates one automatically with the format [key, URI] where the fragment of the URI of each term represents the key. These glossary of terms is used to map each term in the test with each term in the ontology. 
* *Format [optional]*: Themis will provide the result for each test. Three possible formats for the results are provided:
  * JSON (output by default)
  * RDFa following the  [VTC ontology](https://w3id.org/def/vtc#)
  * JUnit (see [JUnit website](https://junit.org/junit4/) for more information about the stucture of the test report)
