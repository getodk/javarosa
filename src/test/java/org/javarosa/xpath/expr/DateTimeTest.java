package org.javarosa.xpath.expr;

import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.test.Scenario;
import org.javarosa.xform.parse.XFormParser;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.label;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

/**
 * SIDE-EFFECT: Mutates JVM-default timezone and Joda-Time's current millis.
 * Must not be run in parallel with other timezone-sensitive tests.
 */
public class DateTimeTest {
    private TimeZone originalTimeZone;
    private static final String SIMULATED_NOW = "1998-05-23T17:49:42.123-07:00"; // 1998-05-24 in UTC
    private static final Instant SIMULATED_INSTANT = OffsetDateTime.parse(SIMULATED_NOW).toInstant();

    private static final TimeZone SIMULATED_TZ = TimeZone.getTimeZone("America/Los_Angeles");

    @Before
    public void setUp() {
        originalTimeZone = TimeZone.getDefault();

        DateTimeUtils.setCurrentMillisFixed(SIMULATED_INSTANT.toEpochMilli());

        TimeZone.setDefault(SIMULATED_TZ);
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    public void nowLabelOutput_isIsoOffsetDateTime() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init(html(
            head(
                title("Date time"),
                model(
                    mainInstance(t("data id=\"date-time\"",
                        t("now"),
                        t("now_note")
                    )),
                    bind("/data/now").type("string").calculate("now()")
                )
            ),
            body(
                input("/data/now_note",
                    label("Now: <output ref=\"/data/now\"/>"))
            )));

        scenario.next();
        assertThat(scenario.answerOf("/data/now").getValue(), is(Date.from(SIMULATED_INSTANT)));

        assertThat(scenario.answerOf("/data/now").getDisplayText(), is("23/05/98 17:49"));
        FormEntryCaption nowCaption = new FormEntryCaption(scenario.getFormDef(), scenario.getCurrentIndex());
        assertThat(nowCaption.getQuestionText(), is("Now: 1998-05-23T17:49:42.123-07:00"));
    }

    @Test
    public void dateTimeStringLabelOutput_isIsoOffsetDateTime() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init(html(
            head(
                title("Date time"),
                model(
                    mainInstance(t("data id=\"date-time\"",
                        t("date_time", "1998-05-23T17:49:42.123-07:00"),
                        t("date_time_note")
                    )),
                    bind("/data/date_time").type("string")
                )
            ),
            body(
                input("/data/date_time_note",
                    label("Date time: <output ref=\"/data/date_time\"/>"))
            )));

