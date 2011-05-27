package org.javarosa.demo.activity.remoteformlist;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.demo.applogic.JRDemoContext;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.view.J2MEDisplay;

public class JRDemoRemoteFormListController implements HandledCommandListener {

	JRDemoRemoteFormListTransitions transitions;
	JRDemoRemoteFormListView view;
	OrderedHashtable formList;
	
	public JRDemoRemoteFormListController(String formListXml) {
		formList = JRDemoUtil.readFormListXML(formListXml);
		view = new JRDemoRemoteFormListView(Localization.get("jrdemo.remoteformlist.title"), formList, JRDemoContext._().getUser().isAdminUser());
		view.setCommandListener(this);
	}
	
	public void setTransitions (JRDemoRemoteFormListTransitions transitions) {
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
				{
					String formUrl = (String) formList.elementAt(index);
					transitions.formDownload(formUrl);
				}
			} else if (c == view.CMD_BACK) {
				transitions.back();
			} 
		}
	}
}
