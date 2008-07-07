package org.javarosa.clforms;

import java.io.IOException;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.storage.User;
import org.javarosa.clforms.storage.UserRMSUtility;

public class NewUserForm extends Form  {

	private TextField userName;
	private TextField password;	   
	private TextField confirmPassword;
	private UserRMSUtility userRMS;
	private ChoiceGroup choice = new ChoiceGroup("",Choice.MULTIPLE);
	
	NewUserForm(String title)
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
	
	String readyToSave()
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
	
	
	
	
	boolean checkNameExistsAlready()
	{
		///find user in RMS:
		   User discoveredUser = new User();
		   String usernameStr = userName.getString();
		   int index = 1;
		   
		   while (index <= userRMS.getNumberOfRecords() )
		   {
			   try 
			   {
				   userRMS.retrieveFromRMS(index, discoveredUser);
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
	
	
	
}
