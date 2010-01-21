/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.services.locale.Localization;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;

public class WidgetEscapeComponent implements IWidgetComponentWrapper {
	private StringItem nextItem;
	
	public WidgetEscapeComponent() {
		//#style button
		 nextItem = new StringItem(null,Localization.get("chatterbox.button.next"),Item.BUTTON);
	}

	public void init() {
	}
	
	public Item wrapEntryWidget(Item i) {
		Container c = new Container(false);
			
		c.add(i);
		c.add(this.nextItem);
		i = (Item)c;
		
		return i;
	}

	public Item wrapInteractiveWidget(Item interactiveWidget) {
		Item i = interactiveWidget;
		i = this.nextItem;
		return i;
	}
	
	public int wrapNextMode(int topReturnMode) {
		int i = topReturnMode;
		i = ChatterboxWidget.NEXT_ON_SELECT;
		return i;
	}
}
