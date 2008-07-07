

package org.javarosa.polishforms;

import java.io.IOException;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextField;

import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.storage.User;
import org.javarosa.clforms.storage.UserRMSUtility;

import de.enough.polish.ui.Form;
import de.enough.polish.ui.backgrounds.ImageBackground;
import javax.microedition.lcdui.Image;

/*import de.enough.polish.ui.Choice;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.TextField;*/

public class LoginForm extends Form
{
	   public final Command CMD_CANCEL_LOGIN = new Command("Cancel",Command.CANCEL, 1);
	   public final Command CMD_LOGIN_LOGIN = new Command("Login", Command.OK, 1);

	   private TextField userName;
	   private TextField password;
	   private UserRMSUtility userRMS;

	   ///these 4 variable is for a rememberMe option that could join the login menu
	   private String rememberMe;
	   private static final String DO_REMEMBER_ME = "DoRememberMe";
       private static final String DONT_REMEMBER_ME = "DontRememberMe";
	   private ChoiceGroup choice = new ChoiceGroup("",Choice.MULTIPLE);
	   ImageBackground background;


	  public LoginForm(String title) {
		   /*//#style mainScreen*/
		   super(title);

		  // de.enough.polish.ui.UiAccess.setStyle( this );


		  userName = new TextField("Username", "", 10, TextField.ANY);
	      password = new TextField("Password:", "", 10, TextField.PASSWORD);

	      this.addCommand(CMD_CANCEL_LOGIN);

	      this.addCommand(CMD_LOGIN_LOGIN);

	      this.append(userName);
	      append(password);
	      choice.append("Developer hack", null);
	      choice.setSelectedIndex(0, true);
	      this.append(choice);

	      userRMS = new UserRMSUtility("LoginMem");
	      if (userRMS.getNumberOfRecords() == 0)
	      {
	    	  //User temp = new User();
	    	  //System.out.println("adding admin type admin user");
	    	  userRMS.writeToRMS(new User ("admin","password",Constants.ADMINUSER));

	      }

	   }

	   public boolean validateUser() {

		   ///temporary exit - just to not have to log in all the time!
		   if (choice.isSelected(0) == true )
		   {
			 return true;
		   }
		   else	{



		   boolean validLogin = false;
		   ///check if this is a first login: is there a default user yet?

		   ///find user in RMS:
		   User discoveredUser = new User();
		   String usernameStr = userName.getString().trim();
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
			   if (discoveredUser.getPassword().equals(password.getString()))
			   {
				   System.out.println("login valid");
				   validLogin = true;

			   }

	     return validLogin;


		   }
	   }

	   public String getLoggedInUserType() {


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
			   System.out.println("found a user: "+discoveredUser.getUsername()+" with type: "+discoveredUser.getType());
			   return discoveredUser.getType();
			   }

	     return "userNotFound";
	   }

	   //*this method returns an Alert that states that the login was unsuccessful. @see validateUser()*/
	   public javax.microedition.lcdui.Alert tryAgain() {
	     javax.microedition.lcdui.Alert error = new javax.microedition.lcdui.Alert("Login Incorrect", "Please try again", null, AlertType.ERROR);

	     System.out.println("error alert returned");
	     return error;
	   }

	   public javax.microedition.lcdui.Alert successfulLogin() {
		     javax.microedition.lcdui.Alert success = new javax.microedition.lcdui.Alert("Login Successful", "Loading your profile", null, AlertType.CONFIRMATION);
		     return success;
		   }


}