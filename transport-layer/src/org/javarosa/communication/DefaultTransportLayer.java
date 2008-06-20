package org.javarosa.communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.CommConnection;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;

import org.javarosa.communication.bluetooth.client.BluetoothClient;
import org.javarosa.communication.bluetooth.client.BluetoothClientListener;
import org.javarosa.core.util.Settings;
import org.javarosa.util.AlertMessage;
import org.javarosa.util.AlertMessageListener;
import org.javarosa.util.SimpleOrderedHashtable;
import org.javarosa.util.db.Persistent;

import com.tinyline.util.GZIPInputStream;


/**
 * Abstracts the communication details.
 * This class is threaded such that the user does not have to deal with threading issues
 * which are a must for midlets handling blocking calls. When requests are processed
 * the user is notified by callbacks through the BluetoothClientEventListener interface .
 * 
 * @author Daniel Kayiwa
 *
 */
public class DefaultTransportLayer implements Runnable, TransportLayer, BluetoothClientListener, AlertMessageListener, MessageListener{

	public static final String BLUETOOTH_SERVER_ID = "F0E0D0C0B0A000908070605040302010";
	
	/** No action is currently in progress. */
	private int ACTION_NONE = 0;
	
	/** Currently processing a user request line form download, upload, etc. */
	private int ACTION_PROCESSING_REQUEST = 1;
	
	/** The connection type to use. */
	private int conType = CON_TYPE_NULL;
	
	/** The connection parameters. */
	protected Hashtable conParams = new Hashtable();
	
	protected Vector connectionParameters = new Vector();
	
	/** Data request parameters. */
	protected Persistent dataInParams;
	
	/** Data receive parameters. */
	protected Persistent dataOutParams;
	
	/** Data to be sent. */
	protected Persistent dataIn;
	
	/** Data received. */
	protected Persistent dataOut;
	
	/** Reference to the listener for communication events. */
	protected TransportLayerListener eventListener;
	
	/** Flag which when true mean we are downloading data, orelse we are uploading. */
	protected boolean isDownload;
	
	/** The display reference for user selecting the connection type. */
	private Display display;
	
	/** The screen to display after closing our connection type selection screen. */
	private Displayable prevScreen;
	
	/** The alert, if any, to display after closing our connection type selection screen. */
	private Alert currentAlert;
		
	/** The action currently processed. */
	private int currentAction = ACTION_NONE;
	
	private SimpleOrderedHashtable conTypes;
	
	private AlertMessage alertMsg;
	
	private String title = "Server Connection"; //TODO Should be parameterised.
	
	private ConnectionSettings conSettings;
	
	private String serviceUrl;
	
	private static final String KEY_CONNECTION_TYPE = "CONNECTION_TYPE";
	private static final String STORAGE_NAME_SETTINGS = "fcitmuk.DefaultTransportLayer";
	private static final String STORAGE_NAME_HTTP_SETTINGS = "fcitmuk.util.HttpSettings";
	
	private Connection con;
	BluetoothClient btClient;
	private boolean cancelled = false;
	private boolean cancelPrompt = false;
	
	private byte[] smsdata = null;
	private int smsTotalLen = 0;
	private int smsCurrentLen = 0;
	
	String userName = "";
	String password = "";
	
	/** Cconstructs a transport layer object. */
	public DefaultTransportLayer(){
		super();
		initConnectionTypes();
		conSettings = new ConnectionSettings();
	}
	
	/**
	 * Constructs a transport layer object with the following parameters:
	 * 
	 * @param display - reference to the display.
	 * @param displayable - the screen to show after closing any of ours.
	 */
	public DefaultTransportLayer(Display display, Displayable displayable){
		this();
		setDisplay(display);
		setPrevScreen(displayable);
	}
	
	protected void initConnectionTypes(){
		conTypes = new SimpleOrderedHashtable();
		conTypes.put(new Integer(TransportLayer.CON_TYPE_HTTP), "Http");
		conTypes.put(new Integer(TransportLayer.CON_TYPE_BLUETOOTH), "Bluetooth");
		conTypes.put(new Integer(TransportLayer.CON_TYPE_CABLE), "Data Cable");
		conTypes.put(new Integer(TransportLayer.CON_TYPE_SMS), "SMS");
		conTypes.put(new Integer(TransportLayer.CON_TYPE_FILE), "File");
		
		loadUserSettings(); //TODO Some issues to resolve here.
	}
	
