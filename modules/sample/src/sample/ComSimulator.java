package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rescuecore2.log.Logger;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class ComSimulator<MessageType>
{
    private int maxCommunicationRange;
    private List<EntityID> agents;
    private Map<EntityID, List<MessageType>> messageInboxes;
    private Map<EntityID, Set<EntityID>> inRange;
    private boolean initialized;
    
    public ComSimulator(int maxCommunicationRange)
    {
        this.maxCommunicationRange = maxCommunicationRange;
        initialized = false;
    }
    
    public void initialize(List<EntityID> agents)
    {
        Logger.debugColor("initialize Com for "+agents.size()+" agents", Logger.BG_BLUE);
        this.agents = agents;
        messageInboxes = new HashMap<EntityID, List<MessageType>>();
        inRange = new HashMap<EntityID, Set<EntityID>>();
        for (EntityID id: agents)
        {
            messageInboxes.put(id, new ArrayList<MessageType>());
            inRange.put(id, new HashSet<EntityID>());
        }
        initialized = true;
    }
    
    public void update(StandardWorldModel world)
    {
        // delete old messages
        for (List<MessageType> inbox: messageInboxes.values())
        {
            inbox.clear();
        }
        for (EntityID id1: agents)
        {
            Set<EntityID>neighbors = inRange.get(id1);
            for (EntityID id2: agents)
            {
                if (id1.getValue() != id2.getValue())
                {
                    int distance = world.getDistance(id1, id2);
                    if (distance <= maxCommunicationRange)
                    {
                        neighbors.add(id2);
                    }
                    else
                    {
                        neighbors.remove(id2);
                    }
                }
            }
        }
    }
    
    public void send(EntityID agentID, MessageType message)
    {
        Set<EntityID>neighbors = inRange.get(agentID);
        for (EntityID neighborID: neighbors)
        {
            messageInboxes.get(neighborID).add(message);
        }
    }
    
    public List<MessageType> retrieveMessages(EntityID agentID)
    {
        List<MessageType> mesageInbox = messageInboxes.get(agentID);
        messageInboxes.put(agentID, new ArrayList<MessageType>());
        return mesageInbox; 
    }
    
    public boolean isInitialized()
    {
        return initialized;
    }
}
