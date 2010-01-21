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
