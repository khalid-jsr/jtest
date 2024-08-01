package me.test;

import java.security.MessageDigest;
import org.apache.commons.codec.binary.Base64;


public class HashTest {

    public static String hashPassword(String password, String salt) {
        String h = hash(salt + password);
        System.out.println("Encrypted pwd: [" + h + "]");
        return h;
    }

    public static String hash(String text$) {
        try {
            byte[] b64 = Base64.encodeBase64(MessageDigest.getInstance("MD5").digest(text$.getBytes()));
            return new String(b64);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
