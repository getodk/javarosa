package org.javarosa.entities.internal;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.jetbrains.annotations.Nullable;

public class EntityFormParser {

    private static final String ENTITIES_NAMESPACE = "http://www.opendatakit.org/xforms/entities";

    private EntityFormParser() {

    }

    @Nullable
    public static String parseFirstDatasetToCreate(FormInstance mainInstance) {
        TreeElement entity = getEntityElement(mainInstance);

        if (entity != null) {
            String create = entity.getAttributeValue(null, "create");

            if (create != null) {
                if (XPathFuncExpr.boolStr(create)) {
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
            return meta.getFirstChild("entity");
        } else {
            return null;
        }
    }
}
