package sample;

public class AssignmentMessage
{
    private int agentID;
    private int targetID;
    
    public AssignmentMessage(int agentID, int targetID)
    {
        this.agentID = agentID;
        this.targetID = targetID;
    }

    public int getAgentID()
    {
        return agentID;
    }

    public int getTargetID()
    {
        return targetID;
    }
    
}
