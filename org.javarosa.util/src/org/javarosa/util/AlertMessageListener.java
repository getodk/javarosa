package org.javarosa.util;


/** 
 * This interface is the means of communication betwen the AlerMessage class and api users. 
 * 
 * @author Daniel
 *
 */
public interface AlertMessageListener {
	
	/**Called when the OK commad of an alert is clicked. */
	public void onAlertMessageOk();
	
	/** Called when the Cancel command of an alert is clicked. */
	public void onAlertMessageCancel();
}
