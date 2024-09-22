package me.test;

import java.math.BigDecimal;


public class BigDecimalTest {
    public static BigDecimal test() {
        int qty = 3;
        BigDecimal price = new BigDecimal("1.2");
        BigDecimal total = price.multiply(new BigDecimal(qty));
        System.out.println("Total: " + total);
        return total;
    }
}
