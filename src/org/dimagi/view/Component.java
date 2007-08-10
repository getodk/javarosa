package org.dimagi.view;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import org.dimagi.chatscreen.Constants;
import org.dimagi.utils.ViewUtils;

import de.enough.polish.util.VectorIterator;

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

	public void add(Component c) {
		_components.addElement(c);
		c.addRefreshListener(this);
	}

	public void setX(int x) {
		_x = x;
	}

	public void setY(int y) {
		_y = y;
	}
	
	public void setText(String text) {
		_text = text;
		refresh();
	}

	public void setWidth(int width) {
		_width = width;
		refresh();
		fireSizeChangedListeners();
		refresh();
	}

	public void setHeight(int height) {
		_height = height;
		refresh();
		fireSizeChangedListeners();
		refresh();
	}

	public void setVisible(boolean visible) {
		_visible = visible;
	}

	public void resizeSelfOptimally() {

	}

	public void setBackgroundColor(int backgroundColor) {
		_backgroundColor = backgroundColor;
	}

	public int getWidth() {
		return _width;
	}

	public int getHeight() {
		return _height;
	}

	public int getX() {
		return _x;
	}

	public int getY() {
		return _y;
	}
	
	public String getText() {
		return _text;
	}

	public int getBackgroundColor() {
		return _backgroundColor;
	}

	public boolean isVisible() {
		return _visible;
	}

	public void addSizeChangeListener(ISizeChangeListener l) {
		_sizeChangedListeners.addElement(l);
	}

	private void fireSizeChangedListeners() {
		VectorIterator iter = new VectorIterator(_sizeChangedListeners);
		while (iter.hasNext()) {
			ISizeChangeListener listener = (ISizeChangeListener) iter.next();

			listener.sizeChanged();

		}
	}

	public void removeSizeChangeListener(ISizeChangeListener l) {
		_sizeChangedListeners.removeElement(l);
	}
	public void addRefreshListener(IRefreshListener l) {
		_refreshListeners.addElement(l);
	}

	private void fireRefreshListeners() {
		VectorIterator iter = new VectorIterator(_refreshListeners);
		while (iter.hasNext()) {
			IRefreshListener listener = (IRefreshListener) iter.next();
			listener.refresh();
		}
	}

	public void removeRefreshListener(IRefreshListener l) {
		_refreshListeners.removeElement(l);
	}


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

	public void drawInternal(Graphics g) {
	}

	public void refresh() {
		fireRefreshListeners();
	}
	
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
}
