package org.javarosa.core.test;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;

import java.util.Date;

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

    public static Matcher<IntegerData> intAnswer(int expectedAnswer) {
        return new TypeSafeMatcher<IntegerData>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("answer with value " + expectedAnswer);
            }

            @Override
            protected void describeMismatchSafely(IntegerData item, Description mismatchDescription) {
                mismatchDescription.appendText("was answer " + item.getDisplayText() + "(").appendValue(item.getValue()).appendText(")");
            }

            @Override
            protected boolean matchesSafely(IntegerData item) {
                return item.getValue().equals(expectedAnswer);
            }
        };
    }

    public static Matcher<IntegerData> intAnswer(Integer expectedAnswer) {
        return new TypeSafeMatcher<IntegerData>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("answer with value " + expectedAnswer);
            }

            @Override
            protected void describeMismatchSafely(IntegerData item, Description mismatchDescription) {
                mismatchDescription.appendText("was answer " + item.getDisplayText() + "(").appendValue(item.getValue()).appendText(")");
            }

            @Override
            protected boolean matchesSafely(IntegerData item) {
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

    public static Matcher<BooleanData> booleanAnswer(boolean expectedAnswer) {
        return new TypeSafeMatcher<BooleanData>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("answer with value " + expectedAnswer);
            }

            @Override
            protected void describeMismatchSafely(BooleanData item, Description mismatchDescription) {
                mismatchDescription.appendText("was answer " + item.getDisplayText() + "(").appendValue(item.getValue()).appendText(")");
            }

            @Override
            protected boolean matchesSafely(BooleanData item) {
                return item.getValue().equals(expectedAnswer);
            }
        };
    }

    public static Matcher<DateTimeData> dateTimeAnswer(long expectedDateTime) {
        return new TypeSafeMatcher<DateTimeData>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("answer with value " + expectedDateTime);
            }

            @Override
            protected void describeMismatchSafely(DateTimeData item, Description mismatchDescription) {
                mismatchDescription.appendText("was answer " + item.getDisplayText() + "(").appendValue(item.getValue()).appendText(")");
            }

            @Override
            protected boolean matchesSafely(DateTimeData item) {
                return item.getValue().equals(new Date(expectedDateTime));
            }
        };
    }
}
