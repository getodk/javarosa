package org.javarosa.core.model.data;

import org.javarosa.core.model.data.helper.Selection;

import java.util.List;

/**
 * This class is only for providing backwards compatibility after renaming SelectMultiData.class to MultipleItemsData
 */
public class SelectMultiData extends MultipleItemsData {

    /**
     * Empty Constructor, necessary for dynamic construction during deserialization. Shouldn't be used otherwise.
     */
    public SelectMultiData() {

    }

    public SelectMultiData(List<Selection> vs) {
        setValue(vs);
    }
}
