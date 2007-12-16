package org.celllife.clforms.view;

import org.celllife.clforms.Controller;
import org.celllife.clforms.api.Prompt;

public interface FormView {
	
	public void registerController(Controller controller);
	
	public void showPrompt(Prompt prompt);

	//public void showDefaultPrompt(Prompt prompt);

}
