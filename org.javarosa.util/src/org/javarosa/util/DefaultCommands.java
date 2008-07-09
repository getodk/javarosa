package org.javarosa.util;

import javax.microedition.lcdui.Command;

/**
 * Commands shared by the entire application
 * Some commands like OK, Cancel, Save, are intensionally reversed, eg OK=Command.CANCEL
 * just to have them positioned by the phone in the way which will be more convenient for the user
 * 
 * @author Daniel
 *
 */
public class DefaultCommands {

	/** Command for closing the application. */
	public static final Command cmdExit= new Command("Exit", Command.EXIT, 1);
	
	/** Command for cancelling changes. */
	public static Command cmdCancel = new Command("Cancel", Command.CANCEL, 1);
	
	/** Command for accepting selection. */
	public static Command cmdOk = new Command("OK", Command.OK, 1);
	
	/** Command for editing a question. */
	public static Command cmdEdit= new Command("Edit", Command.OK, 1);
	
	/** Command for displaying a new form. */
	public static Command cmdNew= new Command("New",Command.SCREEN, 1);
	
	/** Command for saving changes. */
	public static Command cmdSave = new Command("Save",Command.OK,1);
	
	/** Command for deleting. */
	public static Command cmdDelete = new Command("Delete",Command.SCREEN,1);
	
	/** Command for displaying the previous screen. */
	public static Command cmdBack = new Command("Back",Command.BACK,1);
	
	
	/** Command for accepting selection. */
	public static Command cmdYes = new Command("Yes",Command.OK,1);
	
	/** Command for cancelling selection. */
	public static Command cmdNo = new Command("No",Command.CANCEL,1);
	
	/** Command for going to the parent screen. */
	public static Command cmdBackParent = new Command("Back to list",Command.CANCEL,1);
	
	public static Command cmdNext = new Command("Next", Command.CANCEL, 1);
	public static Command cmdPrev = new Command("Previous", Command.OK, 1);

	/** No creation allowed. */
	private DefaultCommands(){
		
	}
}
