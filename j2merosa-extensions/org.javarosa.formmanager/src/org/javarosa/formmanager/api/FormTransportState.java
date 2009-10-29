/**
 * 
 */
package org.javarosa.formmanager.api;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;

import org.javarosa.core.api.State;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.formmanager.api.transitions.FormTransportStateTransitions;
import org.javarosa.formmanager.utility.FormSender;
import org.javarosa.formmanager.view.transport.FormTransportSubmitStatusScreen;
import org.javarosa.formmanager.view.transport.FormTransportViews;
import org.javarosa.formmanager.view.transport.TransportResponseProcessor;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportMessage;

/**
 * @author ctsims
 *
 */
public abstract class FormTransportState implements FormTransportStateTransitions, State, CommandListener, ItemStateListener {
	//not separating out state/controller/etc, as form send is already kind of a mess
	
	FormTransportStateTransitions transitions;
	FormTransportSubmitStatusScreen screen;
	
	FormSender sender;

	public FormTransportState(TransportMessage message) {
		this(message, null);
	}
	
	public FormTransportState(TransportMessage message, TransportResponseProcessor responder) {
		FormTransportViews views = new FormTransportViews(this, this, responder);
		sender = new FormSender(views, message);
		sender.setMultiple(false);
		screen = views.getSubmitStatusScreen();
		this.transitions = this;
	}
	
	public void start() {
		sender.setObserver(screen);
		sender.sendData();
		J2MEDisplay.setView(screen);
	}

	public void commandAction(Command arg0, Displayable arg1) {
		//The way this works now is crazy and needs to be rewritten when we have more time
		//For now, and command means done.
		transitions.done();
	}

	public void itemStateChanged(Item arg0) {
		//The way this works now is crazy and needs to be rewritten when we have more time
		//For now, and command means done.
		transitions.done();
	}

}
