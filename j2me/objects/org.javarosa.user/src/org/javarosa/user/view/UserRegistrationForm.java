/**
 * 
 */
package org.javarosa.user.view;

import org.javarosa.core.services.locale.Localization;

import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.StringItem;

/**
 * @author ctsims
 *
 */
public class UserRegistrationForm extends FramedForm {

	StringItem message;
	
	public UserRegistrationForm(String title) {
		super(title);
		message = new StringItem("",Localization.get("user.registration.attempt"));
		this.append(message);
	}
	
	public void setText(String text) {
		message.setText(text);
	}
}
