package org.javarosa.formmanager.controller;

import org.javarosa.core.api.IView;

public interface IControllerHost {
	void setView (IView view);
	
	void controllerReturn (String status);
}
