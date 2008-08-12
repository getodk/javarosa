package org.javarosa.patient.entry.activity;

import java.util.Hashtable;

import org.javarosa.core.Context;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormDef;
import org.javarosa.patient.entry.util.PatientEntryFormDefFactory;

public class PatientEntryActivity implements IActivity {

	public static final String PATIENT_ENTRY_FORM_KEY = "Patient Entry Form";
	
	Context context;
	IShell parent;
	
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
		// TODO Auto-generated method stub
		return null;
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
		
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		this.context = context;
		FormDef patientEntryForm = PatientEntryFormDefFactory.createPatientEntryFormDef();
		Hashtable table = new Hashtable();
		table.put(PATIENT_ENTRY_FORM_KEY, patientEntryForm);
		parent.returnFromActivity(this, Constants.ACTIVITY_NEEDS_RESOLUTION, table);
	}
	
}
