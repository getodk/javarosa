/**
 * 
 */
package org.javarosa.activity.splashscreen;

import java.io.IOException;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;

import de.enough.polish.ui.splash.ApplicationInitializer;
import de.enough.polish.ui.splash.InitializerSplashScreen;

/**
 * @author Brian DeRenzi
 *
 */
public class SplashScreenActivity implements IActivity, ApplicationInitializer {

	private IShell parent = null;
	private String picture = null;
	private int splashDelay = 2000;
	
	public SplashScreenActivity(IShell p, String pic) {
		this.parent = p;
		this.picture = pic;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.module.IModule#start()
	 */
	public void start(Context context) {
		try {
			// TODO Auto-generated method stub
			Image image = Image.createImage(this.picture);
			int backgroundColor = 0xFFFFFF;
			String readyMessage = "Press any key to continue...";
			//Set readyMessage = null to forward to the next
			//displayabe as soon as it's available
			int messageColor = 0xFF0000;
			InitializerSplashScreen splashScreen = new InitializerSplashScreen(JavaRosaServiceProvider.instance().getDisplay(),
					image, backgroundColor, readyMessage, messageColor, this);
			parent.setDisplay(this, splashScreen);
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
  		parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
  		
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
	
	public void contextChanged(Context globalContext) {
		//don't bother. we don't need context
	}
	
	public void halt() {
	}
	
	public void resume(Context context) {
		start(context);
	}
	
	public void destroy() {
		
	}
}
