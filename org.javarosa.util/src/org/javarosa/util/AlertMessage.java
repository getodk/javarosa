package org.javarosa.util;

import javax.microedition.lcdui.*;


/**
 * Displays alert messages to the user.
 * 
 * @author Daniel
 *
 */
public class AlertMessage implements CommandListener {

	private Display display;
	private Displayable prevScreen;
	private boolean alertMode = false;
	private String title;
	private AlertMessageListener listener;
	private Alert progressAlert;
	
	public AlertMessage(Display display,String title,Displayable prevScreen,AlertMessageListener listener){
		setDisplay(display);
		setTitle(title);
		setPrevScreen(prevScreen);
		setListener(listener);
	}
	
	public boolean isAlertMode(){
		return this.alertMode;
	}
	
	/**
	 * Display a confirmation message.
	 * 
	 * @param message - the message text.
	 */
	public void showConfirm(String message){
		Alert alert = new Alert(title,message, null,AlertType.CONFIRMATION);
		alert.setTimeout(Alert.FOREVER);
		alert.addCommand(DefaultCommands.cmdYes);
		alert.addCommand(DefaultCommands.cmdNo);
		alert.setCommandListener(this);
		display.setCurrent(alert,prevScreen);
		alertMode = true;
	}
	
	public void showWarningMessage(String message){
		show(message,AlertType.CONFIRMATION);
	}
	 
	/**
	 * Displays an error message.
	 * 
	 * @param message - the message text.
	 */
	public void showError(String message){
		show(message,AlertType.ERROR);
	}
	
	/**
	 * Displays a message.
	 * 
	 * @param message - the actual message text.
	 */
	public void show(String message){
		show(message,AlertType.ALARM);
	}
	
	/**
	 * Displays a message.
	 * 
	 * @param message - the actual message text.
	 * @param alertType - the type of message. eg erro, info, etc.
	 */
	public void show( String message,AlertType alertType){
		Alert alert = new Alert(title,message, null,alertType);
		alert.setTimeout(Alert.FOREVER);
		alert.addCommand(DefaultCommands.cmdOk);
		alert.setCommandListener(this);
		display.setCurrent(alert,prevScreen);
		alertMode = true;
	}
	
	/**
	 * Displays a progress window.
	 * 
	 * @param tittle - the title.
	 * @param message - the message text.
	 */
	public void showProgress(String title, String message){
		Alert alert = new Alert(title,message,null, null);
		alert.setTimeout(Alert.FOREVER);
		Gauge indicator = new Gauge(null, false,Gauge.INDEFINITE,Gauge.CONTINUOUS_RUNNING);
		alert.setIndicator(indicator);
		alert.addCommand(DefaultCommands.cmdCancel);
		alert.setCommandListener(this);
		display.setCurrent(alert,prevScreen);
		alertMode = true;
		progressAlert = alert;
	}
	
	/**
	 * Displays a message.
	 * 
	 * @param message - the actual message text.
	 */
	public void showProgress(){
		display.setCurrent(progressAlert,prevScreen);
	}
	
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		try{
			if(c == DefaultCommands.cmdOk || c == DefaultCommands.cmdYes)
				handleOkCommand(d);
			else if(c == DefaultCommands.cmdCancel || c == DefaultCommands.cmdNo)
				handleCancelCommand(d);
		}
		catch(Exception e){
			showError(e.getMessage());
			e.printStackTrace();
		}
		
		this.alertMode = false;
	}
	
	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		if(this.listener != null)
			this.listener.onAlertMessageCancel();
	}
	
	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){
		if(this.listener != null)
			this.listener.onAlertMessageOk();
	}
	
	/** Turns off the current alert. */
	public void turnOffAlert(){
		display.setCurrent(prevScreen);
	}

	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public AlertMessageListener getListener() {
		return listener;
	}

	public void setListener(AlertMessageListener listener) {
		this.listener = listener;
	}

	public Displayable getPrevScreen() {
		return prevScreen;
	}

	public void setPrevScreen(Displayable prevScreen) {
		this.prevScreen = prevScreen;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setAlertMode(boolean alertMode) {
		this.alertMode = alertMode;
	}
}
