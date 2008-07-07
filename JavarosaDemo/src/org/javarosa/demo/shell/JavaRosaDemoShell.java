package org.javarosa.demo.shell;

import java.util.Hashtable;

import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaPlatform;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IModule;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.storage.FormDataRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.util.WorkflowStack;
import org.javarosa.demo.module.SplashScreenModule;
import org.javarosa.formmanager.activity.FormListModule;
import org.javarosa.xform.util.XFormUtils;

/**
 * This is the shell for the JavaRosa demo that handles switching all of the views
 * @author Brian DeRenzi
 *
 */
public class JavaRosaDemoShell implements IShell {
	// List of views that are used by this shell
	FormListModule formModule = null;
	SplashScreenModule splashScreen = null;
	
	WorkflowStack stack;
	
	Context context;
	
	IModule currentModule;

	public JavaRosaDemoShell() {
		stack = new WorkflowStack(); 
		context = new Context();
	}

	public void exitShell() {

	}
	
	public void run() {
		init();
		System.out.println("done init");
		this.splashScreen = new SplashScreenModule(this, "/splash.gif");
		System.out.println("done splash init");
		this.formModule = new FormListModule(this,"Forms List");
		System.out.println("Done formlist init");
		
		currentModule = splashScreen;
		this.splashScreen.start(context);
		System.out.println("Done with splashscreen start");
	//	switchView(ViewTypes.FORM_LIST);
	}
	
	private void init() {

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
	
	private void workflow(IModule lastModule, String cmd, Hashtable returnVals) {
		//TODO: parse any returnvals into context
		if(stack.size() != 0) {
			stack.pop().resume(context);
		}
		// TODO Auto-generated method stub
		if( lastModule == this.splashScreen ) {
			currentModule = formModule;
			this.formModule.start(context);
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.shell.IShell#moduleCompeleted(org.javarosa.module.IModule)
	 */
	public void returnFromModule(IModule module, String returnCode, Hashtable returnVals) {
		module.halt();
		if(returnCode == Constants.ACTIVITY_SUSPEND || returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
			stack.push(module);
		}
		workflow(module, returnCode, returnVals);
	}

	public void setDisplay(IModule callingModule, Displayable display) {
		if(callingModule == currentModule) {
			JavaRosaPlatform.instance().getDisplay().setCurrent(display);
		}
		else {
			System.out.println("Module: " + callingModule + " attempted, but failed, to set the display");
		}
	}
}
