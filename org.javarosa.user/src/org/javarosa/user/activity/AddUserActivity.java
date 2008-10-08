package org.javarosa.user.activity;

import java.util.Hashtable;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.user.view.NewUserForm;

/*
 * @author Julian Hulme
 */

public class AddUserActivity implements IActivity, CommandListener {

	private IShell parent = null;
	public final Command CMD_SAVE = new Command("Save", Command.OK, 1);
	public final Command CMD_CANCEL = new Command("Exit",Command.BACK, 1);
	public static final String COMMAND_KEY = "command";

	Context context;
	NewUserForm addUser = null;
	
	public AddUserActivity (IShell p) {
		this.parent = p;

	}
	
	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub

	}


	public void destroy() {
		// TODO Auto-generated method stub

	}


	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void halt() {
		// TODO Auto-generated method stub

	}

	
	public void resume(Context globalContext) {
		// TODO Auto-generated method stub

	}

	
	public void start(Context context) {
		// TODO Auto-generated method stub
		this.context = context;
		//this.newuserForm = new NewUserForm(this, "Login");
		//this.loginScreen.setCommandListener(this);
		//this.loginScreen.loginButton.setItemCommandListener(this);       // set item command listener
		//parent.setDisplay(this, this.loginScreen);
		
		//take this out into an activity
		addUser = new NewUserForm("Add User");
		addUser.addCommand(CMD_SAVE);
		addUser.addCommand(CMD_CANCEL);
		addUser.setCommandListener(this);
		parent.setDisplay(this, addUser);

	}
	
	public void commandAction(Command c,Displayable d) {
		
		if (c == this.CMD_SAVE)
		{
			String answer = addUser.readyToSave();

    		if (answer.equals(""))	{///success

    			javax.microedition.lcdui.Alert successfulNewUser  = new javax.microedition.lcdui.Alert("User added","User added successfully",null,javax.microedition.lcdui.AlertType.CONFIRMATION);
    			
    			parent.setDisplay(this, successfulNewUser);
    			parent.setDisplay(this, this.addUser);
    		}
    		else if (answer.substring(0,10 ).equals("Username ("))///name already taken..
    		{

    			javax.microedition.lcdui.Alert nameTakenError  = new javax.microedition.lcdui.Alert("Problem adding User - name taken",
						answer, null,AlertType.ERROR);
    			parent.setDisplay(this, nameTakenError);
    			parent.setDisplay(this, this.addUser);
    		}
    		else if (answer.substring(0,9).equals("Please fi") )
    		{
    			System.out.println(answer.substring(9));
    			javax.microedition.lcdui.Alert noInputError  = new javax.microedition.lcdui.Alert("Problem adding User - no input",
						answer, null,AlertType.ERROR);
    			parent.setDisplay(this, noInputError);
    			parent.setDisplay(this, this.addUser);
    		}
    		else if (answer.substring(0,9).equals("Please re"))///password error
    		{
    			System.out.println(answer.substring(9));
    			javax.microedition.lcdui.Alert passwordMismatchError  = new javax.microedition.lcdui.Alert("Problem adding User - passwords don't match",
						answer, null,AlertType.ERROR);
    			passwordMismatchError.setTimeout(javax.microedition.lcdui.Alert.FOREVER);
    			parent.setDisplay(this, passwordMismatchError);
    			parent.setDisplay(this, this.addUser);

    		}

    	}	
		else if (c == this.CMD_CANCEL)
		{
			Hashtable returnArgs = new Hashtable();
			//returnArgs.put(COMMAND_KEY, Commands.CMD_ADD_USER);
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs );
		//	parent.setDisplay(this, this.formsList);			
		}
	}

}
