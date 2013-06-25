package RSLBench.Comm;

import rescuecore2.worldmodel.EntityID;


public class AssignmentMessage extends AbstractMessage
{
    private EntityID _agentID;
    private EntityID _targetID;
    
    public AssignmentMessage(EntityID agentID, EntityID targetID)
    {
        _agentID = agentID;
        _targetID = targetID;
    }

    public EntityID getAgentID()
    {
        return _agentID;
    }

    public EntityID getTargetID()
    {
        return _targetID;
    }    
}
