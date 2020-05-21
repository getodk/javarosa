package org.javarosa.core.model.data.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.javarosa.core.model.data.GeoPointData;
import org.junit.Test;

public class GeoPointDataTests {
    @Test
    public void getDisplayText_returnsSpaceSeparatedComponents() {
        GeoPointData point = new GeoPointData(new double[]{0, 1, 2, 3});

        assertThat(point.getDisplayText(), is("0.0 1.0 2.0 3.0"));
    }

    @Test
    public void getDisplayText_whenAllComponentsAreZero_returnsEmptyString() {
        GeoPointData zeroedOut = new GeoPointData(new double[]{0, 0, 0, 0});

        assertThat(zeroedOut.getDisplayText(), is(""));
    }
}
