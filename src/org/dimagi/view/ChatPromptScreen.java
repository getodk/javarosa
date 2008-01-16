package org.dimagi.view;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.celllife.clforms.IController;
import org.celllife.clforms.MVCComponent;
import org.celllife.clforms.api.Prompt;
import org.celllife.clforms.api.ResponseEvent;
import org.celllife.clforms.view.FormView;
import org.celllife.clforms.view.IPrompter;

import org.dimagi.chatscreen.ChatScreenForm;


public class ChatPromptScreen extends MVCComponent implements IPrompter {

	private IController controller;
	private static Command prevCommand = new Command("Prev", Command.ITEM, 3);
	private static Command nextCommand = new Command("Next", Command.ITEM, 3);
	private static Command goToFormViewCommand = new Command("FormView", Command.ITEM, 3);
	private static Command exitCommand = new Command("Exit", Command.EXIT, 3);
	private static Displayable screen = null;
	private ChatScreenForm chatScreenForm = new ChatScreenForm();
	private int screenIndex;
	private int totalScreens;
	private Prompt p;
	private Vector prompts = new Vector();
	
	
	public ChatPromptScreen() {
		screen = chatScreenForm;
		screen.addCommand(nextCommand);
		screen.addCommand(prevCommand);
		screen.addCommand(goToFormViewCommand);
		screen.addCommand(exitCommand);
	}

	public Displayable getScreen() {
		return screen;
	}
	
	public void commandAction(Command command, Displayable s) {
  	
		try {
		    if (command == exitCommand){
				controller.processEvent(new ResponseEvent(ResponseEvent.EXIT, -1));
			} else if (command == nextCommand) {
				controller.processEvent(new ResponseEvent(ResponseEvent.NEXT, -1));	
			} else if (command == prevCommand) {
				controller.processEvent(new ResponseEvent(ResponseEvent.PREVIOUS, -1));
			} else if ( command == goToFormViewCommand ) {
				controller.processEvent(new ResponseEvent(ResponseEvent.LIST,-1));
			}
		} catch (Exception e) {
			Alert a = new Alert("error.screen" + " 2"); //$NON-NLS-1$
			a.setString(e.getMessage());
			a.setTimeout(Alert.FOREVER);
			display.setCurrent(a);
		}
	}

	// Initialize. If a data member is not backed by RMS, make sure
	// it is uninitialized (null) before you put in values.
	protected void initModel() throws Exception {
		
	}

	protected void createView() {
	}

	protected void updateView() throws Exception {}
	

  	public void registerController(IController controller) {
  		this.controller = controller;		
	}

	public void showPrompt(Prompt prompt) {
		System.out.println("ChatPromptScreen.showPrompt(prompt)");
		this.p = prompt;
		showScreen();
	}

	public void showPrompt(Prompt prompt, int screenIndex, int totalScreens) {
		System.out.println("ChatPromptScreen.showPrompt(prompt, screenIndex, totalScreens)");
		System.out.println(prompt.getLongText() + " " + screenIndex + " " + totalScreens);
		this.p = prompt;
		this.prompts.addElement(p);
		chatScreenForm.setPrompts(prompts);
		this.screenIndex = screenIndex;
		this.totalScreens = totalScreens;
		chatScreenForm.draw(screenIndex);
		//chatScreenForm.goToNextPrompt(screenIndex);
		showScreen();
	}
	
}
