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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.javarosa.core.PathConst;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.xform.util.XFormUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * Author: Meletis Margaritis
 * Date: 8/4/14
 * Time: 3:40 PM
 */
public class GeoShapeAreaTest {
    private static final Logger logger = LoggerFactory.getLogger(GeoShapeAreaTest.class);

  @Test public void testGeoShapeSupportForEnclosedArea() throws Exception {
    // Read the form definition
    String FORM_NAME = (new File(PathConst.getTestResourcePath(), "area.xml")).getAbsolutePath();
    InputStream is = null;
    FormDef formDef = null;
    is = new FileInputStream(new File(FORM_NAME));
    formDef = XFormUtils.getFormFromInputStream(is);

    // trigger all calculations
    formDef.initialize(true, new InstanceInitializationFactory());

    // get the calculated area
    IAnswerData areaResult = formDef.getMainInstance().getRoot().getChildAt(1).getValue();

    assertEquals(151452, (int) Math.rint((Double) areaResult.getValue()));
  }
}
