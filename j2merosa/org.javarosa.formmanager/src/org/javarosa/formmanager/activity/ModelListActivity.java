/*
 * FormList.java
 *
 * Created on 2007/10/25, 10:23:19
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// merge this with Form list to 'metadata list'

package org.javarosa.formmanager.activity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeMetaData;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.util.externalizable.DeserializationException;

/**
 *
 * @author Munier
 */
public class ModelListActivity extends List implements CommandListener, IActivity, IView
{
	
	public final static String returnKey = "ModelListReturnCommand";

	public final static Command CMD_BACK = new Command("Back", Command.BACK, 2);
	public final static Command CMD_SEND = new Command("Send Data",Command.SCREEN,1);
	public final static Command CMD_EDIT = new Command("Edit", Command.SCREEN, 2);
	public final static Command CMD_REFRESH = new Command("Refresh", Command.SCREEN, 3);
	public final static Command CMD_MSGS = new Command("Message Status",Command.SCREEN,4);
	public final static Command CMD_DELETE = new Command("Delete",Command.SCREEN,5);
	public final static Command CMD_EMPTY = new Command("Empty", Command.SCREEN, 6);
	
	Context theContext;

	public void contextChanged(Context globalContext) {
		theContext.mergeInContext(globalContext);
		//TODO: Do we have any values that need updating depending
		//on global context changes?
	}

	public void destroy() {
		// Stub. Nothing to do for this Module
	}

	public void halt() {
		// Stub. Nothing to do for this Module
	}

	public void resume(Context globalContext) {
		this.contextChanged(globalContext);
		this.createView();
	}

	public void start(Context context) {
		theContext = context;
		this.createView();
	}

    private DataModelTreeRMSUtility dataModelRMSUtility;
    private FormDefRMSUtility formDefRMSUtility;
    private IShell mainShell;
    private Vector modelIDs;

	private Image unSentImage;
	private Image deliveredImage;
	private Image unConfirmedImage;
	private Image failedImage;


