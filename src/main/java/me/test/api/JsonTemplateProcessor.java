package me.test.api;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.DocumentContext;
import me.util.FileReaderUtil;

import java.util.HashMap;
import java.util.Map;

public class JsonTemplateProcessor {
    private final Map<String, String> variableJsonPaths = new HashMap<>();

    public void analyzeTemplate(String jsonTemplate) {
        DocumentContext context = JsonPath.parse(jsonTemplate);
        traverseJson(context.json(), "$");
    }

    private void traverseJson(Object jsonNode, String currentPath) {
        if (jsonNode instanceof Map) {
            Map<String, Object> jsonObject = (Map<String, Object>) jsonNode;
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                String newPath = currentPath + "." + key;
                if (value instanceof String && ((String) value).matches("\\$\\{\\{([a-zA-Z0-9\\-_]+?)\\}\\}")) {
                    String variableName = ((String) value).replace("${{", "").replace("}}", "");
                    variableJsonPaths.put(variableName, newPath);
                } else {
                    traverseJson(value, newPath);
                }
            }
        } else if (jsonNode instanceof Iterable) {
            Iterable<Object> jsonArray = (Iterable<Object>) jsonNode;
            int index = 0;
            for (Object item : jsonArray) {
                String siblingCondition = generateSiblingCondition(item);
                String newPath = currentPath + siblingCondition;
                traverseJson(item, newPath);
                index++;
            }
        }
    }

    private String generateSiblingCondition(Object item) {
        if (item instanceof Map) {
            Map<String, Object> itemMap = (Map<String, Object>) item;
            for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
                if (entry.getValue() instanceof String && !((String) entry.getValue()).isEmpty()) {
                    return "[?(@." + entry.getKey() + " == '" + entry.getValue() + "')]";
                }
            }
        }
        return "";
    }

    public Map<String, String> extractVariableValuesFromJson(String jsonString, Map<String, String> variablePaths) {
        Map<String, String> variableValues = new HashMap<>();
        DocumentContext context = JsonPath.parse(jsonString);

        for (Map.Entry<String, String> entry : variablePaths.entrySet()) {
            String variableName = entry.getKey();
            String jsonPath = entry.getValue();

            try {
                Object value = context.read(jsonPath, Object.class);
                if (value instanceof Iterable) {
                    variableValues.put(variableName, ((Iterable<?>) value).iterator().next().toString());
                } else {
                    variableValues.put(variableName, value.toString());
                }
            } catch (Exception e) {
                variableValues.put(variableName, null);
            }
        }
        return variableValues;
    }

    public Map<String, String> getVariableJsonPaths() {
        return variableJsonPaths;
    }

    public static void test() {
        String jsonTemplate = FileReaderUtil.readFileFromResources("templates/template_1.json");
        String jsonContent = FileReaderUtil.readFileFromResources("templates/sample_1.json");

        JsonTemplateProcessor analyzer = new JsonTemplateProcessor();
        analyzer.analyzeTemplate(jsonTemplate);

        Map<String, String> extractedValues = analyzer.extractVariableValuesFromJson(jsonContent, analyzer.getVariableJsonPaths());
        extractedValues.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
