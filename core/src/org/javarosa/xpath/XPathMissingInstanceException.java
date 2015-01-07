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

package org.javarosa.xpath;

public class XPathMissingInstanceException extends XPathException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6564322559371025199L;
	
	String instanceName;
	public XPathMissingInstanceException (String instanceName) {
		super("Instance " + instanceName + " is missing");
		this.instanceName = instanceName;
	}
	
	public XPathMissingInstanceException (String instanceName, String message) {
		super(message);
		this.instanceName = instanceName;
	}
}
