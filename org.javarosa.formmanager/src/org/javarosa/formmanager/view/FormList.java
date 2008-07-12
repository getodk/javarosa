/**
 *
 */
package org.javarosa.formmanager.view;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.formmanager.activity.FormListActivity;

/**
 * @author Brian DeRenzi
 *
 */
public class FormList extends List implements CommandListener {
	// TODO: These should all be il8n'd
	private final Command CMD_EXIT = new Command("Exit", Command.BACK, 2);
    private final Command CMD_GETNEWFORMS = new Command("Get New Forms", Command.SCREEN, 2);
    private final Command CMD_VIEWMODELS = new Command("View Saved", Command.SCREEN, 3);
    private final Command CMD_DELETE_FORM = new Command("Delete",Command.SCREEN,4);
    private final Command CMD_SHAREFORMS = new Command("Share Forms", Command.SCREEN, 2);
    private final Command CMD_SETTINGS = new Command("Settings", Command.SCREEN, 3);
    
    private FormListActivity parent = null;
    
	public FormList(FormListActivity p, String title) {
		this(p, title, List.IMPLICIT);
	}

	public FormList(FormListActivity p, String title, int listType) {
		
		super(title, listType);
		this.parent = p;
	}

	public void loadView(Hashtable args) {
		this.deleteAll();
		this.createView();
		this.setCommandListener(this);
		this.populateWithXForms(args);
	}

	private void createView() {
		addScreenCommands();
	}
	
	private void addScreenCommands() {
		this.addCommand(CMD_EXIT);
        //this.addCommand(CMD_OPEN);
        this.addCommand(CMD_DELETE_FORM);
        this.addCommand(CMD_VIEWMODELS);
        this.addCommand(CMD_GETNEWFORMS);
        this.addCommand(CMD_SHAREFORMS);
        //#if polish.usePolishGui
        this.addCommand(CMD_SETTINGS);
        //#endif
	}
	
	/**
	 * Put the list
	 */
	private void populateWithXForms(Hashtable listOfForms) {
		Integer pos = null;
		Enumeration e = listOfForms.keys();
		while(e.hasMoreElements()){
			pos = (Integer)e.nextElement();
			String form = (String)listOfForms.get(pos);
			this.append(form,null);
		}
	}


	public void commandAction(Command c, Displayable arg1) {
		Hashtable v = new Hashtable();
		
		// TODO Auto-generated method stub
		if (c == List.SELECT_COMMAND) {
			//LOG
			
			v.put( new Integer(0), new Integer(this.getSelectedIndex()));
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_SELECT_XFORM, v);
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}
/*
        if (c == CMD_DELETE_FORM) {
			XFormMetaData data = (XFormMetaData) formIDs.elementAt(this.getSelectedIndex());
			// TODO check for any form dependancies
			this.mainShell.deleteForm(data.getRecordId());
			createView();
		}
*/
		if (c == CMD_EXIT) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_EXIT, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}
/*
		if (c == CMD_GETNEWFORMS) {
			this.mainShell.getNewFormsByTransportPropertySetting();
		}
*/
		if (c == CMD_VIEWMODELS) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_VIEW_DATA, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}
/*
		if (c == CMD_SHAREFORMS) {
			this.mainShell.startBToothClient();
		}
		*/if (c == CMD_SETTINGS) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_SETTINGS, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}
	}

}
