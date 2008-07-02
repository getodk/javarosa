/**
 * 
 */
package org.javarosa.demo.module;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;

import org.javarosa.clforms.storage.XFormMetaData;
import org.javarosa.clforms.storage.XFormRMSUtility;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaPlatform;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IModule;
import org.javarosa.core.api.IShell;
import org.javarosa.demo.view.Commands;
import org.javarosa.demo.view.FormList;
import org.javarosa.view.ViewTypes;

/**
 * @author Brian DeRenzi
 *
 */
public class FormListModule implements IModule {
	private FormList formsList = null;
	private Hashtable listOfForms = null;
	private Vector formIDs = null;
	private IShell parent = null;
	
	Context context;
	
	public FormListModule(IShell p, String title) {
		this.parent = p;
		this.formsList = new FormList(this,title);
	}
	
	public void start(Context context) {
		this.listOfForms = new Hashtable();
		this.formIDs = new Vector();
		getXForms();
		this.formsList.loadView(listOfForms);
		parent.setDisplay(this, this.formsList);
		
		this.context = context;
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
			//LOG
			System.out.println("Selected form: " + formIDs.elementAt( ((Integer)(returnvals.get(cmd))).intValue() ));
			
			// 
			break;
		
		}
		}
	}
	
	private void getXForms() {
		XFormRMSUtility xformRMSUtility = JavaRosaPlatform.instance().getXFormRMS();
		xformRMSUtility.open();
    	RecordEnumeration recordEnum = xformRMSUtility.enumerateMetaData();
    	int pos =0;
    	while(recordEnum.hasNextElement())
    	{
    		int i;
			try {
				i = recordEnum.nextRecordId();
				XFormMetaData mdata = new XFormMetaData();
				xformRMSUtility.retrieveMetaDataFromRMS(i,mdata);
				// TODO fix it so that record id is part of the metadata serialization
				//LOG
				System.out.println(mdata.toString());
				//mdata.setRecordId(i);
				listOfForms.put(new Integer(pos), mdata.getRecordId()+"-"+mdata.getName());
				formIDs.insertElementAt(mdata, pos);
				pos++;
				System.out.println("METADATA: "+mdata.toString());
			} catch (InvalidRecordIDException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    	//LOG
    	System.out.println("Done getting XForms");
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
		JavaRosaPlatform.instance().showView(this.formsList);
	}
	public void destroy() {
		
	}
}
