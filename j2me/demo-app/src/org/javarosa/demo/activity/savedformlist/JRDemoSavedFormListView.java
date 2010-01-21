package org.javarosa.demo.activity.savedformlist;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

public class JRDemoSavedFormListView extends List {

	public final Command CMD_BACK = new Command("Back", Command.BACK, 1);
	public final Command CMD_VIEW_SAVED = new Command("View Saved", Command.OK, 1);
	
	public JRDemoSavedFormListView (String title, Vector formNames) {
		super(title, List.IMPLICIT);
	
		for (int i = 0; i < formNames.size(); i++) {
			append((String)formNames.elementAt(i), null);
		}
		
		addCommand(CMD_BACK);
	}
}
