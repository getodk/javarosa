package org.javarosa.formmanager.activity;

import java.util.Hashtable;

import javax.microedition.lcdui.AlertType;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;

public class MemoryCheckActivity implements IActivity
{
    private DataModelTreeRMSUtility dataModelRMSUtility;
    private FormDefRMSUtility formDefRMSUtility;
    private IShell mainShell;
    public static final String ACTIVITY_ID_KEY = "activity_id";
    Context theContext;
    
	public MemoryCheckActivity(IShell mainShell)
	{
        this.mainShell = mainShell;
        this.dataModelRMSUtility = (DataModelTreeRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(DataModelTreeRMSUtility.getUtilityName());
		this.formDefRMSUtility = (FormDefRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(FormDefRMSUtility.getUtilityName());
		System.out.println("CURRENT SPACE: " + dataModelRMSUtility.computeSpace());
		System.out.println("CURRENT FORMS SPACE" + formDefRMSUtility.computeSpace());
	}
	public void contextChanged(Context globalContext) {
		theContext.mergeInContext(globalContext);
		
	}


	public void destroy() {
		// TODO Auto-generated method stub
		
	}


	public Context getActivityContext() {
		return theContext;
	}


	public void halt() {
		// TODO Auto-generated method stub
		
	}


	public void resume(Context globalContext) {
		this.contextChanged(globalContext);
		this.createView();
		
	}


	public void start(Context context) {
		theContext = context;
		this.createView();
		
	}
	
    public void createView(){
    	//check memory status alert user
		if(dataModelRMSUtility.computeSpace() <= 0.05 || formDefRMSUtility.computeSpace() <= 0.05) //less than 5% of memory left
		{
			String info = "Your phone memory is low and you may not be able to save more forms";
			javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("Memory low!", info, null, AlertType.ERROR);
			a.setTimeout(2000);//calm down and read the alert first
			mainShell.setDisplay(this, a);
		}

		Hashtable returnArgs = new Hashtable();
		returnArgs.put(ACTIVITY_ID_KEY, "MemoryCheckDone");
		mainShell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
		//can add commands to give user option to take action here...
    }
    /*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.mainShell = shell;
	}
}