/*
 * VisibleArray.java
 *
 * Created on 18. NoveiArrayber 2007, 18:27
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
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class VisibleArray {

    private Object[][] iArray;

    public VisibleArray(int[][] array) {
        init(array.length, array[0].length);
        for (int x = 0; x < iArray.length; x++) {
            for (int y = 0; y < iArray[0].length; y++) {
                iArray[x][y] = array[x][y];
            }
        }
    }

    public VisibleArray(double[][] array) {
        init(array.length, array[0].length);
        for (int x = 0; x < iArray.length; x++) {
            for (int y = 0; y < iArray[0].length; y++) {
                iArray[x][y] = array[x][y];
            }
        }
    }

    public VisibleArray(Object[][] array) {
        init(array.length, array[0].length);
        for (int x = 0; x < iArray.length; x++) {
            for (int y = 0; y < iArray[0].length; y++) {
                iArray[x][y] = array[x][y];
            }
        }
    }

    private void init(int x, int y) {
        iArray = new Object[x][y];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("x:" + iArray[0].length + "\ty:" + iArray.length + "\n");
        for (int y = 0; y < iArray.length; y++) {
            if (iArray[y] == null) {
                sb.append("null\n");
                continue;
            }
            for (int x = 0; x < iArray[0].length; x++) {
                sb.append(iArray[y][x] + "\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
