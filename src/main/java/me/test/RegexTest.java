package me.test;

public class RegexTest {
    public static void test() {
        String pattern = ".*[^a-zA-Z0-9_\\-\\.]+.*";
        String[] text = {"a:x", "a:x.y", "a:x-y", "a x", "a@x", "a\\x", "Ax", "aX", "A1", "a1", "a-x", "a_x", "a.x"};

        for (String s : text) {
            System.out.println(s + " > " + pattern + " < " + s.matches(pattern));
        }
    }
}
