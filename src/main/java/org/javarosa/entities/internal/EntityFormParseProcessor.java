package org.javarosa.entities.internal;

import kotlin.Pair;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.BindingAttributeProcessor;
import org.javarosa.xform.parse.FormDefProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityFormParseProcessor implements BindingAttributeProcessor, FormDefProcessor {

    private final List<Pair<XPathReference, String>> saveTos = new ArrayList<>();

    @Override
    public Set<String> getUsedAttributes() {
        HashSet<String> attributes = new HashSet<>();
        attributes.add("saveto");

        return attributes;
    }

    @Override
    public void processBindingAttribute(String name, String value, DataBinding binding) {
        saveTos.add(new Pair<>((XPathReference) binding.getReference(), value));
    }

    @Override
    public void processFormDef(FormDef formDef) {
        EntityFormParseAttachment entityFormParseAttachment = new EntityFormParseAttachment(saveTos);
        formDef.putParseAttachment(entityFormParseAttachment);
    }
}
