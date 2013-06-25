/*
 * AssignmentMatrix.java
 *
 * Created on 1. Dezember 2007, 14:27
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

/**
 * Assume a rectangle matrix m x n where n, m are at least one.
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public interface AssignmentMatrix {

    /**
     * This method returns the entry of column x and row y.
     */
    double get(int columnX, int rowY);

    /**
     * This method adds a new column.
     */
    void addColumn(double[] column);

    /**
     * This method adds a new column.
     */
    void addColumn(int index, double[] column);

    /**
     * This method removes column with specified index.
     */
    public void removeColumn(int index);

    /**
     * This method removes all columns.
     */
    public void clear();

    /**
     * This method returns the specified column.
     */
    double[] getColumn(int column);

    /**
     * @return the number of columns for this matrix.
     */
    int getColumns();

    /**
     * @return the number of rows for this matrix.
     */
    int getRows();

    /**
     * This method returns an entry, which can used to get the minimal
     * value of a row or column. So this entry has to be the largest possible one.
     */
    double createMaxEntry();
}
