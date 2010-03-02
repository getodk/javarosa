/**
 * 
 */
package org.javarosa.formmanager.api;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.formmanager.api.transitions.CompletedFormOptionsTransitions;
import org.javarosa.formmanager.view.transport.SendNowSendLaterForm;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * @author ctsims
 *
 */
public class CompletedFormOptionsController implements CommandListener, ItemStateListener {
	CompletedFormOptionsTransitions transitions;
	FormInstance data;
	SendNowSendLaterForm view;
	
	public CompletedFormOptionsController(FormInstance formData) {
		this.data = formData;
		view = new SendNowSendLaterForm(this, this);
	}

	public void setTransitions (CompletedFormOptionsTransitions transitions) {
		this.transitions = transitions;
	}

	public void start() {
		J2MEDisplay.setView(view);
	}

	public void commandAction(Command c, Displayable arg1) {
		int choice = view.getCommandChoice();
		if(choice == SendNowSendLaterForm.SEND_NOW_DEFAULT) {
			transitions.sendData(data);
		} else if(choice == SendNowSendLaterForm.SEND_LATER) {
			transitions.skipSend(data);
		} else if(choice == SendNowSendLaterForm.SEND_NOW_SPEC) {
			transitions.sendToFreshLocation(data);
		}
	}

	public void itemStateChanged(Item arg0) {
		switch (view.getCommandChoice()) {
		case SendNowSendLaterForm.SEND_NOW_DEFAULT:
			transitions.sendData(data);
			break;
		case SendNowSendLaterForm.SEND_LATER:
			//Since caching and sending later are now part of the queue, we need
			//to let the transition have the data now. Possibly revisit this later.
			transitions.skipSend(data);
			break;
		case SendNowSendLaterForm.SEND_NOW_SPEC:
			transitions.sendToFreshLocation(data);
			break;
		}
	}
	
}
