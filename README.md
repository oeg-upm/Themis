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

