package me.test.comm;

import me.util.FileReaderUtil;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class TemplateAnalyzer {
    private final Map<String, String> namespaces = new HashMap<>();
    private final Map<String, String> variableXPaths = new HashMap<>();

    private void analyzeTemplate(String xmlTemplate) {
        try {
            // Parse XML document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlTemplate.getBytes()));

            // Traverse document and populate namespaces and variableXPaths
            traverseNode(document.getDocumentElement(), "", new HashMap<>());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void traverseNode(Node node, String currentXPath, Map<String, String> parentAttributes) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String prefix = element.getPrefix();
            String localName = element.getLocalName();
            String namespaceURI = element.getNamespaceURI();

            // Register namespace if it exists and isn't already registered
            if (namespaceURI != null) {
                String effectivePrefix = (prefix != null && !prefix.isEmpty()) ? prefix : generateNamespacePrefixFromURI(namespaceURI);
                if (!namespaces.containsKey(effectivePrefix)) {
                    namespaces.put(effectivePrefix, namespaceURI);
                }
                currentXPath += "/" + (effectivePrefix.isEmpty() ? "" : effectivePrefix + ":") + localName;
            } else {
                currentXPath += "/" + localName;
            }

            // Prepare attributes specific to this element, exclude xmlns attributes
            Map<String, String> elementAttributes = new HashMap<>();
            NamedNodeMap attributeNodes = element.getAttributes();
            for (int i = 0; i < attributeNodes.getLength(); i++) {
                Attr attr = (Attr) attributeNodes.item(i);
                String attrName = attr.getName();
                String attrValue = attr.getValue();

                // Process variable attributes (skip xmlns attributes)
                if (!attrName.startsWith("xmlns")) {
                    if (attrValue.startsWith("${{")) {
                        // Add variable attribute to variableXPaths map
                        String variableName = extractVariableNameFromAttribute(attrValue);
                        variableXPaths.put(variableName, currentXPath + "/@" + attrName);
                    } else {
                        // Add other non-variable attributes to the element's attribute map
                        elementAttributes.put(attrName, attrValue);
                    }
                }
            }

            // Append this element's non-variable attributes to the XPath for uniqueness
            for (Map.Entry<String, String> entry : elementAttributes.entrySet()) {
                currentXPath += "[@" + entry.getKey() + "='" + entry.getValue() + "']";
            }

            // Handle variables in text content of elements
            if (nodeHasVariable(node)) {
                String variableName = extractVariableName(node);
                variableXPaths.put(variableName, currentXPath);
            }

            // Recursively process child nodes with current element's attributes passed along
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                traverseNode(childNodes.item(i), currentXPath, elementAttributes);
            }
        }
    }

    private String extractVariableNameFromAttribute(String attributeValue) {
        return attributeValue.substring(attributeValue.indexOf("${{") + 3, attributeValue.indexOf("}}"));
    }

    private String generateNamespacePrefixFromURI(String uri) {
        return uri.replaceAll("[^a-zA-Z0-9]", "");
    }

    private boolean nodeHasVariable(Node node) {
        return node.getTextContent() != null && node.getTextContent().contains("${{");
    }

    private String extractVariableName(Node node) {
        String content = node.getTextContent();
        return content.substring(content.indexOf("${{") + 3, content.indexOf("}}"));
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public Map<String, String> getVariableXPaths() {
        return variableXPaths;
    }

    public Map<String, String> extractVariableValuesFromXML(String xmlString, Map<String, String> namespaces, Map<String, String> variableXPaths) {
        // Prepare the result map
        Map<String, String> variableValues = new HashMap<>();

        try {
            // Parse the XML string into a Document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xmlString.getBytes()));

            // Set up the XPath factory and context with namespaces
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            GenericNamespaceContext namespaceContext = new GenericNamespaceContext(namespaces); // Custom namespace context
            xPath.setNamespaceContext(namespaceContext);

            // Iterate over each variable and its XPath
            for (Map.Entry<String, String> entry : variableXPaths.entrySet()) {
                String variableName = entry.getKey();
                String xpathExpr = entry.getValue();

                try {
                    // Evaluate the XPath expression and get the result as a string
                    Node resultNode = (Node) xPath.evaluate(xpathExpr, document, XPathConstants.NODE);
                    if (resultNode != null) {
                        variableValues.put(variableName, resultNode.getTextContent());
                    } else {
                        variableValues.put(variableName, null); // Variable not found
                    }
                } catch (XPathExpressionException e) {
                    throw new Exception("Error evaluating XPath for variable " + variableName + ": " + xpathExpr, e);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return variableValues;
    }

    private static final String VARIABLE_PATTERN = "\\$\\{\\{([a-zA-Z0-9\\-_]+?)\\}\\}";

    // Helper method to check if a value is a variable (e.g., ${{someVar}})
    private boolean isVariable(String value) {
        return value != null && value.matches(VARIABLE_PATTERN);
    }

    public static void test() {
//        String template = FileReaderUtil.readFileFromResources("templates/template_3.xml");
//        String original = FileReaderUtil.readFileFromResources("templates/original_3.xml");

        String template = FileReaderUtil.readFileFromResources("templates/template_4.xml");
        String original = FileReaderUtil.readFileFromResources("templates/original_4.xml");

        Map<String, String> keyVal = new HashMap<>();

        TemplateAnalyzer analyzer = new TemplateAnalyzer();
        analyzer.analyzeTemplate(template);

        /*
        System.out.println("Namespaces:");
        for (Map.Entry<String, String> entry : analyzer.getNamespaces().entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }

        System.out.println("\nVariable XPaths:");
        for (Map.Entry<String, String> entry : analyzer.getVariableXPaths().entrySet()) {
            System.out.println("result.put(\"" + entry.getKey().replace("\n", " \\n ") + "\", \"" + entry.getValue().replace("\n", " \\n ") + "\");");
//            System.out.println(entry.getKey().replace("\n", " \\n ") + " => >|" + entry.getValue().replace("\n", " \\n ") + "|<");
//            System.out.println();
        }
        */

        keyVal = analyzer.extractVariableValuesFromXML(original, analyzer.getNamespaces(), analyzer.getVariableXPaths());
        for (Map.Entry<String, String> entry : keyVal.entrySet()) {
            System.out.println(entry.getKey() + ": [" + entry.getValue() + "]");
        }
    }
}
