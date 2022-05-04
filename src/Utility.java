import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utility {
    
    public static String getEncodedString(String preEncoded) {
        StringBuilder builder = new StringBuilder();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(preEncoded.getBytes(StandardCharsets.UTF_8));
        
            for (byte b : hash) {
                builder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public static String readFile(File file) {
        StringBuilder builder = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                builder.append(line);   // Might need to add append("\n")
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public static void writeToDotSync(File dotSync, Map<String, List<List<String>>> fileStatus) {
        try {
            FileWriter writer = new FileWriter(dotSync, false);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(fileStatus);

            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFormattedTime(File file) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        return dateFormat.format(new Date(file.lastModified()));
    }

    public static long getTimeForMod(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        try {
            Date timeOnSync = dateFormat.parse(time);
            
            return timeOnSync.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0L;
    }
}
