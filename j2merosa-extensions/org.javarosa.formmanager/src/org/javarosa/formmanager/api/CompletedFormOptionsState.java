/**
 * 
 */
package org.javarosa.formmanager.api;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.State;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.formmanager.api.transitions.CompletedFormOptionsStateTransitions;
import org.javarosa.formmanager.view.transport.SendNowSendLaterForm;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * @author ctsims
 *
 */
public class CompletedFormOptionsState implements State<CompletedFormOptionsStateTransitions>, CommandListener {
	CompletedFormOptionsStateTransitions transitions;
	DataModelTree data;
	SendNowSendLaterForm view;
	
	public CompletedFormOptionsState(DataModelTree formData) {
		this.data = formData;
		view = new SendNowSendLaterForm(this);
	}

	public void enter(CompletedFormOptionsStateTransitions transitions) {
		this.transitions = transitions;
	}

	public void start() {
		J2MEDisplay.setView(view);
	}

	public void commandAction(Command c, Displayable arg1) {
		if(c.equals(SendNowSendLaterForm.SEND_NOW_DEFAULT)) {
			transitions.sendData(data);
		} else if(c.equals(SendNowSendLaterForm.SEND_LATER)) {
			transitions.skipSend();
		} else if(c.equals(SendNowSendLaterForm.SEND_NOW_SPEC)) {
			transitions.sendToFreshLocation(data);
		}
	}

}
