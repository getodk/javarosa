package org.javarosa.entities.internal;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.jetbrains.annotations.Nullable;

public class EntityFormParser {

    private static final String ENTITIES_NAMESPACE = "http://www.opendatakit.org/xforms/entities";

    private EntityFormParser() {

    }

    @Nullable
    public static String parseFirstDatasetToCreate(FormInstance mainInstance) {
        TreeElement entity = getEntityElement(mainInstance);

        if (entity != null) {
            TreeElement create = entity.getFirstChild(ENTITIES_NAMESPACE, "create");

            if (create != null) {
                if (create.isRelevant()) {
                    return entity.getAttributeValue(null, "dataset");
                }
            }
        }

        return null;
    }

    @Nullable
    public static TreeElement getEntityElement(FormInstance mainInstance) {
        TreeElement root = mainInstance.getRoot();
        TreeElement meta = root.getFirstChild("meta");

        if (meta != null) {
            return meta.getFirstChild(ENTITIES_NAMESPACE, "entity");
        } else {
            return null;
        }
    }
}
