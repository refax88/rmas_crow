package sample.solutions;

import rescuecore2.log.Logger;
import sample.ArrayStuff;
import sample.LegacyCentralAssignment;
import sample.solutions.hungarian.HungarianAlgorithm;

public class CentralAssignmentHungarian implements LegacyCentralAssignment
{
    private boolean DEBUG = false;

    // Compute the assignment
    public double[][] compute(double[][] U) {
        Logger.debugColor("Computing Hungarian Assignment ", Logger.BG_BLUE);
        if (U.length == 0 || U[0].length == 0) {
            Logger.errC("ERROR: Cannot compute Matrix. Input U is zero!");
            return null;
        }
        if (DEBUG) {
            System.out.println("Utility Matrix: ");
            ArrayStuff.printMatrixDouble(U);
        }
        // U = transpose(U);
        double Utrans[][] = ArrayStuff.makeSquareMatrix(U, 0.0);
        Utrans = ArrayStuff.transformUtilityToCost(Utrans);

        if (DEBUG) {
            System.out.println("Transformed Utility Matrix: ");
            ArrayStuff.printMatrixDouble(Utrans);
        }
        HungarianAlgorithm hungarian = new HungarianAlgorithm();
        int[][] AA = hungarian.computeAssignments(Utrans);

        if (DEBUG) {
            System.out.println("AA Matrix from Hungarian: ");
            ArrayStuff.printMatrixInt(AA);
            // FIXME: Something is off here, in simple examples, e.g.
            // 3 houses (end of run), 12 agents it should be
            // 3 agents with max utils get the 3 targets, rest nevermind
            // But the printouts/interpretations are the otherway around
            // i.e. agent -> target vs. target -> agent
        }

        // Convert to assignment matrix
        double[][] A = new double[U.length][U[0].length];
        for (int i = 0; i < U.length; i++) {
            for (int j = 0; j < U[0].length; j++) {
                A[i][j] = 0.0;
            }
        }
        // System.out.println("A:" + agents.size() + " T:" + targets.size() +
        // " Urows:" + U.length + " Ucols:" + U[0].length);
        System.out.println("Assignments:");
        for (int i = 0; i < AA.length; i++) {
//            int agent = AA[i][1];
//            int target = AA[i][0];
// FIXME: changing this around
            int agent = AA[i][0];
            int target = AA[i][1];

            System.out.format("agent %d --> target %d\n", agent, target);
            if (agent < A.length && target < A[agent].length)
                A[agent][target] = 1;
        }

        if (DEBUG) {
            System.out.println("Assignment Matrix: ");
            ArrayStuff.printMatrixDouble(A);
        }
        return A;
    }
}
