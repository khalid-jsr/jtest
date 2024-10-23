package me.test.comm;

import java.util.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import me.util.FileReaderUtil;
import org.w3c.dom.*;
import java.io.StringReader;
import org.xml.sax.InputSource;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;



public class XmlExtractor {

    private static final String VARIABLE_PATTERN = "\\$\\{\\{([a-zA-Z0-9\\-_]+?)\\}\\}";

    public static void test() {
        String templateXml = FileReaderUtil.readFileFromResources("templates/template.xml");
        String actualXml = FileReaderUtil.readFileFromResources("templates/original.xml");

        XmlExtractor extractor = new XmlExtractor();
        Map<String, String> result = null;

        try {
            result = extractor.extract(templateXml, actualXml);
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.forEach((k, v) -> System.out.println(k + " => [" + v + "]"));
    }

    public Map<String, String> extract(String templateXml, String actualXml) throws Exception {
        Map<String, String> resultMap = new HashMap<>();

        // Parse the template and actual XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document templateDoc = builder.parse(new InputSource(new StringReader(templateXml)));
        Document actualDoc = builder.parse(new InputSource(new StringReader(actualXml)));

        // Create a NamespaceContext from the template XML
        GenericNamespaceContext namespaceContext = createNamespaceContext(templateDoc);

        // XPath setup with namespace support
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(namespaceContext);
//        XPath xpath = createXPathWithNamespace(templateDoc);

        // Process nodes recursively with XPath
        processNode(templateDoc.getDocumentElement(), templateDoc, actualDoc, resultMap, "/", xpath);
//        processNode(templateDoc.getDocumentElement(), actualDoc, resultMap, "", xpath);

        return resultMap;
    }

    private GenericNamespaceContext createNamespaceContext(Document templateDoc) {
        GenericNamespaceContext namespaceContext = new GenericNamespaceContext();
        extractNamespaces(templateDoc.getDocumentElement(), namespaceContext);
        return namespaceContext;
    }

    private void extractNamespaces(Node node, GenericNamespaceContext namespaceContext) {
        // Extract namespace from node and add to context
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String prefix = node.getPrefix();
            String uri = node.getNamespaceURI();
            if (uri != null && !uri.isEmpty()) {
                namespaceContext.addNamespace(prefix != null ? prefix : "", uri);
            }
        }

        // Recursively extract namespaces from child nodes
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            extractNamespaces(children.item(i), namespaceContext);
        }
    }

    /*private void processNode(Node templateNode, Document actualDoc, Map<String, String> resultMap, String currentXPath, XPath xpath) throws Exception {
        NamedNodeMap templateAttributes = templateNode.getAttributes();

        // Check attributes in the template
        if (templateAttributes != null) {
            for (int i = 0; i < templateAttributes.getLength(); i++) {
                Node templateAttr = templateAttributes.item(i);
                String templateValue = templateAttr.getNodeValue();
                if (isVariable(templateValue)) {
                    String variableName = extractVariableName(templateValue);
                    String xpathExpression = currentXPath + "/@" + templateAttr.getNodeName();
                    String actualValue = evaluateXPath(actualDoc, xpathExpression, xpath);
                    resultMap.put(variableName, actualValue != null ? actualValue : null);
                }
            }
        }

        // Check the text content of the node
        String templateTextContent = templateNode.getTextContent().trim();
        if (isVariable(templateTextContent)) {
            String variableName = extractVariableName(templateTextContent);
            String xpathExpression = currentXPath;
            String actualValue = evaluateXPath(actualDoc, xpathExpression, xpath);
            resultMap.put(variableName, actualValue != null ? actualValue : null);
        }

        // Process child nodes recursively
        NodeList templateChildren = templateNode.getChildNodes();
        for (int i = 0; i < templateChildren.getLength(); i++) {
            Node childNode = templateChildren.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
//                String childXPath = ("/".equals(currentXPath) ? "" : currentXPath) + "/" + childNode.getNodeName();
                String childXPath = currentXPath + "/" + childNode.getNodeName();
                processNode(childNode, actualDoc, resultMap, childXPath, xpath);
            }
        }
    }*/

    private void processNode(Node templateNode, Document templateDoc, Document actualDoc, Map<String, String> resultMap, String currentXPath, XPath xpath) throws Exception {
        NamedNodeMap templateAttributes = templateNode.getAttributes();

        // Handle attributes in the template
        if (templateAttributes != null) {
            for (int i = 0; i < templateAttributes.getLength(); i++) {
                Node templateAttr = templateAttributes.item(i);
                String templateValue = templateAttr.getNodeValue();
                if (isVariable(templateValue)) {
                    String variableName = extractVariableName(templateValue);
                    String xpathExpression = currentXPath + "/@" + templateAttr.getNodeName();
                    String actualValue = evaluateXPath(actualDoc, xpathExpression, xpath);
                    resultMap.put(variableName, actualValue != null ? actualValue : null);
                }
            }
        }

        // Handle element text content in the template (only if it's a leaf node)
        if (isLeafNode(templateNode)) {
            String templateTextContent = templateNode.getTextContent().trim();
            if (isVariable(templateTextContent)) {
                String variableName = extractVariableName(templateTextContent);

                // Generate XPath with sibling-based condition dynamically if sibling exists
                String siblingXPathCondition = null;
                boolean isArray = doesXpathPopulateArray(xpath, currentXPath, actualDoc);
                if(isArray) {
                    siblingXPathCondition = getSiblingCondition(templateNode, currentXPath, templateDoc, actualDoc, xpath);
                }

                String actualValueXPath = siblingXPathCondition != null ? siblingXPathCondition : currentXPath;
                String actualValue = evaluateXPath(actualDoc, actualValueXPath, xpath);
                resultMap.put(variableName, actualValue != null ? actualValue : null);
            }
        }

        // Recursively process child nodes
        NodeList templateChildren = templateNode.getChildNodes();
        for (int i = 0; i < templateChildren.getLength(); i++) {
            Node childNode = templateChildren.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                String childXPath = currentXPath + "/" + childNode.getNodeName();
                processNode(childNode, templateDoc, actualDoc, resultMap, childXPath, xpath);
            }
        }
    }

    /*private void processNode(Node templateNode, Document actualDoc, Map<String, String> resultMap, String currentXPath, XPath xpath) throws Exception {
        NamedNodeMap templateAttributes = templateNode.getAttributes();

        // Handle attributes in the template
        if (templateAttributes != null) {
            for (int i = 0; i < templateAttributes.getLength(); i++) {
                Node templateAttr = templateAttributes.item(i);
                String templateValue = templateAttr.getNodeValue();
                if (isVariable(templateValue)) {
                    String variableName = extractVariableName(templateValue);
                    String xpathExpression = currentXPath + "/@" + templateAttr.getNodeName();
                    String actualValue = evaluateXPathWithNamespace(actualDoc, xpathExpression, xpath);
                    resultMap.put(variableName, actualValue != null ? actualValue : null);
                }
            }
        }

        // Handle element text content in the template (only if it's a leaf node)
        if (isLeafNode(templateNode)) {
            String templateTextContent = templateNode.getTextContent().trim();
            if (isVariable(templateTextContent)) {
                String variableName = extractVariableName(templateTextContent);

                // Generate XPath with sibling-based condition dynamically if sibling exists
                String siblingXPathCondition = getSiblingCondition(templateNode, currentXPath, actualDoc, xpath);
                String actualValueXPath = siblingXPathCondition != null ? siblingXPathCondition : currentXPath;

                String actualValue = evaluateXPathWithNamespace(actualDoc, actualValueXPath, xpath);
                resultMap.put(variableName, actualValue != null ? actualValue : null);
            }
        }

        // Recursively process child nodes
        NodeList templateChildren = templateNode.getChildNodes();
        for (int i = 0; i < templateChildren.getLength(); i++) {
            Node childNode = templateChildren.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                String childXPath = currentXPath + "/" + childNode.getNodeName();
                processNode(childNode, actualDoc, resultMap, childXPath, xpath);
            }
        }
    }*/

    // Helper method to check whether the Xpath evaluates to single element or multiple elements
    private boolean doesXpathPopulateArray(XPath xpath, String xpathExpression, Document document) throws Exception {
        // Compile the XPath expression
        XPathExpression expr = xpath.compile(xpathExpression);

        // Evaluate the expression as a NodeSet (NodeList)
        NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

        // Check if there are multiple nodes returned
        return nodeList != null && nodeList.getLength() > 1;
    }

    // Helper method to check if a node is a leaf (i.e., has no child elements)
    private boolean isLeafNode(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return false;
            }
        }
        return true;
    }

    /*// Function to evaluate XPath with Namespace handling
    private String evaluateXPathWithNamespace(Document actualDoc, String xpathExpression, XPath xpath) throws Exception {
        XPathExpression expr = xpath.compile(xpathExpression);
        return (String) expr.evaluate(actualDoc, XPathConstants.STRING);
    }

    // Helper method to create an XPath with a namespace context
    private XPath createXPathWithNamespace(Document templateDoc) {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        NamespaceResolver namespaceResolver = new NamespaceResolver(templateDoc);
        xpath.setNamespaceContext(namespaceResolver);
        return xpath;
    }*/

    /*private String getSiblingCondition(Node templateNode, String currentXPath, Document templateDoc, Document actualDoc, XPath xpath) throws Exception {
        int idx = getIndexForElement(templateNode);
        String parentXPath = getParentXPath(currentXPath);
        String parentXPathWithIdx = parentXPath + "[" + idx + "]";

        // Generic sibling condition detection based on the structure of the template
//        Node parent = templateNode.getParentNode();
        Node parent = getNodeFromXPath(templateDoc, parentXPathWithIdx, xpath);
        if (parent == null || !(parent instanceof Element)) {
            return null;
        }

        Map<String, String> parentXqueryMap = new HashMap<>();
        NodeList siblings = parent.getChildNodes();
        for (int i = 0; i < siblings.getLength(); i++) {
            Node sibling = siblings.item(i);
            if (sibling.getNodeType() == Node.ELEMENT_NODE && sibling != templateNode) {
                // Use the sibling's content to generate a condition-based XPath
                String siblingName = sibling.getNodeName();
                String siblingValue = sibling.getTextContent().trim();
                if (!siblingValue.isEmpty()) {
                    // Create an XPath expression with a condition based on the sibling value
//                    return parentXPath + "[" + siblingName + "='" + siblingValue + "']" + "/" + templateNode.getNodeName();
                    parentXqueryMap.put(siblingName, siblingValue);
                }

                NamedNodeMap parentAttributes = parent.getAttributes();
                if (parentAttributes != null) {
                    for (int j = 0; j < parentAttributes.getLength(); j++) {
                        Node attr = parentAttributes.item(j);
                        parentXqueryMap.put("@" + attr.getNodeName(), attr.getNodeValue());
                    }
                }
            }
        }

        // Also use this node's attributes to generate a condition-based XPath
        Map<String, String> nodeXqueryMap = new HashMap<>();
        NamedNodeMap nodeAttributes = templateNode.getAttributes();
        if (nodeAttributes != null) {
            for (int j = 0; j < nodeAttributes.getLength(); j++) {
                Node attr = nodeAttributes.item(j);
                nodeXqueryMap.put("@" + attr.getNodeName(), attr.getNodeValue());
            }
        }

        String nodeAttrQry = "";
        if(!nodeXqueryMap.isEmpty()) {
            StringBuilder qry = new StringBuilder();
            for(Map.Entry e : nodeXqueryMap.entrySet()) {
                qry.append("[").append(e.getKey()).append("='").append(e.getValue()).append("']");
            }
            nodeAttrQry =  qry.toString();
        }

        if(!parentXqueryMap.isEmpty()) {
            StringBuilder qry = new StringBuilder();
            for(Map.Entry e : parentXqueryMap.entrySet()) {
                qry.append("[").append(e.getKey()).append("='").append(e.getValue()).append("']");
            }
            return parentXPath + qry.toString() + "/" + templateNode.getNodeName() + nodeAttrQry;
        } else if(!nodeXqueryMap.isEmpty()) {
            return currentXPath + nodeAttrQry;
        } else {
            return null;
        }
    }

    private int getIndexForElement(Node elem) {
        int index = 0; // XPath indexes start from 1, so we will use 1-based indexing
        NodeList siblings = elem.getParentNode().getParentNode().getChildNodes();
        String elemValue = elem.getTextContent().trim();

        for (int i = 0; i < siblings.getLength(); i++) {
            Node node = siblings.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(elem.getParentNode().getNodeName())) {
                index++; // Increment the index each time we encounter a "KeyAttrib" element

                // Find the child node of this node
                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node childNode = childNodes.item(j);

                    if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals(elem.getNodeName())) {
                        String childValue = childNode.getTextContent().trim();

                        if (childValue.equals(elemValue)) {
                            // We found the desired "AttribName" with the matching value
                            return index; // Return the 1-based index of this KeyAttrib
                        }
                    }
                }
            }
        }

        // If the "AttribName" was not found, return -1 or some indicator
        return -1;
    }

    private String getParentXPath(String childXPath) {
        int lastSeparatorIdx = childXPath.lastIndexOf('/');
        return lastSeparatorIdx<0 ? childXPath : childXPath.substring(0, lastSeparatorIdx);
    }*/

    private String getSiblingCondition(Node templateNode, String currentXPath, Document templateDoc, Document actualDoc, XPath xpath) throws Exception {
        int idx = getIndexForElement(templateNode);
        String parentXPath = getParentXPath(currentXPath);
        String parentXPathWithIdx = parentXPath + "[" + idx + "]";

        Node parent = getNodeFromXPath(templateDoc, parentXPathWithIdx, xpath);
        if (parent == null || !(parent instanceof Element)) {
            return null;
        }

        Map<String, String> parentXqueryMap = getNodeConditionMap(parent);
        Map<String, String> nodeXqueryMap = getNodeConditionMap(templateNode);

        // Construct query based on sibling and node attributes
        String nodeAttrQry = buildConditionString(nodeXqueryMap);

        String condition = null;
        if (!parentXqueryMap.isEmpty()) {
            String parentCondition = buildConditionString(parentXqueryMap);
            condition = parentXPath + parentCondition + "/" + templateNode.getNodeName() + nodeAttrQry;
        } else if (!nodeXqueryMap.isEmpty()) {
            condition = currentXPath + nodeAttrQry;
        }

        System.out.println(condition);
        return condition;
    }

    private int getIndexForElement(Node elem) {
        int index = 0; // XPath is 1 based index
        NodeList siblings = elem.getParentNode().getChildNodes(); // Correct: Use direct parent

        for (int i = 0; i < siblings.getLength(); i++) {
            Node node = siblings.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(elem.getNodeName())) {
                index++;
                if (node.isSameNode(elem)) {
                    return index; // Return 1-based index
                }
            }
        }

        return -1; // -1 indicates that the element is not found
    }

    private String getParentXPath(String childXPath) {
        int lastSeparatorIdx = childXPath.lastIndexOf('/');
        return lastSeparatorIdx < 0 ? childXPath : childXPath.substring(0, lastSeparatorIdx);
    }

    private Node getNodeFromXPath(Document doc, String xpathExpression, XPath xpath) throws Exception {
        XPathExpression expr = xpath.compile(xpathExpression);
        return (Node) expr.evaluate(doc, XPathConstants.NODE);
    }

    private Map<String, String> getNodeConditionMap(Node node) {
        Map<String, String> conditionMap = new HashMap<>();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // Collect child element values
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && !child.getTextContent().trim().isEmpty()) {
                    conditionMap.put(child.getNodeName(), child.getTextContent().trim());
                }
            }

            // Collect attributes
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                for (int j = 0; j < attributes.getLength(); j++) {
                    Node attr = attributes.item(j);
                    conditionMap.put("@" + attr.getNodeName(), attr.getNodeValue());
                }
            }
        }

        return conditionMap;
    }

    private String buildConditionString(Map<String, String> conditionMap) {
        StringBuilder condition = new StringBuilder();
        for (Map.Entry<String, String> entry : conditionMap.entrySet()) {
            if(!isVariable(entry.getValue().trim())) {
                condition.append("[").append(entry.getKey().trim()).append("='").append(entry.getValue().trim()).append("']");
            }
        }

        return condition.toString();
    }

    private boolean isVariable(String value) {
        return value != null && value.matches(VARIABLE_PATTERN);
    }

    private String extractVariableName(String value) {
        return value.replaceAll("\\$\\{\\{", "").replaceAll("\\}\\}", "");
    }

