/*
 * Copyright (C) 2009 JavaRosa
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

import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.IInstanceVisitor;

/**
 * ITreeVisitor is a visitor interface for the elements of the
 * FormInstance tree elements. In the case of composite elements,
 * method dispatch for composite members occurs following dispatch
 * for the composing member.
 *
 * @author Clayton Sims
 *
 */
public interface ITreeVisitor extends IInstanceVisitor {
	public void visit(FormInstance tree);
	public void visit(AbstractTreeElement element);
}
