

package org.javarosa.user.view;

import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IView;
import org.javarosa.core.services.TransportManager;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.user.model.User;
import org.javarosa.user.storage.UserRMSUtility;
import org.javarosa.user.utility.LoginContext;

//#if javarosa.login.showbuild
import edu.washington.commcare.util.CommCareContext;
//#endif


public class LoginForm extends Form implements IView
{
	   public final Command CMD_CANCEL_LOGIN = new Command("EXIT",Command.SCREEN, 1);
	   public Command loginButtonCommand = new Command("Login", Command.ITEM, 1);
	   private Alert alertdialog = new Alert("Web Service Error", "No response from server", null, AlertType.ERROR);

	   private TextField userName;
	   private TextField password;
	   public UserRMSUtility userRMS;
	   private User loggedInUser;
	   public StringItem loginButton;
	   //#if javarosa.login.demobutton
	   public StringItem demoButton;
	   public Command demoButtonCommand = new Command("Demo", Command.ITEM, 1);
	   //#endif
	   public boolean validator;
	   
	   private TransportMessage message;
	   private TransportManager transportManager;
	   private String requestPayload = "#";

	   ///these 4 variable is for a rememberMe option that could join the login menu
	   private String rememberMe;
	   private static final String DO_REMEMBER_ME = "DoRememberMe";
       private static final String DONT_REMEMBER_ME = "DontRememberMe";
	   IActivity parent;

	  public LoginForm(IActivity loginActivity, String title) {
		   super(title);
		   parent = loginActivity;

		   userRMS = (UserRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(UserRMSUtility.getUtilityName());

	      if (userRMS.getNumberOfRecords() == 0){

	    	  String usernameVAR= (String)loginActivity.getActivityContext().getElement("username");
	    	  String passwordVAR= (String)loginActivity.getActivityContext().getElement("password");

	    	  // BWD - giving the default admin user an ID of -1...
	    	  loggedInUser = new User (usernameVAR,passwordVAR, -1, User.ADMINUSER);
	    	  userRMS.writeToRMS(loggedInUser);
	      }

		   //get first username from RMS
		   User tempuser = new User();
		   tempuser.setUsername("");

		   if (userRMS.getNumberOfRecords() != 0){
			   	try {
					userRMS.retrieveFromRMS(userRMS.getNextRecordID()-1, tempuser);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DeserializationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      }
		  userName = new TextField("Username:", tempuser.getUsername(), 50, TextField.ANY);
	      password = new TextField("Password:", "", 10, TextField.PASSWORD);
	      this.addCommand(CMD_CANCEL_LOGIN);

	      this.append(userName);
	      append(password);

	      loginButton = new StringItem(null,"LOGIN",Item.BUTTON);
	      this.append(loginButton);
	      loginButton.setDefaultCommand(loginButtonCommand);     // add Command to Item.
	      
	      //#if javarosa.login.demobutton
	      demoButton = new StringItem(null,"DEMO",Item.BUTTON);
	      this.append(demoButton);
	      demoButton.setDefaultCommand(demoButtonCommand);     // add Command to Item.
	      //#endif
	      
	      //#if javarosa.login.showbuild
	      CommCareContext c = new CommCareContext(parent.getActivityContext());
	      if(c.getCommCareVersion() != null || !c.getCommCareVersion().equals("")) {
	    	  this.append(new String("CommCare verison: " + c.getCommCareVersion()));
	      }
	      if(c.getCommCareBuild() != null || !c.getCommCareBuild().equals("")) {
	    	  this.append(new String("Build number: " + c.getCommCareBuild() + "-" + c.getJavaRosaBuild()));
	      }
	      //#endif
	   }
	  
	  public void setPasswordMode(String passwordMode) {
		  if(LoginContext.PASSWORD_FORMAT_NUMERIC.equals(passwordMode)) {
			  password.setConstraints(TextField.PASSWORD | TextField.NUMERIC);
		  } else if(LoginContext.PASSWORD_FORMAT_ALPHA_NUMERIC.equals(passwordMode)) {
			  password.setConstraints(TextField.PASSWORD);
		  }
	  }


	   public boolean validateUser() {

		   boolean validLogin = false;
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
			   catch (DeserializationException uee) {
				   System.out.println(uee);
			   }
			   if (discoveredUser.getUsername().equalsIgnoreCase(usernameStr))
				   break;

			   index++;
		   }

		   if (discoveredUser.getUsername().equalsIgnoreCase(usernameStr)){
			   if (discoveredUser.getPassword().equals(password.getString()))
			   {
				   System.out.println("login valid");
				   validLogin = true;
				   setLoggedInUser(discoveredUser);
			   }
			   }
   
	     return validLogin;

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
			   catch (DeserializationException uee) {
				   System.out.println(uee);
			   }
			   if (discoveredUser.getUsername().equalsIgnoreCase(usernameStr))
				   break;

			   index++;
		   }

		   if (discoveredUser.getUsername().equalsIgnoreCase(usernameStr))
			   {
			   System.out.println("found a user: "+discoveredUser.getUsername()+" with type: "+discoveredUser.getUserType());
			   return discoveredUser.getUserType();
			   }

	     return "userNotFound";
	   }

	   public javax.microedition.lcdui.Alert successfulLogin() {
		     javax.microedition.lcdui.Alert success = new javax.microedition.lcdui.Alert("Login Successful", "Loading your profile", null, AlertType.CONFIRMATION);
		     return success;
		   }
		public UserRMSUtility getUserRMS() {
				return this.userRMS;
		}
		
		public String getPassWord() {
			String pass = password.getString();
			return pass;
		}
		
		public String getUserName() {
			String usr = userName.getString();
			return usr;
		}
	public User getLoggedInUser() {
		return loggedInUser;
	}

	public void setLoggedInUser(User loggedInUser) {
		this.loggedInUser = loggedInUser;
	}
	public Object getScreenObject() {
		return this;
	}
}