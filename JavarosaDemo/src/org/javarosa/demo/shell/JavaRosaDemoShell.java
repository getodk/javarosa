package org.javarosa.demo.shell;

import java.util.Hashtable;

import org.javarosa.core.api.IModule;
import org.javarosa.core.api.IShell;
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

	public JavaRosaDemoShell() {

	}

	public void ExitShell() {

	}

	public void Run() {
		this.splashScreen = new SplashScreenModule(this, "/splash.gif");
		this.formModule = new FormListModule(this,"Forms List");
		
		this.splashScreen.start();
	//	switchView(ViewTypes.FORM_LIST);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.shell.IShell#moduleCompeleted(org.javarosa.module.IModule)
	 */
	public void moduleCompleted(IModule module, Hashtable context) {
		// TODO Auto-generated method stub
		if( module == this.splashScreen )
			this.formModule.start();
	}

}
