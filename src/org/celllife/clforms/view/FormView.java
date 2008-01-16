package org.celllife.clforms.view;

import org.celllife.clforms.IController;
import org.celllife.clforms.api.Prompt;

public interface FormView {
	
	public void registerController(IController controller);
	
	public void showPrompt(Prompt prompt);

	//public void showDefaultPrompt(Prompt prompt);

}
