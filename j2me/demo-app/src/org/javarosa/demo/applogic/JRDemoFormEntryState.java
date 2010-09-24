package org.javarosa.demo.applogic;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.demo.util.JRDemoFormEntryViewFactory;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.formmanager.api.CompletedFormOptionsState;
import org.javarosa.formmanager.api.FormEntryState;
import org.javarosa.formmanager.api.JrFormEntryController;
import org.javarosa.formmanager.api.JrFormEntryModel;
import org.javarosa.formmanager.utility.FormDefFetcher;
import org.javarosa.formmanager.utility.RMSRetreivalMethod;
import org.javarosa.j2me.services.DataCaptureServiceRegistry;
import org.javarosa.j2me.services.LocationCaptureService;
import org.javarosa.j2me.services.LocationCaptureService.Fix;
import org.javarosa.j2me.services.LocationCaptureService.LocationReceiver;
import org.javarosa.j2me.view.J2MEDisplay;

public class JRDemoFormEntryState extends FormEntryState implements
LocationReceiver {

	protected int formID;
	protected int instanceID;
	protected DataCaptureServiceRegistry serviceRegistry;

	boolean cameFromFormList;
	
	protected JrFormEntryController controller;
	
	public JRDemoFormEntryState (int formID) {
		init(formID, -1, true);
	}

	public JRDemoFormEntryState (int formID, int instanceID) {
		init(formID, instanceID, false);
	}

	private void init (int formID, int instanceID, boolean cameFromFormList) {
		this.formID = formID;
		this.instanceID = instanceID;
		this.cameFromFormList = cameFromFormList;
	}
	
	protected JrFormEntryController getController() {

		Vector<IPreloadHandler> preloaders = JRDemoContext._().getPreloaders();
		Vector<IFunctionHandler> funcHandlers = JRDemoContext._().getFuncHandlers();
		FormDefFetcher fetcher = new FormDefFetcher(new RMSRetreivalMethod(formID), preloaders, funcHandlers);
		FormDef form = fetcher.getFormDef();
		
		controller =  new JrFormEntryController(new JrFormEntryModel(form));
		controller.setView(new JRDemoFormEntryViewFactory().getFormEntryView(controller));
		return controller;
	}

	public void abort() {
		JRDemoUtil.goToList(cameFromFormList);
	}

	public void formEntrySaved(FormDef form, FormInstance instanceData, boolean formWasCompleted) {
		System.out.println("form is complete: " + formWasCompleted);
		if (formWasCompleted) {
			final SubmissionProfile profile = form.getSubmissionProfile();
			
			CompletedFormOptionsState completed = new CompletedFormOptionsState(instanceData) {

				public void sendData(FormInstance data) {
					JRDemoFormTransportState send;
					try {
						send = new JRDemoFormTransportState(data, profile) {

							public void done() {
								JRDemoUtil.goToList(cameFromFormList);
							}

							public void sendToBackground() {
								JRDemoUtil.goToList(cameFromFormList);
							}
							
						};
					} catch (IOException e) {
						throw new RuntimeException("Unable to serialize XML Payload!");
					}
					send.start();
				}

				public void sendToFreshLocation(FormInstance data) {
					throw new RuntimeException("Sending to non-default location disabled");
				}

				public void skipSend(FormInstance data) {
					IStorageUtility storage = StorageManager.getStorage(FormInstance.STORAGE_KEY);
					try {
						System.out.println("writing data: " + data.getName());
						storage.write(data);
					} catch (StorageFullException e) {
						new RuntimeException("Storage full, unable to save data.");
					}
					abort();
				}
			};
			completed.start();
		} else {
			abort();
		}
	}

	public void suspendForMediaCapture(int captureType)
			throws UnavailableServiceException {
		if (captureType == FormEntryState.MEDIA_LOCATION) {
			LocationCaptureService ls = DataCaptureServiceRegistry._()
					.getLocationCaptureService();
			if (ls != null)
				ls.getStateForCapture(this).start();
		}
	}

	public void fixObtained(Fix fix) {
		double[] gp = new double[]{fix.getLat(),fix.getLon(),fix.getAltitude(), fix.getAccuracy()};
		controller.answerQuestion(new GeoPointData(gp));
		controller.start(controller.getModel().getFormIndex());
	}

	public void fixFailed() {
		controller.start(controller.getModel().getFormIndex());
	}

}
