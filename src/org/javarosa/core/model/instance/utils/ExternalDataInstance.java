package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;

public class ExternalDataInstance extends DataInstance {
    public ExternalDataInstance(String instanceid) {
        super(instanceid);
    }

    @Override
    public AbstractTreeElement getBase() {
        return null; // ToDo
    }

    @Override
    public AbstractTreeElement getRoot() {
        return null; // ToDo
    }

    @Override
    public void initialize(InstanceInitializationFactory initializer, String instanceId) {

    }
}
