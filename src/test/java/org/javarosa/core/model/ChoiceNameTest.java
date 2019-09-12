package org.javarosa.core.model;

import org.javarosa.core.test.Scenario;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.test.utils.ResourcePathHelper.r;

public class ChoiceNameTest {
    @Test public void choiceNameCallOnLiteralChoiceValue_getsChoiceName() {
        Scenario scenario = Scenario.init(r("jr-choice-name-repeat.xml"));
        assertThat(scenario.answerOf("/jr-choice-name-repeat/literal_choice_name"), is(stringAnswer("Choice 2")));
    }

    @Test public void choiceNameCallOutsideOfRepeatWithStaticChoices_getsChoiceName() {
        Scenario scenario = Scenario.init(r("jr-choice-name-repeat.xml"));
        scenario.answer("/jr-choice-name-repeat/select_one_outside", "choice3");
        assertThat(scenario.answerOf("/jr-choice-name-repeat/select_one_name_outside"), is(stringAnswer("Choice 3")));
    }

    @Test public void choiceNameCallInRepeatWithStaticChoices_getsChoiceName() {
        Scenario scenario = Scenario.init(r("jr-choice-name-repeat.xml"));
        scenario.answer("/jr-choice-name-repeat/my-repeat[1]/select_one", "choice4");
        scenario.answer("/jr-choice-name-repeat/my-repeat[2]/select_one", "choice1");
        scenario.answer("/jr-choice-name-repeat/my-repeat[3]/select_one", "choice5");

        assertThat(scenario.answerOf("/jr-choice-name-repeat/my-repeat[1]/select_one_name"), is(stringAnswer("Choice 4")));
        assertThat(scenario.answerOf("/jr-choice-name-repeat/my-repeat[2]/select_one_name"), is(stringAnswer("Choice 1")));
        assertThat(scenario.answerOf("/jr-choice-name-repeat/my-repeat[3]/select_one_name"), is(stringAnswer("Choice 5")));
    }

    // TODO: tests with translations, dynamic itemsets, complex choice filters
}
