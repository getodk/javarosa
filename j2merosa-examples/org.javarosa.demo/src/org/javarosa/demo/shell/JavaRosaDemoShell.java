/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.demo.shell;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.midlet.MIDlet;

import org.javarosa.activity.splashscreen.SplashScreenActivity;
import org.javarosa.communication.http.HttpTransportModule;
import org.javarosa.communication.http.HttpTransportProperties;
import org.javarosa.communication.ui.CommunicationUIModule;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.core.util.WorkflowStack;
import org.javarosa.demo.properties.DemoAppProperties;
import org.javarosa.demo.util.FormEntryViewFactory;
import org.javarosa.entity.activity.EntitySelectActivity;
import org.javarosa.entity.util.EntitySelectContext;
import org.javarosa.formmanager.FormManagerModule;
import org.javarosa.formmanager.activity.FormEntryActivity;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.activity.FormListActivity;
import org.javarosa.formmanager.activity.FormReviewActivity;
import org.javarosa.formmanager.activity.FormTransportActivity;
import org.javarosa.formmanager.activity.ModelListActivity;
import org.javarosa.formmanager.utility.IFormDefRetrievalMethod;
import org.javarosa.formmanager.utility.RMSRetreivalMethod;
import org.javarosa.formmanager.utility.ReferenceRetrievalMethod;
import org.javarosa.formmanager.utility.TransportContext;
import org.javarosa.formmanager.view.Commands;
import org.javarosa.formmanager.view.chatterbox.widget.ExtendedWidgetsModule;
import org.javarosa.j2me.J2MEModule;
import org.javarosa.j2me.util.DumpRMS;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.patient.PatientModule;
import org.javarosa.patient.entry.activity.PatientEntryActivity;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.select.activity.PatientEntity;
import org.javarosa.patient.storage.PatientRMSUtility;
import org.javarosa.referral.ReferralModule;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.resources.locale.LanguagePackModule;
import org.javarosa.services.properties.activity.PropertyScreenActivity;
import org.javarosa.user.activity.AddUserActivity;
import org.javarosa.user.activity.LoginActivity;
import org.javarosa.user.model.User;
import org.javarosa.xform.util.XFormUtils;

/**
 * This is the shell for the JavaRosa demo that handles switching all of the
 * views
 * 
 * @author Brian DeRenzi
 * 
 */
public class JavaRosaDemoShell implements IShell {

	// the basic J2ME application entity
	private MIDlet midlet;

	// javarosa works with a stack of activities (IActivity)
	private WorkflowStack stack = new WorkflowStack();
	private Context context = new Context();

	private IActivity currentActivity;
	private IActivity mostRecentListActivity; // never accessed, only checked

	// for type

	public JavaRosaDemoShell(MIDlet midlet) {
		setMIDlet(midlet);
	}

	public void run() {
		initDemo();
		workflow(null, null, null);
	}

	private void initDemo() {
		// TODO: comment the reason for this
		DumpRMS.RMSRecoveryHook(this.midlet);

		loadModules();
		loadProperties();
		loadDemoForms();
		loadDemoPatients();
	}

	/**
	 * @param lastActivity
	 * @param returnCode
	 * @param returnVals
	 */
	private void workflow(IActivity lastActivity, String returnCode,
			Hashtable returnVals) {
		if (returnVals == null)
			returnVals = new Hashtable(); // for easier processing

		if (lastActivity != this.currentActivity) {
			// #debug error
			System.out
					.println("Received 'return' event from activity other than the current activity"
							+ " (such as a background process). Can't handle this yet.  Saw: "
							+ lastActivity
							+ " but expecting: "
							+ this.currentActivity);
			return;
		}

		// activities can be suspended, or be in need of resolution
		// these activities are pushed onto the stack
		if (returnCode == Constants.ACTIVITY_SUSPEND
				|| returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
			this.stack.push(lastActivity);
			workflowLaunch(lastActivity, returnCode, returnVals);
		} else {
			// if the last activity was completed
			if (this.stack.size() > 0) {
				workflowResume(this.stack.pop(), lastActivity, returnCode,
						returnVals);
			} else {
				workflowLaunch(lastActivity, returnCode, returnVals);
				if (lastActivity != null)
					lastActivity.destroy();
			}
		}
	}

	 
	private void debug(IActivity returningActivity, String returnCode,
			Hashtable returnVals) {
		String args = "";
		Enumeration keyEnum = returnVals.keys();
		while (keyEnum.hasMoreElements()) {
			Object key = keyEnum.nextElement();
			args += "\t" + key + " = " + returnVals.get(key);
		}
		// #debug debug
		System.out.println("returningActivity: " + returningActivity);
		// #debug debug
		System.out.println("\treturn code: " + returnCode);
		// #debug debug
		System.out.println("\targs: " + args);
	}

	 

