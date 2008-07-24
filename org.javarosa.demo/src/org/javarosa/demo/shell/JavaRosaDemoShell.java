package org.javarosa.demo.shell;

import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.javarosa.communication.http.HttpTransportMethod;
import org.javarosa.communication.http.HttpTransportProperties;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.util.WorkflowStack;
import org.javarosa.demo.activity.SplashScreenModule;
import org.javarosa.demo.properties.DemoAppProperties;
import org.javarosa.formmanager.activity.FormEntryActivity;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.activity.FormListActivity;
import org.javarosa.formmanager.activity.FormTransportActivity;
import org.javarosa.formmanager.activity.ModelListActivity;
import org.javarosa.formmanager.properties.FormManagerProperties;
import org.javarosa.formmanager.utility.TransportContext;
import org.javarosa.formmanager.view.Commands;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.services.properties.activity.PropertyScreenActivity;
import org.javarosa.xform.util.XFormUtils;

/**
 * This is the shell for the JavaRosa demo that handles switching all of the views
 * @author Brian DeRenzi
 *
 */
public class JavaRosaDemoShell implements IShell {
	// List of views that are used by this shell
	MIDlet midlet;
	FormListActivity formListActivity = null;
	SplashScreenModule splashScreen = null;
	FormTransportActivity formTransport = null;
	ModelListActivity modelActivity = null;
	PropertyScreenActivity propertyActivity = null;
	FormEntryActivity entryActivity = null;
	
	WorkflowStack stack;
	
	Context context;
	
	IActivity currentActivity;

	public JavaRosaDemoShell() {
		stack = new WorkflowStack(); 
		context = new Context();
	}

	public void exitShell() {
		midlet.notifyDestroyed();
	}
	
	public void run() {
		init();
		this.splashScreen = new SplashScreenModule(this, "/splash.gif");
		this.formListActivity = new FormListActivity(this,"Forms List");
		this.formTransport = new FormTransportActivity(this);
		formTransport.setDataModelSerializer(new XFormSerializingVisitor());
		this.modelActivity = new ModelListActivity(this);
		this.entryActivity  = new FormEntryActivity(this, new FormEntryViewFactory());
		
		this.propertyActivity = new PropertyScreenActivity(this);
		
		currentActivity = splashScreen;
		this.splashScreen.start(context);
	//	switchView(ViewTypes.FORM_LIST);
	}
	
	private void init() {
		loadProperties();
		
		JavaRosaServiceProvider.instance().getTransportManager().registerTransportMethod(new HttpTransportMethod());
		
		DataModelTreeRMSUtility dataModel = new DataModelTreeRMSUtility(DataModelTreeRMSUtility.getUtilityName());
		FormDefRMSUtility formDef = new FormDefRMSUtility(FormDefRMSUtility.getUtilityName());
		formDef.addModelPrototype(new DataModelTree());
		formDef.addReferencePrototype(new XPathReference());

		// For now let's add the dummy form.
		if (formDef.getNumberOfRecords() == 0) {
			formDef.writeToRMS(XFormUtils
					.getFormFromResource("/hmis-a_draft.xhtml"));
			formDef.writeToRMS(XFormUtils
					.getFormFromResource("/hmis-b_draft.xhtml"));
			formDef.writeToRMS(XFormUtils
					.getFormFromResource("/shortform.xhtml"));
		}
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider()
				.registerRMSUtility(dataModel);
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider()
				.registerRMSUtility(formDef);
	}
	
