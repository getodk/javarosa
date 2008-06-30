/**
 * 
 */
package org.javarosa.module;

import java.io.IOException;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

import org.javarosa.core.JavaRosaPlatform;
import org.javarosa.shell.IShell;
import org.javarosa.view.ReturnValue;

import de.enough.polish.ui.splash.ApplicationInitializer;
import de.enough.polish.ui.splash.InitializerSplashScreen;

/**
 * @author Brian DeRenzi
 *
 */
public class SplashScreenModule implements IModule, ApplicationInitializer {

	private IShell parent = null;
	private String picture = null;
	private int splashDelay = 2000;
	
	public SplashScreenModule(IShell p, String pic) {
		this.parent = p;
		this.picture = pic;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.module.IModule#start()
	 */
	public void start() {
		try {
			// TODO Auto-generated method stub
			Image image = Image.createImage(this.picture);
			int backgroundColor = 0xFFFFFF;
			String readyMessage = "Press any key to continue...";
			//Set readyMessage = null to forward to the next
			//displayabe as soon as it's available
			int messageColor = 0xFF0000;
			InitializerSplashScreen splashScreen = new InitializerSplashScreen(JavaRosaPlatform.getDisplay(),
					image, backgroundColor, readyMessage, messageColor, this);
			JavaRosaPlatform.showView(splashScreen);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.splash.ApplicationInitializer#initApp()
	 */
	public Displayable initApp() {
		// TODO Auto-generated method stub
		try{
  		  Thread.currentThread().sleep(this.splashDelay);
  		} catch(Exception e) {}
  		
  		// Tell the shell that the splash screen has completed
  		parent.moduleCompleted(this, null);
  		
  		// We're using our own architecture
		return null;
	}

	
	/**
	 * @return the splashDelay
	 */
	public int getSplashDelay() {
		return this.splashDelay;
	}

	/**
	 * @param splashDelay the splashDelay to set
	 */
	public void setSplashDelay(int splashDelay) {
		this.splashDelay = splashDelay;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.module.IModule#viewCompleted(org.javarosa.view.ReturnValue, int)
	 */
	public void viewCompleted(ReturnValue rv, int viewId) {
		// TODO Auto-generated method stub

	}

}
