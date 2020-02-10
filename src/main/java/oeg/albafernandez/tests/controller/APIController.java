package oeg.albafernandez.tests.controller;


import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import oeg.albafernandez.tests.model.Ontology;
import oeg.albafernandez.tests.model.Result;
import oeg.albafernandez.tests.service.*;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
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
@Tag(name="Themis evaluator")
public class APIController {
    final static Logger logger = Logger.getLogger(APIController.class);
    ThemisSyntaxChecker syntaxChecker = new ThemisSyntaxChecker();

    @GET
    @Path("/removegot")
    @Produces({ MediaType.APPLICATION_JSON})
    @Hidden
    /*Remove the selected ontology*/
    public Response removeOntology(@Context HttpServletRequest req,  @QueryParam("uri") String ontologyURL){
        if(ontologyURL !=null) {
            Ontology ontology = new Ontology();
            ontology.loadOntologyURL(ontologyURL.toString().replace("\"", ""));
            JSONObject obj = new JSONObject();
            try {
                obj.put("url", ontology.getOntology().getOntologyID().getOntologyIRI().toString());
                obj.put("name", "");
            } catch (JSONException e) {
                logger.error("ERROR REMOVING ONTOLOGY: "+e.getMessage());
            }
            HttpSession session = req.getSession(true);
            ArrayList<Ontology> ontologies = (ArrayList<Ontology>) session.getAttribute("ontologies");
            session.setAttribute("ontologies", ontologies);
            return Response
                    .status(200)
                    .entity(obj.toString())
                    .build();
        }else
            return Response
                    .status(200)
                    .entity("")
                    .build();
    }



    @POST
    @Path("results")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Operation(summary = "Execute tests", description = "Execute a set of tests on an ontology", method = "POST",
            responses = {
                    @ApiResponse( responseCode  = "200", description  = "Tests successfully executed",  content = @Content(mediaType = "application/json")),
                    @ApiResponse( responseCode = "204", description = "Some tests could not be executed",  content = @Content(mediaType = "application/json")),
                    @ApiResponse( responseCode = "500", description = "Internal error. Check inputs",  content = @Content(mediaType = "application/json")),
            })

    public Response getResults(Result results){
        List<String> ontologies = results.getOntologies();
        List<String> tests = results.getTests();
        String got = results.getGot();
        logger.info("New ontologies added from API: "+ ontologies);
        logger.info("New tests added from API: " + tests);
        ThemisResultsGenerator executionService = new ThemisResultsGenerator();
        int status;
        String result = "";
        try {
            result = executionService.getResults(got, tests, ontologies);
            if (result.isEmpty()) {
                status = 204;
            } else {
                status = 200;
            }

        }catch(Exception e){
            status = 500;
        }

        return  Response.status(status)
                .header("Access-Control-Allow-Origin", "*")
                .entity(result)
                .build();
    }



