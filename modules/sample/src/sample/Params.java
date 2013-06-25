package sample;

public class Params {
    public final static int STATION_CHANNEL = 1;
    public final static int PLATOON_CHANNEL = 1;
    
    public final static int MAX_EXPERIMENT_TIME = 300;
    public static int START_EXPERIMENT_TIME = 25;

    public final static int SAMPLE_AGENTS_ASSIGNMENT = 0;
    public final static int EFPO_ALL_TARGETS_ASSIGNMENT = 1;
    public final static int EFPO_SINGLE_CLUSTER_TARGETS_ASSIGNMENT = 20;
    public final static int EFPO_MULTIPLE_CLUSTER_TARGETS_ASSIGNMENT = 30;
    public final static int HUNGARIAN_ASSIGNMENT = 4;
    public final static int LOAD_CLASS_BY_NAME = 5;
    
    public final static int DECENTRAL_DSA_ASSIGNMENT = 100;
    public final static int DSA_A = 101;
    public final static int DSA_B = 102;

    // priorize recently ignited buildings 
    public static boolean OPTIMIZE_ASSIGNMENT = true;
    public static boolean ONLY_ASSIGNED_TARGETS = false;
    
    /* 
     * when true and there is no target assigned by the station, 
     * the agent selects a target on his own
     */
    public static final boolean AGENT_SELECT_IDLE_TARGET = true;

    /*
     * Select here the type of central assignment
     */
    //public static int ASSIGNMENT_MODE = EFPO_MULTIPLE_CLUSTER_TARGETS_ASSIGNMENT;
    //public static int ASSIGNMENT_MODE = EFPO_SINGLE_CLUSTER_TARGETS_ASSIGNMENT;
    //public static int ASSIGNMENT_MODE = EFPO_ALL_TARGETS_ASSIGNMENT;
    //public static int ASSIGNMENT_MODE = SAMPLE_AGENTS_ASSIGNMENT;
    //public static int ASSIGNMENT_MODE = HUNGARIAN_ASSIGNMENT;
    //public static int ASSIGNMENT_MODE = DECENTRAL_DSA_ASSIGNMENT;
    public static int ASSIGNMENT_MODE = LOAD_CLASS_BY_NAME;
   
    /*
     * Clustering
     */
    public final static boolean STICK_TO_LAST_CLUSTER = true;    
    
    /*
     * DSA
     */
    public static int DSA_NUNBER_CYCLES = 10;
    public static double DSA_CHANGE_VALUE_PROBABILITY = 0.5;
    public static int DSA_VARIANT = DSA_A;
    
    // simulated communication
    public static int simulatedCommunicationRange = 10000;
    public static double areaCoveredByFireBrigade = 100;
}
