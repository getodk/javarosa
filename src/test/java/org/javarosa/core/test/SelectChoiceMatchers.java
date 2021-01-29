package org.javarosa.core.test;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.javarosa.core.model.SelectChoice;

public class SelectChoiceMatchers {
    public static Matcher<SelectChoice> choice(String expectedValue) {
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

    public static Matcher<SelectChoice> choice(String expectedValue, String expectedLabelOrId) {
        return new TypeSafeMatcher<SelectChoice>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("choice " + expectedLabelOrId + "(\"" + expectedValue + "\")");
            }

            @Override
            protected boolean matchesSafely(SelectChoice item) {
                return item.getValue().equals(expectedValue) &&
                    (item.isLocalizable() ? item.getTextID().equals(expectedLabelOrId)
                        : item.getLabelInnerText().equals(expectedLabelOrId));
            }

            @Override
            protected void describeMismatchSafely(SelectChoice item, Description mismatchDescription) {
                mismatchDescription.appendText("was choice with value \"" + item.getValue() + "\"");
            }
        };
    }
}
