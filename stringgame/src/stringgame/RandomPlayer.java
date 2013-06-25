package stringgame;

import java.util.Random;

public class RandomPlayer extends Player
{
    static final int checkNum = 8;
    static final int[][] lines = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 },
            { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 }, { 2, 4, 6 } };
    Random rand;

    public RandomPlayer() {
        rand = new Random();
    }

    @Override
    public
    String sense(String state, double reward) {
        StringBuilder ret = new StringBuilder();
        if (isNearSuccess(state, ret) == true)
            return ret.toString();
        if (isOpponentThreaten(state, ret, false) == true)
            return ret.toString();
        return generateNextRandomStep(state);
    }

    @Override
    public
    void terminate(String state, double reward, boolean writeLearningData) {
    }

    boolean isOpponentThreaten(String state, StringBuilder ret, boolean checkX) {
        for (int i = 0; i < checkNum; i++) {
            if (isThreaten(state, lines[i][0], lines[i][1], lines[i][2], ret,
                    checkX) == true)
                return true;
        }
        return false;
    }

    boolean isNearSuccess(String state, StringBuilder ret) {
        if (isOpponentThreaten(state, ret, true) == true)
            return true;
        return false;
    }

    String generateNextRandomStep(String state) {
        String ret;
        int num = 0;
        for (int i = 0; i < 9; i++) {
            if (state.charAt(i) == '_')
                num++;
        }
        int randomVal = rand.nextInt(num);
        for (int i = 0; i < 9; i++) {
            if (state.charAt(i) == '_') {
                if (randomVal == 0) {
                    ret = generateRetStr(i);
                    return ret;
                }
                randomVal--;
            }
        }
        return "1,1";
    }

    boolean isThreaten(String state, int num1, int num2, int num3, StringBuilder ret,
            boolean checkX) {
        char target = 'o';
        if(checkX == true)
           target = 'x';
        if (state.charAt(num1) == '_' && state.charAt(num2) == target
                && state.charAt(num3) == target) {
            ret.delete(0, ret.length());
            ret.append(generateRetStr(num1));
            return true;
        }
        if (state.charAt(num1) == target && state.charAt(num2) == '_'
                && state.charAt(num3) == target) {
            ret.delete(0, ret.length());
            ret.append(generateRetStr(num2));
            return true;
        }
        if (state.charAt(num1) == target && state.charAt(num2) == target
                && state.charAt(num3) == '_') {
            ret.delete(0, ret.length());
            ret.append(generateRetStr(num3));
            return true;
        }
        return false;
    }

    String generateRetStr(int num1) {
        String buf = String.valueOf(num1 / 3 + 1);
        buf += ",";
        buf += String.valueOf(num1 % 3 + 1);
        return buf;
    }
}
