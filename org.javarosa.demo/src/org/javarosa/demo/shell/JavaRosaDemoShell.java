package org.javarosa.demo.shell;

import java.util.Hashtable;

import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.javarosa.communication.http.HttpTransportMethod;
import org.javarosa.communication.http.HttpTransportProperties;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaPlatform;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IModule;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormData;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.storage.FormDataRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.util.WorkflowStack;
import org.javarosa.demo.module.SplashScreenModule;
import org.javarosa.demo.properties.DemoAppProperties;
import org.javarosa.formmanager.activity.FormListModule;
import org.javarosa.formmanager.activity.FormTransportModule;
import org.javarosa.formmanager.activity.ModelListModule;
import org.javarosa.formmanager.utility.TransportContext;
import org.javarosa.formmanager.view.Commands;
import org.javarosa.services.properties.activity.PropertyModule;
import org.javarosa.xform.util.XFormUtils;

/**
 * This is the shell for the JavaRosa demo that handles switching all of the views
 * @author Brian DeRenzi
 *
 */
public class JavaRosaDemoShell implements IShell {
	// List of views that are used by this shell
	MIDlet runningAssembly;
	FormListModule formModule = null;
	SplashScreenModule splashScreen = null;
	FormTransportModule formTransport = null;
	ModelListModule modelModule = null;
	PropertyModule propertyModule = null;
	
	WorkflowStack stack;
	
	Context context;
	
	IModule currentModule;

	public JavaRosaDemoShell() {
		stack = new WorkflowStack(); 
		context = new Context();
	}

	public void exitShell() {
		runningAssembly.notifyDestroyed();
	}
	
	public void run() {
		init();
		System.out.println("done init");
		this.splashScreen = new SplashScreenModule(this, "/splash.gif");
		System.out.println("done splash init");
		this.formModule = new FormListModule(this,"Forms List");
		System.out.println("Done formlist init");
		this.formTransport = new FormTransportModule(this);
		this.modelModule = new ModelListModule(this);
		
		this.propertyModule = new PropertyModule(this);
		
		currentModule = splashScreen;
		this.splashScreen.start(context);
		System.out.println("Done with splashscreen start");
	//	switchView(ViewTypes.FORM_LIST);
	}
	
	private void init() {
		
		JavaRosaPlatform.instance().getPropertyManager().addRules(new JavaRosaPropertyRules());
		JavaRosaPlatform.instance().getPropertyManager().addRules(new DemoAppProperties());
		JavaRosaPlatform.instance().getPropertyManager().addRules(new HttpTransportProperties());
		
		JavaRosaPlatform.instance().getTransportManager().registerTransportMethod(new HttpTransportMethod());
		
		FormDataRMSUtility formData = new FormDataRMSUtility(FormDataRMSUtility.getUtilityName());
		FormDefRMSUtility formDef = new FormDefRMSUtility(FormDefRMSUtility.getUtilityName());

		System.out.println("Loading Forms");
		// For now let's add the dummy form.
		if (formDef.getNumberOfRecords() == 0) {
			formDef.writeToRMS(XFormUtils
					.getFormFromResource("/hmis-a_draft.xhtml"));
			formDef.writeToRMS(XFormUtils
					.getFormFromResource("/hmis-b_draft.xhtml"));
			formDef.writeToRMS(XFormUtils
					.getFormFromResource("/shortform.xhtml"));
		}
		System.out.println("Done Loading Forms");
		JavaRosaPlatform.instance().getStorageManager().getRMSStorageProvider()
				.registerRMSUtility(formData);
		JavaRosaPlatform.instance().getStorageManager().getRMSStorageProvider()
				.registerRMSUtility(formDef);
		System.out.println("Done registering");
	}
	
	private void workflow(IModule lastModule, String returnCode, Hashtable returnVals) {
		//TODO: parse any returnvals into context
		if(stack.size() != 0) {
			IModule module = stack.pop();
			this.currentModule = module;
			module.resume(context);
		}
		else {
			// TODO Auto-generated method stub
			if (lastModule == this.splashScreen) {
				currentModule = formModule;
				this.formModule.start(context);
			}
			if (lastModule == this.modelModule) {
				if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
					Object returnVal = returnVals.get(ModelListModule.returnKey);
					if (returnVal == ModelListModule.CMD_MSGS) {
						// Go to the FormTransport Module look at messages.
						TransportContext msgContext = new TransportContext(
								context);
						msgContext.setRequestedTask(TransportContext.MESSAGE_VIEW);
						currentModule = formTransport;
						formTransport.start(msgContext);
					}
				}
				if (returnCode == Constants.ACTIVITY_COMPLETE) {
					// A Model was selected for some purpose
					Object returnVal = returnVals
							.get(ModelListModule.returnKey);
					if (returnVal == ModelListModule.CMD_EDIT) {
						// Load the Form Entry Module, and feed it the form data
						FormDef form = (FormDef) returnVals.get("form");
						FormData data = (FormData) returnVals.get("data");
					}
					if (returnVal == ModelListModule.CMD_SEND) {
						FormData data = (FormData) returnVals.get("data");
						formTransport.setData(data);
						TransportContext msgContext = new TransportContext(
								context);
						msgContext.setRequestedTask(TransportContext.SEND_DATA);
						currentModule = formTransport;
						formTransport.start(msgContext);
					}
				}
			}
			if (lastModule == this.formTransport) {
				if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
					String returnVal = (String)returnVals.get(FormTransportModule.RETURN_KEY);
					if(returnVal == FormTransportModule.VIEW_MODELS) {
						currentModule = this.modelModule;
						this.modelModule.start(context);
					}
				}
				if (returnCode == Constants.ACTIVITY_COMPLETE) {
					
				}
			}
			if (lastModule == this.formModule) {
				if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
					String returnVal = (String)returnVals.get("command");
					if(returnVal == Commands.CMD_VIEW_DATA) {
						currentModule = this.modelModule;
						this.modelModule.start(context);
					}
					if(returnVal == Commands.CMD_SETTINGS) {
						currentModule = this.propertyModule;
						this.propertyModule.start(context);
					}
				}
				if (returnCode == Constants.ACTIVITY_COMPLETE) {
					
				}
			}
		}
		if(currentModule == lastModule) {
			//We didn't launch anything. Go to default
			currentModule = formModule;
			formModule.start(context);
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.shell.IShell#moduleCompeleted(org.javarosa.module.IModule)
	 */
	public void returnFromModule(IModule module, String returnCode, Hashtable returnVals) {
		module.halt();
		System.out.println("Module: " + module + " returned with code " + returnCode);
		workflow(module, returnCode, returnVals);
		if(returnCode == Constants.ACTIVITY_SUSPEND || returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
			stack.push(module);
		}
	}

	public void setDisplay(IModule callingModule, Displayable display) {
		if(callingModule == currentModule) {
			JavaRosaPlatform.instance().getDisplay().setCurrent(display);
		}
		else {
			System.out.println("Module: " + callingModule + " attempted, but failed, to set the display");
		}
	}
	
	public void setRunningAssembly(MIDlet assembly) {
		this.runningAssembly = assembly;
	}
}
