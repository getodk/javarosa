package org.javarosa.demo.activity.formlist;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

public class JRDemoFormListView extends List {
	public final Command CMD_SETTINGS = new Command("Settings", Command.SCREEN, 5);
	public final Command CMD_ADD_USER = new Command("Add User", Command.SCREEN, 5);
	public final Command CMD_VIEW_SAVED = new Command("View Saved", Command.OK, 1);
	public final Command CMD_BACK = new Command("Back", Command.BACK, 1);
	
	public JRDemoFormListView (String title, Vector formNames, boolean admin) {
		super(title, List.IMPLICIT);
	
		for (int i = 0; i < formNames.size(); i++) {
			append((String)formNames.elementAt(i), null);
		}
		
		addCommand(CMD_BACK);
		addCommand(CMD_VIEW_SAVED);
		if (admin) {
			addCommand(CMD_SETTINGS);
			addCommand(CMD_ADD_USER);
		}
	}
	
}
