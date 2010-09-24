package org.javarosa.demo.activity.selectlanguage;


import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

public class JRDemoSelectLanguageView extends List {
	public final Command CMD_EXIT = new Command("Exit", Command.EXIT, 0);
	public final Command CMD_BACK = new Command("Back", Command.BACK, 0);	

	public JRDemoSelectLanguageView (String title, String[] languageList) {
		super(title, List.IMPLICIT);
		for (int i = 0; i < languageList.length; i++) {
			append(languageList[i],null);
		}
		addCommand(CMD_EXIT);
		addCommand(CMD_BACK);
	}
}
