package stringgame;

/** Base player class - derive from this to create your own player. */
abstract public class Player
{
    /// World interface for the agent - it observes a state, and a reward and should return an action.
    /**
     * Format: state is a 9 character string with chars 'x', 'o' or '_', where '_' marks a free spot
     * the action should output the place like 1,2 for "place mark at 1st row, 2nd column".
     * i.e. action = "x,y", where x, y in {'1', '2', '3'} (9 possible actions).
     */
    abstract public String sense(String state, double reward);

    /**
     *  When the agent has gone into a terminal state, it does not need to produce any action.
     */
    abstract public void terminate(String state, double reward, boolean writeLearningData);

    /** A player might implement load/save to store learned Q-Values in a file. */
    public void load(String qfile) {}
    public void save(String qfile) {}
};
