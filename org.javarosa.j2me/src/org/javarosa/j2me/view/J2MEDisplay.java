package org.javarosa.j2me.view;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.IDisplay;
import org.javarosa.core.api.IView;
import org.javarosa.core.util.IncompatibleViewException;

public class J2MEDisplay implements IDisplay {

	private final Display display;
	
	public J2MEDisplay(Display display) {
		this.display = display;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IDisplay#setView(org.javarosa.core.api.IView)
	 */
	public void setView(IView view) {
		if(view instanceof Displayable) {
			display.setCurrent((Displayable)view);
		} else if(view.getScreenObject() instanceof Displayable) {
			display.setCurrent((Displayable)view.getScreenObject());
		}
		else {
			throw new IncompatibleViewException("J2ME Display did not recognize the type of a view " + view.toString());
		}
	}
	
	public Object getDisplayObject() {
		return getDisplay();
	}
	
	public Display getDisplay() {
		return display;
	}
}
