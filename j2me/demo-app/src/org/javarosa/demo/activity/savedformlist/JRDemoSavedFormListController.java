package org.javarosa.demo.activity.savedformlist;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.j2me.view.J2MEDisplay;

public class JRDemoSavedFormListController implements CommandListener {
	JRDemoSavedFormListTransitions transitions;
	JRDemoSavedFormListView view;

	OrderedHashtable formInfo;

	public JRDemoSavedFormListController() {
		formInfo = JRDemoUtil.getSavedFormList();

		Vector formNames = new Vector();
		for (Enumeration e = formInfo.elements(); e.hasMoreElements();){
			formNames.addElement(e.nextElement());
		}
			

		view = new JRDemoSavedFormListView("Saved Forms", formNames);
		view.setCommandListener(this);
	}

	public void setTransitions(JRDemoSavedFormListTransitions transitions) {
		this.transitions = transitions;
	}

	public void start() {
		J2MEDisplay.setView(view);
	}

	public void commandAction(Command c, Displayable d) {
		if (d == view) {
			if (c == view.CMD_BACK) {
				transitions.back();
			} else if (c == view.CMD_VIEW_SAVED) {
				int index = view.getSelectedIndex();
				if (index != -1)
					transitions.savedFormSelected(((Integer)formInfo.keyAt(index)).intValue());
			} 
		}
	}
}
