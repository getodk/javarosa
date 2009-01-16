package org.javarosa.formmanager.view.chatterbox.widget;

import de.enough.polish.ui.Item;

public interface IWidgetComponentWrapper {
	
	public void init();
	
	public Item wrapEntryWidget(Item i);

	public Item wrapInteractiveWidget(Item interactiveWidget);
	
	public int wrapNextMode(int topReturnMode);
}
