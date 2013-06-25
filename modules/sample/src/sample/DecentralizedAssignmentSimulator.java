package sample;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.log.Logger;
import rescuecore2.worldmodel.EntityID;

public class DecentralizedAssignmentSimulator implements CentralAssignment
{
    private String decentralizedAssignmentClass;
    private List<DecentralAssignment> simulatedAgents;
    private ComSimulator<AssignmentMessage>com;
    private int time;

    public DecentralizedAssignmentSimulator(String decentralAssignmentClass, ComSimulator<AssignmentMessage>com)
    {
        this.decentralizedAssignmentClass = decentralAssignmentClass;
        this.com = com;
        time = 0;
    }

    @Override
    public AgentAssignments compute(UtilityMatrix utility)
    {
        initialize(utility);

        Logger.debugColor("starting DSA\n", Logger.BG_YELLOW);
        boolean done = false;
        int iterations = 0;
        int initialConflicts = 0;
        int finalConflicts = 0;
        while (!done)
        {
            // send messages
            for (DecentralAssignment agent : simulatedAgents)
            {
                EntityID id = utility.getAgentIDFromIndex(agent.getAgentID());
                com.send(id, agent.sendMessage());
            }

            // receive messages
            for (DecentralAssignment agent : simulatedAgents)
            {
                EntityID id = utility.getAgentIDFromIndex(agent.getAgentID());
                agent.receiveMessages(com.retrieveMessages(id));
            }

            // improve assignment
            done = true;
            for (DecentralAssignment agent : simulatedAgents)
            {
                boolean improved = agent.improveAssignment();
                done = done && !improved;
            }
            
            // statistics
            if (iterations == 0)
            {
                initialConflicts = countConflicts(utility);
            }
            iterations++;
        }
        finalConflicts = countConflicts(utility);
        Logger.debugColor("DSA complete\n", Logger.BG_YELLOW);

        // combine assignments
        AgentAssignments assignments = new AgentAssignments(utility.getAgentCount());
        for (DecentralAssignment agent : simulatedAgents)
        {
            assignments.assign(agent.getAgentID(), agent.getTargetID());
        }
        Stats.writeDSAStatsToFile("dsa_stats_"+Params.simulatedCommunicationRange/1000+".dat", time, initialConflicts, finalConflicts, iterations);
        time++;

        return assignments;
    }
    
    private int countConflicts(UtilityMatrix utility)
    {
        int[] targetSelections = new int[utility.getTargetCount()];
        for (DecentralAssignment agent: simulatedAgents)
        {
            int id = agent.getTargetID();
            if (id != -1)
            {
                targetSelections[id]++;
            }
        }

        int conflicts = 0;
        for (int index = 0; index < targetSelections.length; index++)
        {
            conflicts += Math.max(0, targetSelections[index] - utility.getRequiredAgentCount(index));
        }
        
        return conflicts;
    }
    
    private void initialize(UtilityMatrix utility)
    {
        // initialize simulated agents
        simulatedAgents = new ArrayList<DecentralAssignment>();
        try
        {
            Class<?> concreteAssignmentClass = Class.forName(decentralizedAssignmentClass);
            for (int agentID = 0; agentID < utility.getAgentCount(); agentID++)
            {
                DecentralAssignment agent = (DecentralAssignment) concreteAssignmentClass.newInstance();
                agent.initialize(agentID, utility);
                simulatedAgents.add(agent);
            }
        } catch (ClassNotFoundException e)
        {
            Logger.debugColor("SolverClass could not be found: " + decentralizedAssignmentClass, Logger.BG_RED);
            e.printStackTrace();
        } catch (InstantiationException e)
        {
            Logger.debugColor("SolverClass " + decentralizedAssignmentClass + " could not be instantiated. (abstract?!)", Logger.BG_RED);
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            Logger.debugColor("SolverClass " + decentralizedAssignmentClass + " must have an empty constructor.", Logger.BG_RED);
            e.printStackTrace();
        }
    }

}
