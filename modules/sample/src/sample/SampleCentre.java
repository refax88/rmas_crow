package sample;

import static rescuecore2.misc.Handy.objectsToIDs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rescuecore2.config.NoSuchConfigOptionException;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 * A sample center agent.
 */
public class SampleCentre extends StandardAgent<Building>
{
    private AssignmentSolver centralSolver = null;
    private ArrayList<EntityID> agents = new ArrayList<EntityID>();
    private Map<EntityID, CivilianUpdate> civilianUpdates = null;
    private CivilianHealthPredictor healthTracker = null;
    private List<Double> predictionStatiscics;

    @Override
    public String toString()
    {
        return "Sample centre";
    }

    public SampleCentre()
    {
        predictionStatiscics = new ArrayList<Double>();
        civilianUpdates = new HashMap<EntityID, CivilianUpdate>();
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard)
    {
        if (centralSolver == null && model != null)
        {
            centralSolver = new AssignmentSolver(model, config);
            Params.START_EXPERIMENT_TIME = config.getIntValue("experiment_start_time", 25);
            // read com range in m and convert to mm
            Params.simulatedCommunicationRange = config.getIntValue("simulated_communication_range", 100) * 1000;
            Params.areaCoveredByFireBrigade = config.getIntValue("area_covered_by_firebrigade", 30);
            Params.ONLY_ASSIGNED_TARGETS = config.getBooleanValue("only_assigned_targets", false);
            Params.OPTIMIZE_ASSIGNMENT = config.getBooleanValue("optimize_assignment", true);
            Params.DSA_CHANGE_VALUE_PROBABILITY = config.getFloatValue("dsa_change_value_probability", 0.5);
        }
        if (healthTracker == null && model != null)
        {
            healthTracker = instantiateCivilianHealthPredictor();
        }

        // Find all buildings that are on fire
        Collection<EntityID> burning = getBurningBuildings();
        Logger.debugColor("Number of known BURNING buildings: " + burning.size(), Logger.BG_LIGHTBLUE);

        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY))
        {
            // Subscribe to station channels
            sendSubscribe(time, Params.PLATOON_CHANNEL);
        }

        civilianUpdates.clear();
        for (Command next : heard)
        {
            // Process speak messages
            if (next instanceof AKSpeak)
            {
                AKSpeak speak = (AKSpeak) next;
                EntityID senderId = speak.getAgentID();

                // add fire brigade agents
                if (model.getEntity(senderId).getURN() == StandardEntityURN.FIRE_BRIGADE.toString())
                {
                    if (!agents.contains(senderId))
                        agents.add(senderId);
                    byte content[] = speak.getContent();
                    processMessageContent(content, senderId.getValue());
                }
            }
            sendRest(time);
        }
        
        // update civilian health
        if (healthTracker != null)
        {
            for (CivilianUpdate update: civilianUpdates.values())
            {
                double difference = update.getRealHitPoints() - healthTracker.getPrediction(update.getID(), update.getTime());
                Logger.debugColor("updated CIVILIAN: hp=" + update.getRealHitPoints() + ",  dmg=" + update.getRealDamage(), Logger.BG_RED);
                // add to statistics
                predictionStatiscics.add(Math.abs(difference));
                healthTracker.update(update.getID(), update.getTime(), update.getHitPoints(), update.getDamage());
            }
            if (! predictionStatiscics.isEmpty())
            {
                double averageError = 0;
                for (double value: predictionStatiscics)
                {
                    averageError += value;
                }
                averageError /= predictionStatiscics.size();
                Logger.debugColor("prediction statistic: " + time + " # " + predictionStatiscics.size() + ", avg error: " + averageError, Logger.BG_MAGENTA);
            }
        }

        // Compute assignment
        ArrayList<Building> targets = new ArrayList<Building>(burning.size());
        for (EntityID next : burning)
        {
            Building b = (Building) model.getEntity(next);
            targets.add(b);
        }
        byte[] message = centralSolver.act(time, agents, targets);

        // Send out assignment
        if (message != null)
        {
            int[] intArray = SimpleProtocol.byteArrayToIntArray(message, true);
            Logger.debugColor("STATION SENDS AssignmentMessage: ", Logger.BG_WHITE);
            for (int i = 0; i < intArray.length; i++)
                System.out.print(intArray[i] + " ");
            System.out.println();

            sendSpeak(time, Params.STATION_CHANNEL, message);
        }

        if (config.getBooleanValue("search_benchmark", false))
        {
            Logger.debugColor("time: " + time + SearchStatistics.print(), Logger.BG_MAGENTA);
        }
    }

    private void processMessageContent(byte content[], int senderId)
    {
        if (content.length == 0)
            return;
        byte MESSAGE_TYPE = content[0];

        switch (MESSAGE_TYPE)
        {
        case SimpleProtocol.POS_MESSAGE:
            int pos = SimpleProtocol.getPosIdFromMessage(content);
            Logger.debugColor("Station: heard from agent " + senderId + " Pos: " + pos, Logger.BG_LIGHTBLUE);
            break;
        case SimpleProtocol.CIVILIAN_OBSERVATION_MESSAGE:
            Set<CivilianUpdate> updates = SimpleProtocol.parseCivilianObservationMessage(content);
            for (CivilianUpdate update: updates)
            {
                civilianUpdates.put(update.getID(), update);
            }
            Logger.debugColor("Station: heard from agent " + senderId + " updated Civilians " + updates.size(), Logger.BG_LIGHTBLUE);
            break;
        default:
            Logger.debugColor("Station: cannot parse message of type " + MESSAGE_TYPE + " Message size is " + content.length, Logger.BG_RED);
            break;
        }
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum()
    {
        return EnumSet.of(StandardEntityURN.FIRE_STATION, StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.POLICE_OFFICE);
    }

    private Collection<EntityID> getBurningBuildings()
    {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.BUILDING);
        List<Building> result = new ArrayList<Building>();
        for (StandardEntity next : e)
        {
            if (next instanceof Building)
            {
                Building b = (Building) next;
                if (b.getFieryness() > 0 && b.getFieryness() < 4)
                {
                    result.add(b);
                }
            }
        }
        // Sort by distance
        Collections.sort(result, new DistanceSorter(location(), model));
        return objectsToIDs(result);
    }
    
    private CivilianHealthPredictor instantiateCivilianHealthPredictor()
    {
        // construct default search algorithm
        CivilianHealthPredictor instance = null;

        String fullClassName = "";
        try
        {
            // retrieve data from config
            String className = config.getValue("health_predictor_class");
            String basePackage = config.getValue("base_package");
            String teamName = config.getValue("team_name");
            fullClassName = basePackage + "." + teamName + "." + className;
            
            // instantiate search algorithm
            Class<?> concreteSearchClass = Class.forName(fullClassName);
            Object object = concreteSearchClass.newInstance();
            if (object instanceof CivilianHealthPredictor)
            {
                instance = (CivilianHealthPredictor) object;
                instance.init();
            }
            else
            {
                Logger.debugColor(fullClassName + " is not a CivilianHealthPredictor.", Logger.BG_RED);
            }
        } catch (NoSuchConfigOptionException e)
        {
            instance = new NoHealthPredictor();
            Logger.debugColor("CivilianHealthPredictor config not found.", Logger.BG_BLUE);
        } catch (ClassNotFoundException e)
        {
            Logger.debugColor("CivilianHealthPredictor could not be found: " + fullClassName, Logger.BG_RED);
//            e.printStackTrace();
        } catch (InstantiationException e)
        {
            Logger.debugColor("CivilianHealthPredictor " + fullClassName + " could not be instantiated. (abstract?!)", Logger.BG_RED);
//            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            Logger.debugColor("CivilianHealthPredictor " + fullClassName + " must have an empty constructor.", Logger.BG_RED);
//            e.printStackTrace();
        }

        return instance;
    }
}
