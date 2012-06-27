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

import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.api.FormMultimediaController;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;
import org.javarosa.formmanager.view.chatterbox.FakedFormEntryPrompt;
import org.javarosa.formmanager.view.widgets.CollapsedWidget;
import org.javarosa.formmanager.view.widgets.IWidgetStyle;
import org.javarosa.formmanager.view.widgets.IWidgetStyleEditable;
import org.javarosa.formmanager.view.widgets.LabelWidget;
import org.javarosa.formmanager.view.widgets.SelectOneEntryWidget;
import org.javarosa.formmanager.view.widgets.WidgetFactory;

import de.enough.polish.ui.ChoiceGroup;

public class ChatterboxWidgetFactory {
	Chatterbox cbox;
		
	private FormMultimediaController mediaController;
	
	boolean readOnly = false;
	
	WidgetFactory widgetFactory;
	
	public ChatterboxWidgetFactory (Chatterbox cbox, FormMultimediaController mediaController) {
		this(cbox, mediaController, new WidgetFactory(true));
	}
	
	public ChatterboxWidgetFactory (Chatterbox cbox, FormMultimediaController mediaController, WidgetFactory factory) {
		this.cbox = cbox;
		this.mediaController = mediaController;
		this.widgetFactory = factory;
	}
	
	/**
	 * NOTE: Only applicable for Questions right now, not any other kind of IFormElement
	 * @param questionIndex
	 * @param form
	 * @param initViewState
	 * @return
	 */
	public ChatterboxWidget getWidget (FormIndex questionIndex, FormEntryModel model, int initViewState) {
		IWidgetStyle collapsedStyle = null;
		IWidgetStyleEditable expandedStyle = null;
		
		FormEntryPrompt prompt = model.getQuestionPrompt(questionIndex);
		
		int controlType = prompt.getControlType();
		int dataType = prompt.getDataType();
		
		String appearanceAttr = prompt.getPromptAttributes();
		
		collapsedStyle = new CollapsedWidget();
		((CollapsedWidget)collapsedStyle).setSeekable(this.readOnly);
		
		expandedStyle = widgetFactory.getWidget(controlType,dataType,appearanceAttr);
		
		if (collapsedStyle == null || expandedStyle == null) {
			throw new IllegalStateException("No appropriate widget to render question");
		}
		
		expandedStyle.registerMultimediaController(mediaController);
		ChatterboxWidget widget = new ChatterboxWidget(cbox, prompt, initViewState, collapsedStyle, expandedStyle);
		prompt.register(widget);
		return widget;
	}
	
	
    public ChatterboxWidget getNewRepeatWidget (FormIndex index, FormEntryModel model, Chatterbox cbox) {
    	//GroupDef repeat = (GroupDef)f.explodeIndex(index).lastElement();

    	//damn linked lists...
    	FormIndex end = index;
    	while (!end.isTerminal()) {
    		end = end.getNextLevel();
    	}
    	int multiplicity = end.getInstanceIndex();
    	
    	FormEntryCaption p = model.getCaptionPrompt(index);
		
		String label; //decide what text form to use.
	
		label = p.getLongText();
		if(label == null){
			label = p.getShortText();
		}
    	
		String labelInner = (label == null || label.length() == 0 ? Localization.get("repeat.repitition") : label);

		String promptLabel = Localization.get((multiplicity > 0 ? "repeat.message.multiple" : "repeat.message.single"), new String[] {labelInner});
    	
    	FakedFormEntryPrompt prompt = new FakedFormEntryPrompt(promptLabel,
    										Constants.CONTROL_SELECT_ONE, Constants.DATATYPE_TEXT);
    	prompt.addSelectChoice(new SelectChoice(null,Localization.get("yes"), "y", false));
    	prompt.addSelectChoice(new SelectChoice(null,Localization.get("no"), "n", false));
		
		return new ChatterboxWidget(cbox, prompt, ChatterboxWidget.VIEW_EXPANDED, new CollapsedWidget(), new SelectOneEntryWidget(ChoiceGroup.EXCLUSIVE));
    }

    public ChatterboxWidget getRepeatJunctureWidget (FormIndex index, FormEntryModel model, Chatterbox cbox) {
    	FormEntryCaption capt = model.getCaptionPrompt(index);
    	Vector<String> choices = capt.getRepetitionsText();
    	FormEntryCaption.RepeatOptions repopt = capt.getRepeatOptions();
    	
    	FakedFormEntryPrompt prompt = new FakedFormEntryPrompt(repopt.header, Constants.CONTROL_SELECT_ONE, Constants.DATATYPE_TEXT);
    	for (int i = 0; i < choices.size(); i++) {
        	prompt.addSelectChoice(new SelectChoice(null, choices.elementAt(i), "rep" + i, false));
    	}
    	
    	if (repopt.add != null) {
    		prompt.addSelectChoice(new SelectChoice(null, repopt.add, "new", false));
    	}
    	if (repopt.delete != null) {
    		prompt.addSelectChoice(new SelectChoice(null, repopt.delete, "del", false));
    	}
    	prompt.addSelectChoice(new SelectChoice(null, repopt.done, "done", false));
		
		return new ChatterboxWidget(cbox, prompt, ChatterboxWidget.VIEW_EXPANDED, new CollapsedWidget(), new SelectOneEntryWidget(ChoiceGroup.EXCLUSIVE));
    }

    public ChatterboxWidget getRepeatDeleteWidget (FormIndex index, FormEntryModel model, Chatterbox cbox) {
    	FormEntryCaption capt = model.getCaptionPrompt(index);
    	Vector<String> choices = capt.getRepetitionsText();
    	
    	FakedFormEntryPrompt prompt = new FakedFormEntryPrompt(capt.getRepeatOptions().delete_header, Constants.CONTROL_SELECT_ONE, Constants.DATATYPE_TEXT);
    	for (int i = 0; i < choices.size(); i++) {
        	prompt.addSelectChoice(new SelectChoice(null, choices.elementAt(i), "del" + i, false));
    	}
		
		return new ChatterboxWidget(cbox, prompt, ChatterboxWidget.VIEW_EXPANDED, new CollapsedWidget(), new SelectOneEntryWidget(ChoiceGroup.EXCLUSIVE));
    }
    
    public ChatterboxWidget getNewLabelWidget(FormIndex index, String text){
    	//Label Widget;
    	FormEntryPrompt fakePrompt = new FakedFormEntryPrompt(text, Constants.CONTROL_LABEL, Constants.DATATYPE_TEXT);
    	return new ChatterboxWidget(cbox, fakePrompt,ChatterboxWidget.VIEW_LABEL, new LabelWidget(), null);
    }
        
    public void setReadOnly(boolean readOnly) {
    	this.readOnly = readOnly;
    }
}
