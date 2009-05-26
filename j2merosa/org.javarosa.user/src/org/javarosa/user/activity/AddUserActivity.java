package org.javarosa.user.activity;

import java.util.Hashtable;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.user.model.User;
import org.javarosa.user.storage.UserRMSUtility;
import org.javarosa.user.utility.AddUserContext;
import org.javarosa.user.utility.Terms;
import org.javarosa.user.utility.UserValidator;
import org.javarosa.user.view.UserForm;

public class AddUserActivity implements IActivity, CommandListener {

	public final static String NEW_USER_KEY = "new_user";

	private IShell parent = null;

	// --- menu commands?
	public final static Command CMD_SAVE = new Command(JavaRosaServiceProvider.instance().localize("menu.Save"),
			Command.OK, 2);
	public final static Command CMD_CANCEL = new Command(JavaRosaServiceProvider.instance().localize("menu.Exit"),
			Command.EXIT, 2);

	private boolean success = false;
	private AddUserContext context;
	private UserForm view;

	public AddUserActivity(IShell p) {
		this.parent = p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		this.context = new AddUserContext(context);
		this.view = new UserForm(JavaRosaServiceProvider.instance().localize("activity.adduser.adduser"), this.context
				.getDecorator());
		this.view.setPasswordMode(this.context.getPasswordFormat());
		this.view.addCommand(CMD_SAVE);
		this.view.addCommand(CMD_CANCEL);
		this.view.setCommandListener(this);
		this.parent.setDisplay(this, this.view);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.microedition.lcdui.CommandListener#commandAction(javax.microedition
	 * .lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command c, Displayable d) {
		if (!d.equals(this.view)) {
			if (!this.success) {
				this.parent.setDisplay(this, this.view);
			}
		}

		if (c == CMD_CANCEL) {
			Hashtable returnArgs = new Hashtable();
			this.parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE,
					returnArgs);
			return;
		}

		if (c == CMD_SAVE) {
			UserValidator validator = new UserValidator(this.view);
			int status = validator.validateNewUser();

			if (status == UserValidator.OK) {

				User newUser = createNewUser();

				saveNewUserToRMS(newUser);

				alertUser(JavaRosaServiceProvider.instance().localize("activity.adduser.useradded"), JavaRosaServiceProvider.instance().localize("activity.adduser.useraddedsucc"),
						AlertType.CONFIRMATION);

				// return control from activity
				Hashtable returnArgs = new Hashtable();
				returnArgs.put(Constants.RETURN_ARG_KEY, newUser);
				this.parent.returnFromActivity(this,
						Constants.ACTIVITY_COMPLETE, returnArgs);

				return;

			}

			handleProblems(status);
		}

	}

	/**
	 * @param s1
	 * @param s2
	 * @param type
	 */
	private void alertUser(String s1, String s2, AlertType type) {
		// #style mailAlert
		final Alert a = new Alert(s1, s2, null, type);
		a.setCommandListener(this);
		a.setTimeout(Alert.FOREVER);

		this.parent.setDisplay(this, new IView() {
			public Object getScreenObject() {
				return a;
			}
		});
	}

	/**
	 * 
	 * 
	 * 
	 * @param status
	 */
	private void handleProblems(int status) {

		String s = JavaRosaServiceProvider.instance().localize("activity.adduser.problem");

		if (status == UserValidator.USER_EXISTS) {
			s += ": " + JavaRosaServiceProvider.instance().localize("activity.adduser.problem.nametaken", new String[]{this.view.getUsername()});
		}

		if (status == UserValidator.USERORPASSWD_MISSING) {
			s += ": " + JavaRosaServiceProvider.instance().localize("activity.adduser.problem.emptyuser");
		}

		if (status == UserValidator.MISMATCHED_PASSWORDS) {
			s += ": " + JavaRosaServiceProvider.instance().localize("activity.adduser.problem.mismatchingpasswords");
		}

		alertUser(s, "", AlertType.ERROR);
	}

	/**
	 * @return
	 */
	private User createNewUser() {

		User user;

		final String username = this.view.getUsername();
		final String password = this.view.getPassword();
		int userid = this.view.getUserId();

		if (!this.view.adminRightsSelected())
			user = new User(username, password, userid);
		else
			user = new User(username, password, userid,
					org.javarosa.user.model.Constants.ADMINUSER);

		if (this.view.getDecorator() != null) {
			String[] elements = this.view.getDecorator()
					.getPertinentProperties();
			for (int i = 0; i < elements.length; ++i) {
				user.setProperty(elements[i], this.view.getMetaFields()[i]
						.getString());
			}
		}
		return user;
	}

	/**
	 * @param u
	 */
	private void saveNewUserToRMS(User u) {
		UserRMSUtility userRMS = (UserRMSUtility) JavaRosaServiceProvider
				.instance().getStorageManager().getRMSStorageProvider()
				.getUtility(UserRMSUtility.getUtilityName());

		userRMS.writeToRMS(u);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.parent = shell;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api
	 * .ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException(
				"The Activity Class "
						+ this.getClass().getName()
						+ " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}

	public void contextChanged(Context globalContext) {
		// do nothing
	}

	public void destroy() {
		// do nothing
	}

	public Context getActivityContext() {
		return null;
	}

	public void halt() {
		// do nothing
	}

	public void resume(Context globalContext) {
		// do nothing
	}
}
