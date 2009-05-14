/**
 *
 */
package org.javarosa.formmanager.activity;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.storage.FormDefMetaData;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.util.Map;
import org.javarosa.formmanager.view.Commands;
import org.javarosa.formmanager.view.FormList;
import org.javarosa.formmanager.view.ViewTypes;
import org.javarosa.user.model.User;


/**
 * @author Brian DeRenzi
 *
 */
public class FormListActivity implements IActivity {

	public static final String COMMAND_KEY = "command";
	public static final String FORM_ID_KEY = "form_id";
	//refactor these out.
	public final Command CMD_ADD_USER = new Command("Add User", Command.SCREEN, 5);
	public final Command CMD_SAVE = new Command("Save", Command.OK, 1);
	public final Command CMD_CANCEL = new Command("Cancel",Command.BACK, 1);
	
	private FormList formsList = null;
	private Map listOfForms = null;
	private Vector formIDs = null;
	private IShell parent = null;
	private Vector positionToId = null;

	private Context context;

	public Vector customCommands = new Vector();
	
	public FormListActivity(IShell p, String title) {
		this.parent = p;
		this.formsList = new FormList(this,title);
	}

	public void start(Context context) {
		this.context = context;
		
		
		this.listOfForms = new Map();
		this.formIDs = new Vector();
		getXForms();

		if(context.getElement("USER")!=null){
			User loggedInUser = (User)context.getElement("USER");
			if (loggedInUser.isAdminUser())
				this.formsList.addCommand(CMD_ADD_USER);
		}
		this.positionToId = this.formsList.loadView(listOfForms);
		parent.setDisplay(this, this.formsList);
	}
	
	public Context getContext()	{
		return this.context;
	}


	public void viewCompleted(Hashtable returnvals, int view_ID) {
		// Determine which view just completed and act accordingly
		switch(view_ID) {
		case ViewTypes.FORM_LIST:
			processFormsList(returnvals);
			break;
		}
	}

	private void processFormsList(Hashtable returnvals) {
		Enumeration en = returnvals.keys();
		while(en.hasMoreElements()) {
			String cmd = (String)en.nextElement();

			if( cmd == Commands.CMD_SELECT_XFORM){
				int selectedForm = ((Integer)returnvals.get(Commands.CMD_SELECT_XFORM)).intValue();
				int formId = ((Integer)positionToId.elementAt(selectedForm)).intValue();
				//#if debug.output==verbose
				System.out.println("Selected form: " + formIDs.elementAt(formId));
				//#endif
				FormDefMetaData meta = (FormDefMetaData)formIDs.elementAt(formId);

				Hashtable returnArgs = new Hashtable();
				returnArgs.put(FORM_ID_KEY, new Integer(meta.getRecordId()));
				returnArgs.put(COMMAND_KEY, Commands.CMD_SELECT_XFORM);
				parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs );

				break;

			} else if (cmd == Commands.CMD_ADD_USER) {
			   Hashtable returnArgs = new Hashtable(); 
               returnArgs.put(COMMAND_KEY, Commands.CMD_ADD_USER); 
               parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs ); 
			} else if (cmd == Commands.CMD_EXIT) {
				Hashtable returnArgs = new Hashtable();
				returnArgs.put(COMMAND_KEY, Commands.CMD_EXIT);
				parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs );
			} else if (cmd == Commands.CMD_VIEW_DATA) {
				Hashtable returnArgs = new Hashtable();
				returnArgs.put(COMMAND_KEY, Commands.CMD_VIEW_DATA);
				parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs );
			} else if (cmd == Commands.CMD_SETTINGS) {
				Hashtable returnArgs = new Hashtable();
				returnArgs.put(COMMAND_KEY, Commands.CMD_SETTINGS);
				parent.returnFromActivity(this, Constants.ACTIVITY_SUSPEND, returnArgs );
			} else if (cmd == Commands.CMD_GET_NEW_FORM) {
				Hashtable returnArgs = new Hashtable();
				returnArgs.put(COMMAND_KEY, Commands.CMD_GET_NEW_FORM);
				// Using activity complete means that the shell must actively recreate the
				// forms list.  I think this is best to ensure that the list is updated
				// - Brian DeRenzi 18 Aug 2008
				parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
			} else if (cmd == Commands.CMD_DELETE_FORM) {
				int selectedForm = ((Integer)returnvals.get(Commands.CMD_DELETE_FORM)).intValue();
				int formId = ((Integer)positionToId.elementAt(selectedForm)).intValue();
				//#if debug.output==verbose
				System.out.println("Deleting form: " + formIDs.elementAt(formId));
				//#endif
				FormDefMetaData meta = (FormDefMetaData)formIDs.elementAt(formId);
				FormDefRMSUtility rms = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());

				// Delete!
				rms.deleteRecord(meta.getRecordId());

				// refresh
				this.start(this.context);
			} else {
				Hashtable returnArgs = new Hashtable();
				returnArgs.put(COMMAND_KEY, cmd);
				parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs );
			}
		}
	}

	private void getXForms() {

		FormDefRMSUtility formDefRMSUtility = (FormDefRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(FormDefRMSUtility.getUtilityName());
		formDefRMSUtility.open();
    	IRecordStoreEnumeration recordEnum = formDefRMSUtility.enumerateMetaData();
    	int pos =0;
    	while(recordEnum.hasNextElement())
    	{
    		int i;
			try {
				i = recordEnum.nextRecordId();
				FormDefMetaData mdata = new FormDefMetaData();
				formDefRMSUtility.retrieveMetaDataFromRMS(i,mdata);
				// TODO fix it so that record id is part of the metadata serialization

				// BWD 27/7/2008
				// Getting rid of annoying numbers thing
				//listOfForms.put(new Integer(pos), mdata.getRecordId()+"-"+mdata.getName());
				listOfForms.put(new Integer(pos), mdata.getTitle());
				formIDs.insertElementAt(mdata, pos);
				pos++;
			} catch (RecordStorageException e) {
				// TODO Auto-generated catch block
				//#if debug.output==verbose || debug.output==exception
				e.printStackTrace();
				//#endif
			}
		}
    }

	public void contextChanged(Context context) {
		Vector contextChanges = this.context.mergeInContext(context);

		Enumeration en = contextChanges.elements();
		while(en.hasMoreElements()) {
			String changedValue = (String)en.nextElement();
			if(changedValue == Constants.USER_KEY) {
				//update username somewhere
			}
		}
	}

	public void halt() {

	}
	public void resume(Context context) {
		this.contextChanged(context);
		//Possibly want to check for new/updated forms
		JavaRosaServiceProvider.instance().showView(this.formsList);
	}
	public void destroy() {

	}
	public Context getActivityContext() {
		return context;
	}

	public void addNewMenuCommand(Command c) {
		this.formsList.addCommand(c);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.parent = shell;
	}

	public void annotateCommand(ICommand command) {
		customCommands.addElement(command);
	}
}
