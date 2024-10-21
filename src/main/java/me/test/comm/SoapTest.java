package me.test.comm;

import jakarta.xml.soap.*;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import me.util.FileReaderUtil;


public class SoapTest {
    private String processTemplate(String template, Map<String, String> requestMap) {
        String processedTemplate = template;
        for (Map.Entry<String, String> entry : requestMap.entrySet()) {
            String placeholder = "${{" + entry.getKey() + "}}";
            processedTemplate = processedTemplate.replace(placeholder, entry.getValue());
        }
        return processedTemplate;
    }

    protected String sendSoapRequest(String url, String xmlTemplate, Map<String, String> requestMap) {
        String soapRequest = processTemplate(xmlTemplate, requestMap);

        try {
            // Create a SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Create a SOAP Message from the request
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage();

            // Set the SOAP message content
            SOAPPart soapPart = soapMessage.getSOAPPart();
            soapPart.setContent(new StreamSource(new StringReader(soapRequest)));

            // Send the SOAP message to the endpoint
//            String endpoint = url;
            SOAPMessage soapResponse = soapConnection.call(soapMessage, url);

            // Close the connection
            soapConnection.close();

            // Return the response as a string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            soapResponse.writeTo(baos);
            return baos.toString();
        } catch (SOAPException | IOException e) {
            e.printStackTrace();
            return "Error occurred while sending SOAP request: " + e.getMessage();
        }
    }

    public static void test() {
        SoapTest st = new SoapTest();
        String xml = FileReaderUtil.readFileFromResources("templates/oorsprong_country.xml");
        String soapUrl = "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso";
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("CountryCode", "BD");

        String soapResponse = st.sendSoapRequest(soapUrl, xml, requestMap);
        System.out.println(soapResponse);
    }

    /*// SOAP request implementation
    public Map<String, String> sendSoapRequest(String xmlTemplate, Map<String, String> requestMap, String url) {
        String soapRequest = processTemplate(xmlTemplate, requestMap);

        Map<String, String> responseMap = new HashMap<>();
        try {
            // Create SOAP message
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();

            // Create envelope and body
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody body = envelope.getBody();
            SOAPBodyElement bodyElement = body.addBodyElement(envelope.createName("PaymentRequest", "ns", "http://example.com/ns"));
            for (Map.Entry<String, String> entry : requestMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                bodyElement.addChildElement(key, value);
            }

            // Send SOAP message
            URL endpoint = new URL(url);
            SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
            SOAPMessage response = connection.call(soapMessage, endpoint);

            // Extract response
            SOAPBody responseBody = response.getSOAPBody();
            responseBody.getChildElements().forEachRemaining(element -> {
                SOAPElement soapElement = (SOAPElement) element;
                responseMap.put(soapElement.getNodeName(), soapElement.getValue());
            });

            connection.close();
        } catch (SOAPException | MalformedURLException e) {
            e.printStackTrace();
        }

        return responseMap;
    }
    */
}
