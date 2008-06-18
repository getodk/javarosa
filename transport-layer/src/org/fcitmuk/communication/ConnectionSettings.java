package org.fcitmuk.communication;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;

import org.fcitmuk.midp.mvc.AbstractView;
import org.fcitmuk.util.AlertMessage;
import org.fcitmuk.util.AlertMessageListener;
import org.fcitmuk.util.DefaultCommands;
import org.fcitmuk.util.SimpleOrderedHashtable;

/**
 * This class shows existing connection parameters and lets the user modify them,
 * before passing them over.
 * 
 * @author Daniel
 *
 */
public class ConnectionSettings extends AbstractView  implements AlertMessageListener {

	private static final int CA_NONE = 0;
	private static final int CA_CON_TYPES = 1;
	private static final int CA_CON_PARAMS = 2;
	
	String userName = "";
	String password = "";

	
	private AlertMessage alertMsg;
	
	/** The connection type to use. */
	private int conType = TransportLayer.CON_TYPE_NULL;
	
	/** The connection parameters. */
	protected Hashtable conParams = new Hashtable();
	
	/** The connection parameters. */
	protected Vector connectionParameters = new Vector();
	
	private SimpleOrderedHashtable conTypes;
	
	/** Reference to the parent. */
	DefaultTransportLayer defTransLayer;
	
	private int currentAction = CA_NONE;
	
	public ConnectionSettings(){
		
	}
	
	/** 
	 * Displays a screen for the user to select a connection type.
	 * 
	 * @param display - reference to the current display.
	 * @param prevScreen - the screen to display after dismissing our screens.
	 */
	public void getUserSettings(Display display, Displayable prevScreen, int conType, SimpleOrderedHashtable conTypes,Hashtable conParams, DefaultTransportLayer defTransLayer, String name, String password, Vector connectionParameters){
		this.display = display;
		this.prevScreen = prevScreen;
		this.conTypes = conTypes;
		this.conParams = conParams;
		this.connectionParameters = connectionParameters;
		this.defTransLayer = defTransLayer;
		
		currentAction = CA_CON_TYPES;
		
		screen = new List("Connection Type", Choice.IMPLICIT);
		alertMsg = new AlertMessage(display,title,screen,this);
		
		//TODO This hashtable does not maintain the order on devices like sony erickson.
		Enumeration keys = conTypes.keys();
		Object key;
		while(keys.hasMoreElements()){
			key = keys.nextElement();
			((List)screen).append(conTypes.get(key).toString(), null);
		}
			
		screen.addCommand(DefaultCommands.cmdOk);
		screen.addCommand(DefaultCommands.cmdCancel);
		int index = fromConTypeToIndex(conType);
		if(index < ((List)screen).size())
			((List)screen).setSelectedIndex(index,true);
		screen.setCommandListener(this);
				
		this.display.setCurrent(screen);
	}
	
	/** 
	 * Converts a connection type to a UI index.
	 * 
	 * @param conType - the connection type.
	 * @return - the UI index.
	 */
	private int fromConTypeToIndex(int conType){
		//If connection type is not set, then select the Bluetooth item.		
		if(conType == TransportLayer.CON_TYPE_NULL)
			return /*conTypes.size() -   TransportLayer.CON_TYPE_BLUETOOTH;*/ TransportLayer.CON_TYPE_BLUETOOTH - 1;
		return /*conTypes.size() - conType;*/ conType - 1;
	}
	
