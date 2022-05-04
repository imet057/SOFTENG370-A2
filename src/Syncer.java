import java.io.*;
import java.nio.*;
import java.nio.file.Files;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;

public class Syncer {

    public static void initialSync(File dirOne, File dirTwo) {
        setUpDotSync(dirOne);
        setUpDotSync(dirTwo);
        Checker.checkDeletedFiles(dirOne);
        Checker.checkDeletedFiles(dirTwo);

        merge(dirOne, dirTwo);
    }
    
    public static void sync(File dirOne, File dirTwo) {
        setUpDotSync(dirOne);
        setUpDotSync(dirTwo);
        Checker.checkDeletedFiles(dirOne);
        Checker.checkDeletedFiles(dirTwo);

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

            date = Utility.getFormattedTimeOfLastMod(file);
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
                    file.setLastModified(Utility.getTimeForSettingLastMod(fileStatus.get(fileName).get(0).get(0)));
                }
            }
        } catch (Exception e) {
        }
    }

    public static void merge(File dirOne, File dirTwo) {
        File dotSyncOne = new File(dirOne.getAbsolutePath() + "/.sync");
        File dotSyncTwo = new File(dirTwo.getAbsolutePath() + "/.sync");
        
        for (File dirOneFile : dirOne.listFiles()) {
            if (!dirOneFile.getName().equals(".sync")) {
                File sameFileInTwo = new File(dirTwo.getAbsolutePath() + "/" + dirOneFile.getName());

                if (!sameFileInTwo.exists()) {
                    if (dirOneFile.isFile()) {
                        try {
                            Files.copy(dirOneFile.toPath(), sameFileInTwo.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, 
                            java.nio.file.StandardCopyOption.COPY_ATTRIBUTES, java.nio.file.LinkOption.NOFOLLOW_LINKS);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        updateDotSync(dotSyncTwo, sameFileInTwo);
                    } else if (dirOneFile.isDirectory()) {
                        sameFileInTwo.mkdir();
                    }
                }
            }
        }

        for (File dirTwoFile : dirTwo.listFiles()) {
            if (!dirTwoFile.getName().equals(".sync")) {
                File sameFileInOne = new File(dirOne.getAbsolutePath() + "/" + dirTwoFile.getName());

                if (!sameFileInOne.exists()) {
                    if (dirTwoFile.isFile()) {
                        try {
                            Files.copy(dirTwoFile.toPath(), sameFileInOne.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, 
                            java.nio.file.StandardCopyOption.COPY_ATTRIBUTES, java.nio.file.LinkOption.NOFOLLOW_LINKS);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        updateDotSync(dotSyncOne, sameFileInOne);
                    } else if (dirTwoFile.isDirectory()) {
                        sameFileInOne.mkdir();
                    }
                }
            }          
        }
    }
}
