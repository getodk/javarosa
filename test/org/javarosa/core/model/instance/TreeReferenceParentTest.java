package org.javarosa.core.model.instance;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.model.instance.TestHelpers.buildRef;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TreeReferenceParentTest {

    @Parameterized.Parameter(value = 0)
    public String testCase;

    @Parameterized.Parameter(value = 1)
    public String tr;

    @Parameterized.Parameter(value = 2)
    public String base;

    @Parameterized.Parameter(value = 3)
    public String expectedRef;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"Parenting absolute refs doesn't change them", "/foo", "/bar", "/foo"},
            {"Parenting to an empty ref doesn't change them", "../foo", "", "../foo"},
            {"foo.parent(bar) gives bar/foo", "foo", "bar", "bar/foo"},
            {"foo.parent(../bar) gives ../bar/foo", "foo", "../bar", "../bar/foo"},
            // TODO review this. Shouldn't this resolve to "bar/foo"?
            {"../foo.parent(bar/baz) gives null", "../foo", "bar/baz", null},

        });
    }

    @Test
    public void parent_works_as_expected() {
        assertThat(
            buildRef(tr).parent(buildRef(base)),
            expectedRef == null
                ? is(nullValue(TreeReference.class))
                : is(buildRef(expectedRef))
        );
    }

}