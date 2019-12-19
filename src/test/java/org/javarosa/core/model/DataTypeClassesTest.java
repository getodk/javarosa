package org.javarosa.core.model;

import org.javarosa.core.model.data.SelectOneData;
import org.junit.Test;import org.junit.Before;
import static org.junit.Assert.assertEquals;

public class DataTypeClassesTest {
    @Test public void correctClassIsReturned() {
        assertEquals(SelectOneData.class, DataTypeClasses.classForType(DataType.CHOICE));
    }
}
