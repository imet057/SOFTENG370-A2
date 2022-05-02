import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Utility {
    
    public static String getEncodedString(String preEncoded) {
        String encoded = "";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(preEncoded.getBytes(StandardCharsets.UTF_8));
            encoded = Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }

        return encoded;
    }

}
