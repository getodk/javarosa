package org.javarosa.formmanager.view.singlequestionscreen.screen;

import javax.microedition.lcdui.Command;

import org.javarosa.core.services.locale.Localization;

import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.StyleSheet;

public class NewRepeatScreen extends FramedForm {

	public static Command noCommand = new Command("Yes",
			Command.ITEM, 1);
	public static Command yesCommand = new Command("No",
			Command.ITEM, 1);

	public NewRepeatScreen(String title) {
		super(title, StyleSheet.getStyle("OneQPS_Form_Flash"));//Localization.get("formview.repeat.addNew"));
		createView();
		addCommands();
	}

	/**
	 * Add initial view items to form
	 */
	protected void createView() {
		// #style button
		StringItem yesItem = new StringItem(null, Localization
				.get("button.Yes"), Item.BUTTON);
		yesItem.setDefaultCommand(yesCommand);
		// #style button
		StringItem noItem = new StringItem(null, Localization.get("button.No"),
				Item.BUTTON);
		noItem.setDefaultCommand(noCommand);

		this.append(yesItem);
		this.append(noItem);
	}

	/**
	 * Add generic commands
	 */
	private void addCommands() {
		this.addCommand(yesCommand);
		this.addCommand(noCommand);
	}
}
