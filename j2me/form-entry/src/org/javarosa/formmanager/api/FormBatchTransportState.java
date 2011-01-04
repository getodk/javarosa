/**
 * 
 */
package org.javarosa.formmanager.api;

import org.javarosa.core.api.State;
import org.javarosa.formmanager.api.transitions.FormBatchTransportStateTransitions;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.log.HandledItemStateListener;

/**
 * @author ctsims
 *
 */
public abstract class FormBatchTransportState implements FormBatchTransportStateTransitions, State, HandledCommandListener, HandledItemStateListener {
	//not separating out state/controller/etc, as form send is already kind of a mess

//	MultiSubmitStatusScreen screen;
//	
//	FormSender sender;
//	
//	FormBatchTransportStateTransitions transitions;
//
//	public FormBatchTransportState(Vector messages) {
//		this(messages, null);
//	}
//	
//	public FormBatchTransportState(Vector messages, TransportResponseProcessor responder) {
//		FormTransportViews views = new FormTransportViews(this, this, responder);
//		sender = new FormSender(views,messages);
//		sender.setMultiple(true);
//		screen = views.getMultiSubmitStatusScreen();
//		this.transitions = this;
//	}
//
//	public void start() {
//		sender.setObserver(screen);
//		sender.sendData();
//		J2MEDisplay.setView(screen);
//	}
//
//	public void commandAction(Command c, Displayable d) {
//		CrashHandler.commandAction(this, c, d);
//	}  
//
//	public void _commandAction(Command c, Displayable d) {
//		//It's pretty atrocious, but I don't have time to completely rewrite this right now. 
//		//Any exit from the multiscreen is just a bail.
//		transitions.done();
//	}
//
//	public void itemStateChanged(Item i) {
//		CrashHandler.itemStateChanged(this, i);
//	}  
//
//	public void _itemStateChanged(Item i) {
//		//It's pretty atrocious, but I don't have time to completely rewrite this right now. 
//		//Any exit from the multiscreen is just a bail.
//		transitions.done();
//	}

}
