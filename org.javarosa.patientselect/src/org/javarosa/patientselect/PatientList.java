package org.javarosa.patientselect;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import org.javarosa.core.JavaRosaServiceProvider;

public class PatientList extends MIDlet  {
	
	private PatientListShell patientList = null;
		
	public PatientList(){
		
		System.out.println("Creating midlet in constructor");
		
		patientList = new PatientListShell();
		
		System.out.println("Midlet created");
	}
	
	public void startApp(){
		
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
}
