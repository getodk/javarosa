/**
 * 
 */
package org.javarosa.formmanager.api;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;

import org.javarosa.core.api.State;
import org.javarosa.formmanager.api.transitions.FormTransportStateTransitions;
import org.javarosa.formmanager.utility.FormSender;
import org.javarosa.formmanager.view.transport.FormTransportSubmitStatusScreen;
import org.javarosa.formmanager.view.transport.FormTransportViews;
import org.javarosa.formmanager.view.transport.TransportResponseProcessor;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.log.HandledItemStateListener;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportMessage;

/**
 * @author ctsims
 *
 */
public abstract class FormTransportState implements FormTransportStateTransitions, State, HandledCommandListener, HandledItemStateListener {
	//not separating out state/controller/etc, as form send is already kind of a mess
	
	protected FormTransportStateTransitions transitions;
	protected FormTransportSubmitStatusScreen screen;
	
	protected FormSender sender;

	public FormTransportState(TransportMessage message) {
		this(message, null);
	}
	
	public FormTransportState(TransportMessage message, TransportResponseProcessor responder) {
		FormTransportViews views = new FormTransportViews(this, this, responder);
		screen = views.getSubmitStatusScreen();
		sender = new FormSender(screen, message);
		this.transitions = this;
	}
	
	public void start() {
		sender.setObserver(screen);
		sender.sendData();
		J2MEDisplay.setView(screen);
	}

	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command c, Displayable d) {
		//The way this works now is crazy and needs to be rewritten when we have more time
		//For now, and command means done.
		transitions.done();
	}

	public void itemStateChanged(Item i) {
		CrashHandler.itemStateChanged(this, i);
	}  

	public void _itemStateChanged(Item i) {
		//The way this works now is crazy and needs to be rewritten when we have more time
		//For now, and command means done.
		transitions.done();
	}

}
