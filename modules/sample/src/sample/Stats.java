package sample;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class Stats
{
    public static void writeHeader(String fileName, ArrayList<EntityID> agents, ArrayList<Building> targets)
    {
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.write("# Num Agents:" + agents.size() + " Num Targets:" + targets.size() + " StartTime:" + Params.START_EXPERIMENT_TIME + " EndTime: " + Params.MAX_EXPERIMENT_TIME
                    + " StickToLastCluster:" + Params.STICK_TO_LAST_CLUSTER + " SelectTargets when Idle: " + Params.AGENT_SELECT_IDLE_TARGET);
            out.newLine();
            out.write("# Time  NumBuildings  NumBurining  numOnceBurned  numDestroyed  totalAreaDestroyed");
            out.newLine();
            out.close();
        } catch (IOException e)
        {
            System.out.println("IOException:");
            e.printStackTrace();
        }
    }

    public static void writeStatsToFile(String fileName, int time, ArrayList<Building> buildings, StandardWorldModel world, double realizedUtility, double cumulativeRealizedUtility)
    {
        int numBuildings = 0;
        int numBurning = 0;
        int numDestroyed = 0;
        int numOnceBurned = 0;
        double totalAreaDestroyed = 0.0;
        Collection<StandardEntity> allBuildings = world.getEntitiesOfType(StandardEntityURN.BUILDING);
        // Find out known number of buildings and stats

        for (Iterator<StandardEntity> it = allBuildings.iterator(); it.hasNext();)
        {
            Building building = (Building) it.next();
            // double fire = building.getFieryness();
            double area = building.getTotalArea();
            numBuildings++;

            if (building.isOnFire())
                numBurning++;
            if (building.getFieryness() > 3)
            {
                numDestroyed++;
                totalAreaDestroyed = totalAreaDestroyed + area;
            }
            if (building.getFieryness() > 0)
                numOnceBurned++;
        }
        totalAreaDestroyed = totalAreaDestroyed / (1000.0);

        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.write(time + " " + numBuildings + " " + numBurning + " " + numOnceBurned + " " + numDestroyed + " " + totalAreaDestroyed + " " + realizedUtility + " " + cumulativeRealizedUtility);
            out.newLine();
            out.close();
        } catch (IOException e)
        {
            System.out.println("IOException:");
            e.printStackTrace();
        }
    }

    public static void writeDSAStatsToFile(String fileName, int time, int initialConflicts, int finalConflicts, int iterationCount)
    {
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.write(time + " " + initialConflicts + " " + finalConflicts + " " + iterationCount);
            out.newLine();
            out.close();
        } catch (IOException e)
        {
            System.out.println("IOException:");
            e.printStackTrace();
        }
    }

}
