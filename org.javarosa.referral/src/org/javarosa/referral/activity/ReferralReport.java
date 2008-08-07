package org.javarosa.referral.activity;

import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.storage.PatientRMSUtility;
import org.javarosa.referral.util.ReportContext;
import org.javarosa.referral.view.ReportView;

public class ReferralReport implements IActivity {

	IShell parent;
	Patient patient;
	DataModelTree model;
	ReportView view;
	
	public ReferralReport(IShell parent) {
		this.parent = parent;
	}
	
	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub

	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void halt() {
		// TODO Auto-generated method stub

	}

	public void resume(Context globalContext) {
		// TODO Auto-generated method stub

	}

	public void start(Context context) {
		if(context instanceof ReportContext) {
			int patientId = ((ReportContext)context).getPatientId();
			int modelId = ((ReportContext)context).getModelId();
			
			PatientRMSUtility patientUtility = (PatientRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(PatientRMSUtility.getUtilityName());
			DataModelTreeRMSUtility modelUtility = (DataModelTreeRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(DataModelTreeRMSUtility.getUtilityName());
			try {
				this.patient = new Patient();
				patientUtility.retrieveFromRMS(patientId, this.patient);
				
				this.model = new DataModelTree();
				modelUtility.retrieveFromRMS(modelId, this.model);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnavailableExternalizerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		view = new ReportView("Referral Report");
		
		parent.setDisplay(this, view);
	}
	
	public Vector determineReferrals() {
		Vector referrals = new Vector();
		return referrals;
	}
}