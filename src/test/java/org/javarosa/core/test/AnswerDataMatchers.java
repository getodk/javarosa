package org.javarosa.core.test;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;

public class AnswerDataMatchers {
    public static Matcher<StringData> stringAnswer(String expectedAnswer) {
        return new TypeSafeMatcher<StringData>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("answer with value " + expectedAnswer);
            }

            @Override
            protected void describeMismatchSafely(StringData item, Description mismatchDescription) {
                mismatchDescription.appendText("was answer " + item.getDisplayText() + "(").appendValue(item.getValue()).appendText(")");
            }

            @Override
            protected boolean matchesSafely(StringData item) {
                return item.getValue().equals(expectedAnswer);
            }
        };
    }

    public static <T extends IAnswerData> Matcher<T> answer(T expectedAnswer) {
        return new TypeSafeMatcher<T>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("answer " + expectedAnswer.getDisplayText() + "(").appendValue(expectedAnswer.getValue()).appendText(")");
            }

            @Override
            protected void describeMismatchSafely(T item, Description mismatchDescription) {
                mismatchDescription.appendText("was answer " + item.getDisplayText() + "(").appendValue(item.getValue()).appendText(")");
            }

            @Override
            protected boolean matchesSafely(T item) {
                return item.getValue().equals(expectedAnswer.getValue());
            }
        };
    }

    public static <T extends IAnswerData> Matcher<T> answerText(String expectedAnswerText) {
        return new TypeSafeMatcher<T>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("answer " + expectedAnswerText);
            }

            @Override
            protected void describeMismatchSafely(T item, Description mismatchDescription) {
                mismatchDescription.appendText("was answer " + item.getDisplayText() + "(").appendValue(item.getValue()).appendText(")");
            }

            @Override
            protected boolean matchesSafely(T item) {
                return item.getDisplayText().matches(expectedAnswerText);
            }
        };
    }
}
