package sample;


public interface LegacyCentralAssignment {
	
//	/**
//	 * Initialize the method with the list of agents and list of targets.
//	 * Targets can be either buildings or clusters.
//	 */
//	public void init(ArrayList<EntityID> agents, ArrayList<Object> targets);
	
	/**
	 * Takes as input a utility matrix and returns the assignment matrix.
	 * 
	 * @param U - utility matrix for each agent. The matrix is indexed by agents first and targets second.
	 * 
	 * @return An assignment matrix of the same dimensions as the utility matrix. 
	 * An agent is assigned to the target with maximum value.
	 * Normally 1 is chosen for an assigned target and 0 for all others.
	 */
	public double[][] compute(double[][] U);
	
	

}
