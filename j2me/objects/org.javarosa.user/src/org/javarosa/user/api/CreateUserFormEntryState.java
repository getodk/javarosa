/**
 * 
 */
package org.javarosa.user.api;

import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.formmanager.api.FormEntryState;
import org.javarosa.formmanager.api.JrFormEntryController;
import org.javarosa.formmanager.api.JrFormEntryModel;
import org.javarosa.formmanager.utility.FormDefFetcher;
import org.javarosa.formmanager.utility.NamespaceRetrievalMethod;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;
import org.javarosa.user.api.transitions.AddUserTransitions;
import org.javarosa.user.model.User;
import org.javarosa.user.utility.UserModelProcessor;

/**
 * The Add User Form Entry State is responsible for 
 * running the user entry form for this device and 
 * applying the appropriate Model Processor to deal
 * with any user blocks.
 * 
 * @author ctsims
 *
 */
public abstract class CreateUserFormEntryState extends FormEntryState implements AddUserTransitions {

	private String formName;
	private Vector<IPreloadHandler> preloaders;
	private Vector<IFunctionHandler> funcHandlers;
		
	public CreateUserFormEntryState (String formName,
			Vector<IPreloadHandler> preloaders, Vector<IFunctionHandler> funcHandlers) {
		this.formName = formName;
		this.preloaders = preloaders;
		this.funcHandlers = funcHandlers;
	}
	
	public CreateUserFormEntryState (Vector<IPreloadHandler> preloaders, Vector<IFunctionHandler> funcHandlers) {
		this.formName = "http://code.javarosa.org/user_registration";
		this.preloaders = preloaders;
		this.funcHandlers = funcHandlers;
	}
	
	
	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.api.FormEntryState#getController()
	 */
	protected JrFormEntryController getController() {
		FormDefFetcher fetcher = new FormDefFetcher(new NamespaceRetrievalMethod(formName), preloaders, funcHandlers);
		JrFormEntryController controller = new JrFormEntryController(new JrFormEntryModel(fetcher.getFormDef()));
		controller.setView(new Chatterbox(Localization.get("user.create.header"),controller));
		return controller;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.api.transitions.FormEntryTransitions#formEntrySaved(org.javarosa.core.model.FormDef, org.javarosa.core.model.instance.FormInstance, boolean)
	 */
	public void formEntrySaved(FormDef form, FormInstance instanceData, boolean formWasCompleted) {
		if(formWasCompleted) {
			UserModelProcessor processor = new UserModelProcessor();
			processor.processInstance(instanceData);
			User u = processor.getRegisteredUser();
			if(u != null) {
				userCreated(u);
			} else {
				abort();
			}
		} else {
			abort();
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.api.transitions.FormEntryTransitions#suspendForMediaCapture(int)
	 */
	public void suspendForMediaCapture(int captureType) {
		//This doesn't mean anything anymore.
	}
	
	
	//For the Nokia JVM Bug
	public abstract void abort();
	
	public abstract void userCreated(User newUser);
	public abstract void cancel();
}
