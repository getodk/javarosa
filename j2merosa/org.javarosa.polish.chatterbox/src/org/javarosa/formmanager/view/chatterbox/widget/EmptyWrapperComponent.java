/**
 * 
 */
package org.javarosa.formmanager.view.chatterbox.widget;

import de.enough.polish.ui.Item;

/**
 * @author Clayton Sims
 * @date Jan 15, 2009 
 *
 */
public class EmptyWrapperComponent implements IWidgetComponentWrapper {

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetComponentWrapper#init()
	 */
	public void init() {

	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetComponentWrapper#wrapEntryWidget(de.enough.polish.ui.Item)
	 */
	public Item wrapEntryWidget(Item i) {
		return i;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetComponentWrapper#wrapInteractiveWidget(de.enough.polish.ui.Item)
	 */
	public Item wrapInteractiveWidget(Item interactiveWidget) {
		return interactiveWidget;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetComponentWrapper#wrapNextMode(int)
	 */
	public int wrapNextMode(int topReturnMode) {
		return topReturnMode;
	}

}
