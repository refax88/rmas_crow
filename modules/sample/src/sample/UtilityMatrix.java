package sample;

import java.util.ArrayList;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class UtilityMatrix
{
    private double[][] utility;
    private int agentCount;
    private int targetCount;
    private ArrayList<EntityID> agents;
    private ArrayList<Integer> targetAreas;
    
    public UtilityMatrix(ArrayList<EntityID> agents, ArrayList<Object> targets, StandardWorldModel world)
    {
        utility = ArrayStuff.computeUtilityMatrix(agents, targets, world);
        agentCount = utility.length;
        targetCount = utility[0].length;
        this.agents = agents;
        
        targetAreas = new ArrayList<Integer>();
        for (Object object: targets)
        {
            if (object instanceof Building)
            {
                Building building = (Building) object;
                targetAreas.add(building.getTotalArea());
            }
            else if (object instanceof BuildingCluster)
            {
                int area = 0;
                BuildingCluster cluster = (BuildingCluster) object;
                for (Building building: cluster.buildings)
                {
                    area += building.getTotalArea();
                }
                targetAreas.add(area);
            }
        }
    }

    /**
     * Reads the utility value for the specified agent and target. 
     * @param agentID 
     * @param targetID
     * @return the utility value for the specified agent and target.
     */
    public double getUtility(int agentID, int targetID)
    {
        if (agentID >= 0 && agentID < agentCount && targetID >= 0 && targetID < targetCount)
        {
            return utility[agentID][targetID];
        }
        return 0.0;
    }

    /**
     * 
     * @return the number of agents considered in the matrix.
     */
    public int getAgentCount()
    {
        return agentCount;
    }

    /**
     * 
     * @return the number of targets considered in the matrix.
     */
    public int getTargetCount()
    {
        return targetCount;
    }
    
    /**
     * Maps from agent index to the agent's EntityID.
     * @param agentIndex the index of the agent, as used in the matrix.
     * @return the agent's EntityID.
     */
    public EntityID getAgentIDFromIndex(int agentIndex)
    {
        if (agentIndex >= 0 && agentIndex < agents.size())
        {
            return agents.get(agentIndex);
        }
        return null;
    }
    
    /**
     * Returns an estimate of how many agents are required for a specific
     * target.
     * 
     * @param targetID
     *            the id of the target
     * @return the amount of agents required or zero if targetID is out of
     *         range.
     */
    public int getRequiredAgentCount(int targetID)
    {
        return (int) Math.ceil(getTargetArea(targetID) / Params.areaCoveredByFireBrigade);
    }
    
    private int getTargetArea(int targetID)
    {
        if (targetID >= 0 && targetID < targetAreas.size())
        {
            return targetAreas.get(targetID);
        }
        return 0;
    }
    
    @Deprecated
    public double[][] getMatrix()
    {
        return utility;
    }
}
