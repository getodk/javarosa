/**
 * 
 */
package org.javarosa.formmanager.api;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;

import org.javarosa.core.api.State;
import org.javarosa.formmanager.api.transitions.FormBatchTransportStateTransitions;
import org.javarosa.formmanager.utility.FormSender;
import org.javarosa.formmanager.view.transport.FormTransportViews;
import org.javarosa.formmanager.view.transport.MultiSubmitStatusScreen;
import org.javarosa.formmanager.view.transport.TransportResponseProcessor;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.log.HandledItemStateListener;
import org.javarosa.j2me.view.J2MEDisplay;

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
