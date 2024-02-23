package org.javarosa.entities.internal;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.jetbrains.annotations.Nullable;

public class EntityFormParser {

    private EntityFormParser() {

    }

    @Nullable
    public static String parseFirstDatasetToCreate(TreeElement entity) {
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
    public static String parseFirstDatasetToUpdate(TreeElement entity) {
        if (entity != null) {
            String create = entity.getAttributeValue(null, "update");

            if (create != null) {
                if (XPathFuncExpr.boolStr(create)) {
                    return entity.getAttributeValue(null, "dataset");
                }
            }
        }

        return null;
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
}
