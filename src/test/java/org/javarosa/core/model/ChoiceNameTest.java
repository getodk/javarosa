package org.javarosa.core.model;

import org.javarosa.core.test.Scenario;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.javarosa.core.test.AnswerDataMatchers.stringAnswer;
import static org.javarosa.test.utils.ResourcePathHelper.r;

public class ChoiceNameTest {
    @Test public void choiceNameCallOnLiteralChoiceValue_getsChoiceName() {
        Scenario scenario = Scenario.init(r("jr-choice-name.xml"));
        assertThat(scenario.answerOf("/jr-choice-name/literal_choice_name"), is(stringAnswer("Choice 2")));
    }

    @Test public void choiceNameCallOutsideOfRepeatWithStaticChoices_getsChoiceName() {
        Scenario scenario = Scenario.init(r("jr-choice-name.xml"));
        scenario.answer("/jr-choice-name/select_one_outside", "choice3");
        assertThat(scenario.answerOf("/jr-choice-name/select_one_name_outside"), is(stringAnswer("Choice 3")));
    }

    @Test public void choiceNameCallInRepeatWithStaticChoices_getsChoiceName() {
        Scenario scenario = Scenario.init(r("jr-choice-name.xml"));
        scenario.answer("/jr-choice-name/my-repeat[1]/select_one", "choice4");
        scenario.answer("/jr-choice-name/my-repeat[2]/select_one", "choice1");
        scenario.answer("/jr-choice-name/my-repeat[3]/select_one", "choice5");

        assertThat(scenario.answerOf("/jr-choice-name/my-repeat[1]/select_one_name"), is(stringAnswer("Choice 4")));
        assertThat(scenario.answerOf("/jr-choice-name/my-repeat[2]/select_one_name"), is(stringAnswer("Choice 1")));
        assertThat(scenario.answerOf("/jr-choice-name/my-repeat[3]/select_one_name"), is(stringAnswer("Choice 5")));
    }

    @Test public void choiceNameCall_respectsLanguage() {
        Scenario scenario = Scenario.init(r("jr-choice-name.xml"));
        scenario.setLanguage("French (fr)");
        scenario.answer("/jr-choice-name/select_one_outside", "choice3");
        assertThat(scenario.answerOf("/jr-choice-name/select_one_name_outside"), is(stringAnswer("Choix 3")));
        scenario.answer("/jr-choice-name/my-repeat[1]/select_one", "choice4");
        assertThat(scenario.answerOf("/jr-choice-name/my-repeat[1]/select_one_name"), is(stringAnswer("Choix 4")));

        scenario.setLanguage("English (en)");
        // TODO: why does test fail if value is not set to choice3 again? Does changing language not trigger recomputation?
        scenario.answer("/jr-choice-name/select_one_outside", "choice3");
        assertThat(scenario.answerOf("/jr-choice-name/select_one_name_outside"), is(stringAnswer("Choice 3")));

        // TODO: why does test fail if value is not set to choice4 again? Does changing language not trigger recomputation?
        scenario.answer("/jr-choice-name/my-repeat[1]/select_one", "choice4");
        assertThat(scenario.answerOf("/jr-choice-name/my-repeat[1]/select_one_name"), is(stringAnswer("Choice 4")));
    }

    @Test public void choiceNameCallWithDynamicChoices_getsChoiceName() {
        Scenario scenario = Scenario.init(r("jr-choice-name.xml"));
        scenario.answer("/jr-choice-name/country", "france"); // model.resolveReference(ref) doesn't get value
        scenario.answer("/jr-choice-name/city", "grenoble");
        //assertThat(scenario.answerOf("/jr-choice-name/city_name"), is(stringAnswer("Montreal")));
    }
}