	/**
	 * Converts a UI List index to connection type.
	 * 
	 * @param index - the UI index.
	 * @return - the connection type.
	 */
	private int fromIndexToConType(int index){
		return /*conTypes.size() - index;*/ index + 1;
	}
		
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk || c == List.SELECT_COMMAND)
				handleOkCommand(d);
			else if(c == DefaultCommands.cmdCancel)
				handleCancelCommand(d);
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		if(currentAction == CA_CON_PARAMS){
			currentAction = CA_CON_TYPES;
			display.setCurrent(screen);
		}
		else
			defTransLayer.onConnectionSettingsClosed(false);
	}
	
	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){
		if(currentAction == CA_CON_PARAMS){
			if(d != null){
				if(conType == TransportLayer.CON_TYPE_HTTP){
					conParams.put(TransportLayer.KEY_USER_DOWNLOAD_HTTP_URL, ((TextField)((Form)d).get(0)).getString());
					//conParams.put(TransportLayer.KEY_PATIENT_DOWNLOAD_HTTP_URL, ((TextField)((Form)d).get(1)).getString());
					conParams.put(TransportLayer.KEY_FORM_DOWNLOAD_HTTP_URL, ((TextField)((Form)d).get(1)).getString());
					conParams.put(TransportLayer.KEY_DATA_UPLOAD_HTTP_URL, ((TextField)((Form)d).get(2)).getString());
					
					for(int i=0; i<connectionParameters.size(); i++)
					{
						ConnectionParameter conParam = (ConnectionParameter)connectionParameters.elementAt(i);
						if(conParam.getConnectionType() == TransportLayer.CON_TYPE_HTTP)
							conParam.setValue(((TextField)((Form)d).get(i+3)).getString());
					}
				}
				else if(conType == TransportLayer.CON_TYPE_BLUETOOTH)
					conParams.put(TransportLayer.KEY_BLUETOOTH_SERVER_ID, ((TextField)((Form)d).get(0)).getString());
				else if(conType == TransportLayer.CON_TYPE_SMS){
					conParams.put(TransportLayer.KEY_SMS_DESTINATION_ADDRESS, ((TextField)((Form)d).get(0)).getString());
					conParams.put(TransportLayer.KEY_SMS_SOURCE_ADDRESS, ((TextField)((Form)d).get(1)).getString());
				}
			}
			defTransLayer.onConnectionSettingsClosed(true);
		}
		else{
			conType = fromIndexToConType(((List)d).getSelectedIndex());
			showConParams();
		}
	}
	
	private void showConParams(){
		currentAction = CA_CON_PARAMS;
		
		if(conType == TransportLayer.CON_TYPE_HTTP)
			showHttpConParams();
		else if(conType == TransportLayer.CON_TYPE_BLUETOOTH)
			showBluetoothConParams();
		else if(conType == TransportLayer.CON_TYPE_SMS)
			showSMSConParams();
		else
			handleOkCommand(null);
	}
	
	private void showHttpConParams(){
		Form frm = new Form(this.title);
		
		String s = (String)conParams.get(TransportLayer.KEY_USER_DOWNLOAD_HTTP_URL);
		if(s == null) s = "http://localhost:8080/openmrs/moduleServlet/xforms/userDownload"; //?uname="+userName+"&pw="+password;
		TextField txtField = new TextField("Users download url:",s,500,TextField.ANY);
		frm.append(txtField);
		
		/*s = (String)conParams.get(TransportLayer.KEY_PATIENT_DOWNLOAD_HTTP_URL);
		if(s == null) s = "http://localhost:8080/openmrs/module/xforms/patientDownload.form?downloadPatients=true"; //&uname="+userName+"&pw="+password;
		txtField = new TextField("Patients download url:",s,500,TextField.ANY);
		frm.append(txtField);*/
		
		
		s = (String)conParams.get(TransportLayer.KEY_FORM_DOWNLOAD_HTTP_URL);
		if(s == null) s = "http://localhost:8080/openmrs/moduleServlet/xforms/xformDownload?target=xforms"; //&uname="+userName+"&pw="+password;
		txtField = new TextField("Forms download url:",s,500,TextField.ANY);
		frm.append(txtField);
		
		s = (String)conParams.get(TransportLayer.KEY_DATA_UPLOAD_HTTP_URL);
		if(s == null) s = "http://localhost:8080/openmrs/moduleServlet/xforms/xformDataUpload?batchEntry=true"; //&uname="+userName+"&pw="+password;
		txtField = new TextField("Data upload url:",s,500,TextField.ANY);
		frm.append(txtField);
		
		for(int i=0; i<connectionParameters.size(); i++)
		{
			ConnectionParameter conParam = (ConnectionParameter)connectionParameters.elementAt(i);
			if(conParam.getConnectionType() == TransportLayer.CON_TYPE_HTTP)
			{
				txtField = new TextField(conParam.getName(),conParam.getValue(),500,TextField.ANY);
				frm.append(txtField);
			}
		}
		
		frm.addCommand(DefaultCommands.cmdCancel);
		frm.addCommand(DefaultCommands.cmdOk);
		frm.setCommandListener(this);
					
		display.setCurrent(frm);
	}
	
	private void showBluetoothConParams(){
		Form frm = new Form(this.title);
		
		TextField txtField = new TextField("Bluetoot Service ID:",(String)conParams.get(TransportLayer.KEY_BLUETOOTH_SERVER_ID),100,TextField.ANY);
		frm.append(txtField);
		
		frm.addCommand(DefaultCommands.cmdCancel);
		frm.addCommand(DefaultCommands.cmdOk);
		frm.setCommandListener(this);
					
		display.setCurrent(frm);
	}
	
	private void showSMSConParams(){
		Form frm = new Form(this.title);
		
		TextField txtField = new TextField("Server Address:",(String)conParams.get(TransportLayer.KEY_SMS_DESTINATION_ADDRESS),100,TextField.ANY);
		frm.append(txtField);
		
		txtField = new TextField("Source Address:",(String)conParams.get(TransportLayer.KEY_SMS_SOURCE_ADDRESS),100,TextField.ANY);
		frm.append(txtField);
		
		frm.addCommand(DefaultCommands.cmdCancel);
		frm.addCommand(DefaultCommands.cmdOk);
		frm.setCommandListener(this);
					
		display.setCurrent(frm);
	}
	
	public void onAlertMessageOk(){
		
	}
	
	public void onAlertMessageCancel(){
		
	}
	
	public int getConType(){
		return conType;
	}
	
	public Hashtable getConParams(){
		return conParams;
	}
	
	public Vector getConnectionParameters(){
		return connectionParameters;
	}
}
