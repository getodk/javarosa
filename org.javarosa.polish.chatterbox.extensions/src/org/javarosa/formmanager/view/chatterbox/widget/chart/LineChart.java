
package org.javarosa.formmanager.view.chatterbox.widget.chart;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import de.enough.polish.ui.CustomItem;
import de.enough.polish.ui.Style;

public class LineChart extends CustomItem {
    public Vector pointVector;
    public int yScaleFactor;
    public int xScaleFactor;
    public boolean isDrawAxis;
    public int colorFieldOne;
    public int colorFieldTwo;
    public int colorFieldThree;
    public int marginOne;
    public int marginTwo;
    public int marginThree;
    public int marginFour;
    public boolean isMarginTwoTrue;
    public boolean isMarginOneTrue;
    public boolean isLabelXTrue;
    public boolean isLabelYTrue;
    public int scaleCount;
    public int width;
    public int height; 
    public boolean isShadow;
    public int shadowColoOne;
    public int shadowColorTwo;
    public int shadowColorThree;
    public Font font;
    
    int colorOne;
    int colorTwo;
    int colorThree;

    private int ticker;
    private int equalCoordTicker;
    private int pointLabelWidth;
    private int pointLabelHeight;
    
    private int fontSpaceHeight;

    private boolean isDefaultColor;

    public LineChart(String label, Style style) {
    	super(label,style);
            ticker = 1;
            equalCoordTicker = 1;
            pointVector = new Vector();
            yScaleFactor = 100;
            xScaleFactor = 25;
            isDrawAxis = true;
            marginOne = 1;
            marginTwo = 1;
            marginThree = 5;
            marginFour = 1;
            isMarginTwoTrue = true;
            isMarginOneTrue = true;
            isLabelXTrue = true;
            isLabelYTrue = true;
            scaleCount = 0;
            isShadow = false;
            shadowColoOne = 100;
            shadowColorTwo = 100;
            shadowColorThree = 100;
            font = Font.getDefaultFont();
    }
    
    /**
     * Constructor : 
     * Creates a Point object
     */
    public LineChart(String label) {
        this(label,null);
    }

    /**
     * Make shadow visible for lines.
     * Param : boolean flag
     * Return : 
     */
    public void makeShadowVisible(boolean flag) {
        isShadow = flag;
    }

    /**
     * Resets the Points vector.
     * Param : 
     * Return : 
     */
    public void resetData() {
        pointVector = new Vector();
    }
    
    /**
     * Inserts the Point object into point vector
     * Param : 
     *          String pointLabel :- labelForPoint
     *          int xCordValue : x cordinate Value for the Point to be drawn
     *          int yCordValue : y cordinate Value for the Point to be drawn
     *          int colorOne, int colorTwo, int colorThree, : RGB Values Of color for the line
     * Return : 
     */
   
    public void insertItem(String pointLabel, int yCordValue, int xCordValue,  int colorOne, int colorTwo, int colorThree) {
        pointVector.addElement(new LinePointsItem(this, pointLabel, yCordValue, xCordValue, colorOne, colorTwo, colorThree));
        if(yCordValue > yScaleFactor) {
            yScaleFactor = yCordValue;
        }
        if(scaleCount > xScaleFactor) {
            xScaleFactor = scaleCount;
        }
        scaleCount++;
    }

    /**
     * Ued to draw the X and Y axis.
     * Param : 
     *          Graphics g1 :- Graphics Object
     *          int width : Width of axis.
     *          int height : height of axis
     * Return : 
     */
    public void drawAxis(Graphics g1, int width, int height) {
        if(isDrawAxis ) {
            g1.setColor(0, 0, 0);
            g1.drawLine(marginOne, this.height - marginFour, marginOne + width, this.height - marginFour);
            g1.drawLine(marginOne, this.height - marginFour, marginOne, this.height - marginFour - height);
           
        }
    }
    
