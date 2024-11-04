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
import java.util.stream.Collectors;

public class XMLTemplateProcessor extends TemplateProcessor {
    private static final Map<String, String> namespaceMap = new HashMap<>();

    private void analyzeTemplate(String xmlTemplate) {
        try {
            // Parse XML document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlTemplate.getBytes()));

            // Traverse document and populate namespaces and variableXPaths
            traverseNode(document.getDocumentElement(), "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    protected void traverseNode(Object node, String currentPath) {
        if(node instanceof Node)
            traverseNode((Node)node, currentPath);
        else
            System.out.println("Error: Object is not a Node ");
    }

    private void traverseNode(Node node, String currentXPath) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String localName = element.getLocalName();
            String namespaceURI = element.getNamespaceURI();

            // Add sibling condition if necessary
            String siblingCondition = generateSiblingCondition(node);

            // Register namespace if it exists and isn't already registered
            String effectivePrefix = generateNamespacePrefix(node);
            String prefixKey = effectivePrefix.replaceFirst(".$","");
            if (!effectivePrefix.isEmpty() && !getNamespaces().containsKey(prefixKey)) {
                addToNamespace(prefixKey, namespaceURI);
            }

            Map<String, String> elementAttributes = new HashMap<>();
            NamedNodeMap attributeNodes = element.getAttributes();
            currentXPath += siblingCondition + "/" + effectivePrefix + localName + getNodeAttribForXpath(attributeNodes);

            for (int i = 0; i < attributeNodes.getLength(); i++) {
                Attr attr = (Attr) attributeNodes.item(i);
                String attribName = attr.getName();
                String attribValue = attr.getValue();

                // Process variable attributes (skip xmlns attributes)
                if (isProperAttribute(attribName) && isVariable(attribValue)) {
                    // Add variable attribute to variableXPaths map
                    String variableName = extractVariableName(attribValue);
                    addToPath(variableName, currentXPath + "/@" + attribName);
                }
            }

            // Handle variables in text content of elements
            String nodeText = node.getTextContent();
//            if (nodeHasVariable(node)) {
            if (isVariable(nodeText)) {
                String variableName = extractVariableName(nodeText);
                addToPath(variableName, currentXPath);
            }

            // Recursively process child nodes with current element's attributes passed along
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                traverseNode(childNodes.item(i), currentXPath);
            }
        }
    }

    private String getNodeAttribForXpath(NamedNodeMap attributeNodes) {
        if(attributeNodes.getLength() == 0)
            return "";

        Map<String, String> elementAttributes = new HashMap<>();

        for (int i = 0; i < attributeNodes.getLength(); i++) {
            Attr attr = (Attr) attributeNodes.item(i);
            String attribName = attr.getName();
            String attribValue = attr.getValue();

            // skip xsi attributes like xsi:schemaLocation or xsi:nil
            // Allowed: letters, digits, hyphens, underscores, and periods
            if (isProperAttribute(attribName) && !isVariable(attribValue)) {
                // Add non-variable attributes to the element's attribute map
                elementAttributes.put(attribName, attribValue);
            }
        }

        if(elementAttributes.isEmpty())
            return "";

        return elementAttributes.entrySet().stream().map((entry) ->
                        "[@" + entry.getKey() + "='" + entry.getValue() + "']")
                .collect(Collectors.joining(""));
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
        return node.getTextContent() != null && node.getTextContent().contains("${");
    }

    private String extractVariableName(Node node) {
        String content = node.getTextContent();
        return extractVariableName(content);
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
            SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext(namespaces); // Custom namespace context
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

    protected Map<String, String> getNamespaces() {
        return namespaceMap;
    }

    protected void addToNamespace(String variableName, String namespaceValue) {
        getNamespaces().put(variableName, namespaceValue);
    }

    public static void test() {
        String template = FileReaderUtil.readFileFromResources("templates/template_5.xml");
        String content = FileReaderUtil.readFileFromResources("templates/original_5.xml");

        Map<String, String> keyVal = new HashMap<>();

        XMLTemplateProcessor analyzer = new XMLTemplateProcessor();
        analyzer.analyzeTemplate(template);
        keyVal = analyzer.extractVariableValuesFromXML(content, analyzer.getNamespaces(), analyzer.getPaths());
        int counter = 0;

        System.out.println("\nNamespaces:");
        counter = 1;
        for (Map.Entry<String, String> entry : analyzer.getNamespaces().entrySet()) {
            if (null != entry.getValue())
                System.out.println("" + counter + ". " + entry.getKey() + ": [" + entry.getValue() + "]");
            counter++;
        }

        System.out.println("\nXPaths:");
        counter = 1;
        for (Map.Entry<String, String> entry : analyzer.getPaths().entrySet()) {
            if (null != entry.getValue())
                System.out.println("" + counter + ". " + entry.getKey() + ": [" + entry.getValue() + "]");
            counter++;
        }

        System.out.println("\nVariables with values:");
        counter = 1;
        for (Map.Entry<String, String> entry : keyVal.entrySet()) {
            if (null != entry.getValue())
                System.out.println("" + counter + ". " + entry.getKey() + ": [" + entry.getValue() + "]");
            else
                System.out.println("" + counter + ".");
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
