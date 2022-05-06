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

/**
 * A class that provides useful functions 
 * 
 * Author: Issei Metoki
 * UPI: imet057
 */

public class Utility {
    
    /**
     * Takes in a string and return encoded version of the string
     * 
     * @param preEncoded
     * @return
     */
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

    /**
     * Reads a file and returns the content of the file in a String
     * 
     * @param file
     * @return
     */
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

    /**
     * Writes the file status map to a sync file using JSON format
     * 
     * @param dotSync
     * @param fileStatus
     */
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

    /**
     * Returns a String of formatted last modified date of a file
     * 
     * @param file
     * @return
     */
    public static String getFormattedTimeOfLastMod(File file) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        return dateFormat.format(new Date(file.lastModified()));
    }

    /**
     * Converts time in String to a Date object and returns it
     * 
     * @param time
     * @return
     */
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

    /**
     * Converts time in String to equivalent long and returns it
     * 
     * @param time
     * @return
     */
    public static long getTimeForSettingLastMod(String time) {
        return parseTimeFromString(time).getTime();
    }

    /**
     * Returns ZonedDateTime object holding current time
     * 
     * @return
     */
    public static String getFormattedCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        ZonedDateTime currentTime = ZonedDateTime.now();

        return currentTime.format(formatter);
    }

    /**
     * Copies a file to the destination
     * 
     * @param fileToCopy
     * @param dest
     */
    public static void copyFile(File fileToCopy, File dest) {
        try {
            Files.copy(fileToCopy.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, 
            java.nio.file.StandardCopyOption.COPY_ATTRIBUTES, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the sync file and gets the file status stored in a map
     * 
     * @param dotSync
     * @return
     */
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

    /**
     * Gets pairs of a specific file
     * 
     * @param file
     * @param dotSync
     * @return
     */
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

    /**
     * Checks if the file is superceded 
     * 
     * @param currrentDigest
     * @param pairsOfOtherFile
     * @return
     */
    public static boolean isOlderVersion(String currrentDigest, List<List<String>> pairsOfOtherFile) {
        for (int i = 1; i < pairsOfOtherFile.size(); i++) {
            if (currrentDigest.equals(pairsOfOtherFile.get(i).get(1))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds an deletion format pair to the file status map
     * and then writes the map to a sync file
     * 
     * @param file
     * @param dotSync
     */
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
