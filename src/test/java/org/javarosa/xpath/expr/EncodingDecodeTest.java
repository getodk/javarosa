package org.javarosa.xpath.expr;

import static org.javarosa.xpath.expr.Encoding.BASE64;
import static org.javarosa.xpath.expr.Encoding.HEX;
import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class EncodingDecodeTest {
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
            {"Hexadecimal", HEX, "736f6d652074657874", "some text"},
            {"Hexadecimal (empty string)", HEX, "", ""},
            {"Base64", BASE64, "c29tZSB0ZXh0", "some text"},
            {"Base64 (empty string)", BASE64, "", ""},
        });
    }

    @Test
    public void decodes_byte_arrays() {
        assertEquals(expectedOutput, new String(encodingMethod.decode(input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
    }
}