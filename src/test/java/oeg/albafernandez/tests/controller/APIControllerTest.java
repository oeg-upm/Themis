package oeg.albafernandez.tests.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

public class APIControllerTest {

    private APIController controller;

    @BeforeEach
    public void setUp() {
        controller = new APIController();
    }

    @Test
    @DisplayName("getTestExampleFile should return 200 OK for valid ontology code input")
    public void testGetTestExampleFileValid() throws Exception {
        String ttlContent = "@prefix : <http://example.org/test#> .\n" +
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "<http://example.org/test> rdf:type owl:Ontology .\n" +
                ":Person rdf:type owl:Class .\n" +
                ":knows rdf:type owl:ObjectProperty .\n";

        Response response = controller.getTestExampleFile(ttlContent);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    @DisplayName("getTestExampleFile should return 200 OK with 'no uri' for null ontology code input")
    public void testGetTestExampleFileNull() throws Exception {
        Response response = controller.getTestExampleFile(null);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("no uri", response.getEntity());
    }

    @Test
    @DisplayName("getPlainGoT should handle null URI gracefully without throwing NullPointerException")
    public void testGetPlainGoTNullUri() throws Exception {
        String ttlContent = "@prefix : <http://example.org/test#> .\n" +
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "<http://example.org/test> rdf:type owl:Ontology .\n" +
                ":Person rdf:type owl:Class .\n";

        Response response = controller.getPlainGoT(null, ttlContent);
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }
}
