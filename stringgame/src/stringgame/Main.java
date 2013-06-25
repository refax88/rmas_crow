package stringgame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class Main
{
    void test(StringBuilder t) {
        t.delete(0, t.length());
        t.append("y");
    }

    public static void main(String[] args) {
        String logFileName = "stats.dat";
        
        Player x = new StupidPlayer();
        
        Class<?> playerClass = null;
        
        // Load X from subpackage if given as param
        if (args.length >= 1) {
            String className = "stringgame." + args[0] + ".LearningPlayer";
            System.out.println("Instantiating player class: " + className);
            logFileName = "stats_" + args[0] + ".dat";
            // centralized assignment
            try {
                Class<?> concretePlayerClass = Class.forName(className);
                playerClass = concretePlayerClass;
                Object playerInstance = concretePlayerClass.newInstance();
                if (playerInstance instanceof Player) {
                    x = (Player) playerInstance;
                } else {
                    System.err.println("failure loading class: " + className);
                    System.exit(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        boolean againstSelf = false;
        if (args.length >= 2) {
            if(args[1].equals("1"))
                againstSelf = true;
        }
        
        //Player o = new stringgame.grp3.LearningPlayer();
        Player o = new RandomPlayer();
        if (againstSelf) {
            try {
                o = (Player) playerClass.newInstance();
            } catch (InstantiationException e1) {
                e1.printStackTrace();
                return;
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
                return;
            }
        }
        
        final int gamesPerRun = 500; // 50
        final int nrOfRuns = 1000;   // 1000

        FileWriter fstream;
        try {
            fstream = new FileWriter(logFileName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        BufferedWriter out = new BufferedWriter(fstream);

        for(int i = 0; i < nrOfRuns; i++) {
            Game game = new Game(x, o);      // new game, so stats are reset
            Game gameRev = new Game(o, x);      // players reversed for fairness
            for(int j = 0; j < gamesPerRun; j++) {
                game.play();
                gameRev.play();
            }

            System.out.println("Stats: X-win: " + (game.stats.xWin + gameRev.stats.oWin)
                    + " O-win: " + (game.stats.oWin + gameRev.stats.xWin)
                    + " Draw: " + (game.stats.draw + gameRev.stats.draw));
            try {
                out.write(String.valueOf(game.stats.xWin + gameRev.stats.oWin));
                out.write(" ");
                out.write(String.valueOf(game.stats.oWin + gameRev.stats.xWin));
                out.write(" ");
                out.write(String.valueOf(game.stats.draw + gameRev.stats.draw));
                out.write("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}
