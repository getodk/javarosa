package org.javarosa.formmanager.view.transport;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Spacer;

import org.javarosa.core.api.IView;
import org.javarosa.formmanager.utility.Terms;

public class SendNowSendLaterForm extends Form implements IView {
	private ChoiceGroup cg;

	public static final int SEND_NOW_DEFAULT = 0;
	public static final int SEND_LATER = 1;
	public static final int SEND_NOW_SPEC = 2;

	public SendNowSendLaterForm(CommandListener activity) {
		// #style submitPopup
		super(Terms.SUBMIT_FORM_STR);

		// #style submitYesNo
		this.cg = new ChoiceGroup(Terms.SEND_DATA_NOW_STR, Choice.EXCLUSIVE);

		// NOTE! These Indexes are optimized to be added in a certain
		// order. _DO NOT_ change it without updating the static values
		// for their numerical order.
		this.cg.append(Terms.SEND_NOW_STR, null);
		this.cg.append(Terms.SEND_LATER_STR, null);
		
		
		//this.cg.append(Terms.SEND_TO_NEW_SERVER, null);// clients wont need to
		// see
		// this

		append(this.cg);

		append(new Spacer(80, 0));

		setCommandListener(activity);
		setItemStateListener((ItemStateListener)activity);
	}

	public int getCommandChoice() {
		return this.cg.getSelectedIndex();
	}

	public Object getScreenObject() {
		return this;
	}

}
