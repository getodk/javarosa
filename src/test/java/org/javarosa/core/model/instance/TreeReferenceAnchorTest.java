package org.javarosa.core.model.instance;

import static org.hamcrest.Matchers.is;
import static org.javarosa.test.Scenario.getRef;
import static org.junit.Assert.assertThat;

import org.javarosa.xpath.XPathException;
import org.junit.Test;

public class TreeReferenceAnchorTest {

    @Test
    public void a_relative_ref_can_be_anchored_to_an_absolute_ref() {
        // 'baz'.anchor('/foo/bar') -> '/foo/bar/baz'
        TreeReference tr = getRef("baz");
        TreeReference base = getRef("/foo/bar");
        assertThat(tr.anchor(base), is(getRef("/foo/bar/baz")));
    }

    @Test(expected = XPathException.class)
    public void anchoring_to_a_relative_ref_throws() {
        TreeReference tr = getRef("some/relative/path");
        TreeReference trBase = getRef("some/other/relative/path");
        tr.anchor(trBase);
    }

    @Test(expected = XPathException.class)
    public void anchoring_to_a_too_shallow_base_throws() {
        TreeReference tr = getRef("../../../bar");
        TreeReference trBase = getRef("/foo/bar");
        tr.anchor(trBase);
    }

    @Test
    public void otherwise_it_returns_an_absolute_ref() {
        TreeReference tr = getRef("../baz");
        TreeReference trBase = getRef("/foo/bar");
        assertThat(tr.anchor(trBase), is(getRef("/foo/baz")));
    }

    @Test
    public void anchoring_an_absolute_ref_has_no_effect_on_it() {
        TreeReference tr = getRef("/some/absolute/ref");
        TreeReference trBase = getRef("/some/other/absolute/ref");
        assertThat(tr.anchor(trBase), is(tr));
    }
}
