package org.javarosa.entities;

import kotlin.Pair;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.entities.internal.Entities;
import org.javarosa.entities.internal.EntityFormExtra;
import org.javarosa.entities.internal.EntityFormParser;
import org.javarosa.form.api.FormEntryFinalizationProcessor;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XPathReference;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class EntityFormFinalizationProcessor implements FormEntryFinalizationProcessor {

    @Override
    public void processForm(FormEntryModel formEntryModel) {
        FormDef formDef = formEntryModel.getForm();
        FormInstance mainInstance = formDef.getMainInstance();

        EntityFormExtra entityFormExtra = formDef.getExtras().get(EntityFormExtra.class);
        List<Pair<XPathReference, String>> saveTos = entityFormExtra.getSaveTos();

        TreeElement entityElement = EntityFormParser.getEntityElement(mainInstance);
        String createDataset = EntityFormParser.parseFirstDatasetToCreate(entityElement);
        String updateDataset = EntityFormParser.parseFirstDatasetToUpdate(entityElement);

        if (createDataset != null) {
            Entity entity = createEntity(entityElement, 1, createDataset, saveTos, mainInstance);
            formEntryModel.getExtras().put(new Entities(asList(entity)));
        } else if (updateDataset != null ){
            int baseVersion = EntityFormParser.parseBaseVersion(entityElement);
            int newVersion = baseVersion + 1;
            Entity entity = createEntity(entityElement, newVersion, updateDataset, saveTos, mainInstance);
            formEntryModel.getExtras().put(new Entities(asList(entity)));
        } else {
            formEntryModel.getExtras().put(new Entities(emptyList()));
        }
    }

    private Entity createEntity(TreeElement entityElement, int version, String dataset, List<Pair<XPathReference, String>> saveTos, FormInstance mainInstance) {
        List<Pair<String, String>> fields = saveTos.stream().map(saveTo -> {
            IDataReference reference = saveTo.getFirst();
            IAnswerData answerData = mainInstance.resolveReference(reference).getValue();

            if (answerData != null) {
                return new Pair<>(saveTo.getSecond(), answerData.getDisplayText());
            } else {
                return new Pair<>(saveTo.getSecond(), "");
            }
        }).collect(Collectors.toList());

        String id = EntityFormParser.parseId(entityElement);
        String label = EntityFormParser.parseLabel(entityElement);
        return new Entity(dataset, id, label, version, fields);
    }
}
