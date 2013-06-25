package stringgame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class StupidPlayer extends Player
{

    @Override
    public
    String sense(String state, double reward) {
        for (int c = 0; c < 9; c++) {
            if (state.charAt(c) == '_') {
                int i = c / 3;
                int j = c % 3;
                String buf = String.valueOf(i + 1);
                buf += ",";
                buf += String.valueOf(j + 1);
                return buf;
            }
        }
        return "1,1";
    }

    @Override
    public
    void terminate(String state, double reward, boolean writeLearningData) {
        if (writeLearningData) {
            FileWriter fstream;
            try {
                fstream = new FileWriter("learning.dat", true);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            BufferedWriter out = new BufferedWriter(fstream);
            try {
                out.write(String.valueOf(reward));
                out.write("\n");
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
