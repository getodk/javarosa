package org.javarosa.core.model.actions;

import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.XFormParseException;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertThat;

/**
 * Specification: https://opendatakit.github.io/xforms-spec/#the-odk-new-repeat-event.
 */
public class OdkNewRepeatEventTest {
    @Test
    public void setValueOnRepeatInsertInBody_setsValueInRepeat() {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        assertThat(scenario.repeatInstancesOf("/data/my-repeat").size(), is(0));
        scenario.createMissingRepeats("/data/my-repeat[0]");
        assertThat(scenario.repeatInstancesOf("/data/my-repeat").size(), is(1));
        assertThat(scenario.answerOf("/data/my-repeat[0]/defaults-to-now"), is(notNullValue()));
    }

    @Test
    public void addingRepeat_doesNotChangeValueSetForPreviousRepeat() {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        scenario.createMissingRepeats("/data/my-repeat[0]");
        String firstRepeatDisplayText = scenario.answerOf("/data/my-repeat[0]/defaults-to-now").getDisplayText();
        assertThat(firstRepeatDisplayText, is(not(isEmptyOrNullString())));

        scenario.createMissingRepeats("/data/my-repeat[1]");
        assertThat(scenario.answerOf("/data/my-repeat[1]/defaults-to-now"), is(notNullValue()));

        assertThat(scenario.answerOf("/data/my-repeat[0]/defaults-to-now").getDisplayText(), is(firstRepeatDisplayText));
        assertThat(scenario.answerOf("/data/my-repeat[1]/defaults-to-now").getDisplayText(), is(not(firstRepeatDisplayText)));
    }

    @Test
    public void setValueOnRepeatInBody_usesCurrentContextForRelativeReferences() {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        scenario.answer("/data/my-toplevel-value", "12");

        scenario.createMissingRepeats("/data/my-repeat[0]");
        assertThat(scenario.answerOf("/data/my-repeat[0]/defaults-to-toplevel").getDisplayText(), is("14"));
    }

    @Test
    public void setValueOnRepeatWithCount_setsValueForEachRepeat() {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        while (!scenario.atTheEndOfForm()) {
            scenario.next();
        }

        assertThat(scenario.repeatInstancesOf("/data/my-jr-count-repeat").size(), is(4));

        assertThat(scenario.answerOf("/data/my-jr-count-repeat[0]/defaults-to-position").getDisplayText(), is("1"));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[1]/defaults-to-position").getDisplayText(), is("2"));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[2]/defaults-to-position").getDisplayText(), is("3"));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[3]/defaults-to-position").getDisplayText(), is("4"));
    }

    @Test
    public void repeatInFormDefInstance_neverFiresNewRepeatEvent() {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        assertThat(scenario.answerOf("/data/my-repeat-without-template[0]/my-value"), is(nullValue()));
        assertThat(scenario.answerOf("/data/my-repeat-without-template[1]/my-value"), is(nullValue()));

        scenario.createMissingRepeats("/data/my-repeat-without-template[2]");
        assertThat(scenario.answerOf("/data/my-repeat-without-template[2]/my-value").getDisplayText(), is("2"));
    }

    // Not part of ODK XForms so throws parse exception.
    @Test(expected = XFormParseException.class)
    public void setValueOnRepeatInsertInModel_notAllowed() {
        Scenario.init(r("event-odk-new-repeat-model.xml"));
    }
}
