package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.javarosa.core.model.utils.DateUtils.secTicksAsNanoSeconds;
import static org.junit.Assert.assertEquals;

public class DateFieldsTest {

    @Test public void testAsLocalDateTime() {
        DateFields fields = DateFields.of(2023, 1, 1);
        LocalDateTime localDateTime = fields.asLocalDateTime();
        assertEquals(LocalDateTime.of(2023, Month.JANUARY, 1, 0, 0, 0), localDateTime);

    }
    @Test public void convertsSecTicksToNanoTicks() {
        DateFields fields = DateFields.of(2023, 1, 1, 0, 0, 0, 123);
        LocalDateTime localDateTime = fields.asLocalDateTime();
        assertEquals(LocalDateTime.of(2023, Month.JANUARY, 1, 0, 0, 0, secTicksAsNanoSeconds(123)), localDateTime);
    }
}