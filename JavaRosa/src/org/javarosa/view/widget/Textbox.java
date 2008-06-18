package org.javarosa.view.widget;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.javarosa.utils.ViewUtils;
import org.javarosa.view.Widget;

/**
* A custom widget for accepting free text input.
*  
* @author cdunn
* @date Jan-16-2008
*/
public class Textbox extends Widget {

	private int fontHeight;
	private String input = "";
	
	/**
	 * Creates a new custom textbox widget
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
	
	/**
	 * Sets the width and the height of the widget
	 *  
	 *  @param width	width of the widget
	 *  @param height	height of the widget 	
	 */	
	public void sizeWidget(int width, int height) {
		this.setWidth(width);
		this.setHeight(height);
	}
	
	/**
	 * Draws the widget in the inactive state
	 * 
	 * @param g 	Graphics context
	 */		
	public void drawInactiveWidget(Graphics g) {
		int xBufferSize = this.getWidth()/10;
		int yBufferSize = this.getHeight()/10;
		g.setColor(ViewUtils.BLACK);
		Font theFont = Font.getDefaultFont();
		g.drawString(input, getWidth()-20, yBufferSize, g.TOP | g.RIGHT);
	}

	/**
	 * Draws the widget in the active state
	 * 
 	 * @param g 	Graphics context
	 */		
	public void drawActiveWidget(Graphics g) {
		int xBufferSize = this.getWidth()/10;
		int yBufferSize = this.getHeight()/10;
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, yBufferSize, getWidth()-xBufferSize, fontHeight);
		g.drawString(input, 5, yBufferSize, g.TOP | g.LEFT);
	}
	
	/**
	 * Draws the widget
	 *
	 * @param g 	Graphics context
	 */			
	protected void drawInternal(Graphics g) {
		if ( this.isActiveWidget() ) {
			drawActiveWidget(g);
		} else {
			drawInactiveWidget(g);
		}
	}
	
	/**
	 * Process input from keyPress event.
	 * 
	 * @param keyCode	keyCode entered by the user
	 */
	public void keyPressed(int keyCode) {
		// without this if statement the keyPress event is propagated to all textbox widgets 
		if (this.isActiveWidget() ) {
			if (keyCode >= 0) {
				input += (char)keyCode;
			} else if (keyCode == -11) { // delete key
				input = input.substring(0, input.length()-1); // remove last character
			}
			refresh();
			this.setShortAnswer(input);
		}
	}
	
}
