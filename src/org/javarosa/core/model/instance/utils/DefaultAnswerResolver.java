/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
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

package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.util.restorable.RestoreUtils;

/**
 * This class just wraps the default behaviour.
 *
 * Author: Meletis Margaritis
 * Date: 20/5/2013
 * Time: 12:50 πμ
 */
public class DefaultAnswerResolver implements IAnswerResolver {

  @Override
  public IAnswerData resolveAnswer(String textVal, TreeElement treeElement, FormDef formDef) {
      return RestoreUtils.xfFact.parseData(textVal, treeElement.getDataType(), treeElement.getRef(), formDef);
  }
}
