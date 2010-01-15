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

package org.javarosa.form.api;

import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;

public class FormEntryPrompt extends FormEntryCaption {

    TreeElement mTreeElement;

    public FormEntryPrompt(){
    }
    
    public FormEntryPrompt(FormDef form, FormIndex index) {
    	super(form, index);
    	this.mTreeElement = form.getDataModel().resolveReference(index.getReference());
    }

    public int getControlType() {
        return getQuestionDef().getControlType();
    }

    public int getDataType() {
        return mTreeElement.dataType;
    }

    // attributes available in the bind, instance and body
    public String getPromptAttributes() {
        return null;
    }

    public IAnswerData getAnswerValue() {
        return mTreeElement.getValue();
    }

    public String getAnswerText() {
        return mTreeElement.getValue().getDisplayText();
    }

    public String getConstraintText() {
        return mTreeElement.getConstraint().constraintMsg;
    }
    
    public Vector<SelectChoice> getSelectChoices() {
        return getQuestionDef().getChoices();
    }
 
    public String getHelpText() {
        return getQuestionDef().getHelpText();
    }

    public boolean isRequired() {
        return mTreeElement.required;
    }

    public boolean isReadOnly() {
        return !mTreeElement.isEnabled();
    }
}
