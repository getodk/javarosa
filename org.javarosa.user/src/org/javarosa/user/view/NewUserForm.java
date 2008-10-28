package org.javarosa.user.view;

import java.io.IOException;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.api.IView;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.user.model.Constants;
import org.javarosa.user.model.User;
import org.javarosa.user.storage.UserRMSUtility;



/**
 * @author Julian
 *
 */
public class NewUserForm extends Form implements IView{

	private TextField userName;
	private TextField password;
	private TextField confirmPassword;
	private UserRMSUtility userRMS;
	private ChoiceGroup choice = new ChoiceGroup("",Choice.MULTIPLE);

	public NewUserForm(String title)
	{
		super(title);
		userName = new TextField("Name (ie: loginID):", "", 10, TextField.ANY);
	    password = new TextField("User Password:", "", 10, TextField.PASSWORD);
	    confirmPassword = new TextField("Confirm Password:", "", 10, TextField.PASSWORD);
	    choice.append("Give this user admin rights?", null);

	    this.append(userName);
	    this.append(password);
	    this.append(confirmPassword);
	    this.append(choice);

	    userRMS = new UserRMSUtility("LoginMem");
	}

	public String readyToSave()
	{

		System.out.println("reached this far");
		boolean nameAlreadyTaken = checkNameExistsAlready();
		if (nameAlreadyTaken == true)
		{
			System.out.println("username taken love");
			return "Username ("+userName.getString()+") already taken. Please choose another username.";
		}
		else if ((userName.getString().equalsIgnoreCase("")) || (password.getString().equals("")))
		{
			System.out.println("fail");
			return "Please fill in both username and password.";
		}
		else if (!(password.getString().equals(confirmPassword.getString())))
		{
			System.out.println("passwords don't match...");
			return "Please re-enter your password, the password and password confirmation box did not match.";
		}
		else
		{
			System.out.println("ready returned as true");
			if (choice.isSelected(0) == false)
			userRMS.writeToRMS(new User (userName.getString() ,password.getString()));
			else userRMS.writeToRMS(new User (userName.getString() ,password.getString(), Constants.ADMINUSER));
			System.out.println("added user "+ userName.getString() + " passw: "+password.getString()+" = "+confirmPassword.getString() );
			return "";
		}
	}




	private boolean checkNameExistsAlready()
	{
		///find user in RMS:
		   User discoveredUser = new User();
		   String usernameStr = userName.getString();
		   int index = 1;

		   while (index <= userRMS.getNumberOfRecords() )
		   {
			   try
			   {
				   try {
					userRMS.retrieveFromRMS(index, discoveredUser);
				} catch (DeserializationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			   }
			   catch (IOException ioe) {
				   System.out.println(ioe);
			   }
			   if (discoveredUser.getUsername().equalsIgnoreCase(usernameStr))
				   break;

			   index++;
		   }

		   if (discoveredUser.getUsername().equalsIgnoreCase(usernameStr))
		   {
			   System.out.println("name already taken valid");
			   return true;
		   }

		   else return false;
	}
	public Object getScreenObject() {
		return this;
	}
}
