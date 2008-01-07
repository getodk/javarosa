package org.dimagi.view;

import org.dimagi.chatscreen.ChatScreenForm;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.List;

import org.celllife.clforms.Controller;
import org.celllife.clforms.api.Prompt;
import org.celllife.clforms.api.ResponseEvent;
import org.celllife.clforms.util.J2MEUtil;
import org.celllife.clforms.view.IPrompter;
import org.celllife.clforms.view.FormView;
import org.celllife.clforms.MVCComponent;



public class ChatFormView extends MVCComponent implements FormView {

	private Controller controller;
	//private static Command saveAndReloadCommand;
	private static Command prevCommand = new Command("Prev", Command.ITEM, 3);
	private static Command nextCommand = new Command("Next", Command.ITEM, 3);
	private static Command exitCommand = new Command("Exit", Command.EXIT, 3);
	private static Displayable screen = null;
	private Prompt p;
	private ChatScreenForm chatScreenForm = new ChatScreenForm();
	
	/**
	 * Creates a new DForm.
	 */
	public ChatFormView() {
		System.out.println("ChatterboxPromptScreen2()");
		screen = chatScreenForm;
		screen.addCommand(nextCommand);
		screen.addCommand(prevCommand);
//		screen.addCommand(saveAndReloadCommand);
		screen.addCommand(exitCommand);
	}

	public Displayable getScreen() {
		return screen;
	}
	
	public void commandAction(Command command, Displayable s) {
  		System.out.println("ChatterboxPromptScreen2.commandAction()");
		try {
//			if (command == saveAndReloadCommand) {
//		  		System.out.println("ChatterboxPromptScreen2.saveAndReload()");
//				controller.processEvent(new ResponseEvent(ResponseEvent.SAVE_AND_RELOAD, -1));
//			}
//			else 
			if (command == List.SELECT_COMMAND){
		  		System.out.println("ChatterboxPromptScreen2.select()");
				controller.processEvent(new ResponseEvent(ResponseEvent.GOTO,((List)screen).getSelectedIndex()));
			}
			else if (command == exitCommand){
		  		System.out.println("ChatterboxPromptScreen2.exitCommand()");
				controller.processEvent(new ResponseEvent(ResponseEvent.EXIT, -1));
			} else if (command == nextCommand) {
				System.out.println("ChatterboxPromptScreen2.nextCommand()");
				chatScreenForm.goToNextQuestion();
			} else if (command == prevCommand) {
				System.out.println("ChatterboxPromptScreen2.prevCommand()");
				chatScreenForm.goToPreviousQuestion();
			}			 
		} catch (Exception e) {
			Alert a = new Alert("error.screen" + " 2"); //$NON-NLS-1$
			a.setString(e.getMessage());
			a.setTimeout(Alert.FOREVER);
			display.setCurrent(a);
		}
	}

	// Initialize. If a data member is not backed by RMS, make sure
	// it is uninitilzed (null) before you put in values.
	protected void initModel() throws Exception {
		
	}

	protected void createView() {
  		System.out.println("ChatterboxPromptScreen2.createView()");
  		
  	
  		
//	    saveAndReloadCommand = new Command("SAVE&Reload", Command.ITEM, 3);
//		nextCommand = new Command("Next", Command.ITEM, 3);
//		exitCommand = new Command("Exit", Command.EXIT, 3);

//		screen = chatScreenForm;
//		
//		form = controller.getForm();
//		screen = new List("FORM: "+form.getName(),List.IMPLICIT);
//		form.calculateRelevantAll();
//		for(int i = 0; i<form.getPrompts().size(); i++){
//			
//			if(((Prompt)form.getPrompt(i)).isRelevant()){
//				int type = ((Prompt)form.getPrompts().elementAt(i)).getReturnType();
//				String temp= J2MEUtil.getStringValue(((Prompt)form.getPrompts().elementAt(i)).getValue(),type);
//				//((List)screen ).append(((Prompt)form.getPrompts().elementAt(i)).getLongText()+temp,null);
//				// short text + value
//				((List)screen ).append(((Prompt)form.getPrompts().elementAt(i)).getShortText()+" => " +temp,null);
//				// Long text + value on new line indent
//				//((List)screen ).append(((Prompt)form.getPrompts().elementAt(i)).getLongText()+"\n A:"+temp,null);
//			}
//			
//		}
		
//		screen.addCommand(nextCommand);
//		screen.addCommand(saveAndReloadCommand);
//		screen.addCommand(exitCommand);

	}

	protected void updateView() throws Exception {
  		System.out.println("ChatterboxPromptScreen2.updateView()");
		createView();
	}
	

  	public void registerController(Controller controller) {
  		System.out.println("ChatterboxPromptScreen2.registerController()");
  		this.controller = controller;		
	}

	public void showPrompt(Prompt prompt) {
		System.out.println("ChatterboxPromptScreen2.showPrompt(Prompt)");
		this.p = prompt;
		try{
			createView();
			showScreen();
		}catch(Exception e){
			System.out.println("something wrong in CREAT VIEW\n "+e.getMessage());
			e.printStackTrace();			
		}
		//repaint();
	}
	

}
