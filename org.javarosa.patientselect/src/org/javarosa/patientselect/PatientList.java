package org.javarosa.patientselect;

import java.io.IOException;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.patientselect.shell.PatientListShell;

import de.enough.polish.ui.Screen;
import de.enough.polish.ui.splash.ApplicationInitializer;
import de.enough.polish.ui.splash.InitializerSplashScreen;

public class PatientList extends MIDlet implements ApplicationInitializer

{
	
	private PatientListShell patientList = null;
	
	private Screen mainScreen;
	private Display display;
	
		
	public PatientList(){
		
		System.out.println("Creating midlet in constructor");
		
		patientList = new PatientListShell();
		
		System.out.println("Midlet created");
	}
	
	public void startApp() throws MIDletStateChangeException{
		
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
		
		patientList.run();
		patientList.setMIDlet(this);	
	
	}
	
	public void pauseApp()
	{
		
	}
	
	public void destroyApp(boolean conditional)
	{
		
	}

	public Displayable initApp() {
		return this.mainScreen;
	}
}

