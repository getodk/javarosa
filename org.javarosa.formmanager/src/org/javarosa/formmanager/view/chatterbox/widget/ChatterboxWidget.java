package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.formmanager.model.temp.Prompt;
import org.javarosa.formmanager.model.temp.QuestionData;
import org.javarosa.formmanager.utility.QuestionStateListener;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Style;

public class ChatterboxWidget extends Container implements QuestionStateListener {
	public static final int VIEW_NOT_SET = -1;
	public static final int VIEW_EXPANDED = 0;
	public static final int VIEW_COLLAPSED = 1;
	
	private Prompt question;
	private int viewState = VIEW_NOT_SET;
	private IWidgetStyle collapsedStyle;
	private IWidgetStyleEditable expandedStyle;

	private IWidgetStyle activeStyle;
	private Style blankSlateStyle;

	public ChatterboxWidget (Prompt question, int viewState,
				 IWidgetStyle collapsedStyle,
				 IWidgetStyleEditable expandedStyle) {
		super(false);
		blankSlateStyle = this.getStyle();

		this.question = question;
		this.collapsedStyle = collapsedStyle;
		this.expandedStyle = expandedStyle;
		
		setViewState(viewState);

		question.registerStateObserver(this);
	}
	
	public void destroy () {
		question.unregisterStateObserver(this);
	}
	
	public Prompt getQuestion () {
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
			activeStyle.refreshWidget(question, question.getValue(), QuestionStateListener.CHANGE_INIT);
		}
	}


	public QuestionData getData () {
		if (viewState == VIEW_EXPANDED) {
			return expandedStyle.getData();
		} else {
			throw new IllegalStateException("Attempt to fetch data from widget not in expanded mode");
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
		activeStyle.reset();
		clear();
		setStyle(blankSlateStyle);
	}

	public void questionStateChanged (Prompt question, int changeFlags) {
		if (this.question != question)
			throw new IllegalStateException("Widget received event from foreign question");
		activeStyle.refreshWidget(question, question.getValue(), changeFlags);
	}
}