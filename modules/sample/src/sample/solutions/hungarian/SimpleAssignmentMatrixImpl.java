/*
 * SimpleAssignmentMatrixImpl.java
 *
 * Created on 1. Dezember 2007, 21:50
 *
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
class SimpleAssignmentMatrixImpl implements AssignmentMatrix {

    private List<double[]> matrixWeight;
    private int rows;

    SimpleAssignmentMatrixImpl(int rowNumber) {
        rows = rowNumber;
        matrixWeight = new ArrayList<double[]>(rows);
    }

    SimpleAssignmentMatrixImpl(AssignmentMatrix matrix) {
        this(matrix.getRows());
        for (int ii = 0; ii < matrix.getColumns(); ii++) {
            matrixWeight.add(matrix.getColumn(ii));
        }
    }

    @Override
    public double get(int columnX, int rowY) {
        return matrixWeight.get(columnX)[rowY];
    }

    @Override
    public void addColumn(double[] column) {
        if (column.length == rows) {
            matrixWeight.add(column);
        } else {
            throw new IllegalArgumentException(
                    "Couldn't add a colunm with different rows size:" +
                    column.length + ". Should be:" + rows);
        }
    }

    @Override
    public void addColumn(int index, double[] column) {
        if (column.length == rows) {
            matrixWeight.add(index, column);
        } else {
            throw new IllegalArgumentException(
                    "Couldn't add a colunm with different rows size:" +
                    column.length + ". Should be:" + rows);
        }
    }

    @Override
    public double[] getColumn(int column) {
        return matrixWeight.get(column);
    }

    @Override
    public int getColumns() {
        return matrixWeight.size();
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public double createMaxEntry() {
        return Double.MAX_VALUE;
    }

    @Override
    public void removeColumn(int index) {
        matrixWeight.remove(index);
    }

    @Override
    public void clear() {
        matrixWeight.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int cols = getColumns();
        for (int r = 0; r < rows; r++) {
            for (int ti = 0; ti < cols; ti++) {
                sb.append(matrixWeight.get(ti)[r] + "\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
