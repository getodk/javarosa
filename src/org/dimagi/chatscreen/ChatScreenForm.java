package org.dimagi.chatscreen;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import minixpath.XPathExpression;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import org.celllife.clforms.IController;
import org.celllife.clforms.MVCComponent;
import org.celllife.clforms.api.Prompt;
import org.celllife.clforms.api.ResponseEvent;
import org.celllife.clforms.storage.Model;
import org.celllife.clforms.view.FormView;
import org.celllife.clforms.view.IPrompter;
import org.celllife.clforms.util.SimpleOrderedHashtable;
import org.celllife.clforms.util.J2MEUtil;
import javax.microedition.lcdui.CommandListener;

import org.dimagi.utils.ViewUtils;


/**
 * The base container for the Chat Screen interface.
 * 
 * The ChatScreenForm is responsible for containing and laying out frames,
 * as well as the control logic used for navigation of protocols.
 * 
 * It is the primary interface between the Protocol object and the View.
 * 
 * @author ctsims
 *
 */
public class ChatScreenForm extends DForm implements IPrompter, FormView, CommandListener {

	
	private Vector frameSet = new Vector();
	private Vector prompts;
	int activeQuestion = -1;
	int totalQuestions = 0;

	int highestDisplayedQuestion = 0;
	private String name;
	private Model xmlModel;
	private int recordId;
	private IController controller;
	private boolean next = true;
	private boolean prev = false;
	
	Command exitCommand = new Command("Exit", Command.EXIT, 1);
	Command nextCommand = new Command("Next", Command.ITEM, 1);
	Command prevCommand = new Command("Prev", Command.ITEM, 1);
	
	/**
	 * Creates a new ChatScreen Form
	 */
	public ChatScreenForm() {
		addCommand(nextCommand);
		addCommand(prevCommand);
		addCommand(exitCommand);
		this.setCommandListener(this);
		setupComponents();
	}

	
	/**
	 * Lays out the static components for the form
	 */
	private void setupComponents() {
		int width = this.getWidth();
		int height = this.getHeight();
		int frameCanvasHeight = height - (height / 11);
		getContentComponent().setBackgroundColor(ViewUtils.GREY);
	}

	/**
	 * Pushes a new question onto the stack, setting all other 
	 * questions to inactive status, and displaying the new question
	 * to the user.
	 * 
	 * @param theQuestion The new question to be displayed
	 */
	public void addPrompt(Prompt p) {
		System.out.println("ChatScreenForm.addPrompt() " + activeQuestion);
		if (next) {
			activeQuestion++;
			// add a new question
			if (activeQuestion == totalQuestions) {
				totalQuestions++;
				Frame newFrame = new Frame(p);
				newFrame.setWidth(this.getWidth());
				frameSet.addElement(newFrame);
				getContentComponent().add(newFrame);
				setupFrames();
				this.repaint();
			} else { // advance to question that's already there
				getContentComponent().add((Frame)frameSet.elementAt(activeQuestion));
				setupFrames();
			}	
			next = false;
		} else if (prev) {
			System.out.println("goToPreviousPrompt()" + activeQuestion);
			// Don't do anything if user hits prev command for first question
			if (activeQuestion > 0) {
				getContentComponent().remove((Frame)frameSet.elementAt(activeQuestion));
				activeQuestion--;
				setupFrames();
			}
			prev = false;
		}
	}

	
	public void goToPreviousPrompt() {
		System.out.println("goToPreviousPrompt()" + activeQuestion);
		// Don't do anything if user hits prev command for first question
		if (activeQuestion > 0) {
			getContentComponent().remove((Frame)frameSet.elementAt(activeQuestion));
			activeQuestion--;
			setupFrames();
		}
	}

	
	
	/**
	 * Queries all frames for their optimal size, and then lays them out
	 * in a simple stack.
	 */
	private void setupFrames() {
		int frameCanvasHeight = getContentComponent().getHeight()
				- (getContentComponent().getHeight() / 11);

		int frameStart = frameCanvasHeight;
		for (int i=activeQuestion; i >=0; i--) {
			Frame aFrame = (Frame) frameSet.elementAt(i);
			if ( i == activeQuestion ) {
				aFrame.setActiveFrame(true);
			} else {
				aFrame.setActiveFrame(false);
			}
			frameStart -= aFrame.getHeight();
			aFrame.setY(frameStart);
		}
	}

	
	public void setPrompts(Vector prompts) {
		this.prompts = prompts;
	}
	
	
	  public void showPrompt(Prompt prompt) {
		System.out.println("ChatScreenForm.showPrompt(prompt)");
		MVCComponent.display.setCurrent(this);
		addPrompt(prompt);
	}

	public void showPrompt(Prompt prompt, int screenIndex, int totalScreens) {
		System.out.println("ChatScreenForm.showPrompt(screenIndex, totalScreens)");
		showPrompt(prompt);
	}

	public void registerController(IController controller) {
		System.out.println("ChatScreenForm.registerController(controller)");
		this.controller = controller;
	}
	
	   public void commandAction(Command command, Displayable s) {
		try {
			if (command == nextCommand) {
				next = true;
				controller.processEvent(new ResponseEvent(ResponseEvent.NEXT,
						-1));
			} else if (command == prevCommand) {
				goToPreviousPrompt();
				controller.processEvent(new ResponseEvent(ResponseEvent.PREVIOUS, -1));
				
			} else if (command == exitCommand) {
				controller.processEvent(new ResponseEvent(ResponseEvent.EXIT,
						-1));
			}

		} catch (Exception e) {
			Alert a = new Alert("error.screen" + " 2"); //$NON-NLS-1$
			a.setString(e.getMessage());
			a.setTimeout(Alert.FOREVER);
			MVCComponent.display.setCurrent(a);
		}
	}

}
		
		