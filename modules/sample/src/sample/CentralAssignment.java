package sample;

public interface CentralAssignment
{
    
    /**
     * Takes as input a utility matrix and computes the agent assignments.
     * 
     * @param utility
     *            utility matrix for each agent.
     * @return A mapping for each agent to a target
     */
    public AgentAssignments compute(UtilityMatrix utility);
}
