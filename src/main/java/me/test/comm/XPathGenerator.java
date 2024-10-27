package me.test.comm;

import me.util.FileReaderUtil;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XPathGenerator {
    private static final String VARIABLE_PATTERN = "\\$\\{\\{([a-zA-Z0-9\\-_]+?)\\}\\}";

    // Method to generate XPath map from XML string
    public Map<String, String> generateXPathMap(String xmlString) throws Exception {
        // Parse the XML string into a Document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // Important for handling namespaces
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

        Map<String, String> xpathMap = new HashMap<>();
        Map<String, String> namespaceMap = new HashMap<>();

        // Start building the XPath map from the root element
        buildXPathMap(doc.getDocumentElement(), "", xpathMap, namespaceMap);

        return xpathMap;
    }

    // Recursive method to build XPath map
    private void buildXPathMap(Node node, String currentXPath, Map<String, String> xpathMap, Map<String, String> namespaceMap) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // Get the namespace and prefix of the current node
            String nodeNamespace = node.getNamespaceURI();
            String nodePrefix = node.getPrefix();
            String nodeName = node.getLocalName();

            // Check if the node has a default namespace but no prefix, generate a dynamic prefix
            if (nodeNamespace != null && nodePrefix == null) {
                // Generate a dynamic prefix based on the node hierarchy (remove special characters)
                String generatedPrefix = generateNamespacePrefix(currentXPath, nodeName);
                namespaceMap.put(generatedPrefix, nodeNamespace); // Add to namespace map
                nodePrefix = generatedPrefix; // Use the generated prefix for this node
            }

            // Build the full node name with the prefix (if exists)
            String fullNodeName = (nodePrefix != null) ? nodePrefix + ":" + nodeName : nodeName;
            String newXPath = currentXPath.isEmpty() ? "/" + fullNodeName : currentXPath + "/" + fullNodeName;

            // Check if the node has attributes, include them in the XPath, but exclude xmlns attributes and variable placeholders
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    String attrName = attr.getNodeName();
                    String attrValue = attr.getNodeValue();

                    // Handle variables attributes, if any
                    if (isVariable(attrValue)) {
                        // Add attribute with placeholder to XPath map
                        xpathMap.put(attrValue, newXPath + "/@" + attr.getNodeName());
                    }

                    // Ignore xmlns attributes and attributes with variable placeholders
                    if (!attrName.startsWith("xmlns") && !attrValue.contains("${{")) {
                        // Add attribute conditions to XPath
                        newXPath += "[@" + attrName + "='" + attrValue + "']";
                    }
                }
            }

            // If it's a leaf node with text content, add to the XPath map
            if (node.getChildNodes().getLength() == 1 && node.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                String textContent = node.getTextContent().trim();
//                xpathMap.put(newXPath, textContent);
                xpathMap.put(textContent, newXPath);
            }

            // Recursively process child nodes
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                buildXPathMap(children.item(i), newXPath, xpathMap, namespaceMap);
            }
        }
    }

    // Helper method to generate dynamic prefix based on node hierarchy
    private String generateNamespacePrefix(String currentXPath, String nodeName) {
        // Split the XPath by "/" to get the node components
        String[] pathParts = currentXPath.split("/");

        // Concatenate the parts of the path, ignoring any prefixes (i.e., before ":")
        StringBuilder namespacePrefix = new StringBuilder();
        for (String part : pathParts) {
            if (part.contains(":")) {
                // Get only the part after the ":" (remove the existing prefix)
                namespacePrefix.append(part.split(":")[1]);
            } else if (!part.isEmpty()) {
                // No prefix, just use the node name directly
                namespacePrefix.append(part);
            }
        }

        namespacePrefix.append(nodeName);
        return namespacePrefix.toString();
    }


    // Method to evaluate XPath on given XML and return text content
    public String evaluateXPath(String xmlString, String xpathExpr) throws Exception {
        // Parse the XML string into a Document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // Important for handling namespaces
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

        // Create an XPath instance
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        // Set the custom namespace context for dynamic prefixes
        xpath.setNamespaceContext(new NamespaceContext() {
            private final Map<String, String> namespaces = discoverNamespaces(doc); // Dynamically discover namespaces

            @Override
            public String getNamespaceURI(String prefix) {
                return namespaces.getOrDefault(prefix, "");
            }

            @Override
            public String getPrefix(String namespaceURI) {
                for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                    if (entry.getValue().equals(namespaceURI)) {
                        return entry.getKey();
                    }
                }
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return namespaces.keySet().iterator();
            }
        });

        // Compile and evaluate the XPath expression
        XPathExpression expr = xpath.compile(xpathExpr);
        String result = (String) expr.evaluate(doc, XPathConstants.STRING);

        return result != null ? result : "";
    }

    // Method to discover namespaces dynamically from the document
    private Map<String, String> discoverNamespaces(Document doc) {
        Map<String, String> namespaces = new HashMap<>();
//        traverseAndCollectNamespaces(doc.getDocumentElement(), namespaces);
        /*namespaces.put("soap","http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("a","http://a.com");
        namespaces.put("b","http://b.com");
        namespaces.put("c","http://c.com");
        namespaces.put("httpcomp1com", "http://comp-1.com");
        namespaces.put("httpcomp222com","http://comp-222.com");*/

        namespaces.put("httpbcom", "http://b.com");
        namespaces.put("httpcompnc2222com", "http://comp-nc2-222.com");
        namespaces.put("httpcomp222com", "http://comp-222.com");
        namespaces.put("httpcomp1com", "http://comp-1.com");
        namespaces.put("nc2", "http://comp-nc2.com");
        namespaces.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("httpccom", "http://c.com");
        namespaces.put("httpacom", "http://a.com");

        return namespaces;
    }

    // Helper method to traverse and collect namespaces from the document
    private void traverseAndCollectNamespaces(Element element, Map<String, String> namespaces) {
        if (element != null) {
            String namespaceURI = element.getNamespaceURI();
            String prefix = element.getPrefix();
            if (namespaceURI != null && prefix != null) {
                namespaces.put(prefix, namespaceURI);
            }

            // Recursively traverse child nodes
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    traverseAndCollectNamespaces((Element) children.item(i), namespaces);
                }
            }
        }
    }

    // Helper method to check if a value is a variable (e.g., ${{someVar}})
    private boolean isVariable(String value) {
        return value != null && value.matches(VARIABLE_PATTERN);
    }

    private static String extractVariableName(String value) {
        return value.replaceAll("\\$\\{\\{", "").replaceAll("\\}\\}", "");
    }

    public static void test() {
        // Load your XML documents and call the generateXPathMap method
        String templateXml = FileReaderUtil.readFileFromResources("templates/template.xml");
        String actualXml = FileReaderUtil.readFileFromResources("templates/original_namespace.xml");
//        String actualXml = FileReaderUtil.readFileFromResources("templates/original.xml");
        Map<String, String> result = new HashMap<>();


        // Create an instance of XPathGenerator
        XPathGenerator generator = new XPathGenerator();
        try {
//            result = generator.generateXPathMap(templateXml);
//            result.put("a", "/soap:Envelope/soap:Body/a:CountryCurrency/a:sCountryISOCode");
//            result.put("b", "/soap:Envelope/soap:Body/b:CountryCurrency/b:sCountryISOCode");
//            result.put("c", "/soap:Envelope/soap:Body/c:CountryCurrency/c:sCountryISOCode");
//            result.put("x", "/soap:Envelope/soap:Body/CountryCurrency/sCountryISOCode");
//            result.put("bAttrib", "/soap:Envelope/soap:Body/b:CountryCurrency/@CodeStandard");
//            result.put("xAttrib", "/soap:Envelope/soap:Body/CountryCurrency/@CodeStandard");

//            result.put("aInnerElem", "/soap:Envelope/soap:Body/a:CountryCurrency/a:sCountryISOCode[a:otherAttrib='Other Attribute']/a:innerElem");
//            result.put("nested", "/soap:Envelope/soap:Body/httpcomp1com:nestedComplex/httpcomp1com:nextStep/httpcomp222com:newPrefix/httpcomp222com:finalStep");

//            result.put("bChildAttrib_v1", "/soap:Envelope/soap:Body/b:CountryCurrency/b:sCountryISOCode/@checkHere");
//            result.put("bChildAttrib_v2", "/soap:Envelope/soap:Body/b:CountryCurrency[@CodeStandard='ISO B']/b:sCountryISOCode/@checkHere");
//            result.put("xChildAttrib_v1", "/soap:Envelope/soap:Body/CountryCurrency/sCountryISOCode/@checkHere");
//            result.put("xChildAttrib_v2", "/soap:Envelope/soap:Body/CountryCurrency[@CodeStandard='ISO X']/sCountryISOCode/@checkHere");

            result.put("B", "/soap:Envelope/soap:Body/httpbcom:CountryCurrency[@CodeStandard='ISO B']/httpbcom:sCountryISOCode[@checkHere='true for B']");
            result.put("ISO X", "/soap:Envelope/soap:Body/CountryCurrency/@CodeStandard");
            result.put("X", "/soap:Envelope/soap:Body/CountryCurrency/sCountryISOCode");
            result.put("Inner Element", "/soap:Envelope/soap:Body/httpacom:CountryCurrency/httpacom:sCountryISOCode/httpacom:innerElem");
            result.put("Nested Element with NAMED multiple default prefix", "/soap:Envelope/soap:Body/nc2:nestedComplex2/nextStep/httpcompnc2222com:newPrefix/httpcompnc2222com:finalStep");
            result.put("ISO A", "/soap:Envelope/soap:Body/httpacom:CountryCurrency/@CodeStandard");
            result.put("Nested Element with multiple default prefix", "/soap:Envelope/soap:Body/httpcomp1com:nestedComplex/httpcomp1com:nextStep/httpcomp222com:newPrefix/httpcomp222com:finalStep");
            result.put("false for X", "/soap:Envelope/soap:Body/CountryCurrency/sCountryISOCode/@checkHere");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, String> entry : result.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            String var = extractVariableName(k);
            String theValue = "";
            try {
                theValue = generator.evaluateXPath(actualXml, v);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println( "["+ var.equals(extractVariableName(theValue)) + "]: " + var + ": [" + theValue + "] => |" + v + "|");
//            System.out.println(var + ": [" + theValue + "]");
//            System.out.println(var + ": [" + v + "]");
        }
    }
}
