/**
 * 
 */
package org.javarosa.formmanager.api;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.State;
import org.javarosa.core.model.utils.IDataModelSerializingVisitor;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.formmanager.api.transitions.FormBatchTransportStateTransitions;
import org.javarosa.formmanager.utility.FormSender;
import org.javarosa.formmanager.view.transport.FormTransportViews;
import org.javarosa.formmanager.view.transport.MultiSubmitStatusScreen;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * @author ctsims
 *
 */
public class FormBatchTransportState implements State<FormBatchTransportStateTransitions>, CommandListener {

	MultiSubmitStatusScreen screen;
	
	FormSender sender;
	
	FormBatchTransportStateTransitions transitions;

	
	public FormBatchTransportState(Vector dataModels, IDataModelSerializingVisitor serializer, ITransportDestination destination) {
		FormTransportViews views = new FormTransportViews(this);
		sender = new FormSender(views,destination);
		sender.setSerializer(serializer);
		sender.setMultiple(true);
		sender.setMultiData(dataModels);
		screen = views.getMultiSubmitStatusScreen();
	}

	public void enter(FormBatchTransportStateTransitions transitions) {
		this.transitions = transitions;
	}

	public void start() {
		sender.setObserver(screen);
		sender.sendData();
		J2MEDisplay.setView(screen);
	}

	public void commandAction(Command c, Displayable arg1) {
		//It's pretty atrocious, but I don't have time to completely rewrite this right now. 
		//Any exit from the multiscreen is just a bail.
		transitions.done();
	}

}
