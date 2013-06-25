/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Comm;

import RSLBench.Helpers.SimpleID;

import rescuecore2.log.Logger;
import rescuecore2.standard.entities.StandardWorldModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author fabio
 */
public abstract class AbstractCom {
    protected int maxCommunicationRange;
    protected List<EntityID> agents;
    protected Map<EntityID, List<AbstractMessage>> messageInboxes;
    protected Map<EntityID, Set<EntityID>> inRange;
    protected boolean initialized;
    
    public AbstractCom(int maxCommunicationRange)
    {
        this.maxCommunicationRange = maxCommunicationRange;
        initialized = false;
    }
        public void initialize(List<EntityID> agents)
    {
        Logger.debugColor("initialize Com for "+agents.size()+" agents", Logger.BG_BLUE);
        this.agents = agents;
        messageInboxes = new HashMap<EntityID, List<AbstractMessage>>();
        inRange = new HashMap<EntityID, Set<EntityID>>();
        for (EntityID id: agents)
        {
            messageInboxes.put(id, new ArrayList<AbstractMessage>());
            inRange.put(id, new HashSet<EntityID>());
        }
        initialized = true;
    }
        
         public void update(HashMap<EntityID, EntityID> agentLocations , StandardWorldModel world)
    {
        // delete old messages
        for (List<AbstractMessage> inbox: messageInboxes.values()) {
            inbox.clear();
        }
        
        boolean USE_DISJOINT_AGENT_TEAMS=true;
        
        if (USE_DISJOINT_AGENT_TEAMS) { 
        	// Initialize Clusters with minimal distance
        	ArrayList<ArrayList<EntityID>> clusters = buildAgentPairs(agents, agentLocations, world);         
        	for (EntityID id: agents) {
        		// delete old assignments
        		inRange.get(id).clear();
        		// find cluster
        		boolean found = false;
        		for (ArrayList<EntityID> c : clusters) {
        			if (c.contains(id)) {
        				inRange.get(id).addAll(c);
        				found = true;
        			}      				
        		}
        		if (!found) {
        			Logger.debugColor("ERROR could not find cluster of agent " + id, Logger.BG_RED);
        		}      		
        	Logger.debugColor("Agent "+SimpleID.conv(id)+" has the following group of " + inRange.get(id).size() + " : ", Logger.BG_GREEN);
        	for (EntityID a : inRange.get(id)) {
        		System.out.print(SimpleID.conv(a) + ",");
        	}
        	System.out.println(SimpleID.conv(id)+",");
        	}
        }
        else {      	        
      		for (EntityID id1: agents) {
      			// delete old assignments
      			inRange.get(id1).clear();
      			for (EntityID id2: agents) {
      				if (id1.getValue() != id2.getValue()) {
      					int distance = world.getDistance(agentLocations.get(id1), agentLocations.get(id2));
      					if (distance <= maxCommunicationRange) {
      						inRange.get(id1).add(id2);
      					}
      				}
      			}
      		}    	
      	}
    }
           private ArrayList<ArrayList<EntityID>> buildAgentPairs(List<EntityID> agents, HashMap<EntityID, EntityID> agentLocations, StandardWorldModel world) 
    {
    	ArrayList<ArrayList<EntityID>> clusters = new ArrayList<ArrayList<EntityID>>();
    	Set<EntityID> assigned = new HashSet<EntityID>();
    	
    	Logger.debugColor("CLUSTERING START", Logger.BG_GREEN);
    	while (true) {    		
    		int minDist = Integer.MAX_VALUE;
    		EntityID bestA = new EntityID(-1), bestB=new EntityID(-1);
        	boolean found = false;
    		for (EntityID id1: agents) {
    			if (assigned.contains(id1)) continue;
    			for (EntityID id2: agents) {
    				if (assigned.contains(id2)) continue;
    				if (id1 == id2) continue;
    				int distance = world.getDistance(agentLocations.get(id1), agentLocations.get(id2));
    				if (distance <= maxCommunicationRange && distance < minDist) {
    					minDist = distance;
    					bestA = id1;
    					bestB= id2;
    					found = true;
    				}
    			}
    		}
    		if (found) {
    			ArrayList<EntityID> cl = new ArrayList<EntityID>();
    			cl.add(bestA);cl.add(bestB);
    			assigned.add(bestA);assigned.add(bestB);
    			clusters.add(cl);
    			//Logger.debugColor("Adding pair " + SimpleID.conv(bestA) + " --- " + SimpleID.conv(bestB) + " d= " + minDist, Logger.BG_RED);
    		}
    		else 
    			break;
    	}
    	
    	// Merge Clusters
    	while (clusters.size() > 1) {
    		boolean merged = false;
    		for (int i=0; i<clusters.size(); i++) {
        		for (int j=0; j<clusters.size(); j++)  {
        			if (i==j) continue;
        			ArrayList<EntityID> c1 = clusters.get(i);
        			ArrayList<EntityID> c2 = clusters.get(j);
    				if (clustersInRange(c1,c2, agentLocations, world)) {
    					ArrayList<EntityID> newC = clustersMerge(c1, c2);
    					clusters.remove(i);
        			    clusters.remove(--j);
        			    clusters.add(newC);
        			    merged = true;
        			    break;
    				}
        		}
        		if (merged) break;
    		}
      	  if (!merged) break;	
    	}
    			        	        		
        		
    	// Show Clusters
    	int count = 0;
    	for (ArrayList<EntityID> c: clusters) {
    		Logger.debugColor("Cluster " + count++ + ":", Logger.BG_GREEN);
    		for (EntityID id : c) {
    			System.out.print(id + ",");
    		}
    		System.out.println();
    	}    	    	
    	Logger.debugColor("CLUSTERING END", Logger.BG_GREEN);
    	
    	
    	return clusters;
    }
    	
    
    private boolean clustersInRange(ArrayList<EntityID> c1, ArrayList<EntityID> c2, HashMap<EntityID, EntityID> agentLocations, StandardWorldModel world) {
    	for (EntityID id1 : c1 ) {
        	for (EntityID id2 : c2 ) {
				int distance = world.getDistance(agentLocations.get(id1), agentLocations.get(id2));
				if (distance > maxCommunicationRange) {
					return false;
				}					
        	}        		
    	}    	
    	return true;
    }

    private ArrayList<EntityID> clustersMerge(ArrayList<EntityID> c1, ArrayList<EntityID> c2) {
    	ArrayList<EntityID> result = new ArrayList<EntityID>();
    	for (EntityID id1 : c1 ) {
    		result.add(id1);
    	}
        for (EntityID id2 : c2 ) {
        	result.add(id2);
        }    	
    	return result;
    } 
        
    public Set<EntityID> getNeighbors(EntityID agentID){
    	return inRange.get(agentID);
    }
    
   public boolean isInitialized()
    {
        return initialized;
    }
}
