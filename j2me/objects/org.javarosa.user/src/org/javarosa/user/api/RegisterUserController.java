package org.javarosa.user.api;

import java.io.DataInputStream;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.senders.SenderThread;
import org.javarosa.user.api.transitions.RegisterUserTransitions;
import org.javarosa.user.transport.UserRegistrationTranslator;
import org.javarosa.user.view.UserRegistrationForm;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;

/**
 * @author ctsims
 *
 */
public class RegisterUserController<M extends TransportMessage> implements TransportListener, CommandListener {
	
	private UserRegistrationTranslator<M> builder; 
	private RegisterUserState state;
	private RegisterUserTransitions transitions;
	private UserRegistrationForm form;
	
	private static final Command RETRY = new Command(Localization.get("command.retry"),Command.OK, 1);
	private static final Command CANCEL = new Command(Localization.get("command.cancel"), Command.CANCEL,1);
	
	private static final Command OK = new Command(Localization.get("menu.ok"), Command.CANCEL,1);
	

	public RegisterUserController(UserRegistrationTranslator<M> builder, RegisterUserState state) {
		this.builder = builder;
		this.state = state;
	}
	
	public void setTransitions(RegisterUserTransitions transitions) {
		this.transitions = transitions;
	}

	public void start(){
		M message = builder.getUserRegistrationMessage();
		form = new UserRegistrationForm(Localization.get("user.registration.title"));
		form.setCommandListener(this);
		J2MEDisplay.setView(form);
		try {
			SenderThread thread = TransportService.send(message);
			thread.addListener(this);
		} catch (TransportException e) {
			e.printStackTrace();
			onFail();
		}
	}
	
	private void onSuccess(M message) {
		form.addCommand(OK);
	}
	
	private void onFail() {
		form.addCommand(RETRY);
		form.addCommand(CANCEL);
		form.setText(Localization.get("user.registration.failmessage"));
	}

	public void onChange(TransportMessage message, String remark) {
		//Meh. Maybe update the screen with these remarks.
	}

	public void onStatusChange(TransportMessage message) {
		switch(message.getStatus()) {
		case TransportMessageStatus.SENT:
			onSuccess((M)message);
			break;
		default:
			onFail();
			break;
		}
	}

	public void commandAction(Command c, Displayable d) {
		if(c == CANCEL || c == OK) {
			
		} else if(c == RETRY) {
			M message = builder.getUserRegistrationMessage();
			form.removeAllCommands();
			form.setText(Localization.get("user.registration.attempt"));
			try {
				SenderThread thread = TransportService.send(message);
				thread.addListener(this);
			} catch (TransportException e) {
				e.printStackTrace();
				onFail();
			}
		}
	}
}
