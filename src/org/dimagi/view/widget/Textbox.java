package org.dimagi.view.widget;


import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import org.dimagi.chatscreen.Constants;

import org.dimagi.utils.ViewUtils;
import org.dimagi.view.Widget;

public class Textbox extends Widget {

	int fontHeight;
	String str = "";
	
	/**
	 * Creates a new textbox widget
	 */
	public Textbox() {
	}
	
	/**
	 * Sets the height of the widget
	 */
	public void sizeWidget() {
		fontHeight = Font.getDefaultFont().getHeight();
		this.setHeight(fontHeight * 2);
	}
	
	public void sizeWidget(int width, int height) {
		this.setWidth(width);
		this.setHeight(height);
	}
	
	protected void drawInternal(Graphics g) {
		int xBufferSize = this.getWidth()/10;
		int yBufferSize = this.getHeight()/10;
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, yBufferSize, getWidth()-xBufferSize, fontHeight);
		g.drawString(str, 5, yBufferSize, g.TOP | g.LEFT);
	}
	
	public void keyPressed(int keyCode) {
		if (keyCode >= 0) {
			str += (char) keyCode;
			refresh();
		}
	}
	
}
