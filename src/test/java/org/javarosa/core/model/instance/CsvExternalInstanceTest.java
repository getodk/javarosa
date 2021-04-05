package org.javarosa.core.model.instance;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;

public class CsvExternalInstanceTest {
    private TreeElement root;

    @Test
    public void heading_has_no_extra_quotes() {
        assertEquals("label", root.getChildAt(0).getChildAt(0).getName());
    }

    @Test
    public void value_has_no_extra_quotes() {
        assertEquals("A", root.getChildAt(0).getChildAt(0).getValue().getValue());
    }

    @Test
    public void quoted_string_with_comma() {
        assertEquals("121 Main St, NE", root.getChildAt(6).getChildAt(0).getValue().getValue());
    }

    @Test
    public void missing_fields_replaced_with_spaces() {
        for (int fieldIndex = 1; fieldIndex < 2; fieldIndex++) {
            assertEquals("", root.getChildAt(5).getChildAt(fieldIndex).getValue().getValue());
        }
    }

    @Before
    public void setUp() throws IOException {
        root = CsvExternalInstance.parse("id", r("non_trivial.csv").toString());
    }
}
