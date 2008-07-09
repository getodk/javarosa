package org.javarosa.core.formcontroller;

import org.javarosa.core.FormController;
import org.javarosa.clforms.api.Prompt;

public interface FormView {
	
	public void registerController(FormController controller);
	
	public void displayPrompt(Prompt prompt);

	public void destroy();
	
	//public void showDefaultPrompt(Prompt prompt);

}