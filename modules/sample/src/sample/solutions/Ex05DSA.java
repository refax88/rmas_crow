package sample.solutions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import rescuecore2.log.Logger;
import sample.AssignmentMessage;
import sample.DecentralAssignment;
import sample.Params;
import sample.UtilityMatrix;

public class Ex05DSA implements DecentralAssignment
{
    private UtilityMatrix utility;
    private int agentID;
    private int targetID;
    private Collection<AssignmentMessage> neighborAssignments;
    private ArrayList<TargetScore> targetScores;
    private static Random random;

    @Override
    public void initialize(int agentID, UtilityMatrix utility)
    {
        Logger.debugColor("["+agentID+"] initialize targets: "+utility.getTargetCount(), Logger.BG_LIGHTBLUE);
        this.agentID = agentID;
        this.utility = utility;
        targetScores = new ArrayList<TargetScore>();
        targetID = -1;
        double bestTargetUtility = 0;
        for(int index = 0; index < utility.getTargetCount(); index++)
        {
            targetScores.add(new TargetScore(utility.getRequiredAgentCount(index)));
            double currentUtility = utility.getUtility(agentID, index);
            if (bestTargetUtility < currentUtility)
            {
                bestTargetUtility = currentUtility;
                targetID = index;
            }
        }
        if (Ex05DSA.random == null)
        {
            random = new Random();
        }
    }

    @Override
    public boolean improveAssignment()
    {
        Logger.debugColor("["+agentID+"] improveAssignment", Logger.BG_LIGHTBLUE);
        for (TargetScore target: targetScores)
        {
            target.reset();
        }
        Logger.debugColor("["+agentID+"]  received neighbor messages: "+neighborAssignments.size(), Logger.BG_LIGHTBLUE);
        for (AssignmentMessage message: neighborAssignments)
        {
            TargetScore target = targetScores.get(message.getTargetID());
            target.assignAgent(utility.getUtility(message.getAgentID(), message.getTargetID()));
        }
        neighborAssignments.clear();
        
        double bestScore = targetScores.get(targetID).computeMyScore(utility.getUtility(agentID, targetID));
        int bestTargetID = targetID;
//        Logger.debugColor("["+agentID+"]  BEFORE -> target: "+targetID+" score: "+bestScore, Logger.BG_LIGHTBLUE);
        for (int index = 0; index < targetScores.size(); index++)
        {
            double currentScore = targetScores.get(index).computeMyScore(utility.getUtility(agentID, index));
            if (bestScore < currentScore)
            {
                bestScore = currentScore;
                bestTargetID = index;
            }
        }
        
        Logger.debugColor("["+agentID+"]  AFTER -> target: "+bestTargetID+" score: "+bestScore + " "+targetScores.get(bestTargetID), Logger.BG_LIGHTBLUE);
        
        if (bestTargetID != targetID)
        {
            Logger.debugColor("["+agentID+"] assignment can be improved", Logger.BG_LIGHTBLUE);
            // improvement possible
            if (random.nextDouble() <= Params.DSA_CHANGE_VALUE_PROBABILITY)
            {
                Logger.debugColor("["+agentID+"] assignment improved", Logger.BG_GREEN);
                // change target
                targetID = bestTargetID;
                return true;
            }
        }
        return false;
    }

    @Override
    public int getAgentID()
    {
        return agentID;
    }

    @Override
    public int getTargetID()
    {
        return targetID;
    }

    @Override
    public AssignmentMessage sendMessage()
    {
        return new AssignmentMessage(agentID, targetID);
    }
//  private double computeScore()
//  {
//      double totalUtility = 0;
//      if (! agentsUtility.isEmpty())
//      {
//          for (double utility: agentsUtility)
//          {
//              totalUtility += utility;
//          }
//          totalUtility *= Math.max(requiredAgents / agentsUtility.size(), 1);
//      }
//      return totalUtility;
//  }

    @Override
    public void receiveMessages(Collection<AssignmentMessage> messages)
    {
        neighborAssignments = messages;
    }

    private class TargetScore
    {
        private int requiredAgents;
        private LinkedList<Double> agentsUtility;
        
        public TargetScore(int requiredAgetns)
        {
//            this.requiredAgents = 1;
            this.requiredAgents = requiredAgetns;
            agentsUtility = new LinkedList<Double>();
        }
        
        public void assignAgent(double utility)
        {
            agentsUtility.add(utility);
        }
        
        public double computeMyScore(double myUtility)
        {
            if (agentsUtility.size() + 1 <= requiredAgents)
            {
                return myUtility;
            }
            else
            {
                return 0.0;
            }

//            if (agentsUtility.isEmpty())
//            {
//                return myUtility;
//            }
//            else
//            {
//                double utility = computeScore();
//                agentsUtility.add(myUtility);
//                double change = computeScore() - utility;
//                agentsUtility.removeLast();
//                return change;
//            }
        }
        
//        private double computeScore()
//        {
//            double totalUtility = 0;
//            if (! agentsUtility.isEmpty())
//            {
//                for (double utility: agentsUtility)
//                {
//                    totalUtility += utility;
//                }
//                totalUtility *= Math.max(requiredAgents / agentsUtility.size(), 1);
//            }
//            return totalUtility;
//        }
        
        public void reset()
        {
            agentsUtility.clear();
        }
        
        public String toString()
        {
            return "other agents on target: ("+agentsUtility.size()+"/"+requiredAgents+")";
        }
    }
}
