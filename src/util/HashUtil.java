package util;
import java.security.*;

public class HashUtil {
    public static String hashSHA256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encoded = digest.digest(input.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : encoded) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}