    @GET
    @Path("/export")
    @Produces({ MediaType.APPLICATION_JSON})
    @Hidden
    /*MEthod to export the test suite in an TTL file*/
    public Response downloadTestSuite(@QueryParam("test") final String tests) throws JSONException, OWLOntologyStorageException{
        if(tests != null) {
            String[] testsList = tests.replace("\"", "").replace("[", "").replace("]", "").split(",");

            final List<String> testsArrayList = Arrays.asList(testsList);

            StreamingOutput fileStream = new StreamingOutput() {
                @Override
                public void write(java.io.OutputStream output) throws IOException, WebApplicationException {
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
        }else
            return Response.noContent().build();

    }


    @GET
    @Path("/autocomplete")
    @Operation(summary = "Autocomplete", description = "Autocomplete based on the syntax of the tests", method = "POST",
            responses = {
                    @ApiResponse( responseCode  = "200", description  = "Autocomplete successfully executed",  content = @Content(mediaType = "application/json")),
                    @ApiResponse( responseCode = "500", description = "Internal error. Check inputs",  content = @Content(mediaType = "application/json")),
            })

    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    /*Method to autocomplete the test*/
    public Response autocomplete(@QueryParam("test") @Parameter(description = "Test", example = "Sensor type ") String test,
                                 @QueryParam("lastTerm") @Parameter(description = "Last term of the test", example = " ") String lastTerm,
                                 @QueryParam("ontology") @Parameter(description = "URI of the ontology", example = "http://iot.linkeddata.es/def/core#") String ontologyURI) throws JSONException {
        if(lastTerm == null)
            lastTerm = " ";

        return Response
                .status(200)
                .entity(syntaxChecker.autocomplete(test, lastTerm, ontologyURI))
                .build();

    }


    @GET
    @Path("/syntaxChecker")
    @Produces({ MediaType.APPLICATION_JSON})
    @Operation(summary = "Syntax checker", description = "To check the syntax of a text", method = "POST",
            responses = {
                    @ApiResponse( responseCode  = "200", description  = "Syntax checker successfully executed",  content = @Content(mediaType = "application/json")),
                    @ApiResponse( responseCode = "500", description = "Internal error. Check inputs",  content = @Content(mediaType = "application/json")),
            })
    public Response checkSyntax(@QueryParam("test") @Parameter(description = "Test", example = "Sensor type Class") String test) throws JSONException {
        return Response
                .status(200)
                .entity(syntaxChecker.syntaxChecker(test))
                .build();
    }



    @GET
    @Path("/getTableGot")
    @Operation(summary = "Glossary of terms", description = "Get glossary of terms of the ontology as an HTML table", method = "POST",
            responses = {
                    @ApiResponse( responseCode  = "200", description  = "Glossary successfully retrieved",  content = @Content(mediaType = "application/json")),
                    @ApiResponse( responseCode = "500", description = "Internal error. Check inputs",  content = @Content(mediaType = "application/json")),
            })
    @Produces({ MediaType.APPLICATION_JSON})
    /*Method to get the got of each ontology*/
    public  Response  getGoTAsTable(@QueryParam("uri") @Parameter(description = "URI of the ontology", example = "http://iot.linkeddata.es/def/core#") String URI) throws JSONException, OWLOntologyStorageException {
        return Response
                .status(200)
                .entity(syntaxChecker.getGoT(URI))
                .build();
    }


    @GET
    @Path("/getPlainGot")
    @Operation(summary = "Glossary of terms", description = "Get glossary of terms of the ontology", method = "POST",
            responses = {
                    @ApiResponse( responseCode  = "200", description  = "Glossary successfully retrieved",  content = @Content(mediaType = "application/json")),
                    @ApiResponse( responseCode = "500", description = "Internal error. Check inputs",  content = @Content(mediaType = "application/json")),
            })
    @Produces({ MediaType.APPLICATION_JSON})
    /*Method to get the got of each ontology*/
    public  Response  getPlainGoT(@QueryParam("uri") @Parameter(description = "URI of the ontology", example = "http://iot.linkeddata.es/def/core#") String URI) throws JSONException, OWLOntologyStorageException {
        return Response
                .status(200)
                .entity(syntaxChecker.getPlainGoT(URI))
                .build();
    }


    @GET
    @Path("/loadTests")
    @Produces({ MediaType.APPLICATION_JSON})
    @Hidden
    /*Method to load the RDF test design of a given URI*/
    public Response loadTests(@QueryParam("testuri") String uri) throws OWLOntologyStorageException, IOException, OWLOntologyCreationException, JSONException {
        ThemisFileManager themisFileManagement = new ThemisFileManager();
        return Response
                .status(200)
                .entity(themisFileManagement.loadTests(uri))
                .build();
    }

    @GET
    @Path("/renewsession")
    @Produces({ MediaType.TEXT_PLAIN})
    @Hidden
    /*Mehtod to renew the session and delete the previous ontologies in each refresh*/
    public Response renewsession(@Context HttpServletRequest req){
        javax.servlet.http.Cookie[] cookies = req.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);

            }
        NewCookie ontos = new NewCookie("ontos", null);
        return Response.ok("OK").cookie(ontos).build();


    }




}
