/**
 * 
 */
package org.javarosa.splashscreen.api;

import javax.microedition.lcdui.Image;

import org.javarosa.core.api.State;
import org.javarosa.core.util.TrivialTransitions;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Display;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.splash.ApplicationInitializer;
import de.enough.polish.ui.splash.InitializerSplashScreen;

/**
 * @author ctsims
 *
 */
public abstract class SplashScreenState implements TrivialTransitions, State, ApplicationInitializer {
	
	protected String picture;
	protected int delay;
	
	protected int backgroundColor = 0xFFFFFF;
	protected int messageColor = 0xFF0000;
	
	public SplashScreenState (String picture) {
		this(picture, 2000);
	}
	
	public SplashScreenState (String picture, int delay) {
		this.picture = picture;
		this.delay = delay;
	}

	public void start() {
		Image image = null;
		try {
			image = Image.createImage(this.picture);
		} catch (Exception e) {
			throw new RuntimeException("Busted splash screen image. Fix this");
		}

		InitializerSplashScreen splashScreen = new InitializerSplashScreen(Display.getInstance(),
			image, backgroundColor, null, messageColor, this);
		J2MEDisplay.setView(splashScreen);
	}
	
	public Displayable initApp() {
		try{
			Thread.sleep(this.delay);
  		} catch (Exception e) { }

  		//we could be doing, like... actual initialization here
  		
  		done();
  		
  		//we will set the display ourselves
		return null;
		//i'm not sure this is ideal. if the splash screen sets the display to 'null' (as returned by this
		//function), that could be interpreted as a request for the app to be backgrounded, according to
		//J2ME docs
	}
	
}
