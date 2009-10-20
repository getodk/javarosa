/**
 * 
 */
package org.javarosa.splashscreen.api;

import javax.microedition.lcdui.Image;

import org.javarosa.core.api.State;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.splashscreen.api.transitions.SplashScreenTransitions;

import de.enough.polish.ui.Display;
import de.enough.polish.ui.splash.ApplicationInitializer;
import de.enough.polish.ui.splash.InitializerSplashScreen;

/**
 * @author ctsims
 *
 */
public class SplashScreenState implements State<SplashScreenTransitions>, ApplicationInitializer {
	
	private SplashScreenTransitions transitions;
	private final String picture;

	public SplashScreenState(String picture) {
		this.picture = picture;
	}
	
	public void enter(SplashScreenTransitions transitions) {
		this.transitions = transitions;
		
	}

	public void start() {
		//Set readyMessage = null to forward to the next
		//displayabe as soon as it's available
		String readyMessage = null;
		
		Image image = null;
		try {
			image = Image.createImage(this.picture);
		} catch (Exception e) {
			throw new RuntimeException("Busted splash screen image. Fix this");
		}
		int backgroundColor = 0xFFFFFF;

		int messageColor = 0xFF0000;
		final InitializerSplashScreen splashScreen = new InitializerSplashScreen(Display.getInstance(),
				image, backgroundColor, readyMessage, messageColor, this){};
		J2MEDisplay.setView(splashScreen);
	}

}
