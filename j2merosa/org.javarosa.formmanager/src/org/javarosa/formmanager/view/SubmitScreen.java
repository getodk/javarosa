package org.javarosa.formmanager.view;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;

import org.javarosa.core.api.IView;

//#if javarosa.usepolishlocalisation
import de.enough.polish.util.Locale;
//#endif

public class SubmitScreen extends Form implements IView {
	private ChoiceGroup cg;	
	public static final int SEND_NOW_DEFAULT = 0;
	public static final int SEND_LATER = 1;
	public static final int SEND_NOW_SPEC = 2;
	
	//#if javarosa.usepolishlocalisation
	private static final String SEND_NOW_DEFAULT_STRING = Locale.get("menu.SendNow");
	private static final String SEND_NOW_SPEC_STRING = Locale.get("menu.SendToNewServer");
	private static final String SEND_LATER_STRING = Locale.get("menu.SendLater");
	//#else
	private static final String SEND_NOW_DEFAULT_STRING = "Send Now";
	private static final String SEND_NOW_SPEC_STRING = "Send to new server";
	private static final String SEND_LATER_STRING = "Send Later";
	//#endif
	
    //private Command selectCommand;
    
	public SubmitScreen () {
		//#style submitPopup
		super("Submit Form");
		
		//Command selectCommand = new Command("OK", Command.OK, 1);
		
		//#if javarosa.usepolishlocalisation
		cg = new ChoiceGroup(Locale.get("question.SendNow"), ChoiceGroup.EXCLUSIVE);
		//#else
		//#style submitYesNo
		cg = new ChoiceGroup("Send data now?", ChoiceGroup.EXCLUSIVE);
		//#endif
		
		//NOTE! These Indexes are optimized to be added in a certain
		//order. _DO NOT_ change it without updating the static values
		//for their numerical order.
		cg.append(SEND_NOW_DEFAULT_STRING, null);
		cg.append(SEND_LATER_STRING, null);
		cg.append(SEND_NOW_SPEC_STRING, null);//clients wont need to see this
				
		append(cg);
		
		append(new Spacer(80, 0));
	}
	
	public int getCommandChoice() {
		return cg.getSelectedIndex();
	}
	public Object getScreenObject() {
		return this;
	}
	
}
