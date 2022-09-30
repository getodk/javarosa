package org.javarosa.xform.parse;

import kotlin.Pair;
import org.javarosa.core.model.DataBinding;

import java.util.Set;

public interface BindAttributeProcessor {

    Set<Pair<String, String>> getUsedAttributes();

    void processBindingAttribute(String name, String value, DataBinding binding);
}
