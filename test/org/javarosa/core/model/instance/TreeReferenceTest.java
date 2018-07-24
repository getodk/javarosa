package org.javarosa.core.model.instance;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.javarosa.xpath.XPathException;
import org.junit.Test;

public class TreeReferenceTest {

    @Test(expected = XPathException.class)
    public void anchoring_to_a_relative_ref_throws() {
        TreeReference trBase = new TreeReference();
        TreeReference tr = new TreeReference();
        tr.anchor(trBase);
    }

    @Test
    public void anchoring_an_absolute_ref_has_no_effect_on_it() {
        TreeReference tr = new TreeReference();
        tr.setRefLevel(TreeReference.REF_ABSOLUTE);
        TreeReference trBase = new TreeReference();
        assertThat(tr.anchor(trBase), Matchers.is(tr));
    }
}