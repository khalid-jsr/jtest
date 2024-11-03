package me.test.api;

import jakarta.xml.soap.*;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import me.util.FileReaderUtil;
import static me.test.api.Utils.processTemplate;


public class SoapTest {
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
}
