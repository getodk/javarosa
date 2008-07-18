package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.clforms.api.Prompt;

import de.enough.polish.ui.ChoiceGroup;

public class ChatterboxWidgetFactory {
	public ChatterboxWidget getWidget (Prompt question, int initViewState) {
		IWidgetStyle collapsedStyle = null;
		IWidgetStyleEditable expandedStyle = null;
		
		int controlType = question.getFormControlType();
		int dataType = question.getReturnType();
		String appearanceAttr = question.getAppearanceString();
		
		collapsedStyle = new CollapsedWidget();
		
		switch (controlType) {
		case org.javarosa.clforms.api.Constants.INPUT:
			switch (dataType) {
			case org.javarosa.clforms.api.Constants.RETURN_INTEGER:
				expandedStyle = new NumericEntryWidget();
				break;
			case org.javarosa.clforms.api.Constants.RETURN_DATE:
				expandedStyle = new DateEntryWidget();
				break;
			default:
				expandedStyle = new TextEntryWidget();
				break;
			}
			break;
		case org.javarosa.clforms.api.Constants.SELECT1:
			int style;
			
			if ("minimal".equals(appearanceAttr))
				style = ChoiceGroup.POPUP;
			else
				style = ChoiceGroup.EXCLUSIVE;
			
			expandedStyle = new SelectOneEntryWidget(style);
			break;
		case org.javarosa.clforms.api.Constants.SELECT:
			expandedStyle = new SelectMultiEntryWidget();
			break;
		case org.javarosa.clforms.api.Constants.TEXTAREA:
			expandedStyle = new TextEntryWidget();
			break;
		}
		
		if (collapsedStyle == null || expandedStyle == null)
			throw new IllegalStateException("No appropriate widget to render question");
		
		return new ChatterboxWidget(question, initViewState, collapsedStyle, expandedStyle);
	}
}
