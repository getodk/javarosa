package org.javarosa.demo.activity.savedformlist;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.demo.util.SavedFormListItem;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.view.J2MEDisplay;

public class JRDemoSavedFormListController implements HandledCommandListener {
	JRDemoSavedFormListTransitions transitions;
	JRDemoSavedFormListView view;

	OrderedHashtable formInfo;

	public JRDemoSavedFormListController() {
		formInfo = JRDemoUtil.getSavedFormList();

		Vector formNames = new Vector();
		for (Enumeration e = formInfo.elements(); e.hasMoreElements();){
			formNames.addElement(e.nextElement());
		}
		view = new JRDemoSavedFormListView(Localization.get("jrdemo.savedformlist.title"), formNames);
		view.setCommandListener(this);
	}

	public void setTransitions(JRDemoSavedFormListTransitions transitions) {
		this.transitions = transitions;
	}

	public void start() {
		J2MEDisplay.setView(view);
	}

	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  


	public void _commandAction(Command c, Displayable d) {
		if (d == view) {
			if (c == List.SELECT_COMMAND) {
				int index = view.getSelectedIndex();
				if (index != -1){
					SavedFormListItem listItem  = (SavedFormListItem) formInfo.elementAt(index);
					FormInstance formInstance = JRDemoUtil.getSavedFormInstance(listItem.getFormId(),listItem.getInstanceId());
					transitions.savedFormSelected(listItem.getFormId(),listItem.getInstanceId());
				}
			} else 	if (c == view.CMD_SEND_DATA) {
				int index = view.getSelectedIndex();
				if (index != -1){
					SavedFormListItem listItem  = (SavedFormListItem) formInfo.elementAt(index);
					FormInstance formInstance = JRDemoUtil.getSavedFormInstance(listItem.getFormId(),listItem.getInstanceId());
					if (formInstance!=null)
					{
						transitions.sendDataFormInstance(formInstance);
					}
				}
				
			}else 	if (c == view.CMD_BACK) {
				transitions.back();
			}  
		}
	}
}
