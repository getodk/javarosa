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

/**
 *
 */
package org.javarosa.core.services.locale;

import org.javarosa.core.util.OrderedMap;
import org.javarosa.core.util.UnregisteredLocaleException;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Clayton Sims
 * @date May 26, 2009
 *
 */
public class TableLocaleSource implements LocaleDataSource {
	private OrderedMap<String, String> localeData; /*{ String -> String } */

	public TableLocaleSource() {
		localeData = new OrderedMap<String, String>();
	}

	public TableLocaleSource(OrderedMap<String, String> localeData) {
		this.localeData = localeData;
	}


	/**
	 * Set a text mapping for a single text handle for a given locale.
	 *
	 * @param textID Text handle. Must not be null. Need not be previously defined for this locale.
	 * @param text Localized text for this text handle and locale. Will overwrite any previous mapping, if one existed.
	 * If null, will remove any previous mapping for this text handle, if one existed.
	 * @throws UnregisteredLocaleException If locale is not defined or null.
	 * @throws NullPointerException if textID is null
	 */
	public void setLocaleMapping (String textID, String text) {
		if(textID == null) {
			throw new NullPointerException("Null textID when attempting to register " + text + " in locale table");
		}
		if (text == null) {
			localeData.remove(textID);
		} else {
			localeData.put(textID, text);
		}
	}

	/**
	 * Determine whether a locale has a mapping for a given text handle. Only tests the specified locale and form; does
	 * not fallback to any default locale or text form.
	 *
	 * @param textID Text handle.
	 * @return True if a mapping exists for the text handle in the given locale.
	 * @throws UnregisteredLocaleException If locale is not defined.
	 */
	public boolean hasMapping (String textID) {
		return (textID == null ? false : localeData.get(textID) != null);
	}


	public boolean equals(Object o) {
		if(!(o instanceof TableLocaleSource)) {
			return false;
		}
		TableLocaleSource l = (TableLocaleSource)o;
		return ExtUtil.equals(localeData, l.localeData);
	}

	public OrderedMap<String, String> getLocalizedText() {
		return localeData;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		localeData = (OrderedMap<String,String>)ExtUtil.read(in, new ExtWrapMap(String.class, String.class, ExtWrapMap.TYPE_ORDERED), pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapMap(localeData));
	}
}
