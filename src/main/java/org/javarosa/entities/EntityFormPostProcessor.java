package org.javarosa.entities;

import kotlin.Pair;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.entities.internal.Entities;
import org.javarosa.entities.internal.EntityFormExtra;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormPostProcessor;
import org.javarosa.model.xform.XPathReference;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class EntityFormPostProcessor implements FormPostProcessor {

    private static final String ENTITIES_NAMESPACE = "http://www.opendatakit.org/xforms/entities";

    @Override
    public void processForm(FormEntryModel formEntryModel) {
        FormDef formDef = formEntryModel.getForm();
        FormInstance mainInstance = formDef.getMainInstance();

        EntityFormExtra entityFormExtra = formDef.getExtras().get(EntityFormExtra.class);
        List<Pair<XPathReference, String>> saveTos = entityFormExtra.getSaveTos();

        String dataset = getDatasetToCreateWith(mainInstance);
        if (dataset != null) {
            List<Pair<String, String>> fields = saveTos.stream().map(saveTo -> {
                IDataReference reference = saveTo.getFirst();
                String answer = mainInstance.resolveReference(reference).getValue().getDisplayText();
                return new Pair<>(saveTo.getSecond(), answer);
            }).collect(Collectors.toList());

            formEntryModel.getExtras().put(new Entities(asList(new Entity(dataset, fields))));
        } else {
            formEntryModel.getExtras().put(new Entities(emptyList()));
        }
    }

    @Nullable
    private String getDatasetToCreateWith(FormInstance mainInstance) {
        TreeElement root = mainInstance.getRoot();
        TreeElement meta = root.getFirstChild("meta");
        if (meta != null) {
            TreeElement entity = meta.getFirstChild("entity");

            if (entity != null && entity.getNamespace().equals(ENTITIES_NAMESPACE)) {
                TreeElement create = entity.getFirstChild("create");

                if (create != null) {
                    if (create.isRelevant()) {
                        return entity.getAttributeValue(null, "dataset");
                    }
                }
            }
        }

        return null;
    }
}
