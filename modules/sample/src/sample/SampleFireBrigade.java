package sample;

import static rescuecore2.misc.Handy.objectsToIDs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rescuecore2.components.GlobalMemory;
import rescuecore2.components.GlobalMemoryAccess;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * A sample fire brigade agent.
 */
public class SampleFireBrigade extends AbstractSampleAgent<FireBrigade> implements GlobalMemoryAccess
{
    private static final String MAX_WATER_KEY = "fire.tank.maximum";
    private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
    private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";
    private static final String IGNORE_ACT_UNTIL = "kernel.agents.ignoreuntil";

    private int maxWater;
    private int maxDistance;
    private int maxPower;
    private int firstTimeToAct;
    private int assignedTarget = -1;

    private DummyGlobalMemory globalMemory = null;
    private final boolean silent = true;

    @Override
    public String toString() {
        return "Sample fire brigade";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        Params.START_EXPERIMENT_TIME = config.getIntValue("experiment_start_time", 25);
        firstTimeToAct = Math.max(config.getIntValue(IGNORE_ACT_UNTIL) + 1,
                Params.START_EXPERIMENT_TIME);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        Logger.info("Sample fire brigade connected: max extinguish distance = "
                + maxDistance + ", max power = " + maxPower + ", max tank = "
                + maxWater);
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        // TODO: get civilian from changeset and send message
        if (time == config
                .getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to station channel
            sendSubscribe(time, Params.STATION_CHANNEL);
        }
        for (Command next : heard) {
            if (next instanceof AKSpeak) {
                AKSpeak speak = (AKSpeak) next;
                int senderId = speak.getAgentID().getValue();
                byte content[] = speak.getContent();
                EntityID eid = speak.getAgentID();
                StandardEntity entity = model.getEntity(eid);
                if (entity.getURN() == StandardEntityURN.FIRE_STATION
                        .toString()) {
                    if (!silent)
                        Logger.debugColor("Heard FROM FIRE_STATION: " + next,
                                Logger.FG_GREEN);
                    processStationMessage(content, senderId);
                } else if (entity.getURN() == StandardEntityURN.FIRE_BRIGADE
                        .toString()) {
                    // Logger.debugColor("Heard FROM OTHER FIRE_BRIGADE: " +
                    // next,Logger.FG_LIGHTBLUE);
                }
            }
        }

        if (time < firstTimeToAct)
            return;

        if (time == Params.MAX_EXPERIMENT_TIME)
            System.exit(0);

        // Start to act
        // //////////////////////////////////////////////////////////////////////////////////////
        FireBrigade me = me();

        // / Send position to station
        sendSpeak(time, Params.PLATOON_CHANNEL, SimpleProtocol
                .getPosMessage(me()));

        // send visible civilians to station
        Set<Civilian> changedCivilians = findChangedCivilians(changed);
        // for (Civilian civilian: changedCivilians)
        // {
        // Logger.debugColor(" hp=" + civilian.getHP() + " dmg=" +
        // civilian.getDamage(), Logger.BG_RED);
        // }
        sendSpeak(time, Params.PLATOON_CHANNEL, SimpleProtocol
                .buildCivilianObservationMessage(changedCivilians, time));

        // Are we currently filling with water?
        // //////////////////////////////////////
        if (me.isWaterDefined() && me.getWater() < maxWater
                && location() instanceof Refuge) {
            if(!silent)
                Logger.debugColor("Filling with water at " + location(),
                    Logger.FG_MAGENTA);
            sendRest(time);
            return;
        }

        // Are we out of water?
        // //////////////////////////////////////
        if (me.isWaterDefined() && me.getWater() == 0) {
            // Head for a refuge
            List<EntityID> path = search.search(me().getPosition(), refugeIDs,
                    connectivityGraph, distanceMatrix);
            if (path != null) {
                // Logger.debugColor("Moving to refuge", //Logger.FG_MAGENTA);
                sendMove(time, path);
                return;
            } else {
                // Logger.debugColor("Couldn't plan a path to a refuge.",
                // //Logger.BG_RED);
                path = randomWalk();
                // Logger.debugColor("Moving randomly", //Logger.FG_MAGENTA);
                sendMove(time, path);
                return;
            }
        }

        // Find all buildings that are on fire
        Collection<EntityID> burning = getBurningBuildings();
        // //Logger.debug("Number of known buildings: " + all.size());

        // //Logger.debugColor("ONLY ASSIGNED TARGETS " +
        // Params.ONLY_ASSIGNED_TARGETS, //Logger.BG_RED);
        if (Params.ONLY_ASSIGNED_TARGETS) {
            // Try to plan to assigned target
            // ///////////////////////////////
            if (assignedTarget != -1) {
                EntityID target = new EntityID(assignedTarget);
                if (!burning.contains(target)) {
                    assignedTarget = -1;
                    return;
                }

                // extinguish if in range
                if (model.getDistance(me().getPosition(), target) <= maxDistance) {
                    // Logger.debugColor("Extinguishing ASSIGNED " + target,
                    // //Logger.FG_MAGENTA);
                    sendExtinguish(time, target, maxPower);
                    // sendSpeak(time, 1, ("Extinguishing " + next).getBytes());
                    return;
                }

                // move to assigned target
                List<EntityID> path = planPathToFire(target);
                if (path != null) {
                    // Logger.debugColor("Moving to ASSIGNED target",
                    // //Logger.FG_MAGENTA);
                    sendMove(time, path);
                    return;
                } else {
                    // Logger.debugColor("Cannot move to ASSIGNED target",
                    // //Logger.FG_RED);
                    path = randomWalk();
                    // Logger.debugColor("Moving randomly",
                    // //Logger.FG_MAGENTA);
                    sendMove(time, path);
                    return;
                }
            } else {
                // Logger.debugColor("Couldn't plan a path to a refuge.",
                // //Logger.BG_RED);
                List<EntityID> path = randomWalk();
                // Logger.debugColor("Moving randomly", //Logger.FG_MAGENTA);
                sendMove(time, path);
                return;
            }
        }

        // // Can we extinguish any right now?
        // // ///////////////////////////////////////
        // for (EntityID next : burning)
        // {
        // // //Logger.debugColor(next.toString() + " d=" +
        // // model.getDistance(me().getPosition(),next) + " max=" +
        // // maxDistance, //Logger.FG_LIGHTBLUE);
        // if (model.getDistance(me().getPosition(), next) <= maxDistance)
        // {
        // //Logger.debugColor("Extinguishing " + next, //Logger.FG_MAGENTA);
        // sendExtinguish(time, next, maxPower);
        // // sendSpeak(time, 1, ("Extinguishing " + next).getBytes());
        // return;
        // }
        // }

        // if we have no assigned target, select one using the utility function
        if (assignedTarget == -1 && !burning.isEmpty()) {
            ArrayList<EntityID> agents = new ArrayList<EntityID>();
            ArrayList<Object> buildings = new ArrayList<Object>();
            agents.add(getID());
            for (EntityID id : burning) {
                buildings.add(model.getEntity(id));
            }
            UtilityMatrix utility = new UtilityMatrix(agents, buildings, model);
            int bestIndex = 0;
            double bestUtility = 0;
            for (int index = 0; index < utility.getTargetCount(); index++) {
                if (bestUtility < utility.getUtility(0, index)) {
                    bestUtility = utility.getUtility(0, index);
                    bestIndex = index;
                }
            }
            assignedTarget = ((Building) buildings.get(bestIndex)).getID()
                    .getValue();
        }

        // Try to plan to assigned target
        // ///////////////////////////////
        if (assignedTarget != -1) {
            EntityID target = new EntityID(assignedTarget);
            if (!burning.contains(target)) {
                assignedTarget = -1;
                return;
            }

            // if we are in range: extinguish
            if (model.getDistance(me().getPosition(), target) <= maxDistance) {
                // Logger.debugColor("Extinguishing " + target,
                // //Logger.FG_MAGENTA);
                sendExtinguish(time, target, maxPower);
                // sendSpeak(time, 1, ("Extinguishing " + next).getBytes());
                return;
            }

            // else drive to target
            List<EntityID> path = planPathToFire(target);
            if (path != null) {
                // Logger.debugColor("Moving to ASSIGNED target",
                // //Logger.FG_MAGENTA);
                sendMove(time, path);
                return;
            } else {
                // Logger.debugColor("Cannot move to ASSIGNED target",
                // //Logger.FG_RED);
                if (Params.AGENT_SELECT_IDLE_TARGET) {
                    assignedTarget = -1;
                    return;
                }
            }
        }

        // Plan a path to a fire
        // /////////////////////////////////////////
        for (EntityID next : burning) {
            List<EntityID> path = planPathToFire(next);
            if (path != null) {
                // Logger.debugColor("Moving to nearest target",
                // //Logger.FG_MAGENTA);
                sendMove(time, path);
                return;
            }
        }
        // if (burning.size() == 0)
        // Logger.debugColor("No more buildings to extinguish!",
        // //Logger.FG_GREEN);
        // else
        // Logger.debugColor("Couldn't plan a path to one of the " +
        // burning.size() + " fires.", //Logger.FG_RED);
        List<EntityID> path = null;
        path = randomExplore();
        if (path == null) {
            path = randomWalk();
        }
        // Logger.debugColor("Moving randomly", //Logger.FG_MAGENTA);
        sendMove(time, path);
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
    }

