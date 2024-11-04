package me.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {
    private static void testVariableNamePattern() {
        String pattern = ".*[^a-zA-Z0-9_\\-\\.]+.*";
        String[] text = {"a:x", "a:x.y", "a:x-y", "a x", "a@x", "a\\x", "Ax", "aX", "A1", "a1", "a-x", "a_x", "a.x"};

        for (String s : text) {
            System.out.println(s + " > " + pattern + " < " + s.matches(pattern));
        }
    }

    private static String extractVariableName(String value) {
        final String VARIABLE_NAME_PATTERN = "\\$\\{([a-zA-Z0-9\\-_]+?)\\}";
        Matcher matcher = Pattern.compile(VARIABLE_NAME_PATTERN).matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void test() {
        String varValid = extractVariableName("${Abul}");
        String varInvalid = extractVariableName("$[Abul]");

        System.out.println("Valid: >|" + varValid + "|<");
        System.out.println("Invalid: >|" + varInvalid + "|<");
    }
}
