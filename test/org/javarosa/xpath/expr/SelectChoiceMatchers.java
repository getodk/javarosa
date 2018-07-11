package org.javarosa.xpath.expr;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.javarosa.core.model.SelectChoice;

class SelectChoiceMatchers {
    static Matcher<SelectChoice> choice(String expectedValue) {
        return new TypeSafeMatcher<SelectChoice>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("choice with value \"" + expectedValue + "\"");
            }

            @Override
            protected boolean matchesSafely(SelectChoice item) {
                return item.getValue().equals(expectedValue);
            }

            @Override
            protected void describeMismatchSafely(SelectChoice item, Description mismatchDescription) {
                mismatchDescription.appendText("was choice with value \"" + item.getValue() + "\"");
            }
        };
    }
}
