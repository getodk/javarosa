package org.javarosa.demo.activity.savedformlist;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.demo.util.SavedFormListItem;

public class JRDemoSavedFormListView extends List {

	public final Command CMD_BACK = new Command(Localization.get("jrdemo.savedformlist.command.back"), Command.BACK, 1);
	public final Command CMD_SEND_DATA = new Command(Localization.get("jrdemo.savedformlist.command.senddata"), Command.OK, 1);
	
	public JRDemoSavedFormListView (String title, Vector formNames) {
		super(title, List.IMPLICIT);
	
		for (int i = 0; i < formNames.size(); i++) {
			append( ((SavedFormListItem)formNames.elementAt(i) ).toString(), null);
		}
		
		addCommand(CMD_BACK);
		addCommand(CMD_SEND_DATA);
	}
}
