package oeg.albafernandez.tests.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import oeg.albafernandez.tests.model.AutocompleteResource;
import oeg.albafernandez.tests.model.Ontology;
import oeg.albafernandez.tests.model.Result;
import oeg.albafernandez.tests.service.*;
import oeg.albafernandez.tests.utils.Converter;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.util.*;

/*This class  defines all the functions that are called by the webapp in order to load the ontology to be validated,
 * to executeTest the tests, export the tests or get the got*/

@Path("/api")
@Tag(name = "Themis evaluator")
public class APIController {
    static final Logger logger = Logger.getLogger(APIController.class);
    ThemisGuideGenerator syntaxChecker = new ThemisGuideGenerator();


    @POST
    @Path("/example")
    @Operation(summary = "generation of test example file", description = "It generates an example file with several tests based on an ontology URI", method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Example tests successfully generated", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Internal error. Check inputs", content = @Content(mediaType = "application/json")),
            })
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTestExampleFile(@Schema(description = "Code of the ontology to extract the tests", example = "@prefix : <http://delta.linkeddata.es/def/core#> .@prefix owl: <http://www.w3.org/2002/07/owl#> .@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .@prefix xml: <http://www.w3.org/XML/1998/namespace> .@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .@base <http://delta.linkeddata.es/def/core> .<http://delta.linkeddata.es/def/core> rdf:type owl:Ontology  .:hasReward rdf:type owl:ObjectProperty ;           rdfs:domain :Customer ;           rdfs:comment \"Link between a customer and its associated reward\" ;           rdfs:label \"has reward\" .:Consumer rdf:type owl:Class ;          rdfs:subClassOf :Prosumer ;          rdfs:comment \"Entity that consumes energy\" ;          rdfs:label \"Consumer\" .:Prosumer rdf:type owl:Class ;          rdfs:comment \"Entity that consumes or produces energy\" ;          rdfs:label \"Prosumer\" .", required = true) String ontologyFile) throws OWLOntologyStorageException, IOException {
        if (ontologyFile != null) {
            Ontology ontology = new Ontology();
            if (ontologyFile.contains("\\\"")) {
                ontology.loadOntologyFromfile(ontologyFile.replaceAll("^\"", "").replaceAll("\"\\s*$", "").replace("\\\"", "\""));
            } else {
                ontology.loadOntologyFromfile(ontologyFile);
            }
            ThemisExampleGenerator exampleGenerator = new ThemisExampleGenerator();
            ArrayList<String> tests = exampleGenerator.generateExampleFromOntology(ontology);
            if (!tests.isEmpty()) {
                java.io.OutputStream output = new ByteArrayOutputStream();
                OutputStream outputs = ThemisImplementer.storeTestCaseDesign(tests, output);
                String outputString = outputs.toString();
                return Response
                        .status(200)
                        .entity(outputString)
                        .build();
            } else
                return Response
                        .status(200)
                        .entity("no tests")
                        .build();
        } else {
            return Response
                    .status(200)
                    .entity("no uri")
                    .build();
        }
    }


    @GET
    @Path("/removegot")
    @Produces({MediaType.APPLICATION_JSON})
    @Hidden
    /*Remove the selected ontology*/
    public Response removeOntology(@Context HttpServletRequest req, @QueryParam("uri") String ontologyURL) {
        if (ontologyURL != null) {
            Ontology ontology = new Ontology();
            ontology.loadOntologyFromURL(ontologyURL.replace("\"", ""));
            JSONObject obj = new JSONObject();
            try {
                obj.put("url", ontology.getOntology().getOntologyID().getOntologyIRI().toString());
                obj.put("name", "");
            } catch (JSONException e) {
                logger.error("ERROR REMOVING ONTOLOGY: " + e.getMessage());
            }
            HttpSession session = req.getSession(true);
            ArrayList<Ontology> ontologies = (ArrayList<Ontology>) session.getAttribute("ontologies");
            session.setAttribute("ontologies", ontologies.toString());
            return Response
                    .status(200)
                    .entity(obj.toString())
                    .build();
        } else
            return Response
                    .status(200)
                    .entity("")
                    .build();
    }


    @POST
    @Path("results")
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(summary = "Execute tests", description = "Execute a set of tests on an ontology", method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tests successfully executed", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "204", description = "Some tests could not be executed", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Internal error. Check inputs", content = @Content(mediaType = "application/json")),
            })
    public Response getResults(Result results) {
        List<String> ontologies = results.getOntologies();
        List<String> ontologiesCode = results.getOntologiesCode();
        List<String> tests = results.getTests();
        String testfile = results.getTestfile();
        String documentationHTML = results.getDocumentationFile();
        ThemisFileManager themisFileManagement = new ThemisFileManager();
        logger.info("Getting results....");
        if (tests == null) {
            tests = new ArrayList<>();
        }

        if (testfile != null && !testfile.isEmpty()) {
            try {
                tests.addAll(themisFileManagement.loadCodeTests(testfile));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

        }

        if (documentationHTML != null && !documentationHTML.isEmpty()) {
            try {
                tests.addAll(themisFileManagement.parseRDFa(documentationHTML));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        String got = results.getGot();
        logger.info("New ontologies added from API: " + ontologies);
        logger.info("New tests added from API: " + tests);
        ThemisResultsGenerator executionService = new ThemisResultsGenerator();
        int status;
        JSONArray result = new JSONArray();
        String output = "";

        try {
            result = executionService.getResults(got, tests, ontologies, ontologiesCode);

            if (results.getFormat().equalsIgnoreCase("html")) {
                output = Converter.jsonToHtml(result);
            } else if (results.getFormat().equalsIgnoreCase("junit")) {
                output = Converter.jsonToJUnitXML(result);
            } else {
                output = result.toString();
            }

            if (output.isEmpty()) {
                status = 204;
            } else {
                status = 200;
            }

        } catch (Exception e) {
            status = 500;
            logger.error(e.getMessage());
        }



        return Response.status(status)
                .entity(output)
                .build();
    }


    @GET
    @Path("/export")
    @Produces({MediaType.APPLICATION_JSON})
    @Hidden
    /*Method to export the test suite in an TTL file*/
    public Response downloadTestSuite(@QueryParam("test") final String tests)  {
        if (tests != null) {
            String[] testsList = tests.replace("\"", "").replace("[", "").replace("]", "").split(",");

            final List<String> testsArrayList = Arrays.asList(testsList);

            StreamingOutput fileStream = new StreamingOutput() {
                @Override
                public void write(java.io.OutputStream output) throws  WebApplicationException {
                    try {
                        OutputStream outputs = ThemisImplementer.storeTestCaseDesign(testsArrayList, output);
                        outputs.flush();
                    } catch (Exception e) {
                        throw new WebApplicationException("File Not Found !!");
                    }
                }
            };
            return Response
                    .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                    .header("content-disposition", "attachment; filename = testsuite.ttl")
                    .build();
        } else
            return Response.noContent().build();

    }


    @GET
    @Path("/autoComplete")
    @Operation(summary = "Autocomplete", description = "Autocomplete based on the syntax of the tests", method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Autocomplete successfully executed", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Internal error. Check inputs", content = @Content(mediaType = "application/json")),
            })

    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    /*Method to autoComplete the test*/
    public Response autocomplete(@QueryParam("test") @Parameter(description = "Test", example = "Sensor type ") String test,
                                 @QueryParam("lastTerm") @Parameter(description = "Last term of the test", example = " ") String lastTerm,
                                 @QueryParam("ontologyfile") @Parameter(description = "Ontology file", example = " ") String filename,
                                 @QueryParam("ontology") @Parameter(description = "URI of the ontology", example = "http://iot.linkeddata.es/def/core#") String ontologyURI) throws JSONException {
        if (lastTerm == null)
            lastTerm = " ";
        String got = syntaxChecker.autoComplete(test, lastTerm, ontologyURI, filename);

        return Response
                .status(200)
                .entity(got)
                .build();

    }


    @POST
    @Path("/autocompleteFromUriFile")
    @Operation(summary = "Autocomplete", description = "Autocomplete based on the syntax of the tests", method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Autocomplete successfully executed", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Internal error. Check inputs", content = @Content(mediaType = "application/json")),
            })

    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    /*Method to autoComplete the test*/
    public Response autocompleteUriFile(AutocompleteResource autocompleteResource) throws JSONException {
        String lastTerm;
        if (autocompleteResource.getLastTerm() == null)
            lastTerm = " ";
        else
            lastTerm = autocompleteResource.getLastTerm();
        String test = autocompleteResource.getTest();

        String ontologyURI;
        if (autocompleteResource.getOntologyUri() == null)
            ontologyURI = "";
        else
            ontologyURI = autocompleteResource.getOntologyUri();

        String ontologyFile;
        if (autocompleteResource.getCode() == null)
            ontologyFile = "";
        else
            ontologyFile = autocompleteResource.getCode().replace("</http:>", "").replace("</https:>", "");

        String got = syntaxChecker.autoComplete(test, lastTerm, ontologyURI, ontologyFile);


        return Response
                .status(200)
                .entity(got)
                .build();

    }


    @GET
    @Path("/syntaxChecker")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Syntax checker", description = "To check the syntax of a text", method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Syntax checker successfully executed", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Internal error. Check inputs", content = @Content(mediaType = "application/json")),
            })
    public Response checkSyntax(@QueryParam("test") @Parameter(description = "Test", example = "Sensor type Class") String test) throws JSONException {
        return Response
                .status(200)
                .entity(syntaxChecker.syntaxChecker(test))
                .build();
    }


    @POST
    @Path("/gotAsTableFromFile")
    @Operation(summary = "Glossary of terms", description = "Get glossary of terms of the ontology as an HTML table", method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Glossary successfully retrieved", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Internal error. Check inputs", content = @Content(mediaType = "application/json")),
            })
    @Produces({MediaType.APPLICATION_JSON})
    /*Method to get the got of each ontology*/
    public Response getGoTAsTableFile(@Schema(description = "Code of the ontology associated to the glossary of term to be extracted", required = true)
                                              String filename) {

        String got = null;
        logger.info("Ontology: "+ filename);

        try {
            got = syntaxChecker.getGoTFromFilename(filename);
            if (got != null) {
                return Response
                        .status(200)
                        .entity(got)
                        .build();
            } else {
                return Response
                        .status(400)
                        .entity("The ontology could not be loaded. Please check that the ontology syntax is correct. If this error persists please contact with albafernandez@fi.upm.es ")
                        .build();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response
                    .status(400)
                    .entity("The ontology could not be loaded. If this error persists please contact with albafernandez@fi.upm.es ")
                    .build();
        }


    }

    @POST
    @Path("/gotAsTableFromURI")
    @Operation(summary = "Glossary of terms", description = "Get glossary of terms of the ontology as an HTML table", method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Glossary successfully retrieved", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Internal error. Check inputs", content = @Content(mediaType = "application/json")),
            })
    @Produces({MediaType.APPLICATION_JSON})
    /*Method to get the got of each ontology*/
    public Response getGoTAsTable(@Schema(description = "URI of the ontology associated to the glossary of term to be extracted", required = true)
                                          String URI) {


        String got = null;
        logger.info("Ontology: "+ URI);
        System.out.println(URI);
        try {
            got = syntaxChecker.getGoTFromURI(URI);

            if (got != null) {
                return Response
                        .status(200)
                        .entity(got)
                        .build();
            } else {
                return Response
                        .status(400)
                        .entity("The ontology could not be loaded. Please check that the ontology URI is correct. If this error persists please contact with albafernandez@fi.upm.es ")
                        .build();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Response
                    .status(400)
                    .entity("The ontology could not be loaded. If this error persists please contact with albafernandez@fi.upm.es ")
                    .build();
        }


    }


    @POST
    @Path("/plainGot")
    @Operation(summary = "Glossary of terms", description = "Get glossary of terms of the ontology", method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Glossary successfully retrieved", content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "500", description = "Internal error. Check inputs", content = @Content(mediaType = "application/json")),
            })
    @Produces({MediaType.APPLICATION_JSON})
    /*Method to get the got of each ontology*/
    public Response getPlainGoT(@QueryParam("uri") @Parameter(description = "URI of the ontology", example = "http://iot.linkeddata.es/def/core#") String URI,
                                @QueryParam("filename") @Parameter(description = "Ontology file", example = "") String filename
    ) throws JSONException, OWLOntologyStorageException {
        String got;
        if (URI != null || !URI.isEmpty())
            got = syntaxChecker.getPlainGoTFromURI(URI);
        else
            got = syntaxChecker.getGoTFromFilename(filename);

        return Response
                .status(200)
                .entity(got)
                .build();
    }


    @POST
    @Path("/loadTests")
    @Produces({MediaType.APPLICATION_JSON})
    @Hidden
    /*Method to load the RDF test design of a given URI*/
    public Response loadTests(String uri) {
        ThemisFileManager themisFileManagement = new ThemisFileManager();
        String tests = null;
        try {
            logger.info("Loading tests...");

            tests = themisFileManagement.loadTests(uri, "");
            if (tests == null || tests.equalsIgnoreCase("[]")) {
                return Response
                        .status(204)
                        .entity("No tests found")
                        .build();
            }
        } catch (Exception e) {
            return Response
                    .status(400)
                    .entity("The tests cannot be loaded. Please check the URI.  If this error persists please contact with albafernandez@fi.upm.es")
                    .build();
        }
        return Response
                .status(200)
                .entity(tests)
                .build();
    }

    @POST
    @Path("/loadTestsFromFile")
    @Produces({MediaType.APPLICATION_JSON})
    @Hidden
    /*Method to load the RDF test design of a given URI*/
    public Response loadTestsFile(String file) {
        ThemisFileManager themisFileManagement = new ThemisFileManager();
        String tests = null;
        try {
            logger.info("Loading tests...");
            tests = themisFileManagement.loadTests("", file);
            if (tests == null || tests.equalsIgnoreCase("[]")) {
                return Response
                        .status(204)
                        .entity("No tests found")
                        .build();
            }

        } catch (Exception e) {
            return Response
                    .status(400)
                    .entity("The tests cannot be loaded. Please check the URI.  If this error persists please contact with albafernandez@fi.upm.es")
                    .build();
        }
        return Response
                .status(200)
                .entity(tests)
                .build();
    }


    @GET
    @Path("/renewsession")
    @Produces({MediaType.TEXT_PLAIN})
    @Hidden
    /*Mehtod to renew the session and delete the previous ontologies in each refresh*/
    public Response renewsession(@Context HttpServletRequest req) {
        javax.servlet.http.Cookie[] cookies = req.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);

            }
        NewCookie ontos = new NewCookie("ontos", null);
        ontos.isHttpOnly();
        return Response.ok("OK").cookie(ontos).build();


    }


}
