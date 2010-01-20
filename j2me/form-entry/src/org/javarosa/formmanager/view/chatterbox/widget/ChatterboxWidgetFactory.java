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

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.util.externalizable.PrototypeFactoryDeprecated;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.chatterbox.Chatterbox;
import org.javarosa.formmanager.view.chatterbox.FakedFormEntryPrompt;

import de.enough.polish.ui.ChoiceGroup;

public class ChatterboxWidgetFactory {
	Chatterbox cbox;
	
	PrototypeFactoryDeprecated widgetFactory;
	
	boolean readOnly = false;
	
	public ChatterboxWidgetFactory (Chatterbox cbox) {
		widgetFactory = new PrototypeFactoryDeprecated();
		this.cbox = cbox;
	}
	
	public void registerExtendedWidget(int controlType, IWidgetStyle prototype) {
		widgetFactory.addNewPrototype(String.valueOf(controlType), prototype.getClass());
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
		
		switch (controlType) {
		case Constants.CONTROL_INPUT:
			switch (dataType) {
			case Constants.DATATYPE_INTEGER:
				expandedStyle = new NumericEntryWidget();
				break;
			case Constants.DATATYPE_DECIMAL:
				expandedStyle = new NumericEntryWidget(true);
				break;
			case Constants.DATATYPE_DATE_TIME:
				expandedStyle = new DateEntryWidget(true);
				break;
			case Constants.DATATYPE_DATE:
				//#if javarosa.useNewDatePicker 
				expandedStyle = new SimpleDateEntryWidget();
				//expandedStyle = new InlineDateEntryWidget();
				//#else
				expandedStyle = new DateEntryWidget();
				//#endif
				break;
			case Constants.DATATYPE_TIME:
				expandedStyle = new TimeEntryWidget();
				break;
			}
			break;
		case Constants.CONTROL_SELECT_ONE:
			int style;

			if ("minimal".equals(appearanceAttr))
				style = ChoiceGroup.POPUP;
			else
				style = ChoiceGroup.EXCLUSIVE;

			expandedStyle = new SelectOneEntryWidget(style);
			break;
		case Constants.CONTROL_SELECT_MULTI:
			expandedStyle = new SelectMultiEntryWidget();
			break;
		case Constants.CONTROL_TEXTAREA:
			expandedStyle = new TextEntryWidget();
			break;
		case Constants.CONTROL_TRIGGER:
			expandedStyle = new MessageWidget();
			break;
		case Constants.CONTROL_IMAGE_CHOOSE:
			expandedStyle = new ImageChooserWidget();
			break;
		case Constants.CONTROL_AUDIO_CAPTURE:
			expandedStyle = new AudioCaptureWidget();
			break;	
		}

		if (expandedStyle == null) { //catch types text, null, unsupported
			expandedStyle = new TextEntryWidget();
			
			String name = String.valueOf(controlType); //huh? controlType is an int
			Object widget = widgetFactory.getNewInstance(name);
			if (widget != null) {
				expandedStyle = (IWidgetStyleEditable) widget;
			}
		}
		
		if (collapsedStyle == null || expandedStyle == null)
			throw new IllegalStateException("No appropriate widget to render question");
		
		return new ChatterboxWidget(cbox, prompt, initViewState, collapsedStyle, expandedStyle);
	}
	
    public ChatterboxWidget getNewRepeatWidget (FormIndex index, FormEntryModel model, Chatterbox cbox) {
    	//GroupDef repeat = (GroupDef)f.explodeIndex(index).lastElement();

    	//damn linked lists...
    	FormIndex end = index;
    	while (!end.isTerminal()) {
    		end = end.getNextLevel();
    	}
    	int multiplicity = end.getInstanceIndex();
    	
    	String label = model.getCaptionPrompt(index).getLongText();
    	
    	FakedFormEntryPrompt prompt = new FakedFormEntryPrompt("Add " + (multiplicity > 0 ? "another " : "") + (label == null || label.length() == 0 ? "repetition" : label) + "?",
    										Constants.CONTROL_SELECT_ONE, Constants.DATATYPE_TEXT);
    	prompt.addSelectChoice(new SelectChoice("Yes", "y", false));
    	prompt.addSelectChoice(new SelectChoice("No", "n", false));
		
		return new ChatterboxWidget(cbox, prompt, ChatterboxWidget.VIEW_EXPANDED, new CollapsedWidget(), new SelectOneEntryWidget(ChoiceGroup.EXCLUSIVE));
    }
    
    public ChatterboxWidget getNewLabelWidget(FormIndex index, String text){
    	//Label Widget;
    	int multiplicity = index.getInstanceIndex();
    	FormEntryPrompt fakePrompt = new FakedFormEntryPrompt(text, Constants.CONTROL_LABEL, Constants.DATATYPE_TEXT);
    	return new ChatterboxWidget(cbox, fakePrompt,ChatterboxWidget.VIEW_LABEL, new LabelWidget(multiplicity), null);
    }
        
    public void setReadOnly(boolean readOnly) {
    	this.readOnly = readOnly;
    }
}
