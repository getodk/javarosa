package org.javarosa.patientselect;

import java.util.Hashtable;
import org.javarosa.core.*;
import org.javarosa.core.api.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Displayable;
import org.javarosa.core.JavaRosaServiceProvider;


public class PatientListShell implements IShell{

	 private MIDlet patientListMidlet;
	 private ListActivity patientListActivity = null;
	 private Context patientListContext;
	 private IActivity currentActivity = null;
	
	 String shellName = "PatientListShell";
	 
	public PatientListShell()
	{		
		patientListContext = new Context();
		patientListActivity = new ListActivity(this,shellName);
	}
	
	public void exitShell(){
		
		patientListMidlet.notifyDestroyed();
		System.out.println("exitCommand");
	}
	
	public void run(){
		
		System.out.println("Running midlet in Init<>");
		patientListActivity.start(patientListContext);
		
		
	}
	
    public void init(){
    	
    	System.out.println("Init Error!");
    	

	}
	
	public void setMIDlet(MIDlet midlet){
		
		this.patientListMidlet = midlet;
	}
	
	public void returnFromActivity(IActivity i, String s, Hashtable h){
		
		i.resume(patientListContext);
	}
	
	public boolean setDisplay(IActivity callingActivity, Displayable display)
	{
		if(callingActivity == currentActivity) {
			
			JavaRosaServiceProvider.instance().getDisplay().setCurrent(display);
			
			return true;
		}
		else 
		{
			System.out.println("Activity: " + callingActivity + " attempted, but failed, to set the display");
			
			return false;
		}
	}
}


