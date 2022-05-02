import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
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
            if (file.isFile()) {
            }
        }
    }

    public static void updateDotSync(File dotSync, File file) {
        String fileName = file.getName();
        // String fileContent
        Type collectionType = new TypeToken<Map<String, List<List<String>>>>(){}.getType();
        Gson gson = new Gson();


        try {
            JsonReader jsonReader = new JsonReader(new FileReader(dotSync));
            Map<String, List<List<String>>> fileStatus = gson.fromJson(jsonReader, collectionType); 

            if (!fileStatus.containsKey(fileName)) {
                List pair = new ArrayList<>();
                String date = (new Date(file.lastModified())).toString();
                
            }
        } catch (Exception e) {
        }
    }

    public static void initialSync(File dirOne, File dirTwo) {
        setUpDotSync(dirOne);
        setUpDotSync(dirTwo);

    }
}