        scenario.next();
        assertThat(scenario.answerOf("/data/date_time").getValue(), is("1998-05-23T17:49:42.123-07:00"));
        assertThat(scenario.answerOf("/data/date_time").getDisplayText(), is("1998-05-23T17:49:42.123-07:00"));
        FormEntryCaption caption = new FormEntryCaption(scenario.getFormDef(), scenario.getCurrentIndex());
        assertThat(caption.getQuestionText(), is("Date time: 1998-05-23T17:49:42.123-07:00"));
    }

    @Test
    public void dateTimeQuestionLabelOutput_isIsoOffsetDateTime() throws IOException, XFormParser.ParseException {
        Scenario scenario = Scenario.init(html(
            head(
                title("Date time"),
                model(
                    mainInstance(t("data id=\"date-time\"",
                        t("date_time"),
                        t("date_time_note")
                    )),
                    bind("/data/date_time").type("dateTime")
                )
            ),
            body(
                input("/data/date_time",
                    label("Enter a date time")),
                input("/data/date_time_note",
                    label("Date time: <output ref=\"/data/date_time\"/>"))
            )));

        scenario.next();
        scenario.answer(SIMULATED_INSTANT, true);
        assertThat(scenario.answerOf("/data/date_time").getValue(), is(Date.from(SIMULATED_INSTANT)));
        assertThat(scenario.answerOf("/data/date_time").getDisplayText(), is("23/05/98 17:49"));

        scenario.next();
        FormEntryCaption caption = new FormEntryCaption(scenario.getFormDef(), scenario.getCurrentIndex());
        assertThat(caption.getQuestionText(), is("Date time: 1998-05-23T17:49:42.123-07:00"));
    }

    @Test
    public void todayLabelOutput_isIsoDate() throws IOException, XFormParser.ParseException {
        Date expectedDay = Date.from(
            SIMULATED_INSTANT.atZone(SIMULATED_TZ.toZoneId())
                .toLocalDate()
                .atStartOfDay(SIMULATED_TZ.toZoneId())
                .toInstant()
        );

        Scenario scenario = Scenario.init(html(
            head(
                title("Date"),
                model(
                    mainInstance(t("data id=\"date\"",
                        t("today"),
                        t("today_note")
                    )),
                    bind("/data/today").type("date").calculate("today()")
                )
            ),
            body(
                input("/data/today_note",
                    label("Today: <output ref=\"/data/today\"/>"))
            )));

        scenario.next();
        assertThat(scenario.answerOf("/data/today").getValue(), is(expectedDay));

        assertThat(scenario.answerOf("/data/today").getDisplayText(), is("23/05/98"));
        FormEntryCaption nowCaption = new FormEntryCaption(scenario.getFormDef(), scenario.getCurrentIndex());
        assertThat(nowCaption.getQuestionText(), is("Today: 1998-05-23"));
    }

    // Date field type, now() expression
    @Test
    public void nowDateLabelOutput_isIsoDate() throws IOException, XFormParser.ParseException {
        Date expectedDay = Date.from(
            SIMULATED_INSTANT.atZone(SIMULATED_TZ.toZoneId())
                .toLocalDate()
                .atStartOfDay(SIMULATED_TZ.toZoneId())
                .toInstant()
        );

        Scenario scenario = Scenario.init(html(
            head(
                title("Now date"),
                model(
                    mainInstance(t("data id=\"now-date\"",
                        t("now"),
                        t("now_note")
                    )),
                    bind("/data/now").type("date").calculate("now()")
                )
            ),
            body(
                input("/data/now_note",
                    label("Today: <output ref=\"/data/now\"/>"))
            )));

        scenario.next();
        assertThat(scenario.answerOf("/data/now").getValue(), is(expectedDay));

        assertThat(scenario.answerOf("/data/now").getDisplayText(), is("23/05/98"));
        FormEntryCaption nowCaption = new FormEntryCaption(scenario.getFormDef(), scenario.getCurrentIndex());
        assertThat(nowCaption.getQuestionText(), is("Today: 1998-05-23"));
    }

    @Test
    public void dateQuestionLabelOutput_isIsoDate() throws IOException, XFormParser.ParseException {
        Date expectedDay = Date.from(
            SIMULATED_INSTANT.atZone(SIMULATED_TZ.toZoneId())
                .toLocalDate()
                .atStartOfDay(SIMULATED_TZ.toZoneId())
                .toInstant()
        );

        Scenario scenario = Scenario.init(html(
            head(
                title("Date"),
                model(
                    mainInstance(t("data id=\"date\"",
                        t("date"),
                        t("date_note")
                    )),
                    bind("/data/date").type("date")
                )
            ),
            body(
                input("/data/date",
                    label("Enter a date")),
                input("/data/date_note",
                    label("Date: <output ref=\"/data/date\"/>"))
            )));

        scenario.next();
        scenario.answer(SIMULATED_INSTANT, false);
        assertThat(scenario.answerOf("/data/date").getValue(), is(expectedDay));
        assertThat(scenario.answerOf("/data/date").getDisplayText(), is("23/05/98"));

        scenario.next();
        FormEntryCaption caption = new FormEntryCaption(scenario.getFormDef(), scenario.getCurrentIndex());
        assertThat(caption.getQuestionText(), is("Date: 1998-05-23"));
    }
}
