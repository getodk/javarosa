package org.javarosa.demo.shell;

import java.util.Hashtable;

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

	public JavaRosaDemoShell() {
		stack = new WorkflowStack(); 
		context = new Hashtable();
	}

	public void ExitShell() {

	}
	
	public void pushModuleAndRunCommand(IModule module, Hashtable context, String cmd) {
		//TODO: get context from method signature or from halt()?
		module.halt();
		stack.push(module,context);
		workflow(module, cmd);
	}
	public void Run() {
		this.splashScreen = new SplashScreenModule(this, "/splash.gif");
		this.formModule = new FormListModule(this,"Forms List");
		
		this.splashScreen.start();
	//	switchView(ViewTypes.FORM_LIST);
	}
	
	private void workflow(IModule lastModule, String cmd) {
		if(stack.size() != 0) {
			stack.pop().resume();
		}
		// TODO Auto-generated method stub
		if( lastModule == this.splashScreen ) {
			this.formModule.setContext(this.context);
			this.formModule.start();
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.shell.IShell#moduleCompeleted(org.javarosa.module.IModule)
	 */
	public void moduleCompleted(IModule module, Hashtable returnVals) {
		//TODO: parse any returnvals into context
		workflow(module, null);
	}

}
