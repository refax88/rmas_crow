/*
 * This file is part of the TimeFinder project.
 * Visit http://www.timefinder.de for more information.
 * Copyright 2008 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample.solutions.hungarian;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class AssignmentArray {

    private double[][] resultMatrix;
    private int maxDepth;
    // so that floatMax*n + 1 > floatMax*n, where n is the number of rows (or columns)
    private double floatMax;

    public int getMaxDepth() {        
        return maxDepth;
    }

    public double getFloatMax() {
        return floatMax;
    }

    public double[][] getCosts() {
        return resultMatrix;
    }

    /**
     * This Method initialized this matrix object. It adds rows or columns
     * if the costMatrixArg is not quadratic.
     */
    public void init(double[][] costMatrixArg) {
        //ROWS
        maxDepth = costMatrixArg.length;
        assert maxDepth > 0 : "Matrix should have at least one entry.";
        floatMax = AssignmentHelper.getFloatMax(Math.max(costMatrixArg.length, costMatrixArg[0].length));

        // handle the different cases if the matrix is not quadratic
        if (costMatrixArg[0].length < maxDepth) {
            // add columns
            resultMatrix = new double[maxDepth][maxDepth];
            for (int y = 0; y < maxDepth; y++) {
                for (int x = 0; x < costMatrixArg[0].length; x++) {
                    if (costMatrixArg[y][x] < floatMax) {
                        resultMatrix[y][x] = costMatrixArg[y][x];
                    } else {
                        resultMatrix[y][x] = floatMax;
                    }
                }
            }
            for (int y = 0; y < maxDepth; y++) {
                for (int x = costMatrixArg[0].length; x < maxDepth; x++) {
                    resultMatrix[y][x] = floatMax;
                }
            }
        } else if (costMatrixArg[0].length > maxDepth) {
            // add rows
            resultMatrix = new double[costMatrixArg[0].length][];
            for (int y = 0; y < maxDepth; y++) {
                resultMatrix[y] = costMatrixArg[y];
                for (int x = 0; x < costMatrixArg[0].length; x++) {
                    if (costMatrixArg[y][x] > floatMax) {
                        resultMatrix[y][x] = floatMax;
                    }
                }
            }
            for (int y = maxDepth; y < costMatrixArg[0].length; y++) {
                resultMatrix[y] = new double[costMatrixArg[0].length];
                for (int x = 0; x < costMatrixArg[0].length; x++) {
                    resultMatrix[y][x] = floatMax;
                }
            }
            maxDepth = costMatrixArg[0].length;
        } else {
            // change Float.MAX_VALUE to floatMax value
            resultMatrix = costMatrixArg;
            for (int y = 0; y < maxDepth; y++) {
                for (int x = 0; x < maxDepth; x++) {
                    if (costMatrixArg[y][x] < floatMax) {
                        resultMatrix[y][x] = costMatrixArg[y][x];
                    } else {
                        resultMatrix[y][x] = floatMax;
                    }
                }
            }
        }
    }
}
