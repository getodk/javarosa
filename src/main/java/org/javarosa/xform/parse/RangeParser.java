package org.javarosa.xform.parse;

import org.javarosa.core.model.RangeQuestion;
import org.jetbrains.annotations.NotNull;
import org.kxml2.kdom.Element;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Features for parsing the range element
 */
public class RangeParser {
    static void populateQuestionWithRangeAttributes(RangeQuestion question, Element e) throws ParseException {
        final Set<String> rangeAttributeNames = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("start", "end", "step")));

        for (int i = 0; i < e.getAttributeCount(); i++) {
            final String attrName = e.getAttributeName(i);

            if (rangeAttributeNames.contains(attrName)) {
                final String attrStringValue = e.getAttributeValue(i);
                final BigDecimal attrDecimalValue = safeGetBigDecimal(attrName, attrStringValue);

                switch (attrName) {
                    case "start":
                        question.setRangeStart(attrDecimalValue);
                        break;
                    case "end":
                        question.setRangeEnd(attrDecimalValue);
                        break;
                    case "step":
                        question.setRangeStep(attrDecimalValue);
                        break;
                }
            }
        }
    }

    //todo returns null
    public static BigDecimal getDecimalValue(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    @NotNull
    static BigDecimal safeGetBigDecimal(String attrName, String attrStringValue) throws ParseException {
        final BigDecimal attrDecimalValue = getDecimalValue(attrStringValue);
        if (attrDecimalValue == null) {
            throw new ParseException(String.format("Value %s of range attribute %s can't be parsed as a decimal number", attrStringValue, attrName));
        }
        return attrDecimalValue;
    }
}
