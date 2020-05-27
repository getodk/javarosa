package org.javarosa.core.model.instance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.test.Scenario.getRef;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TreeReferenceEqualsTest {
    @Parameterized.Parameter(value = 0)
    public String testCase;

    @Parameterized.Parameter(value = 1)
    public String pathA;

    @Parameterized.Parameter(value = 2)
    public String pathB;

    @Parameterized.Parameter(value = 3)
    public boolean expectToBeEqual;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> testCases() {
        return Arrays.asList(new Object[][]{
            // region Equality across same or different instances

            {"same path in primary instance are equal", "/foo/bar", "/foo/bar", true},
            {"same path in secondary instance are equal", "instance('foo')/bar/baz", "instance('foo')/bar/baz", true},

            // Prior to JR 1.15, attempted to take the instance into account when comparing two TreeReferences but
            // this had unintended consequences. See https://github.com/getodk/javarosa/issues/475
            // {"same path in primary and secondary instance are not equal", "/foo/bar", "instance('foo')/foo/bar", false},
            // {"same path in secondary and primary instance are not equal", "instance('foo')/foo/bar", "/foo/bar", false},
            // {"same path in different secondary instances are not equal", "instance('foo')/foo/bar", "instance('bar')/foo/bar", false},

            {"different paths in primary instance are not equal", "/foo/bar", "/foo/bar/quux", false},
            {"different paths in secondary instance are not equal", "instance('foo')/foo/bar", "instance('foo')/foo/bar/quux", false}
            //endregion
        });
    }

    @Test
    public void treeReferenceEqualsTest() {
        assertThat(getRef(pathA).equals(getRef(pathB)), is(expectToBeEqual));
    }
}
