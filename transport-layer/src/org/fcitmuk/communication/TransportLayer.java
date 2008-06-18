package org.fcitmuk.communication;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.fcitmuk.db.util.Persistent;

/**
 * Abstracts the communication details.
 * 
 * @author Daniel Kayiwa
 * 
 */
public interface TransportLayer {

	/** Connection type not set. */
	public static final int CON_TYPE_NULL = -1;

	/** HTTP connection. */
	public static final int CON_TYPE_HTTP = 1;

	/** Bluetooth connection. */
	public static final int CON_TYPE_BLUETOOTH = 2;

	/** Serial connection. eg USB,Infrared. */
	public static final int CON_TYPE_CABLE = 3;

	/** Sending data as an sms. */
	public static final int CON_TYPE_SMS = 4;

	/** Sending data around as files on say memory card. */
	public static final int CON_TYPE_FILE = 5;

	public static final String KEY_BLUETOOTH_SERVER_ID = "BLUETOOTH_SERVER_ID";

	public static final String KEY_HTTP_URL = "HTTP_URL";

	public static final String KEY_USER_DOWNLOAD_HTTP_URL = "USER_DOWNLOAD_HTTP_URL";

	// public static final String KEY_PATIENT_DOWNLOAD_HTTP_URL =
	// "PATIENT_DOWNLOAD_HTTP_URL";
	public static final String KEY_FORM_DOWNLOAD_HTTP_URL = "FORM_DOWNLOAD_HTTP_URL";

	public static final String KEY_DATA_UPLOAD_HTTP_URL = "DATA_UPLOAD_HTTP_URL";

	public static final String KEY_POST_DATA = "POST_DATA";

	public static final String KEY_SMS_DESTINATION_ADDRESS = "KEY_SMS_DESTINATION_ADDRESS";

	public static final String KEY_SMS_SOURCE_ADDRESS = "KEY_SMS_SOURCE_ADDRESS";

	/**
	 * Sets the value of a communication parameter. If it already exists, it is
	 * overwritten.
	 * 
	 * @param key
	 * @param value
	 */
	public void setCommnucationParameter(String key, String value);
	
	/**
	 * Addss a new connection parameter.
	 * 
	 * @param conParam
	 */
	public void addConnectionParameter(ConnectionParameter conParam);
	
	/**
	 * 
	 * @param conType
	 * @param name
	 * @return
	 */
	public String getConnectionParameterValue(int conType, String name);

	/**
	 * Sets the value of a communication parameter if it does not already exist.
	 * 
	 * @param key
	 * @param value
	 */
	public void setDefaultCommnucationParameter(String key, String value);

	/**
	 * Downloads data over the transport layer.
	 * 
	 * @param dataInParams -
	 *            Data input connection parameters. eg which request type.
	 * @param dataIn -
	 *            Data to be sent if any.
	 * @param dataOutParams -
	 *            Data received parameters. eg failure or success status.
	 * @param dataOut -
	 *            Data received.
	 * @param eventListener -
	 *            Reference to listener for communication events.
	 */
	public void download(Persistent dataInParams, Persistent dataIn,
			Persistent dataOutParams, Persistent dataOut,
			TransportLayerListener eventListener, String userName,
			String password);

	/**
	 * Uploads data over the transport layer.
	 * 
	 * @param dataInParams -
	 *            Data input connection parameters. eg which request type.
	 * @param dataIn -
	 *            Data to be sent.
	 * @param dataOutParams -
	 *            Data received parameters. eg failure or success status.
	 * @param dataOut -
	 *            Data received if any.
	 * @param eventListener -
	 *            Reference to listener to communication events.
	 */
	public void upload(Persistent dataInParams, Persistent dataIn,
			Persistent dataOutParams, Persistent datOut,
			TransportLayerListener eventListener, String userName,
			String password);

	/**
	 * Displays a screen for the user to select a connection type.
	 * 
	 * @param display -
	 *            reference to the current display.
	 * @param prevScreen -
	 *            the screen to display after dismissing our screens.
	 */
	public void getUserSettings(Display display, Displayable prevScreen,
			String name, String password);

	public int getConType();

	/**
	 * Connection type. Set to -1 if you want to display a select connection
	 * type screen.
	 */
	public void setConType(int conType);

	public Display getDisplay();

	public void setDisplay(Display display);

	public Displayable getPrevScreen();

	public void setPrevScreen(Displayable prevScreen);

	public Alert getCurrentAlert();

	public void setCurrentAlert(Alert currentAlert);
}
