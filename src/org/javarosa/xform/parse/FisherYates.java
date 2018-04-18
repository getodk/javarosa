package org.javarosa.xform.parse;

import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class FisherYates {
    @SuppressWarnings("unchecked")
    public static void shuffle(List<?> list, Random random) {
        int size = list.size();
        Object[] array = list.toArray();
        Object[] result = new Object[size];

        for (int i = 0; i < size; ++i) {
            int j = Double.valueOf(random.nextDouble() * (i + 1)).intValue();

            if (j != i)
                result[i] = result[j];

            result[j] = array[i];
        }

        ListIterator it = list.listIterator();
        for (Object e : result) {
            it.next();
            it.set(e);
        }
    }
}
