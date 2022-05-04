import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;

public class Checker {
    
    /**
     * Checks if two directories exist from input
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
     * Returns the directory to make using input directories
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

    public static void checkDeletedFiles(File dir) {
        File dotSync = new File(dir.getAbsolutePath() + "/.sync");
        Type collectionType = new TypeToken<Map<String, List<List<String>>>>(){}.getType();
        Gson gson = new Gson();
        String date;
        String deleted = "deleted";
        List<List<String>> pairs = new ArrayList<>();
        List<String> pair = new ArrayList<>();

        try {
            JsonReader jsonReader = new JsonReader(new FileReader(dotSync));
            Map<String, List<List<String>>> fileStatus = gson.fromJson(jsonReader, collectionType);

            for (Map.Entry<String, List<List<String>>> entry : fileStatus.entrySet()) {
                String fileName = entry.getKey();
                List<List<String>> json = entry.getValue();
                File file = new File(dir.getAbsolutePath() + "/" + fileName);

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
}
