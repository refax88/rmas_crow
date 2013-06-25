package RSLBench.AAMAS12;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import rescuecore2.log.Logger;
import rescuecore2.worldmodel.EntityID;
import RSLBench.Params;
import RSLBench.Assignment.Assignment;
import RSLBench.Assignment.DecentralAssignment;
import RSLBench.Comm.AbstractMessage;
import RSLBench.Comm.AssignmentMessage;
import RSLBench.Helpers.SimpleID;
import RSLBench.Helpers.UtilityMatrix;


public class DSA implements DecentralAssignment
{
    private static final boolean DEBUG_DSA = false;
    
    private UtilityMatrix _utilityM = null;
    private EntityID _agentID;
    private EntityID _targetID;
    private Collection<AbstractMessage> _neighborAssignments = null;
    private TargetScores _targetScores = null;
    private static Random _random;
    private int _ccc = 0;

    @Override
    public void initialize(EntityID agentID, UtilityMatrix utilityM)
    {
        _agentID = agentID;
        _utilityM = utilityM;
        _targetScores = new TargetScores();
        _targetID = Assignment.UNKNOWN_TARGET_ID;

    	if (DEBUG_DSA)
        	Logger.debugColor("A [" + SimpleID.conv(agentID) +"] initializing with "+ _utilityM.getNumTargets() + " targets.", Logger.BG_LIGHTBLUE);
                
        // Find the target with the highest utility and initialize required agents for each target 
        double bestTargetUtility = 0;
        for (EntityID t : _utilityM.getTargets()) {
            _targetScores.initializeTarget(t, _utilityM.getRequiredAgentCount(t));            
            double util = _utilityM.getUtility(agentID, t);
            if (bestTargetUtility < util) {
                bestTargetUtility = util;
                _targetID = t;
            }
        }
        if (DSA._random == null) {
            _random = new Random();
        }
    	if (DEBUG_DSA)
        	Logger.debugColor("A [" + SimpleID.conv(agentID) +"] init done!", Logger.BG_LIGHTBLUE);
    }

    @Override
    public boolean improveAssignment()
    {
        if (DEBUG_DSA)
            Logger.debugColor("["+ SimpleID.conv(_agentID) +"] improveAssignment", Logger.BG_LIGHTBLUE);        
        if (DEBUG_DSA)
            Logger.debugColor("[" + SimpleID.conv(_agentID) +"]  received neighbor messages: " + 
            			      _neighborAssignments.size(), Logger.BG_LIGHTBLUE);

        _targetScores.resetAssignments();        
        for (AbstractMessage message: _neighborAssignments) {
        	if (message.getClass() == AssignmentMessage.class) {        		
        		_targetScores.increaseAgentCount(((AssignmentMessage)message).getTargetID());
        	}
        }
        _neighborAssignments.clear();
        
        // Find the best target given utilities and constraints
        
        double bestScore = _targetScores.computeScore(_targetID, _utilityM.getUtility(_agentID, _targetID));         		
        EntityID bestTarget = _targetID;
        //Logger.debugColor("["+ _agentID +"]  BEFORE -> target: " + _targetID +" score: "+bestScore, Logger.BG_LIGHTBLUE);
        _ccc = 0;
        for (EntityID t : _utilityM.getTargets()) {
        	double score = _targetScores.computeScore(t, _utilityM.getUtility(_agentID, t));
            if (score > bestScore) {
                bestScore = score;
                bestTarget = t;
            }
            _ccc++;
        }        
        if (DEBUG_DSA)
            Logger.debugColor("["+ SimpleID.conv(_agentID) +"]  AFTER -> target: "+bestTarget.getValue() + 
            			      " score: "+ bestScore + " " + bestScore, Logger.BG_LIGHTBLUE);
        
        if (bestTarget != _targetID) {
            if (DEBUG_DSA) Logger.debugColor("[" + SimpleID.conv(_agentID) +"] assignment can be improved", Logger.BG_LIGHTBLUE);
            // improvement possible
            if (_random.nextDouble() <= Params.DSA_CHANGE_VALUE_PROBABILITY) {
                if (DEBUG_DSA) Logger.debugColor("["+ SimpleID.conv(_agentID) +"] assignment improved", Logger.BG_GREEN);
                // change target
                _targetID = bestTarget;
                return true;
            }
        }
        return false;
    }

    public int getCcc(){
        return _ccc;
    }
    @Override
    public EntityID getAgentID()
    {
        return _agentID;
    }

    @Override
    public EntityID getTargetID()
    {
        return _targetID;
    }

    @Override
    public Collection<AbstractMessage> sendMessages()
    {
    	Collection<AbstractMessage> messages = new ArrayList<AbstractMessage>();
    	messages.add(new AssignmentMessage(_agentID, _targetID));
        return messages;
    }

    @Override
    public void receiveMessages(Collection<AbstractMessage> messages)
    {
        _neighborAssignments = messages;
    }

    public void resetStructures() {}
}
