package org.javarosa.core.util;

import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class GeoInvalidDataTest {
    @Parameter public String formName;

    @Parameters(name = "{0}") public static Object[] data() {
        return "invalid-geoshape invalid-geotrace invalid-points distance_with_less_than_two_points".split(" ");
    }

    @Test
    public void invalidGeoDataCausesException() throws IOException {
        try {
            parse(r(formName + ".xml")).formDef.initialize(
                true, new InstanceInitializationFactory());
            fail("Expected XPathTypeMismatchException not thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof XPathTypeMismatchException);
        }
    }
}
