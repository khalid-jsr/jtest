package me.test.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import me.util.FileReaderUtil;
import me.util.HTTPVerb;
import static me.test.api.Utils.processTemplate;

public class RestTest {
    protected String sendRestRequest(String url, String payload, Map<String, String> headers, Map<String, String> requestMap, HTTPVerb httpVerb) {
        try {
            // Process any placeholders in the request URL or body using the template processor
            String processedEndpoint = processTemplate(url, requestMap);
            String requestBody = "";  // Only needed for POST, PUT requests

            // Build the request based on HTTP Verb
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(processedEndpoint));
            // Set headers
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.header(entry.getKey(), entry.getValue());
                }
            }

            switch (httpVerb) {
                case GET:
                    requestBuilder.GET();
                    break;
                case POST:
                    // If it's a POST, build the request body based on requestMap
                    requestBody = processTemplate(payload, requestMap);
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
                    break;
                case PUT:
                    requestBody = processTemplate(payload, requestMap);
                    requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(requestBody));
                    break;
                case DELETE:
                    requestBuilder.DELETE();
                    break;
            }

            // Send the request using HttpClient
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            // Return the response body
            return response.body();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return "Error occurred while sending REST request: " + e.getMessage();
        }
    }


    public static void test() {
        RestTest rt = new RestTest();
        String payload = FileReaderUtil.readFileFromResources("templates/rest_api.json");
        String restUrl = "https://apitest.authorize.net/xml/v1/request.api";
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("id", "4f7Wp36Lsq");
        requestMap.put("key", "46M2fa35yRbXf2H8");

        Map<String, String> headers = new HashMap<>();
        requestMap.put("Content-Type", "application/json");

        String soapResponse = rt.sendRestRequest(restUrl, payload, headers, requestMap, HTTPVerb.POST);
        System.out.println(soapResponse);
    }
}