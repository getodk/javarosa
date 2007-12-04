package org.dimagi.view;

import javax.microedition.lcdui.Graphics;

import org.dimagi.utils.ViewUtils;

/**
 * The DiMEC implementation of a simple button.
 * 
 * @author ctsims
 * @date Aug-08-2007
 * 
 */
public class DButton extends ActionComponent{
	
	/**
	 * Creates a new Button with the given text
	 * @param text The text to be displayed in the button
	 */
	public DButton(String text) {
		setText(text);
	}

	/**
	 * Draws the button.
	 */
	protected void drawInternal(Graphics g) {
		g.setColor(ViewUtils.BLACK);
		
		//Find the center location for the text
		int textY = this.getHeight()/2 - g.getFont().getHeight()/2; 
		
		g.drawString(getText(),this.getWidth()/2,textY,Graphics.TOP |Graphics.HCENTER);
		
		//Finish by drawing the border
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, 0, _width, _height);
	}
	
	/**
	 * Fires an ActionListener when pressed.
	 */
	public void pointerPressed(int x, int y) {
		fireActionListeners();
	}
}
