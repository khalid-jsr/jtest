package me.test;

public class ExceptionTest {
    public static void test() {
        try {
            throw new Exception("Test exception");
        } catch (Exception e) {
            System.out.println("Error caused by ==>\n\t" + e.getMessage());
        }
    }
}
