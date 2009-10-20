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

import javax.microedition.lcdui.Command;

import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.IQuestionWidget;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.Style;

public class ChatterboxWidget extends Container implements IQuestionWidget, ItemStateListener, ItemCommandListener {
	public static final int VIEW_NOT_SET = -1;
	/** A Widget currently interacting with the user **/
	public static final int VIEW_EXPANDED = 0;
	/** A Widget that has been used and finished **/
	public static final int VIEW_COLLAPSED = 1;
	/** A Label that will never be interacted with **/
	public static final int VIEW_LABEL = 2;
	
	public static final int NEXT_ON_MANUAL = 1;
	public static final int NEXT_ON_ENTRY = 2;
	public static final int NEXT_ON_SELECT = 3;
	
	/** Only valid for Labels **/
	private boolean pinned = false;
	
	private Chatterbox cbox;
	private Command nextCommand;
	
	private FormElementBinding binding;
	
	private int viewState = VIEW_NOT_SET;
	private IWidgetStyle collapsedStyle;
	private IWidgetStyleEditable expandedStyle;

	private IWidgetStyle activeStyle;
	//private Style blankSlateStyle;
	
	public ChatterboxWidget (Chatterbox cbox, FormElementBinding binding, int viewState,
			IWidgetStyle collapsedStyle, IWidgetStyleEditable expandedStyle) {
		this(cbox, binding, viewState, collapsedStyle, expandedStyle, null);
	}
			
	public ChatterboxWidget (Chatterbox cbox, FormElementBinding binding, int viewState,
			IWidgetStyle collapsedStyle, IWidgetStyleEditable expandedStyle, 
			Style style) {
		super(false, style);
		//blankSlateStyle = this.getStyle();

		this.cbox = cbox;
        this.nextCommand = new Command("Next", Command.ITEM, 1);
        
        this.binding = binding;
        binding.widget = this;
        
		this.collapsedStyle = collapsedStyle;
		this.expandedStyle = expandedStyle;
				
		setViewState(viewState);
	}
	
	public void destroy () {
		if (viewState == VIEW_EXPANDED)
			detachWidget();
		
		binding.unregister();
	}
	
	public FormElementBinding getBinding () {
		return binding;
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
			
			activeStyle.initWidget(binding.element, this);
			activeStyle.refreshWidget(binding, FormElementStateListener.CHANGE_INIT);
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
		if (viewState == VIEW_EXPANDED)
			detachWidget();
		
		activeStyle.reset();
		clear();
		//if (blankSlateStyle != null) {
		//	setStyle(blankSlateStyle);
		//}
	}

	//call-back from QuestionBinding
	public void refreshWidget (int changeFlags) {
		activeStyle.refreshWidget(binding, changeFlags);		
	}
		
	private void attachWidget () {
		Item widget = expandedStyle.getInteractiveWidget();
		
		widget.addCommand(nextCommand);
		widget.setItemCommandListener(this);
		
		switch(expandedStyle.getNextMode()) {
		case NEXT_ON_MANUAL:
			break;
		case NEXT_ON_ENTRY: 
			widget.setItemStateListener(this);
			break;
		case NEXT_ON_SELECT:
			widget.setDefaultCommand(nextCommand);
			break;
		}
		this.focus(this.itemsList.size()-1);
	}
	
	private void detachWidget () {
		Item widget = expandedStyle.getInteractiveWidget();
		
		switch(expandedStyle.getNextMode()) {
		case NEXT_ON_MANUAL:
			break;
		case NEXT_ON_ENTRY: 
			widget.setItemStateListener(null);
			break;
		case NEXT_ON_SELECT:
			widget.setDefaultCommand(null);
			break;
		}
		
		widget.removeCommand(nextCommand);
		widget.setItemCommandListener((ItemCommandListener)null);
	}
	
	public void commandAction (Command c, Item i) {
    	System.out.println("cw: command action");
		
		if (i == expandedStyle.getInteractiveWidget() && c == nextCommand) {
			// BWD 23/8/2008 Ticket #69.  Added check for menu open
	    	// before passing on the hack.
			if(!cbox.isMenuOpened())
				cbox.questionAnswered();
		} else {
			//unrecognized commandAction, propagate to parent.
			cbox.commandAction(c, cbox);
		}
	}
	
	public void itemStateChanged (Item i) {
		//debugging
    	System.out.println("cw: item state");
    	if (i instanceof ChoiceGroup) {
    		ChoiceGroup cg = (ChoiceGroup)i;
    		System.out.println(cg.size());
    		for (int j = 0; j < cg.size(); j++)
    			System.out.println(cg.getString(j) + " " + cg.isSelected(j));
    		System.out.println("---");
    	}
		
		if (i == expandedStyle.getInteractiveWidget())
			cbox.questionAnswered();
	}
	
	public void UIHack (int hackType) {
		if (hackType == Chatterbox.UIHACK_SELECT_PRESS) {
			if (expandedStyle.getNextMode() == NEXT_ON_SELECT && expandedStyle instanceof TextEntryWidget) {
				String text = (((TextEntryWidget)expandedStyle).textField()).getText();
				System.out.println("Text equals: " + text);
				if (text == null || text.length() == 0) {
					commandAction(nextCommand, expandedStyle.getInteractiveWidget());
				}
				else {
					//Jan 14, 2009 - I don't know why only the P1i was setup to do this. Seems weird to me...
					
					commandAction(nextCommand, expandedStyle.getInteractiveWidget());
					//#if device.identifier == Sony-Ericsson/P1i
					//#endif
				}
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
			FormElementBinding newBinding = (FormElementBinding)binding.clone();
			ChatterboxWidget widget = new ChatterboxWidget(cbox, newBinding, this.getViewState(), (LabelWidget)label.clone(), null);
			return widget;
		}
		return null;
	}
	
	public ChatterboxWidget generateHeader() {
		int mult = -1;
		if(this.activeStyle instanceof LabelWidget) {
			LabelWidget label = (LabelWidget)this.activeStyle;
			mult = label.getMultiplicity();
		}
		LabelWidget labelStyle = new LabelWidget(mult);
		ChatterboxWidget widget = new ChatterboxWidget(cbox, (FormElementBinding)this.getBinding().clone(), ChatterboxWidget.VIEW_LABEL, labelStyle , null);

		return widget;
	}
	
	/** 
	 * @return The height of this widget that, when taken off screen, should result
	 * in a pinned header.
	 */
	public int getPinnableHeight() {
		return this.activeStyle.getPinnableHeight();
	}
}