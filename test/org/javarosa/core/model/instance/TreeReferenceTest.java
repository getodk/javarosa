package org.javarosa.core.model.instance;

import org.javarosa.xpath.XPathException;
import org.junit.Test;

public class TreeReferenceTest {

    @Test(expected = XPathException.class)
    public void anchoring_to_a_relative_ref_throws() {
        TreeReference trBase = new TreeReference();
        TreeReference tr = new TreeReference();
        tr.anchor(trBase);
    }

}