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

package org.javarosa.debug;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;

/**
 * Author: Meletis Margaritis
 * Date: 12/20/14
 * Time: 7:07 PM
 */
public class EvaluationResult {

  private final TreeReference affectedRef;
  private final Object value;

  public EvaluationResult(TreeReference affectedRef, Object value) {
    this.affectedRef = affectedRef;
    this.value = value;
  }

  public TreeReference getAffectedRef() {
    return affectedRef;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    if (getAffectedRef() == null) {
      return "";
    } else {
      StringBuilder sb = new StringBuilder();
      String refStr = getAffectedRef().toShortString();
      sb.append(refStr);
      if (value != null) {
        sb.append(" (");
        if (value instanceof IAnswerData) {
          sb.append(((IAnswerData) value).getDisplayText());
        } else {
          sb.append(String.valueOf(value));
        }
        sb.append(")");
      }
      return sb.toString();
    }
  }
}

