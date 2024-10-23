package me.test.comm;

import me.util.FileReaderUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Element;


public class XPathGenerator {
    private static final String VARIABLE_PATTERN = "\\$\\{\\{([a-zA-Z0-9\\-_]+?)\\}\\}";

    private XPath xpath = XPathFactory.newInstance().newXPath();

    public Map<String, String> generateXPathMap(String xmlString) throws Exception {
        // Parse the input XML string into a Document
        Document templateDoc = loadXMLFromString(xmlString);

        // Get the root element (assuming the template XML has a single root)
        Node root = templateDoc.getDocumentElement();

        // Prepare the map to store placeholders and their corresponding XPath expressions
        Map<String, String> xpathMap = new HashMap<>();

        // Start the XPath generation from the root element
        traverseAndGenerateXPaths(root, "", xpathMap);

        return xpathMap;
    }

    private Document loadXMLFromString(String xmlString) throws Exception {
        // Create a DocumentBuilderFactory and DocumentBuilder to parse the XML string
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);  // Enable namespace-aware parsing
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse the XML string into a Document
        return builder.parse(new InputSource(new StringReader(xmlString)));
    }

    /*
    private void traverseAndGenerateXPaths(Node node, String currentXPath, Map<String, String> xpathMap) throws Exception {
        // Skip non-element nodes
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        // Generate the current XPath for this node
        String nodeName = node.getNodeName();
        String xpathForNode = currentXPath.isEmpty() ? nodeName : currentXPath + "/" + nodeName;

        // Handle attributes, if any
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (isVariable(attribute.getNodeValue())) {
                    // Add attribute with placeholder to XPath map
                    xpathMap.put(attribute.getNodeValue(), xpathForNode + "/@" + attribute.getNodeName());
                }
            }
        }

        // If it's a leaf node and contains a variable, add it to the XPath map
        if (isLeafNode(node) && isVariable(node.getTextContent())) {
            xpathMap.put(node.getTextContent(), xpathForNode);
        }

        // Check if the node is part of an array of objects (e.g., KeyAttrib)
        if (hasSiblingsWithSameName(node)) {
            String siblingCondition = generateSiblingCondition(node);
            xpathForNode += "[" + siblingCondition + "]";
        }

        // Traverse children recursively
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            traverseAndGenerateXPaths(children.item(i), xpathForNode, xpathMap);
        }
    }
    */

    /*
    private void traverseAndGenerateXPaths(Node node, String currentXPath, Map<String, String> xpathMap) throws Exception {
        // Skip non-element nodes
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        // Generate the current XPath for this node
        String nodeName = node.getNodeName();
        String xpathForNode = currentXPath.isEmpty() ? nodeName : currentXPath + "/" + nodeName;

        // Check if the node has siblings with the same name
        boolean hasSameNameSiblings = hasSiblingsWithSameName(node);

        // Handle attributes, if any
        NamedNodeMap attributes = node.getAttributes();
        StringBuilder attributeCondition = new StringBuilder();

        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (isVariable(attribute.getNodeValue())) {
                    // Add attribute with placeholder to XPath map
                    xpathMap.put(attribute.getNodeValue(), xpathForNode + "/@" + attribute.getNodeName());
                }
                if (hasSameNameSiblings || isVariable(node.getTextContent())) {
                    // Append attribute condition for XPath uniqueness if needed
                    attributeCondition.append("[@").append(attribute.getNodeName()).append("='").append(attribute.getNodeValue()).append("']");
                }
            }
        }

        // Update XPath to include attribute-based uniqueness if there are same-named siblings
        if (hasSameNameSiblings) {
            xpathForNode += attributeCondition.toString();
        }

        // If it's a leaf node and contains a variable, add it to the XPath map
        if (isLeafNode(node) && isVariable(node.getTextContent())) {
            xpathMap.put(node.getTextContent(), xpathForNode);
        }

        // Traverse children recursively
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            traverseAndGenerateXPaths(children.item(i), xpathForNode, xpathMap);
        }
    }
    */

    /*
    private void traverseAndGenerateXPaths(Node node, String currentXPath, Map<String, String> xpathMap) throws Exception {
        // Skip non-element nodes
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        // Generate the current XPath for this node, ensuring the root starts with "/"
        String nodeName = node.getNodeName();
        String xpathForNode = currentXPath.isEmpty() ? "/" + nodeName : currentXPath + "/" + nodeName;

        // Check if the node has siblings with the same name
        boolean hasSameNameSiblings = hasSiblingsWithSameName(node);

        // Handle attributes, if any
        NamedNodeMap attributes = node.getAttributes();
        StringBuilder attributeCondition = new StringBuilder();

        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (isVariable(attribute.getNodeValue())) {
                    // Add attribute with placeholder to XPath map
                    xpathMap.put(attribute.getNodeValue(), xpathForNode + "/@" + attribute.getNodeName());
                }
                if (hasSameNameSiblings || isVariable(node.getTextContent())) {
                    // Append attribute condition for XPath uniqueness if needed
                    attributeCondition.append("[@").append(attribute.getNodeName()).append("='").append(attribute.getNodeValue()).append("']");
                }
            }
        }

        // Update XPath to include attribute-based uniqueness if there are same-named siblings
        if (hasSameNameSiblings) {
            xpathForNode += attributeCondition.toString();
        }

        // If it's a leaf node and contains a variable, add it to the XPath map
        if (isLeafNode(node) && isVariable(node.getTextContent())) {
            xpathMap.put(node.getTextContent(), xpathForNode);
        }

        // Traverse children recursively
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            traverseAndGenerateXPaths(children.item(i), xpathForNode, xpathMap);
        }
    }
    */

    private void traverseAndGenerateXPaths(Node node, String currentXPath, Map<String, String> xpathMap) throws Exception {
        // Skip non-element nodes
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        // Generate the current XPath for this node
        String nodeName = node.getNodeName();
        String xpathForNode = currentXPath.isEmpty() ? "/" + nodeName : currentXPath + "/" + nodeName;

        // Check if the node has siblings with the same name
        boolean hasSameNameSiblings = hasSiblingsWithSameName(node);

        // Handle attributes, if any
        NamedNodeMap attributes = node.getAttributes();
        StringBuilder attributeCondition = new StringBuilder();

        // Track whether an attribute was already added for sibling differentiation
        boolean attributeUsedForSibling = false;

        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (isVariable(attribute.getNodeValue())) {
                    // Add attribute with placeholder to XPath map
                    xpathMap.put(attribute.getNodeValue(), xpathForNode + "/@" + attribute.getNodeName());
                }
                if (!attributeUsedForSibling && (hasSameNameSiblings || isVariable(node.getTextContent()))) {
                    // Use the attribute to create unique XPath condition if there are siblings with the same name
                    attributeCondition.append("[@").append(attribute.getNodeName()).append("='").append(attribute.getNodeValue()).append("']");
                    attributeUsedForSibling = true;
                }
            }
        }

        // If no attribute was used for sibling differentiation, look for unique children or sibling nodes
        if (hasSameNameSiblings && !attributeUsedForSibling) {
            // Look for unique children or content to differentiate siblings
            Node uniqueChild = findUniqueChildOrAttribute(node);
            if (uniqueChild != null) {
                // Add condition based on the unique child node or attribute's value
                attributeCondition.append("[").append(uniqueChild.getNodeName()).append("='").append(uniqueChild.getTextContent().trim()).append("']");
            }
        }

        // Update XPath with attribute-based uniqueness or sibling-based uniqueness
        xpathForNode += attributeCondition.toString();

        // If it's a leaf node and contains a variable, add it to the XPath map
        if (isLeafNode(node) && isVariable(node.getTextContent())) {
            xpathMap.put(node.getTextContent(), xpathForNode);
        }

        // Traverse children recursively
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            traverseAndGenerateXPaths(children.item(i), xpathForNode, xpathMap);
        }
    }


    // Method to find a unique child or attribute to distinguish siblings
    private Node findUniqueChildOrAttribute(Node node) {
        // First, try to find a unique child node
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && !child.getTextContent().trim().isEmpty()) {
                return child; // Return the first non-empty child node
            }
        }

        // If no unique child is found, try attributes
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null && attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (!attribute.getNodeValue().isEmpty()) {
                    return attribute; // Return the first non-empty attribute
                }
            }
        }

        return null; // No unique child or attribute found
    }

    // Generates sibling condition based on key-value pairs or sibling nodes (e.g., AttribName in KeyAttrib)
    private String generateSiblingCondition(Node node) {
        String condition = "";
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && !child.getTextContent().trim().isEmpty()) {
                condition = child.getNodeName() + "='" + child.getTextContent().trim() + "'";
                break; // We just need one unique condition
            }
        }
        return condition;
    }

    // Checks if the node is a leaf node (no element children)
    private boolean isLeafNode(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return false;
            }
        }
        return true;
    }

    // Checks if the node has siblings with the same name
    private boolean hasSiblingsWithSameName(Node node) {
        Node parent = node.getParentNode();
        if (parent == null) {
            return false;
        }

        NodeList siblings = parent.getChildNodes();
        String nodeName = node.getNodeName();
        int sameNameCount = 0;

        for (int i = 0; i < siblings.getLength(); i++) {
            Node sibling = siblings.item(i);
            if (sibling.getNodeType() == Node.ELEMENT_NODE && sibling.getNodeName().equals(nodeName)) {
                sameNameCount++;
            }
        }

        return sameNameCount > 1;
    }

    // Helper method to check if a value is a variable (e.g., ${{someVar}})
    private boolean isVariable(String value) {
        return value != null && value.matches(VARIABLE_PATTERN);
    }

    public static String evaluateXPath(String xmlString, String xpathExpression){
        try{
            // Step 1: Parse the XML string into a Document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // Enable namespace awareness
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

            // Step 2: Extract namespaces dynamically
            Map<String, String> namespaces = getNamespaces(doc.getDocumentElement());

            // Step 3: Create XPath object and set the dynamic Namespace Context
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(new GenericNamespaceContext(namespaces));

            // Step 4: Compile the XPath expression
            XPathExpression expr = xpath.compile(xpathExpression);

            // Step 5: Evaluate the expression and return the result as a string
            Node resultNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (resultNode != null) {
                return resultNode.getTextContent().trim(); // Return the text value of the node
            } else {
                return null;
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String extractVariableName(String value) {
        return value.replaceAll("\\$\\{\\{", "").replaceAll("\\}\\}", "");
    }

    // Method to extract namespaces from the root element of the document
    private static Map<String, String> getNamespaces(Element rootElement) {
        Map<String, String> namespaces = new HashMap<>();
        NamedNodeMap attributes = rootElement.getAttributes();

        // Iterate through the attributes to find namespace declarations
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            if (attrName.startsWith("xmlns:")) {
                String prefix = attrName.substring(6); // Extract prefix after "xmlns:"
                String uri = attr.getNodeValue();
                namespaces.put(prefix, uri);
            } else if (attrName.equals("xmlns")) {
                // Default namespace (no prefix)
                namespaces.put("", attr.getNodeValue());
            }
        }

        return namespaces;
    }


    public static void test() {
        // Load your XML documents and call the generateXPathMap method
        String templateXml = FileReaderUtil.readFileFromResources("templates/template.xml");
        String actualXml = FileReaderUtil.readFileFromResources("templates/original2.xml");

        // Create an instance of XPathGenerator
        XPathGenerator generator = new XPathGenerator();
        Map<String, String> result = null;

        try {
            result = generator.generateXPathMap(templateXml);
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.forEach((k, v) -> {
            String var = extractVariableName(k);
            String theValue = evaluateXPath(actualXml, v);
            System.out.println(var + ": [" + theValue + "] => [" + v + "]");
        });
    }
}
