package org.dimagi.view;

import java.util.Vector;

public abstract class Widget extends Component {

	Vector _widgetListeners;
	
	public abstract void sizeWidget();
	
	public void addWidgetListener(IWidgetListener listener) {
		widgetListeners().addElement(listener);
	}
	
	protected void fireWidgetComplete() {
		for(int i = 0 ; i < widgetListeners().size() ; ++i ) {
			IWidgetListener listener = (IWidgetListener)widgetListeners().elementAt(i);
			listener.onWidgetComplete();
		}
	}
	
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
