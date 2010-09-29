package org.javarosa.demo.activity.selectlanguage;


import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

import org.javarosa.core.services.locale.Localization;

public class JRDemoSelectLanguageView extends List {
	public final Command CMD_EXIT = new Command(Localization.get("polish.command.exit"), Command.EXIT, 0);
	public final Command CMD_BACK = new Command(Localization.get("polish.command.back"), Command.BACK, 0);
	Hashtable<Integer,String> map;

	public JRDemoSelectLanguageView (String title, String[] languageList) {
		super(title, List.IMPLICIT);
		map = new Hashtable<Integer, String>();
		for (int i = 0; i < languageList.length; i++) {
			if(!languageList[i].equals("default")) {
				String display = languageList[i]; 
				try { display = Localization.get(languageList[i]); } catch(Exception e) {/* No translation */}
				map.put(new Integer(append(display,null)), languageList[i]);
			}
		}
		addCommand(CMD_EXIT);
		addCommand(CMD_BACK);
	}
	
	public String getSelected() {
		return map.get(new Integer(this.getSelectedIndex()));
	}
}
