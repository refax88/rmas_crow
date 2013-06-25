package RSLBench.Assignment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import rescuecore2.config.Config;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import RSLBench.Params;
import RSLBench.Comm.ComSimulator;
import RSLBench.Comm.SimpleProtocolToServer;
import RSLBench.Helpers.Stats;
import RSLBench.Helpers.UtilityMatrix;

public class AssignmentSolver
{
    private String _assignmentSolverClassName = "";
    private String _logFileName = "no_logfile_name.dat";
    private AssignmentInterface _solver = null;;
    private ComSimulator _com = null;
    
    public AssignmentSolver(StandardWorldModel world, Config config)
    {
        // Initialize mandatory parameters
    	if (!Params.OVERWRITE_FROM_COMMANDLINE) {
    		Params.START_EXPERIMENT_TIME = config.getIntValue("experiment_start_time");               
    		Params.simulatedCommunicationRange = (int) config.getFloatValue("simulated_com_range") * 1000; // convert to mm         
    		Params.TRADE_OFF_FACTOR_TRAVEL_COST_AND_UTILITY = config.getFloatValue("trade_off_factor_travel_cost_and_utility");         		
    	}
		String basePackage = config.getValue("base_package");
		String className = config.getValue("assignment_class");
		String groupName = config.getValue("assignment_group");

        // Specific parameters
        Params.DSA_CHANGE_VALUE_PROBABILITY = config.getFloatValue("dsa_change_value_probability", 0.5);

        _assignmentSolverClassName = basePackage + "." + groupName + "." + className;
        _logFileName = "logs/" + basePackage + "_" + groupName + "_" + className + ".dat";
        System.out.println("Writing ouput to log: " +  _logFileName);
        
        // Initialize Assignment
        Logger.debugColor("Starting decentralized solver with com_range: " 
        		  + Params.simulatedCommunicationRange + " startTime: "  
        		  + Params.START_EXPERIMENT_TIME + " cost_trade_off: " 
        		  + Params.TRADE_OFF_FACTOR_TRAVEL_COST_AND_UTILITY, Logger.BG_GREEN);

        _com = new ComSimulator(Params.simulatedCommunicationRange);
        _solver = new DecentralizedAssignmentSimulator(_assignmentSolverClassName, _com);

        // Delete old log file
        File f1 = new File(_logFileName);
        f1.delete();
    }

    public byte[] act(int time, ArrayList<EntityID> agents, ArrayList<EntityID> targets, HashMap<EntityID, EntityID> agentLocations, StandardWorldModel world)
    {
    	if (world == null) {	
    		Logger.error("Got empty StandardWorldModel !!!");
    		System.exit(-1);
    	}

        // Write statistics header to file
        if (time == 7) {
            Stats.writeHeader(_logFileName);
        }

        // Check whether there is something to do at all
        if (targets.size() == 0 || agents.size() == 0) {
            Logger.debugColor("No agents or targets for assignment! targets=" + targets.size() + " agents:" + agents.size(), Logger.BG_YELLOW);
            return null;
        }

        // Initialize simulated communication
        if (_com != null) {
        	if(!_com.isInitialized()) {
        		_com.initialize(agents);
        	}
        	_com.update(agentLocations, world);
        }

        Logger.debugColor(_assignmentSolverClassName + ": assigning " + agents.size() + " agents to " + targets.size() + " targets", Logger.BG_GREEN);
        UtilityMatrix utility = new UtilityMatrix(agents, targets, agentLocations, world);

        Assignment A = _solver.compute(utility);
        if (A != null) {
        	// Count violated constraints
        	int violatedConstraints = 0;
        	for (EntityID a: agents) {
        		EntityID targetID = A.getAssignment(a);
        		violatedConstraints += Math.abs(A.getTargetSelectionCount(targetID) - utility.getRequiredAgentCount(targetID));         		
        	}
        	Stats.writeStatsToFile(_logFileName, time, world, violatedConstraints);
        	return SimpleProtocolToServer.buildAssignmentMessage(A, true);
        }
        else
        {
        	Stats.writeStatsToFile(_logFileName, time, world, -1);
        	return null;
        }
    }
}
