import java.io.*;

public class ScoreMgmt {
    static int noOfScores=0;
    public static int readScores(long[] arr) throws IOException {
        try{
            BufferedReader fr = new BufferedReader(new FileReader("highscore.txt"));
            System.out.println("Score starting update in file.");
            String sc;
            sc = fr.readLine();
            while (sc != null) {
                arr[noOfScores] = Long.parseLong(sc);
                noOfScores++;
                sc = fr.readLine();
            }
            return noOfScores;
        }
        catch (IOException e){
            System.out.println("Creating high score file");
            return 0;
        }
    }
    public static void writeScores(long[]arr,int noOfScores) throws IOException {
        BufferedWriter fw = new BufferedWriter(new FileWriter("highscore.txt"));
        ScoreMgmt.noOfScores = noOfScores;
        for (int j = 0; j < noOfScores; j++) {
            fw.write(arr[j] + "\n");
            System.out.println("Score updated in file." + arr[j]);
            fw.flush();
        }
    }

}