	/**
	 * @param returningActivity
	 * @param returnCode
	 * @param returnVals
	 */
	private void workflowLaunch(IActivity returningActivity, String returnCode,
			Hashtable returnVals) {

		debug(returningActivity, returnCode, returnVals);

		// at application start, returning activity is null
		if (returningActivity == null) {
			launchActivity(new SplashScreenActivity(this, ),
					context);
			return;
		}

		// once the splash screen has returned control
		if (returningActivity instanceof SplashScreenActivity) {

			return;
		}

		
		// login (won't be activated if dev.shortcuts is true)
		if (returningActivity instanceof LoginActivity) {

			Object returnVal = returnVals.get(LoginActivity.COMMAND_KEY);
			if (returnVal == "USER_VALIDATED") {
				User user = (User) returnVals.get(LoginActivity.USER);
				if (user != null) {
					context.setCurrentUser(user.getUsername());
					context.setElement("USER", user);
				}

				launchEntitySelectActivity(context);
			} else if (returnVal == "USER_CANCELLED") {
				exitShell();
			}
			return;
		}

		
		// after entity select
		if (returningActivity instanceof EntitySelectActivity) {

			if (returnCode == Constants.ACTIVITY_COMPLETE) {
				int patID = ((Integer) returnVals
						.get(EntitySelectActivity.ENTITY_ID_KEY)).intValue();
				context.setElement("PATIENT_ID", new Integer(patID));
				
				System.out.println("Patient " + patID + " selected");

				launchActivity(new FormListActivity(this, "Forms List"),
						context);
				return;
			} 

			if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
				String action = (String) returnVals.get("action");
				if (EntitySelectActivity.ACTION_NEW_ENTITY.equals(action)) {
					launchActivity(new PatientEntryActivity(this), context);
				}
				
				return;
			} 

			if (returnCode == Constants.ACTIVITY_CANCEL) {
				exitShell();
				return;
			}
			
			// TODO: should this throw exception here?
			return;
		}

		if (returningActivity instanceof FormListActivity) {
			String returnVal = (String) returnVals.get(FormListActivity.COMMAND_KEY);
			if (returnVal == Commands.CMD_SETTINGS) {
				launchActivity(new PropertyScreenActivity(this), context);
			} else if (returnVal == Commands.CMD_VIEW_DATA) {
				launchActivity(new ModelListActivity(this), context);
			} else if (returnVal == Commands.CMD_SELECT_XFORM) {
				launchFormEntryActivity(context, ((Integer) returnVals
						.get(FormListActivity.FORM_ID_KEY)).intValue(), -1);
			} else if (returnVal == Commands.CMD_EXIT) {
				// launchEntitySelectActivity(context);
				exitShell();
			} else if (returnVal == Commands.CMD_ADD_USER)
				launchActivity(new AddUserActivity(this), context);

			return;
		}

		if (returningActivity instanceof ModelListActivity) {
			Object returnVal = returnVals.get(ModelListActivity.returnKey);
			if (returnVal == ModelListActivity.CMD_MSGS) {
				launchFormTransportActivity(context,
						TransportContext.MESSAGE_VIEW);
			} else if (returnVal == ModelListActivity.CMD_EDIT) {
				launchFormEntryActivity(context, ((FormDef) returnVals
						.get("form")).getID(), ((DataModelTree) returnVals
						.get("data")).getId());
			} else if (returnVal == ModelListActivity.CMD_REVIEW) {
				launchFormReviewActivity(context, ((FormDef) returnVals
						.get("form")).getID(), ((DataModelTree) returnVals
						.get("data")).getId());
			} else if (returnVal == ModelListActivity.CMD_SEND) {
				launchFormTransportActivity(context,
						TransportContext.SEND_DATA, (DataModelTree) returnVals
								.get("data"));
			} else if (returnVal == ModelListActivity.CMD_SEND_ALL_UNSENT) {
				launchFormTransportActivity(context,
						TransportContext.SEND_MULTIPLE_DATA,
						(Vector) returnVals.get("data_vec"));
			} else if (returnVal == ModelListActivity.CMD_BACK) {
				launchActivity(new FormListActivity(this, "Forms List"),
						context);
			}
			return;
		} 

