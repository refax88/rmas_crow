package stringgame.solutions;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import stringgame.Player;

public class LearningPlayer extends Player
{
    class SA implements Comparable<SA> {
        @Override
        public String toString() {
            return s + " " + a;
        }

        String s;
        String a;

        @Override
        public int compareTo(SA o) {
            int c = s.compareTo(o.s);
            if(c != 0)
                return c;
            c = a.compareTo(o.a);
            return c;
        }
        
        
    };
    Map<SA, Double> q;
    SA lastSA = null;
    double alpha;
    final boolean debug = false;
    Random rand;

    public LearningPlayer() {
        q = new TreeMap<SA, Double>();
        rand = new Random();
        alpha = 1;
        StringBuilder s = new StringBuilder("_________");
        fillHM(s, 0);
        System.out.println("initialized q size: " + q.size());
    }
    
    void fillHM(StringBuilder s, int pos) {
        if(pos >= 9) {
            for(int i = 1; i <= 3; i++)
                for(int j = 1; j <= 3; j++) {
                    SA sa = new SA();
                    sa.s = s.toString();
                    sa.a = String.valueOf(i) + "," + String.valueOf(j);
                    q.put(sa, 0.0);
                }
            return;
        }
        s.replace(pos, pos+1, "_");
        fillHM(s, pos + 1);
        s.replace(pos, pos+1, "o");
        fillHM(s, pos + 1);
        s.replace(pos, pos+1, "x");
        fillHM(s, pos + 1);
    }
    
    void printQ() {
        for(SA sa : q.keySet()) {
            System.out.println(sa.s + " " + sa.a);
        }
    }

    int stringPosFromA(String a)
    {
        char row = a.charAt(0);
        char col = a.charAt(2);
        int i = 0, j = 0;
        switch (row) {
        case '1':
            i = 0;
            break;
        case '2':
            i = 1;
            break;
        case '3':
            i = 2;
            break;
        default:
            return 0;
        }
        switch (col) {
        case '1':
            j = 0;
            break;
        case '2':
            j = 1;
            break;
        case '3':
            j = 2;
            break;
        default:
            return 0;
        }
        return i*3 + j;
    }
    
    String chooseBestAction(String state) {
        String best = "1,1";
        Double bestVal = -Double.MAX_VALUE;
        for(int i = 1; i <= 3; i++)
            for(int j = 1; j <= 3; j++) {
                String a = String.valueOf(i) + "," + String.valueOf(j);
                int k = stringPosFromA(a);
                if(state.charAt(k) != '_')
                    continue;
                SA sa = new SA();
                sa.s = state;
                sa.a = a;
                Double v = q.get(sa);
                if(v != null) {
                    if(v > bestVal) {
                        bestVal = v;
                        best = a;
                    }
                } else {
                    System.err.println("boom " + sa.s + " " + sa.a);
                }
            }
        if(debug)
            System.out.println("best action for " + state + " is " + best);
        return best;
    }
    
    @Override
    public String sense(String state, double reward) {
        alpha *= 0.99;
        if(alpha < 0.05)
            alpha = 0.05;
        if(lastSA == null) {    // start trial
            lastSA = new SA();
            String a = chooseBestAction(state);
            lastSA.s = state;
            lastSA.a = a;
            if(debug)
                System.out.println("first: " + state + " -> " + a);
            return a;
        }
        
        // we have done in lastSA.s action lastSA.a and now landed in state and got reward.
        Double q_old = q.get(lastSA);
        if(q_old == null) {
            System.err.println("boom 2");
        }
        if(debug)
            System.out.println("was in " + lastSA.s + " did " + lastSA.a + " arrived in " + state + " got " + reward + " alpha: " + alpha);
        String a = chooseBestAction(state);
        SA sa = new SA();
        sa.s = state;
        sa.a = a;         // now actually current state + best action
        Double q_best = q.get(sa);
        double q_new = (1-alpha)*q_old + alpha*(reward + 1.0 * q_best);     // gamma = 1.0
        q.put(lastSA, q_new);
        if(debug)
            System.out.println("new Q: " + q_new + " choosing " + a);
        Double q_upd = q.get(lastSA);
        if(q_best == q_upd)
            System.out.println("no change for " + state + " " + a + " -- " + q_upd);
        else
            if(debug)
                System.out.println("update: " + lastSA + " " + q_best + " -> " + q_upd);
        lastSA = sa;
        
        double eps = 0.02 * alpha;
        if(rand.nextDouble() < eps) {
            lastSA.a = generateNextRandomStep(state);
            return lastSA.a;
        }
        
        return a;
    }

    @Override
    public void terminate(String state, double reward, boolean writeLearningData) {
        // finish trial
        sense(state, reward);
        lastSA = null;
    }
    
    String generateNextRandomStep(String state) {
        String ret;
        int num = 0;
        for (int i = 0; i < 9; i++) {
            if (state.charAt(i) == '_')
                num++;
        }
        if(num == 0)
            return "1,1";
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
    String generateRetStr(int num1) {
        String buf = String.valueOf(num1 / 3 + 1);
        buf += ",";
        buf += String.valueOf(num1 % 3 + 1);
        return buf;
    }

}
