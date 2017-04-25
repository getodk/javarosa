package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;

public class ExternalDataInstance extends DataInstance {
    private final String srcLocation;

    public ExternalDataInstance(String srcLocation, String instanceid) {
        super(instanceid);
        this.srcLocation = srcLocation;
    }

    @Override
    public AbstractTreeElement getBase() {
        return null; // ToDo
    }

    @Override
    public AbstractTreeElement getRoot() {
        return new TreeElement(srcLocation); // ToDo
    }

    @Override
    public void initialize(InstanceInitializationFactory initializer, String instanceId) {
        throw new RuntimeException("Not implemented");
    }
}
