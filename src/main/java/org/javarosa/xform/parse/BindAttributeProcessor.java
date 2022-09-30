package org.javarosa.xform.parse;

import org.javarosa.core.model.DataBinding;

import java.util.Set;

public interface BindAttributeProcessor {

    Set<String> getUsedAttributes();

    void processBindingAttribute(String name, String value, DataBinding binding);
}
