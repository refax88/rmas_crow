package sample;

import java.util.ArrayList;
import java.util.Iterator;

import rescuecore2.log.Logger;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class BuildingCluster {

    // the id of the cluster
    public EntityID id;                      
    // Number of needed agents
    double neededAgents;
    // List of buildings assigned to the cluster
    ArrayList<Building> buildings;
    // List of agents assigned to the cluster
    ArrayList<EntityID> agents;
    // Reference to world model
    StandardWorldModel world;
    
    // Constructor
    public BuildingCluster(Building b, StandardWorldModel world) {
       buildings = new ArrayList<Building>();
       agents = new ArrayList<EntityID>();
       this.world = world;
       buildings.add(b);
       id = b.getID();
       neededAgents = 0;
    }

    // Print out some information
    public void info() {
       Logger.debugColor("Cluster " + id + " Buildings: " + buildings.size() + " Agents: " + agents.size(), Logger.BG_LIGHTBLUE);
    }

    /* 
     * Nearest distance from cluster to object
     */
    public double minDist(EntityID entityID) {
       if (buildings.size() == 0) {
          Logger.debugColor("No Buildings in Cluster!!", Logger.BG_RED);
          return Double.MAX_VALUE;
       }
       double min = Double.MAX_VALUE;
       for(int i=0; i<buildings.size(); i++) {
    	   double d = world.getDistance(entityID, buildings.get(i).getID()); 
    	   if (d<min) min = d;
       }
       return min;
    }

    // Distance from cluster centroid to object
    public double avgDist(EntityID entityID) {
       if (buildings.size() == 0) {
    	   Logger.debugColor("No Buildings in Cluster!!", Logger.BG_RED);
    	   return Double.MAX_VALUE;
       }
       double avgX = 0;
       double avgY = 0;
       for(int i=0; i<buildings.size(); i++) {
          avgX = avgX + buildings.get(i).getX();
          avgY = avgY + buildings.get(i).getY();
      }
      avgX = avgX / buildings.size(); 
      avgY = avgY / buildings.size();
      
      Pair<Integer, Integer> posXY = world.getEntity(entityID).getLocation(world);
      double d = Double.MAX_VALUE;
      double dx = avgX - posXY.first().doubleValue();
      double dy = avgY - posXY.second().doubleValue();
      d = Math.sqrt(dx*dx + dy*dy);
      return d;
    }

    // Is there any building burning?
    public boolean hasFire() {
       for(int i=0; i<buildings.size(); i++) {
          Building b = buildings.get(i);
          if (b.isOnFire())
             return true;
       }
       return false;
    }

    // Compute cluster utility
    public double getUtility() {
       double utility = 0.0;
       for(int i=0; i<buildings.size(); i++) {
          Building b = buildings.get(i);
          //if (b.isOnFire())
          utility = utility + (b.getFieryness());
          //utility = utility + (b.getFieryness() * b.getTotalArea());
          //utility = utility + (b.getTotalArea());
       }
       //utility = utility / buildings.size();
       return utility;
    }

    // Is Building b a member of the cluster?
    public boolean contains(Building b) {
       return buildings.contains(b);
    }

    // Update the status of the building 
    public void update(Building b) {
       for(int i=0; i<buildings.size(); i++) {
          if (buildings.get(i) == b) {
             buildings.set(i,b);
          }
       }
    }

    // Returns all burning targets
    public ArrayList<Building> getFiryBuildings() {
       ArrayList<Building> targets = new ArrayList<Building>();
       for (int i=0; i<buildings.size(); i++) {
          Building b = buildings.get(i);
          if (b.isOnFire())
             targets.add(b);
       }
       return targets;
    }

    // Returns the assignment from targets to clusters
    public int[] computeAgentBuildingAssignment()
    {
       ArrayList<Building> targets = getFiryBuildings();
       ArrayList<Object> targetsO = new ArrayList<Object>(targets.size());
       for (int i=0; i<targets.size(); i++)
    	   targetsO.add((Object)targets.get(i));
    	   
       Logger.debugColor("Cluster " + id + ": Assigning " +  agents.size() + " agents to " + targets.size() + " targets",Logger.BG_GREEN);
       CentralAssignmentEFPO assignment = new CentralAssignmentEFPO();
       
       double U[][] = ArrayStuff.computeUtilityMatrix(agents, targetsO, world);
       if (U == null) {
	   Logger.errC("ERROR: matrix is null!");
	   return null;
       }		
       double [][] A = assignment.compute(U);
       int[] agentAss = ArrayStuff.assignmentMatrixToAssignment(A, agents, targetsO, world);	                  
       return agentAss;
    }	
    
    
    public static void updateBuildingClusters(ArrayList<Building> targets, 
	    			       ArrayList<BuildingCluster> clusters, StandardWorldModel world) 
    {
	// Update targets in clusters
	/////////////////////////////
	for (Iterator<BuildingCluster> it2 = clusters.iterator(); it2.hasNext();) {
	    BuildingCluster cluster = (BuildingCluster) it2.next();
	    for (Iterator<Building> it = targets.iterator(); it.hasNext();) {
		Building target = (Building) it.next();
		if(cluster.contains(target)) {
		    cluster.update(target);
		    it.remove();
		}
	    }
	}
	System.out.println("Targets left after update: " + targets.size());

	// Merge targets with existing clusters
	///////////////////////////////////////
	boolean changed = true;
	while (changed) {
	    changed = false;
	    for (Iterator<Building> it = targets.iterator(); it.hasNext();) {
		Building building = it.next();
		for (Iterator<BuildingCluster> it2 = clusters.iterator(); it2.hasNext();) {
		    BuildingCluster cluster = it2.next();
		    double d = cluster.minDist(building.getID());
		    if (d < 40000) {
			cluster.buildings.add(building);
			it.remove();
			changed = true;
			break;
		    }
		}
	    }
	}
	System.out.println("Targets left after merge: " + targets.size());

	// Create new clusters
	/////////////////////////////
	while (targets.size() != 0) {
	    // Select random target
	    Iterator<Building> it = targets.iterator();
	    Building b = (Building) it.next();

	    // Build cluster from target
	    BuildingCluster cluster = new BuildingCluster(b, world);
	    it.remove();

	    // Add all connected targets
	    changed = true;
	    while (changed) {
		changed = false;
		for (Iterator<Building> it2 = targets.iterator(); it2.hasNext();) {
		    Building building = it2.next();
		    double d = cluster.minDist(building.getID());
		    if (d < 40000) {
			cluster.buildings.add(building);
			targets.remove(building);
			changed = true;
			break;
		    }
		}
	    }
	    // Add new cluster
	    clusters.add(cluster);
	}
	System.out.println("Left After create: " + targets.size());

	// Remove clusters without fire
	///////////////////////////////
	for (Iterator<BuildingCluster> it = clusters.iterator(); it.hasNext();) {
	    BuildingCluster cluster = it.next();
	    if(!cluster.hasFire()) {
		System.out.println("Removing EXTINGUISHED cluster " + cluster.id);
		it.remove();
	    }
	}
	System.out.println("Targets left after remove: " + targets.size());

	// TODO Merge clusters

	System.out.println("Have " + clusters.size() + " CLUSTERS");
	for (int i=0; i<clusters.size(); i++) 
	    clusters.get(i).info();

	if (clusters.size() == 0)
	    return;
    }
}
