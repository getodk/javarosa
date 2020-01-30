/*
 * Copyright 2020 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.test.utils;

import java.util.Locale;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SystemHelper {
    public static void withTimeZone(TimeZone timeZone, Runnable block) {
        withTimeZone(timeZone, __ -> block.run());
    }

    public static void withTimeZone(TimeZone timeZone, Consumer<TimeZone> block) {
        TimeZone backupZone = TimeZone.getDefault();
        TimeZone.setDefault(timeZone);
        block.accept(timeZone);
        TimeZone.setDefault(backupZone);
    }

    public static void withLocale(Locale locale, Runnable block) {
        withLocale(locale, __ -> block.run());
    }

    public static void withLocale(Locale locale, Consumer<Locale> block) {
        Locale backupLocale = Locale.getDefault();
        Locale.setDefault(locale);
        block.accept(locale);
        Locale.setDefault(backupLocale);
    }

    public static void withLocaleAndTimeZone(Locale locale, TimeZone timeZone, Runnable block) {
        withLocaleAndTimeZone(locale, timeZone, (__, ___) -> block.run());
    }

    public static void withLocaleAndTimeZone(Locale locale, TimeZone timeZone, BiConsumer<Locale, TimeZone> block) {
        withLocale(locale, l -> withTimeZone(timeZone, () -> block.accept(l, timeZone)));
    }
}
