package org.dimagi.chatscreen;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import org.dimagi.view.Component;
import org.dimagi.view.IRefreshListener;

public class DForm extends Canvas implements IRefreshListener{

	private Component canvasComponent;
	
	public DForm() {
		canvasComponent = new Component();
		canvasComponent.addRefreshListener(this);
		canvasComponent.setWidth(this.getWidth());
		canvasComponent.setHeight(this.getHeight());
	}
	
	protected void pointerPressed(int x, int y) {
		canvasComponent.pointerPressed(x,y);
	}
	
	public void refresh() {
		
		repaint();
	}
	
	protected void paint(Graphics g) {
		canvasComponent.draw(g);
	}
	
	protected Component getContentComponent() {
		return canvasComponent;
	}
}