    private Collection<EntityID> getBurningBuildings() {
        Collection<StandardEntity> e = model
                .getEntitiesOfType(StandardEntityURN.BUILDING);
        List<Building> result = new ArrayList<Building>();
        for (StandardEntity next : e) {
            if (next instanceof Building) {
                Building b = (Building) next;
                if (b.isOnFire()) {
                    result.add(b);
                }
            }
        }
        // Sort by distance
        Collections.sort(result, new DistanceSorter(location(), model));
        return objectsToIDs(result);
    }

    private List<EntityID> planPathToFire(EntityID target) {
        Collection<StandardEntity> targets = model.getObjectsInRange(target,
                maxDistance / 2);
        return search.search(me().getPosition(), objectsToIDs(targets),
                connectivityGraph, distanceMatrix);
    }

    private void processStationMessage(byte msg[], int senderId) {
        // Logger.debugColor("PROCESS MSG FROM STATION", //Logger.BG_LIGHTBLUE);
        if (msg.length == 0)
            return;
        byte MESSAGE_TYPE = msg[0];

        switch (MESSAGE_TYPE) {
        case SimpleProtocol.POS_MESSAGE:
            break;
        case SimpleProtocol.STATION_ASSIGNMENT_MESSAGE:
            // Logger.debugColor("msg received " + me(), //Logger.BG_MAGENTA);
            byte[] raw = SimpleProtocol.removeHeader(msg);
            int[] intArray = SimpleProtocol.byteArrayToIntArray(raw, false);
            int id = me().getID().getValue();
            for (int i = 0; i < intArray.length; i++) {
                if (intArray[i] == id) {
                    int targetID = intArray[i + 1];
                    Building newT = (Building) model.getEntity(new EntityID(
                            targetID));
                    if (!newT.isOnFire()) {
                        // Logger.debugColor("ERROR: Got target from station that is NOT ON FIRE: "
                        // + newT.getID() + " fire is " + newT.getFieryness(),
                        // //Logger.FG_RED);
                        continue;
                    }

                    // Take station assignment
                    if (assignedTarget == -1) {
                        assignedTarget = targetID;
                        // Logger.debugColor("Setting station assignment " +
                        // assignedTarget, //Logger.FG_GREEN);
                        break;
                    }

                    // Change current target if better one assigned
                    Building curT = (Building) model.getEntity(new EntityID(
                            assignedTarget));

                    if (curT.getFieryness() > newT.getFieryness()) {
                        assignedTarget = targetID;
                        // Logger.debugColor("Setting station assignment " +
                        // assignedTarget, //Logger.FG_GREEN);
                        break;
                    }
                }
            }
            break;
        default:
            // Logger.debugColor("Agent: cannot parse message of type " +
            // MESSAGE_TYPE + " Message size is " + msg.length,
            // //Logger.FG_RED);
            break;
        }
    }

    @Override
    public void setGlobalMemory(GlobalMemory g) {
        if (g instanceof DummyGlobalMemory)
            globalMemory = (DummyGlobalMemory) g;
    }

    private Set<Civilian> findChangedCivilians(ChangeSet changed) {
        Set<Civilian> civilians = new HashSet<Civilian>();
        for (EntityID id : changed.getChangedEntities()) {
            Entity entity = model.getEntity(id);
            if (entity instanceof Civilian) {
                Civilian civilian = (Civilian) entity;
                civilians.add(civilian);
            }
        }
        return civilians;
    }
}