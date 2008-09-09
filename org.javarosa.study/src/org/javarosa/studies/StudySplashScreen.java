package org.javarosa.studies;

import java.io.IOException;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.shell.StudyShell;

import de.enough.polish.ui.Screen;
import de.enough.polish.ui.splash.ApplicationInitializer;
import de.enough.polish.ui.splash.InitializerSplashScreen;


public class StudySplashScreen extends MIDlet implements ApplicationInitializer  {

	private StudyShell studyShell = null;
	private Screen mainScreen;
	private Display display;
	
	public StudySplashScreen(){
		
		studyShell = new StudyShell();
		
	}
	protected void destroyApp(boolean conditional) throws MIDletStateChangeException {
		// TODO Auto-generated method stub
		
	}

	
	protected void pauseApp() {
		// TODO Auto-generated method stub
		
	}

	
	protected void startApp() throws MIDletStateChangeException {
		
		this.display = Display.getDisplay(this);
		
		if(this.mainScreen != null){
			this.display.setCurrent(this.mainScreen);
		}
		else{
			try{
				
				Image image = Image.createImage("/splash.gif");
				
				int backgroundcolor = 0xFFFFFF;
				String loadMessage = "Loading Patient Activities...";
				int loadMessageColor = 0xFF0000;
				
				InitializerSplashScreen splash = new InitializerSplashScreen(
						this.display, image, backgroundcolor, loadMessage, loadMessageColor, this);
				
				this.display.setCurrent(splash);
			}
			catch(IOException excep){
				throw new MIDletStateChangeException("Unable to load splash creen");
			}
		}
		
		JavaRosaServiceProvider.instance().initialize();
		JavaRosaServiceProvider.instance().setDisplay(Display.getDisplay(this));
		
		studyShell.run();
		studyShell.setMIDlet(this);	
		
	}


	public Displayable initApp() {
		
		return this.mainScreen;
	}




}
