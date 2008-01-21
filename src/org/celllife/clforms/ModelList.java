/*
 * FormList.java
 *
 * Created on 2007/10/25, 10:23:19
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// merge this with Form list to 'metadata list'

package org.celllife.clforms;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;

import org.celllife.clforms.api.Form;
import org.celllife.clforms.storage.Model;
import org.celllife.clforms.storage.ModelMetaData;
import org.celllife.clforms.storage.ModelRMSUtility;
import org.celllife.clforms.storage.XFormMetaData;
import org.celllife.clforms.storage.XFormRMSUtility;
import org.openmrs.transport.midp.TransportLayer;

/**
 *
 * @author Munier
 */
public class ModelList extends List implements CommandListener
{
	private final Command CMD_BACK = new Command("Back", Command.BACK, 2);
	private final Command CMD_EDIT = new Command("Edit", Command.SCREEN, 1);
	private final Command CMD_DELETE = new Command("Delete",Command.SCREEN,2);
	private final Command CMD_EMPTY = new Command("Empty", Command.SCREEN, 3);
	private final Command CMD_SEND = new Command("SEND Data",Command.SCREEN,1);
	private final Command CMD_MSGS = new Command("Message Status",Command.SCREEN,1);
	

    private ModelRMSUtility modelRMSUtility;
    private XFormRMSUtility xformRMSUtility;
    private TransportShell mainShell;
    private Vector modelIDs;
	private TransportLayer transportLayer = null;

    public ModelList(TransportShell mainShell)
    {
        super("Saved Forms", List.EXCLUSIVE);
        this.mainShell = mainShell;
        this.modelRMSUtility = mainShell.getModelRMSUtility();
        this.xformRMSUtility = mainShell.getXFormRMSUtility();
        createView();
    }
    
    public ModelList(TransportShell mainShell, TransportLayer transportLayer)
    {
        super("Completed Model List", List.EXCLUSIVE);
        this.mainShell = mainShell;
        this.transportLayer = transportLayer;
        this.modelRMSUtility = mainShell.getModelRMSUtility();
        this.xformRMSUtility = mainShell.getXFormRMSUtility();
        createView();
    }

    public void createView(){
    	this.deleteAll();
    	this.setTicker(new Ticker("Please select a Model to send..."));
        this.addCommand(CMD_BACK);
        this.addCommand(CMD_EDIT);
        this.addCommand(CMD_SEND);
        this.addCommand(CMD_MSGS);
        this.addCommand(CMD_DELETE);
        this.addCommand(CMD_EMPTY);
        this.setCommandListener(this);
        this.populateListWithModels();
        Display.getDisplay(mainShell).setCurrent(this);
    }
    
    
    public void commandAction(Command c, Displayable d)
    {
    	System.out.println("IN here");
        if (c == CMD_EDIT)
        {
            try
            {
            	ModelMetaData data = (ModelMetaData) modelIDs.elementAt(this.getSelectedIndex());
            	System.out.println(data.toString());
                Form selectedForm = new Form();
                System.out.println("Attempt retreive: "+data.getXformReference());
                this.xformRMSUtility.retrieveFromRMS(data.getXformReference(), selectedForm);
                System.out.println("Form retrieve OK\nAttempt retreive model: "+data.getRecordId());
                Model model = new Model();
                this.modelRMSUtility.retrieveFromRMS(data.getRecordId(), model);
                model.setEditID(data.getRecordId());
                // TODO the setRecordID for forms needs to be done at load time
                selectedForm.setRecordId(data.getXformReference());
                selectedForm.updatePromptsDefaultValues();
                selectedForm.setXmlModel(model);
                selectedForm.updatePromptsValues();
             // TODO fix this so IDs are in form objects properly
                selectedForm.setName(this.xformRMSUtility.getName(data.getXformReference()));
                System.out.println("Model retrieve OK:"+model.toString()+"\nAttempt FormLoad model: ");
                this.mainShell.controllerLoadForm(selectedForm);
            }
            catch (Exception ex)//IOException ex)
            {
                ex.printStackTrace();
            }
        } else if (c == CMD_SEND)
        {
        	ModelMetaData data = (ModelMetaData) modelIDs.elementAt(this.getSelectedIndex());
            Model model = new Model();
            try {
				this.modelRMSUtility.retrieveFromRMS(data.getRecordId(), model);
			} catch (IOException e) {
				javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("modelLoadError", "Error Loading Model",null,
						AlertType.ERROR);
				Display.getDisplay(this.mainShell).setCurrent(a,this);
				e.printStackTrace();
			}
			System.out.println("trying TL "+model.toString());
			this.transportLayer = this.mainShell.getTransportLayer();
        	this.transportLayer.setData(model.toString());
        	System.out.println("hereM1");
            this.transportLayer.createView();
        	System.out.println("hereM2");
            //this.mainShell.displayTransportLayer();
        	System.out.println("hereM3");
        } else if (c == CMD_EMPTY)
        {
        	this.modelRMSUtility.tempEmpty();
            createView();
        } else if (c == CMD_BACK)
        {
        	if (this.transportLayer != null)
        		this.transportLayer.createView();
        	else
        		this.mainShell.createView();
        } else if (c == CMD_DELETE)
        {
        	ModelMetaData data = (ModelMetaData) modelIDs.elementAt(this.getSelectedIndex());
            this.mainShell.deleteModel(data.getRecordId());
            this.createView();
        } /*else if (c == CMD_SEND)
        {
        	System.out.println("here OK");
        	this.mainShell.displayTransportLayer();
        	System.out.println("here OK2");
        } *//*else if (c == CMD_MSGS)
        {
        	if (this.transportLayer != null)
        		this.transportLayer.createView();
        	else
        		this.mainShell.createView();
        }*/
    }

    public void populateListWithModels()
    {

    	this.modelRMSUtility.open();
    	RecordEnumeration recordEnum = this.modelRMSUtility.enumerateMetaData();
    	modelIDs = new Vector();     
    	int pos =0;
    	while(recordEnum.hasNextElement())
    	{
    		int i;
			try {
				i = recordEnum.nextRecordId();
				ModelMetaData mdata = new ModelMetaData();
				this.modelRMSUtility.retrieveMetaDataFromRMS(i,mdata);
				// TODO fix it so that record id is part of the metadata serialization
				mdata.setRecordId(i);
				this.append(mdata.getName()+"_"+mdata.getRecordId(), null);
				modelIDs.insertElementAt(mdata,pos);
				pos++;
			} catch (InvalidRecordIDException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
}