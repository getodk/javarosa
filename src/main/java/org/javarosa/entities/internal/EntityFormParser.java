package org.javarosa.entities.internal;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityFormParser {

    private EntityFormParser() {

    }

    public static String parseDataset(TreeElement entity) {
        return entity.getAttributeValue(null, "dataset");
    }

    public static String parseLabel(TreeElement entity) {
        TreeElement labelElement = entity.getFirstChild("label");

        if (labelElement != null) {
            return (String) labelElement.getValue().getValue();
        } else {
            return "";
        }
    }

    public static String parseId(TreeElement entity) {
        return entity.getAttributeValue("", "id");
    }

    public static Integer parseBaseVersion(TreeElement entity) {
        return Integer.valueOf(entity.getAttributeValue("", "baseVersion"));
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

        if (create != null) {
            if (XPathFuncExpr.boolStr(create)) {
                return EntityAction.CREATE;
            }
        }

        if (update != null) {
            if (XPathFuncExpr.boolStr(update)) {
                return EntityAction.UPDATE;
            }
        }

        return null;
    }

    public enum EntityAction {
        CREATE,
        UPDATE
    }
}
