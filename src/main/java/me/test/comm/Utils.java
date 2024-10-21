package me.test.comm;

import java.util.Map;


public class Utils {
    public static String processTemplate(String template, Map<String, String> requestMap) {
        String processedTemplate = template;
        for (Map.Entry<String, String> entry : requestMap.entrySet()) {
            String placeholder = "${{" + entry.getKey() + "}}";
            processedTemplate = processedTemplate.replace(placeholder, entry.getValue());
        }
        return processedTemplate;
    }
}
