package me.test.api;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.DocumentContext;
import me.util.FileReaderUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonTemplateProcessor extends TemplateProcessor {
    private final Map<String, String> variableJsonPaths = new HashMap<>();

    public void analyzeTemplate(String jsonTemplate) {
        DocumentContext context = JsonPath.parse(jsonTemplate);
        traverseNode(context.json(), "$");
    }

    protected void traverseNode(Object jsonNode, String currentPath) {
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
                    traverseNode(value, newPath);
                }
            }
        } else if (jsonNode instanceof Iterable) {
            Iterable<Object> jsonArray = (Iterable<Object>) jsonNode;
            int index = 0;
            for (Object item : jsonArray) {
                String siblingCondition = generateSiblingCondition(item);
                String newPath = currentPath + siblingCondition;
                traverseNode(item, newPath);
                index++;
            }
        }
    }

    private String generateSiblingCondition(Object item) {
        Map<String, String> conditions = new HashMap<>();

        if (item instanceof Map) {
            Map<String, Object> itemMap = (Map<String, Object>) item;
            for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
                if (entry.getValue() instanceof String && !((String) entry.getValue()).isEmpty()) {
//                    return "[?(@." + entry.getKey() + " == '" + entry.getValue() + "')]";
                    if(!isVariable((String) entry.getValue()))
                        conditions.put(entry.getKey(), (String) entry.getValue());
                }
            }
        }

        if(conditions.isEmpty())
            return "";

        return conditions.entrySet().stream().map((entry) -> //stream each entry, map it to string value
                        "[?(@." + entry.getKey() + " == '" + entry.getValue() + "')]")
                .collect(Collectors.joining(""));
    }

    public Map<String, String> extractVariableValues(String jsonString, Map<String, String> variablePaths) {
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
        String template = FileReaderUtil.readFileFromResources("templates/template_1.json");
        String content = FileReaderUtil.readFileFromResources("templates/sample_1.json");

        JsonTemplateProcessor analyzer = new JsonTemplateProcessor();
        analyzer.analyzeTemplate(template);
        int counter = 0;

        counter = 1;
        System.out.println("JSON Path:");
        for (Map.Entry<String, String> entry : analyzer.getVariableJsonPaths().entrySet()) {
            System.out.println(counter + ". " + entry.getKey() + ": [" + entry.getValue() + "]");
            counter++;
        }

        // Extract variable values from JSON content
        Map<String, String> variableValues = analyzer.extractVariableValues(content, analyzer.getVariableJsonPaths());

        counter = 1;
        System.out.println("\nVariables with values:");
        for (Map.Entry<String, String> entry : variableValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isEmpty())
                System.out.println(counter + ". " + key + ": [" + value + "]");

            counter++;
        }

        counter = 1;
        System.out.println("\nVariables WITHOUT values:");
        for (Map.Entry<String, String> entry : variableValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null)
                System.out.println(counter + ". " + key + ": [" + value + "]");

            counter++;
        }


    }
}
