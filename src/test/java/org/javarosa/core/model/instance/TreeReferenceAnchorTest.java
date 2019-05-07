package org.javarosa.core.model.instance;

import org.javarosa.xpath.XPathException;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.instance.TestHelpers.buildRef;
import static org.junit.Assert.assertThat;

public class TreeReferenceAnchorTest {

    @Test
    public void a_relative_ref_can_be_anchored_to_an_absolute_ref() {
        // 'baz'.anchor('/foo/bar') -> '/foo/bar/baz'
        TreeReference tr = buildRef("baz");
        TreeReference base = buildRef("/foo/bar");
        assertThat(tr.anchor(base), is(buildRef("/foo/bar/baz")));
    }

    @Test(expected = XPathException.class)
    public void anchoring_to_a_relative_ref_throws() {
        TreeReference tr = buildRef("some/relative/path");
        TreeReference trBase = buildRef("some/other/relative/path");
        tr.anchor(trBase);
    }

    @Test(expected = XPathException.class)
    public void anchoring_to_a_too_shallow_base_throws() {
        TreeReference tr = buildRef("../../../bar");
        TreeReference trBase = buildRef("/foo/bar");
        tr.anchor(trBase);
    }

    @Test
    public void otherwise_it_returns_an_absolute_ref() {
        TreeReference tr = buildRef("../baz");
        TreeReference trBase = buildRef("/foo/bar");
        assertThat(tr.anchor(trBase), is(buildRef("/foo/baz")));
    }

    @Test
    public void anchoring_an_absolute_ref_has_no_effect_on_it() {
        TreeReference tr = buildRef("/some/absolute/ref");
        TreeReference trBase = buildRef("/some/other/absolute/ref");
        assertThat(tr.anchor(trBase), is(tr));
    }
}