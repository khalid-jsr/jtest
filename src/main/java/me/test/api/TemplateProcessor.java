package me.test.api;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class TemplateProcessor {
    private static final Map<String, String> pathMap = new HashMap<>();
    private static final String INVALID_ATTRIBUTE_NAME_PATTERN = ".*[^a-zA-Z0-9_\\-\\.]+.*";
//    private static final String VARIABLE_NAME_PATTERN = "\\$\\{([a-zA-Z0-9\\-_]+?)\\}";
    private static final String VARIABLE_NAME_PATTERN = "\\$\\{([a-zA-Z][a-zA-Z0-9\\-_]*?)\\}";

    abstract void traverseNode(Object node, String currentPath);

    protected static String getVariableNamePattern() {
        return VARIABLE_NAME_PATTERN;
    }

    protected static String getInvalidAttributeNamPattern() {
        return INVALID_ATTRIBUTE_NAME_PATTERN;
    }

    protected void addToPath(String variableName, String pathValue) {
        getPaths().put(variableName, pathValue);
    }

    protected Map<String, String> getPaths() {
        return pathMap;
    }

    protected boolean isVariable(String variableName) {
        return variableName != null && !variableName.isEmpty() && variableName.matches(getVariableNamePattern());
    }

    protected boolean isProperAttribute(String attributeName) {
        return attributeName != null && !attributeName.isEmpty()
                && !attributeName.equals("xmlns")
                && !attributeName.matches(getInvalidAttributeNamPattern());
    }

    protected String extractVariableName(String value) {
        Matcher matcher = Pattern.compile(getVariableNamePattern()).matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
