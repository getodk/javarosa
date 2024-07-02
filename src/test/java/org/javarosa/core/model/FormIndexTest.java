package org.javarosa.core.model;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.model.FormIndex.createBeginningOfFormIndex;
import static org.javarosa.test.Scenario.getRef;

public class FormIndexTest {

    @Test
    public void getPreviousLevel_atBeginningOfForm_returnsNull() {
        assertThat(createBeginningOfFormIndex().getPreviousLevel(), is(nullValue()));
    }

    @Test
    public void getPreviousLevel_atTheTopLevelOfTheForm_returnsNull() {
        FormIndex formIndex = new FormIndex(0, getRef("/data/question"));
        assertThat(formIndex.getPreviousLevel(), is(nullValue()));
    }

    @Test
    public void getPreviousLevel_forIndexInGroup_returnsGroup() {
        FormIndex groupThenQuestionIndex = new FormIndex(
            new FormIndex(0, getRef("/data/group/question")),
            7,
            getRef("/data/group")
        );

        FormIndex groupIndex = new FormIndex(7, getRef("/data/group"));
        assertThat(groupThenQuestionIndex.getPreviousLevel(), is(groupIndex));
    }

    @Test
    public void getPreviousLevel_forIndexInNestedGroup_returnsInnerGroup() {
        FormIndex groupThenGroupThenQuestionIndex = new FormIndex(
            new FormIndex(
                new FormIndex(0, getRef("/data/outer_group/inner_group/question")),
                5,
                getRef("/data/outer_group/inner_group")),
            1,
            getRef("/data/outer_group")
        );

        FormIndex innerGroupIndex = new FormIndex(
            new FormIndex(5, getRef("/data/outer_group/inner_group")),
            1,
            getRef("/data/outer_group")
        );
        assertThat(groupThenGroupThenQuestionIndex.getPreviousLevel(), is(innerGroupIndex));
    }

    @Test
    public void getPreviousLevel_forRepeatIndex_returnsNull() {
        FormIndex repeatIndex = new FormIndex(
            0,
            2,
            getRef("/data/repeat[2]")
        );

        assertThat(repeatIndex.getPreviousLevel(), is(nullValue()));
    }

    @Test
    public void getPreviousLevel_forIndexInRepeat_returnsRepeatWithMultiplicity() {
        FormIndex repeatThenQuestionIndex = new FormIndex(
            new FormIndex(0, getRef("/data/repeat[2]/question")),
            7,
            2,
            getRef("/data/repeat[2]")
        );

        FormIndex repeatIndex = new FormIndex(7, 2, getRef("/data/repeat[2]"));
        assertThat(repeatThenQuestionIndex.getPreviousLevel(), is(repeatIndex));
    }

    @Test
    public void getPreviousLevel_forIndexInNestedRepeat_returnsInnerRepeatWithMultiplicity() {
        FormIndex repeatThenRepeatThenQuestionIndex = new FormIndex(
            new FormIndex(
                new FormIndex(0, getRef("/data/repeat[2]/repeat[3]/question")),
                0,
                3,
                getRef("/data/repeat[2]/question")),
            7,
            2,
            getRef("/data/repeat[2]")
        );

        FormIndex innerRepeatIndex = new FormIndex(
            new FormIndex(0, 3, getRef("/data/repeat[2]/repeat[3]")),
            7,
            2,
            getRef("/data/repeat[2]")
        );
        assertThat(repeatThenRepeatThenQuestionIndex.getPreviousLevel(), is(innerRepeatIndex));
    }
}