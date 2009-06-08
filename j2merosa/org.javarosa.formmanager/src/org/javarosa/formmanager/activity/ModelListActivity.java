// merge this with Form list to 'metadata list'

package org.javarosa.formmanager.activity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeMetaData;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.util.externalizable.DeserializationException;

/**
 * 
 * @author Munier
 */
public class ModelListActivity extends List implements CommandListener,
		IActivity, IView {


	public final static Command CMD_BACK = new Command(JavaRosaServiceProvider.instance().localize("menu.Back"), Command.BACK, 2);
	public final static Command CMD_SEND = new Command(JavaRosaServiceProvider.instance().localize("menu.SendData"),Command.SCREEN,1);
	public static final Command CMD_SEND_ALL_UNSENT = new Command(JavaRosaServiceProvider.instance().localize("menu.send.all"), Command.SCREEN, 2);
	public final static Command CMD_EDIT = new Command(JavaRosaServiceProvider.instance().localize("menu.Edit"), Command.SCREEN, 2);
	public final static Command CMD_REVIEW = new Command(JavaRosaServiceProvider.instance().localize("menu.Review"), Command.SCREEN, 2);
	public final static Command CMD_REFRESH = new Command(JavaRosaServiceProvider.instance().localize("menu.Refresh"), Command.SCREEN, 3);
	public final static Command CMD_MSGS = new Command(JavaRosaServiceProvider.instance().localize("menu.MessageStatus"),Command.SCREEN,4);
	public final static Command CMD_DELETE = new Command(JavaRosaServiceProvider.instance().localize("menu.Delete"),Command.SCREEN,5);
	public final static Command CMD_EMPTY = new Command(JavaRosaServiceProvider.instance().localize("menu.Empty"), Command.SCREEN, 6);	
	
	public final static String returnKey = "ModelListReturnCommand";

	// RMS interfaces
	private DataModelTreeRMSUtility dataModelRMSUtility;
	private FormDefRMSUtility formDefRMSUtility;

	// the invoking shell
	private IShell parent;

	// vector of DataModelTreeMetaDatas
	private Vector models;

	// images
	private Image unSentImage;
	private Image deliveredImage;
	private Image unConfirmedImage;
	private Image failedImage;

	private Context theContext;

	/**
	 * 
	 * 
	 * @param shell
	 *            The shell from which this activity was invoked
	 */
	public ModelListActivity(IShell shell) {
		super(JavaRosaServiceProvider.instance().localize("title.SavedForms"), List.EXCLUSIVE);
		
		this.parent = shell;
		initRMSUtilities();

	}

	public void start(Context context) {
		this.theContext = context;
		initView();
	}

	public void resume(Context globalContext) {
		contextChanged(globalContext);
		initView();
	}

	public void contextChanged(Context globalContext) {
		this.theContext.mergeInContext(globalContext);
		// TODO: Do we have any values that need updating depending
		// on global context changes?
	}

	public void destroy() {
		// Stub. Nothing to do for this Module
	}

	public void halt() {
		// Stub. Nothing to do for this Module
	}

	public void commandAction(Command c, Displayable d) {
		try {
			if (isActionOnSelectedForm(c)) {
				executeModelAction(c);
			} else {
				executeGeneralAction(c);
			}

		} catch (Exception ex)// IOException ex)
		{
			// #if debug.output==verbose || debug.output==exception
			ex.printStackTrace();
			// #endif
		}
	}

	/**
	 * 
	 * Delegates actions which operate on a single selected model
	 * 
	 * @param c
	 *   	the action to be executed
	 * @throws IOException
	 * @throws DeserializationException
	 */
	private void executeModelAction(Command c) throws IOException,
			DeserializationException {
		
		// if there is nothing selected, then these actions cant be used
		if (getSelectedIndex() == -1) {
			// TODO: log error?
			return;
		}
		if (c == CMD_EDIT){
			executeEditModelCommand();
			return;
		}
		if (c == CMD_REVIEW){
			executeReviewModelCommand();
			return;
		}
		if (c == CMD_SEND){
			executeSendCommand();
			return;
		}
		
		throw new IllegalArgumentException("Command "+c.getLabel()+" is not intended for a single model");

	}

	/**
	 * 
	 * Handles actions which are not specific to a selected model
	 * 
	 * 
	 * @param c
	 * 	the action to be executed
	 */
	private void executeGeneralAction(Command c) {
		if (c == CMD_SEND_ALL_UNSENT) {
			executeSendAllUnsentCommand();
		} else if (c == CMD_EMPTY) {
			this.dataModelRMSUtility.tempEmpty();
			initView();
		} else if (c == CMD_BACK) {
			Hashtable returnArgs = new Hashtable();
			returnArgs.put(returnKey, CMD_BACK);
			this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
					returnArgs);
		} else if (c == CMD_DELETE) {
			DataModelTreeMetaData data = (DataModelTreeMetaData) this.models
					.elementAt(this.getSelectedIndex());
			this.dataModelRMSUtility.deleteRecord(data.getRecordId());
			initView();
		} else if (c == CMD_MSGS) {
			// TODO: This is a phenomenal chance to try out the
			// "inherited menus". Should look into that.
			Hashtable returnArgs = new Hashtable();
			returnArgs.put(returnKey, CMD_MSGS);
			this.parent.returnFromActivity(this, Constants.ACTIVITY_SUSPEND,
					returnArgs);

		} else if (c == CMD_REFRESH) {
			initView();
		}
	}

	private boolean isActionOnSelectedForm(Command c) {
		return (c == CMD_EDIT || c == CMD_REVIEW || c == CMD_SEND);
	}

	/**
	 * execute the sending of a model
	 */
	private void executeSendCommand() {

		// retrieve the model to be sent (from RMS)
		DataModelTreeMetaData data = (DataModelTreeMetaData) this.models
				.elementAt(getSelectedIndex());
		ITransportManager transportManager = JavaRosaServiceProvider.instance()
				.getTransportManager();
		DataModelTree model = new DataModelTree();
		
		try {
			
			this.dataModelRMSUtility.retrieveFromRMS(data.getRecordId(), model);
			
		} catch (IOException e) {
			handleModelRetrieveException(e);
			return;
		} catch (DeserializationException e) {
			handleModelRetrieveException(e);
			return;
		}
		
		// send the model retrieved..
		
		
		// restrict resending of sent forms here

		if (TransportMessage.STATUS_DELIVERED == transportManager.getModelDeliveryStatus(data
				.getRecordId(), true)) {
			handleModelAlreadyDelivered();
		} else {
			
			// all ok, actually do the sending... by asking the parent to do the sending
			Hashtable formSendArgs = new Hashtable();
			// TODO: We need some way to codify this Next Action stuff.
			// Maybe a set of Constants for the ModelListModule?
			formSendArgs.put(returnKey, CMD_SEND);
			formSendArgs.put("data", model);
			this.parent.returnFromActivity(this,
					Constants.ACTIVITY_NEEDS_RESOLUTION, formSendArgs);
		}

	}
	
	
	/**
	 * 
	 * what to do if the model status shows that it has already been delivered (TODO: why in RMS then?)
	 * 
	 */
	private void handleModelAlreadyDelivered(){
		final javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert(
				"Resend restriction", "Form already submitted!", null,
				AlertType.INFO);
		this.parent.setDisplay(this, new IView() {
			public Object getScreenObject() {
				return a;
			}
		});
		Hashtable returnArgs = new Hashtable();
		returnArgs.put(returnKey, CMD_MSGS);
		this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
				returnArgs);
	}
	/**
	 * 
	 * What to do if an exception is thrown when the model is retrieved from RMS
	 * 
	 * @param e
	 */
	private void handleModelRetrieveException(Exception e){
		final javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert(
				"modelLoadError", "Error Loading Model", null,
				AlertType.ERROR);
		this.parent.setDisplay(this, new IView() {
			public Object getScreenObject() {
				return a;
			}
		});
		e.printStackTrace();
	}

	/**
	 * 
	 * send all unsent forms
	 * 
	 */
	private void executeSendAllUnsentCommand() {
		Enumeration en = this.models.elements();
		Vector unsent = new Vector();
		while (en.hasMoreElements()) {
			DataModelTreeMetaData data = (DataModelTreeMetaData) en
					.nextElement();
			DataModelTree model = new DataModelTree();
			ITransportManager tm = JavaRosaServiceProvider.instance()
					.getTransportManager();
			try {
				this.dataModelRMSUtility.retrieveFromRMS(data.getRecordId(),
						model);
			 
			} catch (IOException e) {
				handleModelRetrieveException(e);
				return;
			} catch (DeserializationException e) {
				handleModelRetrieveException(e);
				return;
			}
			// restrict resending of sent forms here

			if (TransportMessage.STATUS_DELIVERED == tm.getModelDeliveryStatus(
					data.getRecordId(), true)) {
				// Do Nothing if already sent
			} else {
				unsent.addElement(model);
			}
		}
		
		// if there are unsent forms(models?)
		if (unsent.size() > 0) {
			Hashtable formSendArgs = new Hashtable();
			// TODO: We need some way to codify this Next Action stuff.
			// Maybe a set of Constants for the ModelListModule?
			formSendArgs.put(returnKey, CMD_SEND_ALL_UNSENT);
			formSendArgs.put("data_vec", unsent);
			this.parent.returnFromActivity(this,
					Constants.ACTIVITY_NEEDS_RESOLUTION, formSendArgs);

		} else {
			
			// there are no unsent forms
			initView();
		}
	}

	/**
	 * 
	 * The user requests to edit a model
	 * 
	 * @throws IOException
	 * @throws DeserializationException
	 */
	private void executeEditModelCommand() throws IOException,
			DeserializationException {
		DataModelTreeMetaData data = (DataModelTreeMetaData) this.models
				.elementAt(this.getSelectedIndex());
		FormDef selectedForm = new FormDef();
		// #if debug.output==verbose
		System.out.println("Attempt retreive: " + data.getFormIdReference());
		// #endif
		
		
		// TODO: why no exception handling here?
		this.formDefRMSUtility.retrieveFromRMS(data.getFormIdReference(),
				selectedForm);
		// #if debug.output==verbose
		System.out.println("Form retrieve OK\nAttempt retreive model: "
				+ data.getRecordId());
		// #endif
		DataModelTree formData = new DataModelTree();
		
		// TODO: why no exception handling here?
		this.dataModelRMSUtility.retrieveFromRMS(data.getRecordId(), formData);
		selectedForm.setTitle(this.formDefRMSUtility.getName(data
				.getFormIdReference()));
		
		// #if debug.output==verbose
		System.out.println("data model: " + formData.getName());
		// #endif
		
		Hashtable formEditArgs = new Hashtable();
		formEditArgs.put(returnKey, CMD_EDIT);
		formEditArgs.put("form", selectedForm);
		formEditArgs.put("data", formData);
		this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
				formEditArgs);
	}

	private void executeReviewModelCommand() throws IOException,
			DeserializationException {
		DataModelTreeMetaData data = (DataModelTreeMetaData) this.models
				.elementAt(this.getSelectedIndex());
		FormDef selectedForm = new FormDef();
		// #if debug.output==verbose
		System.out.println("Attempt retreive: " + data.getFormIdReference());
		// #endif
		this.formDefRMSUtility.retrieveFromRMS(data.getFormIdReference(),
				selectedForm);
		// #if debug.output==verbose
		System.out.println("Form retrieve OK\nAttempt retreive model: "
				+ data.getRecordId());
		// #endif
		DataModelTree formData = new DataModelTree();
		this.dataModelRMSUtility.retrieveFromRMS(data.getRecordId(), formData);
		selectedForm.setTitle(this.formDefRMSUtility.getName(data
				.getFormIdReference()));
		// #if debug.output==verbose
		System.out.println("data model: " + formData.getName());
		// #endif
		//
		Hashtable formReviewArgs = new Hashtable();
		formReviewArgs.put(returnKey, CMD_REVIEW);
		formReviewArgs.put("form", selectedForm);
		formReviewArgs.put("data", formData);
		this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
				formReviewArgs);
	}

	/**
	 * models are retrieved from RMS and stored in a Vector
	 * 
	 * @return list of models
	 */
	private Vector retrieveModelsFromRMS() {
		ITransportManager tm = JavaRosaServiceProvider.instance()
				.getTransportManager();

		this.dataModelRMSUtility.open();

		Vector modelsVector = new Vector();

		int pos = 0;

		IRecordStoreEnumeration recordEnum = this.dataModelRMSUtility
				.enumerateMetaData();
		while (recordEnum.hasNextElement()) {

			try {
				DataModelTreeMetaData mdata = getModelFromRMS(recordEnum);

				Image stateImg = getStateImage(tm.getModelDeliveryStatus(mdata
						.getRecordId(), true));
				String serverKey = this.getModelReplyData(mdata.getRecordId());

				this.append(mdata.getName() + " - " + convertDate(mdata)
						+ " - " + serverKey, stateImg);
				modelsVector.insertElementAt(mdata, pos);
				pos++;
			} catch (RecordStorageException e) {
				e.printStackTrace();
			}
		}

		return modelsVector;
	}

	private String convertDate(DataModelTreeMetaData mdata) {
		// /convert the date to special format with calendar
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(mdata.getDateSaved());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int date = calendar.get(Calendar.DATE);
		String dateSTR = date + "-" + month + "-" + year;
		return dateSTR;
	}

	private DataModelTreeMetaData getModelFromRMS(
			IRecordStoreEnumeration recordEnum) throws RecordStorageException {
		int i = recordEnum.nextRecordId();
		DataModelTreeMetaData mdata = new DataModelTreeMetaData();
		this.dataModelRMSUtility.retrieveMetaDataFromRMS(i, mdata);
		// TODO fix it so that record id is part of the metadata serialization
		mdata.setRecordId(i);
		return mdata;
	}

	private String getModelReplyData(int modelId) {

		Enumeration qMessages = JavaRosaServiceProvider.instance()
				.getTransportManager().getMessages();

		TransportMessage message;
		while (qMessages.hasMoreElements()) {
			message = (TransportMessage) qMessages.nextElement();
			if (message.getModelId() == modelId)
				return new String(message.getReplyloadData());
		}
		return "";
	}

	

	// state images
	private void createImages() {
		this.unSentImage = initialiseStateImage(12, 12, 255, 255, 255);
		this.deliveredImage = initialiseStateImage(12, 12, 0, 255, 0);
		this.unConfirmedImage = initialiseStateImage(12, 12, 255, 140, 0);
		this.failedImage = initialiseStateImage(12, 12, 255, 0, 0);
	}
	private Image getStateImage(int modelDeliveryStatus) {
		Image result;
		switch (modelDeliveryStatus) {
		case TransportMessage.STATUS_DELIVERED:
			result = this.deliveredImage;
			break;
		case TransportMessage.STATUS_NEW:
			result = this.unConfirmedImage;
			break;
		case TransportMessage.STATUS_NOT_SENT:
			result = this.unSentImage;
			break;
		case TransportMessage.STATUS_FAILED:
			result = this.failedImage;
			break;
		default:
			result = this.unSentImage;
			break;
		}
		return result;
	}
	private Image initialiseStateImage(int w, int h, int r, int g, int b) {
		Image res = Image.createImage(w, h);
		Graphics gc = res.getGraphics();
		gc.setColor(r, g, b);
		gc.fillRect(0, 0, w, h);
		return res;
	}

	public Context getActivityContext() {
		return this.theContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.parent = shell;
	}

	public Object getScreenObject() {
		// TODO Auto-generated method stub
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api
	 * .ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException(
				"The Activity Class "
						+ this.getClass().getName()
						+ " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}

	private void initView() {
		createImages();
		deleteAll();
		addCommands();
		setCommandListener(this);
		this.models = retrieveModelsFromRMS();
		this.parent.setDisplay(this, this);
	}

	private void addCommands() {
		// this.setTicker(new Ticker("Please select a Model to send..."));
		addCommand(CMD_BACK);
		addCommand(CMD_EDIT);
		addCommand(CMD_REVIEW);
		addCommand(CMD_SEND);
		// this.addCommand(CMD_MSGS);//now redundant as we have color boxes for
		// this TODO: clean up related code!
		addCommand(CMD_SEND_ALL_UNSENT);
		addCommand(CMD_DELETE);
		addCommand(CMD_EMPTY);
		addCommand(CMD_REFRESH);
	}

	private void initRMSUtilities() {
		this.dataModelRMSUtility = (DataModelTreeRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(DataModelTreeRMSUtility.getUtilityName());
		this.formDefRMSUtility = (FormDefRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(FormDefRMSUtility.getUtilityName());
	}
}