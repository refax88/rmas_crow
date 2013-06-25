package RSLBench.AAMAS12;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import rescuecore2.log.Logger;
import rescuecore2.worldmodel.EntityID;
import RSLBench.Assignment.Assignment;
import RSLBench.Assignment.DecentralAssignment;
import RSLBench.Comm.AbstractMessage;
import RSLBench.Comm.AssignmentMessage;
import RSLBench.Helpers.SimpleID;
import RSLBench.Helpers.UtilityMatrix;


public class DA_EFPO implements DecentralAssignment
{
    private static final boolean DEBUG = false;
    
    private UtilityMatrix _utilityM;
    private EntityID _agentID;
    private EntityID _targetID;
    private boolean _doneForRound = false;
    private Assignment _assignment = null;
    private boolean _isLeader = false;
    private Collection<AbstractMessage> _incomingAssignments;
    private Collection<AbstractMessage> _outgoingAssignments;
    private int _waitSteps = 0;

    @Override
    public void initialize(EntityID agentID, UtilityMatrix utilityM)
    {
        _agentID = agentID;
        _utilityM = utilityM;               
        _targetID = Assignment.UNKNOWN_TARGET_ID;
        _doneForRound = false;
        
        if (DEBUG)
        	Logger.debugColor("["+SimpleID.conv(agentID)+"] initialize targets: "+_utilityM.getNumTargets(), Logger.BG_LIGHTBLUE);                
        
    	// Are we the chosen one?
    	_isLeader = (_utilityM.getHighestAgentID() == _agentID);    	
    	if (_isLeader) {
    		if (DEBUG)
    			Logger.debugColor(" Local leader  ["+ SimpleID.conv(_agentID) +"] computes EFPO with " + _utilityM.getNumAgents() + 
    							" agents  and " + _utilityM.getNumTargets() + " targets", Logger.BG_LIGHTBLUE);
    		
    		_assignment = computeAssignmentFromUtilityMatrix(_utilityM);    	
    		//System.out.println("Assignment Matrix: " + localAssignment.toString());    	    		    		
    	}
    }

    @Override
    public boolean improveAssignment()
    {  
    	if (_doneForRound)
    		return false;
    	if (DEBUG) Logger.debugColor("["+ SimpleID.conv(_agentID) +"] improveAssignment", Logger.BG_LIGHTBLUE);

    	if (_isLeader) {
    		// Assign target for leader
    		_targetID = _assignment.getAssignment(_agentID);    		
    		if (DEBUG) Logger.debugColor("	 ["+ SimpleID.conv(_agentID) +"] Grabed own target  " + SimpleID.conv(_targetID), Logger.BG_LIGHTBLUE);
    		
    		// Put assignments for the other agents into send queue
    		_outgoingAssignments = new ArrayList<AbstractMessage>();
    		for (EntityID a :_utilityM.getAgents()) {
    			if (a == _agentID)
    				continue;
            	if (DEBUG) Logger.debugColor("["+ SimpleID.conv(_agentID) +"] SEND ASS TO Agent ["+ SimpleID.conv(a) +"] ", Logger.BG_GREEN);
    			EntityID t = _assignment.getAssignment(a);
    			AssignmentMessage am = new AssignmentMessage(a, t);
        		_outgoingAssignments.add(am);
    		}    		
    		_doneForRound = true;
        	if (DEBUG) Logger.debugColor("["+ SimpleID.conv(_agentID) +"] DONE! t=" + SimpleID.conv(_targetID), Logger.BG_GREEN);
            return false; // Done assignment
    	}
    	else { // We are not the leader but just a sheep
    		// Check for messages and do assignment when found
            for (AbstractMessage message: _incomingAssignments) {
            	if (message.getClass() == AssignmentMessage.class) {
                	if (((AssignmentMessage)message).getAgentID() == _agentID) {
                		_targetID = ((AssignmentMessage)message).getTargetID();
                    	if (DEBUG) Logger.debugColor("["+ SimpleID.conv(_agentID) +"] received target " + SimpleID.conv(_targetID), Logger.BG_LIGHTBLUE);
                        _incomingAssignments.clear();
                		_doneForRound = true;
                    	if (DEBUG) Logger.debugColor("["+ SimpleID.conv(_agentID) +"] DONE!", Logger.BG_GREEN);
                		return false; // Done assignment            		
                	}
            	}
            }    		
    		// Otherwise wait (i.e. still can improve = true) !!
        	if (DEBUG) Logger.debugColor("["+ SimpleID.conv(_agentID) +"] waiting for target", Logger.BG_LIGHTBLUE);
                if (_waitSteps++ < 10)
                   return true;    		
                else {
                   Logger.debugColor("["+ SimpleID.conv(_agentID) +"] Giving up waiting for assignment, chose greedily!",Logger.BG_RED);
                   _targetID = _utilityM.getHighestTargetForAgent(_agentID);
                   return false; // Give up
                }
    	}
    }

    @Override
    public EntityID getAgentID()
    {
        return _agentID;
    }

    @Override
    public EntityID getTargetID()
    {
        return _targetID;
    }

    @Override
    public Collection<AbstractMessage> sendMessages()
    {
    	return _outgoingAssignments;
    }

