package org.javarosa.patientselect.shell;

import java.util.Hashtable;
import org.javarosa.core.*;
import org.javarosa.core.api.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Displayable;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.patientselect.activity.PatientListActivity;

public class PatientListShell {
	
	 	private MIDlet patientListMidlet;
		// private ListActivity patientListActivity = null;
		 private PatientListActivity patientListActivity = null;
		 private Context patientListContext;
		 private IActivity currentActivity = null;
		
		 String shellName = "PatientListShell";
		 
		public PatientListShell()
		{		
			patientListContext = new Context();
			
			patientListActivity = new PatientListActivity(shellName);
		}
		
		public void exitShell(){
			
			patientListMidlet.notifyDestroyed();
		}
		
		public void run(){
			
			//#if debug.output==verbose || debug.output==exception
			System.out.println("Running midlet in Init<>");
			
			//#endif
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
				//#if debug.output==verbose || debug.output==exception
				System.out.println("Activity: " + callingActivity + " attempted, but failed, to set the display");
				//#endif
				
				return false;
			}
		}

}
