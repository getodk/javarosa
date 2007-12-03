package org.dimagi.view;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import org.dimagi.chatscreen.Constants;

/**
 * Widgets are Frame elemnts that are used to capture input to answer
 * questions. Widgets accomplish this task in various and arbitrary ways.
 * 
 * Widgets are largely responsible for sizing themselves, at least vertically. 
 * In order to conserve space on the small screen, frames are sized to the minimum
 * possible height, which might be the minimum height of a widget. A default
 * width is available, but widgets are expected to identify their own height.
 * 
 * @author ctsims
 * @date Aug-09-2007
 *
 */
public abstract class Widget extends Component {

	Vector _widgetListeners;
	int _labelPosition;
	private boolean _activeWidget = true;
	private String _shortAnswer;
	
	/**
	 * Sets the size (height at least, width as well if applicable) of the widget
	 */
	public abstract void sizeWidget();

	public abstract void drawInactiveWidget(Graphics g);
	
	public abstract void drawActiveWidget(Graphics g); 
	
	/**
	 * Returns the label position
	 */
	public int getLabelPosition() {
		return _labelPosition;
	}
	
	/**
	 * Sets the label position
	 */
	public void setLabelPosition(int labelPosition) {
		_labelPosition = labelPosition;
	}

	public boolean isActiveWidget() {
		return _activeWidget;
	}
	
	public void setActiveWidget(boolean bool) {
		_activeWidget = bool;
		// if the widget is inactive, the label is always displayed on the left
		if (!_activeWidget) {
			setLabelPosition(Constants.LABEL_LEFT);
		}
	}
	
	public String getShortAnswer() {
		return _shortAnswer;
	}

	public void setShortAnswer(String ans) {
		_shortAnswer = ans;
	}
	
	/**
	 * Adds a listener to various widget events.
	 * 
	 * @param listener The Widget Listener
	 */
	public void addWidgetListener(IWidgetListener listener) {
		widgetListeners().addElement(listener);
	}
	
	/**
	 * Fires an event signaling that the widget is done accepting input.
	 */
	protected void fireWidgetComplete() {
		for(int i = 0 ; i < widgetListeners().size() ; ++i ) {
			IWidgetListener listener = (IWidgetListener)widgetListeners().elementAt(i);
			listener.onWidgetComplete();
		}
	}
	
	/**
	 * Adds a listener to various widget events.
	 * 
	 * @param listener The Widget Listener
	 */
	public void removeWidgetListener(IWidgetListener listener) {
		widgetListeners().removeElement(listener);
	}
	
	private Vector widgetListeners() {
		if(_widgetListeners ==null ) {
			_widgetListeners = new Vector();
		}
		return _widgetListeners;
	}
}
