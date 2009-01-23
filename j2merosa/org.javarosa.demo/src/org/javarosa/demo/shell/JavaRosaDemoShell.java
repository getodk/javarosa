package org.javarosa.demo.shell;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.core.util.WorkflowStack;
import org.javarosa.demo.properties.DemoAppProperties;
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
import org.javarosa.j2me.storage.rms.RMSStorageModule;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.patient.PatientModule;
import org.javarosa.patient.entry.activity.PatientEntryActivity;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.select.activity.PatientEntity;
import org.javarosa.patient.select.activity.CommCarePatientEntity;
import org.javarosa.patient.select.activity.PatientSelectActivity;
import org.javarosa.patient.storage.PatientRMSUtility;
import org.javarosa.referral.ReferralModule;
import org.javarosa.services.properties.activity.PropertyScreenActivity;
import org.javarosa.user.activity.AddUserActivity;
import org.javarosa.user.activity.LoginActivity;
import org.javarosa.user.model.User;
import org.javarosa.xform.util.XFormUtils;
/**
 * This is the shell for the JavaRosa demo that handles switching all of the views
 * @author Brian DeRenzi
 *
 */
public class JavaRosaDemoShell implements IShell {
	// List of views that are used by this shell
	MIDlet midlet;

	WorkflowStack stack;
	Context context;

	IActivity currentActivity;
	IActivity mostRecentListActivity; //should never be accessed, only checked for type

	public JavaRosaDemoShell() {
		stack = new WorkflowStack();
		context = new Context();
	}

	public void exitShell() {
		midlet.notifyDestroyed();
	}

	public void run() {
		init();
		workflow(null, null, null);
	}

	private void init() {
		loadModules();
		loadProperties();
		
		FormDefRMSUtility formDef = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());
		if (formDef.getNumberOfRecords() == 0) {
			formDef.writeToRMS(XFormUtils.getFormFromResource("/hmis-a_draft.xhtml"));
			formDef.writeToRMS(XFormUtils.getFormFromResource("/hmis-b_draft.xhtml"));
			formDef.writeToRMS(XFormUtils.getFormFromResource("/shortform.xhtml"));
			formDef.writeToRMS(XFormUtils.getFormFromResource("/CHMTTL.xhtml"));
			formDef.writeToRMS(XFormUtils.getFormFromResource("/condtest.xhtml"));
		}
		initTestPatients((PatientRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(PatientRMSUtility.getUtilityName()));
	}
	
	private void loadModules() {
		new RMSStorageModule().registerModule(context);
		new XFormsModule().registerModule(context);
		new CoreModelModule().registerModule(context);
		new HttpTransportModule().registerModule(context);
		new FormManagerModule().registerModule(context);
		new ExtendedWidgetsModule().registerModule(context);
		new CommunicationUIModule().registerModule(context);
		new ReferralModule().registerModule(context);
		new PatientModule().registerModule(context);
	}
	
	private void workflow(IActivity lastActivity, String returnCode, Hashtable returnVals) {
		if (returnVals == null)
			returnVals = new Hashtable(); //for easier processing

		if (lastActivity != currentActivity) {
			System.out.println("Received 'return' event from activity other than the current activity" +
					" (such as a background process). Can't handle this yet.  Saw: " +
					lastActivity + " but expecting: " + currentActivity);
			return; 
		}

		if (returnCode == Constants.ACTIVITY_SUSPEND || returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
			stack.push(lastActivity);
			workflowLaunch(lastActivity, returnCode, returnVals);
		} else {
			if (stack.size() > 0) {
				workflowResume(stack.pop(), lastActivity, returnCode, returnVals);
			} else {
				workflowLaunch(lastActivity, returnCode, returnVals);
				if (lastActivity != null)
					lastActivity.destroy();
			}
		}
	}	

