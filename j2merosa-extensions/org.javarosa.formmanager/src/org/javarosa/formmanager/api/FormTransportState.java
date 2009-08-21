/**
 * 
 */
package org.javarosa.formmanager.api;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.State;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.utils.IDataModelSerializingVisitor;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.formmanager.api.transitions.FormTransportStateTransitions;
import org.javarosa.formmanager.utility.FormSender;
import org.javarosa.formmanager.view.transport.FormTransportSubmitStatusScreen;
import org.javarosa.formmanager.view.transport.FormTransportViews;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * @author ctsims
 *
 */
public class FormTransportState implements State<FormTransportStateTransitions>, CommandListener {
	
	FormTransportStateTransitions transitions;
	FormTransportSubmitStatusScreen screen;
	
	FormSender sender;

	public FormTransportState(DataModelTree dataModel, IDataModelSerializingVisitor serializer, ITransportDestination destination) {
		FormTransportViews views = new FormTransportViews(this);
		sender = new FormSender(views, destination);
		sender.setSerializer(serializer);
		sender.setMultiple(false);
		sender.setData(dataModel);
		screen = views.getSubmitStatusScreen();
	}
	
	public void enter(FormTransportStateTransitions transitions) {
		this.transitions = transitions;
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

}
