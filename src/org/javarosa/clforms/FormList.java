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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;

import org.javarosa.clforms.storage.XFormMetaData;
import org.javarosa.clforms.storage.XFormRMSUtility;
import org.javarosa.properties.view.PropertiesScreen;

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
    private XFormRMSUtility xformRMSUtility;   
    private TransportShell mainShell;
    private Vector formIDs;
    
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
    	this.setTicker(new Ticker("Please select an XForm to load..."));
    	addScreenCommands();
        this.setCommandListener(this);
        this.populateListWithXForms();
        Display.getDisplay(mainShell).setCurrent(this);
    }

	private void addScreenCommands() {
		this.addCommand(CMD_EXIT);
        //this.addCommand(CMD_OPEN);
        this.addCommand(CMD_DELETE_FORM);
        this.addCommand(CMD_VIEWMODELS);
        this.addCommand(CMD_GETNEWFORMS);
        this.addCommand(CMD_SHAREFORMS);
        this.addCommand(CMD_SETTINGS);
	}

    public void commandAction(Command c, Displayable d)
    {
    	if (c == List.SELECT_COMMAND) {
        	XFormMetaData data = (XFormMetaData) formIDs.elementAt(this.getSelectedIndex());
        	System.out.println("chosen indx: "+this.getSelectedIndex()+" recID: "+data.getRecordId());
            this.mainShell.controllerLoadForm(data.getRecordId());
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
            this.mainShell.displayAvailableXFormMethods();
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
				this.append(mdata.getRecordId()+"-"+mdata.getName(), null);
//				this.append(mdata.getName(), null);
				formIDs.insertElementAt(mdata,pos);
				pos++;
				System.out.println("METADATA: "+mdata.toString());
			} catch (InvalidRecordIDException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

}
