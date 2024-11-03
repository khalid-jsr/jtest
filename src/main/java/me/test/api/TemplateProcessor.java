package me.test.api;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class TemplateProcessor {
    private final Map<String, String> pathMap = new HashMap<>();
    private final Map<String, String> namespaceMap = new HashMap<>();
    private final static String ATTRIBUTE_NAME_PATTERN = ".*[^a-zA-Z0-9_\\-\\.]+.*";
    private static final String VARIABLE_NAME_PATTERN = "\\$\\{\\{([a-zA-Z0-9\\-_]+?)\\}\\}";

    abstract void traverseNode(Object node, String currentPath);


    protected static String getVariableNamePattern() {
        return VARIABLE_NAME_PATTERN;
    }

    protected static String getAttributeNamPattern() {
        return ATTRIBUTE_NAME_PATTERN;
    }

    protected void addToNamespace(String variableName, String namespaceValue) {
        getNamespaces().put(variableName, namespaceValue);
    }

    protected void addToPath(String variableName, String pathValue) {
        getPaths().put(variableName, pathValue);
    }

    protected Map<String, String> getNamespaces() {
        return namespaceMap;
    }

    protected Map<String, String> getPaths() {
        return pathMap;
    }

    protected boolean isVariable(String value) {
        return value != null && value.matches(getVariableNamePattern());
    }

    protected String extractVariableName(String value) {
        Matcher matcher = Pattern.compile(getVariableNamePattern()).matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
