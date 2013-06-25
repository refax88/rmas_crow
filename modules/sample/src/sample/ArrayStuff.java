package sample;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import rescuecore2.log.Logger;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class ArrayStuff
{

    /*
     * Transpose Matrix
     */
    final static public double[][] transpose(double[][] matrix)
    {
        double oldM[][] = new double[matrix.length][matrix[0].length];
        double newM[][] = new double[matrix[0].length][matrix.length];
        // Copy matrix
        for (int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix[0].length; j++)
            {
                oldM[i][j] = matrix[i][j];
            }
        }
        for (int i = 0; i < oldM.length; i++)
        {
            for (int j = 0; j < oldM[0].length; j++)
            {
                newM[j][i] = oldM[i][j];
            }
        }
        return newM;
    }

    /*
     * Transform a utility matrix to a cost matrix.
     */
    final static public double[][] transformUtilityToCost(double[][] matrix)
    {
        double oldM[][] = new double[matrix.length][matrix[0].length];
        double newM[][] = new double[matrix.length][matrix[0].length];
        // Copy matrix
        double minVal = Double.MAX_VALUE;
        double maxVal = Double.MIN_VALUE;
        for (int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix[0].length; j++)
            {
                if (matrix[i][j] < minVal)
                    minVal = matrix[i][j];
                if (matrix[i][j] > maxVal)
                    maxVal = matrix[i][j];
                oldM[i][j] = matrix[i][j];
            }
        }
        for (int i = 0; i < oldM.length; i++)
        {
            for (int j = 0; j < oldM[0].length; j++)
            {
                newM[i][j] = maxVal - oldM[i][j];
            }
        }
        return newM;
    }

    /*
     * Make a square matrix and fill new elements with value.
     */
    public final static double[][] makeSquareMatrix(double[][] matrix, double value)
    {
        int rows = matrix.length;
        int cols = matrix[0].length;
        if (rows == cols)
            return matrix;
        int N = cols;
        if (rows > cols)
        {
            N = rows;
        }
        double newM[][] = new double[N][N];
        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < N; j++)
            {
                newM[i][j] = value;
            }
        }

        // Copy matrix
        for (int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix[0].length; j++)
            {
                newM[i][j] = matrix[i][j];
            }
        }
        return newM;
    }

    /*
     * Print out a matrix of Doubles.
     */
    public final static void printMatrixDouble(double[][] A)
    {
        Locale.setDefault(Locale.US);
        System.out.format("Matrix of size rows=%d cols=%d: \n", A.length, A[0].length);
        for (int i = 0; i < A.length; i++)
        {
            for (int j = 0; j < A[0].length; j++)
            {
                if (A[i][j] == Double.MAX_VALUE)
                    System.out.format("inf ");
                else if (A[i][j] == -Double.MAX_VALUE)
                    System.out.format("-inf ");
                else
                    System.out.format("%1.2f ", A[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    /*
     * Print out a matrix of Integers.
     */
    public final static void printMatrixInt(int[][] A)
    {
        System.out.format("Matrix of size rows=%d cols=%d: \n", A.length, A[0].length);
        for (int i = 0; i < A.length; i++)
        {
            for (int j = 0; j < A[0].length; j++)
            {
                if (A[i][j] == Integer.MAX_VALUE)
                    System.out.format("inf ");
                else if (A[i][j] == -Integer.MAX_VALUE)
                    System.out.format("-inf ");
                else
                    System.out.format("%d ", A[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    // This function computes a utility matrix. The
    // targets can either be buildings or clusters
    public static double[][] computeUtilityMatrix(ArrayList<EntityID> agents, ArrayList<Object> targets, StandardWorldModel world)
    {
        if (agents.size() <= 0 || targets.size() <= 0)
            return null;

        double U[][] = new double[agents.size()][targets.size()];
        for (int i = 0; i < agents.size(); i++)
        {
            for (int j = 0; j < targets.size(); j++)
                U[i][j] = -1.0;
        }
        int i = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (Iterator<Object> it = targets.iterator(); it.hasNext();)
        {
            Object targetO = it.next();
            Iterator<EntityID> it2 = agents.iterator();
            int j = 0;
            while (it2.hasNext())
            {
                // Integer id = (Integer) (it2.next());
                EntityID agent = it2.next();

                // Compute distance to target
                double distance = Double.MAX_VALUE;

                if (targetO.getClass() == Building.class)
                {
                    Building target = (Building) targetO;
                    distance = world.getDistance(target.getID(), agent);
                } else if (targetO.getClass() == BuildingCluster.class)
                {
                    BuildingCluster cluster = (BuildingCluster) targetO;
                    distance = cluster.avgDist(agent);
                }

                // Compute utility of target
                double utility = 0.0;
                if (targetO.getClass() == Building.class)
                {
                    Building target = (Building) targetO;
                    double f = target.getFieryness();
                    // double u = target.getTotalArea();
                    utility = 1.0;
                    if (f == 1.0)
                        utility = 10000000.0;
                    else if (f == 2.0)
                        utility = 10000.0;
                    else if (f == 3.0)
                        utility = 1.0;
                } else if (targetO.getClass() == BuildingCluster.class)
                {
                    BuildingCluster c = (BuildingCluster) targetO;
                    utility = c.getUtility();
                }

                U[j][i] = utility / (distance * 0.001);

                if (U[j][i] < min)
                    min = U[j][i];
                if (U[j][i] > max)
                    max = U[j][i];
                j++;
            }
            i++;
        }

        // Normalize
        double divisor = max - min;
        if (divisor == 0)
            divisor = 1.0;
        for (i = 0; i < agents.size(); i++)
        {
            for (int j = 0; j < targets.size(); j++)
            {
                double val = (U[i][j] - min) / (divisor);
                U[i][j] = (1e-3 + val) * 10.0;
            }
        }
        return U;
    }

    public static int[] assignmentMatrixToAssignment(double[][] A, ArrayList<EntityID> agents, ArrayList<Object> targets, StandardWorldModel world)
    {
        // Assign agents to their targets
        int ass[] = new int[agents.size()];
        for (int i = 0; i < agents.size(); i++)
            ass[i] = -1;

        // Assign each agent to the best F1 building then to the best F2
        // building, etc.
        for (int a = 0; a < agents.size(); a++)
        {
            double max = -Double.MAX_VALUE;
            int target = -1;

            // First, only consider newly ignited targets
            if (Params.OPTIMIZE_ASSIGNMENT)
            {
                for (int t = 0; t < targets.size(); t++)
                {
                    Object o = targets.get(t);

                    if (o.getClass() == Building.class)
                    {
                        Building building = (Building) o;
                        double f = building.getFieryness();

                        if (f != 1)
                            continue;
                        else if (A[a][t] > max)
                        {
                            max = A[a][t];
                            target = t;
                        }
                    } else if (o.getClass() == BuildingCluster.class)
                    {
                        BuildingCluster cluster = (BuildingCluster) o;
                        // Compute cluster utility
                        double fireyness = 0.0;
                        int count = 0;
                        for (int i = 0; i < cluster.buildings.size(); i++)
                        {
                            Building b = cluster.buildings.get(i);
                            if (b.getFieryness() == 1)
                            {
                                fireyness = fireyness + b.getFieryness();
                                count++;
                            }
                        }
                        if (count == 0)
                            continue;

                        if (cluster.buildings.size() / count > 0.5)
                            continue;
                        else if (A[a][t] > max)
                        {
                            max = A[a][t];
                            target = t;
                        }
                    }
                }
            }

            // Second, consider all the others
            if (target == -1)
            {
                for (int t = 0; t < targets.size(); t++)
                {
                    if (A[a][t] > max)
                    {
                        max = A[a][t];
                        target = t;
                    }
                }
            }
            if (target >= 0)
            {
                ass[a] = target;
            } else
            {
                Logger.warnC("HAVE NO TARGET FOR AGENT " + a);
            }
        }
        return ass;
    }
}