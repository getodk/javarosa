package org.javarosa.core.model.test;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.form.api.FormEntryPrompt;

public class DummyFormEntryPrompt extends FormEntryPrompt {
    String textId;
    Localizer localizer;

    public DummyFormEntryPrompt(Localizer localizer, String textId, QuestionDef q) {
        this.localizer = localizer;
        this.textId = textId;
        this.element = q;
    }

    protected String getTextID() {
        return textId;
    }

    protected Localizer localizer() {
        return localizer;
    }

    protected String substituteStringArgs(String template) {
        return template;
    }
}
