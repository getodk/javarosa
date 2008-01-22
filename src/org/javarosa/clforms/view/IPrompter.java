package org.javarosa.clforms.view;

import org.javarosa.clforms.Controller;
import org.javarosa.clforms.api.Prompt;

public interface IPrompter  {
	
	public void showPrompt(Prompt prompt);
	
	public void showPrompt(Prompt prompt, int screenIndex, int totalScreens);

	public void registerController(Controller controller);

}
