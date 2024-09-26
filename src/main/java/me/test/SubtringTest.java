package me.test;

public class SubtringTest {

    public static void test() {
        String s = "Hello, world!";
        String sub = s.substring(0, s.length()-1);

        System.out.println("Main String: [" + s + "]");
        System.out.println("Sub string: [" + sub + "]");
    }

}
