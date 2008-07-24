package org.javarosa.formmanager.view;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;

public class SubmitScreen extends Form {
	private ChoiceGroup cg;	
	public static final int SEND_NOW_DEFAULT = 0;
	public static final int SEND_LATER = 1;
	public static final int SEND_NOW_SPEC = 2;
	
	private static final String SEND_NOW_DEFAULT_STRING = "Send Now";
	private static final String SEND_NOW_SPEC_STRING = "Send to new server";
	private static final String SEND_LATER_STRING = "Send Later";
	
    //private Command selectCommand;
    
	//#style submitPopup
	public SubmitScreen () {
		super("Submit Form");
		
		//Command selectCommand = new Command("OK", Command.OK, 1);
		
		//#style submitYesNo
		cg = new ChoiceGroup("Send data now?", ChoiceGroup.EXCLUSIVE);
		
		//NOTE! These Indexes are optimized to be added in a certain
		//order. _DO NOT_ change it without updating the static values
		//for their numerical order.
		cg.append(SEND_NOW_DEFAULT_STRING, null);
		cg.append(SEND_LATER_STRING, null);
		cg.append(SEND_NOW_SPEC_STRING, null);
		cg.setSelectedIndex(0, true);
				
		append(cg);
		append(new Spacer(80, 0));
	}
	
	public int getCommandChoice() {
		return cg.getSelectedIndex();
	}
}
