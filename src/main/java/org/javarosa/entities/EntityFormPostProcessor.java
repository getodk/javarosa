package org.javarosa.entities;

import kotlin.Pair;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.entities.internal.EntitiesAttachment;
import org.javarosa.entities.internal.EntityFormParseAttachment;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormPostProcessor;
import org.javarosa.model.xform.XPathReference;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class EntityFormPostProcessor implements FormPostProcessor {

    @Override
    public void processForm(FormEntryModel formEntryModel) {
        FormDef formDef = formEntryModel.getForm();
        FormInstance mainInstance = formDef.getMainInstance();

        EntityFormParseAttachment entityFormParseAttachment = formDef.getParseAttachment(EntityFormParseAttachment.class);
        String dataset = entityFormParseAttachment.getDataset();
        List<Pair<XPathReference, String>> saveTos = entityFormParseAttachment.getSaveTos();

        if (dataset != null && createIsRelevant(mainInstance)) {
            List<Pair<String, String>> fields = saveTos.stream().map(saveTo -> {
                IDataReference reference = saveTo.getFirst();
                String answer = mainInstance.resolveReference(reference).getValue().getDisplayText();
                return new Pair<>(saveTo.getSecond(), answer);
            }).collect(Collectors.toList());

            formEntryModel.putAttachment(new EntitiesAttachment(asList(new Entity(dataset, fields))));
        } else {
            formEntryModel.putAttachment(new EntitiesAttachment(emptyList()));
        }
    }

    private boolean createIsRelevant(FormInstance mainInstance) {
        TreeElement root = mainInstance.getRoot();
        List<TreeElement> meta = root.getChildrenWithName("meta");
        if (!meta.isEmpty()) {
            List<TreeElement> entity = meta.get(0).getChildrenWithName("entity")
                .stream()
                .filter(node -> node.getNamespace().equals("http://www.opendatakit.org/xforms/entities"))
                .collect(Collectors.toList());

            if (!entity.isEmpty()) {
                List<TreeElement> create = entity.get(0).getChildrenWithName("create");

                if (!create.isEmpty()) {
                    return create.get(0).isRelevant();
                }
            }
        }

        return false;
    }
}