		if (returningActivity instanceof FormEntryActivity) {

			if (((Boolean) returnVals.get("FORM_COMPLETE")).booleanValue()) {
				launchFormTransportActivity(context,
						TransportContext.SEND_DATA, (DataModelTree) returnVals
								.get("DATA_MODEL"));
			} else {
				relaunchListActivity();
			}
			return;
		} 

		
		if (returningActivity instanceof FormReviewActivity) {
			if ("update".equals(returnVals.get(Commands.COMMAND_KEY))) {
				launchFormEntryActivity(context, ((Integer) returnVals
						.get("FORM_ID")).intValue(), ((Integer) returnVals
						.get("INSTANCE_ID")).intValue(), (FormIndex) returnVals
						.get("SELECTED_QUESTION"), new RMSRetreivalMethod());
			} else {
				relaunchListActivity();
			}
			return;
		} 

		if (returningActivity instanceof FormTransportActivity) {
			if (returnVals.get(FormTransportActivity.RETURN_KEY) == FormTransportActivity.NEW_DESTINATION) {
				TransportMethod transport = JavaRosaServiceProvider.instance()
						.getTransportManager().getTransportMethod(
								JavaRosaServiceProvider.instance()
										.getTransportManager()
										.getCurrentTransportMethod());
				IActivity activity = transport
						.getDestinationRetrievalActivity();
				activity.setShell(this);
				this.launchActivity(activity, context);
			} else {
				relaunchListActivity();
			}

			// what is this for?
			/*
			 * if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) { String
			 * returnVal =
			 * (String)returnVals.get(FormTransportActivity.RETURN_KEY);
			 * if(returnVal == FormTransportActivity.VIEW_MODELS) {
			 * currentActivity = this.modelActivity;
			 * this.modelActivity.start(context); } }
			 */
			
			return;
		} 

		
		if (returningActivity instanceof PatientEntryActivity) {
			if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
				FormDef def = (FormDef) returnVals
						.get(PatientEntryActivity.PATIENT_ENTRY_FORM_KEY);
				ReferenceRetrievalMethod method = new ReferenceRetrievalMethod();
				method.setFormDef(def);
				launchFormEntryActivity(context, -1, -1, null, method);
			}
			
