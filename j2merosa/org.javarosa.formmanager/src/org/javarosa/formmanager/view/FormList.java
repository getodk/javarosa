/**
 *
 */
package org.javarosa.formmanager.view;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.Context;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IView;
import org.javarosa.core.util.Map;
import org.javarosa.formmanager.activity.FormListActivity;
import org.javarosa.user.model.User;

/**
 * @author Brian DeRenzi
 *
 */
public class FormList extends List implements CommandListener, IView {
	// TODO: These should all be il8n'd
	private final Command CMD_EXIT = new Command("Exit", Command.BACK, 2);
    private final Command CMD_GETNEWFORMS = new Command("Get New Forms", Command.SCREEN, 2);
    // CZUE added for testing 
    private final Command CMD_CAMERA = new Command("Camera", Command.SCREEN, 2);
    //Added for debugging(Ndubisi)
    private final Command CMD_RECORDER = new Command("Recorder", Command.SCREEN, 2);
    private final Command CMD_IMAGE_BROWSE = new Command("Image Management", Command.SCREEN, 2);
	// others
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

	public Vector loadView(Map args) {
		this.deleteAll();
		this.createView();
		this.setCommandListener(this);
		return this.populateWithXForms(args);
	}

	private void createView() {
		addScreenCommands();
	}

	private void addScreenCommands() {
		Context c = this.parent.getContext();
		boolean demo = false;
		if(c.getElement("USER")!=null){
			User loggedInUser = (User)c.getElement("USER");
			if (loggedInUser.getUserType().equals(User.DEMO_USER))
				demo = true;
		}
		this.addCommand(CMD_EXIT);
        //this.addCommand(CMD_OPEN);
        this.addCommand(CMD_DELETE_FORM);
        this.addCommand(CMD_VIEWMODELS);
        this.addCommand(CMD_GETNEWFORMS);
        this.addCommand(CMD_CAMERA);
        this.addCommand(CMD_RECORDER);
        this.addCommand(CMD_IMAGE_BROWSE);
//        this.addCommand(CMD_SHAREFORMS);

        //#if polish.usePolishGui
        this.addCommand(CMD_SETTINGS);
        //#endif
        
        for (int i = 0; i < parent.customCommands.size(); i++) {
        	addCommand((Command)((ICommand)parent.customCommands.elementAt(i)).getCommand());
        }
	}

	/**
	 * Put the list
	 */
	private Vector populateWithXForms(Map listOfForms) {
		Vector returnVals = new Vector();
		Enumeration e = listOfForms.keys();
		while(e.hasMoreElements()){
			Integer pos = (Integer)e.nextElement();
			String form = (String)listOfForms.get(pos);
			this.append(form,null);
			returnVals.addElement(pos);
		}
		return returnVals;
	}


	public void commandAction(Command c, Displayable arg1) {
		Hashtable v = new Hashtable();
 
		// TODO Auto-generated method stub
		if (c == List.SELECT_COMMAND) {
			//LOG

			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_SELECT_XFORM, new Integer(this.getSelectedIndex()));
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}

        if (c == CMD_DELETE_FORM) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_DELETE_FORM, new Integer(this.getSelectedIndex()));
			// TODO check for any form dependancies
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}

		else if (c == CMD_EXIT) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_EXIT, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}

		else if (c == CMD_GETNEWFORMS) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_GET_NEW_FORM, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}

		else if (c == CMD_VIEWMODELS) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_VIEW_DATA, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}
        // CZUE: camera test
		else if (c == CMD_CAMERA) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_CAMERA, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}
		else if (c == CMD_IMAGE_BROWSE) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_IMAGE_BROWSE, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}

		else if (c == parent.CMD_ADD_USER) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_ADD_USER, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}
        //Recorder debugging(Ndubisi)
		else if(c == CMD_RECORDER)
		{
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_RECORDER, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}
        
        /*
		if (c == CMD_SHAREFORMS) {
			this.mainShell.startBToothClient();
		}
		*/else if (c == CMD_SETTINGS) {
			Hashtable returnvals = new Hashtable();
			returnvals.put(Commands.CMD_SETTINGS, "");
			this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);
		}
		//this case should be triggered when new custom commands are added
		else {
			for (int i = 0; i < parent.customCommands.size(); i++) {
				ICommand ic = (ICommand)parent.customCommands.elementAt(i);
				if (c == ic.getCommand()) {
					Hashtable returnvals = new Hashtable();
					returnvals.put(ic.getCommandId(), "");
					this.parent.viewCompleted(returnvals, ViewTypes.FORM_LIST);		
				}
			}			
		}
	}
	public Object getScreenObject() {
		return this;
	}

}
