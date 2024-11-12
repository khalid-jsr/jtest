package me.util;

public class Utils {
    public static String concatURL(String url1, String url2) {
        String u1="", u2="";
        if(url1 != null && !url1.trim().isEmpty())
            u1 = url1.trim();
        if (url2 != null && !url2.trim().isEmpty()) {
            u2 = url2.trim();
        }

        if(u1.isEmpty() && u2.isEmpty())
            return null;

        if(u2.isEmpty())
            return u1;

        u1 = u1.endsWith("/") ? u1.substring(0, u1.length()-1) : u1;
        u2 = u2.startsWith("/") ? u2.substring(1) : u2;

        return u1 + "/" + u2;
    }

    public static void test() {
        String[][] urls = {
                {"https://test.com", ""},
                {"https://test.com", null},
                {"https://test.com", "newpath"},
                {"https://test.com", "new-path"},
                {"https://test.com", "new/path"},
                {"https://test.com", "new/path/"},
                {"", "new/path/"},
                {null, "new/path/"},
                {"https://test.com/", ""},
                {"https://test.com/", null},
                {"https://test.com/", "newpath"},
                {"https://test.com/", "new-path"},
                {"https://test.com/", "new/path"},
                {"https://test.com/", "new/path/"},
                {"/", "new/path/"},
                {"https://test.com/", "/"},
                {"https://test.com/", "/newpath"},
                {"https://test.com/", "/new-path"},
                {"https://test.com/", "/new/path"},
                {"https://test.com/", "/new/path/"},
                {"/", "/new/path/"},
                {"", ""},
                {null, null},
                {"", null},
                {null, ""},
            };

        for (String[] url : urls) {
            System.out.println("[" + url[0] + "] + [" + url[1] + "] ==> " + concatURL(url[0], url[1]));
        }
    }
}
