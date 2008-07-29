package org.javarosa.action;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.util.DefaultCommands;


/**
 * Activity for selecting an item from a list.
 * 
 * @author daniel
 *
 */
public class SelectFromListAction implements CommandListener,IAction{

	private Vector items;
	private List itemList;
	private String title;
	
	private IActionListener listener;
	
	public SelectFromListAction(){
		
	}
	
	/**
	 * Constructs a new list selection activity with a title and list of items to select from.
	 * 
	 * @param items the list of items to select from.
	 */
	public SelectFromListAction(String title,Vector items){
		this.title = title;
		this.items = items;
	}
	
	public void start(IActionListener listener){
		this.listener = listener;
		
		itemList = new List(title,Choice.IMPLICIT);
		itemList.addCommand(DefaultCommands.cmdOk);
		itemList.addCommand(DefaultCommands.cmdCancel);
		
		if(items != null && items.size() > 0){
			for(int i=0; i<items.size(); i++)
				itemList.append(items.elementAt(i).toString(),null);
		}
		
		itemList.setCommandListener(this);
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(itemList);
	}
	
	public void resume(){
		JavaRosaServiceProvider.instance().getDisplay().setCurrent(itemList);
	}
	
	/**
	 * Sets the list of items to select from.
	 * 
	 * @param items a Vector of items.
	 */
	public void setItems(Vector items){
		this.items = items;
	}
	
	/**
	 * Sets the title of items to select from.
	 * 
	 * @param title the of items.
	 */
	public void setTitle(String title){
		this.title = title;
	}
	
	/**
	 * Processes the command events.
	 * 
	 * @param c - the issued command.
	 * @param d - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
        if (c == DefaultCommands.cmdCancel)
        	handleCancelCommand(d);
        else if(c == DefaultCommands.cmdOk || c == List.SELECT_COMMAND)
        	handleOkCommand(d);
	}
	
	/**
	 * Processes the cancel command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleCancelCommand(Displayable d){
		listener.actionCompleted(this, ActionCommand.CANCEL, null);
	}
	
	/**
	 * Processes the OK command event.
	 * 
	 * @param d - the screen object the command was issued for.
	 */
	private void handleOkCommand(Displayable d){
		listener.actionCompleted(this, ActionCommand.OK, items.elementAt(((List)d).getSelectedIndex()));
	}
}
