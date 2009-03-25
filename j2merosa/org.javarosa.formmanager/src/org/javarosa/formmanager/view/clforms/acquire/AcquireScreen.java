package org.javarosa.formmanager.view.clforms.acquire;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.IView;
import org.javarosa.core.model.data.IAnswerData;

/**
 * @author mel
 * 
 *         A screen that acquires the answer data for a question
 * 
 */
public abstract class AcquireScreen extends javax.microedition.lcdui.Form
		implements IView, CommandListener {

	private AcquiringQuestionScreen questionScreen;
	private CommandListener listenerToReturnTo;
	public Command cancelCommand;
	private Command setCallingScreenDataCommand;

	/**
	 * @param title
	 *            the screen title
	 * @param questionScreen
	 *            the question screen to which the acquiredanswer data will be
	 *            returned
	 * @param listenerToReturnTo
	 *            the listener to which control will be returned once the
	 *            acquiring process has terminated. this can happen becuase data
	 *            is successfully acquired or returned, or because acquiring is
	 *            cancelled
	 */
	public AcquireScreen(String title, AcquiringQuestionScreen questionScreen,
			CommandListener listenerToReturnTo) {
		super(title);
		this.questionScreen = questionScreen;
		this.listenerToReturnTo = listenerToReturnTo;

		createView();
		addCommands();
		setCommandListener(this);
	}

	/**
	 * Add initial view items to form
	 */
	protected abstract void createView();

	/**
	 * command handler for screen-specific commands
	 * 
	 * @param command
	 * @param arg1
	 */
	protected abstract void handleCustomCommand(Command command,
			Displayable arg1);

	/**
	 * Add generic commands
	 */
	private void addCommands() {
		cancelCommand = new Command("Cancel", Command.CANCEL, 9);
		this.addCommand(cancelCommand);
		setCallingScreenDataCommand = getSetCallingScreenDataCommand();
		this.addCommand(setCallingScreenDataCommand);
	}

	/**
	 * @return the command that is used to stop capturing, do any processing and
	 *         attempt to return the acquired data as answer data
	 */
	protected abstract Command getSetCallingScreenDataCommand();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}

	/**
	 * @return the question screen for which data is being acquired
	 */
	public AcquiringQuestionScreen getQuestionScreen() {
		return questionScreen;
	}

	protected abstract IAnswerData getAcquiredData();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.microedition.lcdui.CommandListener#commandAction(javax.microedition
	 * .lcdui.Command, javax.microedition.lcdui.Displayable)
	 * 
	 * Command handler for generic acquisition commands
	 */
	public void commandAction(Command command, Displayable arg1) {

		if (command == cancelCommand) {
			cleanUp();
			listenerToReturnTo.commandAction(command, arg1);

		} else if (command == setCallingScreenDataCommand) {
			IAnswerData data = this.getAcquiredData();
			if (data != null) {
				questionScreen.setAcquiredData(data);
				commandAction(cancelCommand, this);
			}

		} else {
			handleCustomCommand(command, arg1);
		}

	}

	/**
	 * Cleans up the resources used to do the acquiring (e.g. video player)
	 */
	protected abstract void cleanUp();
}
