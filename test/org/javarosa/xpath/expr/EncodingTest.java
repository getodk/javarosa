/*
 * Copyright 2018 Nafundi
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
package org.javarosa.xpath.expr;


import static org.javarosa.xpath.expr.Encoding.BASE64;
import static org.javarosa.xpath.expr.Encoding.HEX;
import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class EncodingTest {
  @Parameterized.Parameter(value = 0)
  public String testName;

  @Parameterized.Parameter(value = 1)
  public Encoding encodingMethod;

  @Parameterized.Parameter(value = 2)
  public String input;

  @Parameterized.Parameter(value = 3)
  public String expectedOutput;

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"Hexadecimal", HEX, "some text", "736f6d652074657874"},
        {"Hexadecimal (empty string)", HEX, "", ""},
        {"Base64", BASE64, "some text", "c29tZSB0ZXh0"},
        {"Base64 (empty string)", BASE64, "", ""},
    });
  }

  @Test
  public void encodes_byte_arrays() throws UnsupportedEncodingException {
    assertEquals(expectedOutput, encodingMethod.encode(input.getBytes("UTF-8")));
  }
}