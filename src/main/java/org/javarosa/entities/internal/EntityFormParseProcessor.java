package org.javarosa.entities.internal;

import kotlin.Pair;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
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
        String dataset = parseDataset(formDef.getMainInstance());
        EntityFormParseAttachment entityFormParseAttachment = new EntityFormParseAttachment(dataset, saveTos);
        formDef.putParseAttachment(entityFormParseAttachment);
    }

    private String parseDataset(FormInstance mainInstance) {
        TreeElement root = mainInstance.getRoot();
        List<TreeElement> meta = root.getChildrenWithName("meta");
        if (!meta.isEmpty()) {
            List<TreeElement> entity = meta.get(0).getChildrenWithName("entity");

            if (!entity.isEmpty()) {
                List<TreeElement> create = entity.get(0).getChildrenWithName("create");

                if (!create.isEmpty()) {
                    return entity.get(0).getAttributeValue(null, "dataset");
                }
            }
        }

        return null;
    }
}
