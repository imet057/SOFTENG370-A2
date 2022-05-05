import java.io.*;
import java.nio.*;
import java.nio.file.Files;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;

public class Syncer {
    
    public static void merge(File dirOne, File dirTwo) {
        setUpDotSync(dirOne);
        setUpDotSync(dirTwo);
        Checker.checkDeletedFiles(dirOne);
        Checker.checkDeletedFiles(dirTwo);

        sync(dirOne, dirTwo);

        for (File subDir : dirOne.listFiles()) {
            if (subDir.isDirectory()) {
                File subDirInOtherDir = new File(dirTwo.getAbsolutePath() + "/" + subDir.getName());
                merge(subDir, subDirInOtherDir);
            }
        }
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

    public static void sync(File dirOne, File dirTwo) {
        File dotSyncOne = new File(dirOne.getAbsolutePath() + "/.sync");
        File dotSyncTwo = new File(dirTwo.getAbsolutePath() + "/.sync");
        
        syncLogic(dirOne, dirTwo, dotSyncTwo);
        syncLogic(dirTwo, dirOne, dotSyncOne);
    }

    /**
     * Peforms sync between two files
     * 
     * @param dir
     * @param otherDir
     * @param dotSync
     */
    public static void syncLogic(File dir, File otherDir, File dotSync) {
        for (File fileInDir : dir.listFiles()) {
            if (!fileInDir.getName().equals(".sync")) {
                File sameFileInOtherDir = new File(otherDir.getAbsolutePath() + "/" + fileInDir.getName());

                if (!sameFileInOtherDir.exists()) {
                    if (fileInDir.isFile()) {
                        Utility.copyFile(fileInDir, sameFileInOtherDir);

                        updateDotSync(dotSync, sameFileInOtherDir);
                    } else if (fileInDir.isDirectory()) {
                        sameFileInOtherDir.mkdir();
                    }
                } else {
                    if (!fileInDir.isDirectory()) {
                        File[] files = {fileInDir, sameFileInOtherDir};
                        File[] dirs = {dir, otherDir};
                        File[] dotSyncs = {new File(dir.getAbsolutePath() + "/.sync"), dotSync};

                        syncTwoExistingFile(files, dirs, dotSyncs);
                    }
                }
            }
        }
    }

    /**
     * Method that syncs two existing files that have the same file name and extension
     * 
     * @param files
     * @param dirs
     * @param dotSyncs
     */
    public static void syncTwoExistingFile(File[] files, File[] dirs, File[] dotSyncs) {
        Map<String, List<List<String>>> fileStatusDirOne = Utility.getFileStatus(dotSyncs[0]);
        Map<String, List<List<String>>> fileStatusDirTwo = Utility.getFileStatus(dotSyncs[1]);
        List<List<String>> pairsOfFileOne = Utility.getPairsOfFileFromDotSync(files[0], dotSyncs[0]);
        List<List<String>> pairsOfFileTwo = Utility.getPairsOfFileFromDotSync(files[1], dotSyncs[1]);
        Date fileOneModTime = Utility.parseTimeFromString(pairsOfFileOne.get(0).get(0));
        Date fileTwoModTime = Utility.parseTimeFromString(pairsOfFileTwo.get(0).get(0));

        // If a file has the same current digest in both directories but different modification dates, the earliest
        // modification date (from one of the sync files) is applied to both versions of the file, and the sync file entry is
        // updated to reflect this.
        if ((pairsOfFileOne.get(0).get(1).equals(pairsOfFileTwo.get(0).get(1))) &&
            !(pairsOfFileOne.get(0).get(0).equals(pairsOfFileTwo.get(0).get(0)))) {
                List<String> newPair = new ArrayList<>();
                
                // If file one's mod time is later
                if (fileOneModTime.after(fileTwoModTime)) {
                    newPair.add(pairsOfFileTwo.get(0).get(0));
                    newPair.add(pairsOfFileOne.get(0).get(1));
                    pairsOfFileOne.add(0, newPair);

                    files[0].setLastModified(fileTwoModTime.getTime());
                    fileStatusDirOne.put(files[0].getName(), pairsOfFileOne);

                    Utility.writeToDotSync(dotSyncs[0], fileStatusDirOne);
                } else {
                    newPair.add(pairsOfFileOne.get(0).get(0));
                    newPair.add(pairsOfFileTwo.get(0).get(1));
                    pairsOfFileTwo.add(0, newPair);

                    files[1].setLastModified(fileOneModTime.getTime());
                    fileStatusDirTwo.put(files[1].getName(), pairsOfFileTwo);

                    Utility.writeToDotSync(dotSyncs[1], fileStatusDirTwo);
                }
        } else if ((pairsOfFileOne.get(0).get(1).equals(pairsOfFileTwo.get(0).get(1))) &&
        (pairsOfFileOne.get(0).get(0).equals(pairsOfFileTwo.get(0).get(0)))) {
            return;
        } else {
            boolean fileOneOlderVer = Utility.isOlderVersion(pairsOfFileOne.get(0).get(1), pairsOfFileTwo);
            boolean fileTwoOlderVer = Utility.isOlderVersion(pairsOfFileTwo.get(0).get(1), pairsOfFileOne);

            if (fileOneOlderVer) {
                Utility.copyFile(files[1], files[0]);
                pairsOfFileOne.add(0, pairsOfFileTwo.get(0));
                fileStatusDirOne.put(files[0].getName(), pairsOfFileOne);

                Utility.writeToDotSync(dotSyncs[0], fileStatusDirOne);
            } else if (fileTwoOlderVer) {
                Utility.copyFile(files[0], files[1]);
                pairsOfFileTwo.add(0, pairsOfFileOne.get(0));
                fileStatusDirTwo.put(files[1].getName(), pairsOfFileTwo);

                Utility.writeToDotSync(dotSyncs[1], fileStatusDirTwo);
            } else {
                List<String> newPair = new ArrayList<>();

                System.out.println("working" + Arrays.toString(files));


                if (fileOneModTime.after(fileTwoModTime)) {
                    Utility.copyFile(files[0], files[1]);
                    newPair.add(pairsOfFileOne.get(0).get(0));
                    newPair.add(pairsOfFileOne.get(0).get(1));
                    pairsOfFileTwo.add(0, newPair);

                    files[1].setLastModified(fileOneModTime.getTime());
                    fileStatusDirTwo.put(files[1].getName(), pairsOfFileTwo);

                    Utility.writeToDotSync(dotSyncs[1], fileStatusDirTwo);
                } else {
                    Utility.copyFile(files[1], files[0]);
                    newPair.add(pairsOfFileTwo.get(0).get(0));
                    newPair.add(pairsOfFileTwo.get(0).get(1));
                    pairsOfFileOne.add(0, newPair);

                    files[0].setLastModified(fileTwoModTime.getTime());
                    fileStatusDirOne.put(files[0].getName(), pairsOfFileOne);

                    Utility.writeToDotSync(dotSyncs[0], fileStatusDirOne);
                }
            }
        }
    }
}
