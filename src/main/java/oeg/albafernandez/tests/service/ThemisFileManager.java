package oeg.albafernandez.tests.service;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semarglproject.rdf.NTriplesParser;
import org.semarglproject.rdf.ParseException;
import org.semarglproject.rdf.TurtleSerializer;
import org.semarglproject.rdf.rdfa.RdfaParser;
import org.semarglproject.sink.CharOutputSink;
import org.semarglproject.source.StreamProcessor;
import org.semarglproject.vocab.RDFa;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ThemisFileManager {

    public String loadTests(String testuri, String testfile) throws OWLOntologyStorageException,  OWLOntologyCreationException, JSONException {
        if(testuri!=null) {
            ArrayList<String> testsuiteDesign = new ArrayList<>();
            ThemisImplementer impl = new ThemisImplementer();
            try {
                testsuiteDesign.addAll(impl.loadTestCaseDesign(testuri, testfile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONArray tests = new JSONArray();
            for (String test : testsuiteDesign) {
                JSONObject obj = new JSONObject();
                if(test!=null) {
                    obj.put("Test", test.replace("^^xsd:string", ""));
                    tests.put(obj);
                }

            }
            return tests.toString();
        }else
            return null;
    }
    public List<String> loadCodeTests(String testfile) throws OWLOntologyStorageException, IOException, OWLOntologyCreationException, JSONException {
            ArrayList<String> testsuiteDesign = new ArrayList<>();
            ThemisImplementer impl = new ThemisImplementer();
            testsuiteDesign.addAll(impl.loadTestCaseDesign("", testfile));
            return  testsuiteDesign;
    }

    public  List<String> parseRDFa(String html) throws SAXException, IOException, ParseException, OWLOntologyCreationException, OWLOntologyStorageException, JSONException {
        StreamProcessor streamProcessor;
        CharOutputSink charOutputSink;
        charOutputSink = new CharOutputSink("UTF-8");
        streamProcessor = new StreamProcessor(RdfaParser.connect(TurtleSerializer.connect(charOutputSink)));
        streamProcessor.setProperty(RdfaParser.ENABLE_VOCAB_EXPANSION, true);
        streamProcessor.setProperty(RdfaParser.ENABLE_OUTPUT_GRAPH, true);
        streamProcessor.setProperty(RdfaParser.ENABLE_PROCESSOR_GRAPH, true);
        // use error-prone HTML parser to produce valid XML documents
        XMLReader reader = SAXParserImpl.newInstance(null).getXMLReader();
        streamProcessor.setProperty(StreamProcessor.XML_READER_PROPERTY, reader);
        //Reader reader2 = new InputStreamReader(url.openStream());
        InputStream targetStream = new ByteArrayInputStream(html.getBytes());

        Writer writer = new StringWriter();
        charOutputSink.connect(writer);
        streamProcessor.process(targetStream, "http://example.org#");
        return loadCodeTests(writer.toString());

    }



}
