package org.javarosa.view;

import java.util.Vector;

/**
 * ActionComponents are a standard set of components that have an action
 * that will be observed.
 * 
 * @author ctsims
 * @date Aug-07-2007
 */
public class ActionComponent extends Component {
	Vector listeners = new Vector();
	
	/**
	 * Adds a listener for this component's action
	 * 
	 * @param actionListener The listener
	 */
	public void addActionListener(IActionListener actionListener) {
		listeners.addElement(actionListener);
	}
	
	/**
	 * Removes a listener for this component's action
	 * 
	 * @param actionListener The listener
	 */
	public void removeActionListener(IActionListener actionListener) {
		listeners.removeElement(actionListener);
	}
	
	/**
	 * Fires the OnAction event for all of the listeners of this component.
	 */
	protected void fireActionListeners() {
		for(int i =0 ; i < listeners.size() ; ++i) {
			IActionListener theListener = (IActionListener)listeners.elementAt(i);
			theListener.OnAction();
		}
	}

}
