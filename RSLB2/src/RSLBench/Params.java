package RSLBench;

public class Params {
	public static boolean OVERWRITE_FROM_COMMANDLINE = false;
	
	public final static int IGNORE_AGENT_COMMANDS_KEY_UNTIL = 3; 
    public static int START_EXPERIMENT_TIME = 25;
    public static int END_EXPERIMENT_TIME = 300;

	public final static int STATION_CHANNEL = 1;
    public final static int PLATOON_CHANNEL = 1;

    /**
     * When this is true, agents will only approach 
     * targets selected by the station (which simulates the decentralize assignment)
     * Otherwise they search for targets on their own.
     */
    public static boolean ONLY_ACT_ON_ASSIGNED_TARGETS = false;
    
    /** 
     * Parameters for simulated communication
     */
    public static int simulatedCommunicationRange = 10000;
    public static double areaCoveredByFireBrigade = 100.0;     
    
    /**
     * This factor controls the influence of travel costs on the
     * utility for targets. As bigger as the factor as bigger the
     * influence.
     */
    public static double TRADE_OFF_FACTOR_TRAVEL_COST_AND_UTILITY = 1.0;
    
    /**
     *  prioritize recently ignited buildings (should always be on) 
     */
     public static boolean OPTIMIZE_ASSIGNMENT = true;
    
    /** 
     * when true and there is no target assigned by the station, 
     * the agent selects a target on his own (rather than doing nothing)
     */
     public static final boolean AGENT_SELECT_IDLE_TARGET = true;

     
     /**
      * DSA specific settings
      */
     public static double DSA_CHANGE_VALUE_PROBABILITY = 0.5;
          
}
