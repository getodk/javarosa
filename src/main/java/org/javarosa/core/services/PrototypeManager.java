package org.javarosa.core.services;

import org.javarosa.core.util.externalizable.CannotCreateObjectException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.util.ArrayList;
import java.util.List;

public class PrototypeManager {
    private static List<String> prototypes;
    private static PrototypeFactory staticDefault;

    public static void registerPrototype (String className) {
        getPrototypes().add(className);

        try {
            PrototypeFactory.getInstance(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new CannotCreateObjectException(className + ": not found");
        }
        rebuild();
    }

    public static void registerPrototypes (String[] classNames) {
        for (String className : classNames) {
            registerPrototype(className);
        }
    }

    public static List<String> getPrototypes () {
        if (prototypes == null) {
            prototypes = new ArrayList<>();
        }
        return prototypes;
    }

    public static PrototypeFactory getDefault() {
        if(staticDefault == null) {
            rebuild();
        }
        return staticDefault;
    }

    private static void rebuild() {
        if(staticDefault == null) {
            staticDefault = new PrototypeFactory(getPrototypes());
            return;
        }
        synchronized(staticDefault) {
            staticDefault = new PrototypeFactory(getPrototypes());
        }
    }
}
