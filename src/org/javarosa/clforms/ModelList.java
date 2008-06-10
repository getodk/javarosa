/*
 * FormList.java
 *
 * Created on 2007/10/25, 10:23:19
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// merge this with Form list to 'metadata list'

package org.javarosa.clforms;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;

import org.javarosa.clforms.api.Form;
import org.javarosa.clforms.storage.Model;
import org.javarosa.clforms.storage.ModelMetaData;
import org.javarosa.clforms.storage.ModelRMSUtility;
import org.javarosa.clforms.storage.XFormRMSUtility;
import org.javarosa.dtree.i18n.XFormsLocaleManager;
import org.openmrs.transport.TransportMessage;
import org.openmrs.transport.midp.TransportLayer;

/**
 *
 * @author Munier
 */
public class ModelList extends List implements CommandListener
{
	private final Command CMD_BACK = new Command("Back", Command.BACK, 2);
	private final Command CMD_SEND = new Command("SEND Data",Command.SCREEN,1);
	private final Command CMD_EDIT = new Command("Edit", Command.SCREEN, 2);
	private final Command CMD_REFRESH = new Command("Refresh", Command.SCREEN, 3);
	private final Command CMD_MSGS = new Command("Message Status",Command.SCREEN,4);
	private final Command CMD_DELETE = new Command("Delete",Command.SCREEN,5);
	private final Command CMD_EMPTY = new Command("Empty", Command.SCREEN, 6);


    private ModelRMSUtility modelRMSUtility;
    private XFormRMSUtility xformRMSUtility;
    private TransportShell mainShell;
    private Vector modelIDs;
	private TransportLayer transportLayer = null;

	private Image unSentImage;
	private Image deliveredImage;
	private Image unConfirmedImage;
	private Image failedImage;


    public ModelList(TransportShell mainShell)
    {
        super("Saved Forms", List.EXCLUSIVE);
        this.mainShell = mainShell;
        this.modelRMSUtility = mainShell.getModelRMSUtility();
        this.xformRMSUtility = mainShell.getXFormRMSUtility();
        this.transportLayer = this.mainShell.getTransportLayer();
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

    	unSentImage = initialiseStateImage(12, 12, 255, 255, 255);
    	deliveredImage = initialiseStateImage(12, 12, 0, 255, 0);
    	unConfirmedImage = initialiseStateImage(12, 12, 255, 140, 0);
    	failedImage = initialiseStateImage(12, 12, 255, 0, 0);
    	this.deleteAll();
    	this.setTicker(new Ticker("Please select a Model to send..."));
        this.addCommand(CMD_BACK);
        this.addCommand(CMD_EDIT);
        this.addCommand(CMD_SEND);
        this.addCommand(CMD_MSGS);
        this.addCommand(CMD_DELETE);
        this.addCommand(CMD_EMPTY);
        this.addCommand(CMD_REFRESH);
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
            	if (this.getSelectedIndex() == -1) {
            		//error
            	} else {
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
            } catch (Exception ex)//IOException ex)
            {
                ex.printStackTrace();
            }
        } else if (c == CMD_SEND)
        {
        	ModelMetaData data = (ModelMetaData) modelIDs.elementAt(this.getSelectedIndex());
            Model model = new Model();
            try {
				this.modelRMSUtility.retrieveFromRMS(data.getRecordId(), model);
				model.setRecordId(data.getRecordId());
			} catch (IOException e) {
				javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("modelLoadError", "Error Loading Model",null,
						AlertType.ERROR);
				Display.getDisplay(this.mainShell).setCurrent(a,this);
				e.printStackTrace();
			}
			this.transportLayer.setData(model);
            this.transportLayer.showURLform();
        } else if (c == CMD_EMPTY)
        {
        	this.modelRMSUtility.tempEmpty();
            createView();
        } else if (c == CMD_BACK)
        {
        	this.mainShell.createView();
        } else if (c == CMD_DELETE)
        {
        	ModelMetaData data = (ModelMetaData) modelIDs.elementAt(this.getSelectedIndex());
            this.mainShell.deleteModel(data.getRecordId());
            this.createView();
        } else if (c == CMD_MSGS)
        {
        	/*ModelListXtra mlx = new ModelListXtra(this.mainShell);
        	mlx.createView();*/
//        	if (this.transportLayer != null)
        		this.transportLayer.showMessageList();
//        	else
//        		this.mainShell.createView();
        } else if (c == CMD_REFRESH)
        {
        	this.createView();
        }
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

				Image stateImg = getStateImage(getModelDeliveryStatus(i));

				this.append(mdata.getRecordId()+"-"+mdata.getName()+"_"+mdata.getDateSaved()+"_"+mdata.getRecordId(), stateImg);
				modelIDs.insertElementAt(mdata,pos);
				pos++;
			} catch (InvalidRecordIDException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

    private Image getStateImage(int modelDeliveryStatus) {
    	Image result;
    	switch (modelDeliveryStatus) {
		case TransportMessage.STATUS_DELIVERED:
			result = this.deliveredImage;
			break;
		case TransportMessage.STATUS_NEW:
			result = this.unConfirmedImage;
			break;
		case TransportMessage.STATUS_NOT_SENT:
			result = this.unSentImage;
			break;
		case TransportMessage.STATUS_FAILED:
			result = this.failedImage;
			break;
		default:
			result = this.unSentImage;
			break;
		}
		return result;
	}

	private Image initialiseStateImage(int w, int h, int r, int g, int b) {
		Image res = Image.createImage(w, h);
		Graphics gc = res.getGraphics();
		gc.setColor(r, g, b);
		gc.fillRect(0, 0, w, h);
		return res;
	}

	private int getModelDeliveryStatus(int modelId) {

		Enumeration qMessages = transportLayer.getTransportMessages();
		TransportMessage message;
		while(qMessages.hasMoreElements())
    	{
			message = (TransportMessage) qMessages.nextElement();
			if(message.getModelId()==modelId)
				return message.getStatus();

    	}
		return 0;
	}
}