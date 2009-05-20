package org.javarosa.user.view;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.api.IView;
import org.javarosa.user.model.User;
import org.javarosa.user.utility.IUserDecorator;
import org.javarosa.user.utility.LoginContext;
import org.javarosa.user.utility.Terms;

/**
 * Form accessible to admins for adding new users to an application
 * 
 */
public class UserForm extends Form implements IView {

	// ------ fields
	private TextField usernameField;
	private TextField passwordField;
	private TextField confirmPasswordField;
	private TextField[] metaFields;

	private IUserDecorator decorator;

	// #if javarosa.adduser.extended
	private TextField userIDField;
	// #endif

	private ChoiceGroup choice = new ChoiceGroup("", Choice.MULTIPLE);

	public UserForm(String title, IUserDecorator d) {
		super(title);
		this.usernameField = new TextField(Terms.UNAME_STR, "", 10,
				TextField.ANY);
		this.passwordField = new TextField(Terms.PASSWRD_STR, "", 10,
				TextField.PASSWORD);
		this.confirmPasswordField = new TextField(Terms.CONFIRMPASSWD_STR, "",
				10, TextField.PASSWORD);
		// #if javarosa.adduser.extended
		this.userIDField = new TextField(Terms.USERID_STR, "", 10,
				TextField.NUMERIC);
		// #endif
		this.choice.append(Terms.GIVE_ADMIN_STR, null);

		append(this.usernameField);
		append(this.passwordField);
		append(this.confirmPasswordField);
		// #if javarosa.adduser.extended
		append(this.userIDField);
		// #endif

		this.decorator = d;
		if (d != null) {
			initMeta();
		}
		append(this.choice);
	}

	public UserForm(String title) {
		this(title, null);
	}

	public int getUserId() {
		int userid = -1;
		// #if javarosa.adduser.extended
		// Clayton Sims - Mar 12, 2009 : this is stupid, and I hate it, but it
		// is Java's
		// only way of identifying whether userID is an int cleanly. If you have
		// a cleaner
		// 100% solid way of doing so, _please_ replace this code.
		try {
			userid = Integer.parseInt(this.userIDField.getString().trim());
		} catch (NumberFormatException e) {
			// Do nothing.
		}
		// #endif
		return userid;
	}

	/**
	 * 
	 * 
	 * Load the users details into the fields of the form (when editing)
	 * 
	 * 
	 * @param user
	 */
	public void loadUser(User user) {
		this.usernameField.setString(user.getUsername());
		this.passwordField.setString(user.getPassword());

		// #if javarosa.adduser.extended
		this.userIDField.setString(String.valueOf(user.getUserID()));
		// #endif

		if (user.isAdminUser()) {
			this.choice.setSelectedIndex(0, true);
		}

		if (this.decorator != null) {
			String[] elements = this.decorator.getPertinentProperties();
			for (int i = 0; i < elements.length; ++i) {
				this.metaFields[i].setString(user.getProperty(elements[i]));
			}
		}

	}

	public String getUsername() {
		return this.usernameField.getString().trim();
	}

	public String getPassword() {
		return this.passwordField.getString().trim();
	}

	public String getPasswordConfirmation() {
		return this.confirmPasswordField.getString().trim();
	}

	public void setPasswordMode(String mode) {
		if (LoginContext.PASSWORD_FORMAT_NUMERIC.equals(mode)) {
			this.passwordField.setConstraints(TextField.PASSWORD
					| TextField.NUMERIC);
			this.confirmPasswordField.setConstraints(TextField.PASSWORD
					| TextField.NUMERIC);
		} else if (LoginContext.PASSWORD_FORMAT_ALPHA_NUMERIC.equals(mode)) {
			this.passwordField.setConstraints(TextField.PASSWORD);
			this.confirmPasswordField.setConstraints(TextField.PASSWORD);
		}
	}

	public void initMeta() {
		String[] elements = this.decorator.getPertinentProperties();
		this.metaFields = new TextField[elements.length];
		for (int i = 0; i < elements.length; ++i) {
			this.metaFields[i] = new TextField(this.decorator
					.getHumanName(elements[i]), "", 100, TextField.ANY);
			this.append(this.metaFields[i]);
		}
	}

	public boolean adminRightsSelected() {
		return this.choice.isSelected(0);
	}

	// ------------ getters
	public Object getScreenObject() {
		return this;
	}

	public TextField[] getMetaFields() {
		return this.metaFields;
	}

	public IUserDecorator getDecorator() {
		return this.decorator;
	}

}
