package org.javarosa.formmanager.view.transport;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Spacer;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IView;

public class SendNowSendLaterForm extends Form implements IView {
	private ChoiceGroup cg;

	public static final int SEND_NOW_DEFAULT = 0;
	public static final int SEND_LATER = 1;
	public static final int SEND_NOW_SPEC = 2;

	public SendNowSendLaterForm(CommandListener activity) {
		//#style submitPopup
		super(JavaRosaServiceProvider.instance().localize("sending.view.submit"));

		//#style submitYesNo
		this.cg = new ChoiceGroup(JavaRosaServiceProvider.instance().localize("sending.view.when"), Choice.EXCLUSIVE);

		// NOTE! These Indexes are optimized to be added in a certain
		// order. _DO NOT_ change it without updating the static values
		// for their numerical order.
		this.cg.append(JavaRosaServiceProvider.instance().localize("sending.view.now"), null);
		this.cg.append(JavaRosaServiceProvider.instance().localize("sending.view.later"), null);
		

		//TODO: Add this back in for admin users. Who took it out?
		//this.cg.append(JavaRosaServiceProvider.instance().localize("sending.view.new"), null);// clients wont need to
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
