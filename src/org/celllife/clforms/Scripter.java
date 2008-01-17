package org.celllife.clforms;

import org.celllife.clforms.api.Prompt;
import org.celllife.clforms.api.ResponseEvent;
import org.celllife.clforms.view.IPrompter;

public class Scripter implements IPrompter {

	private Controller controller;
	
	public void registerController(Controller controller) {
		this.controller = controller;
	}

	public void showPrompt(Prompt prompt) {
		prompt.setValue(getValueFromScript());
		System.out.println(prompt.getLongText());
		controller.processEvent(new ResponseEvent(ResponseEvent.NEXT, -1));
	}


	public String getValueFromScript() {
		return "";
	}

	public void showPrompt(Prompt prompt, int screenIndex, int totalScreens) {
		// TODO Auto-generated method stub
		
	}
}
