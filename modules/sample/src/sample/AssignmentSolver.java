package sample;

import java.io.File;
import java.util.ArrayList;

import rescuecore2.config.Config;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class AssignmentSolver
{
    private String assignmentSolverClassName = "";
    private String basePackage = "";
    private String teamName = "";
    private String logFileName = "none";
    private StandardWorldModel world;
    private EntityID lastCluster;
    private ArrayList<BuildingCluster> clusters;

    private CentralAssignment solver;
    private ComSimulator<AssignmentMessage> com;
    
    private double cumulativeRealizedUtility;

    public AssignmentSolver(StandardWorldModel world, Config config)
    {
        this.world = world;
        Params.ASSIGNMENT_MODE = Params.SAMPLE_AGENTS_ASSIGNMENT;
        cumulativeRealizedUtility = 0.0;
        
        basePackage = config.getValue("base_package");

        // default configuration: no assignment
        Params.ASSIGNMENT_MODE = Params.SAMPLE_AGENTS_ASSIGNMENT;
        logFileName = "central_assignment_sample_agents.dat";
        solver = new NoAssignment();
        teamName = config.getValue("team_name", "solutions");

        String assignmentMode = config.getValue("assignment_mode");
        if (assignmentMode.equals("sample"))
        {
            Params.ASSIGNMENT_MODE = Params.SAMPLE_AGENTS_ASSIGNMENT;
            logFileName = "central_assignment_sample_agents.dat";
        } else if (assignmentMode.equals("all_target"))
        {
            logFileName = "central_assignment_efpo_all_targets.dat";
            Params.ASSIGNMENT_MODE = Params.EFPO_ALL_TARGETS_ASSIGNMENT;
            solver = new CentralAssignmentLegacyAdaptor(new CentralAssignmentEFPO());
            
        } else if (assignmentMode.equals("singlecluster"))
        {
            logFileName = "central_assignment_efpo_single_cluster.dat";
            Params.ASSIGNMENT_MODE = Params.EFPO_SINGLE_CLUSTER_TARGETS_ASSIGNMENT;
        } else if (assignmentMode.equals("multicluster"))
        {
            logFileName = "central_assignment_efpo_multi_cluster.dat";
            Params.ASSIGNMENT_MODE = Params.EFPO_MULTIPLE_CLUSTER_TARGETS_ASSIGNMENT;
        } else if (assignmentMode.equals("hungarian"))
        {
            assignmentSolverClassName = basePackage + "." + teamName + "." + "CentralAssignmentHungarian";
            logFileName = "central_assignment_hungarian.dat";
            try
            {
                Class<?> concreteAssignmentClass = Class.forName(assignmentSolverClassName);
                solver = new CentralAssignmentLegacyAdaptor((LegacyCentralAssignment) concreteAssignmentClass.newInstance());
            } catch (ClassNotFoundException e)
            {
                Logger.debugColor("SolverClass could not be found: " + assignmentSolverClassName, Logger.BG_RED);
                e.printStackTrace();
            } catch (InstantiationException e)
            {
                Logger.debugColor("SolverClass " + assignmentSolverClassName + " could not be instantiated. (abstract?!)", Logger.BG_RED);
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                Logger.debugColor("SolverClass " + assignmentSolverClassName + " must have an empty constructor.", Logger.BG_RED);
                e.printStackTrace();
            }
            Params.ASSIGNMENT_MODE = Params.HUNGARIAN_ASSIGNMENT;
        } else if (assignmentMode.equals("frodo"))
        {
            assignmentSolverClassName = basePackage + "." + "frodo.CentralAssignmentFrodo";
            logFileName = "central_assignment_hungarian.dat";
            try
            {
                Class<?> concreteAssignmentClass = Class.forName(assignmentSolverClassName);
                solver = (CentralAssignment) concreteAssignmentClass.newInstance();
            } catch (ClassNotFoundException e)
            {
                Logger.debugColor("SolverClass could not be found: " + assignmentSolverClassName, Logger.BG_RED);
                e.printStackTrace();
            } catch (InstantiationException e)
            {
                Logger.debugColor("SolverClass " + assignmentSolverClassName + " could not be instantiated. (abstract?!)", Logger.BG_RED);
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                Logger.debugColor("SolverClass " + assignmentSolverClassName + " must have an empty constructor.", Logger.BG_RED);
                e.printStackTrace();
            }
            Params.ASSIGNMENT_MODE = Params.HUNGARIAN_ASSIGNMENT;
        } else if (assignmentMode.equals("dsaa"))
        {
            Params.ASSIGNMENT_MODE = Params.DECENTRAL_DSA_ASSIGNMENT;
            Params.DSA_VARIANT = Params.DSA_A;
            logFileName = "decentral_assignment_dsa_a.dat";
        } else if (assignmentMode.equals("dsab"))
        {
            Params.ASSIGNMENT_MODE = Params.DECENTRAL_DSA_ASSIGNMENT;
            Params.DSA_VARIANT = Params.DSA_B;
            logFileName = "decentral_assignment_dsa_b.dat";
        } else if (assignmentMode.equals("load_class_by_name"))
        {
            Params.ASSIGNMENT_MODE = Params.LOAD_CLASS_BY_NAME;
            String className = config.getValue("assignment_class");
            assignmentSolverClassName = basePackage + "." + teamName + "." + className;
            logFileName = teamName + "_" + className + ".dat";
            if (config.getBooleanValue("decentralized_assignment", false))
            {
                // decentralized assignment
                com = new ComSimulator<AssignmentMessage>(Params.simulatedCommunicationRange);
                solver = new DecentralizedAssignmentSimulator(assignmentSolverClassName, com);
            }
            else
            {
                // centralized assignment
                try
                {
                    Class<?> concreteAssignmentClass = Class.forName(assignmentSolverClassName);
                    Object solverInstance = concreteAssignmentClass.newInstance();
                    if (solverInstance instanceof LegacyCentralAssignment)
                    {
                        solver = new CentralAssignmentLegacyAdaptor((LegacyCentralAssignment) solverInstance);
                    }
                    else if (solverInstance instanceof CentralAssignment)
                    {
                        solver = (CentralAssignment) solverInstance;
                    }
                } catch (ClassNotFoundException e)
                {
                    Logger.debugColor("SolverClass could not be found: " + assignmentSolverClassName, Logger.BG_RED);
                    e.printStackTrace();
                } catch (InstantiationException e)
                {
                    Logger.debugColor("SolverClass " + assignmentSolverClassName + " could not be instantiated. (abstract?!)", Logger.BG_RED);
                    e.printStackTrace();
                } catch (IllegalAccessException e)
                {
                    Logger.debugColor("SolverClass " + assignmentSolverClassName + " must have an empty constructor.", Logger.BG_RED);
                    e.printStackTrace();
                }
            }
        }

        // delete previous log file
        File f1 = new File(logFileName);
        f1.delete();
        System.out.println("ASSIGMENT_MODE: " + Params.ASSIGNMENT_MODE + " log file: " + logFileName);

        clusters = new ArrayList<BuildingCluster>();
        // FIXME: do we need this?
        lastCluster = new EntityID(-1);
    }

    public byte[] act(int time, ArrayList<EntityID> agents, ArrayList<Building> targets)
    {
        if (targets.size() == 0 || agents.size() == 0)
        {
            Logger.debugColor("No agents or targets for assignment! targets=" + targets.size() + " agents:" + agents.size(), Logger.BG_RED);
            return null;
        }

        // Print statistics to file
        if (time == Params.START_EXPERIMENT_TIME - 1)
        {
            Stats.writeHeader(logFileName, agents, targets);
        }

        // XXX: new process begin
        if (Params.ASSIGNMENT_MODE < 10)
        {
            // initialize simulated com system
            if (com != null)
            {
                if(! com.isInitialized())
                {
                    com.initialize(agents);
                }
                com.update(world);
            }
            
            ArrayList<Object> targetsO = new ArrayList<Object>(targets.size());
            for (int i = 0; i < targets.size(); i++)
            {
                targetsO.add((Object) targets.get(i));
            }
            
            Logger.debugColor(assignmentSolverClassName + ": assigning " + agents.size() + " agents to " + targets.size() + " targets", Logger.BG_GREEN);

            // TODO: use new utility matrix
//            double U[][] = ArrayStuff.computeUtilityMatrix(agents, targetsO, world);
//            if (U == null)
//            {
//                Logger.errC("ERROR: matrix is null!");
//                return null;
//            }
            UtilityMatrix utility = new UtilityMatrix(agents, targetsO, world);
            AgentAssignments A = solver.compute(utility);
            if (A != null)
            {
                double realizedUtility = 0.0;
                for (int i = 0; i < agents.size(); i++)
                {
                    int targetID = A.getAssignment(i);
                    double agentUtility = utility.getUtility(i, targetID);
                    double overcrowdFactor = Math.max(1.0, A.getTargetSelectionCount(targetID) / (double) utility.getRequiredAgentCount(targetID));
                    if(utility.getRequiredAgentCount(targetID) <= 0)
                        overcrowdFactor = 1;
                    realizedUtility += agentUtility / overcrowdFactor;
                }
                cumulativeRealizedUtility += realizedUtility;
                Stats.writeStatsToFile(logFileName, time, targets, world, realizedUtility, cumulativeRealizedUtility);
                int[] agentAssignments = ArrayStuff.assignmentMatrixToAssignment(A.toMatrix(agents.size(), targetsO.size()), agents, targetsO, world);
                return SimpleProtocol.buildAssignmentMessage(agents, targets, agentAssignments, true);
            }
            else
            {
                Stats.writeStatsToFile(logFileName, time, targets, world, 0.0, cumulativeRealizedUtility);
                return null;
            }
        }
        // XXX: old process begin
        else if (Params.ASSIGNMENT_MODE == Params.EFPO_MULTIPLE_CLUSTER_TARGETS_ASSIGNMENT)
        {
            BuildingCluster.updateBuildingClusters(targets, clusters, world);
            // Compute matrix for clusters
            ArrayList<Object> clustersO = new ArrayList<Object>(clusters.size());
            for (int i = 0; i < clusters.size(); i++)
                clustersO.add((Object) clusters.get(i));
            CentralAssignmentEFPO assignment = new CentralAssignmentEFPO();

            double U[][] = ArrayStuff.computeUtilityMatrix(agents, clustersO, world);
            if (U == null)
            {
                Logger.errC("ERROR: matrix is null!");
                return null;
            }
            double[][] A = assignment.compute(U);
            int[] clusterAss = ArrayStuff.assignmentMatrixToAssignment(A, agents, clustersO, world);

            // Distribute agents into clusters and compute for each the
            // assignment
            for (int i = 0; i < clusters.size(); i++)
                clusters.get(i).agents.clear();

            for (int i = 0; i < agents.size(); i++)
            {
                if (clusterAss[i] == -1)
                    continue;
                else
                {
                    int c = clusterAss[i];
                    clusters.get(c).agents.add(agents.get(i));
                }
            }
            byte[] message = new byte[1];
            message[0] = SimpleProtocol.STATION_ASSIGNMENT_MESSAGE;
            for (int i = 0; i < clusters.size(); i++)
            {
                BuildingCluster cluster = clusters.get(i);
                if (cluster.agents.size() > 0)
                {
                    int[] agentAss = cluster.computeAgentBuildingAssignment();
                    // Concatenate assignment with message
                    byte[] assignMsg = SimpleProtocol.buildAssignmentMessage(cluster.agents, cluster.getFiryBuildings(), agentAss, false);
                    byte[] newM = new byte[message.length + assignMsg.length];
                    for (int j = 0; j < message.length; j++)
                        newM[j] = message[j];
                    for (int j = 0; j < assignMsg.length; j++)
                        newM[message.length + j] = assignMsg[j];
                    message = newM;
                }
            }
            return message;
        } else if (Params.ASSIGNMENT_MODE == Params.EFPO_SINGLE_CLUSTER_TARGETS_ASSIGNMENT)
        {
            BuildingCluster.updateBuildingClusters(targets, clusters, world);

            // Select a single cluster
            EntityID selectedCluster = new EntityID(-1);

            // Use old cluster if still existent
            if (Params.STICK_TO_LAST_CLUSTER)
            {
                if (lastCluster.getValue() != -1)
                {
                    for (int i = 0; i < clusters.size(); i++)
                    {
                        if (clusters.get(i).id == lastCluster)
                        {
                            selectedCluster = clusters.get(i).id;
                            break;
                        }
                    }
                }
            }

            // Otherwise select best cluster
            if (selectedCluster.getValue() == -1)
            {
                double best = -Double.MAX_VALUE;
                for (int i = 0; i < clusters.size(); i++)
                {
                    // double score = clusters.get(i).buildings.size() /
                    // clusters.get(i).neededAgents;
                    double score = clusters.get(i).buildings.size();
                    if (score > best)
                    {
                        selectedCluster = clusters.get(i).id;
                        best = score;
                    }
                }
            }
            lastCluster = selectedCluster;

            if (clusters.isEmpty())
            {
                Logger.debugColor("Have no cluster, so also no assignment", Logger.BG_YELLOW);
                return null;
            }

            // Find the cluster with ID
            BuildingCluster cluster = clusters.get(0);
            for (int i = 0; i < clusters.size(); i++)
            {
                if (clusters.get(i).id == selectedCluster)
                {
                    cluster = clusters.get(i);
                    break;
                }
            }
            Logger.debugColor("Selected cluster " + selectedCluster + " with " + cluster.buildings.size() + " buildings", Logger.BG_LIGHTBLUE);

            // Put all agents into selected cluster and compute assignment
            // cluster.agents.clear();
            cluster.agents = agents;
            if (cluster.agents.size() <= 0)
            {
                Logger.debugColor("No agents in cluster!!", Logger.BG_RED);
            }
            int ass[] = cluster.computeAgentBuildingAssignment();

            return SimpleProtocol.buildAssignmentMessage(agents, cluster.getFiryBuildings(), ass, true);
        } else if (Params.ASSIGNMENT_MODE == Params.DECENTRAL_DSA_ASSIGNMENT)
        {
            Logger.debugColor("DSA: Assigning " + agents.size() + " agents to " + targets.size() + " targets", Logger.BG_GREEN);
            // Compute utility matrix
            ArrayList<Object> targetsO = new ArrayList<Object>(targets.size());
            for (int i = 0; i < targets.size(); i++)
                targetsO.add((Object) targets.get(i));
            double U[][] = ArrayStuff.computeUtilityMatrix(agents, targetsO, world);
            if (U == null)
            {
                Logger.errC("ERROR: utility matrix is null!");
                return null;
            }

            ArrayList<EntityID> targetsID = new ArrayList<EntityID>(targets.size());
            for (int i = 0; i < targets.size(); i++)
                targetsID.add(targets.get(i).getID());
            return null;
        } else
        {
            Logger.errC("Unable to handle assignment mode " + Params.ASSIGNMENT_MODE);
            return null;
        }
    }
}
