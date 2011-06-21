package org.javarosa.communication.sim.bluetooth;

import javax.microedition.lcdui.*;
import org.javarosa.core.api.*;

/**
 * AlertEngine extends Alert and implements the interface, IView
 * */
public class AlertEngine extends Alert implements IView{

	public AlertEngine(String title, String alertText, Image alertImage,
			AlertType alertType) {
		super(title, alertText, alertImage, alertType);
		// TODO Auto-generated constructor stub
	}

	public Object getScreenObject() {
		// TODO Auto-generated method stub
		return this;
	}

}
