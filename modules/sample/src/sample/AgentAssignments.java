package sample;

import java.util.HashMap;

public class AgentAssignments
{
    public static final int UNASSIGNED = -1;
    private int[] assignment;
    private HashMap<Integer, Integer> targetSelectionCounts;
    
    public AgentAssignments(int agentCount)
    {
        assignment = new int[agentCount];
        for (int index = 0; index < assignment.length; index++)
        {
            assignment[index] = UNASSIGNED;
        }
        targetSelectionCounts = new HashMap<Integer, Integer>();
    }
    
    public void assign(int agentID, int targetID)
    {
        if (agentID < assignment.length)
        {
            assignment[agentID] = targetID;
            if (targetSelectionCounts.containsKey(targetID))
            {
                targetSelectionCounts.put(targetID, targetSelectionCounts.get(targetID) + 1);
            }
            else
            {
                targetSelectionCounts.put(targetID, 1);
            }
        }
    }
    
    public int getAssignment(int agentID)
    {
        if (agentID < assignment.length)
        {
            return assignment[agentID];
        }
        return UNASSIGNED;
    }
    
    public int getTargetSelectionCount(int targetID)
    {
        if (targetSelectionCounts.containsKey(targetID))
        {
            return targetSelectionCounts.get(targetID);
        }
        else
        {
            return 0;
        }
    }
    
    public double[][] toMatrix(int agentCount, int targetCount)
    {
        double[][] assignMatrix = new double[agentCount][targetCount];
        for (int agentID = 0; agentID < agentCount; agentID++)
        {
            for (int targetID = 0; targetID < targetCount; targetID++)
            {
                assignMatrix[agentID][targetID] = 0;
            }
        }
        for (int agentID = 0; agentID < assignment.length; agentID++)
        {
            if (assignment[agentID] != UNASSIGNED)
            {
                assignMatrix[agentID][assignment[agentID]] = 1;
            }
        }
        return assignMatrix;
    }
    
    public static AgentAssignments fromMatrix(double[][] assignMatrix)
    {
        AgentAssignments assignments = new AgentAssignments(assignMatrix.length);
        for (int agentID = 0; agentID < assignMatrix.length; agentID++)
        {
            double bestTargetUtility = 0.0;
            int bestTargetID = -1;
            for (int targetID = 0; targetID < assignMatrix[0].length; targetID++)
            {
                if (assignMatrix[agentID][targetID] > bestTargetUtility)
                {
                    bestTargetID = targetID;
                    bestTargetUtility = assignMatrix[agentID][targetID];
                }
            }
            if (bestTargetID != -1)
            {
                assignments.assign(agentID, bestTargetID);
            }
        }
        return assignments;
    }
    
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("assignments:\n");
        for (int agentID = 0; agentID < assignment.length; agentID++)
        {
            buffer.append("agent ").append(agentID).append(" > ").append(assignment[agentID]).append("\n");
        }
        return buffer.toString();
    }
}
