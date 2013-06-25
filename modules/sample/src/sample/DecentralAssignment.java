package sample;

import java.util.Collection;

public interface DecentralAssignment
{
    /**
     * Initialized this simulated agent.
     * @param agentID the index of the agent (as used in the UtiliyMatrix).
     * @param utility the utility matrix for all agents.
     */
    public void initialize(int agentID, UtilityMatrix utility);
    
    /**
     * computes an assignment for this specific agent.
     * @return true, if the assignment of this agent changed, false otherwise.
     */
    public boolean improveAssignment();
    
    public int getAgentID();

    public int getTargetID();

    /**
     * send the current assignment of this agents to all its neighbors.
     * @return The assignment message containing the agent index and the target index.
     */
    public AssignmentMessage sendMessage();
    
    /**
     * Receive assignments of the neighbor agents.
     * @param messages collection of assignment messages of other agents close by.
     */
    public void receiveMessages(Collection<AssignmentMessage> messages);
}
