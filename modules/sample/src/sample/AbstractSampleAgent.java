package sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rescuecore2.Constants;
import rescuecore2.config.NoSuchConfigOptionException;
import rescuecore2.log.Logger;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.standard.kernel.comms.StandardCommunicationModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   Abstract base class for sample agents.
   @param <E> The subclass of StandardEntity this agent wants to control.
 */
public abstract class AbstractSampleAgent<E extends StandardEntity> extends StandardAgent<E> {
    private static final int RANDOM_WALK_LENGTH = 50;

    private static final String SAY_COMMUNICATION_MODEL = StandardCommunicationModel.class.getName();
    private static final String SPEAK_COMMUNICATION_MODEL = ChannelCommunicationModel.class.getName();

    /**
       Whether to use AKSpeak messages or not.
    */
    protected boolean useSpeak;

    /**
       Cache of building IDs.
    */
    protected List<EntityID> buildingIDs;

    /**
       Cache of road IDs.
    */
    protected List<EntityID> roadIDs;

    /**
     * Cache of refuge IDs.
     */
    protected List<EntityID> refugeIDs;

    /**
     * the connectivity graph of all places in the world
     */
    protected Graph connectivityGraph;
    
    /**
     * a matrix containing the pre-computed distances between each two areas in the world
     */
    protected DistanceInterface distanceMatrix;
    
    /**
     * The search algorithm.
     */
    protected SearchAlgorithm search;
    
    protected EntityID randomExplorationGoal = null;
    
    /**
     * Construct an AbstractSampleAgent.
     */
    protected AbstractSampleAgent() {
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        buildingIDs = new ArrayList<EntityID>();
        roadIDs = new ArrayList<EntityID>();
        refugeIDs = new ArrayList<EntityID>();
        for (StandardEntity next : model) {
            if (next instanceof Building) {
                buildingIDs.add(next.getID());
            }
            if (next instanceof Road) {
                roadIDs.add(next.getID());
            }
            if (next instanceof Refuge) {
                refugeIDs.add(next.getID());
            }
        }
        
        // load correct search algorithm
        search = selectSearchAlgorithm();

        Params.ONLY_ASSIGNED_TARGETS = config.getBooleanValue("only_assigned_targets", false);
        
        connectivityGraph = new Graph(model);
        distanceMatrix = new DistanceInterface(model);
        
        useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
        Logger.debug("Communcation model: " + config.getValue(Constants.COMMUNICATION_MODEL_KEY));
        Logger.debug(useSpeak ? "Using speak model" : "Using say model");
    }

    /**
       Construct a random walk starting from this agent's current location to a random building.
       @return A random walk.
    */
    protected List<EntityID> randomWalk() {
        List<EntityID> result = new ArrayList<EntityID>(RANDOM_WALK_LENGTH);
        Set<EntityID> seen = new HashSet<EntityID>();
        EntityID current = ((Human)me()).getPosition();
        for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
            result.add(current);
            seen.add(current);
            List<EntityID> possible = new ArrayList<EntityID>(connectivityGraph.getNeighbors(current));
            Collections.shuffle(possible, random);
            boolean found = false;
            for (EntityID next : possible) {
                if (seen.contains(next)) {
                    continue;
                }
                current = next;
                found = true;
                break;
            }
            if (!found) {
                // We reached a dead-end.
                break;
            }
        }
        return result;
    }
    
    protected List<EntityID> randomExplore()
    {
        // check if goal reached
        EntityID position = ((Human)me()).getPosition();
        if (randomExplorationGoal != null)
        {
            int distance = model.getDistance(position, randomExplorationGoal);
            //Logger.debugColor("RANDOM_EXPLORATION: distance to goal: " + distance, Logger.BG_BLUE);
            if (distance <= 20000)
            {
                randomExplorationGoal = null;
                //Logger.debugColor("RANDOM_EXPLORATION: goal reached", Logger.BG_BLUE);
            }
        }
        
        // select new exploration goal
        if (randomExplorationGoal == null)
        {
            //Logger.debugColor("RANDOM_EXPLORATION: selecting new goal", Logger.BG_BLUE);
            Collection<StandardEntity> roads = model.getEntitiesOfType(StandardEntityURN.ROAD);
            Entity[] roadArray = roads.toArray(new Entity[0]);
            int index = random.nextInt(100000);
            int step = getID().getValue();
            while (randomExplorationGoal == null)
            {
                index += step;
                index %= roadArray.length;
                Entity entity = roadArray[index];
                if (model.getDistance(position, entity.getID()) > 20000)
                {
                    randomExplorationGoal = entity.getID();
                    //Logger.debugColor("RANDOM_EXPLORATION: new goal selected", Logger.BG_BLUE);
                }
            }
        }
        
        // plan path to goal
        return search.search(position, randomExplorationGoal, connectivityGraph, distanceMatrix);
    }
    
    private SearchAlgorithm selectSearchAlgorithm()
    {
        // construct default search algorithm
        SearchAlgorithm instance = new BreadthFirstSearch();

        String searchClassName = "";
        try
        {
            // retrieve data from config
            String className = config.getValue("search_class");
            String basePackage = config.getValue("base_package");
            String teamName = config.getValue("team_name");
            searchClassName = basePackage + "." + teamName + "." + className;
            
            // instantiate search algorithm
            Class<?> concreteSearchClass = Class.forName(searchClassName);
            Object object = concreteSearchClass.newInstance();
            if (object instanceof SearchAlgorithm)
            {
                instance = (SearchAlgorithm) object;
            }
            else
            {
                Logger.debugColor(searchClassName + " is not a SearchAlgorithm.", Logger.BG_RED);
            }
        } catch (NoSuchConfigOptionException e)
        {
            Logger.debugColor("SearchAlgorithm config not found. Using BreadthFirstSearch.", Logger.BG_BLUE);
        } catch (ClassNotFoundException e)
        {
            Logger.debugColor("SearchAlgorithm could not be found: " + searchClassName, Logger.BG_RED);
//            e.printStackTrace();
        } catch (InstantiationException e)
        {
            Logger.debugColor("SearchAlgorithm " + searchClassName + " could not be instantiated. (abstract?!)", Logger.BG_RED);
//            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            Logger.debugColor("SearchAlgorithm " + searchClassName + " must have an empty constructor.", Logger.BG_RED);
//            e.printStackTrace();
        }

        if (config.getBooleanValue("search_benchmark", false))
        {
            SearchAlgorithm secondarySearch = new BreadthFirstSearch();
            instance = new SearchBenchmark(instance, secondarySearch);
        }
        
        return instance;
    }
}
