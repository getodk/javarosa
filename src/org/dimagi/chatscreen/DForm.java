package org.dimagi.chatscreen;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import org.dimagi.view.Component;
import org.dimagi.view.IRefreshListener;

/**
 * A DForm acts as a full screen form, and serves as the basis for
 * a screen using DiMEC Controls. The DForm is responsible for receiving
 * low-level inputs, and propogating them to its internal components.
 * 
 * @author ctsims
 * @date Aug-09-2007
 *
 */
public class DForm extends Canvas implements IRefreshListener{

	private Component canvasComponent;
	
	/**
	 * Creates a new DForm.
	 */
	public DForm() {
		canvasComponent = new Component();
		canvasComponent.addRefreshListener(this);
		canvasComponent.setWidth(this.getWidth());
		canvasComponent.setHeight(this.getHeight());
	}
	
	/**
	 * This is the actual low-level input event from the 
	 * device.
	 */
	protected void pointerPressed(int x, int y) {
		canvasComponent.pointerPressed(x,y);
	}
	
	protected void keyPressed(int keyCode) {
		canvasComponent.keyPressed(keyCode);		
	}
	
	public void refresh() {
		repaint();
	}

	/**
	 * This paint method is the root drawing event for all components
	 * in the form. 
	 * @param g The low-level graphics canvas
	 */
	protected void paint(Graphics g) {
		canvasComponent.draw(g);
	}
	
	/**
	 * Returns the internal component for this DForm.
	 * This component is where all controls for the form should be added
	 * @return This DForm's component
	 */
	protected Component getContentComponent() {
		return canvasComponent;
	}
}
