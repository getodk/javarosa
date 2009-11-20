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

package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * issues
 * 
 * * conflicting constructors... null(listwrapper())... confuses listwrapper as type, even though it contains val
 */

/**
 *  constructor guidelines: each child of this class should follow these rules with its constructors
 *  
 *  1) every constructor that sets 'val' should have a matching constructor for deserialization that
 *     leaves 'val' null
 *  2) every constructor that accepts an ExternalizableWrapper should also have a convenience constructor
 *     that accepts a Class, and wraps the Class in an ExtWrapBase (the identity wrapper)
 *  3) there must exist a null constructor for meta-deserialization (for applicable wrappers)
 *  4) be careful about properly disambiguating constructors
 */

public abstract class ExternalizableWrapper implements Externalizable {
	/* core data that is being wrapped; will be null when shell wrapper is created for deserialization */
	public Object val;
	
	/* create a copy of a wrapper, but with new val (but all the same type annotations */
	public abstract ExternalizableWrapper clone (Object val);
		
	/* deserialize the state of the externalizable wrapper */
	public abstract void metaReadExternal (DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException;
	
	/* serialize the state of the externalizable wrapper (type information only, not value) */
	public abstract void metaWriteExternal (DataOutputStream out) throws IOException;

	public abstract void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException;
	
	public abstract void writeExternal(DataOutputStream out) throws IOException;
			
	public Object baseValue () {
		Object baseVal = val;
		while (baseVal instanceof ExternalizableWrapper) {
			baseVal = ((ExternalizableWrapper)baseVal).val;
		}
		return baseVal;
	}
	
	public boolean isEmpty () {
		return baseValue() == null;
	}
}
