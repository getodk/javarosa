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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Image;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.utilities.media.MediaUtils;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.MoreUIAccess;
import de.enough.polish.ui.Style;

/**
 * The base widget for multi and single choice selections.
 * 
 * NOTE: This class has a number of Hacks that I've made after rooting through
 * the j2me polish source. If polish is behaving unpredictably, it is possibly because
 * of conflicting changes in new Polish versions with this code. I have outlined
 * the latest versions of polish in which the changes appear to work.
 * 
 * Questions should be directed to csims@dimagi.com
 * 
 * @author Clayton Sims
 * @date Feb 16, 2009 
 *
 */
public abstract class SelectEntryWidget extends ExpandedWidget {
	private int style;
	private boolean autoSelect;
	protected int lastSelected = -1;
	protected FormEntryPrompt prompt;
	
	private ChoiceGroup choicegroup;
	
	public SelectEntryWidget (int style, boolean autoSelect) {
		this.style = style;
		this.autoSelect = autoSelect;
	}
	
	protected Item getEntryWidget (FormEntryPrompt prompt) {
		this.prompt = prompt;
		
		ChoiceGroup cg = new ChoiceGroup("", style) {
			
			/** Hack #1 & Hack #3**/
			// j2me polish refuses to produce events in the case where select items
			// are already selected. This code intercepts key presses that toggle
			// selection, and clear any existing selection to prevent the supression
			// of the appropriate commands.
			//
			// Hack #3 is due to the fact that multiple choice items won't fire updates
			// unless they haven't been selected by default. The return true here for them
			// ensures that the system knows that fires on multi-select always signify
			// a capture.
			//
			// NOTE: These changes are only necessary for Polish versions > 2.0.5 as far
			// as I can tell. I have tested them on 2.0.4 and 2.0.7 and the changes
			// were compatibly with both
			protected boolean handleKeyReleased(int keyCode, int gameAction) {
				boolean gameActionIsFire = getScreen().isGameActionFire(
						keyCode, gameAction);
				if (gameActionIsFire) {
					ChoiceItem choiceItem = (ChoiceItem) this.focusedItem;
					if(this.choiceType != ChoiceGroup.MULTIPLE) {
						//Hack #1
						choiceItem.isSelected = false;
					}
				}
				boolean superReturn = super.handleKeyReleased(keyCode, gameAction);
				if(gameActionIsFire && this.choiceType == ChoiceGroup.MULTIPLE) {
					//Hack #3
					return true;
				} else {
					return superReturn;
				}
			}
			
			public int getScrollHeight() {
				return super.getScrollHeight();
			}
			
			/** Hack #2 **/
			//This is a slight UI hack that is in place to make the choicegroup properly
			//intercept 'up' and 'down' inputs. Essentially Polish is very broken when it comes
			//to scrolling nested containers, and this function properly takes the parent (Widget) position
			//into account as well as the choicegroup's
			//
			// I have tested this change with Polish 2.0.4 on Nokia Phones and the emulators and it works
			// correctly. It also works correctly on Polish 2.0.7 on the emulator, but I have not attempted
			// to use it on a real nokia phone.
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
			
			/** Hack #4! **/
			//Clayton Sims - Jun 16, 2009
			// Once again we're in the land of hacks.
			// This particular hack is responsible for making it so that when a widget is
			// brought up on the screen, its selected value (if one exists), is currently 
			// highlighted. This vastly improves the back/forward usability.
			// Only Tested on Polish 2.0.4
			public Style focus (Style newStyle, int direction) {
				Style retStyle = super.focus(newStyle, direction);
				if(this.getSelectedIndex() > -1) {
					this.focusChild(this.getSelectedIndex());
				}
				return retStyle; 
			}
			
			
			/** Hack #5 **/
			//Clayton Sims (10/18/2010) These should only happen if the autoSelect isn't true 
			//Anton de Winter 4/23/2010
			//To play an audio file whenever a choice is highlighted we need to make the following hack.
			public void focusChild( int index, Item item, int direction ) {
				if(autoSelect) {
					//skip everything here;
				} else {
					//CTS(10/18/2010) : Remove selectedIndex hack.
					if(direction != 0){
						doAudio(index, false);
					} 
				}
				super.focusChild(index, item, direction);
			}
			
			protected boolean handleKeyPressed(int keyCode, int gameAction){
				if(autoSelect || !(keyCode >= Canvas.KEY_NUM1 && keyCode <= Canvas.KEY_NUM9)){
					return super.handleKeyPressed(keyCode,gameAction);
				}else{
					int index = keyCode-Canvas.KEY_NUM1;
					if(index < this.itemsList.size()){
						doAudio(index,true);
						super.focusChild(index);
					}else{
						return super.handleKeyPressed(keyCode,gameAction);
					}
					return true;
				}
			}
		};
		for (int i = 0; i < prompt.getSelectChoices().size(); i++){
			cg.append("", null);
		}
		
		this.choicegroup = cg;
		
		return cg;
	}

	protected ChoiceGroup choiceGroup () {
		return this.choicegroup;
	}
	
	private void doAudio(int index, boolean force){
		if(force || lastSelected != index) {
			lastSelected = index;
			getMultimediaController().playAudioOnDemand(prompt,prompt.getSelectChoices().elementAt(index));
		}
	}

	protected void updateWidget (FormEntryPrompt prompt) {
		for (int i = 0; i < choiceGroup().size(); i++) {
			SelectChoice sc = prompt.getSelectChoices().elementAt(i);
			Image im = MediaUtils.getImage(prompt.getSpecialFormSelectChoiceText(sc, FormEntryCaption.TEXT_FORM_IMAGE));

			choiceGroup().getItem(i).setText(prompt.getSelectChoiceText(sc));
			
			if(im!=null){
				choiceGroup().getItem(i).setImage(im);			
			}
		}
	}
}
