import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

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
                builder.append(line).append("\n"); 
            }

            br.close();
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

    public static String getFormattedTimeOfLastMod(File file) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        return dateFormat.format(new Date(file.lastModified()));
    }

    public static Date parseTimeFromString(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        Date timeDateObj;

        try {
            timeDateObj = dateFormat.parse(time);
            return timeDateObj;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static long getTimeForSettingLastMod(String time) {
        return parseTimeFromString(time).getTime();
    }

    public static String getFormattedCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        ZonedDateTime currentTime = ZonedDateTime.now();

        return currentTime.format(formatter);
    }

    public static void copyFile(File fileToCopy, File dest) {
        try {
            Files.copy(fileToCopy.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, 
            java.nio.file.StandardCopyOption.COPY_ATTRIBUTES, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, List<List<String>>> getFileStatus(File dotSync) {
        Type collectionType = new TypeToken<Map<String, List<List<String>>>>(){}.getType();
        Gson gson = new Gson();

        try {
            JsonReader jsonReader = new JsonReader(new FileReader(dotSync));
            Map<String, List<List<String>>> fileStatus = gson.fromJson(jsonReader, collectionType);

            return fileStatus;
        } catch (Exception e) {
        }

        return null;
    }

    public static List<List<String>> getPairsOfFileFromDotSync(File file, File dotSync) {
        Type collectionType = new TypeToken<Map<String, List<List<String>>>>(){}.getType();
        Gson gson = new Gson();
        List<List<String>> pairs = new ArrayList<>();

        try {
            JsonReader jsonReader = new JsonReader(new FileReader(dotSync));
            Map<String, List<List<String>>> fileStatus = gson.fromJson(jsonReader, collectionType);

            pairs = fileStatus.get(file.getName());
        } catch (Exception e) {
        }

        return pairs;
    }

    public static boolean isOlderVersion(String currrentDigest, List<List<String>> pairsOfOtherFile) {
        for (int i = 1; i < pairsOfOtherFile.size(); i++) {
            if (currrentDigest.equals(pairsOfOtherFile.get(i).get(1))) {
                return true;
            }
        }

        return false;
    }

    public static void addDeleteEntry(File file, File dotSync) {
        Map<String, List<List<String>>> fileStatus = getFileStatus(dotSync);
        List<List<String>> pairs = getPairsOfFileFromDotSync(file, dotSync);
        List<String> newPair = new ArrayList<>();

        newPair.add(getFormattedCurrentTime());
        newPair.add("deleted");
        pairs.add(0, newPair);
        fileStatus.put(file.getName(), pairs);

        writeToDotSync(dotSync, fileStatus);
    }

}
