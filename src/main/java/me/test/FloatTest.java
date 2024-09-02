package me.test;

public class FloatTest {
//    private static Float f = new Float(1.2);
    private static Float F = new Float(1.2);
    private static float f = (float) 1.2;


    public static String testFloatAsClass() {
        return F.toString();
    }

    public static String testFloatAsPrimitive() {
        return ""+f;
    }
}