	private void loadUserSettings(){
		Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
		if(settings.getSetting(KEY_CONNECTION_TYPE) != null)
			conType = Integer.parseInt(settings.getSetting(KEY_CONNECTION_TYPE)); //CON_TYPE_BLUETOOTH - 1;
		
		Enumeration keys = settings.keys();
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			if(isConnectionParam(key))
				conParams.put(key, settings.getSetting(key));
		}
		
		settings = new Settings(STORAGE_NAME_HTTP_SETTINGS,true);
		keys = settings.keys();
		while(keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			connectionParameters.addElement(new ConnectionParameter(TransportLayer.CON_TYPE_HTTP,key,settings.getSetting(key)));
		}
	}
	
	private static boolean isConnectionParam(String key){
		return !key.equals("LAST_SELECTED_MAIN_MENU_ITEM"); //TODO This is very very bad design. Should change it.
	}
	
	protected void addConnectionType(int Id, String name){
		conTypes.put(new Integer(Id), name);
	}
	
	public void setCommnucationParameter(String key, String value){
		conParams.put(key, value);
	}
	
	public void setDefaultCommnucationParameter(String key, String value){
		if(!conParams.containsKey(key))
			conParams.put(key, value);
	}
	
	protected TransportLayerListener getEventListener(){
		return this.eventListener;
	}
	
	/**
	 * Downloads data over the transport layer.
	 * 
	 * @param dataInParams - Data input connection parameters. eg which request type.
	 * @param dataIn - Data to be sent if any.
	 * @param dataOutParams - Data received parameters. eg failure or success status.
	 * @param dataOut - Data received.
	 * @param eventListener - Reference to listener for communication events.
	 */
	public void download(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut,TransportLayerListener eventListener, String userName, String password){
		saveParameters(dataInParams,dataIn,dataOutParams,dataOut,eventListener,true,userName, password);
		new Thread(this).start();
		//handleRequest();
	}
	
	/**
	 * Uploads data over the transport layer.
	 * 
	 * @param dataInParams - Data input connection parameters. eg which request type.
	 * @param dataIn - Data to be sent.
	 * @param dataOutParams - Data received parameters. eg failure or success status.
	 * @param dataOut - Data received if any.
	 * @param eventListener - Reference to listener to communication events.
	 */
	public void upload( Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut,TransportLayerListener eventListener, String userName, String password){
		saveParameters(dataInParams,dataIn,dataOutParams,dataOut,eventListener,false,userName, password);
		new Thread(this).start();
	}
	
	/**
	 * Saves parameters to class level variables.
	 * 
	 * @param dataInParams - Data input connection parameters. eg which request type.
	 * @param dataIn - Data to be sent if any.
	 * @param dataOutParams - Data received parameters. eg failure or success status.
	 * @param dataOut - Data received if any.
	 * @param eventListener - Reference to listener to communication events.
	 */
	private void saveParameters(Persistent dataInParams, Persistent dataIn, Persistent dataOutParams, Persistent dataOut,TransportLayerListener eventListener, boolean isDownload, String userName, String password){
		this.dataInParams = dataInParams;
		this.dataIn = dataIn;
		this.dataOutParams = dataOutParams;
		this.dataOut = dataOut;
		this.eventListener = eventListener;
		this.isDownload = isDownload;
		this.userName = userName;
		this.password = password;
	}
	
	/** 
	 * Called when the thread starts to run the user request.
	 *
	 */
	protected void handleRequest() {
		
		cancelled = false;
		btClient = null;
		
		//TODO Need to parameterise the title.
		alertMsg  = new AlertMessage(display, title, prevScreen,this);
		
		showConnectionProgress("Connecting to server.........");
				
		try{
			switch(conType){
			case CON_TYPE_HTTP:
				connectHttp();
				break;
			case CON_TYPE_BLUETOOTH:
				connectBluetooth();
				break;
			case CON_TYPE_CABLE:
				connectSerial();
				break;
			case CON_TYPE_SMS:
				connectSMS();
				break;
			case CON_TYPE_FILE:
				connectFile();
				break;
			case CON_TYPE_NULL:
				this.currentAction = this.ACTION_PROCESSING_REQUEST;
				getUserSettings(this.display,this.prevScreen,userName,password);
				break;
			default:
				break;
			}
		}catch(IOException e){//TODO This text needs to be in a resource file for localisation support.
			this.eventListener.errorOccured("Problem handling IOException request",e);
			e.printStackTrace();
		}catch(Exception e){
			this.eventListener.errorOccured("Problem handling Exception request:",e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes and writes data to and from the streams.
	 * @param dos - Stream to write data to.
	 * @param dis - Stream to read data from.
	 * @throws IOException - Thrown when there is a problem for a read or write operation.
	 */
	protected void handleStreams(DataOutputStream dos,DataInputStream dis) throws IOException {
		try{			
			showConnectionProgress("Connected to server. Tranfering data........");
			
			if(dataInParams != null) //for now http will not send this value.
				dataInParams.write(dos); 

			if(dataIn != null)
				dataIn.write(dos);
			
			dos.flush(); //if you dont do this, the client will block on a read.
			dos.close();
			dos = null; //Not setting to null results in KErrCouldNotConnect on the client.
				
			dis = getDecompressedStream(dis);
			
			//Turning this off for now to reduce payload.
			/*if(dataOutParams != null) 
				dataOutParams.read(dis);*/
			
			if(dataOut != null) //FO cases where we are not getting any data back.
				dataOut.read(dis); //When the out param shows failure status, this can be null hence throwing EOF exceptions.
			
			if(this.isDownload)
				this.eventListener.downloaded(dataOutParams,dataOut);
			else
				this.eventListener.uploaded(dataOutParams,dataOut);
			
		}catch(InstantiationException e){
			this.eventListener.errorOccured("InstantiationException Problem handling streams",e);
			e.printStackTrace(); //TODO May need to report this to user.
		}catch(IllegalAccessException e){
			this.eventListener.errorOccured("IllegalAccessException Problem handling streams",e);
			e.printStackTrace(); //TODO May need to report this to user.
		}catch(InterruptedIOException e){
			this.eventListener.errorOccured("InterruptedIOException Problem handling streams",e);
			e.printStackTrace(); //TODO May need to report this to user.
		}catch(EOFException e){
			this.eventListener.errorOccured("EOFException Problem handling streams",e);
			e.printStackTrace(); //TODO May need to report this to user.
		}catch(IOException e){
			this.eventListener.errorOccured("IOException Problem handling streams",e);
			e.printStackTrace(); //TODO May need to report this to user.
		}catch(Exception e){
			this.eventListener.errorOccured("AA Exception Problem handling streams",e);
			e.printStackTrace(); //TODO May need to report this to user.
		}finally{
			dis.close();
			dis = null;
		}
	}
	
	/**
	 * Handles File communications. eg memory card transfer.
	 * 
	 * @throws IOException - Thrown when there is a problem reading from or writting
	 * 						 to the connection stream.
	 */
	protected void connectFile() throws IOException{
		
	}
	
	/**
	 * Handles HTTP communications. eg GPRS.
	 * 
	 * @throws IOException - Thrown when there is a problem reading from or writting
	 * 						 to the connection stream.
	 */
	protected void connectHttp() throws IOException{
		
		//HttpConnection  con = null;
		
		try{
			String HTTP_URL = (String)conParams.get(TransportLayer.KEY_HTTP_URL);  
			con = (HttpConnection)Connector.open(HTTP_URL);

			showConnectionProgress("Connected to server. Tranfering data........");
			
			((HttpConnection)con).setRequestMethod(HttpConnection.POST);
			((HttpConnection)con).setRequestProperty("Content-Type","application/octet-stream");
			((HttpConnection)con).setRequestProperty("User-Agent","Profile/MIDP-2.0 Configuration/CLDC-1.0");
			((HttpConnection)con).setRequestProperty("Content-Language", "en-US");

			//For HTTP, we dont write the dataInParam since the action type is determined by the URL.
			if(dataIn != null){
				DataOutputStream dos = ((HttpConnection)con).openDataOutputStream();
				dataIn.write(dos);
				dos.flush();
			}
			
			int status  = ((HttpConnection)con).getResponseCode();
			if(status != HttpConnection.HTTP_OK)
				this.eventListener.errorOccured("Response code not OK="+status, null);
			else {//TODO May need some more specific failure codes
				//For HTTP, we get only data back. Status is via HTTP status code. So no need of readin the data in param.				
				if(dataOut != null) //There are cases where we are not getting any data back apart fomr the HTTP status code.					
					readHttpData(((HttpConnection)con).openDataInputStream());
			}
		}
		catch(SecurityException e){
			this.eventListener.errorOccured("Permission denied",e);
			e.printStackTrace();
		}
		catch(DataReadException e){
			this.eventListener.errorOccured(null,e);
			e.printStackTrace();
		}
		catch(Exception e){
			this.eventListener.errorOccured("Problem handling connectHttp request",e);
			e.printStackTrace();
		}
		finally{
			if(con != null)
				con.close();
		}
	}
	
	private void readHttpData(DataInputStream dis) throws Exception{
		
		try{
			dataOut.read(getDecompressedStream(dis));
			
			if(this.isDownload)
				this.eventListener.downloaded(dataOutParams,dataOut);
			else
				this.eventListener.uploaded(dataOutParams,dataOut);
		}
		catch(Exception e){
			throw new DataReadException("Problem reading data from the server." +(e.getMessage() != null ? e.getMessage() : ""));
		}
	}
	
	/**
	 * Gets
	 * 
	 * @param dis
	 * @return
	 * @throws IOException
	 */
	private DataInputStream getDecompressedStream(DataInputStream dis) throws IOException{
		GZIPInputStream gzip  = new GZIPInputStream(dis);
		return new DataInputStream(gzip);
	}
	
	private void resetSmsDataParams(){
		smsdata = null;
		smsCurrentLen = 0;
		smsTotalLen = 0;	
	}
	
	/**
	 * Handles SMS communications..
	 * 
	 * @throws IOException - Thrown when there is a problem reading from or writting
	 * 						 to the connection stream.
	 */
	protected void connectSMS() throws IOException,Exception{
		showConnectionProgress("SMS Connected to server. Tranfering data........");
		
		closeConnection();
		resetSmsDataParams();
		
		String destAddress = (String)conParams.get(TransportLayer.KEY_SMS_DESTINATION_ADDRESS); //"sms://+256772963035:1234"; //"sms://+256772963035:1234";;
		String srcAddress = (String)conParams.get(TransportLayer.KEY_SMS_SOURCE_ADDRESS); //"sms://:3333";
		con = (MessageConnection)Connector.open(srcAddress);
		((MessageConnection)con).setMessageListener(this);
		//PushRegistry.listConnections(true);
		
		BinaryMessage msg = (BinaryMessage)((MessageConnection)con).newMessage(MessageConnection.BINARY_MESSAGE);
		msg.setPayloadData(getPayLoadData());
		msg.setAddress(destAddress);
		((MessageConnection)con).send(msg);
					
		showConnectionProgress("Sent Message. Waiting for reply...");
	}
	
	private byte[] getPayLoadData(){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			if(dataInParams != null)
				dataInParams.write(dos);
			if(dataIn != null)
				dataIn.write(dos);
			return baos.toByteArray();
		}
		catch(Exception e){
			this.eventListener.errorOccured("Problem getting payload data",e);
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 
	 * @throws IOException- Thrown when there is a problem reading from or writting
	 * 						 to the connection stream.
	 * @throws Exception- Thrown when serial communication is not implemented.
	 */
	protected void connectSerial() throws IOException,Exception {
		
		CommConnection  con = null;
		
		try{
			//"comm:IR0";
			//String CON_STR = (String)conParams.get("CON_STR"); // + ";baudrate=115200;" nokiacomm
			String url = "comm:" + getPort() + ";baudrate=9600";
			con = (CommConnection)Connector.open(url,Connector.READ_WRITE,true);
				
			//TODO May need to check success status
			handleStreams(con.openDataOutputStream(),con.openDataInputStream());
		}finally{
			if(con != null)
				con.close();
		}
	}
	
	private String getPort() throws Exception{
		String port1;
		 String ports = System.getProperty("microedition.commports");
		 if(ports == null)
			 throw new Exception("Serial Communication not implemented.");
		 
		 int comma = ports.indexOf(',');
		 if (comma > 0) {
		     // Parse the first port from the available ports list.
		     port1 = ports.substring(0, comma);
		 } else {
		     // Only one serial port available.
		     port1 =ports;
		 }
		 //System.out.println(port1);
		 return port1;
	}
	
	private boolean openBluetoothConnection(){		
		try{
			con = (StreamConnection)Connector.open(serviceUrl,Connector.READ_WRITE,true);
			return true;
		}catch(Exception e){
			//Thread.currentThread().notifyAll();
			if(con != null){
				try{
				con.close();
				}catch(Exception ex){}
				con = null; //Not setting this to null results into KErrAlreadyExists  failures on the client
			}
			openBluetoothConnection();
		}
		return true;
	}
	
	/**
	 * Handles bluetooth communications.
	 *
	 */
	protected void connectBluetooth() throws IOException{
		try{
			if(serviceUrl == null){
				showConnectionProgress("Getting bluetooth service url.........");
				String bluetooth_server_id = (String)conParams.get(TransportLayer.KEY_BLUETOOTH_SERVER_ID);
				btClient = new BluetoothClient(bluetooth_server_id,this);
				serviceUrl = btClient.getServiceUrl();
			}
		
			if(serviceUrl == null)
				return; //An error message was already reported by whatever made this null.

			showConnectionProgress("Opening bluetooth server connection.........");
			//TODO May need to check success status
			if(openBluetoothConnection()){
				showConnectionProgress("Getting streams.........");
				handleStreams(((StreamConnection)con).openDataOutputStream(),((StreamConnection)con).openDataInputStream());
			}
			else
				this.eventListener.errorOccured("Failed to open connection", null);
		}finally{
			if(con != null){
				con.close();
				con = null; //Not setting this to null results into KErrAlreadyExists  failures on the client
			}
		}
	}
	
	/** 
	 * Called when the thread starts executing.
	 */
	public void run(){
		handleRequest();
	}
	
	private void showConnectionProgress(String message){
		alertMsg.showProgress(title, message);
	}
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void onConnectionSettingsClosed(boolean save) {
		try{
	        if(save){
	        	serviceUrl = null; //May need to get a new url just incase the bluetooth address is changed by user.
	        	
	        	conType = conSettings.getConType();
	        	conParams = conSettings.getConParams();
	        	connectionParameters = conSettings.getConnectionParameters();
					
	        	Settings settings = new Settings(STORAGE_NAME_SETTINGS,true);
	        	
				Enumeration keys = conParams.keys();
				while(keys.hasMoreElements()){
					String key = (String)keys.nextElement();
					settings.setSetting(key, (String)conParams.get(key));
				}
				
				settings.setSetting(KEY_CONNECTION_TYPE, String.valueOf(conType));
				settings.saveSettings();
				
				settings = new Settings(STORAGE_NAME_HTTP_SETTINGS,true);	        	
				for(int i=0; i<connectionParameters.size(); i++)
				{
					ConnectionParameter conParam = (ConnectionParameter)connectionParameters.elementAt(i);
				    settings.setSetting(conParam.getName(), conParam.getValue());
				}
				settings.saveSettings();
		    }
	
	        if(currentAlert != null)
	        	display.setCurrent(currentAlert, prevScreen);
	        
	       	//Check to see if we were invoked due to a user request where
	    	//the connection type was not specified.
	    	if(save && this.currentAction == this.ACTION_PROCESSING_REQUEST)
	    		new Thread(this).start(); 
	     	else
	    		display.setCurrent(prevScreen);
	    	
	    	this.currentAction = this.ACTION_NONE;
		}
		catch(Exception e){
			alertMsg.showError(e.getMessage());
			e.printStackTrace();
		}
    }

	public int getConType() {
		return conType;
	}

	/** Connection type. Set to -1 if you want to display a select connection type screen. */
	public void setConType(int conType) {
		this.conType = conType;
	}

	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public Displayable getPrevScreen() {
		return prevScreen;
	}

	public void setPrevScreen(Displayable prevScreen) {
		this.prevScreen = prevScreen;
	}

	public Alert getCurrentAlert() {
		return currentAlert;
	}

	public void setCurrentAlert(Alert currentAlert) {
		this.currentAlert = currentAlert;
	}
	
	public void errorOccured(String errorMessage, Exception e){
		if(!cancelled) //If user canclled, we dont need to bubble the connection interrupt error message.
			this.eventListener.errorOccured(errorMessage, e);
	}
	
	public void onAlertMessageOk(){
		//For now, cancelling is only for bluetooth since it seems to be the slow one
		//when searching for bluetooth devices and services.
		if(cancelPrompt){
			if(btClient != null) 
				btClient.cancelSearch();
			cancelled = true;
			cancelPrompt = false;
		}
		
		if(con != null){
			try{
				con.close();
			}catch(IOException e){e.printStackTrace();}
		}
		display.setCurrent(prevScreen);
	}
	
	public void onAlertMessageCancel(){
		if(cancelPrompt){ //User was asked if really want to cancel current operation and said NO.
			alertMsg.showProgress(); //continue with the current progress operation.
			cancelPrompt = false;
		}
		else{
			if(!cancelled){ //if not already cancelled
				alertMsg.showConfirm("Do you really want to cancel this operation?");
				cancelPrompt = true;
			}
			else
				display.setCurrent(prevScreen);
		}
	}
	
	private int getSize(byte[] b){
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
		try{
			return dis.readInt();
		}catch(IOException e){return -1;}
	}
	
	public void notifyIncomingMessage(MessageConnection msgCon){
		try{
			Message msg = msgCon.receive();
			
			if(msg instanceof BinaryMessage){
				BinaryMessage binMsg = (BinaryMessage)msg;
				
				byte[] payload = binMsg.getPayloadData();
				
				if(smsdata == null){
					smsTotalLen = getSize(payload); //read off first four bytes for total length.
					smsdata = new byte[smsTotalLen]; //create storage space.
					smsCurrentLen = payload.length - 4; //actual data is minus the four length bytes.
					System.arraycopy(payload,4,smsdata,0,smsCurrentLen);
				}
				else{
					System.arraycopy(payload,0,smsdata,smsCurrentLen,payload.length);
					smsCurrentLen = (smsCurrentLen + payload.length);
				}				
						
				if(smsCurrentLen == smsTotalLen){ //if all bytes have been read.
					DataInputStream dis = new DataInputStream(new ByteArrayInputStream(smsdata));
					dis = this.getDecompressedStream(dis);
					
					if(dataOut != null)
						dataOut.read(dis);
					if(this.isDownload)
						this.eventListener.downloaded(dataOutParams,dataOut);
					else
						this.eventListener.uploaded(dataOutParams,dataOut);
					
					resetSmsDataParams();
				}
				else
					showConnectionProgress("Transferred " + smsCurrentLen + " of " + smsTotalLen + " bytes. " + (smsTotalLen-smsCurrentLen) + " left.");
			}
			else
				this.eventListener.errorOccured("notifyIncomingMessage: Some non non binary text message.",null);
		}
		catch(Exception e){
			this.eventListener.errorOccured("Problem handling notifyIncomingMessage",e);
			e.printStackTrace();
		}
	}
	
	private void closeConnection(){
		try{
			if(con != null){
				con.close();
				con = null;
			}
		}catch(Exception e){}
	}
	
	/** 
	 * Displays a screen for the user to select a connection type.
	 * 
	 * @param display - reference to the current display.
	 * @param prevScreen - the screen to display after dismissing our screens.
	 */
	public void getUserSettings(Display display, Displayable prevScreen, String name, String password){
		this.prevScreen = prevScreen;
		conSettings.setDisplay(display);
		conSettings.setPrevScreen(prevScreen);
		conSettings.setTitle("Connection Settings");
		conSettings.getUserSettings(display, prevScreen,conType,conTypes,conParams,this,name,password,connectionParameters);
	}
	
	public void addConnectionParameter(ConnectionParameter conParam)
	{
		//System.out.println(connectionParameters.size() + " addConnectionParameter");
		for(int i=0; i<connectionParameters.size(); i++)
		{
			ConnectionParameter cp = (ConnectionParameter)connectionParameters.elementAt(i);
			if(cp.getName().equals(conParam.getName()))
				return; //If parameter alread exists, then we preserve the user customized value and dont overwrite it with the api hard coded one.
		}
		
		connectionParameters.addElement(conParam);
	}
	
	public String getConnectionParameterValue(int conType, String name)
	{		
		for(int i=0; i<connectionParameters.size(); i++)
		{
			ConnectionParameter cp = (ConnectionParameter)connectionParameters.elementAt(i);
			if(cp.getConnectionType() == conType && cp.getName().equals(name))
				return cp.getValue(); //If parameter alread exists, then we preserve the user customized value and dont overwrite it with the api hard coded one.
		}
		
		return null;
	}
}
