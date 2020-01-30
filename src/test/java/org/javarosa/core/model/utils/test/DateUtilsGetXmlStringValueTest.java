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

package org.javarosa.core.model.utils.test;

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.utils.DateUtils.getXMLStringValue;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import org.junit.Test;

public class DateUtilsGetXmlStringValueTest {
    /**
     * This test ensures that the Strings returned
     * by the getXMLStringValue function are in
     * the proper XML compliant format, which can be
     * parsed by LocalDate.parse()
     */
    @Test
    public void testGetXMLStringValueFormat() {
        LocalDateTime nowDateTime = LocalDateTime.now();
        Date nowDate = Date.from(nowDateTime.toInstant(OffsetDateTime.now().getOffset()));
        String nowXmlFormatterDate = getXMLStringValue(nowDate);
        assertThat(LocalDate.parse(nowXmlFormatterDate), is(nowDateTime.toLocalDate()));
    }

}
