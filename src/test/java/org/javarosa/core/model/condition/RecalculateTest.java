package org.javarosa.core.model.condition;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Before;
import org.junit.Test;
public class RecalculateTest {
    private FormDef formDef;

    @Before
    public void setUp() throws XFormParser.ParseException {
        FormParseInit fpi = new FormParseInit(r("calculate-now.xml"));
        formDef = fpi.getFormDef();
        formDef.initialize(true, new InstanceInitializationFactory());
    }

    @Test
    public void testComputedConstraintText() {
        FormIndex nowNoteField = new FormIndex(0, 0, formDef.getMainInstance().getRoot().getChild("now_note", 0).getRef());
        String actualQuestionText = new FormEntryPrompt(formDef, nowNoteField).getQuestionText();
        assertEquals("2018-01-01T10:20:30.400", actualQuestionText);
    }
}