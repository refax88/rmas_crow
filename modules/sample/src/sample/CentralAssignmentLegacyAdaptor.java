package sample;

public class CentralAssignmentLegacyAdaptor implements CentralAssignment
{
    private LegacyCentralAssignment solver;
    
    public CentralAssignmentLegacyAdaptor(LegacyCentralAssignment solver)
    {
        this.solver = solver;
    }

    @SuppressWarnings("deprecation")
    @Override
    public AgentAssignments compute(UtilityMatrix utility)
    {
        return AgentAssignments.fromMatrix(solver.compute(utility.getMatrix()));
    }

}
