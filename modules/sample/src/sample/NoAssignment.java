package sample;

import rescuecore2.log.Logger;

public class NoAssignment implements CentralAssignment {

    @Override
    public AgentAssignments compute(UtilityMatrix utility)
    {
        Logger.debugColor("No assignment computation!", Logger.FG_MAGENTA);
        return null;
    }

}
