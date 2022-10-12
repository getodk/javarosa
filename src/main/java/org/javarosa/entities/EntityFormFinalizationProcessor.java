package org.javarosa.entities;

import kotlin.Pair;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.entities.internal.Entities;
import org.javarosa.entities.internal.EntityFormParser;
import org.javarosa.entities.internal.EntityFormExtra;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryFinalizationProcessor;
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

        String dataset = EntityFormParser.parseFirstDatasetToCreate(mainInstance);
        if (dataset != null) {
            List<Pair<String, String>> fields = saveTos.stream().map(saveTo -> {
                IDataReference reference = saveTo.getFirst();
                IAnswerData answerData = mainInstance.resolveReference(reference).getValue();

                if (answerData != null) {
                    return new Pair<>(saveTo.getSecond(), answerData.getDisplayText());
                } else {
                    return new Pair<>(saveTo.getSecond(), "");
                }
            }).collect(Collectors.toList());

            formEntryModel.getExtras().put(new Entities(asList(new Entity(dataset, fields))));
        } else {
            formEntryModel.getExtras().put(new Entities(emptyList()));
        }
    }
}
