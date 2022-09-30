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
            TreeElement entity = meta.getFirstChild(ENTITIES_NAMESPACE, "entity");

            if (entity != null) {
                TreeElement create = entity.getFirstChild(ENTITIES_NAMESPACE, "create");

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