    /**
     * This function is used to draw the labels on graph control 
     * Date - 16 April 2008
     **/
    private void drawAxisLabels(Graphics g, int width, int height) {
        
            g.setColor(0xB22222);
            g.drawString("Vert - Weight", marginOne - 15,(this.height - marginFour) + 15, 20);
            Font msgFont = Font.getDefaultFont();
            int LabelOneWidth =  msgFont.stringWidth("Vert - Weight");
            g.drawString("Hori - Age", (marginOne - 15) + LabelOneWidth,(this.height - marginFour) + 15, 20);
          
    }

    /**
     *  Inherited abstract method.
     */
    public int getMinContentHeight() {
        return 10;
    }

    /**
     *  Inherited abstract method.
     */
    public int getMinContentWidth() {
        return 10;
    }

    /**
     *  Inherited abstract method.
     */
    public int getPrefContentHeight(int i1) {
        return height;
    }

    /**
     *  Inherited abstract method.
     */
    public int getPrefContentWidth(int i1) {
        return width;
    }
    
    /**
     * Used to decide whether the axis to be drawn.
     * Param : 
     *          boolean isDrawAxis : 
     * Return : 
     */
    public void setDrawAxis(boolean isDrawAxis) {
        this.isDrawAxis = isDrawAxis;
    }

    /**
     * Used to set color
     * Param : 
     *          int colorFieldOne, int colorFieldTwo, int colorFieldThree : RGB Values of Color.
     * Return : 
     */
    public void setColor(int colorFieldOne, int colorFieldTwo, int colorFieldThree) {
        this.colorFieldOne = colorFieldOne;
        this.colorFieldTwo = colorFieldTwo;
        this.colorFieldThree = colorFieldThree;
    }
    
    /**
     * Used to decide whether the color is to be defaultcolor
     * Param : 
     *          boolean isDefaultColor:
     * Return : 
     */
    public final void setUseDefaultColor(boolean isDefaultColor) {
        this.isDefaultColor = isDefaultColor;
    }

