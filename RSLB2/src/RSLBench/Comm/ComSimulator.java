package RSLBench.Comm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import RSLBench.Helpers.SimpleID;

import rescuecore2.log.Logger;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class ComSimulator extends AbstractCom
{

    
    public ComSimulator(int maxCommunicationRange)
    {
        super(maxCommunicationRange);
    }
    
    public void send(EntityID agentID, AbstractMessage message)
    {
        Set<EntityID>neighbors = inRange.get(agentID);
        for (EntityID neighborID: neighbors)
        {
            messageInboxes.get(neighborID).add(message);
        }
    }
    
    public void send(EntityID agentID, Collection<AbstractMessage> messages)
    {
		Set<EntityID>neighbors = inRange.get(agentID);
    	for (EntityID neighborID: neighbors) {
        	for (AbstractMessage message : messages) {
    			messageInboxes.get(neighborID).add(message);
    		}
    	}
    }    
    
    public List<AbstractMessage> retrieveMessages(EntityID agentID)
    {
        List<AbstractMessage> mesageInbox = messageInboxes.get(agentID);
        messageInboxes.put(agentID, new ArrayList<AbstractMessage>());
        return mesageInbox; 
    }

}
