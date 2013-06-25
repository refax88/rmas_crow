/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.AAMAS12;
import RSLBench.Assignment.DecentralAssignment;
import rescuecore2.worldmodel.EntityID;
import RSLBench.Helpers.UtilityMatrix;
import RSLBench.Comm.AbstractMessage;
import RSLBench.Assignment.Assignment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 *
 * @author riccardo
 */
public class Dummy implements DecentralAssignment{
    private EntityID _agentID;
    private EntityID _targetID;
    private UtilityMatrix _utility;
     /**
     * Initialized this simulated agent.
     * @param agentID: the ID of the agent (as defined in the world model).
     * @param utility: the utility matrix for all agents that are within communication range 
     */
    public void initialize(EntityID agentID, UtilityMatrix utility){
        _agentID = agentID;
        _utility = utility;   
        _targetID = Assignment.UNKNOWN_TARGET_ID;
    }
    
    /**
     * Improves the assignment for the specific agent.
     * This function is called unless any agent in the world return true.
     * @return true, if the assignment of this agent changed, false otherwise.
     */
    public boolean improveAssignment(){
       
       ArrayList<EntityID> me = new ArrayList<EntityID>();
       me.add(_agentID);
       ArrayList<EntityID> bestTarget = (ArrayList<EntityID>)_utility.getNBestTargets(1, me);
       _targetID = bestTarget.get(0);
        return true;
        
        
    }
    
    /**
     * Returns the ID of the agent.
     * @return the ID of the agent. 
     */
    public EntityID getAgentID(){
        return _agentID;
    }

    /**
     * Returns the ID  of the currently selected target. 
     * @return the ID of the target. 
     */
    public EntityID getTargetID(){
        return _targetID;
        
    }

    /**
     * Send a set of messages to all neighboring agents 
     * (i.e. those which are within communication range).
     * @return The set of messages to be send.
     */
    public Collection<AbstractMessage> sendMessages(){
       return new LinkedList<AbstractMessage>();   
    }
    
    /**
     * Receive messages of the neighbor agents.
     * @param collection of messages received from other agents near by.
     */
    public void receiveMessages(Collection<AbstractMessage> messages){
        
    }
    /**
     * Return constraint checked
     * 
     * @return 
     */
    public int getCcc(){
        return 0;
    }
	
    public void resetStructures() {}
    
}
