package org.javarosa.formmanager.view.chatterbox.widget;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;

public class WidgetEscapeComponent implements IWidgetComponentWrapper {
	private StringItem nextItem;
	
	public WidgetEscapeComponent() {
		
		//#if commcare.lang.sw
		//#style button
		 nextItem = new StringItem(null,"Endelea",Item.BUTTON);
	    //#else
		//#style button
		 nextItem = new StringItem(null,"Done",Item.BUTTON);
		 //#endif
		 
	}

	public void init() {
	}
	
	public Item wrapEntryWidget(Item i) {
		//#if chatterbox.selectmulti.nextbutton
		Container c = new Container(false);
			
		c.add(i);
		c.add(this.nextItem);
		i = (Item)c;
		//#endif
		
		return i;
	}

	public Item wrapInteractiveWidget(Item interactiveWidget) {
		Item i = interactiveWidget;
		//#if chatterbox.selectmulti.nextbutton
		i = this.nextItem;
		//#endif
		return i;
	}
	
	public int wrapNextMode(int topReturnMode) {
		int i = topReturnMode;
		//#if chatterbox.selectmulti.nextbutton
		i = ChatterboxWidget.NEXT_ON_SELECT;
		//#endif
		return i;
	}
}
