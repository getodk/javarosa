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

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;

/**
 * This class gives you all the information you need to display a caption when
 * your current FormIndex references a GroupEvent, RepeatPromptEvent, or
 * RepeatEvent.
 * 
 * 
 */
public class FormEntryCaption {

    FormDef form;
    FormIndex index;
    private GroupDef groupDef;
    private QuestionDef questionDef;


    public FormEntryCaption() {
    }


    public FormEntryCaption(FormDef form, FormIndex index) {
        this.form = form;
        this.index = index;

        IFormElement element = form.getChild(index);
        if (element instanceof GroupDef) {
            this.groupDef = (GroupDef) element;
        } else if (element instanceof QuestionDef) {
            this.questionDef = (QuestionDef) element;
        } else {
            throw new IllegalArgumentException("Unexpected type of IFormElement");
        }
    }


    protected QuestionDef getQuestionDef() {
        return questionDef;
    }


    public String getLongText() {
        String longText = groupDef == null ? questionDef.getLongText() : groupDef.getLongText();
        return substituteStringArgs(longText);
    }


    public String getShortText() {
        String shortText = groupDef == null ? questionDef.getShortText() : groupDef.getShortText();
        return substituteStringArgs(shortText);
    }


    public String substituteStringArgs(String templateStr) {
        return form.fillTemplateString(templateStr, index.getReference());
    }


    public int getMultiplicity() {
        return index.getElementMultiplicity();
    }


    public boolean repeats() {
        return groupDef == null ? false : groupDef.getRepeat();
    }
}