	private void workflowLaunch (IActivity returningActivity, String returnCode, Hashtable returnVals) {
		if (returningActivity == null) {

			launchActivity(new SplashScreenActivity(this, "/splash.gif"), context);

		} else if (returningActivity instanceof SplashScreenActivity) {
			
			//#if javarosa.dev.shortcuts
			
			launchPatientSelectActivity(context);
			
			//#else
			
			String passwordVAR = midlet.getAppProperty("username");
            String usernameVAR = midlet.getAppProperty("password");
            if ((usernameVAR == null) || (passwordVAR == null)) {
            	context.setElement("username","admin");
            	context.setElement("password","p");
            } else {
            	context.setElement("username",usernameVAR);
            	context.setElement("password",passwordVAR);
            }
            context.setElement("authorization", "admin");
			launchActivity(new LoginActivity(this, "Login"), context);

			//#endif

		} else if (returningActivity instanceof LoginActivity) {

			Object returnVal = returnVals.get(LoginActivity.COMMAND_KEY);
			if (returnVal == "USER_VALIDATED") {
				User user = (User)returnVals.get(LoginActivity.USER);
				if (user != null){
					context.setCurrentUser(user.getUsername());
					context.setElement("USER", user);
				}

				launchPatientSelectActivity(context);
			} else if (returnVal == "USER_CANCELLED") {
				exitShell();
			}
						
		} else if (returningActivity instanceof PatientSelectActivity) {
			
			if (returnCode == Constants.ACTIVITY_COMPLETE) {
				int patID = ((Integer)returnVals.get(PatientSelectActivity.ENTITY_ID_KEY)).intValue();
				context.setElement("PATIENT_ID", new Integer(patID));
				System.out.println("Patient " + patID + " selected");
				
				launchActivity(new FormListActivity(this, "Forms List"), context);				
			} else if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
				String action = (String)returnVals.get("action");
				if (PatientSelectActivity.ACTION_NEW_ENTITY.equals(action)) {
					launchActivity(new PatientEntryActivity(this), context);					
				}
			} else if (returnCode == Constants.ACTIVITY_CANCEL) {
				exitShell();
			}
			
		} else if (returningActivity instanceof FormListActivity) {

			String returnVal = (String)returnVals.get(FormListActivity.COMMAND_KEY);
			if (returnVal == Commands.CMD_SETTINGS) {
				launchActivity(new PropertyScreenActivity(this), context);
			} else if (returnVal == Commands.CMD_VIEW_DATA) {
				launchActivity(new ModelListActivity(this), context);
			} else if (returnVal == Commands.CMD_SELECT_XFORM) {
				launchFormEntryActivity(context, ((Integer)returnVals.get(FormListActivity.FORM_ID_KEY)).intValue(), -1);
			} else if (returnVal == Commands.CMD_EXIT) 
				launchPatientSelectActivity(context);
			  else if (returnVal == Commands.CMD_ADD_USER) 
				launchActivity( new AddUserActivity(this),context);
			
		} else if (returningActivity instanceof ModelListActivity) {
			Object returnVal = returnVals.get(ModelListActivity.returnKey);
			if (returnVal == ModelListActivity.CMD_MSGS) {
				launchFormTransportActivity(context, TransportContext.MESSAGE_VIEW);
			} else if (returnVal == ModelListActivity.CMD_EDIT) {
				launchFormEntryActivity(context, ((FormDef)returnVals.get("form")).getID(),
						((DataModelTree)returnVals.get("data")).getId());
			} else if (returnVal == ModelListActivity.CMD_REVIEW) {
				launchFormReviewActivity(context, ((FormDef)returnVals.get("form")).getID(),
						((DataModelTree)returnVals.get("data")).getId());
			} else if (returnVal == ModelListActivity.CMD_SEND) {
				launchFormTransportActivity(context, TransportContext.SEND_DATA, (DataModelTree)returnVals.get("data"));
			} else if (returnVal == ModelListActivity.CMD_SEND_ALL_UNSENT) {
				launchFormTransportActivity(context, TransportContext.SEND_MULTIPLE_DATA, (Vector)returnVals.get("data_vec"));
			} else if (returnVal == ModelListActivity.CMD_BACK) {
				launchActivity(new FormListActivity(this, "Forms List"), context);
			}

		} else if (returningActivity instanceof FormEntryActivity) {

			if (((Boolean)returnVals.get("FORM_COMPLETE")).booleanValue()) {
				launchFormTransportActivity(context, TransportContext.SEND_DATA, (DataModelTree)returnVals.get("DATA_MODEL"));
			} else {
				relaunchListActivity();
			}

		} else if (returningActivity instanceof FormReviewActivity) {
			if ("update".equals(returnVals.get(Commands.COMMAND_KEY))) {
				launchFormEntryActivity(context, ((Integer)returnVals.get("FORM_ID")).intValue(),
						((Integer)returnVals.get("INSTANCE_ID")).intValue(),(FormIndex)returnVals.get("SELECTED_QUESTION"),new RMSRetreivalMethod());
			} else {
				relaunchListActivity();
			}

		}  else if (returningActivity instanceof FormTransportActivity) {
			if(returnVals.get(FormTransportActivity.RETURN_KEY ) == FormTransportActivity.NEW_DESTINATION) {
				TransportMethod transport = JavaRosaServiceProvider.instance().getTransportManager().getTransportMethod(JavaRosaServiceProvider.instance().getTransportManager().getCurrentTransportMethod());
				IActivity activity = transport.getDestinationRetrievalActivity();
				activity.setShell(this);
				this.launchActivity(activity, context);
			} else {
				relaunchListActivity();
			}

			//what is this for?
			/*if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
				String returnVal = (String)returnVals.get(FormTransportActivity.RETURN_KEY);
				if(returnVal == FormTransportActivity.VIEW_MODELS) {
					currentActivity = this.modelActivity;
					this.modelActivity.start(context);
				}
			}*/
		} else if (returningActivity instanceof PatientEntryActivity) {
			if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
				FormDef def = (FormDef)returnVals.get(PatientEntryActivity.PATIENT_ENTRY_FORM_KEY);
				ReferenceRetrievalMethod method = new ReferenceRetrievalMethod();
				method.setFormDef(def);
				launchFormEntryActivity(context, -1, -1, null, method);
			}
		} else if (returningActivity instanceof AddUserActivity) { 
		 	launchActivity(new FormListActivity(this, "Forms List"), context);
		}
	}

	private void workflowResume (IActivity suspendedActivity, IActivity completingActivity,
								 String returnCode, Hashtable returnVals) {

		//default action
		Context newContext = new Context(context);
		newContext.addAllValues(returnVals);
		resumeActivity(suspendedActivity, newContext);
	}

	private void launchActivity (IActivity activity, Context context) {
		if (activity instanceof FormListActivity || activity instanceof ModelListActivity)
			mostRecentListActivity = activity;

		currentActivity = activity;
		activity.start(context);
	}

	private void resumeActivity (IActivity activity, Context context) {
		currentActivity = activity;
		activity.resume(context);
	}

	private void launchFormEntryActivity (Context context, int formID, int instanceID) {
		launchFormEntryActivity(context, formID, instanceID, null, null);
	}
	
	private void launchFormEntryActivity (Context context, int formID, int instanceID, FormIndex selected, IFormDefRetrievalMethod method) {
		FormEntryActivity entryActivity = new FormEntryActivity(this, new FormEntryViewFactory());
		FormEntryContext formEntryContext = new FormEntryContext(context);
		formEntryContext.setFormID(formID);
		formEntryContext.setFirstQuestionIndex(selected);
		if (instanceID != -1)
			formEntryContext.setInstanceID(instanceID);

		if(method != null) {
			entryActivity.setRetrievalMethod(method);
		} else {
			entryActivity.setRetrievalMethod(new RMSRetreivalMethod());
		}
		
		launchActivity(entryActivity, formEntryContext);
	}
	
	private void launchFormReviewActivity (Context context, int formID, int instanceID) {
		FormReviewActivity reviewActivity = new FormReviewActivity(this, new FormEntryViewFactory());
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
	
	private void launchFormTransportActivity (Context context, String task, DataModelTree data) {
		FormTransportActivity formTransport = new FormTransportActivity(this);
		formTransport.setData(data); //why isn't this going in the context?
		TransportContext msgContext = new TransportContext(context);

		launchFormTransportActivity(formTransport, task, msgContext);
	}
	private void launchFormTransportActivity (Context context, String task, Vector multidata) {
		FormTransportActivity formTransport = new FormTransportActivity(this);
		TransportContext msgContext = new TransportContext(context);
		msgContext.setMultipleData(multidata);

		launchFormTransportActivity(formTransport, task, msgContext);
	}

	private void launchFormTransportActivity (Context context, String task) {
		FormTransportActivity formTransport = new FormTransportActivity(this);
		TransportContext msgContext = new TransportContext(context);

		launchFormTransportActivity(formTransport, task, msgContext);
	}
	
	private void launchFormTransportActivity(FormTransportActivity activity, String task, TransportContext context) {
		context.setRequestedTask(task);
		activity.setDataModelSerializer(new XFormSerializingVisitor());

		launchActivity(activity, context);
	}
	
	private void launchPatientSelectActivity (Context context) {
		PatientSelectActivity psa = new PatientSelectActivity(this, "Choose a Patient");
		PatientRMSUtility prms = (PatientRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(PatientRMSUtility.getUtilityName());
		context.setElement(PatientSelectActivity.ENTITY_PROTO_KEY, new /*CommCare*/PatientEntity());
		context.setElement(PatientSelectActivity.ENTITY_RMS_KEY, prms);
		context.setElement(PatientSelectActivity.NEW_ENTITY_ID_KEY_KEY, PatientEntryActivity.NEW_PATIENT_ID);
		
		launchActivity(psa, context);
	}
	
	private void relaunchListActivity () {
		if (mostRecentListActivity instanceof FormListActivity) {
			launchActivity(new FormListActivity(this, "Forms List"), context);
		} else if (mostRecentListActivity instanceof ModelListActivity) {
			launchActivity(new ModelListActivity(this), context);
		} else {
			throw new IllegalStateException("Trying to resume list activity when no most recent set");
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.shell.IShell#activityCompeleted(org.javarosa.activity.IActivity)
	 */
	public void returnFromActivity(IActivity activity, String returnCode, Hashtable returnVals) {
		//activity.halt(); //i don't think this belongs here? the contract reserves halt for unexpected halts;
						   //an activity calling returnFromActivity isn't halting unexpectedly
		workflow(activity, returnCode, returnVals);
	}

	public boolean setDisplay(IActivity callingActivity, IView display) {
		if(callingActivity == currentActivity) {
			JavaRosaServiceProvider.instance().getDisplay().setView(display);
			return true;
		}
		else {
			//#if debug.output==verbose
			System.out.println("Activity: " + callingActivity + " attempted, but failed, to set the display");
			//#endif
			return false;
		}
	}

	public void setMIDlet(MIDlet midlet) {
		this.midlet = midlet;
	}

	private void loadProperties() {
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new JavaRosaPropertyRules());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new DemoAppProperties());

		PropertyUtils.initializeProperty("DeviceID", PropertyUtils.genGUID(25));
		PropertyUtils.initializeProperty(HttpTransportProperties.POST_URL_LIST_PROPERTY, "http://dev.cell-life.org/javarosa/web/limesurvey/admin/post2lime.php");
		PropertyUtils.initializeProperty(HttpTransportProperties.POST_URL_PROPERTY, "http://dev.cell-life.org/javarosa/web/limesurvey/admin/post2lime.php");
	}

	private void initTestPatients (PatientRMSUtility prms) {
		if (prms.getNumberOfRecords() == 0) {
			//read test patient data into byte buffer
			byte[] buffer = new byte[4000]; //make sure buffer is big enough for entire file; it will not grow to file size (budget 40 bytes per patient)
			InputStream is = System.class.getResourceAsStream("/testpatients");
			if (is == null) {
				System.out.println("Test patient data not found.");
				return;
			}
			
			int len = 0;
			try {
				len = is.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//copy byte buffer into character string
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < len; i++)
				sb.append((char)buffer[i]);
			buffer = null;
			String data = sb.toString();
			
			//split lines
			Vector lines = DateUtils.split(data, "\n", false);
			data = null;
			
			//parse patients
			for (int i = 0; i < lines.size(); i++) {
				Vector pat = DateUtils.split((String)lines.elementAt(i), "|", false);
				if (pat.size() != 6)
					continue;
				
				Patient p = new Patient();
				p.setFamilyName((String)pat.elementAt(0));
				p.setGivenName((String)pat.elementAt(1));
				p.setMiddleName((String)pat.elementAt(2));
				p.setPatientIdentifier((String)pat.elementAt(3));
				p.setGender("m".equals((String)pat.elementAt(4)) ? Patient.SEX_MALE : Patient.SEX_FEMALE);
				p.setBirthDate(new Date((new Date()).getTime() - 86400000l * Integer.parseInt((String)pat.elementAt(5))));
			
				prms.writeToRMS(p);				
			}
		}
	}
}
