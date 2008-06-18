package org.javarosa.clforms.view;

import javax.microedition.lcdui.Display;

import org.javarosa.clforms.Controller;
import org.javarosa.clforms.api.Prompt;

public interface IPrompter  {
	
	public void showPrompt(Prompt prompt);
	
	public void showPrompt(Prompt prompt, int screenIndex, int totalScreens);

	public void registerController(Controller controller);

	public void showError(String title, String message, Display display);

}
