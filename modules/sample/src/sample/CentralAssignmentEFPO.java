package sample;

import rescuecore2.log.Logger;

public class CentralAssignmentEFPO implements LegacyCentralAssignment
{
    // Compute the assignment
    public double[][] compute(double[][] U)
    {
        if (U.length == 0 || U[0].length == 0)
        {
            Logger.errC("ERROR: Cannot compute Matrix. Input is zero!");
            return null;
        }

        Logger.warnC("WARNING: NOT computing EFPO assignment - dummy implementation.");
        
        return U;
    }
}
