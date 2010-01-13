/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package org.javarosa.formmanager.view.chatterbox.extendedwidget.chart;

public final class LinePointsItem
{

    public int yCordPt; // Point 1 
    public int xCordPt; // Point 2 
    private int colorOne;
    private int colorTwo;
    private int colorThree;
    public String labelX;
    private boolean colorField;

    public LinePointsItem(String labelX, int yCordPt, int xCordPt, int colorOne, int colorTwo, int colorThree) {
        this.xCordPt = xCordPt;
        this.yCordPt = yCordPt; 
        this.colorOne = colorOne;
        this.colorTwo = colorTwo;
        this.colorThree = colorThree;
        this.labelX = labelX;
        colorField = false;
        // System.out.println("LinePointsItem() --> xCordPt == > " + xCordPt + " yCordPt == " + yCordPt + " colorOne " + colorOne + " colorTwo " + colorTwo + " colorThree " + colorThree ) ;
    }
    
    public boolean isColorField() {
    	return colorField;
    }

    
    public final int getColorOne() {
        return colorOne;
    }

    public final int getColorTwo() {
        return colorTwo;
    }

    public final int getColorThree() {
        return colorThree;
    }
}
