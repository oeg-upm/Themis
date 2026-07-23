package oeg.albafernandez.tests;

import oeg.albafernandez.tests.service.ThemisFileManager;
import oeg.albafernandez.tests.service.ThemisResultsGenerator;
import oeg.albafernandez.tests.utils.Converter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CLI {
    public static void main(String[] args) {
        if (System.getProperty("catalina.base") == null) {
            System.setProperty("catalina.base", "target");
        }
        String ontologyFile = null;
        String testsFile = null;
        String testFormat = "txt";
        String resultFormat = "json";
        String mode = "local";
        String serverUrl = "http://themis.linkeddata.es/rest/api/results";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                ontologyFile = args[++i];
            } else if (args[i].equals("-t") && i + 1 < args.length) {
                testsFile = args[++i];
            } else if (args[i].equals("-f") && i + 1 < args.length) {
                testFormat = args[++i];
            } else if (args[i].equals("-r") && i + 1 < args.length) {
                resultFormat = args[++i];
            } else if (args[i].equals("-mode") && i + 1 < args.length) {
                mode = args[++i];
            } else if (args[i].equals("-server") && i + 1 < args.length) {
                serverUrl = args[++i];
                mode = "online";
            }
        }

        if (ontologyFile == null || testsFile == null) {
            System.out.println("Usage: java -jar jar/themis.jar -o <ontology_file> -t <tests_file> [-f txt|ttl|html] [-r json|html|junit] [-mode local|online] [-server <api_url>]");
            System.exit(1);
        }

        try {
            String ontoContent = new String(Files.readAllBytes(Paths.get(ontologyFile)), StandardCharsets.UTF_8);
            String testContent = new String(Files.readAllBytes(Paths.get(testsFile)), StandardCharsets.UTF_8);

            if (mode.equalsIgnoreCase("online")) {
                runOnline(ontoContent, testContent, testFormat, resultFormat, serverUrl);
            } else {
                runLocal(ontoContent, testContent, testFormat, resultFormat);
            }

        } catch (Exception e) {
            System.err.println("Error executing tests: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void runLocal(String ontoContent, String testContent, String testFormat, String resultFormat) throws Exception {
        List<String> tests = new ArrayList<>();
        ThemisFileManager fileManager = new ThemisFileManager();

        if (testFormat.equalsIgnoreCase("html") || testFormat.equalsIgnoreCase("rdfa")) {
            tests.addAll(fileManager.parseRDFa(testContent));
        } else if (testFormat.equalsIgnoreCase("ttl") || testFormat.equalsIgnoreCase("rdf") || testFormat.equalsIgnoreCase("owl")) {
            tests.addAll(fileManager.loadCodeTests(testContent));
        } else {
            for (String line : testContent.split(";")) {
                String clean = line.replaceAll("[\r\n]", "").trim();
                if (!clean.isEmpty()) {
                    tests.add(clean);
                }
            }
        }

        ThemisResultsGenerator executionService = new ThemisResultsGenerator();
        JSONArray results = executionService.getResults(null, tests, new ArrayList<>(), Collections.singletonList(ontoContent));

        String output;
        if (resultFormat.equalsIgnoreCase("html")) {
            output = Converter.jsonToHtml(results);
        } else if (resultFormat.equalsIgnoreCase("junit")) {
            output = Converter.jsonToJUnitXML(results);
        } else {
            output = results.toString();
        }

        System.out.println(output);
    }

    private static void runOnline(String ontoContent, String testContent, String testFormat, String resultFormat, String serverUrl) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("ontologiesCode", Collections.singletonList(ontoContent));
        payload.put("format", resultFormat);

        if (testFormat.equalsIgnoreCase("html") || testFormat.equalsIgnoreCase("rdfa")) {
            payload.put("documentationFile", testContent);
        } else {
            payload.put("testfile", testContent);
        }

        URL url = new URL(serverUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json, text/plain, */*");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                System.out.println(response.toString());
            }
        } else {
            System.err.println("HTTP Error " + responseCode + " connecting to server " + serverUrl);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                System.err.println(response.toString());
            }
            System.exit(1);
        }
    }
}