	private void workflow(IActivity lastActivity, String returnCode, Hashtable returnVals) {
		//TODO: parse any returnvals into context
		if(stack.size() != 0) {
			IActivity activity = stack.pop();
			this.currentActivity = activity;
			activity.resume(context);
		}
		else {
			// TODO Auto-generated method stub
			if (lastActivity == this.splashScreen) {
				currentActivity = formListActivity;
				this.formListActivity.start(context);
			}
			if (lastActivity == this.modelActivity) {
				if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
					Object returnVal = returnVals.get(ModelListActivity.returnKey);
					if (returnVal == ModelListActivity.CMD_MSGS) {
						// Go to the FormTransport Activity look at messages.
						TransportContext msgContext = new TransportContext(
								context);
						msgContext.setRequestedTask(TransportContext.MESSAGE_VIEW);
						currentActivity = formTransport;
						formTransport.start(msgContext);
					}
				}
				if (returnCode == Constants.ACTIVITY_COMPLETE) {
					// A Model was selected for some purpose
					Object returnVal = returnVals
							.get(ModelListActivity.returnKey);
					if (returnVal == ModelListActivity.CMD_EDIT) {
						// Load the Form Entry Activity, and feed it the form data
						FormDef form = (FormDef) returnVals.get("form");
						DataModelTree data = (DataModelTree) returnVals.get("data");
						FormEntryContext newContext = new FormEntryContext(context);
						newContext.setFormID(form.getID());
						newContext.setInstanceID(data.getFormReferenceId());
						currentActivity = this.modelActivity;
						this.modelActivity.start(newContext);
					}
					if (returnVal == ModelListActivity.CMD_SEND) {
						DataModelTree data = (DataModelTree) returnVals.get("data");
						formTransport.setData(data);
						TransportContext msgContext = new TransportContext(
								context);
						msgContext.setRequestedTask(TransportContext.SEND_DATA);
						currentActivity = formTransport;
						formTransport.start(msgContext);
					}
				}
			}
			if (lastActivity == this.formTransport) {
				if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
					String returnVal = (String)returnVals.get(FormTransportActivity.RETURN_KEY);
					if(returnVal == FormTransportActivity.VIEW_MODELS) {
						currentActivity = this.modelActivity;
						this.modelActivity.start(context);
					}
				}
				if (returnCode == Constants.ACTIVITY_COMPLETE) {
					
				}
			}
			if (lastActivity == this.formListActivity) {
				if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
					String returnVal = (String)returnVals.get(FormListActivity.COMMAND_KEY);
					if(returnVal == Commands.CMD_VIEW_DATA) {
						currentActivity = this.modelActivity;
						this.modelActivity.start(context);
					}
					if(returnVal == Commands.CMD_SETTINGS) {
						currentActivity = this.propertyActivity;
						this.propertyActivity.start(context);
					}
					if(returnVal == Commands.CMD_SELECT_XFORM) {
//						FormDefRMSUtility def = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());
//						FormDef formDef = new FormDef();
//						int formId = ((Integer)returnVals.get(FormListActivity.FORM_ID_KEY)).intValue();
//						try {
//						def.retrieveFromRMS(formId, formDef);
//						}
//						catch (Exception e) {
//							e.printStackTrace();
//						}
//						DataModelTree data = (DataModelTree)formDef.getDataModel();
//						formTransport.setData(data);
//						TransportContext msgContext = new TransportContext(
//								context);
//						msgContext.setRequestedTask(TransportContext.SEND_DATA);
//						currentActivity = formTransport;
//						formTransport.start(msgContext);
						
						FormEntryContext newContext = new FormEntryContext(context);
						newContext.setFormID(((Integer)returnVals.get(FormListActivity.FORM_ID_KEY)).intValue());
						currentActivity = this.entryActivity;
						this.entryActivity.start(newContext);										
					}
				}
				if (returnCode == Constants.ACTIVITY_COMPLETE) {
					
				}
			}
			if (lastActivity == this.entryActivity) {
				if(returnCode == Constants.ACTIVITY_COMPLETE) {
					if (((Boolean)returnVals.get("FORM_COMPLETE")).booleanValue()) {
						DataModelTree data = (DataModelTree)returnVals.get("DATA_MODEL");
						formTransport.setData(data);
						TransportContext msgContext = new TransportContext(context);
						msgContext.setRequestedTask(TransportContext.SEND_DATA);
						currentActivity = formTransport;
						formTransport.start(msgContext);
					}
				}
			}
			if(currentActivity == lastActivity) {
				//We didn't launch anything. Go to default
				currentActivity = formListActivity;
				formListActivity.start(context);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.shell.IShell#activityCompeleted(org.javarosa.activity.IActivity)
	 */
	public void returnFromActivity(IActivity activity, String returnCode, Hashtable returnVals) {
		activity.halt();
		workflow(activity, returnCode, returnVals);
		if(returnCode == Constants.ACTIVITY_SUSPEND || returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
			stack.push(activity);
		}
	}

	public boolean setDisplay(IActivity callingActivity, Displayable display) {
		if(callingActivity == currentActivity) {
			JavaRosaServiceProvider.instance().getDisplay().setCurrent(display);
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
	
	private String initProperty(String propName, String defaultValue) {
		Vector propVal = JavaRosaServiceProvider.instance().getPropertyManager().getProperty(propName);
		if (propVal == null || propVal.size() == 0) {
			propVal = new Vector();
			propVal.addElement(defaultValue);
			JavaRosaServiceProvider.instance().getPropertyManager().setProperty(propName, propVal);
			//#if debug.output==verbose
			System.out.println("No default value for [" + propName
					+ "]; setting to [" + defaultValue + "]"); // debug
			//#endif
			return defaultValue;
		}
		return (String) propVal.elementAt(0);
	}
	
	private void loadProperties() {
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new JavaRosaPropertyRules());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new DemoAppProperties());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new HttpTransportProperties());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new FormManagerProperties());
		
		initProperty("DeviceID", genGUID(25));
		initProperty(HttpTransportProperties.POST_URL_LIST_PROPERTY, "http://openrosa.org/testsubmit.html");
		initProperty(HttpTransportProperties.POST_URL_PROPERTY, "http://openrosa.org/testsubmit.html");
	}

	//TODO: Put this in a utility method
	public static String genGUID(int len) {
		String guid = "";
		Random r = new Random();

		for (int i = 0; i < 25; i++) { // 25 == 128 bits of entropy
			guid += Integer.toString(r.nextInt(36), 36);
		}

		return guid.toUpperCase();
	}
}
