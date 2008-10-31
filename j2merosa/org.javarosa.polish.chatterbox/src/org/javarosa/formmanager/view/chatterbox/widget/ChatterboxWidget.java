package org.javarosa.formmanager.view.chatterbox.widget;

import javax.microedition.lcdui.Command;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.QuestionStateListener;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;

public class ChatterboxWidget extends Container implements QuestionStateListener, ItemStateListener, ItemCommandListener {
	public static final int VIEW_NOT_SET = -1;
	public static final int VIEW_EXPANDED = 0;
	public static final int VIEW_COLLAPSED = 1;
	
	public static final int NEXT_ON_MANUAL = 1;
	public static final int NEXT_ON_ENTRY = 2;
	public static final int NEXT_ON_SELECT = 3;
	
	private Chatterbox cbox;
	private Command nextCommand;
	
	private QuestionDef question;
	private FormDef form; //needed to retrieve answers
	private int viewState = VIEW_NOT_SET;
	private IWidgetStyle collapsedStyle;
	private IWidgetStyleEditable expandedStyle;

	private IWidgetStyle activeStyle;
	//private Style blankSlateStyle;
	
	public ChatterboxWidget (Chatterbox cbox, QuestionDef question, FormDef form, int viewState,
			IWidgetStyle collapsedStyle, IWidgetStyleEditable expandedStyle) {
		this(cbox, question, form, viewState, collapsedStyle, expandedStyle, null);
	}
	
	public ChatterboxWidget (Chatterbox cbox, QuestionDef question, FormDef form, int viewState,
			IWidgetStyle collapsedStyle, IWidgetStyleEditable expandedStyle, 
			Style style) {
		super(false, style);
		//blankSlateStyle = this.getStyle();

		this.cbox = cbox;
        this.nextCommand = new Command("Next", Command.ITEM, 1);
        
		this.question = question;
		this.form = form;
		this.collapsedStyle = collapsedStyle;
		this.expandedStyle = expandedStyle;
				
		setViewState(viewState);

		question.registerStateObserver(this);
	}
	
	public void destroy () {
		if (viewState == VIEW_EXPANDED)
			detachWidget();
		
		question.unregisterStateObserver(this);
	}
	
	public QuestionDef getQuestion () {
		return question;
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
			
			activeStyle.initWidget(question, this);
			activeStyle.refreshWidget(question, form.getValue(question), QuestionStateListener.CHANGE_INIT);
			if (viewState == VIEW_EXPANDED) {
				attachWidget();
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

	public void questionStateChanged (QuestionDef question, int changeFlags) {
		if (this.question != question)
			throw new IllegalStateException("Widget received event from foreign question");
		activeStyle.refreshWidget(question, form.getValue(question), changeFlags);
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
			if (expandedStyle.getNextMode() == NEXT_ON_SELECT && expandedStyle.getInteractiveWidget() instanceof TextField) {
				String text = ((TextField)expandedStyle.getInteractiveWidget()).getText();
				if (text == null || text.length() == 0) {
					commandAction(nextCommand, expandedStyle.getInteractiveWidget());
				}
				else {
					//#if device.identifier == Sony-Ericsson/P1i
					commandAction(nextCommand, expandedStyle.getInteractiveWidget());
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
}