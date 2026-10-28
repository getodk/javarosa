/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.data.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.javarosa.core.test.AnswerDataMatchers.timeAnswer;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.test.Scenario;
import org.javarosa.test.XFormsElement;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Test;

public class TimeDataTests {

    @Test
    public void getValue_doesNotExposeMutableState() {
        TimeData data = new TimeData().cast(new UncastData("10:45:00"));

        Date value = (Date) data.getValue();
        value.setTime(0);

        assertEquals("10:45:00", data.uncast().getValue());
    }

    @Test
    public void getValue_usesSentinelDate() {
        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            TimeData data = new TimeData().cast(new UncastData("10:30:00"));

            assertThat(data.getValue(), is(DateUtils.getDateForTime(LocalTime.parse("10:30:00"))));
        });
    }

    @Test
    public void setValue_rejectsNullWithoutChangingTime() {
        TimeData data = new TimeData().cast(new UncastData("10:45:00"));

        try {
            data.setValue(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // Expected
        }

        assertEquals("10:45:00", data.uncast().getValue());
    }

    @Test
    public void setValue_preservesTime() {
        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            TimeData original = new TimeData().cast(new UncastData("10:30:00"));

            TimeData restored = new TimeData((Date) original.getValue());

            assertThat(restored.uncast().getValue(), is("10:30:00"));
        });
    }

    @Test
    public void cast_maintainsCanonicalValue() {
        TimeData data = new TimeData().cast(new UncastData("10:45:00"));
        assertEquals("10:45:00", data.uncast().getValue());
    }

    @Test
    public void cast_truncatesTimezoneOffset() {
        TimeData negativeOffset = new TimeData().cast(new UncastData("10:45:59-07:00"));
        assertEquals("10:45:59", negativeOffset.uncast().getValue());

        TimeData positiveOffset = new TimeData().cast(new UncastData("10:45:59+02:00"));
        assertEquals("10:45:59", positiveOffset.uncast().getValue());

        TimeData gmt = new TimeData().cast(new UncastData("10:45:59Z"));
        assertEquals("10:45:59", gmt.uncast().getValue());
    }

    @Test
    public void cast_truncatesMilliseconds() {
        TimeData data = new TimeData().cast(new UncastData("10:45:00.187"));
        assertEquals("10:45:00", data.uncast().getValue());
    }

    @Test
    public void cast_truncatesRatherThanRoundsMilliseconds() {
        TimeData data = new TimeData().cast(new UncastData("10:45:59.999"));
        assertEquals("10:45:59", data.uncast().getValue());
    }

    @Test
    public void cast_truncatesMillisecondsAndOffset() {
        TimeData data = new TimeData().cast(new UncastData("10:45:59.987-07:00"));
        assertEquals("10:45:59", data.uncast().getValue());
    }

    @Test
    public void cast_acceptsBoundaryTimes() {
        assertThat(new TimeData().cast(new UncastData("00:00:00")).uncast().getValue(), is("00:00:00"));
        assertThat(new TimeData().cast(new UncastData("23:59:59")).uncast().getValue(), is("23:59:59"));
    }

    @Test
    public void cast_rejectsInvalidTimes() {
        String[] invalidTimes = {
            "24:00:00",
            "10:60:00",
            "10:45:60",
            "-01:30:00",
            "not-a-time"
        };

        for (String time : invalidTimes) {
            try {
                new TimeData().cast(new UncastData(time));
                fail("Expected invalid time to be rejected: " + time);
            } catch (IllegalArgumentException expected) {
                // Expected
            }
        }
    }

    @Test
    public void cast_rejectsDateTimeValues() {
        String[] invalidValues = {
            "2026-10-20T10:45:00",
            "2026-10-20T10:45:00-07:00"
        };

        for (String value : invalidValues) {
            try {
                new TimeData().cast(new UncastData(value));
                fail("Expected datetime to be rejected: " + value);
            } catch (IllegalArgumentException expected) {
                // Expected
            }
        }
    }

    @Test
    public void timeDoesNotChange_whenTimezoneChanges() {
        TimeData data = new TimeData().cast(new UncastData("10:45:00"));

        withTimeZone(TimeZone.getTimeZone("Asia/Tokyo"), () -> {
            assertEquals("10:45:00", data.uncast().getValue());
        });

        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            assertEquals("10:45:00", data.uncast().getValue());
        });
    }

    @Test
    public void timeIsTheSame_forBothOccurrencesOfRepeatedDstHour() {
        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            Date firstOccurrence = Date.from(
                Instant.parse("2026-11-01T08:30:00Z") // 01:30 PDT
            );

            Date secondOccurrence = Date.from(
                Instant.parse("2026-11-01T09:30:00Z") // 01:30 PST
            );

            TimeData first = new TimeData(firstOccurrence);
            TimeData second = new TimeData(secondOccurrence);

            assertEquals("01:30:00", first.uncast().getValue());
            assertEquals("01:30:00", second.uncast().getValue());
            assertEquals(first.uncast().getValue(), second.uncast().getValue());
        });
    }

    @Test
    public void timeIsPreserved_afterDeserialization()
        throws IOException, DeserializationException, XFormParser.ParseException {
        Scenario scenario = Scenario.init(html(
            head(
                title("Time deserialization"),
                model(
                    mainInstance(t("data id=\"time-deserialization\"",
                        t("time")
                    )),
                    bind("/data/time").type("time")
                )
            ),
            body(
                input("/data/time")
            )
        ));

        scenario.next();
        scenario.answer(new TimeData().cast(new UncastData("10:45:00")));

        assertThat(scenario.answerOf("/data/time"), is(timeAnswer("10:45:00")));

        Scenario deserialized = scenario.serializeAndDeserializeForm();

        assertThat(deserialized.answerOf("/data/time"), is(timeAnswer("10:45:00")));
    }

    @Test
    public void timeIsPreserved_afterInstanceDeserializationAcrossTimezoneChange() throws Exception {
        XFormsElement form = html(
            head(
                title("Time deserialization across timezones"),
                model(
                    mainInstance(t("data id=\"time-tz-deserialization\"",
                        t("time")
                    )),
                    bind("/data/time").type("time")
                )
            ),
            body(
                input("/data/time")
            )
        );

        Scenario scenario = Scenario.init(form);
        scenario.next();
        scenario.answer(new TimeData().cast(new UncastData("10:45:00")));

        byte[] serializedInstance = withTimeZone(TimeZone.getTimeZone("GMT-08:00"),
            scenario::serializeInstance
        );

        assertThat(new String(serializedInstance, StandardCharsets.UTF_8), containsString("<time>10:45:00</time>"));

        Scenario deserialized = withTimeZone(TimeZone.getTimeZone("GMT-07:00"), () ->
            Scenario.deserializeInstance(form, serializedInstance)
        );

        assertThat(deserialized.answerOf("/data/time"), is(timeAnswer("10:45:00")));
    }

    @Test
    public void legacyInstanceWithTimeWithOffset_isDeserializedWithoutTimezoneConversion() throws Exception {
        XFormsElement form = html(
            head(
                title("Legacy time default across timezone change"),
                model(
                    mainInstance(t("data id=\"time-dst-deserialization\"",
                        t("time")
                    )),
                    bind("/data/time").type("time")
                )
            ),
            body(input("/data/time"))
        );

        String legacyInstance =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<data id=\"time-dst-deserialization\">" +
                "<time>10:45:00.000-08:00</time>" +
                "</data>";

        Scenario.init(form);

        Scenario deserialized = withTimeZone(TimeZone.getTimeZone("GMT-07:00"), () -> Scenario.deserializeInstance(
                form,
                legacyInstance.getBytes(StandardCharsets.UTF_8)
            )
        );

        assertThat(deserialized.answerOf("/data/time"), is(timeAnswer("10:45:00")));

        byte[] serialized = deserialized.serializeInstance();

        assertThat(new String(serialized, StandardCharsets.UTF_8), containsString("<time>10:45:00</time>"));
    }

    @Test
    public void legacyTimeDefault_isPreservedAfterFormSerializationAcrossTimezoneChange()
        throws Exception {
        XFormsElement form = html(
            head(
                title("Legacy time default across timezone change"),
                model(
                    mainInstance(
                        t("data id=\"legacy-time-default\"",
                            t("time", "10:45:00.000-08:00")
                        )
                    ),
                    bind("/data/time").type("time")
                )
            ),
            body(input("/data/time"))
        );

        Scenario scenario = withTimeZone(TimeZone.getTimeZone("GMT+07:00"), () ->
            Scenario.init(form)
        );

        assertThat(scenario.answerOf("/data/time"), is(timeAnswer("10:45:00")));

        byte[] serializedForm = withTimeZone(TimeZone.getTimeZone("GMT+07:00"),
            scenario::serializeForm
        );

        Scenario restored = withTimeZone(TimeZone.getTimeZone("GMT-07:00"), () ->
            Scenario.deserializeForm(serializedForm)
        );

        assertThat(restored.answerOf("/data/time"), is(timeAnswer("10:45:00")));
    }

    @Test
    public void timeCalculate_usingNow_isPreservedAcrossTimezoneChange() throws Exception {
        XFormsElement form = html(
            head(
                title("Calculated time across timezone change"),
                model(
                    mainInstance(
                        t("data id=\"calculated-time\"",
                            t("time")
                        )
                    ),
                    bind("/data/time")
                        .type("time")
                        .calculate("once(format-date-time(now(), '%H:%M:%S'))")
                )
            ),
            body(input("/data/time"))
        );

        Scenario scenario = withTimeZone(TimeZone.getTimeZone("GMT+07:00"), () ->
            Scenario.init(form)
        );

        String original = scenario.answerOf("/data/time").uncast().getString();
        assertThat(original.matches("\\d{2}:\\d{2}:\\d{2}"), is(true));

        Scenario restoredInstance = withTimeZone(TimeZone.getTimeZone("GMT-07:00"), () ->
            scenario.serializeAndDeserializeInstance(form)
        );
        assertThat(restoredInstance.answerOf("/data/time"), is(timeAnswer(original)));

        byte[] serializedForm = withTimeZone(TimeZone.getTimeZone("GMT+07:00"),
            scenario::serializeForm
        );

        Scenario restoredForm = withTimeZone(TimeZone.getTimeZone("GMT-07:00"), () ->
            Scenario.deserializeForm(serializedForm)
        );
        assertThat(restoredForm.answerOf("/data/time"), is(timeAnswer(original)));
    }

    @Test
    public void timeSubtraction_withDecimalTime_isIndependentOfTimezone() throws Exception {
        XFormsElement form = html(
            head(
                title("Time subtraction"),
                model(
                    mainInstance(
                        t("data id=\"time-subtraction\"",
                            t("start", "09:15:00"),
                            t("end", "10:45:00"),
                            t("duration")
                        )
                    ),
                    bind("/data/start").type("time"),
                    bind("/data/end").type("time"),
                    bind("/data/duration")
                        .type("decimal")
                        .calculate(
                            "(decimal-time(/data/end) - decimal-time(/data/start)) * 24"
                        )
                )
            ),
            body(
                input("/data/start"),
                input("/data/end")
            )
        );

        Scenario scenario = withTimeZone(TimeZone.getTimeZone("GMT+07:00"), () ->
            Scenario.init(form)
        );
        assertThat(Double.parseDouble(scenario.answerOf("/data/duration").uncast().getString()),
            is(closeTo(1.5, 0.000001)));

        scenario = withTimeZone(TimeZone.getTimeZone("GMT-07:00"), () ->
            Scenario.init(form)
        );
        assertThat(Double.parseDouble(scenario.answerOf("/data/duration").uncast().getString()),
            is(closeTo(1.5, 0.000001)));
    }

    @Test
    public void timeSubtraction_withoutDecimalTime_isIndependentOfTimezone() throws Exception {
        XFormsElement form = html(
            head(
                title("Direct time subtraction"),
                model(
                    mainInstance(
                        t("data id=\"direct-time-subtraction\"",
                            t("start", "09:15:00"),
                            t("end", "10:45:00"),
                            t("duration")
                        )
                    ),
                    bind("/data/start").type("time"),
                    bind("/data/end").type("time"),
                    bind("/data/duration")
                        .type("decimal")
                        .calculate(
                            "(/data/end - /data/start) * 24"
                        )
                )
            ),
            body(
                input("/data/start"),
                input("/data/end")
            )
        );

        Scenario scenario = withTimeZone(TimeZone.getTimeZone("GMT+07:00"), () ->
            Scenario.init(form)
        );
        assertThat(Double.parseDouble(scenario.answerOf("/data/duration").uncast().getString()),
            is(closeTo(1.5, 0.000001)));

        scenario = withTimeZone(TimeZone.getTimeZone("GMT-07:00"), () ->
            Scenario.init(form)
        );
        assertThat(Double.parseDouble(scenario.answerOf("/data/duration").uncast().getString()),
            is(closeTo(1.5, 0.000001)));
    }

    @Test
    public void timeNumericConversions_areFractionalTime_acrossTimezones() throws Exception {
        XFormsElement form = html(
            head(
                title("Time numeric conversions"),
                model(
                    mainInstance(
                        t("data id=\"time-numeric\"",
                            t("time", "10:30:00"),
                            t("decimalTime")
                        )
                    ),
                    bind("/data/time").type("time"),
                    bind("/data/decimalTime").type("decimal").calculate("decimal-time(/data/time)")
                )
            ),
            body(input("/data/time"))
        );

        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            try {
                Scenario scenario = Scenario.init(form);
                assertThat(scenario.answerOf("/data/decimalTime").getDisplayText(), is("0.4375"));
            } catch (IOException | XFormParser.ParseException e) {
                throw new RuntimeException(e);
            }
        });

        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            try {
                Scenario scenario = Scenario.init(form);
                assertThat(scenario.answerOf("/data/decimalTime").getDisplayText(), is("0.4375"));
            } catch (IOException | XFormParser.ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Arithmetic between time and datetime is not semantically valid and ideally we would get a blank value to represent
    // invalid mixed-type subtraction. But that's a much bigger change so for now we accept at least a stable result.
    @Test
    public void subtractingDateTimeAndTime_usesFractionalDays() {
        XFormsElement form = html(
            head(
                title("DateTime and time subtraction"),
                model(
                    mainInstance(
                        t("data id=\"datetime-time-subtraction\"",
                            t("datetime", "1970-01-02T09:00:00Z"),
                            t("time", "10:30:00"),
                            t("dtMinusTime"),
                            t("timeMinusDt")
                        )
                    ),
                    bind("/data/datetime").type("dateTime"),
                    bind("/data/time").type("time"),
                    bind("/data/dtMinusTime").type("decimal").calculate("/data/datetime - /data/time"),
                    bind("/data/timeMinusDt").type("decimal").calculate("/data/time - /data/datetime")
                )
            ),
            body(
                input("/data/datetime"),
                input("/data/time")
            )
        );

        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            try {
                Scenario scenario = Scenario.init(form);

                assertThat(scenario.answerOf("/data/dtMinusTime").getDisplayText(), is("25567.9375"));
                assertThat(scenario.answerOf("/data/timeMinusDt").getDisplayText(), is("-25567.9375"));
            } catch (IOException | XFormParser.ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void castingTimeToString_doesNotIncludeSentinelDate() throws Exception {
        XFormsElement form = html(
            head(
                title("Time to string conversion"),
                model(
                    mainInstance(
                        t("data id=\"time-to-string\"",
                            t("time", "10:30:00"),
                            t("result")
                        )
                    ),
                    bind("/data/time").type("time"),
                    bind("/data/result").type("string")
                        .calculate("string(/data/time)")
                )
            ),
            body(input("/data/time"))
        );

        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            try {
                Scenario scenario = Scenario.init(form);
                assertThat(scenario.answerOf("/data/result").getDisplayText(), is("10:30:00"));
            } catch (IOException | XFormParser.ParseException e) {
                throw new RuntimeException(e);
            }
        });

        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            try {
                Scenario scenario = Scenario.init(form);
                assertThat(scenario.answerOf("/data/result").getDisplayText(), is("10:30:00"));
            } catch (IOException | XFormParser.ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
