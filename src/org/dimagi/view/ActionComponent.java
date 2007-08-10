package org.dimagi.view;

import java.util.Vector;

public class ActionComponent extends Component {
	Vector listeners = new Vector();
	
	public void addActionListener(IActionListener actionListener) {
		listeners.addElement(actionListener);
	}
	
	public void removeActionListener(IActionListener actionListener) {
		listeners.removeElement(actionListener);
	}
	
	protected void fireActionListeners() {
		for(int i =0 ; i < listeners.size() ; ++i) {
			IActionListener theListener = (IActionListener)listeners.elementAt(i);
			theListener.OnAction();
		}
	}

}
