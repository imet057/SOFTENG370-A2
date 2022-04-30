import java.io.*;

public class Checker {
    
    public static boolean doTwoDirsExist(String[] dirs) {
        File dirOne = new File(dirs[0]);
        File dirTwo = new File(dirs[1]);

        if (dirOne.exists() && dirTwo.exists() && dirOne.isDirectory() && dirTwo.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }

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
}
