package org.javarosa.entities.internal;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.entities.EntityAction;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityFormParser {

    private EntityFormParser() {

    }

    public static String parseDataset(TreeElement entity) {
        return entity.getAttributeValue(null, "dataset");
    }

    @Nullable
    public static String parseLabel(TreeElement entity) {
        TreeElement labelElement = entity.getFirstChild("label");

        if (labelElement != null) {
            return (String) labelElement.getValue().getValue();
        } else {
            return null;
        }
    }

    public static String parseId(TreeElement entity) {
        return entity.getAttributeValue("", "id");
    }

    public static Integer parseBaseVersion(TreeElement entity) {
        try {
            return Integer.valueOf(entity.getAttributeValue("", "baseVersion"));
        } catch (NumberFormatException e) {
            return 0;
        }
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

    @Nullable
    public static EntityAction parseAction(@NotNull TreeElement entity) {
        String create = entity.getAttributeValue(null, "create");
        String update = entity.getAttributeValue(null, "update");

        if (update != null) {
            if (XPathFuncExpr.boolStr(update)) {
                return EntityAction.UPDATE;
            }
        }

        if (create != null) {
            if (XPathFuncExpr.boolStr(create)) {
                return EntityAction.CREATE;
            }
        }

        return null;
    }
}
