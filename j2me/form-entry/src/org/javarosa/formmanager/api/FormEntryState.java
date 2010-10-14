/**
 * 
 */
package org.javarosa.formmanager.api;

import org.javarosa.core.api.State;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.formmanager.api.transitions.FormEntryTransitions;
import org.javarosa.j2me.services.DataCaptureServiceRegistry;
import org.javarosa.j2me.services.LocationCaptureService;
import org.javarosa.j2me.services.LocationCaptureService.Fix;
import org.javarosa.j2me.services.LocationCaptureService.LocationReceiver;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * @author ctsims
 *
 */
public abstract class FormEntryState implements FormEntryTransitions, State, LocationReceiver {
	
	JrFormEntryController controller;
	FormIndex capturing;
	
	public void start () {
		controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected abstract JrFormEntryController getController ();
	
	public abstract void abort();
	
	public abstract void formEntrySaved(FormDef form, FormInstance instanceData, boolean formWasCompleted);
	
	public void suspendForMediaCapture (int captureType) throws UnavailableServiceException {
		if(captureType == FormEntryTransitions.MEDIA_LOCATION) {
			DataCaptureServiceRegistry._().getLocationCaptureService();
			LocationCaptureService service = DataCaptureServiceRegistry._().getLocationCaptureService();
			if(service != null) {
				capturing = controller.getModel().getFormIndex();
				State capturer = service.getStateForCapture(this);
				J2MEDisplay.startStateWithLoadingScreen(capturer);
			}
		} else {
			throw new UnavailableServiceException("Service Code: " + captureType +" is unavailable");
		}
	}
	

	public void fixObtained(Fix fix) {
		controller.answerQuestion(new GeoPointData(new double [] {fix.getLat(), fix.getLon(), fix.getAccuracy(), fix.getAltitude()}));
		controller.getView().show(capturing);
	}

	public void fixFailed() {
		controller.getView().show(capturing);
	}
}
