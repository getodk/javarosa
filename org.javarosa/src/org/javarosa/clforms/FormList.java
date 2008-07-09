/*
 * FormList.java
 *
 * Created on 2007/10/25, 10:23:19
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.javarosa.clforms;

import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;

import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.storage.XFormMetaData;
import org.javarosa.clforms.storage.XFormRMSUtility;

import com.ev.evgetme.getMidlet;

/**
 *
 * @author Munier
 */
public class FormList extends List implements CommandListener
{
	private final Command CMD_EXIT = new Command("Exit", Command.BACK, 2);
	private final Command CMD_OPEN = new Command("Open", Command.SCREEN, 1);
    private final Command CMD_GETNEWFORMS = new Command("Get New Forms", Command.SCREEN, 2);
    private final Command CMD_VIEWMODELS = new Command("View Saved", Command.SCREEN, 3);
    private final Command CMD_DELETE_FORM = new Command("Delete",Command.SCREEN,4);
    private final Command CMD_SHAREFORMS = new Command("Share Forms", Command.SCREEN, 2);
    private final Command CMD_SETTINGS = new Command("Settings", Command.SCREEN, 3);
     private final Command CMD_ADD_USER = new Command("Add User", Command.SCREEN, 5);
    public final Command CMD_SAVE = new Command("Save", Command.OK, 1);
	public final Command CMD_CANCEL = new Command("Cancel",Command.BACK, 1);
    private XFormRMSUtility xformRMSUtility;
    private TransportShell mainShell;
    private Vector formIDs;
    private NewUserForm addUser;

    public FormList(TransportShell mainShell)
    {
       //super("Forms List", List.EXCLUSIVE);
    	super("Forms List", List.IMPLICIT);
       this.mainShell = mainShell;
       this.xformRMSUtility = mainShell.getXFormRMSUtility();
       this.createView();
    }

    public void createView(){
    	this.deleteAll();
    	this.setTicker(new Ticker("Please select a form to fill out..."));
    	addScreenCommands();
        this.setCommandListener(this);
        this.populateListWithXForms();
        Display.getDisplay(mainShell).setCurrent(this);
    }
    ///Added by Julian: this is almost identical to createView above. this is an alternitive
    ///way to interact with transportshell.
    public void createViewDontTransformDisplay(){
    	this.setTitle("Forms List");// super("Forms List", List.IMPLICIT);

    	this.xformRMSUtility = mainShell.getXFormRMSUtility();

    	this.deleteAll();
    	//polishout
		//	this.setTicker(new Ticker("Please select an XForm to load..."));
    	addScreenCommands();
        this.setCommandListener(this);
        this.populateListWithXForms();

    }

	private void addScreenCommands() {
		this.addCommand(CMD_EXIT);
        //this.addCommand(CMD_OPEN);
        this.addCommand(CMD_DELETE_FORM);
        this.addCommand(CMD_VIEWMODELS);
        this.addCommand(CMD_GETNEWFORMS);
        this.addCommand(CMD_SHAREFORMS);
        System.out.println("admin user? here it is: "+mainShell.getUSERTYPE() +" - expected: "+Constants.ADMINUSER);
        if (mainShell.getUSERTYPE().equals(Constants.ADMINUSER))
        {
        	System.out.println("admin user found, ");
        	this.addCommand(CMD_ADD_USER);
        }
        //#if polish.usePolishGui
        this.addCommand(CMD_SETTINGS);
        //#endif
	}

