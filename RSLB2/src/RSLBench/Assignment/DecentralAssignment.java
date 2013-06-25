package RSLBench.Assignment;

import java.util.Collection;

import rescuecore2.worldmodel.EntityID;

import RSLBench.Comm.AbstractMessage;
import RSLBench.Helpers.UtilityMatrix;

public interface DecentralAssignment
{
    /**
     * Initialized this simulated agent.
     * @param agentID: the ID of the agent (as defined in the world model).
     * @param utility: the utility matrix for all agents that are within communication range 
     */
    public void initialize(EntityID agentID, UtilityMatrix utility);
    
    /**
     * Improves the assignment for the specific agent.
     * This function is called unless any agent in the world return true.
     * @return true, if the assignment of this agent changed, false otherwise.
     */
    public boolean improveAssignment();
    
    /**
     * Returns the ID of the agent.
     * @return the ID of the agent. 
     */
    public EntityID getAgentID();

    /**
     * Returns the ID  of the currently selected target. 
     * @return the ID of the target. 
     */
    public EntityID getTargetID();

    /**
     * Send a set of messages to all neighboring agents 
     * (i.e. those which are within communication range).
     * @return The set of messages to be send.
     */
    public Collection<AbstractMessage> sendMessages();
    
    /**
     * Receive messages of the neighbor agents.
     * @param collection of messages received from other agents near by.
     */
    public void receiveMessages(Collection<AbstractMessage> messages);
    /**
     * Return constraint checked
     * 
     * @return 
     */
    public int getCcc();

    public void resetStructures();
}
