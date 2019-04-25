package org.javarosa.core.model.instance;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.instance.TestHelpers.buildRef;
import static org.junit.Assert.assertThat;

/**
 * This test ensures that {@link TreeReference#anchor(TreeReference)} gives the same
 * result as {@link TreeReference#parent(TreeReference)} when none of the involved
 * refs start with a parent ref (../)
 */
@RunWith(Parameterized.class)
public class TreeReferenceAnchorHarnessTest {

    @Parameterized.Parameter(value = 0)
    public String tr;

    @Parameterized.Parameter(value = 1)
    public String base;

    @Parameterized.Parameters(name = "{0}.anchor({1})")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"foo", "/bar"},
            {"/foo", "/bar"}
        });
    }

    @Test
    public void parent_works_as_expected() {
        assertThat(
            buildRef(tr).anchor(buildRef(base)),
            is(buildRef(tr).parent(buildRef(base)))
        );
    }

}