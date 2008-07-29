package org.javarosa.action;


/**
 * Action commands executed in the application.
 * 
 * @author daniel
 *
 */
public class ActionCommand {
	
	/** Command representing an uninitialized state. */
	public static final byte NONE = 0;
	
	/** Command to accept an action. */
	public static final byte OK = 1;
	
	/** Command to cancel an action. */
	public static final byte CANCEL = 2;
	
	/** Command to create a new item. */
	public static final byte NEW = 3;
	
	/** Command to edit an item. */
	public static final byte EDIT = 4;
	
	/** Command to save an item. */
	public static final byte SAVE = 5;
	
	/** Command to delete an item. */
	public static final byte DELETE = 6;
	
	/** Command to move to the next item. */
	public static final byte NEXT = 7;
	
	/** Command to move to the previous item. */
	public static final byte PREVIOUS = 8;
	
	/** Command to move to the previous screen. */
	public static final byte BACK = 9;
	
	/** Command to close the midlet. */
	public static final byte EXIT = 10;
}
