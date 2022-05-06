import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;

/**
 * A class that checks certain conditions
 * 
 * Author: Issei Metoki
 * UPI: imet057
 */

public class Checker {
    
    /**
     * Checks if the input directories exist 
     * 
     * @param dirs
     * @return boolean
     */
    public static boolean doTwoDirsExist(String[] dirs) {
        File dirOne = new File(dirs[0]);
        File dirTwo = new File(dirs[1]);

        if (dirOne.exists() && dirTwo.exists() && dirOne.isDirectory() && dirTwo.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the directory to make
     * Return null if no directory can be made
     * 
     * @param dirs
     * @return File
     */
    public static File getDirToMake(String[] dirs) {
        File dirOne = new File(dirs[0]);
        File dirTwo = new File(dirs[1]);

        if (dirOne.exists() && dirOne.isDirectory()) {
            return dirTwo;
        } else if(dirTwo.exists() && dirTwo.isDirectory()) {
            return dirOne;
        } else {
            return null;
        }
    }

    /**
     * Checks if some files got deleted prior to the sync
     * and updates the sync file
     * 
     * @param dir
     */
    public static void checkDeletedFiles(File dir) {
        File dotSync = new File(dir.getAbsolutePath() + "/.sync");
        Type collectionType = new TypeToken<Map<String, List<List<String>>>>(){}.getType();
        Gson gson = new Gson();
        String date;
        String deleted = "deleted";
        List<List<String>> pairs = new ArrayList<>();

        try {
            JsonReader jsonReader = new JsonReader(new FileReader(dotSync));
            Map<String, List<List<String>>> fileStatus = gson.fromJson(jsonReader, collectionType);

            for (Map.Entry<String, List<List<String>>> entry : fileStatus.entrySet()) {
                String fileName = entry.getKey();
                List<List<String>> json = entry.getValue();
                File file = new File(dir.getAbsolutePath() + "/" + fileName);
                List<String> pair = new ArrayList<>();

                if ((!file.exists()) && (!json.get(0).get(1).equals(deleted))) {
                    pairs = fileStatus.get(fileName);
                    date = Utility.getFormattedCurrentTime();
                    pair.add(date);
                    pair.add(deleted);
                    pairs.add(0, pair);

                    fileStatus.put(fileName, pairs);

                    Utility.writeToDotSync(dotSync, fileStatus);
                }
            }

        } catch (Exception e) {
        }
    }

    /**
     * This method is called when a file exists in one directory but not in the other directory
     * This method determines if the file is to be deleted or to be copied in the other directory
     * 
     * @param file
     * @param sameFileInOtherDir
     * @param dir
     * @param otherDir
     * @return
     */
    public static boolean toBeDeleted(File file, File sameFileInOtherDir, File dir, File otherDir) {
        File thisDotSync = new File(dir.getAbsolutePath() + "/.sync");
        File otherDotSync = new File(otherDir.getAbsolutePath() + "/.sync");
        Map<String, List<List<String>>> fileStatusInDir = Utility.getFileStatus(thisDotSync);
        Map<String, List<List<String>>> fileStatusInOtherDir = Utility.getFileStatus(otherDotSync);

        if (fileStatusInOtherDir == null) {
            return false;
        }

        if (!fileStatusInOtherDir.containsKey(sameFileInOtherDir.getName())) {
            return false;
        } 

        if ((fileStatusInDir.get(file.getName()).size() < 2) && (fileStatusInOtherDir.get(sameFileInOtherDir.getName()).get(0).get(1).equals("deleted"))) {
            return true;
        }

        if ((!fileStatusInDir.get(file.getName()).get(1).get(1).equals("deleted")) && (fileStatusInOtherDir.get(sameFileInOtherDir.getName()).get(0).get(1).equals("deleted"))) {
            return true;
        } else {
            return false;
        }
    }
}
