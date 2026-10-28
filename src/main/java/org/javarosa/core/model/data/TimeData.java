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

package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a timezone-independent wall-clock time.
 *
 * Values are serialized as HH:mm:ss, without a date, timezone,or fractional seconds. When parsing legacy values containing
 * fractional seconds or timezone offsets, those components are discarded without adjusting the wall-clock time.
 */
public class TimeData implements IAnswerData {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private LocalTime time;

    public TimeData() {
    }

    public TimeData(@NotNull Date date) {
        setValue(date);
    }

    private TimeData(LocalTime time) {
        this.time = time.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public IAnswerData clone() {
        return new TimeData(time);
    }

    @Override
    public void setValue(@NotNull Object value) {
        if (value == null) {
            throw new NullPointerException("TimeData cannot be null");
        }

        time = ((Date) value).toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public @NotNull Object getValue() {
        return DateUtils.getDateForTime(time);
    }

    @Override
    public String getDisplayText() {
        return time.format(TIME_FORMAT);
    }

    @Override
    public UncastData uncast() {
        return new UncastData(time.format(TIME_FORMAT));
    }

    @Override
    public TimeData cast(UncastData data) {
        try {
            LocalTime parsed = LocalTime.from(
                DateTimeFormatter.ISO_TIME.parse(data.value)
            );

            return new TimeData(parsed);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid time: " + data.value, e);
        }
    }

    // Serialize the wall-clock time as seconds since midnight, without a date or
    // timezone. Prior to JavaRosa 7, was serialized as Date representing an instant.
    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        out.writeInt(time.toSecondOfDay());
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException {
        time = LocalTime.ofSecondOfDay(in.readInt());
    }
}
