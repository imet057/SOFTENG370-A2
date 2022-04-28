import java.io.*;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid command. Follow the format: ./sync <dirName1> <dirName2>");
        } else {
            boolean twoDirExist = Checker.doTwoDirsExist(args);
            boolean oneDirExist = Checker.isOneDirOneNot(args);

            if (oneDirExist) {
                
            }
        }
    }
}
