package oeg.albafernandez.tests.controller;

import de.derivo.sparqldlapi.exceptions.QueryParserException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import oeg.albafernandez.tests.model.Result;
import oeg.albafernandez.tests.service.ThemisResultsGenerator;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/rest")
@Tag(name="Themis evaluator")
public class APIController {
    final static Logger logger = Logger.getLogger(WebController.class);


    @POST
    @Path("/results")
    @Operation(summary = "Execute tests", description = "Execute a set of tests on an ontology", method = "POST",
            responses = {
                    @ApiResponse( responseCode  = "200", description  = "Tests successfully executed",  content = @Content(mediaType = "application/json")),
                    @ApiResponse( responseCode = "204", description = "Some tests could not be executed",  content = @Content(mediaType = "application/json")),
                    @ApiResponse( responseCode = "500", description = "Internal error. Check inputs",  content = @Content(mediaType = "application/json")),
            })

    @Produces({ MediaType.APPLICATION_JSON})
    @Consumes({ MediaType.APPLICATION_JSON})
    public Response getResults(Result results){
            //@Schema(description = "Glossary of terms as a table in the form: KEY;URI", example = "{\"ontology\":[{\"Term\":\"Sensor\",\"URI\":\"http://iot.linkeddata.es/def/core#Sensor\"}]}") @FormParam("got") String got,
              //                 @Schema(description = "List of tests to be executed", example = "[\"Sensor type Class\", \"Sensor subclassOf Device\"]") @FormParam("tests") List<String> tests,
                //               @Schema(description = "List of ontologies to be analysed",example = "[\"http://iot.linkeddata.es/def/core#\", \"http://iot.linkeddata.es/def/wot#\"]") @FormParam("ontologies")  List<String> ontologies) throws JSONException, OWLOntologyStorageException, IOException, OWLOntologyCreationException, QueryParserException {
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

}
