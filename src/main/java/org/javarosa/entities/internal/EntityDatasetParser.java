package org.javarosa.entities.internal;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.jetbrains.annotations.Nullable;

public class EntityDatasetParser {

    private static final String ENTITIES_NAMESPACE = "http://www.opendatakit.org/xforms/entities";

    private EntityDatasetParser() {

    }

    @Nullable
    public static String parseFirstDatasetToCreate(FormInstance mainInstance) {
        TreeElement root = mainInstance.getRoot();
        TreeElement meta = root.getFirstChild("meta");
        if (meta != null) {
            TreeElement entity = meta.getFirstChild("entity");

            if (entity != null && entity.getNamespace().equals(ENTITIES_NAMESPACE)) {
                TreeElement create = entity.getFirstChild("create");

                if (create != null && create.getNamespace().equals(ENTITIES_NAMESPACE)) {
                    if (create.isRelevant()) {
                        return entity.getAttributeValue(null, "dataset");
                    }
                }
            }
        }

        return null;
    }
}
