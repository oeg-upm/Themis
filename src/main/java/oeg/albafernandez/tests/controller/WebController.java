package oeg.albafernandez.tests.controller;


import de.derivo.sparqldlapi.exceptions.QueryParserException;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import oeg.albafernandez.tests.model.Ontology;
import oeg.albafernandez.tests.service.*;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
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
public class WebController {
    final static Logger logger = Logger.getLogger(WebController.class);
    ThemisSyntaxChecker syntaxChecker = new ThemisSyntaxChecker();

    @GET
    @Path("/removegot")
    @Produces({ MediaType.APPLICATION_JSON})
    @Hidden
    /*Remove the selected ontology*/

    public Response removeOntology(@Context HttpServletRequest req,  @QueryParam("uri") String ontologyURL){
        if(ontologyURL !=null) {
            Ontology ontology = new Ontology();
            ontology.load_ontologyURL(ontologyURL.toString().replace("\"", ""));
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
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Headers",
                            "origin, content-type, accept, authorization")
                    .header("Access-Control-Allow-Methods",
                            "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .entity(obj.toString())
                    .build();
        }else
            return Response
                    .status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Headers",
                            "origin, content-type, accept, authorization")
                    .header("Access-Control-Allow-Methods",
                            "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .entity("")
                    .build();
    }


    @POST
    @Path("results")
    @Hidden
    @Produces({ MediaType.APPLICATION_JSON})
    public  Response  getResults( @Schema(description = "Glossary of terms as a table in the form: KEY;URI", example = "{\"ontology\":[{\"Term\":\"Sensor\",\"URI\":\"http://iot.linkeddata.es/def/core#Sensor\"}]}") @FormParam("got") String got,
                                  @Schema(description = "List of tests to be executed", example = "[\"Sensor type Class\", \"Sensor subclassOf Device\"]") @FormParam("tests") List<String> testsJSON,
                                  @Schema(description = "List of ontologies to be analysed",example = "[\"http://iot.linkeddata.es/def/core#\", \"http://iot.linkeddata.es/def/wot#\"]") @FormParam("ontologies")  List<String> ontologiesJSON) throws JSONException, OWLOntologyStorageException, IOException, OWLOntologyCreationException, QueryParserException {

        ArrayList<String> ontologies = new ArrayList<String>(Arrays.asList(ontologiesJSON.get(0).replace("[","").replace("]","")));
        ArrayList<String> tests = new ArrayList<String>(Arrays.asList(testsJSON.get(0).replace("[","").replace("]","")));
        logger.info("New tests added:" + tests);
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
    public Response getdownload(@QueryParam("test") final String tests) throws JSONException, OWLOntologyStorageException{
        if(tests != null) {
            String[] testsList = tests.replace("\"", "").replace("[", "").replace("]", "").split(",");

            final List<String> testsArrayList = Arrays.asList(testsList);

            StreamingOutput fileStream = new StreamingOutput() {
                @Override
                public void write(java.io.OutputStream output) throws IOException, WebApplicationException {
                    try {
                        OutputStream outputs = ThemisImplementationService.storeTestCaseDesign(testsArrayList, output);
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
    @Path("/gotautocomplete")
    @Produces({ MediaType.APPLICATION_JSON})
    @Hidden
    /*Method to autocomplete the test*/
    public Response got(@QueryParam("completetest") String test, @QueryParam("term")String pattern,  @QueryParam("ontology") String ontologyuri) throws JSONException {
        return Response
                .status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers",
                        "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Methods",
                        "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .entity(syntaxChecker.got(test, pattern, ontologyuri))
                .build();

    }


    @GET
    @Path("/syntaxChecker")
    @Produces({ MediaType.APPLICATION_JSON})
    @Hidden
    public Response syntaxChecker (@QueryParam("completetest") String test) throws JSONException, ParseException {
        return Response
                .status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers",
                        "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Methods",
                        "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .entity(syntaxChecker.syntaxChecker(test))
                .build();
    }


    @GET
    @Path("/getgot")
    @Produces({ MediaType.APPLICATION_JSON})
    @Hidden
    /*Method to get the got of each ontology*/
    public  Response  getGoT(@QueryParam("uri") String uri) throws JSONException, OWLOntologyStorageException {
        return Response
                .status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers",
                        "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Methods",
                        "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .entity(syntaxChecker.getGoT(uri))
                .build();
    }

    @GET
    @Path("/loadTests")
    @Produces({ MediaType.APPLICATION_JSON})
    @Hidden
    /*Method to load the RDF test design of a given URI*/
    public Response loadTests(@QueryParam("testuri") String uri) throws OWLOntologyStorageException, IOException, OWLOntologyCreationException, JSONException {
        ThemisFileManagement themisFileManagement = new ThemisFileManagement();
        return Response
                .status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers",
                        "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Methods",
                        "GET, POST, PUT, DELETE, OPTIONS, HEAD")
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