			return;
		} 

		if (returningActivity instanceof AddUserActivity) {
			launchActivity(new FormListActivity(this, "Forms List"), context);
		}
	}

	/**
	 * @param suspendedActivity
	 * @param completingActivity
	 * @param returnCode
	 * @param returnVals
	 */
	private void workflowResume(IActivity suspendedActivity,
			IActivity completingActivity, String returnCode,
			Hashtable returnVals) {

		// default action
		Context newContext = new Context(context);
		newContext.addAllValues(returnVals);
		resumeActivity(suspendedActivity, newContext);
	}

	/**
	 * @param activity
	 * @param context
	 */
	private void launchActivity(IActivity activity, Context context) {
		if (activity instanceof FormListActivity
				|| activity instanceof ModelListActivity)
			this.mostRecentListActivity = activity;

		this.currentActivity = activity;
		activity.start(context);
	}

	/**
	 * @param activity
	 * @param context
	 */
	private void resumeActivity(IActivity activity, Context context) {
		this.currentActivity = activity;
		activity.resume(context);
	}

	/**
	 * @param context
	 * @param formID
	 * @param instanceID
	 */
	private void launchFormEntryActivity(Context context, int formID,
			int instanceID) {
		launchFormEntryActivity(context, formID, instanceID, null, null);
	}

	/**
	 * @param context
	 * @param formID
	 * @param instanceID
	 * @param selected
	 * @param method
	 */
	private void launchFormEntryActivity(Context context, int formID,
			int instanceID, FormIndex selected, IFormDefRetrievalMethod method) {
		FormEntryActivity entryActivity = new FormEntryActivity(this,
				new FormEntryViewFactory());
		FormEntryContext formEntryContext = new FormEntryContext(context);
		formEntryContext.setFormID(formID);
		formEntryContext.setFirstQuestionIndex(selected);
		if (instanceID != -1)
			formEntryContext.setInstanceID(instanceID);

		if (method != null) {
			entryActivity.setRetrievalMethod(method);
		} else {
			entryActivity.setRetrievalMethod(new RMSRetreivalMethod());
		}

		launchActivity(entryActivity, formEntryContext);
	}

	private void launchFormReviewActivity(Context context, int formID,
			int instanceID) {
		FormReviewActivity reviewActivity = new FormReviewActivity(this,
				new FormEntryViewFactory());
		FormEntryContext formEntryContext = new FormEntryContext(context);
		formEntryContext.setFormID(formID);
		formEntryContext.setReadOnly(true);
		if (instanceID != -1)
			formEntryContext.setInstanceID(instanceID);
		else {
			reviewActivity.setRetrievalMethod(new RMSRetreivalMethod());
		}

		launchActivity(reviewActivity, formEntryContext);

	}

	private void launchFormTransportActivity(Context context, String task,
			DataModelTree data) {
		FormTransportActivity formTransport = new FormTransportActivity(this);
		formTransport.setData(data); // why isn't this going in the context?
		TransportContext msgContext = new TransportContext(context);

		launchFormTransportActivity(formTransport, task, msgContext);
	}

	private void launchFormTransportActivity(Context context, String task,
			Vector multidata) {
		FormTransportActivity formTransport = new FormTransportActivity(this);
		TransportContext msgContext = new TransportContext(context);
		msgContext.setMultipleData(multidata);

		launchFormTransportActivity(formTransport, task, msgContext);
	}

	private void launchFormTransportActivity(Context context, String task) {
		FormTransportActivity formTransport = new FormTransportActivity(this);
		TransportContext msgContext = new TransportContext(context);

		launchFormTransportActivity(formTransport, task, msgContext);
	}

	private void launchFormTransportActivity(FormTransportActivity activity,
			String task, TransportContext context) {
		context.setRequestedTask(task);
		activity.setDataModelSerializer(new XFormSerializingVisitor());

		launchActivity(activity, context);
	}

	private void launchEntitySelectActivity(Context context) {
		EntitySelectActivity psa = new EntitySelectActivity(this,
				"Choose a Patient");
		PatientRMSUtility prms = (PatientRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(PatientRMSUtility.getUtilityName());
		EntitySelectContext esc = new EntitySelectContext(context);
		esc.setEntityProtoype(new PatientEntity());
		esc.setRMSUtility(prms);
		esc.setNewEntityIDKey(PatientEntryActivity.NEW_PATIENT_ID);

		launchActivity(psa, esc);
	}

	private void relaunchListActivity() {
		if (mostRecentListActivity instanceof FormListActivity) {
			launchActivity(new FormListActivity(this, "Forms List"), context);
		} else if (mostRecentListActivity instanceof ModelListActivity) {
			launchActivity(new ModelListActivity(this), context);
		} else {
			throw new IllegalStateException(
					"Trying to resume list activity when no most recent set");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.shell.IShell#activityCompeleted(org.javarosa.activity.IActivity
	 * )
	 */
	public void returnFromActivity(IActivity activity, String returnCode,
			Hashtable returnVals) {
		// activity.halt(); //i don't think this belongs here? the contract
		// reserves halt for unexpected halts;
		// an activity calling returnFromActivity isn't halting unexpectedly
		workflow(activity, returnCode, returnVals);
	}

	public boolean setDisplay(IActivity callingActivity, IView display) {
		if (callingActivity == this.currentActivity) {
			JavaRosaServiceProvider.instance().getDisplay().setView(display);
			return true;
		}
		// #debug error
		System.out.println("Activity: " + callingActivity
				+ " attempted, but failed, to set the display");

		return false;

	}

	public void setMIDlet(MIDlet midlet) {
		this.midlet = midlet;
	}

	public void exitShell() {
		this.midlet.notifyDestroyed();
	}

}
