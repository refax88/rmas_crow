package stringgame.grp1;

import stringgame.Player;

public class LearningPlayer extends Player
{

    @Override
    public String sense(String state, double reward) {
        // TODO Auto-generated method stub
        return "1,1";
    }

    @Override
    public void terminate(String state, double reward, boolean writeLearningData) {
        // TODO Auto-generated method stub
    }

}
