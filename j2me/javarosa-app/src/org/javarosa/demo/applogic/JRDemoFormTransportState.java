/**
 * 
 */
package org.javarosa.demo.applogic;

import java.io.IOException;

import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.formmanager.api.FormTransportState;
import org.javarosa.formmanager.view.ISubmitStatusObserver;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportMessage;

/**
 * @author ctsims
 *
 */
public abstract class JRDemoFormTransportState extends FormTransportState implements ISubmitStatusObserver {
	int formId;	
	boolean screenDestroyed = false;
	TransportMessage themessage;
	
	public JRDemoFormTransportState(FormInstance tree, SubmissionProfile profile, int formId) throws IOException {
		this(JRDemoContext._().buildMessage(tree, profile), formId);
	}
	
	public JRDemoFormTransportState(TransportMessage message, int formId) {
		super(message);
		themessage = message; 
		this.formId = formId;
	}
	
	public void start() {
		sender.setObserver(this);
		sender.sendData();
		J2MEDisplay.setView(screen);
	}
	
	///OVERRIDES
	
	
	/// IMPLEMENTATIONS FOR TRANSPORT STATUS
	
	
	public void onChange(TransportMessage message, String remark) {
		//Doesn't actually reflect a change in send/not sent
		if(!screenDestroyed && screen != null) {
			screen.onChange(message, remark);
		}
	}

	public void onStatusChange(TransportMessage message) {
		if(!screenDestroyed && screen != null) {
			screen.onStatusChange(message);
		}
		if(message.isSuccess()) {
				
			//make sure we've got the right message
			if(message.equals(themessage) || message.getCacheIdentifier().equals(themessage.getCacheIdentifier())) {
				IStorageUtility storage = StorageManager.getStorage(FormInstance.STORAGE_KEY);
				
				if(storage.exists(formId)) {
					storage.remove(formId);
				}
			}
		}
	}

	//Forward along to children
	
	/**
	 * Destroys the current status screen and cleans up any running
	 * processes.
	 */
	public void destroy() {
		if(!screenDestroyed && screen != null) {
			screen.destroy();
			screenDestroyed = true;
		}
	}

	/**
	 * Destroys the current status screen and cleans up any running
	 * processes.
	 */
	public void receiveError(String details) {
		if(!screenDestroyed && screen != null) {
			screen.receiveError(details);
		}
	}

}
