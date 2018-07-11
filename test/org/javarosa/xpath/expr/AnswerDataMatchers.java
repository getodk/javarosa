package org.javarosa.xpath.expr;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.javarosa.core.model.data.IAnswerData;

class AnswerDataMatchers {
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
}
