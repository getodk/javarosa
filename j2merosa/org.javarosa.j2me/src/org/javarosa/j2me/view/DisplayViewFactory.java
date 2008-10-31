package org.javarosa.j2me.view;

import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.IView;

public class DisplayViewFactory {
	
	/**
	 * Factory method to create a JavaRosa View from a 
	 * j2me displayable
	 * @param d The displayable to be wrapped
	 * @return An IView object that can be used by a j2me
	 * IDisplay
	 */
	public static IView createView(final Displayable d) {
		return new IView () {
			public Object getScreenObject() {
				return d;
			}
		};
	}
}
