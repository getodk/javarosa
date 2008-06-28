package org.javarosa.view.object;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import org.javarosa.chatscreen.Constants;
import org.javarosa.utils.ViewUtils;

import de.enough.polish.util.VectorIterator;

/**
 * Component is the base class of all non-Form UI elements in
 * the DiMEC toolkit.
 * 
 * Component maintins standard state data for rectangular controls,
 * an x,y location, width, height, visibility, and a background.
 * 
 * The class is reponsible for maintaining the internal container,
 * propogating drawing and input events to child components, and translating
 * locations relative to their placement.
 * 
 * The class is also responsible for identifying size changes, and refresh
 * calls from its children, and propogating those events back up to the highest
 * level (generally a DForm that can call for a re-paint).
 * 
 * Controls that are not Visible are not drawn by parent controls, and do not
 * receive input events.
 * 
 * If the Background Color of the control is ViewUtils.TRANSPARENT, no background
 * is drawn.
 * 
 * To see a breakdown of the seperation between components on the screen, 
 * ViewUtils.GUIDEBUG can be set to true.
 * 
 * @author ctsims
 * @date Aug-07-2007
 *
 */
public class Component implements IRefreshListener{

	protected Vector _components = new Vector();

	protected Vector _sizeChangedListeners = new Vector();
	
	protected Vector _refreshListeners = new Vector();

	protected int _x;

	protected int _y;

	protected int _width;

	protected int _height;
	
	protected String _text;

	protected boolean _visible = true;

	protected int _backgroundColor = ViewUtils.TRANSPARENT;

	/**
	 * Adds a component to this control. 
	 * The inputted component will be drawn along with this one, 
	 * and will receive all input events that fall within the appropriate
	 * place.
	 *  
	 * @param c the component to be added.
	 */
	public void add(Component c) {
		_components.addElement(c);
		c.addRefreshListener(this);
	}
	
	public void remove(Component c) {
		_components.removeElement(c);
	}

	/**
	 * @param x The x component of the new location
	 */
	public void setX(int x) {
		_x = x;
	}

	/**
	 * @param y The y component of the new location
	 */
	public void setY(int y) {
		_y = y;
	}
	
	/**
	 * Sets the Text of this widget. This text is not displayed in a standardized
	 * fashion, but is common to most controls, and is given here for convenience. 
	 * 
	 * @param text The text element of this component
	 */
	public void setText(String text) {
		_text = text;
		refresh();
	}

	/**
	 * Sets the width of this control.
	 *  
	 * @param width the new width
	 */
	public void setWidth(int width) {
		_width = width;
		fireSizeChangedListeners();
		refresh();
	}

	/**
	 * Sets the height of this control.
	 * 
	 * @param height the new height
	 */
	public void setHeight(int height) {
		_height = height;
		fireSizeChangedListeners();
		refresh();
	}

	/**
	 * Sets the visibilty of this control.
	 * 
	 * @param visible true if the control will be visible, false otherwise
	 */
	public void setVisible(boolean visible) {
		_visible = visible;
	}

	/**
	 * @param backgroundColor The new color of the widget's background. 
	 */
	public void setBackgroundColor(int backgroundColor) {
		_backgroundColor = backgroundColor;
	}

	/**
	 * @return The width of this control
	 */
	public int getWidth() {
		return _width;
	}

	/**
	 * @return The height of this control
	 */
	public int getHeight() {
		return _height;
	}

	/**
	 * @return The x component of this control's location
	 */
	public int getX() {
		return _x;
	}

	/**
	 * @return The y component of this control's location
	 */
	public int getY() {
		return _y;
	}
	
	/** 
	 * @return The control's text
	 */
	public String getText() {
		return _text;
	}

	/** 
	 * @return The control's background color
	 */
	public int getBackgroundColor() {
		return _backgroundColor;
	}

	/**
	 * @return True if the control is visible, false otherwise.
	 */
	public boolean isVisible() {
		return _visible;
	}

