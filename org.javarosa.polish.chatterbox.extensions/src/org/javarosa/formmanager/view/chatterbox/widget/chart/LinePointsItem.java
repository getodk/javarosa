
package org.javarosa.formmanager.view.chatterbox.widget.chart;

public final class LinePointsItem
{

    public int yCordPt; // Point 1 
    public int xCordPt; // Point 2 
    private int colorOne;
    private int colorTwo;
    private int colorThree;
    public String labelX;
    private boolean colorField;
    private final LineChart chartItem;

    public LinePointsItem(LineChart chartItem, String labelX, int yCordPt, int xCordPt, int colorOne, int colorTwo, int colorThree) {
        this.chartItem = chartItem;
        this.xCordPt = xCordPt;
        this.yCordPt = yCordPt; 
        this.colorOne = colorOne;
        this.colorTwo = colorTwo;
        this.colorThree = colorThree;
        this.labelX = labelX;
        colorField = false;
        // System.out.println("LinePointsItem() --> xCordPt == > " + xCordPt + " yCordPt == " + yCordPt + " colorOne " + colorOne + " colorTwo " + colorTwo + " colorThree " + colorThree ) ;
    }

    
    public final int getColorOne() {
        if(colorField) {
            return this.chartItem.colorOne;
        } else {
            return colorOne;
        }
    }

    public final int getColorTwo() {
        if(colorField) {
            return this.chartItem.colorTwo;
        } else {
            return colorTwo;
        }
    }

    public final int getColorThree() {
        if(colorField) {
            return this.chartItem.colorThree;
        } else {
            return colorThree;
        }
    }
}