    /**
     * Draws the Graph on screen
     * Param : 
     *          Graphics g1 :- Graphics Object
     *          int width : 
     *          int height : 
     * Return : 
     */
    public final void paint(Graphics g, int width, int height) {
        Graphics g1 = g;
        marginAndFontHeight();
        int l = this.width - marginOne;
        int i1 = this.height - fontSpaceHeight;
//        // System.out.println("j ==> " + j + " k ==> " + k + " this.width ==> " + this.width + " this.height == " + this.height + " l = " + l + " i1 == " + i1);
        g.setFont(font);
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, this.width, this.height);
        drawChart(g, l, i1);
        return;
    }

    /**
     * Decides margin according to font size
     * Param : 
     * Return : 
     */
    private void marginAndFontHeight() {
        /*
         * Added to adjust width and height for Graph
         * Date : April 16, 2008
         */
        pointLabelWidth = font.stringWidth(String.valueOf(yScaleFactor)) + 5;
        pointLabelHeight = font.getHeight() + 10;
        /*
         * Addition ends here.
         */ 
        if(isMarginOneTrue && pointLabelWidth > marginOne - 3) {
            marginOne = pointLabelWidth + 3;
        }
        if(isMarginTwoTrue && pointLabelHeight > fontSpaceHeight - 3) {
            fontSpaceHeight = pointLabelHeight + 3;
        }
    }

    /**
     * Draws the Chart
     * Param : 
     *          Graphics g1 :- Graphics Object
     *          int width : 
     *          int height : 
     * Return : 
     */
    
   public final void drawChart(Graphics g, int width, int height) {
        marginAndFontHeight();
        drawAxis(g, width, height);
        // Draws the labels on X and Y axis
        drawAxisLabels(g, width, height);
        if(isMarginOneTrue) {
            g.setColor(0, 0, 0);
            boolean isDrawPoint = true;
            for(int i = 0; i <= height; i += 10) {
                int j1 = this.height - fontSpaceHeight - i;
                g.drawLine(marginOne, j1, marginOne - 2, j1);
                int point = (yScaleFactor * i) / height;
                pointLabelWidth = font.stringWidth(String.valueOf(point));
                
                int pointXPosition = marginOne - 3 - pointLabelWidth;
                int pointYPosition = j1 - pointLabelHeight / 2;
                if(isDrawPoint & isLabelYTrue) {
                    g.drawString(String.valueOf(point), pointXPosition,  pointYPosition, 20);
                }
                isDrawPoint = !isDrawPoint;
            }
        }

        pointVector.size();
        int count = -1;
        int pointX1 = marginOne;
        int pointY1 = this.height - marginFour;
        Enumeration enumeration = pointVector.elements();
        int size = pointVector.size();
        int scaleFactor = 0;
        if(size <= 4 ) {
            scaleFactor = size;
        } else if (size <= 10) {
            scaleFactor = 6;
        }
        
        do {
            if(size > 10) {
                break;
            }
            if(!enumeration.hasMoreElements()) {
                break;
            }
            count++;
            LinePointsItem b1 = (LinePointsItem)enumeration.nextElement();
            
            if(isDefaultColor) {
                g.setColor(colorFieldOne, colorFieldTwo, colorFieldThree);
            } else {
                g.setColor(b1.getColorOne(), b1.getColorTwo(), b1.getColorThree());
            }
            
            int pointY2 = this.height - fontSpaceHeight - (height * b1.yCordPt) / (yScaleFactor);
            int pointX2 = marginOne + (width * b1.xCordPt) / (xScaleFactor * scaleFactor);
            // System.out.println("pointX1 ==  " + pointX1 + " pointY1 == " + pointY1 + " pointX2 == " + pointX2 + " pointY2 == " + pointY2);
            
            if(count > -1) {
                g.drawLine(pointX1, pointY1, pointX2, pointY2);
            }
            pointX1 = pointX2;
            pointY1 = pointY2;
            if(isMarginTwoTrue) {
                g.setColor(0, 0, 0);
                if((count + 1) % ticker == 0) {
                    //g.drawLine(pointX2, this.height - fontSpaceHeight, pointX2, (this.height - fontSpaceHeight) + 2);
                }
                pointLabelWidth = font.stringWidth(b1.labelX);
                
                if(isLabelXTrue && (count + 1) % (equalCoordTicker * ticker) == 0) {
                    g.drawString(b1.labelX, pointX2 - pointLabelWidth / 2, (this.height - fontSpaceHeight) + 1, 20);
                }
            }
        } while(true);
    }
     
    /**
     * Set Fonts
     * Param : 
     *          int i, int j, int k : 
     * Return : 
     */
    public void setFont(int i, int j, int k) {
        font = Font.getFont(i, j, k);
    }

    /**
     * Set shadow color
     * Param : 
     *          int i, int j, int k : 
     * Return : 
     */
    public void setShadowColor(int i, int j, int k) {
        shadowColoOne = i;
        shadowColorTwo = j;
        shadowColorThree = k;
    }

    /**
     * Set Margin
     * Param : 
     *          int i, int j, int k, int l : 
     * Return : 
     */
    public void setMargins(int i, int j, int k, int l) {
        marginOne = l;
        marginTwo = j;
        marginThree = i;
        marginFour = k;
    }
    
    /**
     * Set Preffered size of screen
     * Param : 
     *        int width, int height
     * Return : 
     */
    public void setPreferredSize(int width, int height) {
        super.setPreferredSize(width, height);
        this.width = width;
        this.height = height;
    }

    /**
     * Set MaxValue for the Y Coordinate scaling
     * Param : 
     *        int yScaleFactor
     * Return : 
     */
    public void setMaxYScaleFactor(int yScaleFactor) {
        this.yScaleFactor = yScaleFactor;
    }

    /**
     * Set Min Value for the Y Coordinate scaling
     * Param : 
     *        int yScaleFactor
     * Return : 
     */
    public void setMinYScaleFactor(int yScaleFactor) {
        this.yScaleFactor = yScaleFactor;
    }

    /**
     * Set MaxValue for the X Coordinate scaling
     * Param : 
     *        int xScaleFactor
     * Return : 
     */
    public void setMaxXScaleFactor(int xScaleFactor) {
        this.xScaleFactor = xScaleFactor;
    }

    /**
     * Set Min Value for the x Coordinate scaling
     * Param : 
     *        int xScaleFactor
     * Return : 
     */
    public void setMinXScaleFactor(int xScaleFactor) {
        this.xScaleFactor = xScaleFactor;
    }
    
}
