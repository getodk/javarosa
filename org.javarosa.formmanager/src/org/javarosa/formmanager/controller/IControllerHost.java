package org.javarosa.formmanager.controller;

import javax.microedition.lcdui.*;

public interface IControllerHost {
	void setDisplay (Displayable view);
	
	void controllerReturn (String status);
}
