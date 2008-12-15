package org.javarosa.user.activity;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.properties.IPropertyRules;

public class UpdateProfileActivity implements IActivity, CommandListener 
{
	private IShell parent;
	private Context context;
	private PropertyManager propertyManager;

	public UpdateProfileActivity(IShell parent) 
	{
		this.parent =parent;
	}

	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub
		
	}

	public void destroy() {
		// TODO Auto-generated method stub
		this.propertyManager = null;
	}


	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}


	public void halt() {
		// TODO Auto-generated method stub
		
	}


	public void resume(Context globalContext) {
		// TODO Auto-generated method stub
		
	}


	public void setShell(IShell shell) {
		// TODO Auto-generated method stub
		
	}

	public void start(Context context) {
		
		this.context = context;
		
		//prepare property manager
		this.propertyManager = JavaRosaServiceProvider.instance().getPropertyManager();
		
		//read profile contents
		String post = (String) context.getElement("posturl");
		String get = (String) context.getElement("geturl");
		String type = (String) context.getElement("viewtype");

        //if(propertyManager.checkValueAllowed("GetURL", get))System.out.println("safe to proceed");
        
        //write properties to RMS
		propertyManager.setProperty("PostURLlist", post);
		propertyManager.setProperty("GetURL",get);
		propertyManager.setProperty("ViewStyle", type);
		//System.out.println("properties set");
		parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,null);

	}


	public void commandAction(Command arg0, Displayable arg1) {
		// TODO Auto-generated method stub
		
	}
	
}