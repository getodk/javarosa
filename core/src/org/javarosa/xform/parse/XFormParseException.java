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
 * Exception thrown when an XForms Parsing error occurs.
 * 
 * @author Drew Roos
 *
 */
// Clayton Sims - Aug 18, 2008 : This doesn't actually seem
// to be a RuntimeException to me. Is there justification
// as to why it is?
public class XFormParseException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3852163642144479197L;
	
	Element element;
	
	public XFormParseException () { }
	
	public XFormParseException (String msg) {
		super(msg);
		element = null;
	}
	
	public XFormParseException (String msg, Element e) {
		super(msg);
		element = e;
	}
	
	public String getMessage() {
		if(element == null) {
			return super.getMessage();
		}
		return super.getMessage() + XFormParser.getVagueLocation(element);
	}
}
