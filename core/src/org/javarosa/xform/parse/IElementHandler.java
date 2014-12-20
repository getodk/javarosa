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

package org.javarosa.xform.parse;

import org.kxml2.kdom.Element;

/**
 * An IElementHandler is responsible for handling the parsing of a particular
 * XForms node.
 *  
 * @author Drew Roos
 *
 */
public interface IElementHandler {
	/*Object*/ void handle (XFormParser p, Element e, Object parent);
}
