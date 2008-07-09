/**
 * 
 */
package org.javarosa.core.formcontroller;

import javax.microedition.lcdui.Display;

import org.javarosa.clforms.api.Prompt;
import org.javarosa.core.FormController;

/**
 * @author Brian DeRenzi
 *
 */
public interface IPrompter  {
	
	public void showPrompt(Prompt prompt);
	
	public void showPrompt(Prompt prompt, int screenIndex, int totalScreens);

	public void registerController(FormController controller);

	public void showError(String title, String message, Display display);

}
