package org.javarosa.core.util;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.junit.Test;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xform.parse.FormParserHelper.parse;
import static org.junit.Assert.assertEquals;

public class GeoInvalidDataTest {
    @Test
     public void testDistanceWithLessThanTwoPoints() throws Exception {
         // Read the form definition
         final FormDef formDef = parse(r("distance_with_less_than_two_points.xml"));

         // Trigger all calculations
         formDef.initialize(true, new InstanceInitializationFactory());

         // Check the results. The data and expected results come from GeoUtilsTest.
         TreeElement root = formDef.getMainInstance().getRoot();

         IAnswerData area = root.getChildAt(2).getValue();
         assertEquals(0, area.getValue());

         area = root.getChildAt(3).getValue();
         assertEquals(0, area.getValue());
     }
}