/*    private Node getNodeFromXPath(Document doc, String xpathExpression, XPath xpath) throws Exception {
        XPathExpression expr = xpath.compile(xpathExpression);
        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        return node;
    }*/

    private String evaluateXPath(Document doc, String xpathExpression, XPath xpath) throws Exception {
        Node resultNode = getNodeFromXPath(doc, xpathExpression, xpath);
        String s = resultNode != null ? resultNode.getTextContent() : null;
//        System.out.println("XPath: [" + xpath + "] ==> Expr: [" + xpathExpression + "]");
        return s;
    }


    // This function determines whether the xml array is an array of elements or objects. True for leaf, false for object.
    // true -> <MetaData><Meta attrib="Country">Bangladesh</Meta><Meta attrib="City">Dhaka</Meta></MetaData>
    // false -> <KeyAttribs><KeyAttrib><AttribName>Has Mountain</AttribName><AttribValue>false</AttribValue></KeyAttrib>
    //              <KeyAttrib><AttribName>Area</AttribName><AttribValue>1,44,000</AttribValue></KeyAttrib></KeyAttribs>
    private boolean isArrayAsElement(Node node) {
        if (node == null || node.getParentNode() == null) {
            return false; // If node or parent is null, no siblings exist
        }

        Node parent = node.getParentNode();
        NodeList siblings = parent.getChildNodes();
        String nodeName = node.getNodeName();

        int sameNameCount = 0;

        // Iterate through siblings
        for (int i = 0; i < siblings.getLength(); i++) {
            Node sibling = siblings.item(i);
            if (sibling.getNodeType() == Node.ELEMENT_NODE && sibling.getNodeName().equals(nodeName)) {
                sameNameCount++;
            }
        }

        // If count is greater than 1, the node has siblings with the same name
        return sameNameCount > 1;
    }

}
