package org.example;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;


public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        Main m = new Main();
        m.test();
    }

    private void test() {
        Map originalMap = new HashMap<String, Object>();
        originalMap.put("a", "abul");
        originalMap.put("b", "babul");
        originalMap.put("c", "cabul");

        System.out.println("Map\n" + originalMap.toString());
    }
}