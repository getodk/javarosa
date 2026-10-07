package org.javarosa.test.utils;

import java.time.Instant;
import java.util.TimeZone;
import org.joda.time.DateTimeUtils;
import org.junit.rules.ExternalResource;

public class FixedTimeZoneAndInstantRule extends ExternalResource {
    private final Instant instant;
    private final TimeZone timeZone;

    private TimeZone originalTimeZone;

    public FixedTimeZoneAndInstantRule(Instant instant, TimeZone timeZone) {
        this.instant = instant;
        this.timeZone = timeZone;
    }

    @Override
    protected void before() {
        originalTimeZone = TimeZone.getDefault();

        TimeZone.setDefault(timeZone);
        DateTimeUtils.setCurrentMillisFixed(instant.toEpochMilli());
    }

    @Override
    protected void after() {
        DateTimeUtils.setCurrentMillisSystem();
        TimeZone.setDefault(originalTimeZone);
    }
}