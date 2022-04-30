import java.io.*;

public class Checker {
    
    /**
     * Checks if two directories exist
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
}