    public void commandAction(Command c, Displayable d)
    {
    	if (c == List.SELECT_COMMAND) {
        	XFormMetaData data = (XFormMetaData) formIDs.elementAt(this.getSelectedIndex());
        	System.out.println("chosen indx: "+this.getSelectedIndex()+" recID: "+data.getRecordId());
            this.mainShell.controllerLoadForm(data.getRecordId());
		}
		else if (c == CMD_ADD_USER)
    	{
    		System.out.println("cmd_add_user");
    		///load an add User Form class here
    		System.out.println("1111");
    		addUser = new NewUserForm("Adding a new user");
    		addUser.addCommand(CMD_SAVE);
    		addUser.addCommand(CMD_CANCEL);
    		System.out.println("2222");
    		addUser.setCommandListener(this);
    		System.out.println("3333");
    		Display.getDisplay(mainShell).setCurrent(addUser);
    		System.out.println("4444");
    	}
    	/*if (c == CMD_OPEN)
        {
        	XFormMetaData data = (XFormMetaData) formIDs.elementAt(this.getSelectedIndex());
        	System.out.println("chosen indx: "+this.getSelectedIndex()+" recID: "+data.getRecordId());
            this.mainShell.controllerLoadForm(data.getRecordId());
        }*/

        if (c == CMD_DELETE_FORM)
        {
        	XFormMetaData data = (XFormMetaData) formIDs.elementAt(this.getSelectedIndex());
        	// TODO check for any form dependancies
            this.mainShell.deleteForm(data.getRecordId());
            createView();
        }

        if (c == CMD_EXIT)
        {
            this.mainShell.destroyApp(true);
        }

        if (c == CMD_GETNEWFORMS)
        {
            this.mainShell.getNewFormsByTransportPropertySetting();
        }

        if (c == CMD_VIEWMODELS)
        {
            this.mainShell.displayModelList();
        }

        if (c == CMD_SHAREFORMS)
        {
            this.mainShell.startBToothClient();
        }
        if (c == CMD_SETTINGS)
        {
            this.mainShell.editProperties();
        }
        else if (c == CMD_SAVE)
    	{
    		System.out.println("SAVE SELECTED");

    		String answer = addUser.readyToSave();

    		if (answer.equals(""))	{///success

    			javax.microedition.lcdui.Alert successfulNewUser  = new javax.microedition.lcdui.Alert("User added successfully",
						"The new user can only be logged in when the current user logs out.", null,AlertType.CONFIRMATION);
    			successfulNewUser.setTimeout(javax.microedition.lcdui.Alert.FOREVER);
    			Display.getDisplay(mainShell).setCurrent(successfulNewUser,this);
    		}
    		else if (answer.substring(0,10 ).equals("Username ("))///name already taken..
    		{

    			javax.microedition.lcdui.Alert nameTakenError  = new javax.microedition.lcdui.Alert("Problem adding User",
						answer, null,AlertType.ERROR);
    			Display.getDisplay(mainShell).setCurrent(nameTakenError,addUser);
    		}
    		else if (answer.substring(0,9).equals("Please fi") )
    		{
    			System.out.println(answer.substring(9));
    			javax.microedition.lcdui.Alert noInputError  = new javax.microedition.lcdui.Alert("Problem adding User",
						answer, null,AlertType.ERROR);
    			Display.getDisplay(mainShell).setCurrent(noInputError,addUser);
    		}
    		else if (answer.substring(0,9).equals("Please re"))///password error
    		{
    			System.out.println(answer.substring(9));
    			javax.microedition.lcdui.Alert passwordMismatchError  = new javax.microedition.lcdui.Alert("Problem adding User",
						answer, null,AlertType.ERROR);
    			passwordMismatchError.setTimeout(javax.microedition.lcdui.Alert.FOREVER);
    			Display.getDisplay(mainShell).setCurrent(passwordMismatchError,addUser);
    		}

    	}
    }

    public void populateListWithXForms()
    {
    	this.xformRMSUtility.open();
    	RecordEnumeration recordEnum = this.xformRMSUtility.enumerateMetaData();
    	formIDs = new Vector();
    	int pos =0;
    	while(recordEnum.hasNextElement())
    	{
    		int i;
			try {
				i = recordEnum.nextRecordId();
				XFormMetaData mdata = new XFormMetaData();
				this.xformRMSUtility.retrieveMetaDataFromRMS(i,mdata);
				// TODO fix it so that record id is part of the metadata serialization
				//LOG
				System.out.println(mdata.toString());
				//mdata.setRecordId(i);
				this.append(/*mdata.getRecordId()+"-"+*/ mdata.getName(), null);
				formIDs.insertElementAt(mdata,pos);
				pos++;
			} catch (InvalidRecordIDException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

}
