package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.QuestionStateListener;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Style;

public class ChatterboxWidget extends Container implements QuestionStateListener {
	public static final int VIEW_NOT_SET = -1;
	public static final int VIEW_EXPANDED = 0;
	public static final int VIEW_COLLAPSED = 1;
	
	private QuestionDef question;
	private FormDef form; //needed to retrieve answers
	private int viewState = VIEW_NOT_SET;
	private IWidgetStyle collapsedStyle;
	private IWidgetStyleEditable expandedStyle;

	private IWidgetStyle activeStyle;
	private Style blankSlateStyle;

	public ChatterboxWidget (QuestionDef question, FormDef form, int viewState,
			IWidgetStyle collapsedStyle, IWidgetStyleEditable expandedStyle) {
		this(question, form, viewState, collapsedStyle, expandedStyle, null);
	}
	
	public ChatterboxWidget (QuestionDef question, FormDef form, int viewState,
			IWidgetStyle collapsedStyle, IWidgetStyleEditable expandedStyle,
			Style style) {
		super(false, style);
		blankSlateStyle = this.getStyle();

		this.question = question;
		this.form = form;
		this.collapsedStyle = collapsedStyle;
		this.expandedStyle = expandedStyle;
		
		setViewState(viewState);

		question.registerStateObserver(this);
	}
	
	public void destroy () {
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
			if (expandedStyle.focus())
				repaint();
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
		activeStyle.reset();
		clear();
		if (blankSlateStyle != null) {
			setStyle(blankSlateStyle);
		}
	}

	public void questionStateChanged (QuestionDef question, int changeFlags) {
		if (this.question != question)
			throw new IllegalStateException("Widget received event from foreign question");
		activeStyle.refreshWidget(question, form.getValue(question), changeFlags);
	}
}