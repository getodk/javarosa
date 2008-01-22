package org.javarosa.clforms.view;

import org.javarosa.clforms.Controller;
import org.javarosa.clforms.api.Prompt;

public interface FormView {
	
	public void registerController(Controller controller);
	
	public void showPrompt(Prompt prompt);

	//public void showDefaultPrompt(Prompt prompt);

}