	/**
	 * Adds a listener to changes in the control's size
	 * 
	 * @param l The Size Change event Listener
	 */
	public void addSizeChangeListener(ISizeChangeListener l) {
		_sizeChangedListeners.addElement(l);
	}

	/**
	 * Fires an event to signify a resize of this control.
	 */
	private void fireSizeChangedListeners() {
		VectorIterator iter = new VectorIterator(_sizeChangedListeners);
		while (iter.hasNext()) {
			ISizeChangeListener listener = (ISizeChangeListener) iter.next();

			listener.sizeChanged();

		}
	}

	/**
	 * Removes a listener to changes in the control's size
	 * 
	 * @param l The Size Change event Listener
	 */
	public void removeSizeChangeListener(ISizeChangeListener l) {
		_sizeChangedListeners.removeElement(l);
	}
	
	/**
	 * Adds a listener for refresh events
	 * @param l The Refresh Listener
	 */
	public void addRefreshListener(IRefreshListener l) {
		_refreshListeners.addElement(l);
	}

	/**
	 * Fires an event to signify a Refresh of this control.
	 */
	private void fireRefreshListeners() {
		VectorIterator iter = new VectorIterator(_refreshListeners);
		while (iter.hasNext()) {
			IRefreshListener listener = (IRefreshListener) iter.next();
			listener.refresh();
		}
	}

	/**
	 * Removes a listener for refresh events
	 * @param l The Refresh Listener
	 */
	public void removeRefreshListener(IRefreshListener l) {
		_refreshListeners.removeElement(l);
	}


	/**
	 * If this control is visible:
	 * Translates the graphics context to start at the position
	 * of this component. 
	 * Draws a background over the rectangular space 
	 * defined by the dimensions of this component.
	 * Calls for this method's internal drawing methods.
	 * Iterates through each child component, and calls for their
	 * drawing events (translated by the location of this control).
	 *  
	 * @param g
	 */
	public void draw(Graphics g) {
		if (_visible) {
			g.translate(_x, _y);
			
			if (_backgroundColor != ViewUtils.TRANSPARENT) {
				g.setColor(_backgroundColor);
				g.fillRect(0, 0, _width, _height);
			}
			drawInternal(g);
			VectorIterator iter = new VectorIterator(_components);
			while (iter.hasNext()) {
				Component component = (Component) iter.next();
				component.draw(g);
			}
			
			if(Constants.GUIDEBUG) {
				g.setColor(ViewUtils.PINK_GREY);
				//g.drawRect(0, 0, _width, _height);
			}
			g.translate(-_x, -_y);
		}
	}

	/**
	 * Draws custom graphics for this control. Intended to be overriden
	 * by subclasses that have custom drawing methods
	 * @param g The graphics context
	 */
	protected void drawInternal(Graphics g) {
	}

	/**
	 * Fires a refresh event.
	 */
	public void refresh() {
		fireRefreshListeners();
	}
	
	/**
	 * Inherits a pointerPress event, and propogates it to any visibile child
	 * whose boundaries contain the position of the pointer press event.
	 * 
	 * Note, any subclass inheritting this method with by default stop this propogation,
	 * and this version must be called if propogation is still desired.
	 * 
	 * @param x the x component of the press position
	 * @param y the y component of the press position
	 */
	public void pointerPressed(int x, int y) {
		VectorIterator iter = new VectorIterator(_components);
		while (iter.hasNext()) {
			Component component = (Component) iter.next();
			if(component.isVisible() && ViewUtils.checkPointInRectangle(x,y, component.getX(), 
					component.getY(), component.getWidth(), component.getHeight())) {
				//We are going to need to manually propogate and translate the event
				component.pointerPressed(x - component.getX(), y - component.getY());
			}
		}
	}
	
	public void keyPressed(int keyCode) {
		VectorIterator iter = new VectorIterator(_components);
		while (iter.hasNext()) {
			Component component = (Component) iter.next();
				component.keyPressed(keyCode);
			}
		}
	
}
