package org.javarosa.demo.activity.formlist;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.demo.applogic.JRDemoContext;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.view.J2MEDisplay;

public class JRDemoFormListController implements HandledCommandListener {

	JRDemoFormListTransitions transitions;
	JRDemoFormListView view;
	
	OrderedHashtable formInfo;
	
	public JRDemoFormListController () {
		formInfo = JRDemoUtil.getFormList();
		
		Vector formNames = new Vector();
		for (Enumeration e = formInfo.elements(); e.hasMoreElements(); )
			formNames.addElement(e.nextElement());
		
		view = new JRDemoFormListView("Choose Form", formNames, JRDemoContext._().getUser().isAdminUser());
		view.setCommandListener(this);
	}
	
	public void setTransitions (JRDemoFormListTransitions transitions) {
		this.transitions = transitions;
	}
		
	public void start () {
		J2MEDisplay.setView(view);
	}
	
	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command c, Displayable d) {
		if (d == view) {
			if (c == List.SELECT_COMMAND) {
				int index = view.getSelectedIndex();
				if (index != -1)
					transitions.formSelected(((Integer)formInfo.keyAt(index)).intValue());
			} else if (c == view.CMD_BACK) {
				transitions.back();
			} else if (c == view.CMD_SETTINGS) {
				transitions.settings();
			} else if (c == view.CMD_VIEW_SAVED) {
				transitions.viewSaved();
			} else if (c == view.CMD_ADD_USER) {
				transitions.addUser();
			}
		}
	}
}
