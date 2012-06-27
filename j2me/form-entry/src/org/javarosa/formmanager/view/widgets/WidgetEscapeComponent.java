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

package org.javarosa.formmanager.view.widgets;

import javax.microedition.lcdui.Canvas;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.MoreUIAccess;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

public class WidgetEscapeComponent implements IWidgetComponentWrapper {
	private StringItem nextItem;
	private Item wrapped;
	private Container container;
	
	public WidgetEscapeComponent() {
		//#style button
		 nextItem = new StringItem(null,Localization.get("button.Next"),Item.BUTTON) {
			 
			 	//Pass up events that might be relevant and that we didn't process
				protected boolean handleKeyPressed(int keyCode, int gameAction){
					if(!super.handleKeyPressed(keyCode, gameAction) && wrapped != null) {
						 return UiAccess.handleKeyPressed(wrapped, keyCode, gameAction);
					} else {
						return true;
					}
				}
 
		 };
	}

	public void init() {
	}
	
	public Item wrapEntryWidget(Item i) {
		wrapped = i;
		container = new Container(false) {
			public int getRelativeScrollYOffset() {
				if (!this.enableScrolling && this.parent instanceof Container) {
					
					// Clayton Sims - Feb 9, 2009 : Had to go through and modify this code again.
					// The offsets are now accumulated through all of the parent containers, not just
					// one.
					Item walker = this.parent;
					int offset = 0;
					
					//Walk our parent containers and accumulate their offsets.
					while(walker instanceof Container) {
						// Clayton Sims - Apr 3, 2009 : 
						// If the container can scroll, it's relativeY is useless, it's in a frame and
						// the relativeY isn't actually applicable.
						// Actually, this should almost certainly _just_ break out of the loop if
						// we hit something that scrolls, but if we have multiple scrolling containers
						// nested, someone _screwed up_.
						if(!MoreUIAccess.isScrollingContainer((Container)walker)) {
							offset += walker.relativeY;
						}
						walker = walker.getParent();
					}
					
					//The value returned here (The + offest part) is the fix.
					int absOffset = ((Container)this.parent).getScrollYOffset() + this.relativeY + offset;
					
					return absOffset;
					
					// Clayton Sims - Feb 10, 2009 : Rolled back because it doesn't work on the 3110c, apparently!
					// Fixing soon.
					//return ((Container)this.parent).getScrollYOffset() + this.relativeY + this.parent.relativeY;
				}
				int offset = this.targetYOffset;
				//#ifdef polish.css.scroll-mode
					if (!this.scrollSmooth) {
						offset = this.yOffset;
					}
				//#endif
				return offset;
			}
			
			//Do not try to scroll yourself. Signal upwards that you're part of something else
			public int getScrollHeight() {
				return -1;
			}

		};
			
		container.add(i);
		container.add(this.nextItem);
		i = (Item)container;
		
		return i;
	}

	public Item wrapInteractiveWidget(Item interactiveWidget) {
		return this.nextItem;
	}
	
	public int wrapNextMode(int topReturnMode) {
		int i = topReturnMode;
		i = ExpandedWidget.NEXT_ON_MANUAL;
		return i;
	}
}
