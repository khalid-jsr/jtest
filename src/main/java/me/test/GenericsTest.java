package me.test;

import java.util.HashMap;
import java.util.Map;


public class GenericsTest {
    Map<String, Object> _config = new HashMap();

    public GenericsTest() {
        HashMap<String, String> map = new HashMap<>();
        map.put("a", "A");
        map.put("b", "B");

        _config.put("string", "one");
        _config.put("int", 2);
        _config.put("float", new Float(3.5));
        _config.put("boolean", true);
        _config.put("map", map);
    }

    //This function will return the value of a key from the config map
    // the config map has key as string and value as any class type
    // the function take the class type as generic and return value as that type
    // if the key is not found, throw an IllegalStateException
    // if the key is found but the value is not of the generic type, throw a ClassCastException
    public <T> T getConfig(String key, Class<T> clazz) {
        if (!_config.containsKey(key)) {
            throw new IllegalStateException("Key not found: " + key);
        }

        Object value = _config.get(key);
        if (!clazz.isInstance(value)) {
            throw new ClassCastException("Value for key " + key + " is not of type " + clazz.getName());
        }

        return clazz.cast(value);
    }

    // create a test function for above function
    public void test() {
        System.out.println(getConfig("string", String.class) + " >> " + getConfig("string", String.class).getClass().getName());
        System.out.println(getConfig("int", Integer.class) + " >> " + getConfig("int", Integer.class).getClass().getName());
        System.out.println(getConfig("float", Float.class) + " >> " + getConfig("float", Float.class).getClass().getName());
        System.out.println(getConfig("boolean", Boolean.class) + " >> " + getConfig("boolean", Boolean.class).getClass().getName());
        System.out.println(getConfig("map", Map.class) + " >> " + getConfig("map", Map.class).getClass().getName());
//        System.out.println(getConfig("notfound", Object.class) + " >> " + getConfig("notfound", Object.class).getClass().getName());
        System.out.println(getConfig("string", Integer.class) + " >> " + getConfig("notfound", Integer.class).getClass().getName());
    }

}
