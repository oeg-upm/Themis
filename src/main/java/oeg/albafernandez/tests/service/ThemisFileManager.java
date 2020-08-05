package oeg.albafernandez.tests.service;

import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semarglproject.rdf.ParseException;
import org.semarglproject.rdf.TurtleSerializer;
import org.semarglproject.rdf.rdfa.RdfaParser;
import org.semarglproject.sink.CharOutputSink;
import org.semarglproject.source.StreamProcessor;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ThemisFileManager {
    static final Logger logger = Logger.getLogger(ThemisExecuter.class);

    public String loadTests(String testuri, String testfile) throws  OWLOntologyCreationException, JSONException {
        if(testuri!=null) {
            ArrayList<String> testsuiteDesign = new ArrayList<>();
            ThemisImplementer impl = new ThemisImplementer();
            testsuiteDesign.addAll(impl.loadTestCaseDesign(testuri, testfile));

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
    public List<String> loadCodeTests(String testfile) throws IOException, OWLOntologyCreationException {
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
        InputStream targetStream = new ByteArrayInputStream(html.getBytes());

        Writer writer = new StringWriter();
        charOutputSink.connect(writer);
        streamProcessor.process(targetStream, "http://example.org#");
        return loadCodeTests(writer.toString());

    }



}
