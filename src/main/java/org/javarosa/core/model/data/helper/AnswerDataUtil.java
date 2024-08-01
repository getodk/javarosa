package org.javarosa.core.model.data.helper;

import org.javarosa.core.model.data.IAnswerData;

public class AnswerDataUtil {
    public static int answerDataToInt(IAnswerData countData) {
        if (countData == null) {
            return 0;
        }

        Object count = countData.getValue();
        if (count instanceof Integer) {
            return (int) count;
        } else if (count instanceof Double) {
            return (int) Math.floor((Double) count);
        } else if (count instanceof Long) {
            return (int) ((Long) count).longValue();
        } else if (count instanceof String) {
            try {
                double value = Double.parseDouble((String) count);
                return (int) Math.floor(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
