import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;

public class Syncer {
    
    public static void sync(File dirOne, File dirTwo) {

    }

    public static void setUpDotSync(File dir) {
        File dotSync = new File(dir.getAbsolutePath() + "/.sync");

        try {
            if (!dotSync.exists()) {
                dotSync.createNewFile();
            }
        } catch (Exception e) {
        }

        for (File file : dir.listFiles()) {
            if (file.isFile() && (!file.getName().equals(".sync"))) {
                updateDotSync(dotSync, file);
            }
        }
    }

    public static void updateDotSync(File dotSync, File file) {
        String fileName = file.getName();
        String fileContent = Utility.readFile(file);
        Type collectionType = new TypeToken<Map<String, List<List<String>>>>(){}.getType();
        Gson gson = new Gson();
        List<String> pair = new ArrayList<>();
        String date, encoded;


        try {
            JsonReader jsonReader = new JsonReader(new FileReader(dotSync));
            Map<String, List<List<String>>> fileStatus = gson.fromJson(jsonReader, collectionType); 
            List<List<String>> pairs = new ArrayList<>();

            date = Utility.getFormattedTime(file);
            encoded = Utility.getEncodedString(fileContent);
            pair.add(date);
            pair.add(encoded);

            if (fileStatus == null) {
                Map<String, List<List<String>>> temp = new HashMap<String, List<List<String>>>();

                pairs.add(pair);
                temp.put(fileName, pairs);
                fileStatus = temp;

                Utility.writeToDotSync(dotSync, fileStatus);
            } else if (!fileStatus.containsKey(fileName)) {
                pairs.add(pair);
                fileStatus.put(fileName, pairs);

                Utility.writeToDotSync(dotSync, fileStatus);
            }else {
                if (!encoded.equals(fileStatus.get(fileName).get(0).get(1))) {
                    pairs = fileStatus.get(fileName);
                    pairs.add(0, pair);
                    fileStatus.put(fileName, pairs);

                    Utility.writeToDotSync(dotSync, fileStatus);
                } else if(date != fileStatus.get(fileName).get(0).get(0)) {
                    file.setLastModified(Utility.getTimeForMod(fileStatus.get(fileName).get(0).get(0)));
                }
            }
        } catch (Exception e) {
        }
    }

    public static void initialSync(File dirOne, File dirTwo) {
        setUpDotSync(dirOne);
        setUpDotSync(dirTwo);

    }
}