    @Override
    public void receiveMessages(Collection<AbstractMessage> messages)
    {
    	_incomingAssignments = messages;
    }    
    
    /**
     * This function returns the maximally possible summed utility when assigning N agents to 
     * the target under consideration of the target util and the proportion assigned by EFPO. 
     * Where N is the number of maximally needed agents on the target.
     * @param target
     * @param um
     * @param assMat
     * @return
     */
    private double getTotalTargetUtil(EntityID target, UtilityMatrix um, double[][] efpoAssMat) 
    {
   
	double tu = 0.0;
	int t_idx = um.target_id2idx(target);
	// Initialize map
	HashSet<EntityID> assA = new HashSet<EntityID>();

	// Assign best N agents
	int count = 0;
	while (count < um.getRequiredAgentCount(target)) {
		// Grab best available agent
		double best = - Double.MAX_VALUE;
		EntityID sel = um.getAgentID(0);
		for (EntityID a : um.getAgents()) {
			if (assA.contains(a)) continue;
			double u = efpoAssMat[um.id2idx(a)][t_idx] * um.getUtility(a, target);		    
			if (u>best) {best=u;sel=a;}
		}
		assA.add(sel);
		tu += um.getUtility(sel, target) * efpoAssMat[um.id2idx(sel)][t_idx];
		count++;
	}
	if (DEBUG) Logger.debugColor("Total Util for target " + SimpleID.conv(target) + " = " + tu + 
			" reqAmount = " + um.getRequiredAgentCount(target) , Logger.BG_YELLOW);
	return tu / (um.getRequiredAgentCount(target)*um.getRequiredAgentCount(target));
    }
    
     /**
      * This function selects for each agent the best target given the fact
      * that there might be multiple agents assigned to the same target. 
      * @param assignMatrix
      * @return
      */
     private Assignment computeAssignmentFromUtilityMatrix(UtilityMatrix um)
     {        	  
    	 double[][] efpoAssMat = computeEFPO(um.getMatrix());    	 
         Assignment assignments = new Assignment();
         
         if (DEBUG) {
             System.out.println("ASSMAT: ");
             for (EntityID t : um.getTargets()) {
        	 System.out.printf("%d ",SimpleID.conv(t));             
             }
             System.out.printf("\n");
             for (EntityID a : um.getAgents()) {
        	 for (EntityID t : um.getTargets()) {
        	     int a_idx = um.id2idx(a);
        	     int t_idx = um.target_id2idx(t);
        	     double util = efpoAssMat[a_idx][t_idx];
        	     System.out.printf("%1.2f ",util);
        	 }
        	 System.out.println();
             }
         }
         
 	// Initialize target to total utility map
 	HashMap<EntityID, Double> utilMap = new HashMap<EntityID, Double>(); // Mapping target to total util
 	for (EntityID t : um.getTargets()) {
 	    double totalUtil = getTotalTargetUtil(t, um, efpoAssMat);
 	    utilMap.put(t, totalUtil); 	    
 	}
 	ArrayList<EntityID> sortedList = (ArrayList<EntityID>) UtilityMatrix.sortByValue(utilMap);
 	
 	// Go over all sorted targets
	HashSet<EntityID> assA = new HashSet<EntityID>();
        for (EntityID t : sortedList) {
            if (DEBUG) Logger.debugColor("Assigning now target " + SimpleID.conv(t) + " = " + utilMap.get(t), Logger.BG_GREEN);
            int t_idx = um.target_id2idx(t);
            // Assign best N agents to target
            int count = 0;
            while (count != um.getRequiredAgentCount(t)) {
           
        	// Grab best and available agent
        	double best = - Double.MAX_VALUE;
        	boolean found = false;
        	EntityID selA = um.getAgentID(0);
        	for (EntityID a : um.getAgents()) {
        	    if (assA.contains(a)) continue;
        	    double u = efpoAssMat[um.id2idx(a)][t_idx];		    
        	    if (u>best) {best=u;selA=a;found=true;}
        	}
        	if (found) {
        	    assignments.assign(selA, t);
        	    assA.add(selA);
        	    count++;
        	    Logger.debugColor("Assigning a:" + SimpleID.conv(selA) + " to t:"+ SimpleID.conv(t), Logger.BG_GREEN);
        	}
        	else {
        	    if (DEBUG) Logger.debugColor("Not enough agents for target " + SimpleID.conv(t), Logger.BG_RED);
        	    break;
        	}
            }		
 	}
 	 	
 	return assignments;
     }    
     
     
     // Compute the assignment
     private double[][] computeEFPO(double[][] U)
     {       	    	
         if (U.length == 0 || U[0].length == 0)
         {
             Logger.errC("ERROR: Cannot compute Matrix. Input is zero!");
             return null;
         }
         if (DEBUG)
         {
             System.out.println("Utility Matrix: ");
             for (int i = 0; i < U.length; i++)
             {
                 for (int j = 0; j < U[i].length; j++)
                 {
                     System.out.format("%1.2f ", U[i][j]);
                 }
                 System.out.println();
             }
             System.out.println();
         }
         
         return new double[5][5];
     }
     public int getCcc(){
         return 0;
     }
	public void resetStructures() {}
}
