package org.javarosa.patient.entry.activity;

import java.io.IOException;
import java.util.Hashtable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.model.utils.IModelProcessor;
import org.javarosa.core.util.externalizable.DeserializationException;

/**
 * NOTICE:
 * 
 * This class is a form post-processing wrapper, and should be replaced as soon as is possible
 * by a generic form post-processing approach.
 *  
 * @author Clayton Sims
 *
 */
public class PatientEntryActivity implements IActivity {

	public static final String PATIENT_ENTRY_FORM_KEY = "jr-patient-reg";
	public static final String NEW_PATIENT_ID = "patient-id";
	
	PatientEntryContext context;
	IShell parent;
	
	FormDef patientEntryForm;
	
	IModelProcessor processor;
	
	public PatientEntryActivity(IShell parent) {
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#getActivityContext()
	 */
	public Context getActivityContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		context.mergeInContext(globalContext);
		Hashtable returnVals = new Hashtable();
		if(((Boolean)globalContext.getElement("QUIT_WITHOUT_SAVING")).booleanValue()) {
			parent.returnFromActivity(this,Constants.ACTIVITY_ERROR, returnVals);
		} else {
			processor.initializeContext(this.context);
			processor.processModel((DataModelTree) patientEntryForm.getDataModel());
			processor.loadProcessedContext(this.context);
			
			int patID =((Integer) this.context.getElement("PATIENT_ID")).intValue();
			returnVals.put(NEW_PATIENT_ID, new Integer(patID));
			returnVals.put("DATA_MODEL", globalContext.getElement("DATA_MODEL"));
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
					returnVals);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		this.context = new PatientEntryContext(context);
		this.processor = this.context.getProcessor();
		String title;
		if (this.context.getEntryFormTitle() != null) {
			title = this.context.getEntryFormTitle();
		} else {
			String mode = (String) context.getElement("ENTRY_MODE");
			if ("BATCH".equals(mode)) {
				title = "jr-patient-batch-reg";
			} else {
				title = "jr-patient-single-reg";
			}
		}
		FormDefRMSUtility utility = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());
		
		patientEntryForm = new FormDef();
		
		try { 
			utility.retrieveFromRMS(utility.getIDfromName(title), patientEntryForm);
		}
		catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException("No Patient Registration form found with title + '" + title + "'! Please check settings.");

		}
		catch(DeserializationException e) {
			e.printStackTrace();
			throw new RuntimeException("No Patient Registration form found with title + '" + title + "'! Please check settings.");
		}
		
		if(patientEntryForm == null) {
			throw new RuntimeException("No Patient Registration form found with title + '" + title + "'! Please check settings.");
		}

		Hashtable table = new Hashtable();
		table.put(PATIENT_ENTRY_FORM_KEY, patientEntryForm);
		parent.returnFromActivity(this, Constants.ACTIVITY_NEEDS_RESOLUTION,
				table);
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
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
