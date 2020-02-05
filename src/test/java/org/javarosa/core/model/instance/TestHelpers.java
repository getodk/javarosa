package org.javarosa.core.model.instance;

import org.javarosa.core.test.Scenario;

public class TestHelpers {
    public static TreeReference buildRef(String xpath) {
        return Scenario.getRef(xpath);
    }
}
