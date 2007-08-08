package org.dimagi.view;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import org.dimagi.utils.ViewUtils;

import de.enough.polish.util.VectorIterator;

public class Component {
	
	protected Vector _components = new Vector();
	
	protected Vector _sizeChangedListeners = new Vector();
	
	protected int _x;
	protected int _y;
	
	protected int _width;
	protected int _height;
	
	protected int _backgroundColor = ViewUtils.TRANSPARENT;
	
	public void add(Component c) {
		_components.addElement(c);
	}
	
	public void setX(int x) {
		_x = x;
	}
	
	public void setY(int y) {
		_y = y;
	}
	
	public void setWidth(int width) {
		_width = width;
		refresh();
		fireSizeChangedListeners();
	}
	
	public void setHeight(int height) {
		_height = height;
		refresh();
		fireSizeChangedListeners();
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
	
	public int getBackgroundColor() {
		return _backgroundColor;
	}
	
	public void addSizeChangeListener(ISizeChangeListener l) {
		_sizeChangedListeners.addElement(l);
	}
	
	private void fireSizeChangedListeners() {
		VectorIterator iter = new VectorIterator(_sizeChangedListeners);
		while(iter.hasNext()) {
			ISizeChangeListener listener = (ISizeChangeListener)iter.next();
			
			listener.sizeChanged();
			
		}
	}
	
	public void removeSizeChangeListener(ISizeChangeListener l) {
		_sizeChangedListeners.removeElement(l);
	}
	
	public void draw(Graphics g) {
		g.translate(_x,_y);	
		if(_backgroundColor != ViewUtils.TRANSPARENT){
			g.setColor(_backgroundColor);
			g.fillRect(0, 0, _width, _height);
		}	
		drawInternal(g);
		VectorIterator iter = new VectorIterator(_components);
		while(iter.hasNext()) {
			Component component = (Component)iter.next();
			
			component.draw(g);
		}
		g.translate(-_x, -_y);
	}
	
	public void drawInternal(Graphics g){}
	
	public void refresh() {}
}
