/**
 * 
 */
package org.javarosa.formmanager.view;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField;

/**
 * @author ctsims
 *
 */
public class InputSafeTextField extends TextField {
	
	InvalidInputListener listener;

	public InputSafeTextField(String label, String text, int maxSize, int constraints, InvalidInputListener listener, Style style) {
		super(label, text, maxSize, constraints, style);
		this.listener = listener;
	}

	public InputSafeTextField(String label, String text, int maxSize, int constraints, InvalidInputListener listener) {
		super(label, text, maxSize, constraints);
		this.listener = listener;
	}
//	public void setString(String string) {
//		//TODO: We should probably be buffering the string first
//		//to undo any side effects in setString
//		try {
//			super.setString(string);
//		} catch (IllegalArgumentException e) {
//			listener.invalidNativeInput(string);
//		}
//	}
	
	public void commandAction(Command cmd, Displayable box) {
		//TODO: We should probably be buffering the string first
		//to undo any side effects in setString
		try {
			super.commandAction(cmd, box);
		} catch (IllegalArgumentException e) {
			listener.invalidNativeInput("");
		}
	}
}
