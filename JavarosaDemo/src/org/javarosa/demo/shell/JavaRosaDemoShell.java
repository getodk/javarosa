package org.javarosa.demo.shell;

import java.util.Hashtable;

import javax.microedition.lcdui.Displayable;

import org.javarosa.core.JavaRosaPlatform;;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IModule;
import org.javarosa.core.api.IShell;
import org.javarosa.core.util.WorkflowStack;
import org.javarosa.demo.module.FormListModule;
import org.javarosa.demo.module.SplashScreenModule;

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
	
	Hashtable context;
	
	IModule currentModule;

	public JavaRosaDemoShell() {
		stack = new WorkflowStack(); 
		context = new Hashtable();
	}

	public void ExitShell() {

	}
	
	public void Run() {
		this.splashScreen = new SplashScreenModule(this, "/splash.gif");
		this.formModule = new FormListModule(this,"Forms List");
		
		this.splashScreen.start();
		currentModule = splashScreen;
	//	switchView(ViewTypes.FORM_LIST);
	}
	
	private void workflow(IModule lastModule, String cmd, Hashtable returnVals) {
		//TODO: parse any returnvals into context
		if(stack.size() != 0) {
			stack.pop().resume();
		}
		// TODO Auto-generated method stub
		if( lastModule == this.splashScreen ) {
			this.formModule.setContext(this.context);
			this.formModule.start();
			currentModule = formModule;
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.shell.IShell#moduleCompeleted(org.javarosa.module.IModule)
	 */
	public void returnFromModule(IModule module, String returnCode, Hashtable returnVals) {
		Hashtable moduleContext = module.halt();
		if(returnCode != Constants.MODULE_COMPLETE) {
			stack.push(module,moduleContext);
		}
		workflow(module, returnCode, returnVals);
	}

	public void setDisplay(IModule callingModule, Displayable display) {
		if(callingModule == currentModule) {
		JavaRosaPlatform.instance().getDisplay().setCurrent(display);
		}
	}
}
