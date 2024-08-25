package me.test;


public class Guid {

    public static String getGuid() {
        return java.util.UUID.randomUUID().toString();
    }

    /*public static String addHyphensToUUID(String uuid) {
        if (uuid == null || uuid.isEmpty() || uuid.length() != 32) {
            return new UUID.randomUUID().toString();
        }

        return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
    }*/

    // create a function that adds hyphens to a UUID
    // first make the input lowercase
    // if the input is matches regex for a UUID with hyphens, return it
    // else if the input matches regex for a UUID without hyphens, add hyphens and return it
    // else generate a new UUID and return it
    public static String formatUUID(String input) {
        if(null == input)
            return java.util.UUID.randomUUID().toString().toLowerCase();

        String uuid = input.toLowerCase();

        if (uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            return uuid;
        } else if (uuid.matches("[0-9a-f]{32}")) {
            return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
        } else {
            return java.util.UUID.randomUUID().toString();
        }
    }

}
