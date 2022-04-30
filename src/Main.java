import java.io.*;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid command. Follow the format: ./sync <dirName1> <dirName2>");
            return;
        } else {
            boolean twoDirExist = Checker.doTwoDirsExist(args);

            // If one or two directories missing
            if (!twoDirExist) {
                File dirToMake = Checker.getDirToMake(args); // Returns a directory to make if one directory exists but one does not

                // If dirToMake is null then both directories do not exist
                if (dirToMake != null) {
                    dirToMake.mkdir();
                } else {
                    System.out.println("Cannot sync: Both of the input directories do not exist");
                    return;
                }
            }

        }

        System.out.println("test");
    }
}
