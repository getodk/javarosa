package org.javarosa.user.api;

import java.io.IOException;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.UnrecognizedResponseException;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.senders.SenderThread;
import org.javarosa.user.api.transitions.RegisterUserTransitions;
import org.javarosa.user.model.User;
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
	private RegisterUserTransitions transitions;
	private UserRegistrationForm form;
	private User registerdUser;
	
	private static final Command RETRY = new Command(Localization.get("command.retry"),Command.OK, 1);
	private static final Command CANCEL = new Command(Localization.get("command.cancel"), Command.CANCEL,1);
	
	private static final Command OK = new Command(Localization.get("menu.ok"), Command.CANCEL,1);
	

	public RegisterUserController(UserRegistrationTranslator<M> builder) {
		this.builder = builder;
	}
	
	public void setTransitions(RegisterUserTransitions transitions) {
		this.transitions = transitions;
	}

	public void start(){
		
		form = new UserRegistrationForm(Localization.get("user.registration.title"));
		form.setCommandListener(this);
		J2MEDisplay.setView(form);
		
		try{
			M message = builder.getUserRegistrationMessage();
			SenderThread thread = TransportService.send(message);
			thread.addListener(this);
		} catch (TransportException e) {
			e.printStackTrace();
			onFail();
		} catch(IOException ioe) {
			//This is from actually building the message
			ioe.printStackTrace();
			onFail();
		}
	}
	
	private void onSuccess(M message) {
		//TODO: Get feedback about the user from this builder?
		try {
			registerdUser = builder.readResponse(message);
			form.addCommand(OK);
		} catch(UnrecognizedResponseException ure) {
			form.addCommand(RETRY);
			form.addCommand(CANCEL);
			form.setText(Localization.get("user.registration.badresponse"));
		}
		form.setText(Localization.get("user.registration.success"));
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
		if(c == CANCEL) {
			transitions.cancel();
		} else if(c == OK ){
			transitions.succesfullyRegistered(registerdUser);
		} else if(c == RETRY) {
			form.removeAllCommands();
			form.setText(Localization.get("user.registration.attempt"));
			try {
				M message = builder.getUserRegistrationMessage();
				SenderThread thread = TransportService.send(message);
				thread.addListener(this);
			} catch (TransportException e) {
				e.printStackTrace();
				onFail();
			} catch(IOException ioe) {
				//This is from actually building the message
				ioe.printStackTrace();
				onFail();
			}
		}
	}
}
