package org.javarosa.demo.activity.selectlanguage;

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

public class JRDemoSelectLanguageController implements HandledCommandListener {

	JRDemoSelectLanguageTransitions transitions;
	JRDemoSelectLanguageView view;
	
	String[] languageList;
	
	public JRDemoSelectLanguageController () {
		languageList = JRDemoUtil.getLanguageList(); 
		view = new JRDemoSelectLanguageView("Choose Initial Language",languageList);
		view.setCommandListener(this);
	}
	
	public void setTransitions (JRDemoSelectLanguageTransitions transitions) {
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
				String key = view.getSelected();
				if(key != null) {
					transitions.languageSelected( key  );
				}
			}else if (c == view.CMD_EXIT) {
				transitions.exit();
			} 
		}
	} 
	
}
