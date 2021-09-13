package org.javarosa.core.model.instance;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class CsvExternalInstanceTest {
    private TreeElement commaSeparated;
    private TreeElement tabSeparated;

    @Before
    public void setUp() throws IOException {
        commaSeparated = CsvExternalInstance.parse("id", r("external-secondary-comma-complex.csv").toString());
        tabSeparated = CsvExternalInstance.parse("id", r("external-secondary-semicolon-complex.csv").toString());
    }

    @Test
    public void heading_has_no_extra_quotes() {
        assertEquals("label", commaSeparated.getChildAt(0).getChildAt(0).getName());
        assertEquals("label", tabSeparated.getChildAt(0).getChildAt(0).getName());
    }

    @Test
    public void value_has_no_extra_quotes() {
        assertEquals("A", commaSeparated.getChildAt(0).getChildAt(0).getValue().getValue());
        assertEquals("A", tabSeparated.getChildAt(0).getChildAt(0).getValue().getValue());
    }

    @Test
    public void quoted_string_with_comma() {
        assertEquals("121 Main St, NE", commaSeparated.getChildAt(6).getChildAt(0).getValue().getValue());
        assertEquals("121 Main St, NE", tabSeparated.getChildAt(6).getChildAt(0).getValue().getValue());
    }

    @Test
    public void quoted_string_with_semicolon() {
        assertEquals("text; more text", commaSeparated.getChildAt(7).getChildAt(0).getValue().getValue());
        assertEquals("text; more text", tabSeparated.getChildAt(7).getChildAt(0).getValue().getValue());
    }

    @Test
    public void missing_fields_replaced_with_spaces() {
        for (int fieldIndex = 1; fieldIndex < 2; fieldIndex++) {
            assertEquals("", commaSeparated.getChildAt(5).getChildAt(fieldIndex).getValue().getValue());
            assertEquals("", tabSeparated.getChildAt(5).getChildAt(fieldIndex).getValue().getValue());
        }
    }
}
