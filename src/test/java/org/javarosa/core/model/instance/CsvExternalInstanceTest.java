package org.javarosa.core.model.instance;

import org.junit.Test;

import java.io.IOException;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;

public class CsvExternalInstanceTest {

    @Test
    public void heading_has_no_extra_quotes() throws IOException {
        TreeElement root = CsvExternalInstance.parse("id", r("non_trivial.csv").toString());
        assertEquals("label", root.getChildAt(0).getChildAt(0).getName());
    }

    @Test
    public void value_has_no_extra_quotes() throws IOException {
        TreeElement root = CsvExternalInstance.parse("id", r("non_trivial.csv").toString());
        assertEquals("A", root.getChildAt(0).getChildAt(0).getValue().getValue());
    }

}
