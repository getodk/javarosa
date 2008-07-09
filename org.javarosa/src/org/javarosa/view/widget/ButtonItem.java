
package org.javarosa.view.widget;

import de.enough.polish.ui.Style;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class ButtonItem extends de.enough.polish.ui.CustomItem {
    public static final int KEY_UP = -1;
    public static final int KEY_DOWN = -2;
    public static final int KEY_SELECT = -5;

    private String message;

    public ButtonItem(String title) {
        super(title);
    }

    public ButtonItem(String title,Style style) {
        super(title,style);
    }


    // CustomItem abstract methods.

    public int getMinContentWidth() {
        return 25;
    }
    public int getMinContentHeight() { return 25; }

    public int getPrefContentWidth(int width) {
        Font msgFont = Font.getDefaultFont();
        width =  msgFont.stringWidth("Ok") + 10;
        return width;
    }

    public int getPrefContentHeight(int height) {
        Font msgFont = Font.getDefaultFont();
        height =  msgFont.getHeight() + 8 ;
        return height;
        //return getMinContentHeight()
    }

    public void paint(Graphics g, int w, int h) {
        g.setColor(135,206,250);
        g.fillRect(0, 0, w, h);
        g.setColor(0,0,0);
        g.drawRect(0, 0, w - 1, h - 1);
        //if(this.message != null){
        //g.drawString(this.message,5,2,Graphics.TOP|Graphics.LEFT);
        g.drawString("Ok",5,2,Graphics.TOP|Graphics.LEFT);
        //}
    }


    public boolean handleKeyPressed(int keyCode,int gameAction) {
        if(keyCode == KEY_SELECT) {
            notifyStateChanged();
            return true;
        }
        return false;
    }

//    public void setText(String message) {
//        this.message = message;
//        repaint();
//    }
}
