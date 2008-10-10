package org.javarosa.referral.activity;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.referral.model.Referrals;
import org.javarosa.referral.storage.ReferralRMSUtility;
import org.javarosa.referral.util.ReportContext;
import org.javarosa.referral.view.ReportView;
import org.javarosa.xform.util.XFormAnswerDataSerializer;

public class ReferralReport implements IActivity, CommandListener {

	private IShell parent;
	private Referrals referrals;
	private DataModelTree model;
	private ReportView view;
	
	private Context context;
	
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
			this.context = context;
			int formId = ((ReportContext)context).getFormId();
			int modelId = ((ReportContext)context).getModelId();
			
			ReferralRMSUtility referralRms = (ReferralRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(ReferralRMSUtility.getUtilityName());
			DataModelTreeRMSUtility modelUtility = (DataModelTreeRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(DataModelTreeRMSUtility.getUtilityName());
			if(!referralRms.containsFormReferrals(formId)) {
				this.referrals = new Referrals();
			} else {
				try {
					this.referrals = referralRms.retrieveFromRMS(formId);

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
				} catch (DeserializationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		view = new ReportView("Referral Report");
		view.setReferrals(referrals.getPositiveReferrals(model, new XFormAnswerDataSerializer()));
		view.setCommandListener(this);
		
		parent.setDisplay(this, view);
	}
	public Context getActivityContext() {
		return context;
	}

	public void commandAction(Command arg0, Displayable arg1) {
		parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.parent = shell;
	}
}