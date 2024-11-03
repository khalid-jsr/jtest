package me.test.api;

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

public class XMLTemplateProcessor {
    private final Map<String, String> namespaces = new HashMap<>();
    private final Map<String, String> variableXPaths = new HashMap<>();
    private final static String ATTRIBUTE_NAME_PATTERN = ".*[^a-zA-Z0-9_\\-\\.]+.*";
    private static final String VARIABLE_PATTERN = "\\$\\{\\{([a-zA-Z0-9\\-_]+?)\\}\\}";


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
            String localName = element.getLocalName();
            String namespaceURI = element.getNamespaceURI();

            // Add sibling condition if necessary
            String siblingCondition = generateSiblingCondition(node);

            // Register namespace if it exists and isn't already registered
            String effectivePrefix = generateNamespacePrefix(node);
            if (!effectivePrefix.isEmpty() && !namespaces.containsKey(effectivePrefix.replaceFirst(".$",""))) {
                namespaces.put(effectivePrefix.replaceFirst(".$",""), namespaceURI);
            }
            currentXPath += siblingCondition + "/" + effectivePrefix + localName;

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
                    }
                    // skip xsi attributes like xsi:schemaLocation or xsi:nil
                    // Allowed: letters, digits, hyphens, underscores, and periods
                    else if(!attrName.matches(ATTRIBUTE_NAME_PATTERN)) {
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

    private String generateSiblingCondition(Node node) {
        Node parent = node.getParentNode();
        NodeList siblings = parent.getChildNodes();
        String nodeName = node.getNodeName();
        StringBuilder condition = new StringBuilder();
        Map<String, String> siblingMap = new HashMap<>();

        // Count siblings with the same node name
        int siblingCount = 0;
        for (int i = 0; i < siblings.getLength(); i++) {
            Node sibling = siblings.item(i);
            String nodeText = sibling.getTextContent().trim();
            if (isLeafNode(node) && isLeafNode(sibling) && !sibling.getNodeName().equals(nodeName) && !isVariable(nodeText) && !nodeText.isEmpty()) {
                siblingMap.put(generateNamespacePrefix(sibling) + sibling.getLocalName(), nodeText);
            }
        }

        for(Map.Entry<String, String> entry : siblingMap.entrySet()) {
            condition.append("[").append(entry.getKey()).append("='").append(entry.getValue()).append("']");
        }

        return condition.toString();
    }

    private String extractVariableNameFromAttribute(String attributeValue) {
        return attributeValue.substring(attributeValue.indexOf("${{") + 3, attributeValue.indexOf("}}"));
    }


    private String generateNamespacePrefix(Node node) {
        Element element = (Element) node;
        return generateNamespacePrefix(element.getPrefix(), element.getNamespaceURI());
    }

    private String generateNamespacePrefix(String prefix, String uri) {
        String p = null;
        if (prefix != null && !prefix.isEmpty())
            p = prefix ;
        else if (uri != null && !uri.isEmpty())
            p = uri.replaceAll("[^a-zA-Z0-9]", "");

        return p==null ? "" : (p + ":");
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

    // Helper method to check if a value is a variable (e.g., ${{someVar}})
    private boolean isVariable(String value) {
        return value != null && value.matches(VARIABLE_PATTERN);
    }

    // Helper method to check if a node is a leaf (i.e., has no child elements)
    private boolean isLeafNode(Node node) {
        if(node.getNodeType() != Node.ELEMENT_NODE)
            return false;

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return false;
            }
        }
        return true;
    }

    private static String removePrefix(String nodeName) {
        if(!nodeName.contains(":"))
            return nodeName;

        return nodeName.split(":")[1];
    }


    public static void test() {
        String template = FileReaderUtil.readFileFromResources("templates/template_6.xml");
        String content = FileReaderUtil.readFileFromResources("templates/original_6.xml");

        Map<String, String> keyVal = new HashMap<>();

        XMLTemplateProcessor analyzer = new XMLTemplateProcessor();
        analyzer.analyzeTemplate(template);
        keyVal = analyzer.extractVariableValuesFromXML(content, analyzer.getNamespaces(), analyzer.getVariableXPaths());
        int counter = 0;

        System.out.println("\nXPaths:");
        counter = 1;
        for (Map.Entry<String, String> entry : analyzer.variableXPaths.entrySet()) {
            if (null != entry.getValue())
                System.out.println("" + counter + ". " + entry.getKey() + ": [" + entry.getValue() + "]");
            counter++;
        }

        System.out.println("\nVariables with values:");
        counter = 1;
        for (Map.Entry<String, String> entry : keyVal.entrySet()) {
            if (null != entry.getValue())
                System.out.println("" + counter + ". " + entry.getKey() + ": [" + entry.getValue() + "]");
            counter++;
        }

        System.out.println("\nVariables NOT found:");
        counter = 1;
        for (Map.Entry<String, String> entry : keyVal.entrySet()) {
            if (null == entry.getValue())
                System.out.println("" + counter + ". " + entry.getKey() + ": [" + entry.getValue() + "]");
            counter++;
        }
    }
}
