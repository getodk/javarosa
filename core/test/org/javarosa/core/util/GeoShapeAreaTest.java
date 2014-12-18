/*
 * Copyright (C) 2012-14 Dobility, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.util;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.xform.util.XFormUtils;

/**
 * Author: Meletis Margaritis
 * Date: 8/4/14
 * Time: 3:40 PM
 */
public class GeoShapeAreaTest extends TestCase {

  public GeoShapeAreaTest(String name, TestMethod rTestMethod) {
    super(name, rTestMethod);
  }

  public Test suite() {
    TestSuite aSuite = new TestSuite();

    aSuite.addTest(new GeoUtilsTest("GeoShapeArea Test", new TestMethod() {
      public void run (TestCase tc) {
        try {
          ((GeoShapeAreaTest)tc).testGeoShapeSupportForEnclosedArea();
        } catch (Exception e) {
          e.printStackTrace();

          fail(e.getMessage());
        }
      }
    }));

    return aSuite;
  }

  public void testGeoShapeSupportForEnclosedArea() throws Exception {
    // Read the form definition
    FormDef formDef = XFormUtils.getFormFromInputStream(getClass().getResourceAsStream("org/javarosa/core/util/area.xml"));

    // trigger all calculations
    formDef.initialize(true, new InstanceInitializationFactory());

    // get the calculated area
    IAnswerData areaResult = formDef.getMainInstance().getRoot().getChildAt(1).getValue();

    assertTrue((int) Math.rint((Double) areaResult.getValue()) == 151452);
  }
}
