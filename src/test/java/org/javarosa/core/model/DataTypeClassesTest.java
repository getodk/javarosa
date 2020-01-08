package org.javarosa.core.model;

import static org.junit.Assert.assertEquals;

import org.javarosa.core.model.data.SelectOneData;
import org.junit.Test;

public class DataTypeClassesTest {
    @Test public void correctClassIsReturned() {
        assertEquals(SelectOneData.class, DataTypeClasses.classForType(DataType.CHOICE));
    }
}
