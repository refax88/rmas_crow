/*
 * This file is part of the TimeFinder project.
 * Visit http://www.timefinder.de for more information.
 * Copyright 2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample.solutions.hungarian;

import java.util.LinkedList;

/** 
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BruteForceAssignment implements AssignmentAlgorithm {

    private int[][] assignment;
    private int[][] minAssignment = null;
    private double minSum = Float.MAX_VALUE;
    private LinkedList<Integer> stack;
    private int MAX_DEPTH;
    private double floatMax;
    private double[][] costMatrix;

    public int[][] computeAssignments(double[][] costMatrixArg) {
        AssignmentArray matrix = new AssignmentArray();
        matrix.init(costMatrixArg);
        MAX_DEPTH = matrix.getMaxDepth();
        floatMax = matrix.getFloatMax();
        costMatrix = matrix.getCosts();

        assignment = new int[MAX_DEPTH][];
        stack = new LinkedList<Integer>();

        for (int i = 0; i < MAX_DEPTH; i++) {
            stack.add(i);
        }

        int sSize = stack.size();
        for (int tmpX = 0; tmpX < sSize; tmpX++) {
            int lastY = stack.removeFirst();
            if (this.costMatrix[lastY][0] < floatMax) {
                assignment[0] = new int[]{lastY, 0};
            } else {
                assignment[0] = null;
            }
            recursiveSearch(0);
            stack.addLast(lastY);
        }

        return minAssignment;
    }

    //PERFORMANCE use for-loop instead recursion
    private void recursiveSearch(int x) {
        x++;
        if (x == MAX_DEPTH) {
            double tmp = AssignmentHelper.calculateSum(costMatrix, assignment);
            if (minSum > tmp) {
                minSum = tmp;
                minAssignment = assignment.clone();
            }
            return;
        }
        int sSize = stack.size();
        for (int tmpX = 0; tmpX < sSize; tmpX++) {
            int lastY = stack.removeFirst();
            if (costMatrix[lastY][x] < floatMax) {
                assignment[x] = new int[]{lastY, x};
            } else {
                assignment[x] = null;
            }
            recursiveSearch(x);

            stack.addLast(lastY);
        }
    }
}
