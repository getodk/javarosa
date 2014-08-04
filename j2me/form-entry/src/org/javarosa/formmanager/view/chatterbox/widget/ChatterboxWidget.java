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

import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.IQuestionWidget;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;
import org.javarosa.formmanager.view.widgets.ExpandedWidget;
import org.javarosa.formmanager.view.widgets.IWidgetStyle;
import org.javarosa.formmanager.view.widgets.IWidgetStyleEditable;
import org.javarosa.formmanager.view.widgets.LabelWidget;
import org.javarosa.formmanager.view.widgets.TextEntryWidget;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledPItemCommandListener;
import org.javarosa.j2me.log.HandledPItemStateListener;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.MoreUIAccess;
import de.enough.polish.ui.Style;

public class ChatterboxWidget extends Container implements IQuestionWidget, HandledPItemStateListener, HandledPItemCommandListener {
	public static final int VIEW_NOT_SET = -1;
	/** A Widget currently interacting with the user **/
	public static final int VIEW_EXPANDED = 0;
	/** A Widget that has been used and finished **/
	public static final int VIEW_COLLAPSED = 1;
	/** A Label that will never be interacted with **/
	public static final int VIEW_LABEL = 2;
	
	/** Only valid for Labels **/
	private boolean pinned = false;
	
	private Chatterbox cbox;
	private Command nextCommand;
	
	private int viewState = VIEW_NOT_SET;
	private IWidgetStyle collapsedStyle;
	private IWidgetStyleEditable expandedStyle;
	
	private FormEntryPrompt prompt;

	private IWidgetStyle activeStyle;
	//private Style blankSlateStyle;
	
	public ChatterboxWidget (Chatterbox cbox, FormEntryPrompt prompt, int viewState, IWidgetStyle collapsedStyle, IWidgetStyleEditable expandedStyle) {
		this(cbox, prompt, viewState, collapsedStyle, expandedStyle, null);
	}
			
	public ChatterboxWidget (Chatterbox cbox, FormEntryPrompt prompt, int viewState, IWidgetStyle collapsedStyle, IWidgetStyleEditable expandedStyle, 
			Style style) {
		super(false, style);
		//blankSlateStyle = this.getStyle();

		this.cbox = cbox;
		this.nextCommand = new Command(Localization.get("command.next"), Command.ITEM, 1);
        this.prompt = prompt;
        
		this.collapsedStyle = collapsedStyle;
		this.expandedStyle = expandedStyle;
				
		setViewState(viewState);
	}
	
	public void destroy () {
		if (viewState == VIEW_EXPANDED) {
			detachWidget();
		}
		
		prompt.unregister();
	}
		
	public int getViewState () {
		return viewState;
	}

	public void setViewState (int viewState) {
		if (viewState != this.viewState) {
			if (this.viewState != VIEW_NOT_SET)
				reset();

			this.viewState = viewState;
			activeStyle = getActiveStyle();
			
			activeStyle.initWidget(prompt, this);
			activeStyle.refreshWidget(prompt, FormElementStateListener.CHANGE_INIT);
			if (viewState == VIEW_EXPANDED) {
				attachWidget();
			} if(viewState == VIEW_COLLAPSED) {
			}
		}
	}

	public IAnswerData getData () {
		if (viewState == VIEW_EXPANDED) {
			return expandedStyle.getData();
		} else {
			throw new IllegalStateException("Attempt to fetch data from widget not in expanded mode");
		}
	}	

	public void setFocus () {
		if (viewState == VIEW_EXPANDED) {
			if (expandedStyle.focus()) {
				repaint();
			}
		} else {
			throw new IllegalStateException("Attempt to focus widget in non-interactive mode");
		}
	}	
	
	private IWidgetStyle getActiveStyle () {
		switch (viewState) {
		case VIEW_EXPANDED: return expandedStyle;
		case VIEW_COLLAPSED: return collapsedStyle;
		case VIEW_LABEL: return collapsedStyle;
		default: throw new IllegalArgumentException("Attempt to set invalid view style");
		}
	}

	private void reset () {
		if (viewState == VIEW_EXPANDED) {
			detachWidget();
		}
		
		activeStyle.reset();
		clear();
	}

	//call-back from QuestionBinding
	public void refreshWidget (int changeFlags) {
		activeStyle.refreshWidget(prompt, changeFlags);		
	}
		