    public ModelListActivity(IShell mainShell)
    {
        super("Saved Forms", List.EXCLUSIVE);
        this.mainShell = mainShell;
        this.dataModelRMSUtility = (DataModelTreeRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(DataModelTreeRMSUtility.getUtilityName());
		this.formDefRMSUtility = (FormDefRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(FormDefRMSUtility.getUtilityName());

    }
    public void createView(){
    	unSentImage = initialiseStateImage(12, 12, 255, 255, 255);
    	deliveredImage = initialiseStateImage(12, 12, 0, 255, 0);
    	unConfirmedImage = initialiseStateImage(12, 12, 255, 140, 0);
    	failedImage = initialiseStateImage(12, 12, 255, 0, 0);
    	this.deleteAll();
    	//this.setTicker(new Ticker("Please select a Model to send..."));
        this.addCommand(CMD_BACK);
        this.addCommand(CMD_EDIT);
        this.addCommand(CMD_SEND);
        //this.addCommand(CMD_MSGS);//now redundant as we have color boxes for this TODO: clean up related code!
        this.addCommand(CMD_DELETE);
        this.addCommand(CMD_EMPTY);
        this.addCommand(CMD_REFRESH);
        this.setCommandListener(this);
        this.populateListWithModels();
		mainShell.setDisplay(this,this);
    }


    public void commandAction(Command c, Displayable d)
    {
        if (c == CMD_EDIT)
        {
            try
            {
            	if (this.getSelectedIndex() == -1) {
            		//error
            	} else {
            		DataModelTreeMetaData data = (DataModelTreeMetaData) modelIDs.elementAt(this.getSelectedIndex());
            		FormDef selectedForm = new FormDef();
        			//#if debug.output==verbose
            		System.out.println("Attempt retreive: "+data.getFormIdReference());
        			//#endif
            		this.formDefRMSUtility.retrieveFromRMS(data.getFormIdReference(), selectedForm);
        			//#if debug.output==verbose
            		System.out.println("Form retrieve OK\nAttempt retreive model: "+data.getRecordId());
            		//#endif
            		DataModelTree formData = new DataModelTree();
            		this.dataModelRMSUtility.retrieveFromRMS(data.getRecordId(), formData);
            		selectedForm.setName(this.formDefRMSUtility.getName(data.getFormIdReference()));
            		System.out.println("data model: "+formData.getName());
            		Hashtable formEditArgs = new Hashtable();
            		formEditArgs.put(returnKey, CMD_EDIT);
            		formEditArgs.put("form", selectedForm);
            		formEditArgs.put("data", formData);
            		mainShell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, formEditArgs);
            	}
            } catch (Exception ex)//IOException ex)
            {
    			//#if debug.output==verbose || debug.output==exception
                ex.printStackTrace();
                //#endif
            }
        } else if (c == CMD_SEND)
        {
            if (this.getSelectedIndex() != -1) {
            	DataModelTreeMetaData data = (DataModelTreeMetaData) modelIDs.elementAt(this.getSelectedIndex());
            	DataModelTree model = new DataModelTree();
            	ITransportManager tm = JavaRosaServiceProvider.instance().getTransportManager();
                try {
                    this.dataModelRMSUtility.retrieveFromRMS(data.getRecordId(), model);
                    //model.setRecordId(data.getRecordId());
                } catch (IOException e) {
                    final javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("modelLoadError", "Error Loading Model", null, AlertType.ERROR);
                    mainShell.setDisplay(this, new IView() {public Object getScreenObject() { return a;}});
                    e.printStackTrace();
                } catch (DeserializationException e) {
                    final javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("modelLoadError", "Error Loading Model", null, AlertType.ERROR);
                    mainShell.setDisplay(this, new IView() {public Object getScreenObject() { return a;}});
                    e.printStackTrace();
                }
                //restrict resending of sent forms here
                
                if(TransportMessage.STATUS_DELIVERED == tm.getModelDeliveryStatus(data.getRecordId(), true))
                {
                	final javax.microedition.lcdui.Alert a = new javax.microedition.lcdui.Alert("Resend restriction", "Form already submitted!", null, AlertType.INFO);
                    mainShell.setDisplay(this, new IView() {public Object getScreenObject() { return a;}});
                	//Hashtable returnArgs = new Hashtable();
                	//returnArgs.put(returnKey, CMD_BACK);
                	//mainShell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
                }	
                
                Hashtable formSendArgs = new Hashtable();
                //TODO: We need some way to codify this Next Action stuff. Maybe a set of Constants for the ModelListModule?
                formSendArgs.put(returnKey, CMD_SEND);
                formSendArgs.put("data", model);
                mainShell.returnFromActivity(this, Constants.ACTIVITY_NEEDS_RESOLUTION, formSendArgs);
                
            }
        } else if (c == CMD_EMPTY)
        {
        	this.dataModelRMSUtility.tempEmpty();
            createView();
        } else if (c == CMD_BACK)
        {
        	Hashtable returnArgs = new Hashtable();
        	returnArgs.put(returnKey, CMD_BACK);
        	mainShell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
        } else if (c == CMD_DELETE)
        {
        	DataModelTreeMetaData data = (DataModelTreeMetaData) modelIDs.elementAt(this.getSelectedIndex());
            dataModelRMSUtility.deleteRecord(data.getRecordId());
            this.createView();
        } else if (c == CMD_MSGS)
        {	
        	//TODO: This is a phenomenal chance to try out the "inherited menus". Should look into that. 
        	Hashtable returnArgs = new Hashtable();
        	returnArgs.put(returnKey, CMD_MSGS);
        	mainShell.returnFromActivity(this, Constants.ACTIVITY_SUSPEND, returnArgs);

        } else if (c == CMD_REFRESH)
        {
        	this.createView();
        }
    }

    public void populateListWithModels()
    {
    	ITransportManager tm = JavaRosaServiceProvider.instance().getTransportManager();
    	
    	this.dataModelRMSUtility.open();
    	IRecordStoreEnumeration recordEnum = this.dataModelRMSUtility.enumerateMetaData();
    	modelIDs = new Vector();
    	int pos =0;
    	while(recordEnum.hasNextElement())
    	{
    		int i;
			try {

				i = recordEnum.nextRecordId();
				DataModelTreeMetaData mdata = new DataModelTreeMetaData();
				this.dataModelRMSUtility.retrieveMetaDataFromRMS(i,mdata);
				// TODO fix it so that record id is part of the metadata serialization
				mdata.setRecordId(i);

				Image stateImg = getStateImage(tm.getModelDeliveryStatus(i, true));
				String serverKey = this.getModelReplyData(mdata.getRecordId());
				
				
				///convert the date to special format with calendar
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(mdata.getDateSaved());
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH)+1;
				int date = calendar.get(Calendar.DATE);

				String dateSTR = date+"-"+month+"-"+year;
				
				this.append(mdata.getName()+" - "+dateSTR+" - "+serverKey, stateImg);
				modelIDs.insertElementAt(mdata,pos);
				pos++;
			} catch (RecordStorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

    private String getModelReplyData(int modelId) {

    	
		Enumeration qMessages = JavaRosaServiceProvider.instance().getTransportManager().getMessages(); 
		
		TransportMessage message;
		while(qMessages.hasMoreElements())
    	{
			message = (TransportMessage) qMessages.nextElement();
			if(message.getModelId()==modelId)
				return new String (message.getReplyloadData());
    	}
		return "";
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
	public Context getActivityContext() {
		return theContext;
	}
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.mainShell = shell;
	}

	public Object getScreenObject() {
		// TODO Auto-generated method stub
		return this;
	}
}