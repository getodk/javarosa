package org.dimagi.view;

import javax.microedition.lcdui.Graphics;

import org.dimagi.utils.ViewUtils;

public class DButton extends ActionComponent{
	
	public DButton(String text) {
		setText(text);
	}

	public void drawInternal(Graphics g) {
		//Finish by drawing the border	
		g.setColor(ViewUtils.BLACK);
		
		int textY = this.getHeight()/2 - g.getFont().getHeight()/2; 
		
		g.drawString(getText(),this.getWidth()/2,textY,Graphics.TOP |Graphics.HCENTER);
		
		//Finish by drawing the border
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, 0, _width, _height);
	}
	
	public void pointerPressed(int x, int y) {
		fireActionListeners();
	}
}