	private void attachWidget () {
		Item widget = expandedStyle.getInteractiveWidget();
		
		widget.addCommand(nextCommand);
		widget.setItemCommandListener(this);
		
		switch(expandedStyle.getNextMode()) {
		case ExpandedWidget.NEXT_ON_MANUAL:
			widget.setDefaultCommand(nextCommand);
			break;
		case ExpandedWidget.NEXT_ON_ENTRY: 
			widget.setItemStateListener(this);
			break;
		case ExpandedWidget.NEXT_ON_SELECT:
			widget.setDefaultCommand(nextCommand);
			break;
		}
		this.focusChild(this.itemsList.size()-1);
	}
	
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

	
	private void detachWidget () {
		Item widget = expandedStyle.getInteractiveWidget();
		expandedStyle.reset();
		
		switch(expandedStyle.getNextMode()) {
		case ExpandedWidget.NEXT_ON_MANUAL:
			break;
		case ExpandedWidget.NEXT_ON_ENTRY: 
			widget.setItemStateListener(null);
			break;
		case ExpandedWidget.NEXT_ON_SELECT:
			if(widget.getDefaultCommand() != null) {
				widget.removeCommand(widget.getDefaultCommand());
			}
			break;
		}
				
		widget.removeCommand(nextCommand);
		widget.setItemCommandListener((ItemCommandListener)null);
	}
	
	public void commandAction(Command c, Item i) {
		CrashHandler.commandAction(this, c, i);
	}  

	public void _commandAction(Command c, Item i) {
    	System.out.println("cw: command action");
		
		if (i == expandedStyle.getInteractiveWidget() && c == nextCommand) {
			// BWD 23/8/2008 Ticket #69.  Added check for menu open
	    	// before passing on the hack.
			if(!cbox.isMenuOpened())
				cbox.questionAnswered();
		} else {
			//unrecognized commandAction, propagate to parent.
			cbox._commandAction(c, cbox);
		}
	}
	
	public void itemStateChanged(Item i) {
		CrashHandler.itemStateChanged(this, i);
	}  

	public void _itemStateChanged(Item i) {
		//debugging
    	System.out.println("cw: item state");
    	if (i instanceof ChoiceGroup) {
    		ChoiceGroup cg = (ChoiceGroup)i;
    		System.out.println(cg.size());
    		for (int j = 0; j < cg.size(); j++)
    			System.out.println(cg.getString(j) + " " + cg.isSelected(j));
    		System.out.println("---");
    	}
		
		if (i == expandedStyle.getInteractiveWidget()) {
			cbox.questionAnswered();
		}
	}
	
	public void UIHack (int hackType) {
		if (hackType == Chatterbox.UIHACK_SELECT_PRESS) {
			if (expandedStyle.getNextMode() == ExpandedWidget.NEXT_ON_SELECT && expandedStyle instanceof TextEntryWidget) {
				_commandAction(nextCommand, expandedStyle.getInteractiveWidget());
			}
		}
	}
	
	public void showCommands() {
		super.showCommands();
		Item widget = expandedStyle.getInteractiveWidget();
		widget.showCommands();
	}
	
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}
	
	public boolean isPinned() {
		return pinned;
	}
	
	public String toString() {
		if(this.activeStyle instanceof LabelWidget) {
			LabelWidget label = (LabelWidget)activeStyle;
			return label.toString();
		}
		return super.toString();
	}
	
	
	public boolean equals(Object o) {
		if(!(o instanceof ChatterboxWidget)) {
			return false;
		}
		ChatterboxWidget w = (ChatterboxWidget)o;
		if(w.getViewState() == this.getViewState() && w.getViewState() == VIEW_LABEL) {
			return this.toString().equals(o.toString());
		}
		
		return this == o;
	}
	
	public Object clone() {
		if(this.getViewState() == ChatterboxWidget.VIEW_LABEL) {
			LabelWidget label = (LabelWidget)this.activeStyle;
			ChatterboxWidget widget = new ChatterboxWidget(cbox, prompt, this.getViewState(), (LabelWidget)label.clone(), null);
			return widget;
		}
		return null;
	}
	
	public ChatterboxWidget generateHeader() {
		return new ChatterboxWidget(cbox, prompt, ChatterboxWidget.VIEW_LABEL, new LabelWidget(), null);
	}
	
	/** 
	 * @return The height of this widget that, when taken off screen, should result
	 * in a pinned header.
	 */
	public int getPinnableHeight() {
		return this.activeStyle.getPinnableHeight();
	}
	
	public FormEntryPrompt getPrompt () {
		return prompt;
	}
}